package secureauth.controller;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingWorker;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import secureauth.model.ReportChartData;
import secureauth.model.ReportChartPoint;
import secureauth.service.OwnerService;
import secureauth.service.enterprise.SalesTransactionService;
import secureauth.ui.components.PanelReports;

/**
 * Controlador MVC del módulo de reportes.
 *
 * <p>Carga métricas reales desde la base de datos en un hilo de fondo
 * ({@link SwingWorker}) para no bloquear el EDT de Swing.</p>
 *
 * <ul>
 *   <li>Ventas del día</li>
 *   <li>Ventas del mes</li>
 *   <li>Ítems vendidos en el mes</li>
 *   <li>Clientes nuevos en el mes (antes hardcodeado en "0")</li>
 *   <li>Tendencia de ventas y productos más vendidos para los gráficos del panel</li>
 * </ul>
 *
 * @author Diego
 * @version 2.2 — Gráficos reales alimentados desde SalesTransactionService (sin datos estáticos).
 */
public class ReportController {

    private static final Logger LOGGER = Logger.getLogger(ReportController.class.getName());

    private final PanelReports view;
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.of("es", "CO"));
    private final SalesTransactionService salesService;
    private final OwnerService ownerService;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Construye el controlador y registra la acción de refresco en la vista.
     *
     * @param view panel de reportes
     */
    public ReportController(PanelReports view) {
        this(view, new SalesTransactionService(), new OwnerService(new secureauth.dao.OwnerDAO()));
    }

    public ReportController(PanelReports view, SalesTransactionService salesService) {
        this(view, salesService, new OwnerService(new secureauth.dao.OwnerDAO()));
    }

    public ReportController(PanelReports view, SalesTransactionService salesService, OwnerService ownerService) {
        this.view = view;
        this.salesService = salesService;
        this.ownerService = ownerService;
        this.view.setOnRefresh(() -> loadMetrics(true));
        this.view.setOnExport(this::exportSalesCsv);
    }

    /** Cantidad de días incluidos en el gráfico de tendencia de ventas. */
    private static final int TREND_DAYS = 14;
    /** Cantidad máxima de productos mostrados en el gráfico de más vendidos. */
    private static final int TOP_PRODUCTS_LIMIT = 6;

    private static record ReportData(
            String salesToday,
            String salesMonth,
            String itemsMonth,
            String newClients,
            List<Object[]> reportRows,
            ReportChartData salesTrendChart,
            ReportChartData topProductsChart
    ) {}

    /**
     * Carga todas las métricas del dashboard en un hilo de fondo.
     * Actualiza la vista cuando la carga termina.
     */
    public void loadMetrics() {
        loadMetrics(false);
    }

    public void loadMetrics(boolean notifyResult) {
        new SwingWorker<ReportData, Void>() {
            @Override
            protected ReportData doInBackground() throws Exception {
                var stats = salesService.loadStats();

                // Métrica real de clientes — antes hardcodeada en "0"
                int newClients = ownerService.countNewThisMonth();
                List<Object[]> rows = buildReportRows(salesService.recentSales(100));

                // Datos reales para los gráficos reutilizables del panel (sin datos estáticos)
                List<ReportChartPoint> trendPoints = formatTrendLabels(salesService.salesTrend(TREND_DAYS));
                List<ReportChartPoint> topPoints = salesService.topProducts(TOP_PRODUCTS_LIMIT);
                ReportChartData salesTrendChart = new ReportChartData("Tendencia de Ventas", trendPoints);
                ReportChartData topProductsChart = new ReportChartData("Distribución de Productos", topPoints);

                return new ReportData(
                        currency.format(stats.salesToday()),
                        currency.format(stats.salesMonth()),
                        String.valueOf(stats.itemsMonth()),
                        String.valueOf(newClients),
                        rows,
                        salesTrendChart,
                        topProductsChart
                );
            }

            @Override
            protected void done() {
                try {
                    ReportData data = get();
                    view.updateMetrics(data.salesToday(), data.salesMonth(), data.itemsMonth(), data.newClients());
                    view.renderSalesRows(data.reportRows());
                    view.updateSalesTrendChart(data.salesTrendChart());
                    view.updateTopProductsChart(data.topProductsChart());
                    if (notifyResult) {
                        JOptionPane.showMessageDialog(view, "Reporte actualizado correctamente.", "Reportes",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.log(Level.WARNING, "Carga de métricas interrumpida", e);
                    view.updateMetrics(currency.format(0), currency.format(0), "0", "0");
                    view.renderSalesRows(List.of());
                    clearCharts();
                    if (notifyResult) {
                        JOptionPane.showMessageDialog(view, "La generación del reporte fue interrumpida.",
                                "Reportes", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (ExecutionException e) {
                    LOGGER.log(Level.WARNING, "Error cargando métricas de reportes", e.getCause());
                    view.updateMetrics(currency.format(0), currency.format(0), "0", "0");
                    view.renderSalesRows(List.of());
                    clearCharts();
                    if (notifyResult) {
                        JOptionPane.showMessageDialog(view,
                                "No se pudo generar el reporte: " + e.getCause().getMessage(),
                                "Reportes", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }.execute();
    }

    /**
     * Deja los gráficos en estado vacío ante un error de carga, evitando
     * mostrar información desactualizada o inconsistente.
     */
    private void clearCharts() {
        view.updateSalesTrendChart(new ReportChartData("Tendencia de Ventas", List.of()));
        view.updateTopProductsChart(new ReportChartData("Distribución de Productos", List.of()));
    }

    /**
     * Convierte las etiquetas ISO ({@code yyyy-MM-dd}) devueltas por la
     * consulta de tendencia en un formato corto legible ({@code dd/MM}).
     *
     * @param points puntos originales con fecha en formato ISO
     * @return mismos puntos con etiqueta reformateada; si una etiqueta no es
     *         una fecha válida se deja tal cual
     */
    private List<ReportChartPoint> formatTrendLabels(List<ReportChartPoint> points) {
        DateTimeFormatter shortDate = DateTimeFormatter.ofPattern("dd/MM");
        List<ReportChartPoint> formatted = new ArrayList<>();
        for (ReportChartPoint point : points) {
            String label = point.label();
            try {
                label = java.time.LocalDate.parse(point.label()).format(shortDate);
            } catch (java.time.format.DateTimeParseException ignored) {
                // La etiqueta no es una fecha ISO; se conserva sin transformar.
            }
            formatted.add(new ReportChartPoint(label, point.value()));
        }
        return formatted;
    }

    private List<Object[]> buildReportRows(List<secureauth.dao.enterprise.SalesTransactionDAO.SaleReportRow> rows) {
        List<Object[]> out = new ArrayList<>();
        for (var row : rows) {
            out.add(new Object[]{
                    row.createdAt().format(dateFormatter),
                    emptyAs(row.userName(), "Sistema"),
                    emptyAs(row.clientName(), "Mostrador"),
                    currency.format(row.total()),
                    emptyAs(row.itemsSummary(), row.itemsCount() + " item(s)"),
                    row.paymentMethod()
            });
        }
        return out;
    }

    private void exportSalesCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Exportar ventas");
        chooser.setSelectedFile(new File("reporte_ventas.csv"));
        if (chooser.showSaveDialog(view) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".csv")) {
            file = new File(file.getParentFile(), file.getName() + ".csv");
        }
        try {
            List<String> lines = new ArrayList<>();
            lines.add("fecha,usuario,cliente,total,productos,pago");
            for (var row : salesService.recentSales(1000)) {
                lines.add(String.join(",",
                        csv(row.createdAt().format(dateFormatter)),
                        csv(emptyAs(row.userName(), "Sistema")),
                        csv(emptyAs(row.clientName(), "Mostrador")),
                        String.valueOf(row.total()),
                        csv(emptyAs(row.itemsSummary(), row.itemsCount() + " item(s)")),
                        csv(row.paymentMethod())));
            }
            Files.write(file.toPath(), lines, StandardCharsets.UTF_8);
            JOptionPane.showMessageDialog(view, "Reporte exportado:\n" + file.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, "No se pudo exportar: " + ex.getMessage(), "Exportar",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private String emptyAs(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String csv(String value) {
        String safe = value == null ? "" : value;
        return "\"" + safe.replace("\"", "\"\"") + "\"";
    }
}
