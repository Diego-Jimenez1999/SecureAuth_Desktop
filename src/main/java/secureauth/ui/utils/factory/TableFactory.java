package secureauth.ui.utils.factory;

import javax.swing.JTable;

import secureauth.ui.utils.UiTheme;

/** Fábrica de tablas enterprise para mantener estilo consistente. */
public final class TableFactory {

    private TableFactory() { }

    public static void applyEnterpriseStyle(JTable table) {
        table.setRowHeight(28);
        table.setFont(UiTheme.BODY_FONT);
        table.setGridColor(UiTheme.BORDER_COLOR);
        table.setSelectionBackground(UiTheme.themePrimary().brighter());
        table.setSelectionForeground(UiTheme.TEXT_PRIMARY);
        table.getTableHeader().setFont(UiTheme.BODY_FONT.deriveFont(java.awt.Font.BOLD));
        table.getTableHeader().setBackground(UiTheme.PANEL_WHITE);
        table.getTableHeader().setForeground(UiTheme.TEXT_SECONDARY);
    }
}
