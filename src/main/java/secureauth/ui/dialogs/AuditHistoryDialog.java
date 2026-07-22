package secureauth.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import secureauth.model.ActividadReciente;
import secureauth.service.enterprise.ActividadRecienteService;
import secureauth.ui.utils.UiTheme;

/**
 * Diálogo profesional de Auditoría del Sistema con filtros avanzados de búsqueda y exportación.
 *
 * @author Diego
 * @version 1.0
 */
public class AuditHistoryDialog extends JDialog {

    private final ActividadRecienteService auditService;
    private final JTextField searchField = new JTextField(15);
    private final JTextField userField = new JTextField(10);
    private final JComboBox<String> moduleCombo = new JComboBox<>(new String[]{"Todas", "Ventas", "Inventario", "Usuarios", "Servicios", "Citas", "Sistema"});
    private final JComboBox<String> dateCombo = new JComboBox<>(new String[]{"Todas", "Hoy", "Esta semana", "Este mes", "Este año"});
    private final DefaultTableModel tableModel;
    private final JTable auditTable;
    private List<ActividadReciente> loadedRecords;

    public AuditHistoryDialog(java.awt.Window owner, ActividadRecienteService auditService) {
        super(owner, "Auditoría de Actividad del Sistema", ModalityType.APPLICATION_MODAL);
        this.auditService = auditService;

        setLayout(new BorderLayout(0, 16));
        getContentPane().setBackground(UiTheme.BG_PAGE);
        getRootPane().setBorder(BorderFactory.createLineBorder(UiTheme.BORDER_COLOR));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(15, 23, 42));
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel title = new JLabel("Historial de Auditoría del Sistema");
        title.setFont(UiTheme.TITLE_FONT_SECTION.deriveFont(java.awt.Font.BOLD, 20f));
        title.setForeground(UiTheme.TEXT_LIGHT);
        headerPanel.add(title, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Filters Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        filterPanel.setOpaque(false);
        filterPanel.setBorder(new EmptyBorder(0, 10, 0, 10));

        searchField.setFont(UiTheme.BODY_FONT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        searchField.setPreferredSize(new Dimension(150, 32));

        userField.setFont(UiTheme.BODY_FONT);
        userField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        userField.setPreferredSize(new Dimension(100, 32));

        styleCombo(moduleCombo);
        styleCombo(dateCombo);

        JButton btnSearch = buildDarkButton("Filtrar");
        btnSearch.addActionListener(e -> refreshData());

        filterPanel.add(new JLabel("Descripción:"));
        filterPanel.add(searchField);
        filterPanel.add(new JLabel("Usuario:"));
        filterPanel.add(userField);
        filterPanel.add(new JLabel("Módulo:"));
        filterPanel.add(moduleCombo);
        filterPanel.add(new JLabel("Fecha:"));
        filterPanel.add(dateCombo);
        filterPanel.add(btnSearch);

        // Table
        String[] columns = {"ID", "Fecha / Hora Real", "Usuario", "Módulo", "Descripción / Acción"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        auditTable = new JTable(tableModel);
        styleTable(auditTable);

        JScrollPane scrollPane = new JScrollPane(auditTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER_COLOR));

        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(0, 20, 0, 20));
        centerPanel.add(filterPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Bottom Actions
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        bottomPanel.setBackground(UiTheme.BG_PAGE);
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UiTheme.BORDER_COLOR));

        JButton btnClose = new JButton("Cerrar");
        UiTheme.styleButton(btnClose, UiTheme.BTN_DARK, UiTheme.BTN_DARK_HOVER, UiTheme.TEXT_LIGHT, 120, 36, 13, true, false, 8);
        btnClose.addActionListener(e -> dispose());
        bottomPanel.add(btnClose);
        add(bottomPanel, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(1100, 600));
        pack();
        setLocationRelativeTo(owner);
        refreshData();
    }

    private void styleCombo(JComboBox<String> combo) {
        combo.setFont(UiTheme.BODY_FONT);
        combo.setPreferredSize(new Dimension(120, 32));
        combo.setBackground(Color.WHITE);
    }

    private void refreshData() {
        try {
            String query = searchField.getText();
            String userFilter = userField.getText();
            String moduleFilter = mapModuleFilter(moduleCombo.getSelectedItem().toString());
            String dateFilter = mapDateFilter(dateCombo.getSelectedItem().toString());

            loadedRecords = auditService.findAdvanced(query, moduleFilter, dateFilter, userFilter);
            tableModel.setRowCount(0);

            for (ActividadReciente record : loadedRecords) {
                tableModel.addRow(new Object[]{
                        record.idActividad(),
                        record.timestampReal(), // Exact real timestamp retrieved directly from DB without shift!
                        record.usuario(),
                        record.tipo(),
                        record.descripcion()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al filtrar auditoría: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String mapModuleFilter(String selection) {
        return switch (selection) {
            case "Ventas" -> "VENTAS";
            case "Inventario" -> "INVENTARIO";
            case "Usuarios" -> "USUARIOS";
            case "Servicios" -> "SERVICIOS";
            case "Citas" -> "CITAS";
            case "Sistema" -> "SISTEMA";
            default -> "TODAS";
        };
    }

    private String mapDateFilter(String selection) {
        return switch (selection) {
            case "Hoy" -> "HOY";
            case "Esta semana" -> "SEMANA";
            case "Este mes" -> "MES";
            case "Este año" -> "ANIO";
            default -> "TODAS";
        };
    }

    private void styleTable(JTable table) {
        table.setRowHeight(38);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(UiTheme.BORDER_COLOR);
        table.setFont(UiTheme.BODY_FONT);
        table.setSelectionBackground(new Color(240, 245, 255));
        table.setSelectionForeground(Color.BLACK);
        table.setBackground(Color.WHITE);

        table.getTableHeader().setBackground(Color.WHITE);
        table.getTableHeader().setFont(UiTheme.SMALL_FONT.deriveFont(Font.BOLD));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setBackground(Color.WHITE);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 225, 230)),
                        BorderFactory.createEmptyBorder(10, 15, 10, 15)
                ));
                return c;
            }
        });

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(100);
        table.getColumnModel().getColumn(4).setPreferredWidth(550);
    }

    private JButton buildDarkButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(UiTheme.SMALL_FONT.deriveFont(Font.BOLD));
        btn.setBackground(UiTheme.BTN_DARK);
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(90, 32));
        return btn;
    }
}
