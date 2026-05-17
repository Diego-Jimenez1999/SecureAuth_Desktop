package secureauth.events;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/** EventBus simple para desacoplar módulos de UI/servicio. */
public final class AppEventBus {

    private static final AppEventBus INSTANCE = new AppEventBus();

    private final Map<Class<?>, List<Consumer<Object>>> listeners = new ConcurrentHashMap<>();

    private AppEventBus() { }

    public static AppEventBus getInstance() { return INSTANCE; }

    public <T extends DomainEvent> void subscribe(Class<T> eventType, Consumer<T> listener) {
        listeners.computeIfAbsent(eventType, ignored -> new CopyOnWriteArrayList<>())
                .add(event -> listener.accept(eventType.cast(event)));
    }

    public void publish(DomainEvent event) {
        List<Consumer<Object>> eventListeners = listeners.get(event.getClass());
        if (eventListeners == null) {
            return;
        }
        for (Consumer<Object> consumer : eventListeners) {
            consumer.accept(event);
        }
    }
}
