package secureauth.ui.utils;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Utilidades de tema visual reutilizables para la capa de UI.
 */
public final class UiTheme {

    // Colores principales usados en el proyecto
    public static final Color ACCENT_RED = new Color(198, 40, 40);
    public static final Color DARK_PRIMARY = new Color(23, 33, 49);
    public static final Color DARK_SIDEBAR = new Color(30, 36, 48);
    public static final Color DARK_HOVER = new Color(60, 70, 90);
    public static final Color TEXT_LIGHT = Color.WHITE;
    public static final Color TEXT_MUTED = new Color(200, 200, 200);
    public static final Color BG_LIGHT = new Color(0xE0, 0xE0, 0xE0);
    public static final Color PANEL_WHITE = new Color(0xFF, 0xFF, 0xFF);
    public static final Color SUBTLE_BORDER = new Color(0xD5, 0xD5, 0xD5);
    public static final Color FOREST_GREEN = new Color(0x28, 0xA7, 0x45);
    public static final Color FOREST_GREEN_HOVER = new Color(0x21, 0x8A, 0x3A);

    private UiTheme() {
        throw new IllegalStateException("Clase utilitaria");
    }

    /**
     * Crea una fuente en negrita con el tamaño especificado.
     * @param size tamaño de la fuente
     * @return la fuente en negrita con el tamaño dado
     */
    public static Font bold(int size) {
        return new Font("SansSerif", Font.BOLD, size);
    }

    /**
     * Crea y estiliza un botón base con colores y estados hover/activo.
     */
    public static void styleButton(
            JButton btn,
            Color baseColor,
            Color hoverColor,
            Color activeColor,
            int width,
            int height,
            int fontSize,
            boolean filled,
            boolean leftAligned,
            int leftPadding) {

        btn.setPreferredSize(new Dimension(width, height));
        btn.setMaximumSize(new Dimension(width, height));
        btn.setFont(new Font("SansSerif", Font.PLAIN, fontSize));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, leftPadding, 10, leftPadding));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (leftAligned) {
            btn.setHorizontalAlignment(SwingConstants.LEFT);
        }

        btn.setContentAreaFilled(filled);
        btn.setOpaque(filled);

        btn.setBackground(baseColor);
        btn.setForeground(TEXT_LIGHT);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(baseColor);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                btn.setBackground(activeColor);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                btn.setBackground(hoverColor);
            }
        });

        btn.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                btn.setBackground(activeColor);
            }

            @Override
            public void focusLost(FocusEvent e) {
                btn.setBackground(baseColor);
            }
        });
    }

    /**
     * Crea un botón de sidebar dinámico basado en colores.
     */
    public static JButton createSidebarButton(
            String text,
            Color baseColor,
            Color hoverColor,
            Color activeColor,
            int width,
            int height) {

        JButton btn = new JButton(text);
        styleButton(btn, baseColor, hoverColor, activeColor, width, height, 14, true, true, 20);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        return btn;
    }

    /**
     * Crea un botón con icono escalado.
     */
    public static JButton createSidebarButtonWithIcon(
            String text,
            String imagePath,
            int iconWidth,
            int iconHeight,
            Color baseColor,
            Color hoverColor,
            Color activeColor,
            int width,
            int height) {

        JButton btn = createSidebarButton(text, baseColor, hoverColor, activeColor, width, height);
        ImageIcon icon = scaleImage(imagePath, iconWidth, iconHeight);
        if (icon != null) {
            btn.setIcon(icon);
        }
        return btn;
    }

    /**
     * Escala una imagen a un tamaño específico.
     */
    public static ImageIcon scaleImage(String path, int width, int height) {
        try {
            ImageIcon icon = new ImageIcon(UiTheme.class.getResource(path));
            Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) {
            return null;
        }
    }
}
