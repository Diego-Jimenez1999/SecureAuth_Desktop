package secureauth.shared.events;

public interface EventPublisher {
    void publish(ApplicationEvent event);
}
