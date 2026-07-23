package secureauth.ui.components.dashboard;

import secureauth.config.AppContext;

public final class ScheduledAppointmentsCard implements DashboardCard {
    @Override
    public String getId() {
        return "scheduled_appointments";
    }

    @Override
    public String getDefaultTitle() {
        return "Citas Programadas";
    }

    @Override
    public String getIconPath() {
        return "/icon/H10102.png";
    }

    @Override
    public boolean isSummaryCard() {
        return true;
    }

    @Override
    public String getValue(AppContext appContext) throws Exception {
        int count = appContext.getAppointmentService().countScheduledAppointments();
        return String.valueOf(count);
    }
}
