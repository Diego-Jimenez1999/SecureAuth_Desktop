package secureauth.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
 * Vista de registro de mascota (Panel del módulo Mascotas).
 * * <p>
 * Esta clase se enfoca exclusivamente en la construcción de la interfaz gráfica y la interacción con el usuario.
 * Controla el diseño del formulario en dos columnas utilizando GridBagLayout para garantizar proporciones modernas
 * y estilizadas en los componentes de entrada.
 * </p>
 * * @author Diego
 * @version 1.1
 */
public class RegMascotaPanel extends JPanel {

    private static final Color COLOR_BG = UiTheme.BG_PAGE;
    private static final Color COLOR_CARD = UiTheme.PANEL_WHITE;
    private static final Font BASE_FONT = UiTheme.BODY_FONT;
    private static final Dimension FIELD_SIZE = new Dimension(20, 18);
    private static final Color DEFAULT_BORDER_COLOR = UiTheme.BORDER_COLOR;

    // Componentes de la interfaz de usuario
    private JLabel lblImagenMascota;
    private String rutaImagenSeleccionada;

    private JTextField txtNombreMascota;
    private JComboBox<Owner> cbOwner; // Combo para seleccionar dueño existente
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
    private JButton btnNuevoDueno;

    /** Lista de placeholders flotantes para la guía visual del usuario */
    private final List<FloatingPlaceholder> placeholders = new ArrayList<>();

    /**
     * Constructor principal de la vista de registro de mascota.
     * Inicializa y ensambla todos los componentes visuales.
     */
    public RegMascotaPanel() {
        init();
    }

    /**
     * Método inicial para construir y estructurar la interfaz de usuario.
     */
    private void init() {
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(COLOR_BG);
        root.setBorder(BorderFactory.createEmptyBorder(8, 12, 10, 12));

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildContentCard(), BorderLayout.CENTER);

