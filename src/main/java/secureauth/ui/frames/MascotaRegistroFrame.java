package secureauth.ui.frames;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import secureauth.controller.MascotaController;

/**
 * Vista de registro de mascota.
 *
 * <p>
 * Esta clase se enfoca exclusivamente en la construcción de la interfaz gráfica y la interacción con el usuario.
 * No debe contener lógica de negocio ni acceso a datos. Toda la lógica debe ser delegada al {@link MascotaController}.
 * </p>
 *
 * @author Diego
 * @version 1.0
 */
public class MascotaRegistroFrame extends JPanel {

    private final Color COLOR_BG = new Color(244, 246, 249);
    private final Color COLOR_CARD = new Color(236, 238, 243);

    private final MascotaController controller;

    private JLabel lblImagenMascota;
    private String rutaImagenSeleccionada;

    private JTextField txtNombreMascota;
    private JComboBox<String> cbTipoMascota;
    private JTextField txtRaza;
    private JTextField txtEdad;
    private JTextField txtPeso;
    private JComboBox<String> cbSexo;
    private JTextField txtFrecuencia;
    private JTextArea taCuidados;
    private JComboBox<String> cbEstadoSalud;

    private JTextField txtNombreDueno;
    private JTextField txtTelefonoDueno;
    private JTextField txtCorreoDueno;
    private JTextField txtDireccionDueno;


    /**
     * Constructor principal de la vista de registro de mascota.
     * @param controller Controlador de mascota inyectado desde el bootstrap (MainApp) para 
     * manejar la logica de negocio y eventos. No se debe crear un nuevo controlador dentro de esta clase.
     */ 
    public MascotaRegistroFrame(MascotaController controller) {
        this.controller = controller;
        this.controller.bindView(this);
        init();
    }
    

    // Metoddo inicial para construir la UI
    private void init() {
        setLayout(new BorderLayout());

        JPanel root = new JPanel(new BorderLayout(0, 10));
        root.setBackground(COLOR_BG);
        root.setBorder(BorderFactory.createEmptyBorder(8, 20, 14, 20));

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
        title.setFont(new Font("SansSerif", Font.BOLD, 48));
        JLabel sub = new JLabel("Ingresa la información de una nueva mascota");
        sub.setForeground(Color.GRAY);
        sub.setFont(new Font("SansSerif", Font.PLAIN, 16));
        left.add(title);
        left.add(sub);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setOpaque(false);

        panel.add(left, BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    private JPanel buildContentCard() {
        JPanel card = new JPanel(new BorderLayout(20, 0));
        card.setBackground(COLOR_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 213, 220)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        card.add(buildImageSide(), BorderLayout.WEST);
        card.add(buildFormSide(), BorderLayout.CENTER);
        return card;
    }

    private JPanel buildImageSide() {
        JPanel left = new JPanel(new BorderLayout(0, 10));
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(360, 0));

        JLabel t = new JLabel("Imagen de la mascota");
        t.setFont(new Font("SansSerif", Font.BOLD, 24));
        left.add(t, BorderLayout.NORTH);

        lblImagenMascota = new JLabel();
        lblImagenMascota.setHorizontalAlignment(JLabel.CENTER);
        lblImagenMascota.setOpaque(true);
        lblImagenMascota.setBackground(Color.WHITE);
        lblImagenMascota.setBorder(BorderFactory.createLineBorder(new Color(195, 199, 210)));
        lblImagenMascota.setPreferredSize(new Dimension(330, 360));
        setDefaultImage();

        JPanel centerWrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerWrap.setOpaque(false);
        centerWrap.add(lblImagenMascota);
        left.add(centerWrap, BorderLayout.CENTER);

        JButton btnSubir = new JButton("Subir imagen");
        btnSubir.setPreferredSize(new Dimension(210, 42));
        btnSubir.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSubir.addActionListener(e -> seleccionarImagen());

        JPanel bottom = new JPanel(new GridLayout(3, 1, 0, 4));
        bottom.setOpaque(false);
        JPanel bWrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bWrap.setOpaque(false);
        bWrap.add(btnSubir);

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
        cbTipoMascota = new JComboBox<>(new String[] { "Perro", "Gato", "Ave", "Conejo", "Otro" });
        txtRaza = new JTextField();
        txtEdad = new JTextField();
        txtPeso = new JTextField();
        cbSexo = new JComboBox<>(new String[] { "M", "F" });
        txtFrecuencia = new JTextField();
        taCuidados = new JTextArea(6, 20);
        taCuidados.setLineWrap(true);
        taCuidados.setWrapStyleWord(true);
        cbEstadoSalud = new JComboBox<>(new String[] { "Activo", "Enfermo", "En tratamiento" });

        txtNombreDueno = new JTextField();
        txtTelefonoDueno = new JTextField();
        txtCorreoDueno = new JTextField();
        txtDireccionDueno = new JTextField();

        grid.add(field("Nombre de la mascota *", txtNombreMascota));
        grid.add(field("Tipo de mascota *", cbTipoMascota));
        grid.add(field("Raza *", txtRaza));
        grid.add(field("Edad *", txtEdad));
        grid.add(field("Peso *", txtPeso));
        grid.add(field("Sexo *", cbSexo));
        grid.add(field("Frecuencia de alimentacion *", txtFrecuencia));
        grid.add(field("Estado de salud *", cbEstadoSalud));
        grid.add(field("Nombre del dueno *", txtNombreDueno));
        grid.add(field("Telefono del dueno *", txtTelefonoDueno));
        grid.add(field("Correo electronico *", txtCorreoDueno));
        grid.add(field("Direccion *", txtDireccionDueno));
        JScrollPane cuidadosScroll = new JScrollPane(taCuidados);
        cuidadosScroll.setPreferredSize(new Dimension(0, 120));
        grid.add(field("Cuidados especiales", cuidadosScroll));

        right.add(grid, BorderLayout.CENTER);

        JButton btnLimpiar = new JButton("Limpiar formulario");
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        JButton btnGuardar = new JButton("Guardar mascota");
        btnGuardar.setBackground(new Color(205, 42, 42));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.addActionListener(e -> controller.guardarMascota());

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
        l.setFont(new Font("SansSerif", Font.BOLD, 14));
        p.add(l, BorderLayout.NORTH);
        p.add(input, BorderLayout.CENTER);
        return p;
    }

    private void seleccionarImagen() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Imagenes JPG/PNG", "jpg", "jpeg", "png"));
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            rutaImagenSeleccionada = file.getAbsolutePath();
            setImage(file.getAbsolutePath());
        }
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

