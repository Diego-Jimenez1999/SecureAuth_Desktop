package secureauth.shared.events;

import java.time.LocalDateTime;

public record InventoryConsumptionEvent(LocalDateTime occurredAt, int productsCount, int unitsConsumed)
        implements ApplicationEvent {
}
