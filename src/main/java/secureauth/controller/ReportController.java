package secureauth.controller;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingWorker;

import secureauth.dao.OwnerDAO;
import secureauth.events.AppEventBus;
import secureauth.events.InventoryUpdatedEvent;
import secureauth.events.SaleCreatedEvent;
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
    private final SalesTransactionService salesService = new SalesTransactionService();
    private final OwnerDAO ownerDAO = new OwnerDAO();

    /**
     * Construye el controlador y registra la acción de refresco en la vista.
     *
     * @param view panel de reportes
     */
    public ReportController(PanelReports view) {
        this.view = view;
        this.view.setOnRefresh(this::loadMetrics);
        AppEventBus.getInstance().subscribe(SaleCreatedEvent.class, event -> loadMetrics());
        AppEventBus.getInstance().subscribe(InventoryUpdatedEvent.class, event -> loadMetrics());
    }

    /**
     * Carga todas las métricas del dashboard en un hilo de fondo.
     * Actualiza la vista cuando la carga termina.
     */
    public void loadMetrics() {
        new SwingWorker<String[], Void>() {
            @Override
            protected String[] doInBackground() throws Exception {
                salesService.initializeSchema();
                var stats = salesService.loadStats();

                // Métrica real de clientes — antes hardcodeada en "0"
                int newClients = ownerDAO.countNewThisMonth();

                return new String[]{
                        currency.format(stats.salesToday()),
                        currency.format(stats.salesMonth()),
                        String.valueOf(stats.itemsMonth()),
                        String.valueOf(newClients)
                };
            }

            @Override
            protected void done() {
                try {
                    String[] metrics = get();
                    view.updateMetrics(metrics[0], metrics[1], metrics[2], metrics[3]);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOGGER.log(Level.WARNING, "Carga de métricas interrumpida", e);
                    view.updateMetrics(currency.format(0), currency.format(0), "0", "0");
                } catch (ExecutionException e) {
                    LOGGER.log(Level.WARNING, "Error cargando métricas de reportes", e.getCause());
                    view.updateMetrics(currency.format(0), currency.format(0), "0", "0");
                }
            }
        }.execute();
    }
}
