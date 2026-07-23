package secureauth.service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicReference;

class NotificationAndSoundServiceTest {

    @Test
    void testNotificationPublishAndListenFlow() {
        NotificationService service = NotificationService.getInstance();
        assertNotNull(service);

        AtomicReference<NotificationService.Notification> received = new AtomicReference<>();
        NotificationService.NotificationListener listener = received::set;

        service.addListener(listener);
        service.publish("Test message", NotificationService.NotificationType.SUCCESS);

        assertNotNull(received.get());
        assertEquals("Test message", received.get().message());
        assertEquals(NotificationService.NotificationType.SUCCESS, received.get().type());

        service.removeListener(listener);
    }

    @Test
    void testSoundServiceSingleton() {
        SoundService soundService = SoundService.getInstance();
        assertNotNull(soundService);
        // It plays sound without throwing exception
        assertDoesNotThrow(() -> soundService.playSound(SoundService.SoundEvent.CONFIRMATION));
    }
}
