package secureauth.ui.components.dashboard;

import secureauth.config.AppContext;

public final class LowStockProductsCard implements DashboardCard {
    @Override
    public String getId() {
        return "low_stock_products";
    }

    @Override
    public String getDefaultTitle() {
        return "Productos con Bajo Stock";
    }

    @Override
    public String getIconPath() {
        return "/icon/H10102.png";
    }

    @Override
    public boolean isSummaryCard() {
        return false;
    }

    @Override
    public String getValue(AppContext appContext) throws Exception {
        long count = appContext.getInventoryService().findAll("")
                .stream()
                .filter(i -> i.stock() <= i.minStock())
                .count();
        return String.valueOf(count);
    }
}
