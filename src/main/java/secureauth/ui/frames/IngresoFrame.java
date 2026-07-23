package secureauth.ui.frames;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;

import secureauth.config.AppContext;
import secureauth.controller.IngresoController;
import secureauth.controller.InventoryController;
import secureauth.controller.PetController;
import secureauth.controller.ReportController;
import secureauth.controller.SalesController;
import secureauth.model.User;
import secureauth.ui.components.HomeDashboardPanel;
import secureauth.ui.components.PanelConfig;
import secureauth.ui.components.PanelInventory;
import secureauth.ui.components.PanelReports;
import secureauth.ui.components.RegMascotaPanel;
import secureauth.ui.components.SalesPanel;
import secureauth.ui.components.SidebarPanel;
import secureauth.ui.components.UserPanel;
import secureauth.ui.dialogs.SubServiceSelector;
import secureauth.ui.enterprise.ClientsPanel;

/**
 * Orquestador principal de la interfaz de usuario (Dashboard).
 * 
 * <p>Esta clase gestiona la navegación entre los diferentes módulos del sistema 
 * utilizando un {@link CardLayout}. Actúa como el punto de integración donde 
 * se inyectan los controladores y servicios para cada panel.</p>
 * 
 * @author Diego Alexander Gaviria Jimenez
 * @version 2.0
 * @see secureauth.ui.components.SidebarPanel
 * @see secureauth.ui.components.RegMascotaPanel
 */
public class IngresoFrame extends javax.swing.JFrame {

    private static final String PANEL_HOME = "home";
    private static final String PANEL_CLIENTES = "clientes";
    private static final String PANEL_USUARIOS = "usuarios";
    private static final String PANEL_MASCOTAS = "mascotas";
    private static final String PANEL_VENTAS = "ventas";
    private static final String PANEL_CONFIGURACION = "configuracion";
    private static final String PANEL_INVENTARIO = "inventario";
    private static final String PANEL_REPORTES = "reportes";

    /** Color de fondo corporativo para los paneles principales. */
    private final Color COLOR_BG = new Color(244, 246, 249);

    /** Información del usuario que inició la sesión. */
    private final User usuarioActual;
    /** Controlador maestro para la lógica de la sesión. */
    private final IngresoController controller;
    
    /** Contenedor de servicios y estado inyectados. */
    private final AppContext appContext;
    private final SubServiceSelector subServiceSelector;

    /** Paneles de los módulos del sistema. */
    private UserPanel userPanel;
    private final HomeDashboardPanel homePanel;
    private ClientsPanel clientsPanel;
    private RegMascotaPanel mascotaRegistroPanel;
    private SalesPanel salesPanel;
    private PanelConfig configPanel;
    private PanelInventory inventoryPanel;
    private PanelReports panelReports;
    private PetController petController;
    private InventoryController inventoryController;
    private ReportController reportController;
    
    /** Gestor de capas para el intercambio dinámico de vistas. */
    private final CardLayout contentLayout;
    /** Contenedor principal donde se apilan los módulos. */
    private final JPanel contentPanel;

    /**
     * Constructor del dashboard con dependencias inyectadas desde MainApp.
     *
     * @param controller controlador ya construido en el bootstrap
     * @param usuario usuario autenticado
     * @param subServiceSelector diálogo para selección de subservicios en ventas
     */
    public IngresoFrame(IngresoController controller, User usuario, SubServiceSelector subServiceSelector) {
        this(controller, usuario, subServiceSelector, new AppContext());
    }

    /**
     * Constructor principal con dependencias centralizadas por la aplicación.
     *
     * @param controller controlador de sesión y usuarios
     * @param usuario usuario autenticado
     * @param subServiceSelector diálogo para selección de subservicios en ventas
     * @param appContext contenedor de servicios compartidos
     */
    public IngresoFrame(IngresoController controller, User usuario, SubServiceSelector subServiceSelector,
            AppContext appContext) {
        this.controller = controller;
        this.usuarioActual = usuario;
        this.subServiceSelector = subServiceSelector;
        this.appContext = appContext;
        this.homePanel = new HomeDashboardPanel(usuario, appContext.getSalesTransactionService(),
                appContext.getOwnerService(), appContext.getUserService(), appContext.getInventoryService(),
                appContext.getActividadRecienteService(), appContext.getAppointmentService());
        this.contentLayout = new CardLayout();
        this.contentPanel = new JPanel(contentLayout);

        initComponents();
        setupFrame();

        // Carga inicial de datos para evitar pantalla vacía al inicio
        this.homePanel.refresh();

        this.controller.bindView(this, usuario);
    }

