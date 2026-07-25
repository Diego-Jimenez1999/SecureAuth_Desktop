package secureauth.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import secureauth.config.DatabaseConnection;
import secureauth.config.SchemaInspector;
import secureauth.model.Owner;

/**
 * DAO (Data Access Object) para gestionar las consultas y operaciones
 * de los dueños o clientes directamente en la base de datos (tabla owners).
 *
 * @author Diego Alexander Gaviria Jimenez
 */
public class OwnerDAO {

    private static boolean schemaInitialized = false;

    /**
     * Garantiza que la tabla de dueños exista antes de consultar o insertar.
     */
    public void ensureSchema() {
        synchronized (OwnerDAO.class) {
            if (schemaInitialized) {
                return;
            }
        }
        final String sql = """
                CREATE TABLE IF NOT EXISTS owners (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    nombre_completo VARCHAR(180) NOT NULL,
                    telefono VARCHAR(60),
                    correo VARCHAR(160),
                    direccion VARCHAR(220),
                    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {
            st.execute(sql);
            try {
                st.execute("CREATE INDEX idx_owners_fecha_registro ON owners(fecha_registro)");
            } catch (SQLException ignored) {}
            migrateOwnersTable(conn, st);
            synchronized (OwnerDAO.class) {
                schemaInitialized = true;
            }
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo inicializar la tabla de dueños: " + e.getMessage(), e);
        }
    }

    /**
     * Recupera todos los dueños registrados en la base de datos.
     *
     * @return Una lista con todos los objetos Owner, ordenada alfabéticamente por nombre.
     */
    public List<Owner> findAll() {
        ensureSchema();
        final String sql = "SELECT id, nombre_completo, telefono, correo, direccion FROM owners ORDER BY nombre_completo";
        List<Owner> owners = new ArrayList<>();

        // Usamos try-with-resources para que las conexiones se cierren solas
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                owners.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error consultando la lista de dueños.", e);
        }
        return owners;
    }

    /**
     * Busca un dueño específico usando su identificador único (id).
     *
     * @param id El número de identificación del dueño en la base de datos.
     * @return El objeto Owner si lo encuentra, o null si no existe.
     */
    public Owner findById(int id) {
        ensureSchema();
        final String sql = "SELECT id, nombre_completo, telefono, correo, direccion FROM owners WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando al dueño por su id.", e);
        }
        return null;
    }

    /**
     * Busca dueños por id, nombre, teléfono, correo o dirección.
     *
     * @param query texto de búsqueda
     * @return lista filtrada
     */
    public List<Owner> search(String query) {
        ensureSchema();
        if (query == null || query.trim().isEmpty()) {
            return findAll();
        }

        final String sql = """
                SELECT id, nombre_completo, telefono, correo, direccion
                FROM owners
                WHERE CAST(id AS CHAR) LIKE ?
                   OR nombre_completo LIKE ?
                   OR telefono LIKE ?
                   OR correo LIKE ?
                   OR direccion LIKE ?
                ORDER BY nombre_completo
                """;
        List<Owner> owners = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String filter = "%" + query.trim() + "%";
            ps.setString(1, filter);
            ps.setString(2, filter);
            ps.setString(3, filter);
            ps.setString(4, filter);
            ps.setString(5, filter);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    owners.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error buscando dueños.", e);
        }
        return owners;
    }

    /**
     * Guarda un nuevo dueño/cliente en la base de datos.
     *
     * @param owner El objeto con los datos del cliente que queremos registrar.
     */
    public Owner insert(Owner owner) {
        ensureSchema();
        final String sql = "INSERT INTO owners (nombre_completo, telefono, correo, direccion) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, owner.getNombreCompleto());
            ps.setString(2, owner.getTelefono());
            ps.setString(3, owner.getCorreo());
            ps.setString(4, owner.getDireccion());
            ps.executeUpdate(); // Ejecuta la inserción

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    owner.setId(keys.getInt(1));
                }
            }
            return owner;
        } catch (SQLException e) {
            throw new RuntimeException("Error al intentar guardar un nuevo dueño: " + e.getMessage(), e);
        }
    }

    /**
     * Actualiza un dueño existente.
     *
     * @param owner datos actualizados
     */
    public void update(Owner owner) {
        ensureSchema();
        final String sql = "UPDATE owners SET nombre_completo=?, telefono=?, correo=?, direccion=? WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, owner.getNombreCompleto());
            ps.setString(2, owner.getTelefono());
            ps.setString(3, owner.getCorreo());
            ps.setString(4, owner.getDireccion());
            ps.setInt(5, owner.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error actualizando dueño.", e);
        }
    }

    /**
     * Elimina un dueño si no tiene registros dependientes bloqueantes.
     *
     * @param id identificador del dueño
     */
    public void delete(int id) {
        ensureSchema();
        final String sql = "DELETE FROM owners WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error eliminando dueño. Verifica si tiene mascotas asociadas.", e);
        }
    }

    /**
     * Cuenta cuántos dueños o clientes se han registrado en el mes actual.
     * Ideal para alimentar las métricas o gráficas del dashboard.
     *
     * @return La cantidad de clientes nuevos en este mes. Retorna 0 si hay algún error 
     *         (por ejemplo, si la tabla o la columna fecha_registro aún no existen).
     */
    public int countNewThisMonth() {
        ensureSchema();
        final String sql = """
                SELECT COUNT(*) FROM owners
                WHERE YEAR(fecha_registro)  = YEAR(CURRENT_DATE())
                AND   MONTH(fecha_registro) = MONTH(CURRENT_DATE())
                """;
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            return rs.next() ? rs.getInt(1) : 0;

        } catch (SQLException e) {
            // Se traga la excepción a propósito para que el dashboard no se caiga 
            // si la base de datos todavía se está configurando.
            return 0;
        }
    }

    /**
     * Método ayudante (helper) para convertir una fila que devuelve la base de datos 
     * en un objeto Java tipo Owner. Así no repetimos este bloque de código en cada consulta.
     *
     * @param rs El ResultSet que trae los datos de la consulta SQL.
     * @return Un objeto Owner con todos sus datos armados.
     * @throws SQLException Si algo falla al leer las columnas.
     */
    private Owner mapRow(ResultSet rs) throws SQLException {
        return new Owner(
                rs.getInt("id"),
                rs.getString("nombre_completo"),
                rs.getString("telefono"),
                rs.getString("correo"),
                rs.getString("direccion"));
    }

    private void migrateOwnersTable(Connection conn, Statement st) throws SQLException {
        addColumnIfMissing(conn, st, "nombre_completo", "VARCHAR(180) NOT NULL");
        addColumnIfMissing(conn, st, "telefono", "VARCHAR(60) NULL");
        addColumnIfMissing(conn, st, "correo", "VARCHAR(160) NULL");
        addColumnIfMissing(conn, st, "direccion", "VARCHAR(220) NULL");
        addColumnIfMissing(conn, st, "fecha_registro", "TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP");

        st.execute("ALTER TABLE owners MODIFY COLUMN nombre_completo VARCHAR(180) NOT NULL");
        st.execute("ALTER TABLE owners MODIFY COLUMN telefono VARCHAR(60) NULL");
        st.execute("ALTER TABLE owners MODIFY COLUMN correo VARCHAR(160) NULL");
        st.execute("ALTER TABLE owners MODIFY COLUMN direccion VARCHAR(220) NULL");
        makeLegacyRequiredColumnsNullable(conn, st);
    }

    private void addColumnIfMissing(Connection conn, Statement st, String column, String definition)
            throws SQLException {
        if (!SchemaInspector.columnExists(conn, "owners", column)) {
            st.execute("ALTER TABLE owners ADD COLUMN " + column + " " + definition);
        }
    }

    private void makeLegacyRequiredColumnsNullable(Connection conn, Statement st) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getColumns(conn.getCatalog(), null, "owners", null)) {
            while (rs.next()) {
                String column = rs.getString("COLUMN_NAME");
                String normalized = column.toLowerCase(Locale.ROOT);
                if (isManagedColumn(normalized)
                        || rs.getInt("NULLABLE") != java.sql.DatabaseMetaData.columnNoNulls
                        || rs.getString("COLUMN_DEF") != null
                        || isAutoIncrement(rs)) {
                    continue;
                }

                String definition = sqlDefinition(rs);
                if (definition != null) {
                    st.execute("ALTER TABLE owners MODIFY COLUMN " + column + " " + definition + " NULL");
                }
            }
        }
    }

    private boolean isManagedColumn(String column) {
        return column.equals("id")
                || column.equals("nombre_completo")
                || column.equals("telefono")
                || column.equals("correo")
                || column.equals("direccion")
                || column.equals("fecha_registro");
    }

    private boolean isAutoIncrement(ResultSet rs) throws SQLException {
        String value = rs.getString("IS_AUTOINCREMENT");
        return value != null && value.equalsIgnoreCase("YES");
    }

    private String sqlDefinition(ResultSet rs) throws SQLException {
        String type = rs.getString("TYPE_NAME");
        int size = rs.getInt("COLUMN_SIZE");
        int decimalDigits = rs.getInt("DECIMAL_DIGITS");
        String normalizedType = type.toUpperCase(Locale.ROOT);

        if (normalizedType.contains("CHAR") || normalizedType.contains("BINARY")) {
            return normalizedType + "(" + Math.max(size, 1) + ")";
        }
        if (normalizedType.equals("DECIMAL") || normalizedType.equals("NUMERIC")) {
            return normalizedType + "(" + Math.max(size, 1) + "," + Math.max(decimalDigits, 0) + ")";
        }
        if (normalizedType.contains("TEXT") || normalizedType.contains("BLOB")
                || normalizedType.contains("INT") || normalizedType.equals("DATE")
                || normalizedType.equals("TIME") || normalizedType.equals("DATETIME")
                || normalizedType.equals("TIMESTAMP") || normalizedType.equals("DOUBLE")
                || normalizedType.equals("FLOAT") || normalizedType.equals("BOOLEAN")) {
            return normalizedType;
        }
        return null;
    }

    public record OwnerStats(int clientsToday, int clientsWeek, int clientsMonth) {}

    public OwnerStats loadOwnerStats() {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.sql.Timestamp tsTodayStart = java.sql.Timestamp.valueOf(today.atStartOfDay());
        java.sql.Timestamp tsTomorrowStart = java.sql.Timestamp.valueOf(today.plusDays(1).atStartOfDay());

        java.sql.Timestamp tsWeekStart = java.sql.Timestamp.valueOf(today.with(java.time.DayOfWeek.MONDAY).atStartOfDay());
        java.sql.Timestamp tsNextWeekStart = java.sql.Timestamp.valueOf(today.with(java.time.DayOfWeek.MONDAY).plusWeeks(1).atStartOfDay());

        java.sql.Timestamp tsMonthStart = java.sql.Timestamp.valueOf(today.withDayOfMonth(1).atStartOfDay());
        java.sql.Timestamp tsNextMonthStart = java.sql.Timestamp.valueOf(today.withDayOfMonth(1).plusMonths(1).atStartOfDay());

        final String sql = """
                SELECT
                  COALESCE(SUM(CASE WHEN fecha_registro >= ? AND fecha_registro < ? THEN 1 ELSE 0 END), 0) AS clients_today,
                  COALESCE(SUM(CASE WHEN fecha_registro >= ? AND fecha_registro < ? THEN 1 ELSE 0 END), 0) AS clients_week,
                  COALESCE(SUM(CASE WHEN fecha_registro >= ? AND fecha_registro < ? THEN 1 ELSE 0 END), 0) AS clients_month
                FROM owners
                """;

        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, tsTodayStart);
            ps.setTimestamp(2, tsTomorrowStart);
            ps.setTimestamp(3, tsWeekStart);
            ps.setTimestamp(4, tsNextWeekStart);
            ps.setTimestamp(5, tsMonthStart);
            ps.setTimestamp(6, tsNextMonthStart);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new OwnerStats(rs.getInt(1), rs.getInt(2), rs.getInt(3));
                }
            }
        } catch (SQLException e) {
            // Se traga la excepción a propósito para robustez
        }
        return new OwnerStats(0, 0, 0);
    }
}
