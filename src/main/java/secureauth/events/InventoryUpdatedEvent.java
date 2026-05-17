package secureauth.events;

/** Evento emitido tras una actualización de inventario. */
public record InventoryUpdatedEvent(String source) implements DomainEvent { }
