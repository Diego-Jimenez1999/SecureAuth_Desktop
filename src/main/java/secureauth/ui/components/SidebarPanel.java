package secureauth.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
 * * <p>Esta clase implementa un patrón de diseño basado en callbacks (delegación) 
 * utilizando {@link Runnable}. No conoce la implementación de los módulos, 
 * simplemente notifica al orquestador ({@code IngresoFrame}) cuando se debe 
 * cambiar la vista.</p>
 * * @author Diego Alexander Gaviria Jimenez
 * @version 2.0
 * @see secureauth.ui.frames.IngresoFrame
 */
public class SidebarPanel extends JPanel {

    private final User usuarioActual;
    private final IngresoController controller;
    
    private final Runnable onHomeClick;
    private final Runnable onUsersClick;
    private final Runnable onPetsClick;
    private final Runnable onInventoryClick;
    private final Runnable onSalesClick;
    private final Runnable onSettingsClick;
    private final Runnable onReportesClick;
    
    /** Referencia al botón que se encuentra resaltado visualmente. */
    private JButton botonActivo;

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

    public SidebarPanel(User user, IngresoController controller, Runnable onHome,
                        Runnable onPets, Runnable onInventory, Runnable onReportes, Runnable onSettings) {
        this(user, controller, onHome, onHome, onPets, onInventory, null, onSettings, onReportes);
    }

    private void init() {
        setBackground(UiTheme.DARK_SIDEBAR);
        setPreferredSize(new Dimension(UiTheme.SIDEBAR_WIDTH, getHeight()));
        setLayout(new BorderLayout());

        add(buildTopPanel(), BorderLayout.NORTH);
        add(buildMenuPanel(), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildTopPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(new EmptyBorder(30, 10, 30, 10));

        JLabel lblImage = new JLabel();
        lblImage.setPreferredSize(new Dimension(150, 130));
        lblImage.setIcon(UiTheme.scaleImage("/logo_2.png", 150, 130));
        lblImage.setAlignmentX(Component.CENTER_ALIGNMENT);

        String nombre = (usuarioActual != null) ? usuarioActual.getNombre() : "invitado";
        JLabel lblTitle = new JLabel(nombre);
        lblTitle.setForeground(UiTheme.TEXT_LIGHT);
        lblTitle.setFont(UiTheme.bold(18));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitle = new JLabel(UiTheme.appSlogan().toUpperCase());
        lblSubtitle.setForeground(UiTheme.TEXT_MUTED);
        lblSubtitle.setFont(UiTheme.SMALL_FONT);
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        topPanel.add(lblImage);
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(lblTitle);
        topPanel.add(lblSubtitle);

        return topPanel;
    }

    private JPanel buildMenuPanel() {
        JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
        menuPanel.setOpaque(false);

        JButton btnHome = createSidebarButton("  Home", true, "/icon/home.png");
        JButton btnUsuarios = createSidebarButton("  Personal", false, "/icon/usuario.png");
        JButton btnMascotas = createSidebarButton("   Mascotas", false, "/icon/huella.png");
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

        btnHome.addActionListener(e -> {
            cambiarBotonActivo(btnHome);
            if (onHomeClick != null) onHomeClick.run();
        });
        btnUsuarios.addActionListener(e -> {
            cambiarBotonActivo(btnUsuarios);
            if (onUsersClick != null) onUsersClick.run();
        });
        btnMascotas.addActionListener(e -> {
            cambiarBotonActivo(btnMascotas);
            if (onPetsClick != null) onPetsClick.run();
        });
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

    private void confirmarLogout() {
        int confirm = JOptionPane.showConfirmDialog(this, "¿Seguro que deseas cerrar sesión?", "Confirmar",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            controller.logout();
        }
    }

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
                Color.RED,
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
            btn.setBackground(Color.RED); 
            btn.setForeground(UiTheme.TEXT_LIGHT);
        } else {
            btn.setForeground(UiTheme.TEXT_MUTED);
        }

        // Solo un ligero efecto gris al pasar el ratón para saber que es clickeable, 
        // pero NUNCA se pinta de rojo a menos que sea el botón activo.
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

        return btn;
    }

    
    /**
     * Gestiona el cambio de estado visual asegurando que solo un botón quede rojo de forma estática.
     * Lo dejamos 'public' por si necesitas forzar el cambio desde otra vista o controlador.
     */
    public void cambiarBotonActivo(JButton nuevoBoton) {
        if (botonActivo != null) {
            botonActivo.setBackground(UiTheme.DARK_SIDEBAR);
            botonActivo.setForeground(UiTheme.TEXT_MUTED);
            botonActivo.setOpaque(false);
        }

        nuevoBoton.setBackground(Color.RED);
        nuevoBoton.setForeground(UiTheme.TEXT_LIGHT);
        nuevoBoton.setOpaque(true);
        
        botonActivo = nuevoBoton;
    }
}