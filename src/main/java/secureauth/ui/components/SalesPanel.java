package secureauth.ui.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import secureauth.application.command.RegisterSaleCommand;
import secureauth.application.dto.AppointmentDTO;
import secureauth.application.dto.SaleDTO;
import secureauth.application.dto.SaleItemDTO;
import secureauth.application.dto.ServiceOrderDTO;
import secureauth.application.usecase.RegisterSaleUseCase;
import secureauth.controller.SalesController;
import secureauth.domain.sales.SaleItemType;
import secureauth.infrastructure.persistence.JdbcSalesRepository;
import secureauth.service.OwnerService;
import secureauth.service.enterprise.AppointmentService;
import secureauth.service.enterprise.SalesTransactionService;
import secureauth.ui.dialogs.GestionVentasServiciosDialog;
import secureauth.ui.dialogs.PreciosPorTamanoDialog;
import secureauth.ui.dialogs.ServiceAppointmentDialog;
import secureauth.ui.dialogs.SubServiceSelector;
import secureauth.ui.sales.SalesServiceCatalog;
import secureauth.ui.sales.SalesServiceCatalog.ServiceItemEntry;
import secureauth.ui.utils.UiTheme;

/** * Panel principal de ventas (Punto de Venta - POS).
 * Integra la visualización del catálogo de servicios y productos, barra de búsqueda,
 * y el resumen del carrito de compras actual con sus respectivos cálculos (Subtotal, IVA, Total).
 */
public class SalesPanel extends JPanel {

    private final SalesController controller;
    private final NumberFormat currency;
    private final SalesServiceCatalog catalog;

    private JLabel subtotalValueLabel;
    private JLabel taxValueLabel;
    private JLabel totalValueLabel;
    private JPanel gridPanel;
    private JTextField searchField;
    private JTable cartTable;
    private DefaultTableModel cartTableModel;
    private final RegisterSaleUseCase registerSaleUseCase;
    private final AppointmentService appointmentService;
    private final OwnerService ownerService;

    /**
     * Constructor principal del panel de ventas.
     * * @param controller Controlador que maneja la lógica de los items en la venta actual.
     * @param subServiceSelector Selector para sub-servicios adicionales (si aplica).
     */
    public SalesPanel(SalesController controller, SubServiceSelector subServiceSelector) {
        this(controller, subServiceSelector, new SalesTransactionService());
    }

    /**
     * Constructor sobrecargado del panel de ventas con inyección de servicio de transacciones.
     * * @param controller Controlador de ventas.
     * @param subServiceSelector Selector para sub-servicios.
     * @param salesTransactionService Servicio para registrar las transacciones en la base de datos.
     */
    public SalesPanel(SalesController controller, SubServiceSelector subServiceSelector,
            SalesTransactionService salesTransactionService) {
        this(controller, subServiceSelector, salesTransactionService, new AppointmentService(),
                new OwnerService(new secureauth.dao.OwnerDAO()));
    }

    public SalesPanel(SalesController controller, SubServiceSelector subServiceSelector,
            SalesTransactionService salesTransactionService, AppointmentService appointmentService) {
        this(controller, subServiceSelector, salesTransactionService, appointmentService,
                new OwnerService(new secureauth.dao.OwnerDAO()));
    }

    public SalesPanel(SalesController controller, SubServiceSelector subServiceSelector,
            SalesTransactionService salesTransactionService, AppointmentService appointmentService,
            OwnerService ownerService) {
        this(controller, subServiceSelector, new RegisterSaleUseCase(new JdbcSalesRepository(salesTransactionService)),
                appointmentService, ownerService);
    }

    public SalesPanel(SalesController controller, SubServiceSelector subServiceSelector,
            RegisterSaleUseCase registerSaleUseCase, AppointmentService appointmentService,
            OwnerService ownerService) {
        this.controller = controller;
        this.currency = NumberFormat.getCurrencyInstance(Locale.of("es", "CO"));
        this.catalog = SalesServiceCatalog.getInstance();
        this.registerSaleUseCase = registerSaleUseCase;
        this.appointmentService = appointmentService;
        this.ownerService = ownerService;

        setLayout(new BorderLayout(16, 0));
        setBackground(UiTheme.BG_PAGE);
        setBorder(new EmptyBorder(12, 0, 12, 0));

        add(buildCatalogContainer(), BorderLayout.CENTER);
        add(buildSummarySection(), BorderLayout.EAST);

        // Escuchar cambios en el catálogo para actualizar la vista dinámicamente
        catalog.addCatalogListener(evt -> refreshCatalog());
        refreshCatalog();
    }

