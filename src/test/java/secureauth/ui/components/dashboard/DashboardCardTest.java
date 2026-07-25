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

    @Test
    public void testNewCardsRegistered() {
        var cards = DashboardCardRegistry.getCards();
        assertTrue(cards.stream().anyMatch(c -> c.getId().equals("profit_month")));
        assertTrue(cards.stream().anyMatch(c -> c.getId().equals("low_stock_products")));
        assertTrue(cards.stream().anyMatch(c -> c.getId().equals("registered_pets")));
        assertTrue(cards.stream().anyMatch(c -> c.getId().equals("sales_by_branch")));
    }

    @Test
    public void testNewCardsBasicGetters() {
        var profit = new ProfitMonthCard();
        assertEquals("profit_month", profit.getId());
        assertEquals("Utilidad del Mes", profit.getDefaultTitle());
        assertFalse(profit.isSummaryCard());

        var lowStock = new LowStockProductsCard();
        assertEquals("low_stock_products", lowStock.getId());
        assertEquals("Productos con Bajo Stock", lowStock.getDefaultTitle());
        assertFalse(lowStock.isSummaryCard());

        var registeredPets = new RegisteredPetsCard();
        assertEquals("registered_pets", registeredPets.getId());
        assertEquals("Mascotas Registradas", registeredPets.getDefaultTitle());
        assertFalse(registeredPets.isSummaryCard());

        var salesByBranch = new SalesByBranchCard();
        assertEquals("sales_by_branch", salesByBranch.getId());
        assertEquals("Ventas por Sucursal", salesByBranch.getDefaultTitle());
        assertFalse(salesByBranch.isSummaryCard());
    }

    @Test
    public void testDbPersistenceAndFallback() {
        // Save to DB and fallback properties file
        DashboardCardConfig.saveConfig("db_test_card", true, "DB Title");

        // Even if database connection is down or fails, reading should fall back and succeed via properties file
        assertTrue(DashboardCardConfig.isVisible("db_test_card", false));
        assertEquals("DB Title", DashboardCardConfig.getTitle("db_test_card", "Fallback Default"));
    }
}
