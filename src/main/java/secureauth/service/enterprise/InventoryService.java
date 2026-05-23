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
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import secureauth.dao.enterprise.InventoryDAO;
import secureauth.model.enterprise.InventoryItem;

/** Servicio enterprise para inventario e importación CSV/XLSX. */
public class InventoryService {

    private final InventoryDAO dao = new InventoryDAO();
    private final EnterpriseContext context = EnterpriseContext.getInstance();

    public void initializeSchema() throws SQLException {
        dao.ensureSchema();
    }

    public List<InventoryItem> findAll(String query) throws SQLException {
        return dao.findAll(context.getActiveBusinessId(), context.getActiveBranchId(), query);
    }

    public void upsert(InventoryItem item) throws SQLException {
        dao.upsert(item);
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
            rows.add(line.split(","));
        }
        return rows;
    }

    private List<String[]> readXlsx(File file) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file); XSSFWorkbook workbook = new XSSFWorkbook(fis)) {
            XSSFSheet sheet = workbook.getSheetAt(0);
            Iterator<Row> it = sheet.iterator();
            while (it.hasNext()) {
                Row row = it.next();
                int cells = Math.max(9, row.getLastCellNum());
                String[] values = new String[cells];
                for (int i = 0; i < cells; i++) {
                    Cell cell = row.getCell(i);
                    values[i] = cell == null ? "" : cell.toString();
                }
                rows.add(values);
            }
        }
        return rows;
    }

    private int parseInt(String value) {
        try { return Integer.parseInt(value.trim()); } catch (Exception ex) { return 0; }
    }

    private double parseDouble(String value) {
        try { return Double.parseDouble(value.trim().replace(',', '.')); } catch (Exception ex) { return 0d; }
    }

    /** Resultado de validación para preview de importación. */
    public record ImportPreview(List<String[]> validRows, List<String> errors) { }
}
