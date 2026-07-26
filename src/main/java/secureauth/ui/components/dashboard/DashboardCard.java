package secureauth.ui.components.dashboard;

import secureauth.config.AppContext;

public interface DashboardCard {
    /**
     * Unique identifier for the card (e.g., "sales_today", "sales_month").
     */
    String getId();

    /**
     * The default title for the card.
     */
    String getDefaultTitle();

    /**
     * The path to the icon asset for this card.
     */
    String getIconPath();

    /**
     * Whether this card belongs to the main "Resumen del Día" (summary) row or the Month KPI cards row.
     */
    boolean isSummaryCard();

    /**
     * Retrieves the display value for this card.
     *
     * @param appContext the application dependency context
     * @return the formatted display value
     * @throws Exception if data retrieval fails
     */
    String getValue(AppContext appContext) throws Exception;
}
