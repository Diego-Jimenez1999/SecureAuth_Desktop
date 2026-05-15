package secureauth.ui.components;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import secureauth.controller.IngresoController;
import secureauth.model.User;
import secureauth.ui.utils.UiTheme;

/**
 * Panel de navegación lateral (Sidebar) del dashboard principal.
 * 
 * <p>Esta clase implementa un patrón de diseño basado en callbacks (delegación) 
 * utilizando {@link Runnable}. No conoce la implementación de los módulos, 
 * simplemente notifica al orquestador ({@code IngresoFrame}) cuando se debe 
 * cambiar la vista.</p>
 * 
 * @author Diego Alexander Gaviria Jimenez
 * @version 2.0
 * @see secureauth.ui.frames.IngresoFrame
 */
public class SidebarPanel extends JPanel {

    /** Usuario que mantiene la sesión activa. */
    private final User usuarioActual;
    /** Controlador para acciones globales como el logout. */
    private final IngresoController controller;
    
    /** Callbacks de navegación inyectados desde el frame principal. */
    private final Runnable onHomeClick;
    private final Runnable onUsersClick;
    private final Runnable onPetsClick;
    private final Runnable onInventoryClick;
    private final Runnable onSalesClick;
    private final Runnable onSettingsClick;
    private final Runnable onReportesClick;
    
    /** Referencia al botón que se encuentra resaltado visualmente. */
    private JButton botonActivo;

    /**
     * Constructor principal que inicializa la barra lateral con sus dependencias y rutas de navegación.
     * 
     * @param user Usuario autenticado para mostrar en el perfil.
     * @param controller Controlador para la gestión de la sesión.
     * @param onHome Acción para mostrar el panel de usuarios/inicio.
     * @param onPets Acción para mostrar el registro de mascotas.
     * @param onInventory Acción para mostrar el inventario.
     * @param onSales Acción para mostrar el módulo de ventas.
     * @param onSettings Acción para mostrar la configuración del sistema.
     * @param onReportes Acción para mostrar el módulo de reportes.
     */
    public SidebarPanel(User user, IngresoController controller, Runnable onHome,
                        Runnable onUsers,
                        Runnable onPets, Runnable onInventory, Runnable onSales, Runnable onSettings, Runnable onReportes) {
        this.usuarioActual = user;
        this.controller = controller;
        this.onHomeClick = onHome;
        this.onUsersClick = onUsers;
        this.onPetsClick = onPets;
        this.onInventoryClick = onInventory;
        this.onSalesClick = onSales;
        this.onSettingsClick = onSettings;
        this.onReportesClick = onReportes;
        init();
    }

    /**
     * Constructor compatible con navegación base solicitada:
     * Home, Mascotas, Inventario, Reportes y Configuración.
     *
     * <p>Ventas queda opcionalmente deshabilitado al no inyectar callback.</p>
     */
    public SidebarPanel(User user, IngresoController controller, Runnable onHome,
                        Runnable onPets, Runnable onInventory, Runnable onReportes, Runnable onSettings) {
        this(user, controller, onHome, onHome, onPets, onInventory, null, onSettings, onReportes);
    }

    /**
     * Configura las propiedades visuales básicas y ensambla las regiones del panel.
     */
    private void init() {
        setBackground(UiTheme.DARK_SIDEBAR);
        setPreferredSize(new Dimension(UiTheme.SIDEBAR_WIDTH, getHeight()));
        setLayout(new BorderLayout());

        add(buildTopPanel(), BorderLayout.NORTH);
        add(buildMenuPanel(), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);
    }

    /**
     * Construye la sección superior con el logo y la información del usuario logueado.
     * 
     * @return JPanel con el encabezado de perfil.
     */
    private JPanel buildTopPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(new EmptyBorder(30, 10, 30, 10));

        JLabel lblImage = new JLabel(UiTheme.scaleImage("/logo_2.png", 150, 150));
        lblImage.setPreferredSize(new Dimension(150, 150));
        lblImage.setAlignmentX(Component.CENTER_ALIGNMENT);

        String nombre = (usuarioActual != null) ? usuarioActual.getNombre() : "invitado";
        JLabel lblTitle = new JLabel(nombre);
        lblTitle.setForeground(UiTheme.TEXT_LIGHT);
        lblTitle.setFont(UiTheme.bold(18));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitle = new JLabel(UiTheme.APP_SUBTITLE.toUpperCase());
        lblSubtitle.setForeground(UiTheme.TEXT_MUTED);
        lblSubtitle.setFont(UiTheme.SMALL_FONT);
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        topPanel.add(lblImage);
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(lblTitle);
        topPanel.add(lblSubtitle);

