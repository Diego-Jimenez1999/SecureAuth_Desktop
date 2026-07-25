package secureauth.ui.components.dashboard;

import secureauth.config.AppContext;

public final class RegisteredPetsCard implements DashboardCard {
    @Override
    public String getId() {
        return "registered_pets";
    }

    @Override
    public String getDefaultTitle() {
        return "Mascotas Registradas";
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
        int count = appContext.getPetService().countAll();
        return String.valueOf(count);
    }
}
