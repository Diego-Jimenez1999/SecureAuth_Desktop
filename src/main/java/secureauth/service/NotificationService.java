package secureauth.service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Servicio central para la publicación y recepción de notificaciones en el sistema (Info, Success, Warning, Error).
 */
public final class NotificationService {

    private static final NotificationService INSTANCE = new NotificationService();

    private final List<NotificationListener> listeners = new CopyOnWriteArrayList<>();

    private NotificationService() {}

    public static NotificationService getInstance() {
        return INSTANCE;
    }

    public enum NotificationType {
        INFO,
        SUCCESS,
        WARNING,
        ERROR
    }

    public record Notification(String message, NotificationType type) {}

    public interface NotificationListener {
        void onNotificationReceived(Notification notification);
    }

    public void addListener(NotificationListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeListener(NotificationListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    /**
     * Publica una nueva notificación global para ser capturada por las vistas u otros servicios.
     */
    public void publish(String message, NotificationType type) {
        Notification notification = new Notification(message, type);
        for (NotificationListener listener : listeners) {
            try {
                listener.onNotificationReceived(notification);
            } catch (Exception e) {
                // Ignore exception to keep it fully isolated
            }
        }
    }
}
