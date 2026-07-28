package secureauth.dao.enterprise;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import secureauth.config.DatabaseConnection;
import secureauth.config.SchemaInspector;
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

            st.execute("""
                CREATE TABLE IF NOT EXISTS roles (
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  nombre_rol VARCHAR(80) NOT NULL UNIQUE
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS users (
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  email VARCHAR(160) NOT NULL UNIQUE,
                  password VARCHAR(100) NOT NULL,
                  nombre VARCHAR(120) NOT NULL,
                  apellido VARCHAR(120) NOT NULL,
                  fecha_nacimiento DATE NULL,
                  genero VARCHAR(20),
                  rol_id INT NOT NULL DEFAULT 3,
                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  CONSTRAINT fk_users_roles FOREIGN KEY (rol_id) REFERENCES roles(id)
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS owners (
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  nombre_completo VARCHAR(180) NOT NULL,
                  telefono VARCHAR(60),
                  correo VARCHAR(160),
                  direccion VARCHAR(220),
                  fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS pets (
                  id INT AUTO_INCREMENT PRIMARY KEY,
                  business_id INT NOT NULL,
                  owner_id INT NOT NULL,
                  nombre_mascota VARCHAR(140) NOT NULL,
                  raza VARCHAR(120) NOT NULL,
                  edad VARCHAR(60),
                  peso DECIMAL(8,2) NOT NULL,
                  sexo VARCHAR(20) NOT NULL,
                  frecuencia_alimentacion VARCHAR(180),
                  tipo_alimento VARCHAR(180),
                  estado_salud VARCHAR(80),
                  vacunas VARCHAR(300),
                  cuidados_especiales TEXT,
                  notas_adicionales TEXT,
                  imagen_path VARCHAR(500),
                  fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  CONSTRAINT fk_pets_owner_bootstrap FOREIGN KEY (owner_id) REFERENCES owners(id)
                )
                """);

            if (!SchemaInspector.columnExists(conn, "users", "created_at")) {
                st.execute("ALTER TABLE users ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP");
            }
            st.execute("ALTER TABLE users MODIFY COLUMN password VARCHAR(100) NOT NULL");
            if (!SchemaInspector.columnExists(conn, "owners", "fecha_registro")) {
                st.execute("ALTER TABLE owners ADD COLUMN fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP");
            }
            if (SchemaInspector.columnExists(conn, "pets", "sexo")) {
                st.execute("ALTER TABLE pets MODIFY COLUMN sexo VARCHAR(20) NOT NULL");
            }

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

        try (Connection conn = DatabaseConnection.getConnection(); Statement st = conn.createStatement()) {
            st.execute("INSERT INTO roles(id, nombre_rol) VALUES (1,'Administrador') ON DUPLICATE KEY UPDATE nombre_rol=VALUES(nombre_rol)");
            st.execute("INSERT INTO roles(id, nombre_rol) VALUES (2,'Supervisor') ON DUPLICATE KEY UPDATE nombre_rol=VALUES(nombre_rol)");
            st.execute("INSERT INTO roles(id, nombre_rol) VALUES (3,'Recepcionista') ON DUPLICATE KEY UPDATE nombre_rol=VALUES(nombre_rol)");
            st.execute("INSERT INTO roles(id, nombre_rol) VALUES (4,'Médico') ON DUPLICATE KEY UPDATE nombre_rol=VALUES(nombre_rol)");
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

    /**
     * Prueba si la conexión con la base de datos MySQL es válida.
     */
    public boolean testConnection() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return conn.isValid(3);
        }
    }

    /**
     * Ejecuta la optimización de las tablas de base de datos.
     */
    public void optimizeTables() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection(); Statement st = conn.createStatement()) {
            st.execute("OPTIMIZE TABLE appointments, ventas, detalle_venta, actividad_reciente, citas_servicio");
        }
    }

}
