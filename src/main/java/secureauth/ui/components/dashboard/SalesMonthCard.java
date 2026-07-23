package secureauth.ui.components.dashboard;

import java.text.NumberFormat;
import java.util.Locale;
import secureauth.config.AppContext;

public final class SalesMonthCard implements DashboardCard {
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.of("es", "CO"));

    @Override
    public String getId() {
        return "sales_month";
    }

    @Override
    public String getDefaultTitle() {
        return "Ventas del Mes";
    }

    @Override
    public String getIconPath() {
        return "/icon/H10104.png";
    }

    @Override
    public boolean isSummaryCard() {
        return false;
    }

    @Override
    public String getValue(AppContext appContext) throws Exception {
        var stats = appContext.getSalesTransactionService().loadStats();
        return currency.format(stats.salesMonth());
    }
}
