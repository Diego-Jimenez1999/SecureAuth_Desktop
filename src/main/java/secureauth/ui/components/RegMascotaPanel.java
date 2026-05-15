package secureauth.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import secureauth.model.Owner;
import secureauth.ui.utils.ComponentUtils;
import secureauth.ui.utils.FloatingPlaceholder;
import secureauth.ui.utils.RoundedLineBorder;
import secureauth.ui.utils.UiTheme;

/**
 * Vista de registro de mascota.
 *
 * <p>
 * Esta clase se enfoca exclusivamente en la construcción de la interfaz gráfica y la interacción con el usuario.
 * No debe contener lógica de negocio ni acceso a datos.
 * </p>
 *
 * @author Diego
 * @version 1.0
 */
public class RegMascotaPanel extends JPanel {

    private static final Color COLOR_BG = UiTheme.BG_PAGE;
    private static final Color COLOR_CARD = UiTheme.PANEL_WHITE;
    private static final Font BASE_FONT = UiTheme.BODY_FONT;
    private static final Dimension FIELD_SIZE = new Dimension(20, 18);
    private static final Color DEFAULT_BORDER_COLOR = UiTheme.BORDER_COLOR;

    private JLabel lblImagenMascota;
    private String rutaImagenSeleccionada;

    private JTextField txtNombreMascota;
    private JComboBox<Owner> cbOwner;// nuevo combo para seleccionar dueño existente
    private JComboBox<String> cbTipoMascota;
    private JTextField txtRaza;
    private JTextField txtEdad;
    private JTextField txtPeso;
    private JComboBox<String> cbSexo;
    private JTextField txtFrecuencia;
    private JTextField txtTipoAlimento;
    private JTextField txtVacunas;
    private JTextArea taCuidados;
    private JTextArea taNotasAdicionales;
    private JComboBox<String> cbEstadoSalud;

    private JTextField txtNombreDueno;
    private JTextField txtTelefonoDueno;
    private JTextField txtCorreoDueno;
    private JTextField txtDireccionDueno;
    private JButton btnSubirImagen;
    private JButton btnGuardar;

    private final List<FloatingPlaceholder> placeholders = new ArrayList<>();


    /**
     * Constructor principal de la vista de registro de mascota.
     */ 
    public RegMascotaPanel() {
        init();
    }
    

    // Metoddo inicial para construir la UI
    private void init() {
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(COLOR_BG);
        root.setBorder(BorderFactory.createEmptyBorder(8, 12, 10, 12));

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildContentCard(), BorderLayout.CENTER);

