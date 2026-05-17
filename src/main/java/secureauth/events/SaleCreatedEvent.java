package secureauth.events;

/** Evento emitido cuando una venta POS se registra. */
public record SaleCreatedEvent(double total, double gain, double tax, int itemsCount) implements DomainEvent { }
