package secureauth.dao.enterprise;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import secureauth.config.DatabaseConnection;

/** DAO de ventas POS para persistir totales y métricas por sucursal. */
public class SalesTransactionDAO {

    public void ensureSchema() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection(); Statement st = conn.createStatement()) {
            st.execute("""
                    CREATE TABLE IF NOT EXISTS sales_tx (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        business_id INT NOT NULL,
                        branch_id INT NOT NULL,
                        total DECIMAL(12,2) NOT NULL,
                        gain DECIMAL(12,2) NOT NULL,
                        tax DECIMAL(12,2) NOT NULL,
                        items_count INT NOT NULL,
                        payment_method VARCHAR(30) NOT NULL,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                    )
                    """);

            // Migración para instalaciones previas: Asegurar que las columnas business_id y branch_id existan
            // If the table existed before these columns were added to the CREATE TABLE statement
            if (!columnExists(conn, "sales_tx", "business_id")) {
                st.execute("ALTER TABLE sales_tx ADD COLUMN business_id INT NOT NULL DEFAULT 1");
            }
            if (!columnExists(conn, "sales_tx", "branch_id")) {
                st.execute("ALTER TABLE sales_tx ADD COLUMN branch_id INT NOT NULL DEFAULT 1");
            }
            if (!columnExists(conn, "sales_tx", "items_summary")) {
                st.execute("ALTER TABLE sales_tx ADD COLUMN items_summary VARCHAR(600) NULL");
            }
            if (!columnExists(conn, "sales_tx", "client_name")) {
                st.execute("ALTER TABLE sales_tx ADD COLUMN client_name VARCHAR(180) NULL");
            }
            if (!columnExists(conn, "sales_tx", "user_name")) {
                st.execute("ALTER TABLE sales_tx ADD COLUMN user_name VARCHAR(180) NULL");
            }

        } catch (SQLException e) {
            // Log as SEVERE as schema initialization is critical
            System.err.println("Error initializing SalesTransactionDAO schema: " + e.getMessage());
            throw e; // Re-throw to indicate a critical failure
        }
    }

    private boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(null, null, tableName, columnName)) {
            return rs.next();
        }
    }

    public void insertTx(int businessId, int branchId, double total, double gain, double tax, int items, String paymentMethod) throws SQLException {
        insertTx(businessId, branchId, total, gain, tax, items, paymentMethod, null, null, null);
    }

    public void insertTx(int businessId, int branchId, double total, double gain, double tax, int items,
            String paymentMethod, String itemsSummary, String clientName, String userName) throws SQLException {
        String sql = "INSERT INTO sales_tx(business_id, branch_id, total, gain, tax, items_count, payment_method, items_summary, client_name, user_name) VALUES(?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, businessId);
            ps.setInt(2, branchId);
            ps.setDouble(3, total);
            ps.setDouble(4, gain);
            ps.setDouble(5, tax);
            ps.setInt(6, items);
            ps.setString(7, paymentMethod);
            ps.setString(8, itemsSummary);
            ps.setString(9, clientName);
            ps.setString(10, userName);
            ps.executeUpdate();
        }
    }

    public List<SaleReportRow> recentSales(int businessId, int branchId, int limit) throws SQLException {
        String sql = """
                SELECT id, created_at, COALESCE(user_name,''), COALESCE(client_name,''), total,
                       COALESCE(items_summary,''), items_count, payment_method
                FROM sales_tx
                WHERE business_id=? AND branch_id=?
                ORDER BY created_at DESC
                LIMIT ?
                """;
        List<SaleReportRow> rows = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, businessId);
            ps.setInt(2, branchId);
            ps.setInt(3, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new SaleReportRow(rs.getInt(1), rs.getTimestamp(2).toLocalDateTime(), rs.getString(3),
                            rs.getString(4), rs.getDouble(5), rs.getString(6), rs.getInt(7), rs.getString(8)));
                }
            }
        }
        return rows;
    }

    public double salesToday(int businessId, int branchId) throws SQLException {
        return singleDouble("SELECT COALESCE(SUM(total),0) FROM sales_tx WHERE business_id=? AND branch_id=? AND DATE(created_at)=CURRENT_DATE()", businessId, branchId);
    }

    public double salesMonth(int businessId, int branchId) throws SQLException {
        return singleDouble("SELECT COALESCE(SUM(total),0) FROM sales_tx WHERE business_id=? AND branch_id=? AND YEAR(created_at)=YEAR(CURRENT_DATE()) AND MONTH(created_at)=MONTH(CURRENT_DATE())", businessId, branchId);
    }

    public double gainMonth(int businessId, int branchId) throws SQLException {
        return singleDouble("SELECT COALESCE(SUM(gain),0) FROM sales_tx WHERE business_id=? AND branch_id=? AND YEAR(created_at)=YEAR(CURRENT_DATE()) AND MONTH(created_at)=MONTH(CURRENT_DATE())", businessId, branchId);
    }

    public int itemsMonth(int businessId, int branchId) throws SQLException {
        return (int) singleDouble("SELECT COALESCE(SUM(items_count),0) FROM sales_tx WHERE business_id=? AND branch_id=? AND YEAR(created_at)=YEAR(CURRENT_DATE()) AND MONTH(created_at)=MONTH(CURRENT_DATE())", businessId, branchId);
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

    public record SaleReportRow(int id, java.time.LocalDateTime createdAt, String userName, String clientName,
                                double total, String itemsSummary, int itemsCount, String paymentMethod) { }
}