        add(root, BorderLayout.CENTER);
    }

    /**
     * Construye el panel superior de la vista que contiene el título y subtítulo correlativo.
     * * @return JPanel con la cabecera del módulo.
     */
    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel left = new JPanel(new java.awt.GridLayout(2, 1, 0, 2));
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

    /**
     * Construye la tarjeta contenedora principal blanca dividida en sección de imagen (izquierda)
     * y el formulario de datos (derecha).
     * * @return JPanel estilo tarjeta con los componentes principales.
     */
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

    /**
     * Construye el bloque lateral izquierdo dedicado a la previsualización y carga de la foto de la mascota.
     * * @return JPanel con el cargador de imágenes y etiquetas descriptivas.
     */
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

        JPanel bottom = new JPanel(new java.awt.GridLayout(3, 1, 0, 4));
        bottom.setOpaque(false);
        JPanel bWrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bWrap.setOpaque(false);
        bWrap.add(btnSubirImagen);

        JLabel f1 = new JLabel("Formatos permitidos: JPG, PNG", JLabel.CENTER);
        f1.setForeground(Color.GRAY);
        
        // CORREGIDO AQUÍ: Se añadió el tipo explícito JLabel para f2
        JLabel f2 = new JLabel("Tamaño máximo recomendado: 5MB", JLabel.CENTER);
        f2.setForeground(Color.GRAY);

        bottom.add(bWrap);
        bottom.add(f1);
        bottom.add(f2);

        left.add(bottom, BorderLayout.SOUTH);
        return left;
    }

    /**
     * Construye la sección del formulario utilizando GridBagLayout. Esto soluciona los problemas
     * de estiramiento desproporcionado fijando una altura estilizada para las cajas de texto ordinarias.
     * * @return JPanel con todos los campos del formulario organizados adecuadamente.
     */
    private JPanel buildFormSide() {
        JPanel right = new JPanel(new BorderLayout(0, 12));
        right.setOpaque(false);

        // GridBagLayout reemplaza a GridLayout para evitar la deformación vertical
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(6, 8, 6, 8); // Margen elegante entre campos
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.5;
        gbc.weighty = 0.0;

        // Inicialización de componentes
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
        btnNuevoDueno = new JButton("Nuevo dueño");
        btnNuevoDueno.setPreferredSize(new Dimension(130, 34));
        btnNuevoDueno.setFont(BASE_FONT.deriveFont(Font.BOLD));

        // Aplicar estilos centralizados externos
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
        setOwnerFieldsEditable(false);

        ComponentUtils.styleComboBox(cbOwner, FIELD_SIZE, BASE_FONT);
        ComponentUtils.styleComboBox(cbTipoMascota, FIELD_SIZE, BASE_FONT);
        ComponentUtils.styleComboBox(cbSexo, FIELD_SIZE, BASE_FONT);
        ComponentUtils.styleComboBox(cbEstadoSalud, FIELD_SIZE, BASE_FONT);

        // Ajuste de tamaño preferido estandarizado (Altura de 34px) para todas las entradas simples
        Dimension inputDimension = new Dimension(150, 34);
        txtNombreMascota.setPreferredSize(inputDimension);
        cbOwner.setPreferredSize(inputDimension);
        cbTipoMascota.setPreferredSize(inputDimension);
        txtRaza.setPreferredSize(inputDimension);
        txtEdad.setPreferredSize(inputDimension);
        txtPeso.setPreferredSize(inputDimension);
        cbSexo.setPreferredSize(inputDimension);
        txtFrecuencia.setPreferredSize(inputDimension);
        txtTipoAlimento.setPreferredSize(inputDimension);
        txtVacunas.setPreferredSize(inputDimension);
        cbEstadoSalud.setPreferredSize(inputDimension);
        txtNombreDueno.setPreferredSize(inputDimension);
        txtTelefonoDueno.setPreferredSize(inputDimension);
        txtCorreoDueno.setPreferredSize(inputDimension);
        txtDireccionDueno.setPreferredSize(inputDimension);

        // Agregar Placeholders de guía
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

        // Estilos para JTextArea
        taCuidados.setBorder(new CompoundBorder(new RoundedLineBorder(DEFAULT_BORDER_COLOR, 14, 1), new EmptyBorder(8, 12, 8, 12)));
        taNotasAdicionales.setBorder(new CompoundBorder(new RoundedLineBorder(DEFAULT_BORDER_COLOR, 14, 1), new EmptyBorder(8, 12, 8, 12)));

        JScrollPane cuidadosScroll = new JScrollPane(taCuidados);
        cuidadosScroll.setPreferredSize(new Dimension(150, 90));
        
        JScrollPane notasScroll = new JScrollPane(taNotasAdicionales);
        notasScroll.setPreferredSize(new Dimension(150, 72));

        // Mapeo ordenado de los campos estructurados en la cuadrícula
        JPanel ownerSelector = new JPanel(new BorderLayout(8, 0));
        ownerSelector.setOpaque(false);
        ownerSelector.add(cbOwner, BorderLayout.CENTER);
        ownerSelector.add(btnNuevoDueno, BorderLayout.EAST);

        java.awt.Component[] components = {
            field("Dueño registrado *", ownerSelector),
            field("Nombre de la mascota *", txtNombreMascota),
            field("Tipo de mascota", cbTipoMascota),
            field("Raza *", txtRaza),
            field("Edad *", txtEdad),
            field("Peso *", txtPeso),
            field("Sexo *", cbSexo),
            field("Frecuencia de alimentación *", txtFrecuencia),
            field("Tipo de alimento", txtTipoAlimento),
            field("Estado de salud *", cbEstadoSalud),
            field("Vacunas", txtVacunas),
            field("Nombre del dueño", txtNombreDueno),
            field("Teléfono del dueño", txtTelefonoDueno),
            field("Correo electrónico", txtCorreoDueno),
            field("Dirección", txtDireccionDueno),
            field("Cuidados especiales", cuidadosScroll),
            field("Notas adicionales", notasScroll)
        };

        // Inserción indexada en GridBagLayout (2 columnas automáticas)
        for (int i = 0; i < components.length; i++) {
            gbc.gridx = i % 2;
            gbc.gridy = i / 2;
            grid.add(components[i], gbc);
        }

        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setOpaque(false);
        gridWrapper.add(grid, BorderLayout.NORTH);

        JScrollPane formScroll = new JScrollPane(gridWrapper);
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);
        formScroll.setBorder(null);
        formScroll.getVerticalScrollBar().setUnitIncrement(16);

        right.add(formScroll, BorderLayout.CENTER);

        // Configuración y dimensionamiento de los botones de acción inferiores
        JButton btnLimpiar = new JButton("Limpiar formulario");
        btnLimpiar.setPreferredSize(new Dimension(160, 36));
        btnLimpiar.setFont(BASE_FONT.deriveFont(Font.BOLD));
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        
        btnGuardar = new JButton("Guardar mascota");
        btnGuardar.setBackground(UiTheme.themePrimary());
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setPreferredSize(new Dimension(160, 36));
        btnGuardar.setFont(BASE_FONT.deriveFont(Font.BOLD));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        actions.setOpaque(false);
        actions.add(btnLimpiar);
        actions.add(btnGuardar);

        right.add(actions, BorderLayout.SOUTH);

        return right;
    }

    /**
     * Construye un contenedor vertical para agrupar una etiqueta y su campo de entrada.
     * * @param label Texto descriptivo del campo.
     * @param input Componente interactivo (JTextField, JComboBox, JScrollPane).
     * @return JPanel con la composición del campo de formulario.
     */
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
     * Establece la imagen por defecto de la mascota escalada dinámicamente.
     */
    private void setDefaultImage() {
        java.net.URL url = getClass().getResource("/icon/newpet.png");
        if (url != null) {
            ImageIcon icon = new ImageIcon(url); 
            Image scaledImage = icon.getImage().getScaledInstance(240, 260, Image.SCALE_SMOOTH);
            lblImagenMascota.setIcon(new ImageIcon(scaledImage));
            lblImagenMascota.setText("");
        } else {
            lblImagenMascota.setText("Sin imagen");
        }
    }

    /**
     * Restablece todos los campos del formulario a sus valores iniciales vacíos.
     */
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

    /**
     * Setea y rellena visualmente los datos informativos del propietario seleccionado.
     * * @param owner Modelo del propietario (Owner).
     */
    public void setOwnerDetails(Owner owner) {
        if (owner == null) {
            txtNombreDueno.setText("");
            txtTelefonoDueno.setText("");
            txtCorreoDueno.setText("");
            txtDireccionDueno.setText("");
            return;
        }
        txtNombreDueno.setText(owner.getNombreCompleto());
        txtTelefonoDueno.setText(owner.getTelefono());
        txtCorreoDueno.setText(owner.getCorreo());
        txtDireccionDueno.setText(owner.getDireccion());
    }

    /**
     * Habilita o deshabilita la edición de los campos del propietario.
     * * @param editable true si se permite la edición directa, false para sólo lectura.
     */
    private void setOwnerFieldsEditable(boolean editable) {
        txtNombreDueno.setEditable(editable);
        txtTelefonoDueno.setEditable(editable);
        txtCorreoDueno.setEditable(editable);
        txtDireccionDueno.setEditable(editable);

        Color background = editable ? Color.WHITE : new Color(248, 250, 252);
        txtNombreDueno.setBackground(background);
        txtTelefonoDueno.setBackground(background);
        txtCorreoDueno.setBackground(background);
        txtDireccionDueno.setBackground(background);
    }

    public void showError(String msg) { JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE); }
    public void showSuccess(String msg) { JOptionPane.showMessageDialog(this, msg, "Éxito", JOptionPane.INFORMATION_MESSAGE); }

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
    public JButton getBtnNuevoDueno() { return btnNuevoDueno; }
}
