/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 * Paquete encargado de la capa de presentación (UI).
 * 
 * Contiene todas las ventanas, paneles y componentes visuales
 * del sistema SecureAuth Desktop.
 * 
 * Esta capa SOLO se encarga de mostrar información al usuario
 * y capturar eventos (clicks, inputs), delegando la lógica
 * al Controller correspondiente.
 * 
 * Buenas prácticas aplicadas:
 * - Separación de responsabilidades (MVC)
 * - No contiene lógica de negocio
 * - Preparado para escalabilidad
 * 
 * @author Diego Alexander Gaviria Jimenez
 * @version 1.0
 */
package secureauth.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import secureauth.controller.IngresoController;
import secureauth.model.User;

public class IngresoFrame extends JFrame {

    private JTable table;
    private JTextField txtBuscarHeader, txtBusquedaRapida;

    // Colores de la interfaz
    private final Color COLOR_SIDEBAR = new Color(30, 36, 48);
    private final Color COLOR_ACCENT = new Color(198, 40, 40); // Rojo profesional
    private final Color COLOR_BG = new Color(244, 246, 249);
    
    // Usuario actualmente logueado (puede ser null si no hay sesión)
    private User usuarioActual;
    private final IngresoController controller;
    
    // Botón actualmente activo en el menú lateral (para gestionar estilos)
    private JButton botonActivo;
    
    // Botones del menú lateral
    private JButton btnHome;
    private JButton btnUsuarios;
    private JButton btnConfig;
    private JButton btnLogout;
    private JButton btnRefresh;
    private JButton btnConsultar;



        

    public IngresoFrame() {
        this.controller = new IngresoController(this);
        initComponents();
        setupFrame();
        this.controller.cargarUsuarios();
    }
    
    public IngresoFrame(User usuario) {
        this.usuarioActual = usuario;
        this.controller = new IngresoController(this, usuario);

        initComponents();
        setupFrame();
        this.controller.cargarUsuarios();
    }
    
    
    

    private void initComponents() {
        setLayout(new BorderLayout());
        add(buildSidebar(), BorderLayout.WEST);
        add(buildMainPanel(), BorderLayout.CENTER);
        
        
        
    }

