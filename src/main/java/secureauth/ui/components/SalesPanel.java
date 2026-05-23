package secureauth.ui.components;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import secureauth.controller.SalesController;
import secureauth.model.SaleItem;
import secureauth.service.enterprise.SalesTransactionService;
import secureauth.ui.dialogs.GestionVentasServiciosDialog;
import secureauth.ui.dialogs.PreciosPorTamanoDialog;
import secureauth.ui.dialogs.SubServiceSelector;
import secureauth.ui.sales.SalesServiceCatalog;
import secureauth.ui.sales.SalesServiceCatalog.ServiceItemEntry;
import secureauth.ui.utils.UiTheme;

/** Panel de ventas integrado con gestión de servicios, categorías y precios por tamaño. */
public class SalesPanel extends JPanel {

    private final SalesController controller;
    private final NumberFormat currency;
    private final SalesServiceCatalog catalog;

    private JLabel subtotalValueLabel;
    private JLabel taxValueLabel;
    private JLabel totalValueLabel;
    private JPanel gridPanel;
    private JTextField searchField;
    private JList<String> itemList;
    private final SalesTransactionService salesTransactionService;

    public SalesPanel(SalesController controller, SubServiceSelector subServiceSelector) {
        this(controller, subServiceSelector, new SalesTransactionService());
    }

    public SalesPanel(SalesController controller, SubServiceSelector subServiceSelector,
            SalesTransactionService salesTransactionService) {
        this.controller = controller;
        this.currency = NumberFormat.getCurrencyInstance(Locale.of("es", "CO"));
        this.catalog = SalesServiceCatalog.getInstance();
        this.salesTransactionService = salesTransactionService;

        setLayout(new BorderLayout(16, 0));
        setBackground(UiTheme.BG_PAGE);
        setBorder(new EmptyBorder(12, 0, 12, 0));

        add(buildCatalogContainer(), BorderLayout.CENTER);
        add(buildSummarySection(), BorderLayout.EAST);

        catalog.addCatalogListener(evt -> refreshCatalog());
        refreshCatalog();
    }

