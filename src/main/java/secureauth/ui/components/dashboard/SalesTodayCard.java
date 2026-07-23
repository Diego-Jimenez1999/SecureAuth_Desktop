package secureauth.ui.components.dashboard;

import java.text.NumberFormat;
import java.util.Locale;
import secureauth.config.AppContext;

public final class SalesTodayCard implements DashboardCard {
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.of("es", "CO"));

    @Override
    public String getId() {
        return "sales_today";
    }

    @Override
    public String getDefaultTitle() {
        return "Ingresos del Día";
    }

    @Override
    public String getIconPath() {
        return "/icon/H10104.png";
    }

    @Override
    public boolean isSummaryCard() {
        return true;
    }

    @Override
    public String getValue(AppContext appContext) throws Exception {
        var stats = appContext.getSalesTransactionService().loadStats();
        return currency.format(stats.salesToday());
    }
}