        // Escalar imagen a 330x360
        Image scaledImage = icon.getImage().getScaledInstance(
                330, 
                360, 
                Image.SCALE_SMOOTH // mejor calidad
        );

        lblImagenMascota.setIcon(new ImageIcon(scaledImage));
        lblImagenMascota.setText(""); // limpiar texto si había

    } else {
        lblImagenMascota.setText("Sin imagen");
    }
}

    private void setImage(String absolutePath) {
        setImage(new ImageIcon(absolutePath));
    }

    private void setImage(java.net.URL url) {
        setImage(new ImageIcon(url));
    }

    private void setImage(javax.swing.ImageIcon icon) {
        Image scaled = icon.getImage().getScaledInstance(330, 360, Image.SCALE_SMOOTH);
        lblImagenMascota.setText("");
        lblImagenMascota.setIcon(new javax.swing.ImageIcon(scaled));
    }

    public void limpiarFormulario() {
        txtNombreMascota.setText("");
        cbTipoMascota.setSelectedIndex(0);
        txtRaza.setText("");
        txtEdad.setText("");
        txtPeso.setText("");
        cbSexo.setSelectedIndex(0);
        txtFrecuencia.setText("");
        taCuidados.setText("");
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
    public String getTipoMascota() { return (String) cbTipoMascota.getSelectedItem(); }
    public String getRazaMascota() { return txtRaza.getText(); }
    public String getEdadMascota() { return txtEdad.getText(); }
    public String getPesoMascota() { return txtPeso.getText(); }
    public String getSexoMascota() { return (String) cbSexo.getSelectedItem(); }
    public String getFrecuenciaAlimentacion() { return txtFrecuencia.getText(); }
    public String getCuidadosEspeciales() { return taCuidados.getText(); }
    public String getEstadoSalud() { return (String) cbEstadoSalud.getSelectedItem(); }
    public String getNombreDueno() { return txtNombreDueno.getText(); }
    public String getTelefonoDueno() { return txtTelefonoDueno.getText(); }
    public String getCorreoDueno() { return txtCorreoDueno.getText(); }
    public String getDireccionDueno() { return txtDireccionDueno.getText(); }
    public String getRutaImagenSeleccionada() { return rutaImagenSeleccionada; }
}
