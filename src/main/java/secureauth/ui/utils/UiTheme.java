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

import secureauth.ui.config.ApplicationVisualSettings;

/**
 * =========================================================
 * CLASE: UiTheme
 * =========================================================
 *
 * Clase centralizada de configuración visual reutilizable
 * para toda la interfaz gráfica del sistema SecureAuth.
 *
 * =========================================================
 * RESPONSABILIDADES
 * =========================================================
 *
 * Esta clase permite:
 *
 * ✅ Gestionar colores globales
 * ✅ Configurar tipografías
 * ✅ Gestionar logos e iconos
 * ✅ Crear botones reutilizables
 * ✅ Escalar imágenes
 * ✅ Mantener consistencia visual
 *
 * =========================================================
 * VENTAJAS
 * =========================================================
 *
 * ✔ Unificación del diseño
 * ✔ Fácil mantenimiento
 * ✔ Cambio rápido de branding
 * ✔ Reutilización de componentes
 * ✔ Arquitectura más limpia
 *
 * =========================================================
 *
 * EJEMPLO DE USO
 * =========================================================
 *
 * JButton btn = UiTheme.createSidebarButton(
 *      "Dashboard",
 *      UiTheme.DARK_PRIMARY,
 *      UiTheme.DARK_HOVER,
 *      UiTheme.themePrimary(),
 *      220,
 *      50
 * );
 *
 * =========================================================
 *
 * @author Diego
 * @version 2.0
 */
public final class UiTheme {

    /* =========================================================
     * CONSTRUCTOR PRIVADO
     * =========================================================
     */

    /**
     * Evita instancias de la clase utilitaria.
     */
    private UiTheme() {
        throw new IllegalStateException("Clase utilitaria");
    }

    /* =========================================================
     * INFORMACIÓN DE MARCA
     * =========================================================
     */

    /**
     * Nombre principal del sistema.
     */
    public static final String APP_NAME =
            "SecureAuth";

    /* =========================================================
     * RUTAS DE IMÁGENES
     * =========================================================
     */

    /**
     * Ruta del logo principal.
     */
    public static final String LOGO_PATH =
            "/assets/logo.png";

    /**
     * Ruta del icono principal.
     */
    public static final String ICON_PATH =
            "/assets/icon.png";

    /* =========================================================
     * COLORES PRINCIPALES
     * =========================================================
     */

    /**
     * Color principal oscuro.
     */
    public static final Color DARK_PRIMARY =
            new Color(23, 33, 49);

    /**
     * Color sidebar.
     */
    public static final Color DARK_SIDEBAR =
            new Color(30, 36, 48);

    /**
     * Color hover oscuro.
     */
    public static final Color DARK_HOVER =
            new Color(60, 70, 90);

    /**
     * Texto claro.
     */
    public static final Color TEXT_LIGHT =
            Color.WHITE;

    /**
     * Texto secundario.
     */
    public static final Color TEXT_MUTED =
            new Color(200, 200, 200);
            
    /**
     * Texto primario de alta visibilidad.
     */
    public static final Color TEXT_PRIMARY = new Color(0x111827);

    /**
     * Texto secundario para descripciones.
     */
    public static final Color TEXT_SECONDARY = new Color(0x6B7280);

    /**
     * Color de fondo de página tipo dashboard.
     */
    public static final Color BG_PAGE = new Color(0xF5F7F9);

    /**
     * Color de acento azul.
     */
    public static final Color ACCENT_BLUE = new Color(0x2563EB);

    /**
     * Color de acento ámbar.
     */
    public static final Color ACCENT_AMBER = new Color(0xD97706);

    /**
     * Color de acento púrpura.
     */
    public static final Color ACCENT_PURPLE = new Color(0x7C3AED);

    /**
     * Color para acciones de edición.
     */
    public static final Color EDIT_BLUE = new Color(0x3B82F6);
    
    /**
     * Color de borde estándar.
     */
    public static final Color BORDER_COLOR = new Color(0xE5E7EB);

    /**
     * Color de botón oscuro (sidebar/acciones).
     */
    public static final Color BTN_DARK = new Color(0x1F2937);

    /**
     * Color de hover para botones oscuros.
     */
    public static final Color BTN_DARK_HOVER = new Color(0x374151);

    /**
     * Fondo claro general.
     */
    public static final Color BG_LIGHT =
            new Color(224, 224, 224);

    /**
     * Bordes sutiles.
     */
    public static final Color SUBTLE_BORDER =
            new Color(213, 213, 213);

    /**
     * Verde principal.
     */
    public static final Color FOREST_GREEN =
            new Color(40, 167, 69);

    /**
     * Verde hover.
     */
    public static final Color FOREST_GREEN_HOVER =
            new Color(33, 138, 58);

    /**
     * Color error.
     */
    public static final Color ERROR_COLOR =
            new Color(183, 28, 28);

    /**
     * Color éxito.
     */
    public static final Color SUCCESS_COLOR =
            new Color(46, 125, 50);

    public static Color PRIMARY_BLUE =
            ApplicationVisualSettings.parseColor(ApplicationVisualSettings.load().getPrimaryColor(), DARK_PRIMARY);

