package secureauth.events;

/** Evento emitido al registrar un cliente/dueño. */
public record ClientCreatedEvent(int ownerId, String ownerName) implements DomainEvent { }