    private JPanel buildSidebar() {
        JPanel panel = new JPanel();
        panel.setBackground(COLOR_SIDEBAR);
        panel.setPreferredSize(new Dimension(260, getHeight()));
        panel.setLayout(new BorderLayout());

        // --- PARTE SUPERIOR (Logo y Nombre) ---
        JPanel topPanel = new JPanel();
        topPanel.setOpaque(false);
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(new EmptyBorder(30, 10, 30, 10));

        JLabel lblImage = new JLabel(scaleImage("/imagen.png", 80, 80));
        lblImage.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        String nombre = (usuarioActual != null) ? usuarioActual.getNombre() : "invitado";
        JLabel lblTitle = new JLabel(nombre);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        
        JLabel lblSubtitle = new JLabel("DASHBOARD");
        lblSubtitle.setForeground(new Color(150, 150, 150));
        lblSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 10));
        lblSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        topPanel.add(lblImage);
        topPanel.add(Box.createVerticalStrut(10));
        topPanel.add(lblTitle);
        topPanel.add(lblSubtitle);

        // --- PARTE CENTRAL (Botones) ---
        JPanel menuPanel = new JPanel();
        menuPanel.setOpaque(false);
        menuPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 5));

        // 🔥 CREAR BOTONES
        btnHome = createSidebarButton("Home", true);
        btnUsuarios = createSidebarButton("Usuarios", false);
        btnConfig = createSidebarButton("Configuración", false);

        
        menuPanel.add(btnHome);
        menuPanel.add(btnUsuarios);
        menuPanel.add(btnConfig);

        // 🔥 Estado inicial
        botonActivo = btnHome;

        // 🔥 Eventos de foco
        btnHome.addActionListener(e -> cambiarBotonActivo(btnHome));
        btnUsuarios.addActionListener(e -> cambiarBotonActivo(btnUsuarios));
        btnConfig.addActionListener(e -> cambiarBotonActivo(btnConfig));
        
        
        
    
        // EVENTOS (CAMBIO DE FOCO Entre Botones)
        
        

        // --- PARTE INFERIOR (Sesión y Salir) ---
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(new EmptyBorder(10, 20, 20, 20));

        // Info Sesión (Redondeado pequeño)
        JpanelR sessionBox = new JpanelR();
        sessionBox.setBackgroundColor(new Color(45, 52, 65));
        sessionBox.setArc(15);
        sessionBox.setMaximumSize(new Dimension(220, 70));
        sessionBox.setLayout(new GridLayout(2, 1, 0, -5));
        sessionBox.setBorder(new EmptyBorder(10, 15, 10, 15));

        String email = (usuarioActual != null) ? usuarioActual.getEmail(): "sin sesion";
        JLabel userMail = new JLabel(email);
        userMail.setForeground(Color.WHITE);
        userMail.setFont(new Font("SansSerif", Font.PLAIN, 11));
        
        JLabel status = new JLabel("● Conectado");
        status.setForeground(new Color(76, 175, 80));
        status.setFont(new Font("SansSerif", Font.BOLD, 10));

        sessionBox.add(userMail);
        sessionBox.add(status);

        btnLogout = createSidebarButton("Cerrar Sesión", false);

        btnLogout.addActionListener(l -> accionboont("btnLogout"));



        
        bottomPanel.add(sessionBox);
        bottomPanel.add(Box.createVerticalStrut(15));
        bottomPanel.add(btnLogout);
        
        


        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(menuPanel, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 20));
        panel.setBackground(COLOR_BG);
        panel.setBorder(new EmptyBorder(20, 25, 20, 25));

        panel.add(buildHeader(), BorderLayout.NORTH);
        panel.add(buildTableSection(), BorderLayout.CENTER);
        panel.add(buildQuickConsultSection(), BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        // Títulos
        JPanel textGroup = new JPanel(new GridLayout(2, 1, 0, 5));
        textGroup.setOpaque(false);
        JLabel title = new JLabel("Gestión de Usuarios");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        JLabel sub = new JLabel("Administra los usuarios registrados en el sistema");
        sub.setForeground(Color.GRAY);
        textGroup.add(title);
        textGroup.add(sub);

        // Buscador superior
        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchBox.setOpaque(false);
        txtBuscarHeader = new JTextField(15);
        txtBuscarHeader.setPreferredSize(new Dimension(200, 35));
        btnRefresh = new JButton("🔄"); // Boton de refrescar
        
        btnRefresh.setBackground(COLOR_SIDEBAR);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnRefresh.setPreferredSize(new Dimension(55, 35));
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        searchBox.add(new JLabel("🔍"));
        searchBox.add(txtBuscarHeader);
        searchBox.add(btnRefresh);

        btnRefresh.addActionListener(e -> controller.cargarUsuarios());
        txtBuscarHeader.addActionListener(e -> controller.buscarUsuarios());

        header.add(textGroup, BorderLayout.WEST);
        header.add(searchBox, BorderLayout.EAST);

        return header;
    }

    private JPanel buildTableSection() {
        JPanel container = new JPanel(new BorderLayout(0, 15));
        container.setOpaque(false);
        
        // Métricas (Activos, Suspendidos, etc)
        JPanel metrics = new JPanel(new GridLayout(1, 4, 15, 0));
        metrics.setOpaque(false);
        metrics.setPreferredSize(new Dimension(0, 90));

        metrics.add(createMetricCard("Total Usuarios", "24", Color.WHITE, null));
        metrics.add(createMetricCard("Activos", "22", Color.WHITE, "92%"));
        metrics.add(createMetricCard("Suspendidos", "2", Color.WHITE, "8%"));
        metrics.add(createMetricCard("Hoy", "3", Color.WHITE, null));

        // Tabla redondeada dentro de un JpanelR
        JpanelR tablePanel = new JpanelR();
        tablePanel.setBackgroundColor(Color.WHITE);
        tablePanel.setLayout(new BorderLayout());
        tablePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        String[] columns = {"ID", "NOMBRE COMPLETO", "EMAIL",  "GENERO", "ACCION"};
        DefaultTableModel model = new DefaultTableModel(columns, 5); // 5 filas de ejemplo



        table = new JTable(model);
        table.setRowHeight(45);
        table.setSelectionBackground(new Color(240, 240, 240));
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(230, 230, 230));
        
        table.getTableHeader().setBackground(Color.WHITE);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setForeground(Color.GRAY);
        
        // Configurar columna de acciones con botones
        table.getColumn("ACCION").setCellRenderer(new ActionCellRenderer());
        table.getColumn("ACCION").setCellEditor(new ActionCellEditor(new JCheckBox(), this, controller, table));

        // Quitar bordes y fondo del JScrollPane
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);
        
        tablePanel.add(scroll, BorderLayout.CENTER);

        container.add(metrics, BorderLayout.NORTH);
        container.add(tablePanel, BorderLayout.CENTER);

        return container;
    }

    private JPanel buildQuickConsultSection() {
        JpanelR footer = new JpanelR();
        footer.setBackgroundColor(Color.WHITE);
        footer.setPreferredSize(new Dimension(0, 80));
        footer.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 20));

        JLabel lblTitle = new JLabel("Consultas rápidas");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        
        JLabel lblHint = new JLabel("Buscar por nombre o email");
        lblHint.setForeground(Color.GRAY);

        txtBusquedaRapida = new JTextField(30);
        txtBusquedaRapida.setPreferredSize(new Dimension(300, 35));

        btnConsultar = new JButton("Consultar");
        btnConsultar.setBackground(COLOR_SIDEBAR);
        btnConsultar.setForeground(Color.WHITE);
        btnConsultar.setFocusPainted(false);
        btnConsultar.setPreferredSize(new Dimension(120, 35));

        btnConsultar.addActionListener(e -> {
            txtBuscarHeader.setText(txtBusquedaRapida.getText());
            controller.buscarUsuarios();
        });

        footer.add(lblTitle);
        footer.add(lblHint);
        footer.add(txtBusquedaRapida);
        footer.add(btnConsultar);

        return footer;
    }

    // --- MÉTODOS DE APOYO ---

    private JButton createSidebarButton(String text, boolean isActive) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(220, 45));
        btn.setMaximumSize(new Dimension(220, 45));
        btn.setFont(new Font("SansSerif", Font.PLAIN, 14));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(0, 20, 0, 0));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(isActive);
        
        if(isActive) {
            btn.setBackground(COLOR_ACCENT);
            btn.setForeground(Color.WHITE);
        } else {
            btn.setForeground(new Color(200, 200, 200));
        }

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if(!isActive) {
                    btn.setOpaque(true);
                    btn.setBackground(new Color(60, 70, 90));
                    btn.repaint();
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if(!isActive) {
                    btn.setOpaque(false);
                    btn.repaint();
                }
            }
        });

        return btn;
    }
    
    /* 
    * Crea una tarjeta de métrica con título, valor y porcentaje.
    * @param title título de la tarjeta
    * @param value valor de la tarjeta
    * @param bg color de fondo
    * @param percent porcentaje
    * @return JpanelR tarjeta de métrica configurada
    */
    private JpanelR createMetricCard(String title, String value, Color bg, String percent) {
        JpanelR card = new JpanelR();
        card.setBackgroundColor(bg);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(Color.GRAY);
        lblTitle.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JLabel lblVal = new JLabel(value);
        lblVal.setFont(new Font("SansSerif", Font.BOLD, 22));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblVal, BorderLayout.WEST);

        if(percent != null) {
            JLabel lblPerc = new JLabel(percent);
            lblPerc.setForeground(new Color(76, 175, 80));
            lblPerc.setFont(new Font("SansSerif", Font.BOLD, 12));
            card.add(lblPerc, BorderLayout.SOUTH);
        }

        return card;
    }

    private ImageIcon scaleImage(String path, int width, int height) {
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(path));
            Image img = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) {
            return null;
        }
    }

    private void setupFrame() {
        setTitle("SecureAuth Dashboard");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

        /**
     * Maneja el cambio de foco entre botones del menú lateral.
     *
     * @param nuevoBoton botón seleccionado
     */
    private void cambiarBotonActivo(JButton nuevoBoton) {


        // Restaurar botón anterior
        if (botonActivo != null) {
            botonActivo.setBackground(COLOR_SIDEBAR);
            botonActivo.setForeground(new Color(200, 200, 200));
            botonActivo.setOpaque(false);
        }

        // Activar nuevo botón
        nuevoBoton.setBackground(COLOR_ACCENT);
        nuevoBoton.setForeground(Color.WHITE);
        nuevoBoton.setOpaque(true);

        botonActivo = nuevoBoton;
    }
    
    private void accionboont(String text){

        if("btnLogout".equals(text)){

            int confirm = JOptionPane.showConfirmDialog(
            this,
            "¿Seguro que deseas cerrar sesión?",
            "Confirmar",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            controller.logout();
        }
    }

        



    }


    /**
     * =========================
     * CREAR BOTÓN DE MENÚ
     * =========================
     *
     * Aplica estilo uniforme a todos los botones del sidebar.
     *
     * @param texto texto del botón
     * @return JButton configurado
     */
    private JButton createMenuButton(String texto) {

        JButton btn = new JButton(texto);

        /**
         * 🔥 ESTILO BASE (estado normal)
         */
        btn.setBackground(new Color(30, 30, 30));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        /**
         * 🔥 HOVER (efecto al pasar el mouse)
         */
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btn != botonActivo) {
                    btn.setBackground(new Color(50, 50, 50));
                }
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (btn != botonActivo) {
                    btn.setBackground(new Color(30, 30, 30));
                }
            }
        });

        return btn;
    }





    
    
    /**
     * Retorna la tabla de usuarios.
     * 
     * ✔ Usado por el Controller para llenar datos
     */
    public JTable getTable() {
        return table;
    }

    /**
     * Retorna el texto de búsqueda.
     * 
     * ✔ Usado por el Controller
     */
    public String getTextoBusqueda() {
        return txtBuscarHeader.getText();
    }

    /**
     * Retorna el usuario actualmente logueado.
     * 
     * ✔ Usado por el Controller para mostrar info de sesión
     */
    public User getUsuarioActual() {
        return usuarioActual;
    }
    
    
}



