package secureauth.ui.components.dashboard;

import secureauth.config.AppContext;

public final class NewClientsMonthCard implements DashboardCard {
    @Override
    public String getId() {
        return "new_clients_month";
    }

    @Override
    public String getDefaultTitle() {
        return "Clientes Nuevos/Mes";
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
        int count = appContext.getOwnerService().countNewThisMonth();
        return String.valueOf(count);
    }
}
