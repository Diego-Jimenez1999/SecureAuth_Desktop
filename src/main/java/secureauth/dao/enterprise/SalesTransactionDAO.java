package secureauth.dao.enterprise;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import secureauth.config.DatabaseConnection;
import secureauth.config.SchemaInspector;
import secureauth.model.SaleItem;
import secureauth.model.Venta;

/** DAO de ventas POS para persistir totales y métricas por sucursal. */
public class SalesTransactionDAO {

    private static boolean schemaInitialized = false;

    public synchronized void ensureSchema() throws SQLException {
        if (schemaInitialized) {
            return;
        }
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
            if (!SchemaInspector.columnExists(conn, "sales_tx", "business_id")) {
                st.execute("ALTER TABLE sales_tx ADD COLUMN business_id INT NOT NULL DEFAULT 1");
            }
            if (!SchemaInspector.columnExists(conn, "sales_tx", "branch_id")) {
                st.execute("ALTER TABLE sales_tx ADD COLUMN branch_id INT NOT NULL DEFAULT 1");
            }
            if (!SchemaInspector.columnExists(conn, "sales_tx", "items_summary")) {
                st.execute("ALTER TABLE sales_tx ADD COLUMN items_summary VARCHAR(600) NULL");
            }
            if (!SchemaInspector.columnExists(conn, "sales_tx", "client_name")) {
                st.execute("ALTER TABLE sales_tx ADD COLUMN client_name VARCHAR(180) NULL");
            }
            if (!SchemaInspector.columnExists(conn, "sales_tx", "user_name")) {
                st.execute("ALTER TABLE sales_tx ADD COLUMN user_name VARCHAR(180) NULL");
            }

            try {
                st.execute("CREATE INDEX idx_sales_tx_biz_branch_date ON sales_tx(business_id, branch_id, created_at)");
            } catch (SQLException ignored) {}

            st.execute("""
                    CREATE TABLE IF NOT EXISTS ventas (
                        id_venta INT AUTO_INCREMENT PRIMARY KEY,
                        fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        cliente VARCHAR(180) NULL,
                        total DECIMAL(12,2) NOT NULL,
                        metodo_pago VARCHAR(40) NOT NULL,
                        usuario_vendedor VARCHAR(180) NULL
                    )
                    """);

            st.execute("""
                    CREATE TABLE IF NOT EXISTS detalle_venta (
                        id_detalle INT AUTO_INCREMENT PRIMARY KEY,
                        id_venta INT NOT NULL,
                        id_producto INT NULL,
                        cantidad INT NOT NULL,
                        precio_unitario DECIMAL(12,2) NOT NULL,
                        subtotal DECIMAL(12,2) NOT NULL,
                        CONSTRAINT fk_detalle_venta FOREIGN KEY (id_venta) REFERENCES ventas(id_venta)
                    )
                    """);
            schemaInitialized = true;
        } catch (SQLException e) {
            // Log as SEVERE as schema initialization is critical
            System.err.println("Error initializing SalesTransactionDAO schema: " + e.getMessage());
            throw e; // Re-throw to indicate a critical failure
        }
    }

    public void insertTx(int businessId, int branchId, double total, double gain, double tax, int items, String paymentMethod) throws SQLException {
        insertTx(businessId, branchId, total, gain, tax, items, paymentMethod, null, null, null);
    }

    public void insertTx(int businessId, int branchId, double total, double gain, double tax, int items,
            String paymentMethod, String itemsSummary, String clientName, String userName) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            insertTx(conn, businessId, branchId, total, gain, tax, items, paymentMethod, itemsSummary, clientName,
                    userName);
        }
    }

    /**
     * Inserta la transacción POS usada por reportes y dashboard.
     *
     * @param conn conexión transaccional
     * @param businessId negocio activo
     * @param branchId sucursal activa
     * @param total total final
     * @param gain ganancia estimada
     * @param tax impuesto calculado
     * @param items cantidad de unidades vendidas
     * @param paymentMethod método de pago
     * @param itemsSummary resumen textual
     * @param clientName cliente
     * @param userName vendedor
     * @throws SQLException si falla el insert
     */
    public void insertTx(Connection conn, int businessId, int branchId, double total, double gain, double tax, int items,
            String paymentMethod, String itemsSummary, String clientName, String userName) throws SQLException {
        String sql = "INSERT INTO sales_tx(business_id, branch_id, total, gain, tax, items_count, payment_method, items_summary, client_name, user_name) VALUES(?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
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

    /**
     * Inserta la cabecera de venta solicitada por el módulo profesional.
     *
     * @param conn conexión transaccional
     * @param venta venta validada
     * @return id generado de la venta
     * @throws SQLException si falla el insert
     */
    public int insertVenta(Connection conn, Venta venta) throws SQLException {
        String sql = "INSERT INTO ventas(fecha, cliente, total, metodo_pago, usuario_vendedor) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, Timestamp.valueOf(venta.getFecha()));
            ps.setString(2, venta.getCliente());
            ps.setDouble(3, venta.getTotal());
            ps.setString(4, venta.getMetodoPago());
            ps.setString(5, venta.getUsuarioVendedor());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("No fue posible obtener el identificador de la venta.");
    }

    /**
     * Inserta los detalles de una venta.
     *
     * @param conn conexión transaccional
     * @param saleId id de venta
     * @param items líneas del carrito
     * @throws SQLException si falla el batch
     */
    public void insertDetalles(Connection conn, int saleId, List<SaleItem> items) throws SQLException {
        String sql = """
                INSERT INTO detalle_venta(id_venta, id_producto, cantidad, precio_unitario, subtotal)
                VALUES(?,?,?,?,?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (SaleItem item : items) {
                ps.setInt(1, saleId);
                if (item.getInventoryItemId() == null) {
                    ps.setNull(2, java.sql.Types.INTEGER);
                } else {
                    ps.setInt(2, item.getInventoryItemId());
                }
                ps.setInt(3, item.getQuantity());
                ps.setDouble(4, item.getPrice());
                ps.setDouble(5, item.getSubtotal());
                ps.addBatch();
            }
            ps.executeBatch();
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

    public record SalesStats(
            double salesToday,
            double salesWeek,
            double salesMonth,
            double salesYear,
            double gainMonth,
            int itemsMonth
    ) {}

    public SalesStats loadDashboardStats(int businessId, int branchId) throws SQLException {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.sql.Timestamp tsTodayStart = java.sql.Timestamp.valueOf(today.atStartOfDay());
        java.sql.Timestamp tsTomorrowStart = java.sql.Timestamp.valueOf(today.plusDays(1).atStartOfDay());

        java.sql.Timestamp tsWeekStart = java.sql.Timestamp.valueOf(today.with(java.time.DayOfWeek.MONDAY).atStartOfDay());
        java.sql.Timestamp tsNextWeekStart = java.sql.Timestamp.valueOf(today.with(java.time.DayOfWeek.MONDAY).plusWeeks(1).atStartOfDay());

        java.sql.Timestamp tsMonthStart = java.sql.Timestamp.valueOf(today.withDayOfMonth(1).atStartOfDay());
        java.sql.Timestamp tsNextMonthStart = java.sql.Timestamp.valueOf(today.withDayOfMonth(1).plusMonths(1).atStartOfDay());

        java.sql.Timestamp tsYearStart = java.sql.Timestamp.valueOf(today.withDayOfYear(1).atStartOfDay());
        java.sql.Timestamp tsNextYearStart = java.sql.Timestamp.valueOf(today.withDayOfYear(1).plusYears(1).atStartOfDay());

        String sql = """
                SELECT
                  COALESCE(SUM(CASE WHEN created_at >= ? AND created_at < ? THEN total ELSE 0 END), 0) AS sales_today,
                  COALESCE(SUM(CASE WHEN created_at >= ? AND created_at < ? THEN total ELSE 0 END), 0) AS sales_week,
                  COALESCE(SUM(CASE WHEN created_at >= ? AND created_at < ? THEN total ELSE 0 END), 0) AS sales_month,
                  COALESCE(SUM(CASE WHEN created_at >= ? AND created_at < ? THEN total ELSE 0 END), 0) AS sales_year,
                  COALESCE(SUM(CASE WHEN created_at >= ? AND created_at < ? THEN gain ELSE 0 END), 0) AS gain_month,
                  COALESCE(SUM(CASE WHEN created_at >= ? AND created_at < ? THEN items_count ELSE 0 END), 0) AS items_month
                FROM sales_tx
                WHERE business_id=? AND branch_id=?
                """;

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, tsTodayStart);
            ps.setTimestamp(2, tsTomorrowStart);
            ps.setTimestamp(3, tsWeekStart);
            ps.setTimestamp(4, tsNextWeekStart);
            ps.setTimestamp(5, tsMonthStart);
            ps.setTimestamp(6, tsNextMonthStart);
            ps.setTimestamp(7, tsYearStart);
            ps.setTimestamp(8, tsNextYearStart);
            ps.setTimestamp(9, tsMonthStart);
            ps.setTimestamp(10, tsNextMonthStart);
            ps.setTimestamp(11, tsMonthStart);
            ps.setTimestamp(12, tsNextMonthStart);
            ps.setInt(13, businessId);
            ps.setInt(14, branchId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new SalesStats(
                            rs.getDouble(1),
                            rs.getDouble(2),
                            rs.getDouble(3),
                            rs.getDouble(4),
                            rs.getDouble(5),
                            rs.getInt(6)
                    );
                }
            }
        }
        return new SalesStats(0, 0, 0, 0, 0, 0);
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
