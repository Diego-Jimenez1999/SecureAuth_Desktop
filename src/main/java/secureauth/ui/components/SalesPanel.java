package secureauth.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import secureauth.controller.SalesController;
import secureauth.model.SaleItem;
import secureauth.ui.dialogs.SubServiceSelector;

/**
 * Vista principal del módulo de ventas.
 *
 * <p>Implementa un {@link BorderLayout} con dos áreas:
 * catálogo en el CENTER y resumen de venta en el EAST.
 * El controlador se encarga del estado y esta vista
 * renderiza y propaga eventos de selección.
 * </p>
 */
public class SalesPanel extends JPanel {

    /** Controlador MVC con la lógica de cálculo y agregado de items al resumen. */
    private final SalesController controller;
    /** Formateador monetario para mostrar precios en formato local. */
    private final NumberFormat currency;

    /** Etiqueta visual para mostrar el subtotal acumulado. */
    private JLabel subtotalValueLabel;
    /** Etiqueta visual para mostrar el IVA calculado al 19%. */
    private JLabel taxValueLabel;
    /** Etiqueta visual para mostrar el total final de la venta. */
    private JLabel totalValueLabel;
    /** Contenedor dinámico donde se dibujan las cards de servicios. */
    private JPanel gridPanel;
    /** Campo de búsqueda para filtrar servicios por nombre. */
    private JTextField searchField;
    /** Scroll del catálogo para ajustar columnas dinámicamente por tamaño de ventana. */
    private JScrollPane catalogScrollPane;
    /** Mapa de subcategorías por servicio principal. */
    private final Map<String, List<SaleItem>> subServiceMap;
    /** Selector de sub-servicios inyectado desde bootstrap. */
    private final SubServiceSelector subServiceSelector;

    /**
     * Construye la vista de ventas con su controlador.
     *
     * @param controller controlador MVC del flujo de ventas
     */
    public SalesPanel(SalesController controller, SubServiceSelector subServiceSelector) {
        this.controller = controller;
        this.currency = NumberFormat.getCurrencyInstance(Locale.of("es", "CO"));
        this.subServiceMap = buildSubServiceMap();
        this.subServiceSelector = subServiceSelector;

        setLayout(new BorderLayout(16, 0));
        setOpaque(false);
        setBorder(new EmptyBorder(12, 0, 12, 0));

        add(buildCatalogContainer(), BorderLayout.CENTER);
        add(buildSummarySection(), BorderLayout.EAST);
    }

