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

/** DAO de inventario multi-sucursal adaptado al esquema ERP (tabla inventory). */
public class InventoryDAO {

    public void ensureSchema() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection(); Statement st = conn.createStatement()) {
            st.execute("""
                    CREATE TABLE IF NOT EXISTS inventory (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        business_id INT NOT NULL,
                        branch_id INT,
                        category_id INT,
                        codigo_barras VARCHAR(100) UNIQUE,
                        nombre_producto VARCHAR(150) NOT NULL,
                        descripcion TEXT,
                        marca VARCHAR(100),
                        unidad_medida VARCHAR(30),
                        stock_actual INT DEFAULT 0,
                        stock_minimo INT DEFAULT 5,
                        precio_compra DECIMAL(10,2),
                        precio_venta DECIMAL(10,2),
                        fecha_vencimiento DATE,
                        ubicacion VARCHAR(100),
                        imagen_path VARCHAR(255),
                        activo BOOLEAN DEFAULT TRUE,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                    )
                    """);
        }
    }

    public List<InventoryItem> findAll(int businessId, int branchId, String query) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT id,business_id,COALESCE(branch_id,?),codigo_barras,nombre_producto,
                       COALESCE((SELECT nombre FROM inventory_categories c WHERE c.id=i.category_id), 'General') AS categoria,
                       stock_actual,stock_minimo,COALESCE(marca,''),COALESCE(precio_compra,0),COALESCE(precio_venta,0),
                       IF(activo,'ACTIVO','INACTIVO')
                FROM inventory i
                WHERE business_id=? AND (branch_id=? OR branch_id IS NULL)
                """);
        boolean useFilter = query != null && !query.isBlank();
        if (useFilter) sql.append(" AND (codigo_barras LIKE ? OR nombre_producto LIKE ?)");
        sql.append(" ORDER BY nombre_producto");

        List<InventoryItem> out = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, branchId);
            ps.setInt(2, businessId);
            ps.setInt(3, branchId);
            if (useFilter) {
                String q = "%" + query.trim() + "%";
                ps.setString(4, q);
                ps.setString(5, q);
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
        String exists = "SELECT id FROM inventory WHERE business_id=? AND (branch_id=? OR branch_id IS NULL) AND codigo_barras=?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(exists)) {
            ps.setInt(1, item.businessId());
            ps.setInt(2, item.branchId());
            ps.setString(3, item.sku());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    update(item, rs.getInt(1));
                    return;
                }
            }
        }

        String insert = "INSERT INTO inventory (business_id, branch_id, codigo_barras, nombre_producto, marca, stock_actual, stock_minimo, precio_compra, precio_venta, activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(insert)) {
            ps.setInt(1, item.businessId());
            ps.setInt(2, item.branchId());
            ps.setString(3, item.sku());
            ps.setString(4, item.name());
            ps.setString(5, item.supplier());
            ps.setInt(6, item.stock());
            ps.setInt(7, item.minStock());
            ps.setDouble(8, item.cost());
            ps.setDouble(9, item.price());
            ps.executeUpdate();
        }
    }

    private void update(InventoryItem item, int id) throws SQLException {
        String sql = "UPDATE inventory SET nombre_producto=?, marca=?, stock_actual=?, stock_minimo=?, precio_compra=?, precio_venta=?, activo=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, item.name());
            ps.setString(2, item.supplier());
            ps.setInt(3, item.stock());
            ps.setInt(4, item.minStock());
            ps.setDouble(5, item.cost());
            ps.setDouble(6, item.price());
            ps.setBoolean(7, "ACTIVO".equalsIgnoreCase(item.status()));
            ps.setInt(8, id);
            ps.executeUpdate();
        }
    }
}
