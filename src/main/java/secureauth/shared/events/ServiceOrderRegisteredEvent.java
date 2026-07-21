package secureauth.shared.events;

import java.time.LocalDateTime;

public record ServiceOrderRegisteredEvent(LocalDateTime occurredAt, int ordersCount, int consumedProductsCount)
        implements ApplicationEvent {
}