        return topPanel;
    }

    /**
     * Construye el cuerpo del menú con los botones de acceso a los módulos.
     * 
     * @return JPanel con la lista de botones de navegación.
     */
    private JPanel buildMenuPanel() {
        JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
        menuPanel.setOpaque(false);

        JButton btnHome = createSidebarButton("  Home", true, "/icon/home.png");
        JButton btnUsuarios = createSidebarButton("  Personal", false, "/icon/usuario.png");
        JButton btnMascotas = createSidebarButton("   Mascotas", false, "/icon/huella.png");
        
        // Botón de Inventario estilizado (Caja/Paquete)
        JButton btnInventario = createSidebarButton("  Inventario", false, "/icon/inventario.png");
        
        JButton btnVentas = createSidebarButton("  Ventas", false, "/icon/ventas.png");
        JButton btnReportes = createSidebarButton("  Reportes", false, "/icon/reportes.png");
        JButton btnConfig = createSidebarButton("  Configuración", false, "/icon/config.png");

        menuPanel.add(btnHome);
        menuPanel.add(btnUsuarios);
        menuPanel.add(btnMascotas);
        menuPanel.add(btnVentas);
        menuPanel.add(btnInventario);
        menuPanel.add(btnReportes);
        menuPanel.add(btnConfig);

        botonActivo = btnHome;

        /* Acciones de los botones */
        btnHome.addActionListener(e -> {
            cambiarBotonActivo(btnHome);
            if (onHomeClick != null) onHomeClick.run();// Home actúa como Usuarios en este flujo
        });
        btnUsuarios.addActionListener(e -> {
            cambiarBotonActivo(btnUsuarios);
            if (onUsersClick != null) onUsersClick.run();
        });
        btnMascotas.addActionListener(e -> {
            cambiarBotonActivo(btnMascotas);
            if (onPetsClick != null) onPetsClick.run();
        });
        
        // Acción del botón Inventario
        btnInventario.addActionListener(e -> {
            cambiarBotonActivo(btnInventario);
            if (onInventoryClick != null) onInventoryClick.run();
        });

        btnVentas.addActionListener(e -> {
            cambiarBotonActivo(btnVentas);
            if (onSalesClick != null) onSalesClick.run();
        });

        btnConfig.addActionListener(e -> {
            cambiarBotonActivo(btnConfig);
            if (onSettingsClick != null) onSettingsClick.run();
        });

        btnReportes.addActionListener(e -> {
            cambiarBotonActivo(btnReportes);
            if (onReportesClick != null) onReportesClick.run();
        });

        return menuPanel;
    }

    /**
     * Construye la sección inferior que contiene el estado de conexión y el botón de salida.
     * 
     * @return JPanel con las acciones de pie de página.
     */
    private JPanel buildBottomPanel() {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(new EmptyBorder(10, 20, 20, 20));

        String email = (usuarioActual != null) ? usuarioActual.getEmail() : "sin sesion";
        UserButtonPanel userPanel = new UserButtonPanel(email, "Conectado");

        JButton btnLogout = createSidebarButton("Cerrar Sesión", false, "/icon/cerrar_seccion.png");
        btnLogout.addActionListener(e -> confirmarLogout());

        bottomPanel.add(userPanel);
        bottomPanel.add(Box.createVerticalStrut(15));
        bottomPanel.add(btnLogout);

        return bottomPanel;
    }

    /**
     * Lanza un diálogo de confirmación antes de cerrar la sesión del usuario.
     */
    private void confirmarLogout() {
        int confirm = JOptionPane.showConfirmDialog(this, "¿Seguro que deseas cerrar sesión?", "Confirmar",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            controller.logout();
        }
    }

    /**
     * Factoría de botones para el sidebar. Aplica estilos de {@link UiTheme} y listeners de estado.
     * 
     * @param text Texto descriptivo del botón.
     * @param isActive Define si el botón inicia con el estado de selección activo.
     * @param texticon Ruta del recurso de imagen para el ícono.
     * @return JButton configurado con el diseño corporativo.
     * @see UiTheme#styleButton
     */
    private JButton createSidebarButton(String text, boolean isActive, String texticon) {
        JButton btn = new JButton(text);
        ImageIcon icon = UiTheme.scaleImage(texticon, 35, 35);
        if (icon != null) {
            btn.setIcon(icon);
        }

        UiTheme.styleButton(
                btn,
                UiTheme.DARK_SIDEBAR,
                UiTheme.DARK_HOVER,
                UiTheme.ACCENT_RED,
                220,
                45,
                14,
                false,
                true,
                20);

        btn.setFocusable(true);
        btn.setContentAreaFilled(false);
        btn.setOpaque(isActive);

        if (isActive) {
            btn.setBackground(UiTheme.ACCENT_RED);
            btn.setForeground(UiTheme.TEXT_LIGHT);
        } else {
            btn.setForeground(UiTheme.TEXT_MUTED);
        }

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn != botonActivo) {
                    btn.setOpaque(true);
                    btn.setBackground(UiTheme.DARK_HOVER);
                    btn.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn != botonActivo) {
                    btn.setOpaque(false);
                    btn.repaint();
                }
            }
        });

        btn.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (btn != botonActivo) {
                    btn.setOpaque(true);
                    btn.setBackground(UiTheme.ACCENT_RED);
                    btn.setForeground(UiTheme.TEXT_LIGHT);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (btn != botonActivo) {
                    btn.setOpaque(false);
                    btn.setForeground(UiTheme.TEXT_MUTED);
                }
            }
        });

        return btn;
    }

    /**
     * Gestiona el cambio de estado visual entre botones para indicar la sección actual.
     * 
     * @param nuevoBoton El botón que acaba de ser presionado.
     */
    private void cambiarBotonActivo(JButton nuevoBoton) {
        if (botonActivo != null) {
            botonActivo.setBackground(UiTheme.DARK_SIDEBAR);
            botonActivo.setForeground(UiTheme.TEXT_MUTED);
            botonActivo.setOpaque(false);
        }

        nuevoBoton.setBackground(UiTheme.ACCENT_RED);
        nuevoBoton.setForeground(UiTheme.TEXT_LIGHT);
        nuevoBoton.setOpaque(true);
        botonActivo = nuevoBoton;
    }
}