    /**
     * Configura el layout principal y ensambla el {@link SidebarPanel} con sus respectivos 
     * callbacks de navegación vinculados a los métodos {@code mostrar...} de esta clase.
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        add(new SidebarPanel(usuarioActual, controller, this::mostrarHome, this::mostrarUsuarios,
                this::mostrarMascotas, this::mostrarInventario, this::mostrarVentas, this::mostrarConfiguracion, this::mostrarReportes),
                BorderLayout.WEST);
        add(buildMainPanel(), BorderLayout.CENTER);
    }

    /**
     * Construye el panel central que contiene el {@code contentPanel} con todos los módulos registrados.
     * 
     * @return JPanel con el CardLayout configurado.
     */
    private JPanel buildMainPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_BG);
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));

        contentPanel.setOpaque(false);
        contentPanel.add(homePanel, PANEL_HOME);
        contentLayout.show(contentPanel, PANEL_HOME);
        panel.add(contentPanel, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Establece las propiedades básicas de la ventana como tamaño, título 
     * y posición inicial.
     */
    private void setupFrame() {
        setTitle("SecureAuth Dashboard");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    // =========================================================
    // GETTERS PARA LAZY LOADING (CARGA PEREZOSA)
    // =========================================================

    private ClientsPanel getClientsPanel() {
        if (clientsPanel == null) {
            clientsPanel = new ClientsPanel();
            contentPanel.add(clientsPanel, PANEL_CLIENTES);
        }
        return clientsPanel;
    }

    private UserPanel getUserPanel() {
        if (userPanel == null) {
            userPanel = new UserPanel(this, appContext.getOwnerService(), () -> {
                getPetController().reloadOwners();
            });
            contentPanel.add(userPanel, PANEL_USUARIOS);
        }
        return userPanel;
    }

    private RegMascotaPanel getMascotaRegistroPanel() {
        if (mascotaRegistroPanel == null) {
            mascotaRegistroPanel = new RegMascotaPanel();
            contentPanel.add(mascotaRegistroPanel, PANEL_MASCOTAS);
        }
        return mascotaRegistroPanel;
    }

    private PetController getPetController() {
        if (petController == null) {
            petController = new PetController(getMascotaRegistroPanel(), appContext.getPetService(), appContext.getOwnerService());
        }
        return petController;
    }

    private SalesPanel getSalesPanel() {
        if (salesPanel == null) {
            salesPanel = new SalesPanel(new SalesController(), subServiceSelector,
                    appContext.getSalesTransactionService(), appContext.getAppointmentService(),
                    appContext.getOwnerService());
            contentPanel.add(salesPanel, PANEL_VENTAS);
        }
        return salesPanel;
    }

    private PanelConfig getConfigPanel() {
        if (configPanel == null) {
            configPanel = new PanelConfig(appContext.getUserService(), this.controller,
                    appContext.getSalesTransactionService(), appContext.getOwnerService());
            contentPanel.add(configPanel, PANEL_CONFIGURACION);
        }
        return configPanel;
    }

    private PanelInventory getInventoryPanel() {
        if (inventoryPanel == null) {
            inventoryPanel = new PanelInventory();
            contentPanel.add(inventoryPanel, PANEL_INVENTARIO);
        }
        return inventoryPanel;
    }

    private InventoryController getInventoryController() {
        if (inventoryController == null) {
            inventoryController = new InventoryController(getInventoryPanel(), appContext.getInventoryService());
        }
        return inventoryController;
    }

    private PanelReports getPanelReports() {
        if (panelReports == null) {
            panelReports = new PanelReports();
            contentPanel.add(panelReports, PANEL_REPORTES);
        }
        return panelReports;
    }

    private ReportController getReportController() {
        if (reportController == null) {
            reportController = new ReportController(getPanelReports(),
                    appContext.getSalesTransactionService(), appContext.getOwnerService());
            reportController.loadMetrics();
        }
        return reportController;
    }

    /**
     * Retorna tabla para el controlador.
     *
     * @return JTable principal
     */
    public JTable getTable() {
        return getUserPanel().getTable();
    }

    /**
     * Retorna texto de búsqueda superior.
     *
     * @return texto de filtro
     */
    public String getTextoBusqueda() {
        return getUserPanel().getTextoBusqueda();
    }

    /**
     * Retorna usuario autenticado.
     *
     * @return usuario de sesión
     */
    public User getUsuarioActual() {
        return usuarioActual;
    }

    public void mostrarHome() {
        homePanel.refresh();
        contentLayout.show(contentPanel, PANEL_HOME);
    }

    /** Muestra el módulo de gestión de usuarios. */
    public void mostrarUsuarios() {
        getUserPanel();
        contentLayout.show(contentPanel, PANEL_USUARIOS);
    }

    /** Muestra el módulo de registro de mascotas. */
    public void mostrarMascotas() {
        getMascotaRegistroPanel();
        getPetController();
        contentLayout.show(contentPanel, PANEL_MASCOTAS);
    }

    /**
     * Cambia la vista al módulo de Gestión de Inventario.
     */
    public void mostrarInventario() {
        System.out.println("[DEBUG] Navegando al módulo de Inventario");
        getInventoryPanel();
        getInventoryController();
        contentLayout.show(contentPanel, PANEL_INVENTARIO);
    }

    /**
     * Cambia la vista al módulo de Punto de Venta (POS).
     */
    public void mostrarVentas() {
        System.out.println("[DEBUG] Navegando al módulo de Ventas desde IngresoFrame");
        getSalesPanel();
        contentLayout.show(contentPanel, PANEL_VENTAS);
    }

    /**
     * Cambia la vista al panel de configuración.
     */
    public void mostrarConfiguracion() {
        System.out.println("[DEBUG] Navegando a Panel de Configuración desde IngresoFrame");
        getConfigPanel().loadConfigMetrics();
        contentLayout.show(contentPanel, PANEL_CONFIGURACION);
    }

    /**
     * Cambia la vista al módulo de reportes.
     */
    public void mostrarReportes() {
        getPanelReports();
        getReportController();
        contentLayout.show(contentPanel, PANEL_REPORTES);
    }
}
