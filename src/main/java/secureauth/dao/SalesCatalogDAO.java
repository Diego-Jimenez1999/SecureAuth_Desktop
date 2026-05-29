package secureauth.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import secureauth.config.DatabaseConnection;
import secureauth.config.SchemaInspector;
import secureauth.ui.sales.SalesServiceCatalog.CategoryEntry;
import secureauth.ui.sales.SalesServiceCatalog.ServiceItemEntry;

/** DAO del catálogo multi-sucursal de ventas y servicios. */
public class SalesCatalogDAO {

    private static final Logger LOGGER = Logger.getLogger(SalesCatalogDAO.class.getName());

    public void ensureSchema() {
        try (Connection conn = DatabaseConnection.getConnection(); Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS sales_categories (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    business_id INT NOT NULL,
                    branch_id INT NOT NULL,
                    category_name VARCHAR(120) NOT NULL,
                    subcategory_name VARCHAR(120) NOT NULL
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS sales_items (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    business_id INT NOT NULL,
                    branch_id INT NOT NULL,
                    category_name VARCHAR(120) NOT NULL,
                    subcategory_name VARCHAR(120) NOT NULL,
                    item_name VARCHAR(150) NOT NULL,
                    item_type VARCHAR(30) NOT NULL,
                    price DECIMAL(12,2) NOT NULL,
                    cost DECIMAL(12,2) NOT NULL,
                    gain DECIMAL(12,2) NOT NULL,
                    status_name VARCHAR(30) NOT NULL,
                    stock INT NULL
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS sales_item_sizes (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    sales_item_id INT NOT NULL,
                    size_label VARCHAR(80) NOT NULL,
                    size_price DECIMAL(12,2) NOT NULL,
                    CONSTRAINT fk_sales_size_item FOREIGN KEY (sales_item_id) REFERENCES sales_items(id) ON DELETE CASCADE
                )
                """);

            // Migración para instalaciones previas: Asegurar que las columnas business_id y branch_id existan
            if (!SchemaInspector.columnExists(conn, "sales_categories", "business_id")) {
                st.execute("ALTER TABLE sales_categories ADD COLUMN business_id INT NOT NULL DEFAULT 1");
            }
            if (!SchemaInspector.columnExists(conn, "sales_categories", "branch_id")) {
                st.execute("ALTER TABLE sales_categories ADD COLUMN branch_id INT NOT NULL DEFAULT 1");
            }
            if (!SchemaInspector.columnExists(conn, "sales_items", "business_id")) {
                st.execute("ALTER TABLE sales_items ADD COLUMN business_id INT NOT NULL DEFAULT 1");
            }
            if (!SchemaInspector.columnExists(conn, "sales_items", "branch_id")) {
                st.execute("ALTER TABLE sales_items ADD COLUMN branch_id INT NOT NULL DEFAULT 1");
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "No fue posible inicializar esquema de ventas/servicios", e);
        }
    }

    public void removeLegacyDomainData(int businessId, int branchId) {
        String deleteHotelCategories = "DELETE FROM sales_categories WHERE business_id=? AND branch_id=? AND category_name = 'Hotel'";
        String deleteBurgerItems = "DELETE FROM sales_items WHERE business_id=? AND branch_id=? AND item_name = 'Hamburguesa'";
        try (Connection conn = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(deleteBurgerItems)) {
                ps.setInt(1, businessId);
                ps.setInt(2, branchId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(deleteHotelCategories)) {
                ps.setInt(1, businessId);
                ps.setInt(2, branchId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "No fue posible limpiar datos legado de dominio", e);
        }
    }

    public List<CategoryEntry> findAllCategories(int businessId, int branchId) {
        List<CategoryEntry> list = new ArrayList<>();
        String sql = "SELECT id, category_name, subcategory_name FROM sales_categories WHERE business_id=? AND branch_id=? ORDER BY id";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, businessId);
            ps.setInt(2, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(new CategoryEntry(rs.getInt(1), rs.getString(2), rs.getString(3)));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "No fue posible cargar categorías", e);
        }
        return list;
    }

    public void syncInventoryCatalog(int businessId, int branchId) {
        String sql = """
                INSERT INTO sales_categories (business_id, branch_id, category_name, subcategory_name)
                SELECT DISTINCT i.business_id, i.branch_id, i.category_name, 'Inventario'
                FROM inventory_items i
                WHERE i.business_id = ?
                  AND i.branch_id = ?
                  AND NOT EXISTS (
                      SELECT 1
                      FROM sales_categories c
                      WHERE c.business_id = i.business_id
                        AND c.branch_id = i.branch_id
                        AND LOWER(c.category_name) = LOWER(i.category_name)
                        AND LOWER(c.subcategory_name) = 'inventario'
                  )
                """;
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (!SchemaInspector.tableExists(conn, "inventory_items")) {
                return;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, businessId);
                ps.setInt(2, branchId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "No fue posible sincronizar categorías de inventario hacia ventas", e);
        }
    }

    public List<ServiceItemEntry> findInventoryItems(int businessId, int branchId) {
        List<ServiceItemEntry> list = new ArrayList<>();
        String sql = """
                SELECT id, sku, item_name, category_name, stock, cost, price, status_name
                FROM inventory_items
                WHERE business_id = ?
                  AND branch_id = ?
                  AND stock > 0
                ORDER BY category_name, item_name
                """;
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (!SchemaInspector.tableExists(conn, "inventory_items")) {
                return list;
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, businessId);
                ps.setInt(2, branchId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        double price = rs.getDouble("price");
                        double cost = rs.getDouble("cost");
                        list.add(new ServiceItemEntry(
                                -rs.getInt("id"),
                                rs.getString("category_name"),
                                "Inventario",
                                rs.getString("item_name"),
                                "Producto",
                                price,
                                cost,
                                price - cost,
                                rs.getString("status_name"),
                                rs.getInt("stock"),
                                Map.of(),
                                rs.getInt("id"),
                                rs.getString("sku")));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "No fue posible cargar productos de inventario en ventas", e);
        }
        return list;
    }

    public List<ServiceItemEntry> findAllItems(int businessId, int branchId) {
        List<ServiceItemEntry> list = new ArrayList<>();
        Map<Integer, Map<String, Double>> sizesByItem = loadSizesByItem();

        String sql = "SELECT id, category_name, subcategory_name, item_name, item_type, price, cost, gain, status_name, stock FROM sales_items WHERE business_id=? AND branch_id=? ORDER BY id";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, businessId);
            ps.setInt(2, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    Integer stock = rs.getObject("stock") == null ? null : rs.getInt("stock");
                    list.add(new ServiceItemEntry(id, rs.getString("category_name"), rs.getString("subcategory_name"), rs.getString("item_name"), rs.getString("item_type"), rs.getDouble("price"), rs.getDouble("cost"), rs.getDouble("gain"), rs.getString("status_name"), stock, sizesByItem.getOrDefault(id, Map.of())));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "No fue posible cargar items", e);
        }
        return list;
    }

    public int insertCategory(int businessId, int branchId, String category, String subcategory) {
        String sql = "INSERT INTO sales_categories (business_id, branch_id, category_name, subcategory_name) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, businessId);
            ps.setInt(2, branchId);
            ps.setString(3, category);
            ps.setString(4, subcategory);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) { if (keys.next()) return keys.getInt(1); }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "No fue posible insertar categoría", e);
        }
        return -1;
    }

    public void deleteCategory(int categoryId) {
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM sales_categories WHERE id = ?")) {
            ps.setInt(1, categoryId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "No fue posible eliminar categoría", e);
        }
    }

    public int upsertItem(int businessId, int branchId, ServiceItemEntry item) {
        if (item.id() <= 0) return insertItem(businessId, branchId, item);
        updateItem(item);
        replaceSizes(item.id(), item.sizePrices());
        return item.id();
    }

    public void deleteItem(int itemId) {
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM sales_items WHERE id = ?")) {
            ps.setInt(1, itemId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "No fue posible eliminar item", e);
        }
    }

    private int insertItem(int businessId, int branchId, ServiceItemEntry item) {
        String sql = "INSERT INTO sales_items (business_id, branch_id, category_name, subcategory_name, item_name, item_type, price, cost, gain, status_name, stock) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, businessId);
            ps.setInt(2, branchId);
            bindItem(ps, item, 3);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int newId = keys.getInt(1);
                    replaceSizes(newId, item.sizePrices());
                    return newId;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "No fue posible insertar item", e);
        }
        return -1;
    }

    private void updateItem(ServiceItemEntry item) {
        String sql = "UPDATE sales_items SET category_name=?, subcategory_name=?, item_name=?, item_type=?, price=?, cost=?, gain=?, status_name=?, stock=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            bindItem(ps, item, 1);
            ps.setInt(10, item.id());
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "No fue posible actualizar item", e);
        }
    }

    private void bindItem(PreparedStatement ps, ServiceItemEntry item, int offset) throws SQLException {
        ps.setString(offset, item.category());
        ps.setString(offset + 1, item.subcategory());
        ps.setString(offset + 2, item.name());
        ps.setString(offset + 3, item.type());
        ps.setDouble(offset + 4, item.price());
        ps.setDouble(offset + 5, item.cost());
        ps.setDouble(offset + 6, item.gain());
        ps.setString(offset + 7, item.status());
        if (item.stock() == null) ps.setNull(offset + 8, java.sql.Types.INTEGER); else ps.setInt(offset + 8, item.stock());
    }

    private Map<Integer, Map<String, Double>> loadSizesByItem() {
        Map<Integer, Map<String, Double>> map = new HashMap<>();
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT sales_item_id, size_label, size_price FROM sales_item_sizes"); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) map.computeIfAbsent(rs.getInt(1), ignored -> new HashMap<>()).put(rs.getString(2), rs.getDouble(3));
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "No fue posible cargar precios por tamaño", e);
        }
        return map;
    }

    private void replaceSizes(int itemId, Map<String, Double> sizePrices) {
        deleteSizes(itemId);
        String sql = "INSERT INTO sales_item_sizes (sales_item_id, size_label, size_price) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Map.Entry<String, Double> entry : sizePrices.entrySet()) {
                ps.setInt(1, itemId);
                ps.setString(2, entry.getKey());
                ps.setDouble(3, entry.getValue());
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "No fue posible guardar precios por tamaño", e);
        }
    }

    private void deleteSizes(int itemId) {
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM sales_item_sizes WHERE sales_item_id = ?")) {
            ps.setInt(1, itemId);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "No fue posible limpiar precios por tamaño", e);
        }
    }
}
