package secureauth.shared.events;

import java.time.LocalDateTime;

public interface ApplicationEvent {
    LocalDateTime occurredAt();
}
