package secureauth.ui.components.dashboard;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import secureauth.config.AppContext;

public class DashboardCardTest {

    @Test
    public void testRegistryAndDefaultCards() {
        var cards = DashboardCardRegistry.getCards();
        assertNotNull(cards);
        assertFalse(cards.isEmpty());

        // Verify scheduled appointments card is registered
        boolean hasScheduled = cards.stream()
                .anyMatch(c -> c.getId().equals("scheduled_appointments"));
        assertTrue(hasScheduled);
    }

    @Test
    public void testCustomCardRegistration_OCP() {
        int initialSize = DashboardCardRegistry.getCards().size();

        // Create and register a custom card (OCP demonstration)
        DashboardCard customCard = new DashboardCard() {
            @Override
            public String getId() {
                return "test_custom_card";
            }

            @Override
            public String getDefaultTitle() {
                return "Custom Test Metric";
            }

            @Override
            public String getIconPath() {
                return "/icon/custom.png";
            }

            @Override
            public boolean isSummaryCard() {
                return false;
            }

            @Override
            public String getValue(AppContext appContext) {
                return "TestVal";
            }
        };

        DashboardCardRegistry.registerCard(customCard);

        var updatedCards = DashboardCardRegistry.getCards();
        assertEquals(initialSize + 1, updatedCards.size());
        assertTrue(updatedCards.stream().anyMatch(c -> c.getId().equals("test_custom_card")));
    }

    @Test
    public void testDashboardCardConfig() {
        // Save test config
        DashboardCardConfig.saveConfig("test_custom_card", false, "New Custom Title");

        assertFalse(DashboardCardConfig.isVisible("test_custom_card", true));
        assertEquals("New Custom Title", DashboardCardConfig.getTitle("test_custom_card", "Default"));
    }
}
