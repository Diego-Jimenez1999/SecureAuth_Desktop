package secureauth.dao.enterprise;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import secureauth.config.DatabaseConnection;
import secureauth.model.enterprise.Branch;
import secureauth.model.enterprise.Business;
import secureauth.model.enterprise.BusinessType;

/** DAO de bootstrap enterprise: tipos de negocio, negocios y sucursales. */
public class EnterpriseBootstrapDAO {

    public void ensureSchema() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection(); Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS business_type (
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  name VARCHAR(120) NOT NULL UNIQUE,
                  description VARCHAR(255),
                  icon VARCHAR(120),
                  status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS business (
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  business_type_id INT NOT NULL,
                  name VARCHAR(160) NOT NULL,
                  nit VARCHAR(80),
                  address VARCHAR(200),
                  phone VARCHAR(80),
                  logo VARCHAR(255),
                  primary_color VARCHAR(20),
                  secondary_color VARCHAR(20),
                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  CONSTRAINT fk_business_type FOREIGN KEY (business_type_id) REFERENCES business_type(id)
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS branches (
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  business_id INT NOT NULL,
                  branch_name VARCHAR(160) NOT NULL,
                  address VARCHAR(200),
                  phone VARCHAR(80),
                  status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
                  CONSTRAINT fk_branch_business FOREIGN KEY (business_id) REFERENCES business(id)
                )
                """);
        }
    }

    public void seedBusinessTypes() throws SQLException {
        String[] names = {"Hotel","Guardería","Baños","Consultorios","Restaurante","Veterinaria","Gimnasio","Clínica","Barbería","Tienda"};
        String sql = "INSERT INTO business_type(name, description, icon, status) VALUES(?,?,?,?) ON DUPLICATE KEY UPDATE description=VALUES(description)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String n : names) {
                ps.setString(1, n);
                ps.setString(2, "Rubro " + n + " para ERP multi-negocio");
                ps.setString(3, "icon-" + n.toLowerCase());
                ps.setString(4, "ACTIVE");
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public int ensureDefaultBusinessAndBranch() throws SQLException {
        int typeId = findBusinessTypeId("Guardería");
        if (typeId <= 0) {
            return 1;
        }

        int businessId = findBusinessIdByName("SecureAuth Demo");
        if (businessId <= 0) {
            String ins = "INSERT INTO business(business_type_id, name, nit, address, phone, primary_color, secondary_color) VALUES(?,?,?,?,?,?,?)";
            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, typeId);
                ps.setString(2, "SecureAuth Demo");
                ps.setString(3, "900000001-1");
                ps.setString(4, "Principal");
                ps.setString(5, "0000000000");
                ps.setString(6, "#1F2937");
                ps.setString(7, "#2563EB");
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) businessId = keys.getInt(1);
                }
            }
        }

        int branchId = findBranchIdByName(businessId, "Sucursal Principal");
        if (branchId <= 0) {
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO branches(business_id, branch_name, address, phone, status) VALUES(?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, businessId);
                ps.setString(2, "Sucursal Principal");
                ps.setString(3, "Principal");
                ps.setString(4, "0000000000");
                ps.setString(5, "ACTIVE");
                ps.executeUpdate();
            }
        }
        return businessId;
    }

    public List<BusinessType> findBusinessTypes() throws SQLException {
        List<BusinessType> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id,name,description,icon,status FROM business_type ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(new BusinessType(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)));
        }
        return list;
    }

    public List<Business> findBusinesses() throws SQLException {
        List<Business> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id,business_type_id,name,nit,address,phone,logo,primary_color,secondary_color FROM business ORDER BY name");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(new Business(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8), rs.getString(9)));
        }
        return list;
    }

    public List<Branch> findBranchesByBusiness(int businessId) throws SQLException {
        List<Branch> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id,business_id,branch_name,address,phone,status FROM branches WHERE business_id=? ORDER BY branch_name")) {
            ps.setInt(1, businessId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(new Branch(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6)));
            }
        }
        return list;
    }

    private int findBusinessTypeId(String name) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id FROM business_type WHERE name=?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : -1; }
        }
    }

    private int findBusinessIdByName(String name) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id FROM business WHERE name=?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : -1; }
        }
    }

    private int findBranchIdByName(int businessId, String name) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id FROM branches WHERE business_id=? AND branch_name=?")) {
            ps.setInt(1, businessId);
            ps.setString(2, name);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : -1; }
        }
    }
}
