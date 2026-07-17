package secureauth.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import secureauth.model.Appointment;
import secureauth.model.Owner;
import secureauth.model.Pet;
import secureauth.model.SaleItem;
import secureauth.service.OwnerService;
import secureauth.service.enterprise.AppointmentService;
import secureauth.ui.utils.UiTheme;

/**
 * Diálogo modal para agendar automáticamente una cita después de vender un servicio.
 *
 * <p>Incluye información de solo lectura del servicio vendido, autocompletado
 * de dueño desde base de datos, carga de mascotas asociadas al dueño y
 * validación de fecha/hora antes de persistir la cita en {@code appointments}.</p>
 *
 * @author Diego
 * @version 1.0
 */
public class ServiceAppointmentDialog extends JDialog {

    private static final int ESTIMATED_DURATION_MINUTES = 60;

    private final SaleItem saleItem;
    private final AppointmentService appointmentService;
    private final OwnerService ownerService;
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.of("es", "CO"));

    private final JComboBox<Owner> ownerCombo = new JComboBox<>();
    private final JComboBox<Pet> petCombo = new JComboBox<>();
    private final JTextField phoneField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JTextField dateField = new JTextField(LocalDate.now().toString());
    private final JTextField timeField = new JTextField(LocalTime.now().plusHours(1).withMinute(0).toString());
    private final JTextArea notesArea = new JTextArea(4, 24);
    private JTextField ownerEditorField;
    private Timer ownerSearchTimer;
    private SwingWorker<List<Owner>, Void> ownerSearchWorker;
    private SwingWorker<List<Pet>, Void> petSearchWorker;
    private Owner selectedOwner;
    private Appointment preparedAppointment;
    private int ownerSearchVersion;
    private boolean suppressOwnerEvents;
    private boolean saved;

    /**
     * Crea el diálogo para una línea de venta de servicio.
     *
     * @param owner ventana padre
     * @param saleItem item vendido que requiere agendamiento
     * @param appointmentService servicio de citas
     */
    public ServiceAppointmentDialog(java.awt.Window owner, SaleItem saleItem, AppointmentService appointmentService) {
        this(owner, saleItem, appointmentService, new OwnerService(new secureauth.dao.OwnerDAO()));
    }

    public ServiceAppointmentDialog(java.awt.Window owner, SaleItem saleItem, AppointmentService appointmentService,
            OwnerService ownerService) {
        super(owner, "Agendar servicio veterinario", ModalityType.APPLICATION_MODAL);
        this.saleItem = saleItem;
        this.appointmentService = appointmentService;
        this.ownerService = ownerService;
        build();
        wireAutocomplete();
        searchOwnersAsync("", false);
    }

    /**
     * @return {@code true} si la cita fue guardada
     */
    public boolean isSaved() {
        return saved;
    }

    /**
     * @return cita validada y lista para persistir con la venta
     */
    public Appointment getPreparedAppointment() {
        return preparedAppointment;
    }

    private void build() {
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(UiTheme.BG_PAGE);
        add(buildHeader(), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);
        setPreferredSize(new Dimension(680, 620));
        pack();
        setLocationRelativeTo(getOwner());
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new GridBagLayout());
        header.setBackground(UiTheme.PANEL_WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UiTheme.BORDER_COLOR),
                new EmptyBorder(18, 20, 16, 20)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        JLabel title = new JLabel("Agendar servicio vendido");
        title.setFont(UiTheme.TITLE_FONT_SECTION);
        title.setForeground(UiTheme.TEXT_PRIMARY);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        header.add(title, gbc);

        gbc.gridwidth = 1;
        addReadOnlyInfo(header, gbc, 1, 0, "Servicio", saleItem.getName());
        addReadOnlyInfo(header, gbc, 1, 1, "Precio", currency.format(saleItem.getPrice()));
        addReadOnlyInfo(header, gbc, 2, 0, "Duración", ESTIMATED_DURATION_MINUTES + " minutos");
        addReadOnlyInfo(header, gbc, 2, 1, "Fecha de venta", LocalDateTime.now().toLocalDate().toString());
        return header;
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UiTheme.PANEL_WHITE);
        form.setBorder(new EmptyBorder(18, 20, 12, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        ownerCombo.setEditable(true);
        ownerCombo.setRenderer(new OwnerRenderer());
        petCombo.setRenderer(new PetRenderer());
        phoneField.setEditable(false);
        emailField.setEditable(false);
        styleField(phoneField);
        styleField(emailField);
        styleField(dateField);
        styleField(timeField);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setFont(UiTheme.BODY_FONT);

        addRow(form, gbc, 0, "Nombre del dueño", ownerCombo);
        addRow(form, gbc, 1, "Teléfono", phoneField);
        addRow(form, gbc, 2, "Correo", emailField);
        addRow(form, gbc, 3, "Mascota", petCombo);
        addRow(form, gbc, 4, "Fecha del servicio (yyyy-MM-dd)", dateField);
        addRow(form, gbc, 5, "Hora del servicio (HH:mm)", timeField);

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.weightx = 0;
        JLabel notesLabel = new JLabel("Observaciones");
        notesLabel.setFont(UiTheme.BODY_FONT);
        form.add(notesLabel, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(new JScrollPane(notesArea), gbc);
        return form;
    }

    private JPanel buildActions() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        actions.setBackground(UiTheme.PANEL_WHITE);
        actions.setBorder(new EmptyBorder(0, 20, 12, 20));
        JButton cancel = new JButton("Cancelar");
        JButton save = new JButton("Guardar cita");
        UiTheme.styleButton(cancel, UiTheme.BTN_DARK, UiTheme.BTN_DARK_HOVER, UiTheme.TEXT_LIGHT, 130, 34, 12,
                true, false, 8);
        UiTheme.styleButton(save, UiTheme.FOREST_GREEN, UiTheme.FOREST_GREEN_HOVER, UiTheme.TEXT_LIGHT, 140, 34,
                12, true, false, 8);
        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> saveAppointment());
        actions.add(cancel);
        actions.add(save);
        return actions;
    }

    private void wireAutocomplete() {
        ownerCombo.addItemListener(e -> {
            if (!suppressOwnerEvents && e.getStateChange() == ItemEvent.SELECTED
                    && e.getItem() instanceof Owner owner) {
                selectOwner(owner);
            }
        });
        ownerCombo.addActionListener(e -> {
            if (!suppressOwnerEvents && ownerCombo.getSelectedItem() instanceof Owner owner) {
                selectOwner(owner);
            }
        });

        Component editor = ownerCombo.getEditor().getEditorComponent();
        if (editor instanceof JTextField textField) {
            ownerEditorField = textField;
            ownerSearchTimer = new Timer(250, e -> searchOwnersAsync(textField.getText(), true));
            ownerSearchTimer.setRepeats(false);
            textField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    scheduleOwnerSearch();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    scheduleOwnerSearch();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    scheduleOwnerSearch();
                }
            });
        }
    }

    private void scheduleOwnerSearch() {
        if (suppressOwnerEvents || ownerSearchTimer == null) {
            return;
        }
        Object selected = ownerCombo.getSelectedItem();
        if (selected instanceof Owner owner && owner.equals(selectedOwner)) {
            return;
        }
        selectedOwner = null;
        clearOwnerDetails();
        ownerSearchTimer.restart();
    }

    private void searchOwnersAsync(String query, boolean showPopup) {
        String normalizedQuery = query == null ? "" : query.trim();
        int version = ++ownerSearchVersion;
        if (ownerSearchWorker != null && !ownerSearchWorker.isDone()) {
            ownerSearchWorker.cancel(true);
        }
        ownerSearchWorker = new SwingWorker<>() {
            @Override
            protected List<Owner> doInBackground() {
                return ownerService.searchOwners(normalizedQuery).stream().limit(8).toList();
            }

            @Override
            protected void done() {
                if (isCancelled() || version != ownerSearchVersion) {
                    return;
                }
                try {
                    updateOwnerSuggestions(get(), query, showPopup);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ServiceAppointmentDialog.this,
                            "No se pudieron cargar dueños: " + ex.getMessage(),
                            "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        ownerSearchWorker.execute();
    }

    private void updateOwnerSuggestions(List<Owner> owners, String editorText, boolean showPopup) {
        suppressOwnerEvents = true;
        try {
            DefaultComboBoxModel<Owner> model = new DefaultComboBoxModel<>();
            for (Owner owner : owners) {
                model.addElement(owner);
            }
            ownerCombo.setModel(model);
            ownerCombo.getEditor().setItem(editorText == null ? "" : editorText);
        } finally {
            suppressOwnerEvents = false;
        }
        if (showPopup && ownerCombo.isShowing() && ownerEditorField != null
                && ownerEditorField.hasFocus() && !owners.isEmpty()
                && editorText != null && !editorText.isBlank()) {
            ownerCombo.setPopupVisible(true);
        } else if (ownerCombo.isPopupVisible()) {
            ownerCombo.setPopupVisible(false);
        }
    }

    private void selectOwner(Owner owner) {
        if (owner == null) {
            return;
        }
        if (ownerSearchTimer != null) {
            ownerSearchTimer.stop();
        }
        ownerSearchVersion++;
        if (ownerSearchWorker != null && !ownerSearchWorker.isDone()) {
            ownerSearchWorker.cancel(true);
        }
        selectedOwner = owner;
        suppressOwnerEvents = true;
        try {
            ownerCombo.getEditor().setItem(owner.getNombreCompleto());
            ownerCombo.setSelectedItem(owner);
        } finally {
            suppressOwnerEvents = false;
        }
        phoneField.setText(empty(owner.getTelefono()));
        emailField.setText(empty(owner.getCorreo()));
        loadPetsAsync(owner.getId());
    }

    private void loadPetsAsync(int ownerId) {
        if (petSearchWorker != null && !petSearchWorker.isDone()) {
            petSearchWorker.cancel(true);
        }
        petCombo.setModel(new DefaultComboBoxModel<>());
        petSearchWorker = new SwingWorker<>() {
            @Override
            protected List<Pet> doInBackground() {
                return appointmentService.findPetsByOwner(ownerId);
            }

            @Override
            protected void done() {
                if (isCancelled()) {
                    return;
                }
                try {
                    DefaultComboBoxModel<Pet> petsModel = new DefaultComboBoxModel<>();
                    for (Pet pet : get()) {
                        petsModel.addElement(pet);
                    }
                    petCombo.setModel(petsModel);
                    if (petsModel.getSize() > 0) {
                        petCombo.setSelectedIndex(0);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ServiceAppointmentDialog.this,
                            "No se pudieron cargar mascotas: " + ex.getMessage(),
                            "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        petSearchWorker.execute();
    }

    private void clearOwnerDetails() {
        phoneField.setText("");
        emailField.setText("");
        if (petCombo.getItemCount() > 0) {
            petCombo.setModel(new DefaultComboBoxModel<>());
        }
    }

    private void saveAppointment() {
        try {
            Owner owner = selectedOwner();
            Pet pet = selectedPet();
            LocalDate date = LocalDate.parse(required(dateField, "Fecha del servicio"));
            LocalTime time = LocalTime.parse(required(timeField, "Hora del servicio"));
            Appointment appointment = new Appointment(null, saleItem.getCatalogItemId(), saleItem.getName(),
                    owner.getId(), owner.getNombreCompleto(), pet.getId(), pet.getNombreMascota(), date, time,
                    "PENDIENTE", notesArea.getText().trim(), LocalDateTime.now(), "Sistema");
            preparedAppointment = appointmentService.prepareForRegistration(appointment);
            saved = true;
            dispose();
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Revisa el formato de fecha u hora.", "Datos inválidos",
                    JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Datos incompletos", JOptionPane.WARNING_MESSAGE);
        }
    }

    private Owner selectedOwner() {
        if (selectedOwner != null) {
            return selectedOwner;
        }
        Object selected = ownerCombo.getSelectedItem();
        if (selected instanceof Owner owner) {
            return owner;
        }
        throw new IllegalArgumentException("Selecciona un dueño registrado desde las sugerencias.");
    }

    private Pet selectedPet() {
        Object selected = petCombo.getSelectedItem();
        if (selected instanceof Pet pet) {
            return pet;
        }
        throw new IllegalArgumentException("Selecciona una mascota registrada para el dueño.");
    }

    private void addReadOnlyInfo(JPanel panel, GridBagConstraints gbc, int row, int column, String label,
            String value) {
        JPanel item = new JPanel(new BorderLayout(0, 3));
        item.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UiTheme.BODY_FONT.deriveFont(12f));
        lbl.setForeground(UiTheme.TEXT_SECONDARY);
        JLabel val = new JLabel(value);
        val.setFont(UiTheme.BODY_FONT.deriveFont(java.awt.Font.BOLD, 14f));
        val.setForeground(UiTheme.TEXT_PRIMARY);
        item.add(lbl, BorderLayout.NORTH);
        item.add(val, BorderLayout.CENTER);
        gbc.gridx = column;
        gbc.gridy = row;
        panel.add(item, gbc);
    }

    private void addRow(JPanel form, GridBagConstraints gbc, int row, String label, Component component) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UiTheme.BODY_FONT);
        form.add(lbl, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(component, gbc);
    }

    private void styleField(JTextField field) {
        field.setFont(UiTheme.BODY_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiTheme.BORDER_COLOR),
                new EmptyBorder(6, 10, 6, 10)));
        field.setBackground(Color.WHITE);
    }

    private String required(JTextField field, String label) {
        String value = field.getText().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Completa el campo: " + label);
        }
        return value;
    }

    private String empty(String value) {
        return value == null ? "" : value;
    }

    private static final class OwnerRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Owner owner) {
                setText(owner.getNombreCompleto() + " - " + (owner.getTelefono() == null ? "" : owner.getTelefono()));
            }
            return this;
        }
    }

    private static final class PetRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Pet pet) {
                setText(pet.getNombreMascota());
            }
            return this;
        }
    }
}
