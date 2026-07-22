package secureauth.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import secureauth.model.Appointment;
import secureauth.model.AppointmentStatus;
import secureauth.service.enterprise.AppointmentService;
import secureauth.shared.util.ServiceScheduleHelper;
import secureauth.ui.utils.UiTheme;

/**
 * Diálogo profesional para la administración de citas e historial con filtros avanzados.
 *
 * @author Diego
 * @version 1.0
 */
public class AppointmentHistoryDialog extends JDialog {

    private final AppointmentService appointmentService;
    private final JTextField searchField = new JTextField(20);
    private final JComboBox<String> dateFilterCombo = new JComboBox<>(new String[]{"Todas", "Hoy", "Esta semana", "Este mes", "Este año"});
    private final JComboBox<String> statusFilterCombo = new JComboBox<>(new String[]{"Todas", "Pendientes", "Finalizadas", "Canceladas", "Archivadas"});
    private final DefaultTableModel tableModel;
    private final JTable appointmentsTable;
    private List<Appointment> loadedAppointments;

    public AppointmentHistoryDialog(java.awt.Window owner, AppointmentService appointmentService) {
        super(owner, "Historial y Administración de Citas", ModalityType.APPLICATION_MODAL);
        this.appointmentService = appointmentService;

        setLayout(new BorderLayout(0, 16));
        getContentPane().setBackground(UiTheme.BG_PAGE);
        getRootPane().setBorder(BorderFactory.createLineBorder(UiTheme.BORDER_COLOR));

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(9, 25, 47));
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel title = new JLabel("Historial y Administración de Citas");
        title.setFont(UiTheme.TITLE_FONT_SECTION.deriveFont(java.awt.Font.BOLD, 20f));
        title.setForeground(UiTheme.TEXT_LIGHT);
        headerPanel.add(title, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Filters Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        filterPanel.setOpaque(false);
        filterPanel.setBorder(new EmptyBorder(0, 10, 0, 10));

        searchField.setFont(UiTheme.BODY_FONT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        searchField.setPreferredSize(new Dimension(200, 32));

        styleCombo(dateFilterCombo);
        styleCombo(statusFilterCombo);

        JButton btnSearch = buildDarkButton("Buscar");
        btnSearch.addActionListener(e -> refreshData());

        filterPanel.add(new JLabel("Buscar:"));
        filterPanel.add(searchField);
        filterPanel.add(new JLabel("Fecha:"));
        filterPanel.add(dateFilterCombo);
        filterPanel.add(new JLabel("Estado:"));
        filterPanel.add(statusFilterCombo);
        filterPanel.add(btnSearch);

        // Table Panel
        String[] columns = {"ID", "Mascota", "Servicio", "Dueño", "Horario / Rango", "Veterinario", "Estado"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        appointmentsTable = new JTable(tableModel);
        styleTable(appointmentsTable);
        appointmentsTable.setComponentPopupMenu(createPopupMenu());
        appointmentsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                selectRowAtPoint(e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                selectRowAtPoint(e);
            }
        });

        JScrollPane scrollPane = new JScrollPane(appointmentsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER_COLOR));

        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(0, 20, 0, 20));
        centerPanel.add(filterPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Actions panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        bottomPanel.setBackground(UiTheme.BG_PAGE);
        bottomPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UiTheme.BORDER_COLOR));

        JButton btnClose = new JButton("Cerrar");
        UiTheme.styleButton(btnClose, UiTheme.BTN_DARK, UiTheme.BTN_DARK_HOVER, UiTheme.TEXT_LIGHT, 120, 36, 13, true, false, 8);
        btnClose.addActionListener(e -> dispose());
        bottomPanel.add(btnClose);
        add(bottomPanel, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(1000, 600));
        pack();
        setLocationRelativeTo(owner);
        refreshData();
    }

    private void styleCombo(JComboBox<String> combo) {
        combo.setFont(UiTheme.BODY_FONT);
        combo.setPreferredSize(new Dimension(140, 32));
        combo.setBackground(Color.WHITE);
    }

    private void selectRowAtPoint(MouseEvent e) {
        int r = appointmentsTable.rowAtPoint(e.getPoint());
        if (r >= 0 && r < appointmentsTable.getRowCount()) {
            appointmentsTable.setRowSelectionInterval(r, r);
        } else {
            appointmentsTable.clearSelection();
        }
    }

    private void refreshData() {
        try {
            String query = searchField.getText();
            String dateFilter = mapDateFilter(dateFilterCombo.getSelectedItem().toString());
            String statusFilter = mapStatusFilter(statusFilterCombo.getSelectedItem().toString());

            loadedAppointments = appointmentService.findAdvanced(query, dateFilter, statusFilter);
            tableModel.setRowCount(0);

            for (Appointment appointment : loadedAppointments) {
                String timeAndDate;
                if (appointment.getEndDate() != null && !appointment.getEndDate().equals(appointment.getAppointmentDate())) {
                    String interval = ServiceScheduleHelper.formatInterval(appointment.getAppointmentDate(), appointment.getEndDate());
                    timeAndDate = interval + " (" + appointment.getAppointmentTime() + " → " + appointment.getEndTime() + ")";
                } else {
                    timeAndDate = appointment.getAppointmentDate().toString() + " " + appointment.getAppointmentTime();
                }

                tableModel.addRow(new Object[]{
                        appointment.getId(),
                        appointment.getPetName(),
                        appointment.getServiceName(),
                        appointment.getOwnerName(),
                        timeAndDate,
                        appointment.getCreatedBy(),
                        displayStatus(appointment.getStatus())
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar las citas: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
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

    private String mapStatusFilter(String selection) {
        return switch (selection) {
            case "Pendientes" -> "PENDIENTES";
            case "Finalizadas" -> "FINALIZADAS";
            case "Canceladas" -> "CANCELADAS";
            case "Archivadas" -> "ARCHIVADAS";
            default -> "TODAS";
        };
    }

    private static String displayStatus(String status) {
        return AppointmentStatus.fromDatabaseValue(status)
                .map(AppointmentStatus::displayName)
                .orElse(status == null || status.isBlank() ? "-" : status);
    }

    private JPopupMenu createPopupMenu() {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem editItem = new JMenuItem("Editar Cita");
        JMenuItem finalizeItem = new JMenuItem("Finalizar Cita");
        JMenuItem cancelItem = new JMenuItem("Cancelar Cita");
        JMenuItem archiveItem = new JMenuItem("Archivar Cita");

        editItem.addActionListener(e -> editSelected());
        finalizeItem.addActionListener(e -> updateSelectedStatus("FINALIZADO"));
        cancelItem.addActionListener(e -> updateSelectedStatus("CANCELADA"));
        archiveItem.addActionListener(e -> updateSelectedStatus("ARCHIVADA"));

        menu.add(editItem);
        menu.add(finalizeItem);
        menu.add(cancelItem);
        menu.add(archiveItem);

        return menu;
    }

    private Appointment getSelectedAppointment() {
        int idx = appointmentsTable.getSelectedRow();
        if (idx < 0 || idx >= loadedAppointments.size()) {
            return null;
        }
        return loadedAppointments.get(idx);
    }

    private void updateSelectedStatus(String status) {
        Appointment appointment = getSelectedAppointment();
        if (appointment == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una cita.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            appointmentService.updateStatus(appointment.getId(), status);
            JOptionPane.showMessageDialog(this, "Estado actualizado correctamente.");
            refreshData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al actualizar estado: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editSelected() {
        Appointment appointment = getSelectedAppointment();
        if (appointment == null) {
            JOptionPane.showMessageDialog(this, "Selecciona una cita.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }
        EditAppointmentDialog dialog = new EditAppointmentDialog(this, appointment);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshData();
        }
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

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (column == 6) {
                    String status = value == null ? "" : value.toString();
                    setHorizontalAlignment(SwingConstants.CENTER);
                    setFont(UiTheme.BODY_FONT.deriveFont(Font.BOLD));
                    if (status.equalsIgnoreCase("En proceso")) {
                        c.setBackground(new Color(219, 234, 254));
                        c.setForeground(new Color(30, 64, 175));
                    } else if (status.equalsIgnoreCase("Finalizado") || status.equalsIgnoreCase("Finalizada")) {
                        c.setBackground(new Color(220, 252, 231));
                        c.setForeground(new Color(22, 101, 52));
                    } else if (status.equalsIgnoreCase("Cancelada") || status.equalsIgnoreCase("Cancelado")) {
                        c.setBackground(new Color(254, 226, 226));
                        c.setForeground(new Color(153, 27, 27));
                    } else if (status.equalsIgnoreCase("Archivada") || status.equalsIgnoreCase("Archivado")) {
                        c.setBackground(new Color(243, 244, 246));
                        c.setForeground(new Color(75, 85, 99));
                    } else {
                        c.setBackground(new Color(254, 249, 195));
                        c.setForeground(new Color(66, 32, 6));
                    }
                } else {
                    c.setBackground(isSelected ? new Color(240, 245, 255) : Color.WHITE);
                    c.setForeground(Color.BLACK);
                    setHorizontalAlignment(SwingConstants.LEFT);
                }
                return c;
            }
        });
    }

    private JButton buildDarkButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(UiTheme.SMALL_FONT.deriveFont(Font.BOLD));
        btn.setBackground(UiTheme.BTN_DARK);
        btn.setForeground(Color.WHITE);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(100, 32));
        return btn;
    }

    /**
     * Sub-diálogo modal para editar una cita.
     */
    private static class EditAppointmentDialog extends JDialog {
        private final Appointment appointment;
        private final JTextField serviceField;
        private final JTextField vetField;
        private final JSpinner dateSpinner;
        private final JSpinner hourSpinner;
        private final JSpinner minuteSpinner;
        private final JSpinner endDateSpinner;
        private final JSpinner endHourSpinner;
        private final JSpinner endMinuteSpinner;
        private final JTextArea notesArea;
        private final JTextField durationField;
        private final JComboBox<String> statusCombo;
        private boolean saved = false;

        public EditAppointmentDialog(JDialog parent, Appointment appointment) {
            super(parent, "Editar Cita", ModalityType.APPLICATION_MODAL);
            this.appointment = appointment;

            setLayout(new BorderLayout(0, 16));
            getContentPane().setBackground(UiTheme.PANEL_WHITE);

            serviceField = new JTextField(appointment.getServiceName());
            vetField = new JTextField(appointment.getCreatedBy());

            Date startDateVal = Date.from(appointment.getAppointmentDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
            dateSpinner = new JSpinner(new SpinnerDateModel(startDateVal, null, null, Calendar.DAY_OF_MONTH));
            dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));

            hourSpinner = new JSpinner(new SpinnerNumberModel(appointment.getAppointmentTime().getHour(), 0, 23, 1));
            minuteSpinner = new JSpinner(new SpinnerNumberModel(appointment.getAppointmentTime().getMinute(), 0, 59, 5));

            Date endDateVal = Date.from(appointment.getEndDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
            endDateSpinner = new JSpinner(new SpinnerDateModel(endDateVal, null, null, Calendar.DAY_OF_MONTH));
            endDateSpinner.setEditor(new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd"));

            endHourSpinner = new JSpinner(new SpinnerNumberModel(appointment.getEndTime().getHour(), 0, 23, 1));
            endMinuteSpinner = new JSpinner(new SpinnerNumberModel(appointment.getEndTime().getMinute(), 0, 59, 5));

            notesArea = new JTextArea(appointment.getNotes() != null ? appointment.getNotes() : "", 3, 20);
            notesArea.setLineWrap(true);
            notesArea.setWrapStyleWord(true);

            durationField = new JTextField();
            durationField.setEditable(false);

            statusCombo = new JComboBox<>(new String[]{"PENDIENTE", "CONFIRMADA", "EN_PROCESO", "FINALIZADO", "CANCELADA", "ARCHIVADA"});
            statusCombo.setSelectedItem(appointment.getStatus());

            JPanel form = new JPanel(new GridBagLayout());
            form.setBackground(Color.WHITE);
            form.setBorder(new EmptyBorder(16, 20, 16, 20));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(8, 8, 8, 8);

            // Row 0: Servicio
            gbc.gridx = 0; gbc.gridy = 0;
            form.add(new JLabel("Servicio:"), gbc);
            gbc.gridx = 1;
            form.add(serviceField, gbc);

            // Row 1: Veterinario
            gbc.gridx = 0; gbc.gridy = 1;
            form.add(new JLabel("Veterinario:"), gbc);
            gbc.gridx = 1;
            form.add(vetField, gbc);

            // Row 2: Inicio
            gbc.gridx = 0; gbc.gridy = 2;
            form.add(new JLabel("Inicio:"), gbc);
            JPanel startPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            startPanel.setOpaque(false);
            startPanel.add(dateSpinner);
            startPanel.add(hourSpinner);
            startPanel.add(new JLabel(":"));
            startPanel.add(minuteSpinner);
            gbc.gridx = 1;
            form.add(startPanel, gbc);

            // Row 3: Fin
            gbc.gridx = 0; gbc.gridy = 3;
            form.add(new JLabel("Fin:"), gbc);
            JPanel endPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            endPanel.setOpaque(false);
            endPanel.add(endDateSpinner);
            endPanel.add(endHourSpinner);
            endPanel.add(new JLabel(":"));
            endPanel.add(endMinuteSpinner);
            gbc.gridx = 1;
            form.add(endPanel, gbc);

            // Row 4: Duración
            gbc.gridx = 0; gbc.gridy = 4;
            form.add(new JLabel("Duración:"), gbc);
            gbc.gridx = 1;
            form.add(durationField, gbc);

            // Row 5: Estado
            gbc.gridx = 0; gbc.gridy = 5;
            form.add(new JLabel("Estado:"), gbc);
            gbc.gridx = 1;
            form.add(statusCombo, gbc);

            // Row 6: Observaciones
            gbc.gridx = 0; gbc.gridy = 6;
            form.add(new JLabel("Notas:"), gbc);
            gbc.gridx = 1;
            form.add(new JScrollPane(notesArea), gbc);

            add(form, BorderLayout.CENTER);

            // Update duration real-time
            javax.swing.event.ChangeListener scheduleListener = e -> updateDuration();
            dateSpinner.addChangeListener(scheduleListener);
            hourSpinner.addChangeListener(scheduleListener);
            minuteSpinner.addChangeListener(scheduleListener);
            endDateSpinner.addChangeListener(scheduleListener);
            endHourSpinner.addChangeListener(scheduleListener);
            endMinuteSpinner.addChangeListener(scheduleListener);
            updateDuration();

            // Actions
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
            actions.setBackground(UiTheme.BG_PAGE);
            JButton btnCancel = new JButton("Cancelar");
            JButton btnSave = new JButton("Guardar");

            UiTheme.styleButton(btnCancel, UiTheme.BTN_DARK, UiTheme.BTN_DARK_HOVER, UiTheme.TEXT_LIGHT, 100, 32, 12, true, false, 6);
            UiTheme.styleButton(btnSave, UiTheme.FOREST_GREEN, UiTheme.FOREST_GREEN_HOVER, UiTheme.TEXT_LIGHT, 100, 32, 12, true, false, 6);

            btnCancel.addActionListener(e -> dispose());
            btnSave.addActionListener(e -> saveChanges());

            actions.add(btnCancel);
            actions.add(btnSave);
            add(actions, BorderLayout.SOUTH);

            pack();
            setLocationRelativeTo(parent);
        }

        private LocalDate getLocalDate(JSpinner spinner) {
            Date selected = (Date) spinner.getValue();
            return selected.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }

        private LocalTime getLocalTime(JSpinner hrSpinner, JSpinner minSpinner) {
            int h = ((Number) hrSpinner.getValue()).intValue();
            int m = ((Number) minSpinner.getValue()).intValue();
            return LocalTime.of(h, m);
        }

        private void updateDuration() {
            try {
                LocalDate startD = getLocalDate(dateSpinner);
                LocalTime startT = getLocalTime(hourSpinner, minuteSpinner);
                LocalDate endD = getLocalDate(endDateSpinner);
                LocalTime endT = getLocalTime(endHourSpinner, endMinuteSpinner);

                String durStr = ServiceScheduleHelper.calculateDurationString(serviceField.getText(), startD, startT, endD, endT);
                durationField.setText(durStr);
            } catch (RuntimeException ex) {
                durationField.setText("Intervalo inválido");
            }
        }

        private void saveChanges() {
            try {
                LocalDate startD = getLocalDate(dateSpinner);
                LocalTime startT = getLocalTime(hourSpinner, minuteSpinner);
                LocalDate endD = getLocalDate(endDateSpinner);
                LocalTime endT = getLocalTime(endHourSpinner, endMinuteSpinner);

                ServiceScheduleHelper.validateInterval(startD, startT, endD, endT);

                appointment.setServiceName(serviceField.getText().trim());
                appointment.setCreatedBy(vetField.getText().trim());
                appointment.setAppointmentDate(startD);
                appointment.setAppointmentTime(startT);
                appointment.setEndDate(endD);
                appointment.setEndTime(endT);
                appointment.setStatus(statusCombo.getSelectedItem().toString());
                appointment.setNotes(notesArea.getText().trim());

                AppointmentHistoryDialog parent = (AppointmentHistoryDialog) getParent();
                parent.appointmentService.updateAppointment(appointment);
                saved = true;
                dispose();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Datos inválidos", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error al guardar cambios: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        public boolean isSaved() {
            return saved;
        }
    }
}
