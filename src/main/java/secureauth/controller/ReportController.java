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
 * </ul>
 *
 * @author Diego
 * @version 2.1 — SwingWorker + métrica real de clientes nuevos.
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
        this.view.setOnRefresh(this::loadMetrics);
        this.view.setOnExport(this::exportSalesCsv);
    }

    private static record ReportData(
            String salesToday,
            String salesMonth,
            String itemsMonth,
            String newClients,
            List<Object[]> reportRows
    ) {}

    /**
     * Carga todas las métricas del dashboard en un hilo de fondo.
     * Actualiza la vista cuando la carga termina.
     */
    public void loadMetrics() {
        new SwingWorker<ReportData, Void>() {
            @Override
            protected ReportData doInBackground() throws Exception {
                var stats = salesService.loadStats();

                // Métrica real de clientes — antes hardcodeada en "0"
                int newClients = ownerService.countNewThisMonth();
                List<Object[]> rows = buildReportRows(salesService.recentSales(100));

                return new ReportData(
                        currency.format(stats.salesToday()),
                        currency.format(stats.salesMonth()),
                        String.valueOf(stats.itemsMonth()),
                        String.valueOf(newClients),
                        rows
                );
            }

            @Override
            protected void done() {
                try {
                    ReportData data = get();
                    view.updateMetrics(data.salesToday(), data.salesMonth(), data.itemsMonth(), data.newClients());
                    view.renderSalesRows(data.reportRows());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.log(Level.WARNING, "Carga de métricas interrumpida", e);
                    view.updateMetrics(currency.format(0), currency.format(0), "0", "0");
                    view.renderSalesRows(List.of());
                } catch (ExecutionException e) {
                    LOGGER.log(Level.WARNING, "Error cargando métricas de reportes", e.getCause());
                    view.updateMetrics(currency.format(0), currency.format(0), "0", "0");
                    view.renderSalesRows(List.of());
                }
            }
        }.execute();
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