    public static Color SECONDARY_COLOR =
            ApplicationVisualSettings.parseColor(ApplicationVisualSettings.load().getSecondaryColor(), ACCENT_BLUE);

    public static Color TERTIARY_COLOR =
            ApplicationVisualSettings.parseColor(ApplicationVisualSettings.load().getTertiaryColor(), ACCENT_PURPLE);

    public static Color themePrimary() {
        return PRIMARY_BLUE;
    }

    public static Color themeSecondary() {
        return SECONDARY_COLOR;
    }

    public static Color themeTertiary() {
        return TERTIARY_COLOR;
    }

    public static void reloadThemeFromSettings() {
        ApplicationVisualSettings settings = ApplicationVisualSettings.load();
        PRIMARY_BLUE = ApplicationVisualSettings.parseColor(settings.getPrimaryColor(), DARK_PRIMARY);
        SECONDARY_COLOR = ApplicationVisualSettings.parseColor(settings.getSecondaryColor(), ACCENT_BLUE);
        TERTIARY_COLOR = ApplicationVisualSettings.parseColor(settings.getTertiaryColor(), ACCENT_PURPLE);
    }

    public static void restoreDefaultTheme() {
        PRIMARY_BLUE = DARK_PRIMARY;
        SECONDARY_COLOR = ACCENT_BLUE;
        TERTIARY_COLOR = ACCENT_PURPLE;
    }

    public static String appSlogan() {
        return ApplicationVisualSettings.text(ApplicationVisualSettings.load().getSlogan(),
                ApplicationVisualSettings.DEFAULT_SLOGAN);
    }

    public static String loginTitle() {
        return ApplicationVisualSettings.text(ApplicationVisualSettings.load().getLoginTitle(),
                ApplicationVisualSettings.DEFAULT_LOGIN_TITLE);
    }

    public static String loginSubtitle() {
        return ApplicationVisualSettings.text(ApplicationVisualSettings.load().getLoginSubtitle(),
                ApplicationVisualSettings.DEFAULT_LOGIN_SUBTITLE);
    }

    /* =========================================================
     * TIPOGRAFÍAS
     * =========================================================
     */

    /**
     * Nombre de la fuente principal.
     */
    public static final String FONT_FAMILY =
            "Segoe UI";

    public static final Color PANEL_WHITE = Color.WHITE;
    /**
     * Fuente para títulos grandes.
     */
    public static final Font TITLE_FONT =
            new Font(
                    FONT_FAMILY,
                    Font.BOLD,
                    30
            );

    /**
     * Fuente para títulos de sección (Dashboard).
     */
    public static final Font TITLE_FONT_SECTION = new Font(FONT_FAMILY, Font.BOLD, 22);

    /**
     * Fuente para valores destacados en tarjetas.
     */
    public static final Font CARD_VALUE_FONT = new Font(FONT_FAMILY, Font.BOLD, 26);

    /**
     * Fuente para subtítulos.
     */
    public static final Font SUBTITLE_FONT =
            new Font(
                    FONT_FAMILY,
                    Font.PLAIN,
                    18
            );

    /**
     * Fuente estándar del sistema.
     */
    public static final Font BODY_FONT =
            new Font(
                    FONT_FAMILY,
                    Font.PLAIN,
                    14
            );

    /**
     * Fuente para botones.
     */
    public static final Font BUTTON_FONT =
            new Font(
                    FONT_FAMILY,
                    Font.BOLD,
                    15
            );

    /**
     * Fuente pequeña auxiliar.
     */
    public static final Font SMALL_FONT =
            new Font(
                    FONT_FAMILY,
                    Font.PLAIN,
                    12
            );

    /* =========================================================
     * TAMAÑOS ESTÁNDAR
     * =========================================================
     */

    /**
     * Altura estándar de campos.
     */
    public static final int FIELD_HEIGHT = 44;

    /**
     * Padding general.
     */
    public static final int DEFAULT_PADDING = 15;

    /**
     * Radio bordes.
     */
    public static final int BORDER_RADIUS = 14;

    /* =========================================================
     * DIMENSIONES ESTRUCTURALES
     * =========================================================
     */
    public static final int SIDEBAR_WIDTH = 260;
    public static final int HEADER_HEIGHT = 70;
    public static final int CARD_SPACING = 16;
    public static final Dimension FIELD_SIZE_LARGE = new Dimension(320, 44);
    public static final Dimension FIELD_SIZE_MEDIUM = new Dimension(230, 42);

    /* =========================================================
     * MÉTODOS DE FUENTES
     * =========================================================
     */

    /**
     * Crea una fuente en negrita.
     *
     * =====================================================
     * EJEMPLO
     * =====================================================
     *
     * JLabel lbl = new JLabel("Hola");
     * lbl.setFont(UiTheme.bold(20));
     *
     * @param size tamaño de fuente
     *
     * @return fuente en negrita
     */
    public static Font bold(int size) {

        return new Font(
                FONT_FAMILY,
                Font.BOLD,
                size
        );

    }

