package secureauth.service.enterprise;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.opencsv.CSVReader;

import secureauth.dao.enterprise.InventoryDAO;
import secureauth.model.enterprise.InventoryItem;

/** Servicio enterprise para inventario e importación CSV/XLSX con mapeo de columnas. */
public class InventoryService {

    public static final List<String> SUPPORTED_FIELDS = List.of(
            "codigo", "nombre", "categoria", "subcategoria", "stock", "stock_minimo", "costo", "precio", "proveedor"
    );

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

    public RawImportData readRawImport(File file) throws Exception {
        String name = file.getName().toLowerCase();
        List<String[]> rows = name.endsWith(".xlsx") ? readXlsx(file) : readCsv(file);
        if (rows.isEmpty()) {
            return new RawImportData(List.of(), List.of());
        }
        List<String> headers = normalizeHeaders(rows.getFirst());
        List<String[]> data = rows.size() > 1 ? rows.subList(1, rows.size()) : List.of();
        return new RawImportData(headers, data);
    }

    public ImportPlan buildImportPlan(RawImportData raw, Map<String, Integer> mapping) {
        List<ImportRow> validRows = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Set<String> seenSku = new HashSet<>();

        if (!mapping.keySet().containsAll(List.of("codigo", "nombre", "categoria", "stock", "stock_minimo", "costo", "precio", "proveedor"))) {
            errors.add("Faltan campos obligatorios en el mapeo.");
            return new ImportPlan(validRows, errors, 0);
        }

        int duplicates = 0;
        for (int i = 0; i < raw.rows().size(); i++) {
            String[] row = raw.rows().get(i);
            String sku = value(row, mapping.get("codigo"));
            String nombre = value(row, mapping.get("nombre"));
            String categoria = value(row, mapping.get("categoria"));
            String subcategoria = mapping.containsKey("subcategoria") ? value(row, mapping.get("subcategoria")) : "";
            String proveedor = value(row, mapping.get("proveedor"));

            if (sku.isBlank() || nombre.isBlank()) {
                errors.add("Fila " + (i + 2) + " sin código o nombre.");
                continue;
            }
            if (!seenSku.add(sku)) {
                duplicates++;
                continue;
            }

            ImportRow importRow = new ImportRow(
                    sku,
                    nombre,
                    subcategoria.isBlank() ? categoria : categoria + " / " + subcategoria,
                    parseInt(value(row, mapping.get("stock"))),
                    parseInt(value(row, mapping.get("stock_minimo"))),
                    proveedor,
                    parseDouble(value(row, mapping.get("costo"))),
                    parseDouble(value(row, mapping.get("precio")))
            );
            validRows.add(importRow);
        }

        return new ImportPlan(validRows, errors, duplicates);
    }

    public void importRows(List<ImportRow> rows) throws SQLException {
        for (ImportRow row : rows) {
            InventoryItem item = new InventoryItem(0, context.getActiveBusinessId(), context.getActiveBranchId(),
                    row.codigo(), row.nombre(), row.categoria(), row.stock(), row.stockMinimo(), row.proveedor(),
                    row.costo(), row.precio(), "ACTIVO");
            dao.upsert(item);
        }
    }

    private String value(String[] row, int index) {
        return index >= 0 && index < row.length ? row[index].trim() : "";
    }

    private List<String[]> readCsv(File file) throws Exception {
        List<String[]> rows = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(file))) {
            String[] next;
            while ((next = reader.readNext()) != null) rows.add(next);
        }
        return rows;
    }

    private List<String[]> readXlsx(File file) throws Exception {
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

    private List<String> normalizeHeaders(String[] headerRow) {
        List<String> headers = new ArrayList<>();
        for (String h : headerRow) {
            headers.add(h == null ? "" : h.trim().toLowerCase().replace(" ", "_"));
        }
        return headers;
    }

    private int parseInt(String value) {
        try { return Integer.parseInt(value.trim()); } catch (Exception ex) { return 0; }
    }

    private double parseDouble(String value) {
        try { return Double.parseDouble(value.trim().replace(',', '.')); } catch (Exception ex) { return 0d; }
    }

    public record RawImportData(List<String> headers, List<String[]> rows) { }

    public record ImportRow(String codigo, String nombre, String categoria, int stock, int stockMinimo,
                            String proveedor, double costo, double precio) { }

    public record ImportPlan(List<ImportRow> validRows, List<String> errors, int duplicates) { }
}
