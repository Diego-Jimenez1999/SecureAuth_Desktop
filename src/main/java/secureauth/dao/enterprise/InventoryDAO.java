package secureauth.dao.enterprise;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import secureauth.config.DatabaseConnection;
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
                        UNIQUE KEY uk_inventory_scope (business_id, branch_id, sku)
                    )
                    """);
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
}