    private JPanel buildCatalogContainer() {
        JPanel container = new JPanel(new BorderLayout(0, 12));
        container.setBackground(UiTheme.PANEL_WHITE);
        container.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiTheme.BORDER_COLOR),
                new EmptyBorder(16, 16, 16, 16)));
        container.add(buildCatalogHeader(), BorderLayout.NORTH);
        container.add(buildCatalogSection(), BorderLayout.CENTER);
        return container;
    }

    private JPanel buildCatalogHeader() {
        JPanel header = new JPanel();
        header.setBackground(UiTheme.PANEL_WHITE);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Ventas - Servicios y Productos");
        title.setFont(UiTheme.TITLE_FONT_SECTION);
        title.setForeground(UiTheme.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Catálogo conectado con categorías, subcategorías y tamaños dinámicos.");
        subtitle.setFont(UiTheme.BODY_FONT);
        subtitle.setForeground(UiTheme.TEXT_SECONDARY);

        JPanel actions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        JButton btnTabla = new JButton("Tabla de Servicios");
        JButton btnSizes = new JButton("Precios por Tamaño");
        JButton btnPos = new JButton("REALIZAR VENTA");
        UiTheme.styleButton(btnTabla, UiTheme.BTN_DARK, UiTheme.BTN_DARK_HOVER, UiTheme.TEXT_LIGHT, 170, 34, 12, true, false, 8);
        UiTheme.styleButton(btnSizes, UiTheme.BTN_DARK, UiTheme.BTN_DARK_HOVER, UiTheme.TEXT_LIGHT, 170, 34, 12, true, false, 8);
        UiTheme.styleButton(btnPos, UiTheme.FOREST_GREEN, UiTheme.FOREST_GREEN_HOVER, UiTheme.TEXT_LIGHT, 170, 34, 12, true, false, 8);
        btnTabla.addActionListener(e -> openGestionDialog());
        btnSizes.addActionListener(e -> openSizesDialog());
        btnPos.addActionListener(e -> registerSale());
        actions.add(btnTabla);
        actions.add(btnSizes);
        actions.add(btnPos);

        searchField = new JTextField();
        searchField.setFont(UiTheme.BODY_FONT);
        searchField.setPreferredSize(new Dimension(0, 34));
        searchField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiTheme.BORDER_COLOR),
                new EmptyBorder(6, 10, 6, 10)));
        searchField.addActionListener(e -> refreshCatalog());

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);
        header.add(Box.createVerticalStrut(10));
        header.add(actions);
        header.add(Box.createVerticalStrut(10));
        header.add(searchField);
        return header;
    }

    private JScrollPane buildCatalogSection() {
        gridPanel = new JPanel(new GridLayout(0, 3, 12, 12));
        gridPanel.setOpaque(false);
        return new JScrollPane(gridPanel);
    }

    private JPanel buildSummarySection() {
        JPanel summary = new JPanel(new BorderLayout());
        summary.setPreferredSize(new Dimension(320, 0));
        summary.setBackground(UiTheme.PANEL_WHITE);
        summary.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiTheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder()));

        JLabel panelTitle = new JLabel("Venta Actual");
        panelTitle.setOpaque(true);
        panelTitle.setBackground(UiTheme.DARK_PRIMARY);
        panelTitle.setForeground(UiTheme.TEXT_LIGHT);
        panelTitle.setFont(UiTheme.TITLE_FONT_SECTION);
        panelTitle.setBorder(new EmptyBorder(10, 12, 10, 12));
        summary.add(panelTitle, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setBackground(UiTheme.PANEL_WHITE);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(10, 10, 10, 10));

        itemList = new JList<>(controller.getListModel());
        itemList.setFont(UiTheme.BODY_FONT);
        JScrollPane listScroll = new JScrollPane(itemList);
        listScroll.setPreferredSize(new Dimension(280, 280));

        subtotalValueLabel = new JLabel(currency.format(0));
        taxValueLabel = new JLabel(currency.format(0));
        totalValueLabel = new JLabel(currency.format(0));

        JPanel totalsPanel = new JPanel(new GridLayout(3, 2, 8, 6));
        totalsPanel.setOpaque(false);
        totalsPanel.add(new JLabel("Subtotal:"));
        totalsPanel.add(rightAligned(subtotalValueLabel));
        totalsPanel.add(new JLabel("IVA (19%):"));
        totalsPanel.add(rightAligned(taxValueLabel));
        totalsPanel.add(new JLabel("Total:"));
        totalsPanel.add(rightAligned(totalValueLabel));

        body.add(listScroll);
        body.add(Box.createVerticalStrut(12));
        body.add(buildCartActions());
        body.add(Box.createVerticalStrut(12));
        body.add(totalsPanel);

        summary.add(body, BorderLayout.CENTER);
        return summary;
    }

    private JPanel buildCartActions() {
        JPanel actions = new JPanel(new GridLayout(1, 2, 8, 0));
        actions.setOpaque(false);

        JButton remove = new JButton("Eliminar");
        JButton cancel = new JButton("Cancelar compra");
        UiTheme.styleButton(remove, UiTheme.BTN_DARK, UiTheme.BTN_DARK_HOVER, UiTheme.TEXT_LIGHT, 120, 34, 12, true, false, 8);
        UiTheme.styleButton(cancel, new java.awt.Color(220, 38, 38), new java.awt.Color(185, 28, 28), UiTheme.TEXT_LIGHT, 150, 34, 12, true, false, 8);

        remove.addActionListener(e -> removeSelectedCartItem());
        cancel.addActionListener(e -> cancelSale());
        actions.add(remove);
        actions.add(cancel);
        return actions;
    }

    private void refreshCatalog() {
        String normalized = searchField == null ? "" : searchField.getText().trim().toLowerCase();
        gridPanel.removeAll();
        for (ServiceItemEntry item : catalog.getItems()) {
            if (!normalized.isEmpty() && !item.name().toLowerCase().contains(normalized)) {
                continue;
            }
            addServiceCard(item);
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void addServiceCard(ServiceItemEntry item) {
        JButton card = new JButton("<html><center>" + item.name() + "<br/>" + currency.format(item.price()) + "</center></html>");
        card.setHorizontalAlignment(SwingConstants.CENTER);
        card.setFont(UiTheme.BODY_FONT.deriveFont(Font.BOLD));
        card.setPreferredSize(new Dimension(190, 120));
        card.setBackground(UiTheme.PANEL_WHITE);
        card.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER_COLOR));
        card.addActionListener(e -> addItemToSale(item));
        gridPanel.add(card);
    }

    private void addItemToSale(ServiceItemEntry item) {
        double price = item.price();
        String name = item.name();
        if (item.sizePrices() != null && !item.sizePrices().isEmpty()) {
            Object selected = JOptionPane.showInputDialog(this, "Selecciona tamaño", "Precios por Tamaño",
                    JOptionPane.PLAIN_MESSAGE, null, item.sizePrices().keySet().toArray(), null);
            if (selected != null) {
                String size = selected.toString();
                price = item.sizePrices().getOrDefault(size, item.price());
                name = item.name() + " - " + size;
            }
        }
        controller.addItem(new SaleItem(name, price));
        updateTotals();
    }

    private void openGestionDialog() {
        java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
        new GestionVentasServiciosDialog(w instanceof javax.swing.JFrame ? (javax.swing.JFrame) w : null).setVisible(true);
    }

    private void openSizesDialog() {
        java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
        new PreciosPorTamanoDialog(w instanceof javax.swing.JFrame ? (javax.swing.JFrame) w : null).setVisible(true);
    }

    private void updateTotals() {
        subtotalValueLabel.setText(currency.format(controller.getSubtotal()));
        taxValueLabel.setText(currency.format(controller.getTax()));
        totalValueLabel.setText(currency.format(controller.getTotal()));
    }

    private void removeSelectedCartItem() {
        int index = itemList.getSelectedIndex();
        if (!controller.removeItemAt(index)) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto del carrito para eliminar.");
            return;
        }
        updateTotals();
    }

    private void cancelSale() {
        if (controller.getItems().isEmpty()) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "¿Cancelar toda la compra actual?", "Cancelar compra",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            controller.clearSale();
            updateTotals();
        }
    }

    private void registerSale() {
        if (controller.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay items en el carrito.");
            return;
        }
        Object[] methods = {"Efectivo", "Tarjeta", "Transferencia"};
        Object selected = JOptionPane.showInputDialog(this, "Método de pago", "POS", JOptionPane.PLAIN_MESSAGE, null, methods, methods[0]);
        if (selected == null) {
            return;
        }
        double gain = 0d;
        for (var saleItem : controller.getItems()) {
            ServiceItemEntry matched = catalog.getItems().stream().filter(i -> saleItem.getName().startsWith(i.name())).findFirst().orElse(null);
            if (matched != null) {
                gain += matched.gain();
            }
        }
        try {
            salesTransactionService.initializeSchema();
            String itemsSummary = controller.getItems().stream().map(SaleItem::getName).collect(java.util.stream.Collectors.joining(", "));
            salesTransactionService.registerSale(controller.getTotal(), gain, controller.getTax(), controller.getItems().size(), selected.toString(), itemsSummary, "Mostrador", "");
            controller.clearSale();
            searchField.setText("");
            refreshCatalog();
            updateTotals();
            JOptionPane.showMessageDialog(this, "Venta registrada correctamente.");
        } catch (java.sql.SQLException ex) { // Changed to SQLException as database operations are the primary source of exceptions
            JOptionPane.showMessageDialog(this, "No se pudo registrar venta: " + ex.getMessage());
        }
    }

    private JLabel rightAligned(JLabel label) {
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        label.setFont(UiTheme.BODY_FONT.deriveFont(Font.BOLD));
        return label;
    }
}
