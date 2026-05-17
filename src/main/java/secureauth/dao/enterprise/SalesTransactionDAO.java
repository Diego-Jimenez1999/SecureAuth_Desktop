package secureauth.dao.enterprise;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import secureauth.config.DatabaseConnection;

/** DAO de ventas POS sobre esquema ERP (tabla sales). */
public class SalesTransactionDAO {

    public void ensureSchema() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection(); Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS sales (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    business_id INT NOT NULL,
                    branch_id INT,
                    user_id INT NOT NULL,
                    owner_id INT,
                    subtotal DECIMAL(10,2) NOT NULL,
                    descuento DECIMAL(10,2) DEFAULT 0,
                    impuestos DECIMAL(10,2) DEFAULT 0,
                    total DECIMAL(10,2) NOT NULL,
                    metodo_pago_id INT,
                    estado ENUM('PENDIENTE','COMPLETADA','CANCELADA') DEFAULT 'COMPLETADA',
                    observaciones TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS payment_methods (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    nombre VARCHAR(50) NOT NULL UNIQUE,
                    activo BOOLEAN DEFAULT TRUE
                )
                """);

            st.execute("INSERT IGNORE INTO payment_methods(nombre, activo) VALUES ('Efectivo', TRUE), ('Tarjeta', TRUE), ('Transferencia', TRUE)");
        }
    }

    public void insertTx(int businessId, int branchId, int userId, double total, double gain, double tax, int items, String paymentMethod) throws SQLException {
        int paymentId = resolvePaymentMethod(paymentMethod);
        double subtotal = total - tax;
        String sql = "INSERT INTO sales(business_id, branch_id, user_id, subtotal, descuento, impuestos, total, metodo_pago_id, estado, observaciones) VALUES(?,?,?,?,0,?,?,?,'COMPLETADA',?)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, businessId);
            ps.setInt(2, branchId);
            ps.setInt(3, userId);
            ps.setDouble(4, subtotal);
            ps.setDouble(5, tax);
            ps.setDouble(6, total);
            ps.setInt(7, paymentId);
            ps.setString(8, "items=" + items + ";gain=" + gain);
            ps.executeUpdate();
        }
    }

    public double salesToday(int businessId, int branchId) throws SQLException {
        return singleDouble("SELECT COALESCE(SUM(total),0) FROM sales WHERE business_id=? AND (branch_id=? OR branch_id IS NULL) AND DATE(created_at)=CURRENT_DATE()", businessId, branchId);
    }

    public double salesMonth(int businessId, int branchId) throws SQLException {
        return singleDouble("SELECT COALESCE(SUM(total),0) FROM sales WHERE business_id=? AND (branch_id=? OR branch_id IS NULL) AND YEAR(created_at)=YEAR(CURRENT_DATE()) AND MONTH(created_at)=MONTH(CURRENT_DATE())", businessId, branchId);
    }

    public double gainMonth(int businessId, int branchId) throws SQLException {
        return singleDouble("SELECT COALESCE(SUM(total-impuestos-descuento),0) FROM sales WHERE business_id=? AND (branch_id=? OR branch_id IS NULL) AND YEAR(created_at)=YEAR(CURRENT_DATE()) AND MONTH(created_at)=MONTH(CURRENT_DATE())", businessId, branchId);
    }

    public int itemsMonth(int businessId, int branchId) throws SQLException {
        return (int) singleDouble("SELECT COALESCE(SUM(CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(observaciones,'items=',-1),';',1) AS UNSIGNED)),0) FROM sales WHERE business_id=? AND (branch_id=? OR branch_id IS NULL) AND YEAR(created_at)=YEAR(CURRENT_DATE()) AND MONTH(created_at)=MONTH(CURRENT_DATE())", businessId, branchId);
    }

    private int resolvePaymentMethod(String name) throws SQLException {
        String select = "SELECT id FROM payment_methods WHERE nombre=? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(select)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement("INSERT INTO payment_methods(nombre, activo) VALUES(?, TRUE)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 1;
            }
        }
    }

    private double singleDouble(String sql, int businessId, int branchId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, businessId);
            ps.setInt(2, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0d;
            }
        }
    }
}