    /**
     * Crea una fuente normal.
     *
     * @param size tamaño fuente
     *
     * @return fuente plain
     */
    public static Font regular(int size) {

        return new Font(
                FONT_FAMILY,
                Font.PLAIN,
                size
        );

    }

    /* =========================================================
     * BOTONES
     * =========================================================
     */

    /**
     * Estiliza un botón base reutilizable.
     *
     * =====================================================
     * FUNCIONALIDADES
     * =====================================================
     *
     * ✅ Hover
     * ✅ Focus
     * ✅ Active
     * ✅ Cursor Hand
     * ✅ Padding
     * ✅ Tamaño fijo
     *
     * @param btn botón a estilizar
     * @param baseColor color base
     * @param hoverColor color hover
     * @param activeColor color activo
     * @param width ancho botón
     * @param height alto botón
     * @param fontSize tamaño fuente
     * @param filled fondo sólido
     * @param leftAligned alineación izquierda
     * @param leftPadding padding izquierdo
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
            int leftPadding

    ) {

        btn.setPreferredSize(
                new Dimension(width, height)
        );

        

        btn.setFont(
                new Font(
                        FONT_FAMILY,
                        Font.PLAIN,
                        fontSize
                )
        );

        btn.setFocusPainted(false);

        btn.setBorder(
                new EmptyBorder(
                        10,
                        leftPadding,
                        10,
                        leftPadding
                )
        );

        btn.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        /*
         * Alineación opcional izquierda.
         */
        if (leftAligned) {

            btn.setHorizontalAlignment(
                    SwingConstants.LEFT
            );

        }

        btn.setContentAreaFilled(filled);// Permite fondo transparente si filled es false

        btn.setOpaque(filled);

        btn.setBackground(baseColor);

        btn.setForeground(TEXT_LIGHT);

        /*
         * Eventos hover y click.
         */
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

        /*
         * Eventos focus.
         */
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

    /* =========================================================
     * BOTONES SIDEBAR
     * =========================================================
     */

    /**
     * Crea botón sidebar reutilizable.
     *
     * @param text texto botón
     * @param baseColor color base
     * @param hoverColor color hover
     * @param activeColor color activo
     * @param width ancho
     * @param height alto
     *
     * @return botón configurado
     */
    public static JButton createSidebarButton(

            String text,
            Color baseColor,
            Color hoverColor,
            Color activeColor,
            int width,
            int height

    ) {

        JButton btn = new JButton(text);

        styleButton(
                btn,
                baseColor,
                hoverColor,
                activeColor,
                width,
                height,
                14,
                true,
                true,
                20
        );

        btn.setBorder(
                new EmptyBorder(10, 20, 10, 20)
        );

        return btn;

    }

    /**
     * Crea botón sidebar con icono.
     *
     * @param text texto botón
     * @param imagePath ruta imagen
     * @param iconWidth ancho icono
     * @param iconHeight alto icono
     * @param baseColor color base
     * @param hoverColor color hover
     * @param activeColor color activo
     * @param width ancho botón
     * @param height alto botón
     *
     * @return botón configurado
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
            int height

    ) {

        JButton btn =
                createSidebarButton(
                        text,
                        baseColor,
                        hoverColor,
                        activeColor,
                        width,
                        height
                );

        ImageIcon icon =
                scaleImage(
                        imagePath,
                        iconWidth,
                        iconHeight
                );

        if (icon != null) {

            btn.setIcon(icon);

        }

        return btn;

    }

    /* =========================================================
     * IMÁGENES
     * =========================================================
     */

    /**
     * Escala una imagen.
     *
     * =====================================================
     * EJEMPLO
     * =====================================================
     *
     * JLabel logo = new JLabel(
     *      UiTheme.scaleImage(
     *          "/assets/logo.png",
     *          120,
     *          120
     *      )
     * );
     *
     * @param path ruta imagen
     * @param width ancho deseado
     * @param height alto deseado
     *
     * @return imagen escalada
     */
    public static ImageIcon scaleImage(

            String path,
            int width,
            int height

    ) {

        try {

            ImageIcon icon =
                    new ImageIcon(
                            UiTheme.class.getResource(path)
                    );

            Image img =
                    icon.getImage()
                            .getScaledInstance(
                                    width,
                                    height,
                                    Image.SCALE_SMOOTH
                            );

            return new ImageIcon(img);

        } catch (Exception e) {

            System.err.println(
                    "Error cargando imagen: " + path
            );

            return null;

        }

    }

    /* =========================================================
     * LOGOS
     * =========================================================
     */

    /**
     * Obtiene el logo principal.
     *
     * @param width ancho logo
     * @param height alto logo
     *
     * @return logo escalado
     */
    public static ImageIcon getLogo(
            int width,
            int height
    ) {

        return scaleImage(
                LOGO_PATH,
                width,
                height
        );

    }

    /**
     * Obtiene el icono principal.
     *
     * @param width ancho icono
     * @param height alto icono
     *
     * @return icono escalado
     */
    public static ImageIcon getIcon(
            int width,
            int height
    ) {

        return scaleImage(
                ICON_PATH,
                width,
                height
        );

    }

}
