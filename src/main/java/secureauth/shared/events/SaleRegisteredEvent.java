package secureauth.shared.events;

import java.time.LocalDateTime;

public record SaleRegisteredEvent(
        LocalDateTime occurredAt,
        double total,
        int linesCount,
        boolean affectsInventory,
        boolean affectsAppointments) implements ApplicationEvent {
}