    /**
     * Construye el contenedor principal izquierdo que aloja el catálogo de productos/servicios.
     * * @return JPanel configurado con la cabecera y la cuadrícula del catálogo.
     */
    private JPanel buildCatalogContainer() {
        JPanel container = new JPanel(new BorderLayout(0, 12));
        container.setBackground(UiTheme.PANEL_WHITE);
        container.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiTheme.BORDER_COLOR),
                new EmptyBorder(16, 16, 16, 16)));
        container.add(buildCatalogHeader(), BorderLayout.NORTH);
        container.add(buildCatalogSection(), BorderLayout.CENTER);
        return container;
    }

    /**
     * Construye la cabecera del catálogo, incluyendo títulos, botones de administración
     * (Tabla de Servicios, Precios por Tamaño, Realizar Venta) y la barra de búsqueda.
     * * @return JPanel con los componentes superiores del catálogo.
     */
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
        // Permite filtrar el catálogo al presionar Enter en el campo de búsqueda
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

    /**
     * Construye la sección donde se muestran las tarjetas de los productos/servicios.
     * Utiliza un JScrollPane para permitir navegación si hay muchos elementos.
     * * @return JScrollPane que contiene la cuadrícula de productos.
     */
    private JScrollPane buildCatalogSection() {
        gridPanel = new JPanel(new GridLayout(0, 3, 12, 12));
        gridPanel.setOpaque(false);
        return new JScrollPane(gridPanel);
    }

    /**
     * Construye la sección derecha de resumen de la venta actual (Carrito).
     * Muestra la tabla de items agregados, cantidades editables y los totales.
     * * @return JPanel contenedor del resumen de ventas.
     */
    private JPanel buildSummarySection() {
        JPanel summary = new JPanel(new BorderLayout());
        summary.setPreferredSize(new Dimension(500, 0));
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
        body.setPreferredSize(new Dimension(500, 800));
        body.setBackground(UiTheme.PANEL_WHITE);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(10, 10, 10, 10));

        cartTableModel = new DefaultTableModel(new String[]{"Producto", "Precio", "-", "Cantidad", "+", "Subtotal", "Stock"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2 || column == 3 || column == 4;
            }
        };
        cartTable = new JTable(cartTableModel);
        cartTable.setFont(UiTheme.BODY_FONT);
        cartTable.setRowHeight(34);
        cartTable.getColumnModel().getColumn(2).setMaxWidth(42);
        cartTable.getColumnModel().getColumn(4).setMaxWidth(42);
        cartTable.getColumnModel().getColumn(3).setMaxWidth(80);
        cartTable.getColumnModel().getColumn(6).setMaxWidth(70);
        cartTable.getColumnModel().getColumn(2).setCellRenderer(new QuantityButtonCell("-"));
        cartTable.getColumnModel().getColumn(4).setCellRenderer(new QuantityButtonCell("+"));
        cartTable.getColumnModel().getColumn(2).setCellEditor(new QuantityButtonEditor("-", false));
        cartTable.getColumnModel().getColumn(4).setCellEditor(new QuantityButtonEditor("+", true));
        cartTableModel.addTableModelListener(e -> handleManualQuantityEdit(e.getFirstRow(), e.getColumn()));
        JScrollPane listScroll = new JScrollPane(cartTable);
        listScroll.setPreferredSize(new Dimension(420, 280));

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

    /**
     * Construye los controles de acción para el carrito (Eliminar, Cancelar compra).
     * Se limitó la altura máxima (setMaximumSize) para evitar que BoxLayout 
     * estire los botones desproporcionadamente.
     * * @return JPanel con los botones de acción del carrito.
     */
    private JPanel buildCartActions() {
        JPanel actions = new JPanel(new GridLayout(1, 2, 5, 3));
        actions.setOpaque(false);
        
        // RESTRICCIÓN DE ALTURA AÑADIDA AQUÍ
        // Evita que el panel ocupe el resto del espacio disponible verticalmente
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        actions.setPreferredSize(new Dimension(300, 40));

        JButton remove = new JButton("Eliminar");
        JButton cancel = new JButton("Cancelar compra");
        
        UiTheme.styleButton(remove, UiTheme.BTN_DARK, UiTheme.BTN_DARK_HOVER, UiTheme.TEXT_LIGHT, 100, 10, 12, true, false, 8);
        UiTheme.styleButton(cancel, new java.awt.Color(220, 38, 38), new java.awt.Color(185, 28, 28), UiTheme.TEXT_LIGHT, 100, 10, 12, true, false, 8);

        remove.addActionListener(e -> removeSelectedCartItem());
        cancel.addActionListener(e -> cancelSale());
        actions.add(remove);
        actions.add(cancel);
        return actions;
    }

    /**
     * Filtra y recarga la cuadrícula del catálogo basándose en el texto 
     * introducido en el campo de búsqueda.
     */
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

    /**
     * Crea y añade un botón que representa una tarjeta de producto/servicio al gridPanel.
     * * @param item Objeto ServiceItemEntry con los datos a mostrar.
     */
    private void addServiceCard(ServiceItemEntry item) {
        JButton card = new JButton("<html><center>" + item.name() + "<br/>" + currency.format(item.price()) + "</center></html>");
        card.setHorizontalAlignment(SwingConstants.CENTER);
        card.setFont(UiTheme.BODY_FONT.deriveFont(Font.BOLD));
        card.setPreferredSize(new Dimension(190, 120));
        card.setBackground(UiTheme.PANEL_WHITE);
        card.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER_COLOR));
        // Al hacer clic, se añade el elemento al carrito
        card.addActionListener(e -> addItemToSale(item));
        gridPanel.add(card);
    }

    /**
     * Añade un producto o servicio al carrito de compras actual.
     * Si el servicio tiene precios variantes por tamaño, abre un diálogo selector.
     * * @param item Objeto a añadir a la venta.
     */
    private void addItemToSale(ServiceItemEntry item) {
        PricedSaleSelection selection = resolvePricedSelection(item);
        if (selection == null) {
            return;
        }
        if (item.stock() != null && item.stock() <= 0) {
            JOptionPane.showMessageDialog(this, "Producto agotado. No hay stock disponible.");
            return;
        }
        SaleItemType itemType = item.saleItemType();
        SaleItemDTO saleItem = new SaleItemDTO(selection.name(), selection.price(), item.id(), item.inventoryItemId(),
                item.sku(), itemType, item.category(), item.stock(), item.gain(), 1);
        if (itemType.requiresAppointment()) {
            ScheduledService scheduledService = openScheduleDialog(saleItem);
            if (scheduledService == null) {
                JOptionPane.showMessageDialog(this, "El servicio no se agregó porque el agendamiento fue cancelado.",
                        "Agendamiento requerido", JOptionPane.WARNING_MESSAGE);
                return;
            }
            saleItem = saleItem.withAppointment(scheduledService.appointment())
                    .withServiceOrder(scheduledService.serviceOrder());
        }
        try {
            controller.addItem(saleItem);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
            return;
        }
        refreshCartTable();
        updateTotals();
    }

    /**
     * Abre el diálogo de administración general de servicios.
     */
    private void openGestionDialog() {
        java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
        new GestionVentasServiciosDialog(w instanceof javax.swing.JFrame ? (javax.swing.JFrame) w : null).setVisible(true);
    }

    /**
     * Abre el diálogo de configuración de precios dinámicos por tamaño.
     */
    private void openSizesDialog() {
        java.awt.Window w = javax.swing.SwingUtilities.getWindowAncestor(this);
        new PreciosPorTamanoDialog(w instanceof javax.swing.JFrame ? (javax.swing.JFrame) w : null).setVisible(true);
    }

    /**
     * Actualiza las etiquetas numéricas de subtotal, impuestos (IVA) y total
     * consultando el controlador de ventas.
     */
    private void updateTotals() {
        subtotalValueLabel.setText(currency.format(controller.getSubtotal()));
        taxValueLabel.setText(currency.format(controller.getTax()));
        totalValueLabel.setText(currency.format(controller.getTotal()));
    }

    /**
     * Elimina del carrito el elemento que esté seleccionado en el JList (lista visual).
     */
    private void removeSelectedCartItem() {
        int index = cartTable.getSelectedRow();
        if (!controller.removeItemAt(index)) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto del carrito para eliminar.");
            return;
        }
        refreshCartTable();
        updateTotals();
    }

    /**
     * Cancela toda la venta actual limpiando el carrito, previo aviso de confirmación.
     */
    private void cancelSale() {
        if (controller.getItemDTOs().isEmpty()) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "¿Cancelar toda la compra actual?", "Cancelar compra",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            controller.clearSale();
            refreshCartTable();
            updateTotals();
        }
    }

    /**
     * Procesa la venta. Si hay servicios, primero solicita el agendamiento;
     * después confirma el método de pago y registra todo transaccionalmente.
     */
    private void registerSale() {
        if (controller.getItemDTOs().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay items en el carrito.");
            return;
        }

        try {
            String paymentMethod = requestPaymentMethod();
            if (paymentMethod == null) {
                return;
            }

            SaleDTO sale = new SaleDTO(null, java.time.LocalDateTime.now(), "Mostrador", controller.getTotal(),
                    paymentMethod, "", controller.getItemDTOs());
            List<AppointmentDTO> appointmentDTOs = controller.getItemDTOs().stream()
                    .map(SaleItemDTO::appointment)
                    .filter(java.util.Objects::nonNull)
                    .toList();
            registerSaleUseCase.register(new RegisterSaleCommand(sale, appointmentDTOs));
                    
            // Limpiar interfaz
            controller.clearSale();
            refreshCartTable();
            searchField.setText("");
            catalog.reload();
            updateTotals();
            
            JOptionPane.showMessageDialog(this, "Venta realizada correctamente.");
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo registrar venta: " + ex.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshCartTable() {
        if (cartTableModel == null) {
            return;
        }
        cartTableModel.setRowCount(0);
        for (SaleItemDTO item : controller.getItemDTOs()) {
            cartTableModel.addRow(new Object[]{
                    item.name(),
                    currency.format(item.price()),
                    "-",
                    item.quantity(),
                    "+",
                    currency.format(item.subtotal()),
                    item.stockAvailable() == null ? "-" : Math.max(0, item.stockAvailable() - item.quantity())
            });
        }
    }

    private void handleManualQuantityEdit(int row, int column) {
        if (column != 3 || row < 0 || row >= controller.getItemDTOs().size()) {
            return;
        }
        Object value = cartTableModel.getValueAt(row, column);
        try {
            int quantity = Integer.parseInt(String.valueOf(value).trim());
            controller.updateQuantity(row, quantity);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage() == null ? "Cantidad inválida." : ex.getMessage());
        }
        refreshCartTable();
        updateTotals();
    }

    private String requestPaymentMethod() {
        Object[] methods = {"Efectivo", "Tarjeta", "Transferencia", "Nequi", "Daviplata"};
        Object selected = JOptionPane.showInputDialog(this, "Método de pago", "POS", JOptionPane.PLAIN_MESSAGE,
                null, methods, methods[0]);
        return selected == null ? null : selected.toString();
    }

    private ScheduledService openScheduleDialog(SaleItemDTO item) {
        java.awt.Window window = javax.swing.SwingUtilities.getWindowAncestor(this);
        ServiceAppointmentDialog dialog = new ServiceAppointmentDialog(window, item, appointmentService, ownerService);
        dialog.setVisible(true);
        if (!dialog.isSaved() || dialog.getPreparedAppointment() == null || dialog.getPreparedServiceOrder() == null) {
            return null;
        }
        return new ScheduledService(dialog.getPreparedAppointment(), dialog.getPreparedServiceOrder());
    }

    private record ScheduledService(AppointmentDTO appointment, ServiceOrderDTO serviceOrder) {
    }

    private PricedSaleSelection resolvePricedSelection(ServiceItemEntry item) {
        double price = item.price();
        String name = item.name();
        Map<String, Double> sizePrices = item.sizePrices();

        if (sizePrices != null && !sizePrices.isEmpty()) {
            List<SizePriceOption> options = new ArrayList<>();
            for (Map.Entry<String, Double> entry : sizePrices.entrySet()) {
                options.add(new SizePriceOption(entry.getKey(), entry.getValue()));
            }
            Object selected = JOptionPane.showInputDialog(this, "Selecciona tamaño", "Precios por Tamaño",
                    JOptionPane.PLAIN_MESSAGE, null, options.toArray(), options.get(0));
            if (selected == null) {
                return null;
            }
            SizePriceOption option = (SizePriceOption) selected;
            price = option.price();
            name = item.name() + " - " + option.size();
        }

        if (price <= 0d) {
            JOptionPane.showMessageDialog(this, "El precio del servicio debe ser mayor que cero.",
                    "Precio inválido", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return new PricedSaleSelection(name, price);
    }

    private record PricedSaleSelection(String name, double price) { }

    private final class SizePriceOption {
        private final String size;
        private final double price;

        private SizePriceOption(String size, double price) {
            this.size = size;
            this.price = price;
        }

        private String size() {
            return size;
        }

        private double price() {
            return price;
        }

        @Override
        public String toString() {
            return size + " - " + currency.format(price);
        }
    }

    /**
     * Utilidad para alinear a la derecha el texto dentro de un JLabel y darle formato en negrita.
     * * @param label JLabel a modificar.
     * @return El mismo JLabel ya modificado.
     */
    private JLabel rightAligned(JLabel label) {
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        label.setFont(UiTheme.BODY_FONT.deriveFont(Font.BOLD));
        return label;
    }

    private final class QuantityButtonCell extends JButton implements TableCellRenderer {
        private QuantityButtonCell(String text) {
            super(text);
            setFocusPainted(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            setText(String.valueOf(value));
            return this;
        }
    }

    private final class QuantityButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton button;

        private QuantityButtonEditor(String text, boolean increment) {
            this.button = new JButton(text);
            button.setFocusPainted(false);
            button.addActionListener(e -> {
                int row = cartTable.getEditingRow();
                try {
                    if (increment) {
                        controller.incrementQuantity(row);
                    } else {
                        controller.decrementQuantity(row);
                    }
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(SalesPanel.this, ex.getMessage());
                }
                fireEditingStopped();
                refreshCartTable();
                updateTotals();
            });
        }

        @Override
        public Object getCellEditorValue() {
            return button.getText();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {
            return button;
        }
    }
}
