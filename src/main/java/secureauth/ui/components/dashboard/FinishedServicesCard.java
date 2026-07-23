package secureauth.ui.components.dashboard;

import secureauth.config.AppContext;

public final class FinishedServicesCard implements DashboardCard {
    @Override
    public String getId() {
        return "finished_services";
    }

    @Override
    public String getDefaultTitle() {
        return "Servicios Finalizados";
    }

    @Override
    public String getIconPath() {
        return "/icon/H10103.png";
    }

    @Override
    public boolean isSummaryCard() {
        return true;
    }

    @Override
    public String getValue(AppContext appContext) throws Exception {
        int count = appContext.getAppointmentService().countFinishedServices();
        return String.valueOf(count);
    }
}
