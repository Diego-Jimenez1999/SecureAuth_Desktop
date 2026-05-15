package secureauth.controller;

import java.text.NumberFormat;
import java.util.Locale;

import secureauth.service.enterprise.SalesTransactionService;
import secureauth.ui.components.PanelReports;

/**
 * Controlador MVC del módulo de reportes.
 */
public class ReportController {

    private final PanelReports view;
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.of("es", "CO"));
    private final SalesTransactionService salesService = new SalesTransactionService();

    public ReportController(PanelReports view) {
        this.view = view;
        this.view.setOnRefresh(this::loadMetrics);
    }

    public void loadMetrics() {
        try {
            salesService.initializeSchema();
            var stats = salesService.loadStats();
            view.updateMetrics(
                    currency.format(stats.salesToday()),
                    currency.format(stats.salesMonth()),
                    String.valueOf(stats.itemsMonth()));
        } catch (Exception ex) {
            view.updateMetrics(currency.format(0), currency.format(0), "0");
        }
    }
}
