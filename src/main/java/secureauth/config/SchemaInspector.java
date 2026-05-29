package secureauth.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

/**
 * Utilidad centralizada para inspeccionar el esquema real de MySQL antes de
 * aplicar migraciones progresivas.
 */
public final class SchemaInspector {

    private SchemaInspector() {
    }

    public static boolean tableExists(Connection conn, String tableName) throws SQLException {
        return exists(conn, tableName, null);
    }

    public static boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        return exists(conn, tableName, columnName);
    }

    private static boolean exists(Connection conn, String tableName, String columnName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        String catalog = conn.getCatalog();

        if (find(meta, catalog, tableName, columnName)) {
            return true;
        }

        String normalizedTable = tableName.toLowerCase(Locale.ROOT);
        String normalizedColumn = columnName == null ? null : columnName.toLowerCase(Locale.ROOT);
        return find(meta, catalog, normalizedTable, normalizedColumn);
    }

    private static boolean find(DatabaseMetaData meta, String catalog, String tableName, String columnName)
            throws SQLException {
        if (columnName == null) {
            try (ResultSet rs = meta.getTables(catalog, null, tableName, new String[] {"TABLE"})) {
                return rs.next();
            }
        }

        try (ResultSet rs = meta.getColumns(catalog, null, tableName, columnName)) {
            return rs.next();
        }
    }
}
