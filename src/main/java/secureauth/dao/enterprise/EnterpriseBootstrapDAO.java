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

/** Bootstrap enterprise adaptado al esquema nuevo ERP/POS multi-negocio. */
public class EnterpriseBootstrapDAO {

    public void ensureSchema() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection(); Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS business_types (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    nombre VARCHAR(100) NOT NULL UNIQUE,
                    descripcion TEXT,
                    icono VARCHAR(255),
                    activo BOOLEAN DEFAULT TRUE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS businesses (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    business_type_id INT NOT NULL,
                    nombre_negocio VARCHAR(150) NOT NULL,
                    descripcion TEXT,
                    telefono VARCHAR(30),
                    correo VARCHAR(100),
                    direccion VARCHAR(255),
                    logo_path VARCHAR(255),
                    activo BOOLEAN DEFAULT TRUE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT fk_businesses_business_type
                        FOREIGN KEY (business_type_id)
                        REFERENCES business_types(id)
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS branches (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    business_id INT NOT NULL,
                    nombre VARCHAR(150) NOT NULL,
                    direccion VARCHAR(255),
                    telefono VARCHAR(30),
                    principal BOOLEAN DEFAULT FALSE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    CONSTRAINT fk_branches_business
                        FOREIGN KEY (business_id)
                        REFERENCES businesses(id)
                        ON DELETE CASCADE
                )
                """);
        }
    }

    public void seedBusinessTypes() throws SQLException {
        String sql = "INSERT INTO business_types(nombre, descripcion, activo) VALUES(?,?,TRUE) ON DUPLICATE KEY UPDATE descripcion=VALUES(descripcion)";
        String[] types = {"Veterinaria", "Guardería", "Barbería", "Salsamentaria", "Hotel", "Restaurante", "Tienda"};

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String type : types) {
                ps.setString(1, type);
                ps.setString(2, "Rubro " + type + " en plataforma ERP/POS.");
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public int ensureDefaultBusinessAndBranch() throws SQLException {
        int typeId = findBusinessTypeId("Veterinaria");
        if (typeId <= 0) {
            return 1;
        }

        int businessId = findBusinessIdByName("SecureAuth ERP");
        if (businessId <= 0) {
            String insertBusiness = "INSERT INTO businesses(business_type_id, nombre_negocio, descripcion, telefono, correo, direccion, activo) VALUES(?,?,?,?,?,?,TRUE)";
            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(insertBusiness, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, typeId);
                ps.setString(2, "SecureAuth ERP");
                ps.setString(3, "Sistema ERP/POS multi negocio");
                ps.setString(4, "000000000");
                ps.setString(5, "admin@secureauth.com");
                ps.setString(6, "Principal");
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) businessId = keys.getInt(1);
                }
            }
        }

        int branchId = findBranchIdByName(businessId, "Sucursal Principal");
        if (branchId <= 0) {
            String insertBranch = "INSERT INTO branches(business_id, nombre, direccion, telefono, principal) VALUES(?,?,?,?,TRUE)";
            try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(insertBranch)) {
                ps.setInt(1, businessId);
                ps.setString(2, "Sucursal Principal");
                ps.setString(3, "Principal");
                ps.setString(4, "000000000");
                ps.executeUpdate();
            }
        }

        return businessId;
    }

    public List<BusinessType> findBusinessTypes() throws SQLException {
        List<BusinessType> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id,nombre,descripcion,icono,IF(activo,'ACTIVE','INACTIVE') FROM business_types ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new BusinessType(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5)));
            }
        }
        return list;
    }

    public List<Business> findBusinesses() throws SQLException {
        List<Business> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id,business_type_id,nombre_negocio,'',direccion,telefono,logo_path,'#DC2626','#111827' FROM businesses ORDER BY nombre_negocio");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Business(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8), rs.getString(9)));
            }
        }
        return list;
    }

    public List<Branch> findBranchesByBusiness(int businessId) throws SQLException {
        List<Branch> list = new ArrayList<>();
        String sql = "SELECT id,business_id,nombre,direccion,telefono,IF(principal,'ACTIVE','ACTIVE') FROM branches WHERE business_id=? ORDER BY principal DESC, nombre";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, businessId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Branch(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6)));
                }
            }
        }
        return list;
    }

    private int findBusinessTypeId(String name) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT id FROM business_types WHERE nombre=?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    private int findBusinessIdByName(String name) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT id FROM businesses WHERE nombre_negocio=?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    private int findBranchIdByName(int businessId, String name) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT id FROM branches WHERE business_id=? AND nombre=?")) {
            ps.setInt(1, businessId);
            ps.setString(2, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }
}
