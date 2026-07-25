package secureauth.ui.components.dashboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DashboardCardRegistry {
    private static final List<DashboardCard> CARDS = new ArrayList<>();

    static {
        registerCard(new ScheduledAppointmentsCard());
        registerCard(new FinishedServicesCard());
        registerCard(new SalesTodayCard());
        registerCard(new SalesMonthCard());
        registerCard(new NewClientsMonthCard());
        registerCard(new NewUsersMonthCard());

        // New configurable cards
        registerCard(new ProfitMonthCard());
        registerCard(new LowStockProductsCard());
        registerCard(new RegisteredPetsCard());
        registerCard(new SalesByBranchCard());
    }

    public static synchronized void registerCard(DashboardCard card) {
        if (card != null) {
            CARDS.add(card);
        }
    }

    public static synchronized List<DashboardCard> getCards() {
        return Collections.unmodifiableList(new ArrayList<>(CARDS));
    }
}
