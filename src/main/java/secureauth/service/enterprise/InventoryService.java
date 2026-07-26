package secureauth.service.enterprise;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import secureauth.dao.enterprise.InventoryDAO;
import secureauth.config.DatabaseConnection;
import secureauth.model.enterprise.InventoryItem;

/** Servicio enterprise para inventario e importación CSV/XLSX. */
public class InventoryService {

    private final InventoryDAO dao;
    private final EnterpriseContext context = EnterpriseContext.getInstance();

    public InventoryService() {
        this(new InventoryDAO());
    }

    public InventoryService(InventoryDAO dao) {
        this.dao = dao;
    }

    public void initializeSchema() throws SQLException {
        dao.ensureSchema();
    }

    public List<InventoryItem> findAll(String query) throws SQLException {
        return dao.findAll(context.getActiveBusinessId(), context.getActiveBranchId(), query);
    }

    public InventoryDAO.InventorySummary loadSummary() throws SQLException {
        return dao.loadSummary(context.getActiveBusinessId(), context.getActiveBranchId());
    }

    public void upsert(InventoryItem item) throws SQLException {
        dao.upsert(item);
        secureauth.shared.events.DashboardEventBus.notifyDataChanged();
    }

    /**
     * Verifica si existe stock suficiente para un producto del inventario activo.
     *
     * @param idProducto identificador del producto
     * @param cantidad cantidad requerida
     * @return true si el producto existe y el stock alcanza
     * @throws SQLException si falla la consulta JDBC
     */
    public boolean verificarStock(int idProducto, int cantidad) throws SQLException {
        if (cantidad <= 0) {
            return false;
        }
        try (var conn = DatabaseConnection.getConnection()) {
            return dao.hasStockForUpdate(conn, context.getActiveBusinessId(), context.getActiveBranchId(),
                    idProducto, cantidad);
        }
    }

    public ImportPreview previewImport(File file) throws IOException {
        String name = file.getName().toLowerCase();
        List<String[]> rows = name.endsWith(".xlsx") ? readXlsx(file) : readCsv(file);
        return validateRows(rows);
    }

    public void importRows(List<String[]> rows) throws SQLException {
        for (String[] row : rows) {
            InventoryItem item = new InventoryItem(0, context.getActiveBusinessId(), context.getActiveBranchId(),
                    row[0], row[1], row[2], parseInt(row[3]), parseInt(row[4]), row[5], parseDouble(row[6]),
                    parseDouble(row[7]), row.length > 8 ? row[8] : "ACTIVO");
            dao.upsert(item);
        }
        secureauth.shared.events.DashboardEventBus.notifyDataChanged();
    }

    public void exportCsv(Path target, String query) throws IOException, SQLException {
        List<InventoryItem> items = findAll(query);
        List<String> lines = new ArrayList<>();
        lines.add("sku,producto,categoria,stock,min_stock,proveedor,costo,precio,estado,stock_bajo,valor_total");
        for (InventoryItem item : items) {
            boolean lowStock = item.stock() <= item.minStock();
            double value = item.stock() * item.price();
            lines.add(String.join(",",
                    csv(item.sku()),
                    csv(item.name()),
                    csv(item.category()),
                    String.valueOf(item.stock()),
                    String.valueOf(item.minStock()),
                    csv(item.supplier()),
                    String.valueOf(item.cost()),
                    String.valueOf(item.price()),
                    csv(item.status()),
                    lowStock ? "SI" : "NO",
                    String.valueOf(value)));
        }
        Files.write(target, lines, StandardCharsets.UTF_8);
    }

    private String csv(String value) {
        String safe = value == null ? "" : value;
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }

    private ImportPreview validateRows(List<String[]> rows) {
        List<String> errors = new ArrayList<>();
        List<String[]> valid = new ArrayList<>();
        if (rows.isEmpty()) {
            errors.add("Archivo vacío");
            return new ImportPreview(List.of(), errors);
        }
        for (int i = 1; i < rows.size(); i++) {
            String[] r = rows.get(i);
            if (r.length < 8) {
                errors.add("Fila " + (i + 1) + " tiene columnas insuficientes");
                continue;
            }
            if (r[0].isBlank() || r[1].isBlank()) {
                errors.add("Fila " + (i + 1) + " sin SKU o Nombre");
                continue;
            }
            valid.add(r);
        }
        return new ImportPreview(valid, errors);
    }

    private List<String[]> readCsv(File file) throws IOException {
        List<String[]> rows = new ArrayList<>();
        for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
            rows.add(parseCsvLine(line));
        }
        return rows;
    }

    private List<String[]> readXlsx(File file) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file); XSSFWorkbook workbook = new XSSFWorkbook(fis)) {
            DataFormatter formatter = new DataFormatter();
            XSSFSheet sheet = workbook.getSheetAt(0);
            Iterator<Row> it = sheet.iterator();
            while (it.hasNext()) {
                Row row = it.next();
                int cells = Math.max(9, row.getLastCellNum());
                String[] values = new String[cells];
                for (int i = 0; i < cells; i++) {
                    Cell cell = row.getCell(i);
                    values[i] = cell == null ? "" : formatter.formatCellValue(cell).trim();
                }
                rows.add(values);
            }
        }
        return rows;
    }

    private int parseInt(String value) {
        try {
            return (int) Math.round(Double.parseDouble(normalizeNumber(value)));
        } catch (Exception ex) {
            return 0;
        }
    }

    private double parseDouble(String value) {
        try { return Double.parseDouble(normalizeNumber(value)); } catch (Exception ex) { return 0d; }
    }

    private String normalizeNumber(String value) {
        if (value == null) {
            return "0";
        }
        return value.trim().replace("$", "").replace(" ", "").replace(',', '.');
    }

    private String[] parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (c == ',' && !quoted) {
                values.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        values.add(current.toString().trim());
        return values.toArray(String[]::new);
    }

    /** Resultado de validación para preview de importación. */
    public record ImportPreview(List<String[]> validRows, List<String> errors) { }
}
