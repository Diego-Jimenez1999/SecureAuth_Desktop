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
    
    /** Paneles de los módulos del sistema. */
    private final UserPanel userPanel;
    private final HomeDashboardPanel homePanel;
    private final ClientsPanel clientsPanel;
    private final RegMascotaPanel mascotaRegistroPanel;
    private final SalesPanel salesPanel;
    private final PanelConfig configPanel;
    private final PanelInventory inventoryPanel;
    private final PanelReports panelReports;
    private final PetController petController;
    private final InventoryController inventoryController;
    private final ReportController reportController;
    
    /** Gestor de capas para el intercambio dinámico de vistas. */
    private final CardLayout contentLayout;
    /** Contenedor principal donde se apilan los módulos. */
    private final JPanel contentPanel;

    /** Panel lateral de navegación con botones de foco centralizados. */
    private SidebarPanel sidebarPanel;

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
        this.homePanel = new HomeDashboardPanel(usuario, appContext.getSalesTransactionService(),
                appContext.getOwnerService(), appContext.getUserService(), appContext.getInventoryService(),
                appContext.getActividadRecienteService(), appContext.getAppointmentService());
        this.clientsPanel = new ClientsPanel();
        this.mascotaRegistroPanel = new RegMascotaPanel();
        this.petController = new PetController(mascotaRegistroPanel, appContext.getPetService(), appContext.getOwnerService());
        this.userPanel = new UserPanel(this, appContext.getOwnerService(), this.petController::reloadOwners);
        this.salesPanel = new SalesPanel(new SalesController(), subServiceSelector,
                appContext.getSalesTransactionService(), appContext.getAppointmentService(),
                appContext.getOwnerService());
        this.configPanel = new PanelConfig(appContext.getUserService(), this.controller);
        this.inventoryPanel = new PanelInventory();
        this.panelReports = new PanelReports();
        this.inventoryController = new InventoryController(this.inventoryPanel, appContext.getInventoryService());
        this.reportController = new ReportController(this.panelReports,
                appContext.getSalesTransactionService(), appContext.getOwnerService());
        this.reportController.loadMetrics();
        this.contentLayout = new CardLayout();
        this.contentPanel = new JPanel(contentLayout);

        initComponents();
        setupFrame();
        this.controller.bindView(this, usuario);
    }

    /**
     * Configura el layout principal y ensambla el {@link SidebarPanel} con sus respectivos 
     * callbacks de navegación vinculados a los métodos {@code mostrar...} de esta clase.
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        this.sidebarPanel = new SidebarPanel(usuarioActual, controller, this::mostrarHome, this::mostrarUsuarios,
                this::mostrarMascotas, this::mostrarInventario, this::mostrarVentas, this::mostrarConfiguracion, this::mostrarReportes);
        add(sidebarPanel, BorderLayout.WEST);
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
        contentPanel.add(clientsPanel, PANEL_CLIENTES);
        contentPanel.add(userPanel, PANEL_USUARIOS);
        contentPanel.add(mascotaRegistroPanel, PANEL_MASCOTAS);
        contentPanel.add(salesPanel, PANEL_VENTAS);
        contentPanel.add(configPanel, PANEL_CONFIGURACION);
        contentPanel.add(inventoryPanel, PANEL_INVENTARIO);
        contentPanel.add(panelReports, PANEL_REPORTES);
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

    /**
     * Retorna tabla para el controlador.
     *
     * @return JTable principal
     */
    public JTable getTable() {
        return userPanel.getTable();
    }

    /**
     * Retorna texto de búsqueda superior.
     *
     * @return texto de filtro
     */
    public String getTextoBusqueda() {
        return userPanel.getTextoBusqueda();
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
        contentLayout.show(contentPanel, PANEL_USUARIOS);
    }

    /** Muestra el módulo de registro de mascotas. */
    public void mostrarMascotas() {
        contentLayout.show(contentPanel, PANEL_MASCOTAS);
    }

    /**
     * Cambia la vista al módulo de Gestión de Inventario.
     */
    public void mostrarInventario() {
        System.out.println("[DEBUG] Navegando al módulo de Inventario");
        contentLayout.show(contentPanel, PANEL_INVENTARIO);
    }

    /**
     * Cambia la vista al módulo de Punto de Venta (POS).
     */
    public void mostrarVentas() {
        System.out.println("[DEBUG] Navegando al módulo de Ventas desde IngresoFrame");
        contentLayout.show(contentPanel, PANEL_VENTAS);
    }

    /**
     * Cambia la vista al panel de configuración.
     */
    public void mostrarConfiguracion() {
        System.out.println("[DEBUG] Navegando a Panel de Configuración desde IngresoFrame");
        contentLayout.show(contentPanel, PANEL_CONFIGURACION);
    }

    /**
     * Cambia la vista al módulo de reportes.
     */
    public void mostrarReportes() {
        contentLayout.show(contentPanel, PANEL_REPORTES);
    }

    /**
     * Muestra el módulo según el nombre centralizado.
     */
    public void showModule(String moduleName) {
        if (moduleName == null) return;
        switch (moduleName.toUpperCase().trim()) {
            case "DASHBOARD", "HOME" -> mostrarHome();
            case "USUARIOS" -> mostrarUsuarios();
            case "MASCOTAS" -> mostrarMascotas();
            case "INVENTARIO" -> mostrarInventario();
            case "VENTAS" -> mostrarVentas();
            case "CONFIGURACION", "CONFIG" -> mostrarConfiguracion();
            case "REPORTES" -> mostrarReportes();
        }
        if (sidebarPanel != null) {
            sidebarPanel.setFocusOnModule(moduleName);
        }
    }
}
