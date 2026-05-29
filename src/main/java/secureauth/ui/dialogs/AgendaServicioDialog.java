package secureauth.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import secureauth.model.CitaServicio;
import secureauth.service.enterprise.AgendaService;
import secureauth.ui.utils.UiTheme;

/**
 * Diálogo modal para agendar una cita asociada a una venta de servicio.
 *
 * <p>Solicita dueño, mascota, contacto, fecha y horas del servicio. Al
 * confirmar, delega en {@link AgendaService#registrarCita(CitaServicio)} para
 * guardar la cita y publicar la actividad reciente.</p>
 */
public class AgendaServicioDialog extends JDialog {

    private final JTextField ownerField = new JTextField();
    private final JTextField dogField = new JTextField();
    private final JTextField breedField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JTextField serviceField = new JTextField();
    private final JTextField dateField = new JTextField(LocalDate.now().toString());
    private final JTextField startTimeField = new JTextField("09:00");
    private final JTextField pickupTimeField = new JTextField("11:00");
    private final JTextArea notesArea = new JTextArea(4, 24);
    private final AgendaService agendaService;
    private boolean saved;

    /**
     * Crea el diálogo de agenda.
     *
     * @param owner ventana padre
     * @param serviceName servicio vendido que se agenda
     * @param agendaService servicio de agenda
     */
    public AgendaServicioDialog(java.awt.Window owner, String serviceName, AgendaService agendaService) {
        super(owner, "Agendar servicio", ModalityType.APPLICATION_MODAL);
        this.agendaService = agendaService;
        this.serviceField.setText(serviceName);
        build();
    }

    /**
     * @return true si la cita fue guardada correctamente
     */
    public boolean isSaved() {
        return saved;
    }

    private void build() {
        setLayout(new BorderLayout(0, 12));
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(16, 16, 8, 16));
        form.setBackground(UiTheme.PANEL_WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        addRow(form, gbc, 0, "Nombre del dueño", ownerField);
        addRow(form, gbc, 1, "Nombre del perro", dogField);
        addRow(form, gbc, 2, "Raza", breedField);
        addRow(form, gbc, 3, "Teléfono", phoneField);
        addRow(form, gbc, 4, "Servicio solicitado", serviceField);
        addRow(form, gbc, 5, "Fecha (yyyy-MM-dd)", dateField);
        addRow(form, gbc, 6, "Hora servicio (HH:mm)", startTimeField);
        addRow(form, gbc, 7, "Hora recogida (HH:mm)", pickupTimeField);

        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.weightx = 0;
        form.add(new JLabel("Observaciones"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        form.add(new JScrollPane(notesArea), gbc);

        JPanel actions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 10));
        actions.setBackground(UiTheme.PANEL_WHITE);
        JButton cancel = new JButton("Cancelar");
        JButton save = new JButton("Guardar cita");
        UiTheme.styleButton(cancel, UiTheme.BTN_DARK, UiTheme.BTN_DARK_HOVER, UiTheme.TEXT_LIGHT, 130, 34, 12, true, false, 8);
        UiTheme.styleButton(save, UiTheme.FOREST_GREEN, UiTheme.FOREST_GREEN_HOVER, UiTheme.TEXT_LIGHT, 140, 34, 12, true, false, 8);
        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> saveAppointment());
        actions.add(cancel);
        actions.add(save);

        add(form, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
        getRootPane().setBorder(BorderFactory.createLineBorder(UiTheme.BORDER_COLOR));
        setPreferredSize(new Dimension(560, 520));
        pack();
        setLocationRelativeTo(getOwner());
    }

    private void addRow(JPanel form, GridBagConstraints gbc, int row, String label, JTextField field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        form.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(field, gbc);
    }

    private void saveAppointment() {
        try {
            CitaServicio cita = new CitaServicio(null, required(ownerField, "Nombre del dueño"),
                    required(dogField, "Nombre del perro"), breedField.getText().trim(),
                    required(phoneField, "Teléfono"), required(serviceField, "Servicio solicitado"),
                    LocalDate.parse(required(dateField, "Fecha")),
                    LocalTime.parse(required(startTimeField, "Hora del servicio")),
                    LocalTime.parse(required(pickupTimeField, "Hora de recogida")),
                    notesArea.getText().trim(), "AGENDADA");
            agendaService.registrarCita(cita);
            saved = true;
            JOptionPane.showMessageDialog(this, "Cita agendada correctamente.");
            dispose();
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Revisa el formato de fecha u hora.", "Datos inválidos",
                    JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Datos incompletos", JOptionPane.WARNING_MESSAGE);
        } catch (java.sql.SQLException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo registrar la cita: " + ex.getMessage(),
                    "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String required(JTextField field, String label) {
        String value = field.getText().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Completa el campo: " + label);
        }
        return value;
    }
}