    /**
     * Crea la sección de catálogo de servicios.
     *
     * <p>Usa GridLayout de 3 columnas dentro de un scroll para
     * soportar más tarjetas sin romper el layout principal.
     * </p>
     *
     * @return panel scrollable con cards de servicios
     */
    private JPanel buildCatalogContainer() {
        JPanel container = new JPanel(new BorderLayout(0, 12));
        container.setOpaque(false);
        container.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 230, 237)),
                new EmptyBorder(16, 16, 16, 16)));

        container.add(buildCatalogHeader(), BorderLayout.NORTH);
        container.add(buildCatalogSection(), BorderLayout.CENTER);
        return container;
    }

    /**
     * Construye el encabezado visual del catálogo con título y buscador.
     *
     * @return panel superior del módulo de ventas
     */
    private JPanel buildCatalogHeader() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Ventas - Servicios y Productos");
        title.setFont(new Font("SansSerif", Font.BOLD, 34));
        title.setForeground(new Color(25, 31, 42));

        JLabel subtitle = new JLabel("Realiza la venta de servicios y productos para mascotas.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 16));
        subtitle.setForeground(new Color(94, 103, 116));

        searchField = new JTextField();
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        searchField.setPreferredSize(new Dimension(0, 38));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(206, 211, 219)),
                new EmptyBorder(6, 10, 6, 10)));
        searchField.setToolTipText("Buscar servicio...");

        searchField.addActionListener(e -> filterCatalog(searchField.getText()));

        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(subtitle);
        header.add(Box.createVerticalStrut(12));
        header.add(searchField);
        return header;
    }

    /**
     * Construye la sección de catálogo con tarjetas de servicios.
     * @return panel scrollable con cards de servicios
     */
    private JScrollPane buildCatalogSection() {
        gridPanel = new JPanel(new GridLayout(0, 3, 12, 12));
        gridPanel.setOpaque(false);
        gridPanel.setBorder(new EmptyBorder(4, 4, 4, 4));

        buildCatalogItems(item -> addServiceCard(gridPanel, item.iconLabel, item.name, item.price));

        catalogScrollPane = new JScrollPane(gridPanel);
        catalogScrollPane.setBorder(BorderFactory.createLineBorder(new Color(206, 211, 219)));
        catalogScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        catalogScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        catalogScrollPane.getVerticalScrollBar().setUnitIncrement(14);
        catalogScrollPane.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateCatalogColumns();
            }
        });
        return catalogScrollPane;
    }

    /**
     * Crea el panel de resumen de la venta.
     *
     * <p>Incluye lista de items, breakdown de valores y acción
     * principal para registrar la venta.
     * </p>
     *
     * @return panel lateral de resumen
     */
    private JPanel buildSummarySection() {
        JPanel summary = new JPanel(new BorderLayout());
        summary.setPreferredSize(new Dimension(300, 0));
        summary.setBackground(new Color(250, 250, 250));
        summary.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(178, 60, 60)),
                BorderFactory.createEmptyBorder()));

        JLabel panelTitle = new JLabel("Venta Actual");
        panelTitle.setOpaque(true);
        panelTitle.setBackground(new Color(23, 33, 49));
        panelTitle.setForeground(Color.WHITE);
        panelTitle.setFont(new Font("SansSerif", Font.BOLD, 34));
        panelTitle.setBorder(new EmptyBorder(14, 16, 14, 16));
        summary.add(panelTitle, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel title = new JLabel("Resumen de la Venta");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setAlignmentX(LEFT_ALIGNMENT);

        JList<String> itemList = new JList<>(controller.getListModel());
        JScrollPane listScroll = new JScrollPane(itemList);
        listScroll.setPreferredSize(new Dimension(260, 320));
        listScroll.setAlignmentX(LEFT_ALIGNMENT);
        listScroll.setBorder(BorderFactory.createLineBorder(new Color(188, 194, 204)));

        subtotalValueLabel = new JLabel(currency.format(0));
        taxValueLabel = new JLabel(currency.format(0));
        totalValueLabel = new JLabel(currency.format(0));

        JPanel totalsPanel = new JPanel(new GridLayout(3, 2, 8, 6));
        totalsPanel.setOpaque(false);
        totalsPanel.setAlignmentX(LEFT_ALIGNMENT);
        totalsPanel.add(new JLabel("Subtotal:"));
        totalsPanel.add(rightAligned(subtotalValueLabel));
        totalsPanel.add(new JLabel("IVA (19%):"));
        totalsPanel.add(rightAligned(taxValueLabel));
        totalsPanel.add(new JLabel("Total:"));
        totalsPanel.add(rightAligned(totalValueLabel));

        JButton registerButton = new JButton("🧾 Registrar Venta");
        registerButton.setPreferredSize(new Dimension(260, 44));
        registerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        registerButton.setAlignmentX(LEFT_ALIGNMENT);
        registerButton.setBackground(new Color(198, 40, 40));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        registerButton.setBorder(new EmptyBorder(10, 12, 10, 12)); 

        body.add(title);
        body.add(Box.createVerticalStrut(12));
        body.add(listScroll);
        body.add(Box.createVerticalStrut(12));
        body.add(totalsPanel);
        body.add(Box.createVerticalStrut(16));
        body.add(registerButton);
        summary.add(body, BorderLayout.CENTER);

        return summary;
    }

    /**
     * Agrega una card interactiva al grid.
     *
     * <p>Cuando el usuario hace clic, se delega al controlador
     * para agregar el servicio y luego se refrescan totales.
     * </p>
     *
     * @param parent contenedor del grid
     * @param icon marcador visual del servicio
     * @param name nombre del servicio
     * @param price valor del servicio
     */
    private void addServiceCard(JPanel parent, JLabel iconLabel, String name, double price) {
        JButton card = new JButton();
        card.setLayout(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 225, 225)),
                new EmptyBorder(10, 10, 10, 10)));
        card.setFocusPainted(false);
        card.setPreferredSize(new Dimension(190, 155));
        card.setMinimumSize(new Dimension(190, 155));

        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel nameLabel = new JLabel(name, SwingConstants.CENTER);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        JLabel priceLabel = new JLabel(currency.format(price), SwingConstants.CENTER);
        priceLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        priceLabel.setForeground(new Color(95, 95, 95));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        card.add(iconLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new java.awt.Insets(10, 0, 0, 0);
        card.add(nameLabel, gbc);

        gbc.gridy = 2;
        gbc.insets = new java.awt.Insets(4, 0, 0, 0);
        card.add(priceLabel, gbc);

        card.addActionListener(e -> {
            showSubServiceDialog(name, price);
        });

        parent.add(card);
    }

    /**
     * Refresca labels de subtotal, IVA y total según el estado actual.
     */
    private void updateTotals() {
        subtotalValueLabel.setText(currency.format(controller.getSubtotal()));
        taxValueLabel.setText(currency.format(controller.getTax()));
        totalValueLabel.setText(currency.format(controller.getTotal()));
    }

    private JLabel rightAligned(JLabel label) {
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        return label;
    }

    /**
     * Filtra las tarjetas del catálogo por texto.
     *
     * @param query término a buscar en nombre del servicio
     */
    private void filterCatalog(String query) {
        String normalized = query == null ? "" : query.trim().toLowerCase();
        gridPanel.removeAll();
        buildCatalogItems(item -> {
            if (normalized.isEmpty() || item.name.toLowerCase().contains(normalized)) {
                addServiceCard(gridPanel, item.iconLabel, item.name, item.price);
            }
        });
        gridPanel.revalidate();
        gridPanel.repaint();
        updateCatalogColumns();
    }
    
    /**
     * Construye la lista de servicios disponibles.
     * @param consumer función que recibe cada item para agregarlo al catálogo visualmente
     */
    private void buildCatalogItems(Consumer<ServiceCatalogItem> consumer) {
        consumer.accept(new ServiceCatalogItem(createIconLabel("/icon/hotel.png", "/icon/hotel.png", "🏠"), "Hotel", 150000));
        consumer.accept(new ServiceCatalogItem(createIconLabel("/icon/school.png", "/icon/usuario.png", "🏫"), "Colegio", 50000));
        consumer.accept(new ServiceCatalogItem(createIconLabel("/icon/daycare.png", "/icon/huella.png", "🐾"), "Guarderia", 40000));
        consumer.accept(new ServiceCatalogItem(createIconLabel("/icon/bathroom.png", "/icon/newpet.png", "🛁"), "Baño", 30000));
        consumer.accept(new ServiceCatalogItem(createIconLabel("/icon/consult.png", "/icon/inventario.png", "🩺"), "Consultas Medicas", 30000));
        consumer.accept(new ServiceCatalogItem(createIconLabel("/icon/surgery.png", "/icon/config.png", "🧪"), "Cirugías", 10000));
        consumer.accept(new ServiceCatalogItem(createIconLabel("/icon/training.png", "/icon/ventas.png", "🐕"), "Adiestramiento", 50000));
        consumer.accept(new ServiceCatalogItem(createIconLabel("/icon/food.png", "/icon/home.png", "🥫"), "Alimentos", 45000));
        consumer.accept(new ServiceCatalogItem(createIconLabel("/icon/accessories.png", "/icon/reportes.png", "🎀"), "Accesorios", 30000));
    }

    /**
     * Crea un JLabel de ícono reutilizable para cada card.
     *
     * @param iconText emoji o texto que representa el ícono
     * @return etiqueta configurada para usarse en la card
     */
    private JLabel createIconLabel(String iconPath, String fallbackIconPath, String fallbackText) {
        JLabel iconLabel = new JLabel(fallbackText, SwingConstants.CENTER);
        iconLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        iconLabel.setForeground(new Color(45, 53, 67));
        java.net.URL resource = getClass().getResource(iconPath);
        if (resource == null) {
            resource = getClass().getResource(fallbackIconPath);
        }
        if (resource != null) {
            ImageIcon icon = new ImageIcon(resource);
            Image scaled = icon.getImage().getScaledInstance(44, 44, Image.SCALE_SMOOTH);
            iconLabel.setText("");
            iconLabel.setIcon(new ImageIcon(scaled));
        }
        return iconLabel;
    }

    /**
     * Ajusta número de columnas según el ancho visible para mejorar
     * el comportamiento en ventanas grandes y pequeñas.
     */
    private void updateCatalogColumns() {
        if (catalogScrollPane == null || !(gridPanel.getLayout() instanceof GridLayout)) {
            return;
        }
        int width = catalogScrollPane.getViewport().getWidth();
        int cols = width >= 900 ? 3 : (width >= 580 ? 2 : 1);
        GridLayout layout = (GridLayout) gridPanel.getLayout();
        if (layout.getColumns() != cols) {
            layout.setColumns(cols);
            gridPanel.revalidate();
            gridPanel.repaint();
        }
    }

    /**
     * Muestra diálogo de subcategorías y notifica selección al flujo de venta.
     *
     * @param serviceName nombre del servicio principal pulsado
     * @param fallbackPrice precio base si no hay subcategorías
     */
    private void showSubServiceDialog(String serviceName, double fallbackPrice) {
        List<SaleItem> subServices = subServiceMap.get(serviceName);
        if (subServices == null || subServices.isEmpty()) {
            notifySubServiceSelected(new SaleItem(serviceName, fallbackPrice));
            return;
        }
        SaleItem selected = subServiceSelector.select(javax.swing.SwingUtilities.getWindowAncestor(this), serviceName,
                subServices);
        if (selected != null) {
            notifySubServiceSelected(selected);
        }
    }

    /**
     * Observer simple para desacoplar selección de sub-servicio y actualización de UI.
     *
     * @param item item seleccionado desde el diálogo
     */
    private void notifySubServiceSelected(SaleItem item) {
        controller.addItem(item);
        updateTotals();
    }

    /**
     * Construye el mapa de sub-servicios por categoría principal.
     *
     * @return mapeo categoría -> lista de sub-servicios
     */
    private Map<String, List<SaleItem>> buildSubServiceMap() {
        Map<String, List<SaleItem>> map = new HashMap<>();

        map.put("Hotel", List.of(
                new SaleItem("hotel - basico", 30000),
                new SaleItem("hotel - premium", 45000),
                new SaleItem("hotel - todo incluido", 65000)));
        
        map.put("Colegio", List.of(
                new SaleItem("school - basico", 30000),
                new SaleItem("school - premium", 45000),
                new SaleItem("school - todo incluido", 65000)));

        map.put("Guarderia", List.of(
                new SaleItem("guarderia - basico", 30000),
                new SaleItem("guarderia - premium", 45000),
                new SaleItem("guarderia - todo incluido", 65000)));


        map.put("Baño", List.of(
                new SaleItem("Baño - Normal", 30000),
                new SaleItem("Baño - Medicado", 45000),
                new SaleItem("Baño - Completo", 65000)));

        map.put("Consultas Medicas", List.of(
                new SaleItem("Consultas - General", 30000),
                new SaleItem("Consultas - Especialista", 60000),
                new SaleItem("Consultas - Urgencias", 90000)));

        map.put("Cirugías", List.of(
                new SaleItem("Cirugías - Esterilización", 180000),
                new SaleItem("Cirugías - Tejidos blandos", 350000),
                new SaleItem("Cirugías - Ortopedia", 650000)));

        map.put("Adiestramiento", List.of(
                new SaleItem("Adiestramiento - Básico", 30000),
                new SaleItem("Adiestramiento - Premium", 45000),
                new SaleItem("Adiestramiento - Todo Incluido", 65000)));
        
        map.put("Alimentos", List.of(
                new SaleItem("Alimentos - Básico", 45000),
                new SaleItem("Alimentos - Premium", 70000),
                new SaleItem("Alimentos - Super Premium", 120000)));
        
        map.put("Accesorios", List.of(
                new SaleItem("Accesorios - Básico", 30000),
                new SaleItem("Accesorios - Premium", 50000),
                new SaleItem("Accesorios - Lujo", 90000)));

        return map;
    }

    /**
     * DTO interno para transportar datos del catálogo.
     */
    private static class ServiceCatalogItem {
        /** Etiqueta con el ícono visual de la card. */
        private final JLabel iconLabel;
        /** Nombre del servicio que se mostrará al usuario. */
        private final String name;
        /** Precio del servicio usado para cálculos de venta. */
        private final double price;

        /**
         * Crea un item de catálogo.
         *
         * @param iconLabel etiqueta de ícono
         * @param name nombre del servicio
         * @param price precio base
         */
        private ServiceCatalogItem(JLabel iconLabel, String name, double price) {
            this.iconLabel = iconLabel;
            this.name = name;
            this.price = price;
        }
    }
}
