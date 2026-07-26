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
import secureauth.model.enterprise.InventoryItem;

/** DAO de inventario multi-sucursal. */
public class InventoryDAO {

    public void ensureSchema() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection(); Statement st = conn.createStatement()) {
            st.execute("""
                    CREATE TABLE IF NOT EXISTS inventory_items (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        business_id INT NOT NULL,
                        branch_id INT NOT NULL,
                        sku VARCHAR(80) NOT NULL,
                        item_name VARCHAR(160) NOT NULL,
                        category_name VARCHAR(120) NOT NULL,
                        stock INT NOT NULL,
                        min_stock INT NOT NULL,
                        supplier VARCHAR(160),
                        cost DECIMAL(12,2) NOT NULL,
                        price DECIMAL(12,2) NOT NULL,
                        status_name VARCHAR(30) NOT NULL,
                        fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        UNIQUE KEY uk_inventory_scope (business_id, branch_id, sku)
                    )
                    """);
            if (!SchemaInspector.columnExists(conn, "inventory_items", "fecha_registro")) {
                st.execute("ALTER TABLE inventory_items ADD COLUMN fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP");
            }
        }
    }

    public List<InventoryItem> findAll(int businessId, int branchId, String query) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT id,business_id,branch_id,sku,item_name,category_name,stock,min_stock,supplier,cost,price,status_name FROM inventory_items WHERE business_id=? AND branch_id=?");
        boolean useFilter = query != null && !query.isBlank();
        if (useFilter) sql.append(" AND (sku LIKE ? OR item_name LIKE ? OR category_name LIKE ?)");
        sql.append(" ORDER BY item_name");

        List<InventoryItem> out = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, businessId);
            ps.setInt(2, branchId);
            if (useFilter) {
                String q = "%" + query.trim() + "%";
                ps.setString(3, q); ps.setString(4, q); ps.setString(5, q);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new InventoryItem(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getInt(7), rs.getInt(8), rs.getString(9), rs.getDouble(10), rs.getDouble(11), rs.getString(12)));
                }
            }
        }
        return out;
    }

    public InventorySummary loadSummary(int businessId, int branchId) throws SQLException {
        ensureSchema();
        String sql = """
                SELECT COUNT(*) AS item_count,
                       COALESCE(SUM(stock), 0) AS total_stock,
                       COALESCE(SUM(CASE WHEN stock <= min_stock THEN 1 ELSE 0 END), 0) AS low_stock_count
                FROM inventory_items
                WHERE business_id = ?
                  AND branch_id = ?
                  AND UPPER(status_name) <> 'INACTIVO'
                """;
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, businessId);
            ps.setInt(2, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new InventorySummary(rs.getInt("item_count"), rs.getInt("total_stock"),
                            rs.getInt("low_stock_count"));
                }
            }
        }
        return new InventorySummary(0, 0, 0);
    }

    public void upsert(InventoryItem item) throws SQLException {
        String sql = """
                INSERT INTO inventory_items(business_id, branch_id, sku, item_name, category_name, stock, min_stock, supplier, cost, price, status_name)
                VALUES(?,?,?,?,?,?,?,?,?,?,?)
                ON DUPLICATE KEY UPDATE item_name=VALUES(item_name), category_name=VALUES(category_name), stock=VALUES(stock), min_stock=VALUES(min_stock), supplier=VALUES(supplier), cost=VALUES(cost), price=VALUES(price), status_name=VALUES(status_name)
                """;
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, item.businessId()); ps.setInt(2, item.branchId()); ps.setString(3, item.sku());
            ps.setString(4, item.name()); ps.setString(5, item.category()); ps.setInt(6, item.stock());
            ps.setInt(7, item.minStock()); ps.setString(8, item.supplier()); ps.setDouble(9, item.cost());
            ps.setDouble(10, item.price()); ps.setString(11, item.status());
            ps.executeUpdate();
        }
    }

    public void decreaseStock(int businessId, int branchId, int inventoryId, int quantity) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            decreaseStock(conn, businessId, branchId, inventoryId, quantity);
        }
    }

    /**
     * Descuenta stock dentro de una transacción existente.
     *
     * @param conn conexión JDBC con autocommit controlado por el servicio
     * @param businessId negocio activo
     * @param branchId sucursal activa
     * @param inventoryId producto de inventario
     * @param quantity cantidad a descontar
     * @throws SQLException si el producto no existe o no hay stock suficiente
     */
    public void decreaseStock(Connection conn, int businessId, int branchId, int inventoryId, int quantity)
            throws SQLException {
        if (quantity <= 0) {
            throw new SQLException("La cantidad debe ser mayor que cero.");
        }
        String sql = """
                UPDATE inventory_items
                SET stock = stock - ?,
                    status_name = CASE WHEN stock - ? <= 0 THEN 'AGOTADO' ELSE status_name END
                WHERE id = ?
                  AND business_id = ?
                  AND branch_id = ?
                  AND stock >= ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, quantity);
            ps.setInt(3, inventoryId);
            ps.setInt(4, businessId);
            ps.setInt(5, branchId);
            ps.setInt(6, quantity);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new SQLException("No hay suficiente inventario disponible.");
            }
        }
    }

    /**
     * Valida existencia y stock disponible bloqueando la fila para la transacción.
     *
     * @param conn conexión transaccional
     * @param businessId negocio activo
     * @param branchId sucursal activa
     * @param inventoryId producto a validar
     * @param quantity cantidad requerida
     * @return true si hay stock suficiente
     * @throws SQLException si ocurre un error JDBC
     */
    public boolean hasStockForUpdate(Connection conn, int businessId, int branchId, int inventoryId, int quantity)
            throws SQLException {
        String sql = """
                SELECT stock
                FROM inventory_items
                WHERE id = ?
                  AND business_id = ?
                  AND branch_id = ?
                FOR UPDATE
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, inventoryId);
            ps.setInt(2, businessId);
            ps.setInt(3, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt("stock") >= quantity;
            }
        }
    }

    public InventoryConsumptionSource findConsumptionSourceForUpdate(Connection conn, int businessId, int branchId,
            int inventoryId) throws SQLException {
        String sql = """
                SELECT id, sku, item_name, stock, cost, price, status_name
                FROM inventory_items
                WHERE id = ?
                  AND business_id = ?
                  AND branch_id = ?
                FOR UPDATE
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, inventoryId);
            ps.setInt(2, businessId);
            ps.setInt(3, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                String status = rs.getString("status_name");
                boolean active = status == null || (!status.equalsIgnoreCase("INACTIVO")
                        && !status.equalsIgnoreCase("AGOTADO"));
                return new InventoryConsumptionSource(rs.getInt("id"), rs.getString("sku"), rs.getString("item_name"),
                        rs.getInt("stock"), rs.getDouble("cost"), rs.getDouble("price"), status, active);
            }
        }
    }

    public record InventoryConsumptionSource(int id, String sku, String name, int stock, double cost, double price,
                                             String status, boolean active) {
    }

    public record InventorySummary(int itemCount, int totalStock, int lowStockCount) {
    }
}