        add(root, BorderLayout.CENTER);
    }
    

    // metodo creacion panel superior con titulo y subtitulo
    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel left = new JPanel(new GridLayout(2, 1, 0, 2));
        left.setOpaque(false);
        JLabel title = new JLabel("Registro de Mascota");
        title.setFont(UiTheme.TITLE_FONT_SECTION);
        JLabel sub = new JLabel("Ingresa la información de una nueva mascota");
        sub.setForeground(UiTheme.TEXT_SECONDARY);
        sub.setFont(UiTheme.BODY_FONT);
        left.add(title);
        left.add(sub);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setOpaque(false);

        panel.add(left, BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildContentCard() {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(COLOR_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        card.add(buildImageSide(), BorderLayout.WEST);
        card.add(buildFormSide(), BorderLayout.CENTER);
        return card;
    }

    private JPanel buildImageSide() {
        JPanel left = new JPanel(new BorderLayout(0, 10));
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(280, 0));

        JLabel t = new JLabel("Imagen de la mascota");
        t.setFont(UiTheme.BODY_FONT.deriveFont(Font.BOLD, 16f));
        left.add(t, BorderLayout.NORTH);

        lblImagenMascota = new JLabel();
        lblImagenMascota.setHorizontalAlignment(JLabel.CENTER);
        lblImagenMascota.setOpaque(true);
        lblImagenMascota.setBackground(Color.WHITE);
        lblImagenMascota.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER_COLOR));
        lblImagenMascota.setPreferredSize(new Dimension(240, 260));
        setDefaultImage();

        JPanel centerWrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerWrap.setOpaque(false);
        centerWrap.add(lblImagenMascota);
        left.add(centerWrap, BorderLayout.CENTER);

        btnSubirImagen = new JButton("Subir imagen");
        btnSubirImagen.setPreferredSize(new Dimension(180, 34));
        btnSubirImagen.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel bottom = new JPanel(new GridLayout(3, 1, 0, 4));
        bottom.setOpaque(false);
        JPanel bWrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bWrap.setOpaque(false);
        bWrap.add(btnSubirImagen);

        JLabel f1 = new JLabel("Formatos permitidos: JPG, PNG", JLabel.CENTER);
        f1.setForeground(Color.GRAY);
        JLabel f2 = new JLabel("Tamano maximo recomendado: 5MB", JLabel.CENTER);
        f2.setForeground(Color.GRAY);

        bottom.add(bWrap);
        bottom.add(f1);
        bottom.add(f2);

        left.add(bottom, BorderLayout.SOUTH);
        return left;
    }

    private JPanel buildFormSide() {
        JPanel right = new JPanel(new BorderLayout(0, 12));
        right.setOpaque(false);

        JPanel grid = new JPanel(new GridLayout(0, 2, 14, 10));
        grid.setOpaque(false);

        txtNombreMascota = new JTextField();
        cbOwner = new JComboBox<>();
        cbTipoMascota = new JComboBox<>(new String[] { "Perro", "Gato", "Ave", "Otro" });
        txtRaza = new JTextField();
        txtEdad = new JTextField();
        txtPeso = new JTextField();
        cbSexo = new JComboBox<>(new String[] { "Macho", "Hembra" });
        txtFrecuencia = new JTextField();
        txtTipoAlimento = new JTextField();
        txtVacunas = new JTextField();
        taCuidados = new JTextArea(6, 20);
        taCuidados.setLineWrap(true);
        taCuidados.setWrapStyleWord(true);
        taNotasAdicionales = new JTextArea(4, 20);
        taNotasAdicionales.setLineWrap(true);
        taNotasAdicionales.setWrapStyleWord(true);
        cbEstadoSalud = new JComboBox<>(new String[] { "Activo", "Enfermo", "En tratamiento" });

        txtNombreDueno = new JTextField();
        txtTelefonoDueno = new JTextField();
        txtCorreoDueno = new JTextField();
        txtDireccionDueno = new JTextField();

        // Aplicar estilos centralizados de utils (identicos a LoginFrame)
        ComponentUtils.styleTextField(txtNombreMascota, FIELD_SIZE, BASE_FONT, UiTheme.FOREST_GREEN);
        ComponentUtils.styleTextField(txtRaza, FIELD_SIZE, BASE_FONT, UiTheme.FOREST_GREEN);
        ComponentUtils.styleTextField(txtEdad, FIELD_SIZE, BASE_FONT, UiTheme.FOREST_GREEN);
        ComponentUtils.styleTextField(txtPeso, FIELD_SIZE, BASE_FONT, UiTheme.FOREST_GREEN);
        ComponentUtils.styleTextField(txtFrecuencia, FIELD_SIZE, BASE_FONT, UiTheme.FOREST_GREEN);
        ComponentUtils.styleTextField(txtTipoAlimento, FIELD_SIZE, BASE_FONT, UiTheme.FOREST_GREEN);
        ComponentUtils.styleTextField(txtVacunas, FIELD_SIZE, BASE_FONT, UiTheme.FOREST_GREEN);
        ComponentUtils.styleTextField(txtNombreDueno, FIELD_SIZE, BASE_FONT, UiTheme.FOREST_GREEN);
        ComponentUtils.styleTextField(txtTelefonoDueno, FIELD_SIZE, BASE_FONT, UiTheme.FOREST_GREEN);
        ComponentUtils.styleTextField(txtCorreoDueno, FIELD_SIZE, BASE_FONT, UiTheme.FOREST_GREEN);
        ComponentUtils.styleTextField(txtDireccionDueno, FIELD_SIZE, BASE_FONT, UiTheme.FOREST_GREEN);

        ComponentUtils.styleComboBox(cbOwner, FIELD_SIZE, BASE_FONT);
        ComponentUtils.styleComboBox(cbTipoMascota, FIELD_SIZE, BASE_FONT);
        ComponentUtils.styleComboBox(cbSexo, FIELD_SIZE, BASE_FONT);
        ComponentUtils.styleComboBox(cbEstadoSalud, FIELD_SIZE, BASE_FONT);

        // Agregar Placeholders para mejorar la guía del usuario
        placeholders.add(new FloatingPlaceholder("Nombre de la mascota...", txtNombreMascota));
        placeholders.add(new FloatingPlaceholder("Ej: Labrador, Criollo...", txtRaza));
        placeholders.add(new FloatingPlaceholder("Ej: 2 años o 5 meses...", txtEdad));
        placeholders.add(new FloatingPlaceholder("Peso en Kg...", txtPeso));
        placeholders.add(new FloatingPlaceholder("Ej: 2 veces al día...", txtFrecuencia));
        placeholders.add(new FloatingPlaceholder("Tipo de alimento...", txtTipoAlimento));
        placeholders.add(new FloatingPlaceholder("Vacunas aplicadas...", txtVacunas));
        placeholders.add(new FloatingPlaceholder("Nombre del dueño...", txtNombreDueno));
        placeholders.add(new FloatingPlaceholder("Número de contacto...", txtTelefonoDueno));
        placeholders.add(new FloatingPlaceholder("ejemplo@correo.com...", txtCorreoDueno));
        placeholders.add(new FloatingPlaceholder("Dirección de residencia...", txtDireccionDueno));

        // Estilo para JTextArea para que coincida con la estética de los inputs
        taCuidados.setBorder(new CompoundBorder(new RoundedLineBorder(DEFAULT_BORDER_COLOR, 14, 1), new EmptyBorder(8, 12, 8, 12)));
        taNotasAdicionales.setBorder(new CompoundBorder(new RoundedLineBorder(DEFAULT_BORDER_COLOR, 14, 1), new EmptyBorder(8, 12, 8, 12)));

        grid.add(field("Dueño registrado *", cbOwner));
        grid.add(field("Nombre de la mascota *", txtNombreMascota));
        grid.add(field("Tipo de mascota", cbTipoMascota));
        grid.add(field("Raza *", txtRaza));
        grid.add(field("Edad *", txtEdad));
        grid.add(field("Peso *", txtPeso));
        grid.add(field("Sexo *", cbSexo));
        grid.add(field("Frecuencia de alimentacion *", txtFrecuencia));
        grid.add(field("Tipo de alimento", txtTipoAlimento));
        grid.add(field("Estado de salud *", cbEstadoSalud));
        grid.add(field("Vacunas", txtVacunas));
        grid.add(field("Nombre del dueno *", txtNombreDueno));
        grid.add(field("Telefono del dueno *", txtTelefonoDueno));
        grid.add(field("Correo electronico *", txtCorreoDueno));
        grid.add(field("Direccion *", txtDireccionDueno));
        JScrollPane cuidadosScroll = new JScrollPane(taCuidados);
        cuidadosScroll.setPreferredSize(new Dimension(0, 90));
        grid.add(field("Cuidados especiales", cuidadosScroll));
        JScrollPane notasScroll = new JScrollPane(taNotasAdicionales);
        notasScroll.setPreferredSize(new Dimension(0, 72));
        grid.add(field("Notas adicionales", notasScroll));

        // Envoltura para evitar distorsión: Usamos un panel intermedio en el NORTH del scroll
        // Esto asegura que el Grid mantenga su altura natural y no se estire.
        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setOpaque(false);
        gridWrapper.add(grid, BorderLayout.NORTH);

        JScrollPane formScroll = new JScrollPane(gridWrapper);
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);
        formScroll.setBorder(null);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);

        right.add(formScroll, BorderLayout.CENTER);

        JButton btnLimpiar = new JButton("Limpiar formulario");
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnGuardar = new JButton("Guardar mascota");
        btnGuardar.setBackground(new Color(205, 42, 42));
        btnGuardar.setForeground(Color.WHITE);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        actions.setOpaque(false);
        actions.add(btnLimpiar);
        actions.add(btnGuardar);

        right.add(actions, BorderLayout.SOUTH);

        return right;
    }

    private JPanel field(String label, java.awt.Component input) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(BASE_FONT.deriveFont(Font.BOLD));
        p.add(l, BorderLayout.NORTH);
        p.add(input, BorderLayout.CENTER);
        return p;
    }

    /**
    *Establece la imagen por defecto de la mascota escalada a 330x360.
        *
        * La imagen se carga desde los recursos del proyecto y se ajusta
        * al tamaño del JLabel manteniendo calidad de renderizado.
    */
    private void setDefaultImage() {
    java.net.URL url = getClass().getResource("/icon/newpet.png");
    System.err.println("Cargando imagen por defecto desde: " + url);

    if (url != null) {
        ImageIcon icon = new ImageIcon(url); 

        // Escalar imagen a tamaño compacto
        Image scaledImage = icon.getImage().getScaledInstance(
                240,
                260,
                Image.SCALE_SMOOTH // mejor calidad
        );

        lblImagenMascota.setIcon(new ImageIcon(scaledImage));
        lblImagenMascota.setText(""); // limpiar texto si había

    } else {
        lblImagenMascota.setText("Sin imagen");
    }
}

    public void limpiarFormulario() {
        txtNombreMascota.setText("");
        cbOwner.setSelectedItem(null);
        cbTipoMascota.setSelectedIndex(0);
        txtRaza.setText("");
        txtEdad.setText("");
        txtPeso.setText("");
        cbSexo.setSelectedIndex(0);
        txtFrecuencia.setText("");
        txtTipoAlimento.setText("");
        txtVacunas.setText("");
        taCuidados.setText("");
        taNotasAdicionales.setText("");
        cbEstadoSalud.setSelectedIndex(0);
        txtNombreDueno.setText("");
        txtTelefonoDueno.setText("");
        txtCorreoDueno.setText("");
        txtDireccionDueno.setText("");
        rutaImagenSeleccionada = null;
        setDefaultImage();
    }

    public void showError(String msg) { JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE); }
    public void showSuccess(String msg) { JOptionPane.showMessageDialog(this, msg, "Exito", JOptionPane.INFORMATION_MESSAGE); }

    public String getNombreMascota() { return txtNombreMascota.getText(); }
    public Owner getSelectedOwner() { return (Owner) cbOwner.getSelectedItem(); }
    public JComboBox<Owner> getCbOwner() { return cbOwner; }
    public String getTipoMascota() { return cbTipoMascota != null ? (String) cbTipoMascota.getSelectedItem() : ""; }
    public String getRazaMascota() { return txtRaza.getText(); }
    public String getEdadMascota() { return txtEdad.getText(); }
    public String getPesoMascota() { return txtPeso.getText(); }
    public String getSexoMascota() { return (String) cbSexo.getSelectedItem(); }
    public String getFrecuenciaAlimentacion() { return txtFrecuencia.getText(); }
    public String getTipoAlimento() { return txtTipoAlimento.getText(); }
    public String getVacunas() { return txtVacunas.getText(); }
    public String getCuidadosEspeciales() { return taCuidados.getText(); }
    public String getNotasAdicionales() { return taNotasAdicionales.getText(); }
    public String getEstadoSalud() { return (String) cbEstadoSalud.getSelectedItem(); }
    public String getNombreDueno() { return txtNombreDueno.getText(); }
    public String getTelefonoDueno() { return txtTelefonoDueno.getText(); }
    public String getCorreoDueno() { return txtCorreoDueno.getText(); }
    public String getDireccionDueno() { return txtDireccionDueno.getText(); }
    public String getRutaImagenSeleccionada() { return rutaImagenSeleccionada; }
    public void setRutaImagenSeleccionada(String rutaImagenSeleccionada) { this.rutaImagenSeleccionada = rutaImagenSeleccionada; }
    public JLabel getLblImagenMascota() { return lblImagenMascota; }
    public JButton getBtnSubirImagen() { return btnSubirImagen; }
    public JButton getBtnGuardarMascota() { return btnGuardar; }
}
