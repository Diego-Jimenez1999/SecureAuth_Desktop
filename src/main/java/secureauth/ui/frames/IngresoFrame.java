package secureauth.ui.frames;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;

import secureauth.controller.IngresoController;
import secureauth.controller.MascotaController;
import secureauth.controller.SalesController;
import secureauth.model.User;
import secureauth.repository.MascotaRepositoryImpl;
import secureauth.service.MascotaService;
import secureauth.ui.components.SalesPanel;
import secureauth.ui.components.SidebarPanel;
import secureauth.ui.components.UserPanel;
import secureauth.ui.dialogs.SubServiceSelector;

/**
 * Vista principal del dashboard de usuarios.
 *
 * <p>
 * Esta clase ahora funciona como orquestador de componentes UI más pequeños:
 * SidebarPanel, UserPanel y MascotaRegistroFrame.
 * </p>
 *
 * @author Diego Alexander Gaviria Jimenez
 * @version 2.0
 */
public class IngresoFrame extends javax.swing.JFrame {

    private final Color COLOR_BG = new Color(244, 246, 249);

    private final User usuarioActual;
    private final IngresoController controller;
    private final UserPanel userPanel;
    private final MascotaRegistroFrame mascotaRegistroPanel;
    private final SalesPanel salesPanel;
    private final CardLayout contentLayout;
    private final JPanel contentPanel;

    /**
     * Constructor del dashboard con dependencias inyectadas desde MainApp.
     *
     * @param controller controlador ya construido en el bootstrap
     * @param usuario usuario autenticado
     */
    public IngresoFrame(IngresoController controller, User usuario, SubServiceSelector subServiceSelector) {
        this.controller = controller;
        this.usuarioActual = usuario;
        this.userPanel = new UserPanel(this, controller);
        MascotaService mascotaService = new MascotaService(new MascotaRepositoryImpl());
        MascotaController mascotaController = new MascotaController(mascotaService);
        this.mascotaRegistroPanel = new MascotaRegistroFrame(mascotaController);
        this.salesPanel = new SalesPanel(new SalesController(), subServiceSelector);
        this.contentLayout = new CardLayout();
        this.contentPanel = new JPanel(contentLayout);

        initComponents();
        setupFrame();
        this.controller.bindView(this, usuario);
        this.controller.cargarUsuarios();
    }

    /**
     * Inicializa layout principal.
     */
    private void initComponents() {
        setLayout(new BorderLayout());
        add(new SidebarPanel(usuarioActual, controller, this::mostrarUsuarios, this::mostrarMascotas, this::mostrarVentas),
                BorderLayout.WEST);
        add(buildMainPanel(), BorderLayout.CENTER);
    }

    private JPanel buildMainPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_BG);
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));

        contentPanel.setOpaque(false);
        contentPanel.add(userPanel, "usuarios");
        contentPanel.add(mascotaRegistroPanel, "mascotas");
        contentPanel.add(salesPanel, "ventas");
        contentLayout.show(contentPanel, "usuarios");
        panel.add(contentPanel, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Configuración de ventana.
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

    public void mostrarUsuarios() {
        contentLayout.show(contentPanel, "usuarios");
    }

    public void mostrarMascotas() {
        contentLayout.show(contentPanel, "mascotas");
    }

    /**
     * Cambia la vista del contenedor principal al módulo de ventas.
     */
    public void mostrarVentas() {
        contentLayout.show(contentPanel, "ventas");
    }
}