class ActionCellRenderer extends JPanel implements TableCellRenderer {

    private JButton btnEditar = new JButton("✏");
    private JButton btnEliminar = new JButton("🗑");

    public ActionCellRenderer() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));

        btnEditar.setBackground(new Color(33, 150, 243));
        btnEditar.setForeground(Color.WHITE);

        btnEliminar.setBackground(new Color(198, 40, 40));
        btnEliminar.setForeground(Color.WHITE);

        add(btnEditar);
        add(btnEliminar);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        return this;
    }
    
   


}





class ActionCellEditor extends DefaultCellEditor {

    private JPanel panel;
    private JButton btnEditar;
    private JButton btnEliminar;
    private JTable table;
    private int row;
    private JFrame parentFrame;
    private IngresoController controller;
    

    public ActionCellEditor(JCheckBox checkBox, JFrame parent, IngresoController controller, JTable table) {
        super(checkBox);
        
        this.parentFrame = parent;
        this.controller = controller;
        this.table = table;

        panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));

        btnEditar = new JButton("✏");
        btnEliminar = new JButton("🗑");

        btnEditar.setBackground(new Color(33, 150, 243));
        btnEditar.setForeground(Color.WHITE);

        btnEliminar.setBackground(new Color(198, 40, 40));
        btnEliminar.setForeground(Color.WHITE);

        // EVENTO EDITAR
        btnEditar.addActionListener(e -> {
            fireEditingStopped();

            int id = (int) table.getValueAt(row, 0);
            System.out.println("Editar usuario ID: " + id);

            controller.editarUsuario(id);
        });

        // EVENTO ELIMINAR
        btnEliminar.addActionListener(e -> {
            fireEditingStopped();

            int id = (int) table.getValueAt(row, 0);
            System.out.println("Eliminar usuario ID: " + id);

            int confirm = JOptionPane.showConfirmDialog(
                    parentFrame,
                    "¿Seguro que deseas eliminar este usuario?",
                    "Confirmar eliminación",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                controller.eliminarUsuario(id);
            }
        });

        panel.add(btnEditar);
        panel.add(btnEliminar);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
                                                boolean isSelected, int row, int column) {
        this.row = row;
        return panel;
    }

}
