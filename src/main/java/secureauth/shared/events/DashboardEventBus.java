package secureauth.shared.events;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public final class DashboardEventBus {
    private static final PropertyChangeSupport SUPPORT = new PropertyChangeSupport(DashboardEventBus.class);

    public static void addListener(PropertyChangeListener listener) {
        SUPPORT.addPropertyChangeListener("dashboardUpdate", listener);
    }

    public static void removeListener(PropertyChangeListener listener) {
        SUPPORT.removePropertyChangeListener("dashboardUpdate", listener);
    }

    public static void notifyDataChanged() {
        SUPPORT.firePropertyChange("dashboardUpdate", null, null);
    }
}
