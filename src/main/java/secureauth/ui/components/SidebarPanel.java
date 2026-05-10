package secureauth.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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

public class SidebarPanel extends JPanel {

    private final User usuarioActual;
    private final IngresoController controller;
    private final Runnable onUsuariosClick;
    private final Runnable onMascotasClick;
    private final Runnable onVentasClick;
    private JButton botonActivo;

    public SidebarPanel(User usuarioActual, IngresoController controller, Runnable onUsuariosClick,
            Runnable onMascotasClick, Runnable onVentasClick) {
        this.usuarioActual = usuarioActual;
        this.controller = controller;
        this.onUsuariosClick = onUsuariosClick;
        this.onMascotasClick = onMascotasClick;
        this.onVentasClick = onVentasClick;
        init();
    }

    private void init() {
        setBackground(UiTheme.DARK_SIDEBAR);
        setPreferredSize(new Dimension(260, getHeight()));
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

        JLabel lblImage = new JLabel(UiTheme.scaleImage("/logo_2.png", 150, 150));
        lblImage.setPreferredSize(new Dimension(150, 150));
        lblImage.setAlignmentX(Component.CENTER_ALIGNMENT);

        String nombre = (usuarioActual != null) ? usuarioActual.getNombre() : "invitado";
        JLabel lblTitle = new JLabel(nombre);
        lblTitle.setForeground(UiTheme.TEXT_LIGHT);
        lblTitle.setFont(UiTheme.bold(18));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitle = new JLabel("DASHBOARD");
        lblSubtitle.setForeground(new Color(150, 150, 150));
        lblSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 10));
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
        JButton btnUsuarios = createSidebarButton("  Usuarios", false, "/icon/usuario.png");
        JButton btnMascotas = createSidebarButton("   Mascotas", false, "/icon/huella.png");
        JButton btnVentas = createSidebarButton("  Ventas", false, "/icon/ventas.png");
        JButton btnInventario = createSidebarButton("  Inventario", false, "/icon/inventario.png");
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

        btnHome.addActionListener(e -> cambiarBotonActivo(btnHome));
        btnUsuarios.addActionListener(e -> {
            cambiarBotonActivo(btnUsuarios);
            if (onUsuariosClick != null) {
                onUsuariosClick.run();
            }
        });
        btnMascotas.addActionListener(e -> {
            cambiarBotonActivo(btnMascotas);
            if (onMascotasClick != null) {
                onMascotasClick.run();
            }
        });
        btnVentas.addActionListener(e -> {
            cambiarBotonActivo(btnVentas);
            if (onVentasClick != null) {
                onVentasClick.run();
            }
        });
        btnInventario.addActionListener(e -> cambiarBotonActivo(btnInventario));
        btnReportes.addActionListener(e -> cambiarBotonActivo(btnReportes));
        btnConfig.addActionListener(e -> cambiarBotonActivo(btnConfig));

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
