package secureauth.ui.components.dashboard;

import secureauth.config.AppContext;

public final class NewUsersMonthCard implements DashboardCard {
    @Override
    public String getId() {
        return "new_users_month";
    }

    @Override
    public String getDefaultTitle() {
        return "Usuarios Nuevos/Mes";
    }

    @Override
    public String getIconPath() {
        return "/icon/H10101.png";
    }

    @Override
    public boolean isSummaryCard() {
        return false;
    }

    @Override
    public String getValue(AppContext appContext) throws Exception {
        int count = appContext.getUserService().countNewThisMonth();
        return String.valueOf(count);
    }
}
