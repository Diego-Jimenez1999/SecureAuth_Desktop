package secureauth.shared.events;

public class NoOpEventPublisher implements EventPublisher {
    @Override
    public void publish(ApplicationEvent event) {
        // Base preparada para reemplazar por un bus interno en una fase posterior.
    }
}
