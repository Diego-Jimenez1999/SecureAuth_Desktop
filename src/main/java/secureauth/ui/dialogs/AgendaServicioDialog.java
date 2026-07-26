package secureauth.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import secureauth.dao.OwnerDAO;
import secureauth.dao.PetDAO;
import secureauth.model.CitaServicio;
import secureauth.model.Owner;
import secureauth.model.Pet;
import secureauth.service.OwnerService;
import secureauth.service.PetService;
import secureauth.service.enterprise.AgendaService;
import secureauth.ui.utils.UiTheme;

/**
 * Diálogo compacto para agendar servicios desde ventas con flujo de ERP veterinario.
 */
public class AgendaServicioDialog extends JDialog {

    private static final int DEFAULT_DURATION_MINUTES = 120;

    private final JTextField serviceField = new JTextField();
    private final JLabel endTimeField = new JLabel();
    private final JComboBox<Owner> ownerCombo = new JComboBox<>();
    private final JTextField phoneField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JComboBox<Pet> petCombo = new JComboBox<>();
    private final JTextField speciesField = new JTextField("Canino");
    private final JTextField breedField = new JTextField();
    private final JTextField ageField = new JTextField();
    private final JLabel availabilityLabel = new JLabel(" ");
    private final JLabel summaryOwnerLabel = new JLabel("-");
    private final JLabel summaryPetLabel = new JLabel("-");
    private final JLabel summaryServiceLabel = new JLabel("-");
    private final JLabel summaryDurationLabel = new JLabel("-");
    private final JLabel summaryDateLabel = new JLabel("-");
    private final JLabel summaryStartLabel = new JLabel("-");
    private final JLabel summaryEndLabel = new JLabel("-");
    private final JSpinner dateSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null,
            Calendar.DAY_OF_MONTH));
    private final JSpinner hourSpinner = new JSpinner(new SpinnerNumberModel(nextBookableTime().getHour(), 0, 23, 1));
    private final JSpinner minuteSpinner = new JSpinner(new SpinnerNumberModel(nextBookableTime().getMinute(), 0, 59,
            5));
    private final JSpinner durationSpinner = new JSpinner(new SpinnerNumberModel(DEFAULT_DURATION_MINUTES, 1, 480,
            5));
    private final JTextArea notesArea = new JTextArea(3, 28);

    private final AgendaService agendaService;
    private final OwnerService ownerService;
    private final PetService petService;
    private JTextField ownerEditorField;
    private Timer ownerSearchTimer;
    private SwingWorker<List<Owner>, Void> ownerSearchWorker;
    private SwingWorker<List<Pet>, Void> petSearchWorker;
    private Owner selectedOwner;
    private boolean suppressOwnerEvents;
    private int ownerSearchVersion;
    private boolean saved;
    private boolean finalizeSale;

    /**
     * Crea el diálogo de agenda.
     *
     * @param owner ventana padre
     * @param serviceName servicio vendido que se agenda
     * @param agendaService servicio de agenda
     */
    public AgendaServicioDialog(Window owner, String serviceName, AgendaService agendaService) {
        this(owner, serviceName, agendaService, new OwnerService(new OwnerDAO()), new PetService(new PetDAO()));
    }

    public AgendaServicioDialog(Window owner, String serviceName, AgendaService agendaService,
            OwnerService ownerService, PetService petService) {
        super(owner, "Agenda de servicio veterinario", ModalityType.APPLICATION_MODAL);
        this.agendaService = agendaService;
        this.ownerService = ownerService;
        this.petService = petService;
        serviceField.setText(serviceName);
        build();
        wireEvents();
        searchOwnersAsync("", false);
        updateEndTime();
        checkAvailabilityAsync();
    }

    /**
     * @return true si la cita fue guardada correctamente
     */
    public boolean isSaved() {
        return saved;
    }

    /**
     * @return true si la acción elegida fue finalizar la venta después de agendar
     */
    public boolean isFinalizeSale() {
        return finalizeSale;
    }

    private void build() {
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(UiTheme.BG_PAGE);
        add(buildTopBar(), BorderLayout.NORTH);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(UiTheme.BG_PAGE);
        content.setBorder(new EmptyBorder(10, 12, 10, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 0, 10, 0);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        // IMPORTANTE: si ninguna fila tiene weighty, GridBagLayout reparte cualquier
        // espacio vertical sobrante como huecos entre filas (aquí aparecía ese hueco
        // en blanco tapando "Cliente"/"Mascota"). Cada fila fija va con weighty = 0
        // explícito y solo la última (Observaciones) se queda con weighty = 1 para
        // absorber el espacio extra que deja setMinimumSize más abajo.
        gbc.weighty = 0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        content.add(buildServiceCard(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 10, 8);
        content.add(buildClientSection(), gbc);
        gbc.gridx = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 8, 10, 0);
        content.add(buildPetSection(), gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(0, 0, 10, 0);
        content.add(buildScheduleSection(), gbc);
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 16, 10, 0);
        gbc.weightx = 0.35;
        gbc.gridheight = 2;
        content.add(buildSummarySection(), gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.gridheight = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        content.add(buildNotesSection(), gbc);

        add(content, BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);
        getRootPane().setBorder(BorderFactory.createLineBorder(UiTheme.BORDER_COLOR));
        configureInputs();
        updateSummaryVisual();
        pack();
        // setMinimumSize (no setPreferredSize) evita forzar un tamaño mayor al contenido
        // real antes de calcular el layout; ahora que las filas tienen weighty correcto,
        // cualquier espacio extra por el mínimo se lo queda "Observaciones", no un hueco
        // en medio del formulario.
        setMinimumSize(new Dimension(1120, 720));
        setLocationRelativeTo(getOwner());
    }

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(9, 25, 47));
        top.setBorder(new EmptyBorder(12, 16, 12, 16));
        JLabel title = new JLabel("Agendar servicio vendido");
        title.setFont(UiTheme.TITLE_FONT_SECTION.deriveFont(java.awt.Font.BOLD, 18f));
        title.setForeground(UiTheme.TEXT_LIGHT);
        top.add(title, BorderLayout.WEST);
        return top;
    }

    private JPanel buildServiceCard() {
        JPanel header = cardPanel();
        header.setLayout(new GridBagLayout());
        header.setBorder(cardBorder());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 18);

        JLabel badge = new JLabel("SERVICIO");
        badge.setHorizontalAlignment(JLabel.CENTER);
        badge.setOpaque(true);
        badge.setForeground(UiTheme.TEXT_LIGHT);
        badge.setBackground(UiTheme.FOREST_GREEN);
        badge.setBorder(new EmptyBorder(18, 16, 18, 16));
        gbc.gridx = 0;
        gbc.weightx = 0;
        header.add(badge, gbc);

        JPanel name = new JPanel(new BorderLayout(0, 5));
        name.setOpaque(false);
        JLabel title = new JLabel("Servicio: " + serviceField.getText());
        title.setFont(UiTheme.TITLE_FONT_SECTION.deriveFont(java.awt.Font.BOLD, 20f));
        title.setForeground(UiTheme.TEXT_PRIMARY);
        JLabel state = new JLabel("VENDIDO");
        state.setFont(UiTheme.BODY_FONT.deriveFont(java.awt.Font.BOLD, 11f));
        state.setForeground(UiTheme.FOREST_GREEN);
        name.add(title, BorderLayout.NORTH);
        name.add(state, BorderLayout.CENTER);
        gbc.gridx = 1;
        gbc.weightx = 0.55;
        header.add(name, gbc);

        JPanel metrics = new JPanel(new GridBagLayout());
        metrics.setOpaque(false);
        GridBagConstraints mg = new GridBagConstraints();
        mg.fill = GridBagConstraints.HORIZONTAL;
        mg.weightx = 1;
        mg.insets = new Insets(0, 0, 0, 22);
        mg.gridx = 0;
        metrics.add(metric("Precio", valueLabel("Según venta")), mg);
        mg.gridx = 1;
        metrics.add(metric("Duración", durationSpinner), mg);
        mg.gridx = 2;
        mg.insets = new Insets(0, 0, 0, 0);
        metrics.add(metric("Fecha de venta", valueLabel(LocalDate.now().toString())), mg);
        gbc.gridx = 2;
        gbc.weightx = 1;
        header.add(metrics, gbc);

        JPanel total = new JPanel(new BorderLayout(0, 4));
        total.setBackground(new Color(237, 250, 242));
        total.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(188, 226, 204)),
                new EmptyBorder(12, 24, 12, 24)));
        JLabel totalLabel = fieldLabel("Total a pagar");
        JLabel totalValue = new JLabel("Según venta");
        totalValue.setFont(UiTheme.TITLE_FONT_SECTION.deriveFont(java.awt.Font.BOLD, 18f));
        totalValue.setForeground(UiTheme.FOREST_GREEN);
        total.add(totalLabel, BorderLayout.NORTH);
        total.add(totalValue, BorderLayout.CENTER);
        gbc.gridx = 3;
        gbc.weightx = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        header.add(total, gbc);
        return header;
    }

    private JPanel buildClientSection() {
        ownerCombo.setEditable(true);
        ownerCombo.setRenderer(new OwnerRenderer());
        JButton newOwner = secondaryButton("Nuevo cliente");
        newOwner.addActionListener(e -> createOwnerInline());
        JPanel panel = cardPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(accentCardBorder());
        addCardHeader(panel, "Cliente", newOwner);
        addFullField(panel, 1, "Buscar cliente (Documento, nombre o teléfono)", ownerCombo);
        addHalfFields(panel, 3, "Teléfono", phoneField, "Correo", emailField);
        panel.setPreferredSize(new Dimension(520, 210));
        return panel;
    }

    private JPanel buildPetSection() {
        petCombo.setRenderer(new PetRenderer());
        JButton newPet = secondaryButton("Nueva mascota");
        newPet.addActionListener(e -> createPetInline());
        JPanel panel = cardPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(cardBorder());
        addCardHeader(panel, "Mascota", newPet);
        addFullField(panel, 1, "Seleccionar mascota", petCombo);
        addThirdFields(panel, 3, "Especie", speciesField, "Raza", breedField, "Edad", ageField);
        panel.setPreferredSize(new Dimension(520, 210));
        return panel;
    }

    private JPanel buildScheduleSection() {
        JPanel panel = section("Agenda", new String[]{"Fecha", "Hora de inicio", "Duración", "Hora de finalización"},
                new Component[]{dateSpinner, hourSpinner, durationSpinner, endTimeField});
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(12, 0, 0, 0);
        panel.add(availabilityLabel, gbc);
        return panel;
    }

    private JPanel buildSummarySection() {
        JPanel panel = cardPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(accentCardBorder());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(sectionTitle("Resumen de la cita"), gbc);
        addSummaryRow(panel, 1, "Cliente:", summaryOwnerLabel);
        addSummaryRow(panel, 2, "Mascota:", summaryPetLabel);
        addSummaryRow(panel, 3, "Servicio:", summaryServiceLabel);
        addSummaryRow(panel, 4, "Duración:", summaryDurationLabel);
        addSummaryRow(panel, 5, "Fecha:", summaryDateLabel);
        addSummaryRow(panel, 6, "Hora inicio:", summaryStartLabel);
        addSummaryRow(panel, 7, "Hora finalización:", summaryEndLabel);
        panel.setPreferredSize(new Dimension(280, 292));
        return panel;
    }

    private JPanel buildNotesSection() {
        JPanel panel = cardPanel();
        panel.setLayout(new BorderLayout(0, 8));
        panel.setBorder(cardBorder());
        JLabel title = sectionTitle("Observaciones");
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setFont(UiTheme.BODY_FONT);
        notesArea.setRows(3);
        JScrollPane scroll = new JScrollPane(notesArea);
        scroll.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER_COLOR));
        panel.add(title, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildActions() {
        JPanel actions = new JPanel(new GridBagLayout());
        actions.setBackground(UiTheme.BG_PAGE);
        actions.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UiTheme.BORDER_COLOR),
                new EmptyBorder(12, 16, 12, 16)));
        JButton cancel = new JButton("Cancelar");
        JButton save = new JButton("Guardar Agenda");
        JButton finish = new JButton("Finalizar Venta");
        UiTheme.styleButton(cancel, UiTheme.BTN_DARK, UiTheme.BTN_DARK_HOVER, UiTheme.TEXT_LIGHT, 160, 38, 13,
                true, false, 8);
        UiTheme.styleButton(save, UiTheme.FOREST_GREEN, UiTheme.FOREST_GREEN_HOVER, UiTheme.TEXT_LIGHT, 160, 38,
                13, true, false, 8);
        UiTheme.styleButton(finish, UiTheme.FOREST_GREEN, UiTheme.FOREST_GREEN_HOVER, UiTheme.TEXT_LIGHT, 160, 38,
                13, true, false, 8);
        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> saveAppointment(false));
        finish.addActionListener(e -> saveAppointment(true));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets(0, 0, 0, 10);
        gbc.gridx = 0;
        actions.add(cancel, gbc);
        gbc.gridx = 1;
        actions.add(save, gbc);
        gbc.gridx = 2;
        gbc.insets = new Insets(0, 0, 0, 0);
        actions.add(finish, gbc);
        return actions;
    }

    private JPanel section(String title, String[] labels, Component[] components) {
        JPanel panel = cardPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(cardBorder());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 0, 4, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = labels.length;
        panel.add(sectionTitle(title), gbc);
        gbc.gridwidth = 1;
        for (int i = 0; i < labels.length; i++) {
            gbc.gridy = 1;
            gbc.gridx = i;
            gbc.weightx = 1;
            gbc.insets = new Insets(8, i == 0 ? 0 : 8, 2, 0);
            JLabel label = fieldLabel(labels[i]);
            panel.add(label, gbc);
            gbc.gridy = 2;
            gbc.insets = new Insets(0, i == 0 ? 0 : 8, 0, 0);
            panel.add(components[i], gbc);
        }
        return panel;
    }


    private JPanel cardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(UiTheme.PANEL_WHITE);
        return panel;
    }

    private javax.swing.border.Border cardBorder() {
        return BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiTheme.BORDER_COLOR),
                new EmptyBorder(12, 14, 14, 14));
    }

    private javax.swing.border.Border accentCardBorder() {
        return BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, UiTheme.FOREST_GREEN),
                cardBorder());
    }

    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UiTheme.TITLE_FONT_SECTION.deriveFont(java.awt.Font.BOLD, 15f));
        label.setForeground(UiTheme.TEXT_PRIMARY);
        return label;
    }

    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UiTheme.BODY_FONT.deriveFont(12f));
        label.setForeground(UiTheme.TEXT_SECONDARY);
        return label;
    }

    private JPanel metric(String label, Component value) {
        JPanel panel = new JPanel(new BorderLayout(0, 3));
        panel.setOpaque(false);
        panel.add(fieldLabel(label), BorderLayout.NORTH);
        panel.add(value, BorderLayout.CENTER);
        return panel;
    }

    private void addCardHeader(JPanel panel, String title, JButton action) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(sectionTitle(title), BorderLayout.WEST);
        header.add(action, BorderLayout.EAST);
        panel.add(header, gbc);
    }

    private void addFullField(JPanel panel, int row, String label, Component field) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 0, 4, 0);
        panel.add(fieldLabel(label), gbc);
        gbc.gridy = row + 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(field, gbc);
    }

    private void addHalfFields(JPanel panel, int row, String label1, Component field1, String label2, Component field2) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.insets = new Insets(12, 0, 4, 8);
        panel.add(fieldLabel(label1), gbc);
        gbc.gridx = 1;
        gbc.insets = new Insets(12, 8, 4, 0);
        panel.add(fieldLabel(label2), gbc);
        gbc.gridy = row + 1;
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 0, 8);
        panel.add(field1, gbc);
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 8, 0, 0);
        panel.add(field2, gbc);
    }

    private void addThirdFields(JPanel panel, int row, String label1, Component field1, String label2,
            Component field2, String label3, Component field3) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.insets = new Insets(12, 0, 4, 8);
        panel.add(fieldLabel(label1), gbc);
        gbc.gridx = 1;
        gbc.insets = new Insets(12, 8, 4, 8);
        panel.add(fieldLabel(label2), gbc);
        gbc.gridx = 2;
        gbc.insets = new Insets(12, 8, 4, 0);
        panel.add(fieldLabel(label3), gbc);
        gbc.gridy = row + 1;
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 0, 8);
        panel.add(field1, gbc);
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 8, 0, 8);
        panel.add(field2, gbc);
        gbc.gridx = 2;
        gbc.insets = new Insets(0, 8, 0, 0);
        panel.add(field3, gbc);
    }

    private void addSummaryRow(JPanel panel, int row, String label, JLabel value) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.insets = new Insets(10, 0, 0, 12);
        JLabel left = fieldLabel(label);
        left.setFont(UiTheme.BODY_FONT.deriveFont(java.awt.Font.BOLD, 13f));
        panel.add(left, gbc);
        value.setFont(UiTheme.BODY_FONT.deriveFont(13f));
        value.setForeground(UiTheme.TEXT_PRIMARY);
        value.setHorizontalAlignment(JLabel.RIGHT);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.insets = new Insets(10, 0, 0, 0);
        panel.add(value, gbc);
    }

    private JLabel valueLabel(String value) {
        JLabel label = new JLabel(value);
        styleInfoLabel(label);
        return label;
    }

    private void configureInputs() {
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        styleField(serviceField, false);
        styleInfoLabel(endTimeField);
        styleField(phoneField, false);
        styleField(emailField, false);
        styleField(speciesField, false);
        styleField(breedField, false);
        styleField(ageField, false);
        styleSpinner(dateSpinner);
        styleSpinner(hourSpinner);
        styleSpinner(minuteSpinner);
        styleSpinner(durationSpinner);
        availabilityLabel.setFont(UiTheme.BODY_FONT);
        availabilityLabel.setForeground(UiTheme.TEXT_SECONDARY);
    }

    private void wireEvents() {
        ownerCombo.addItemListener(e -> {
            if (!suppressOwnerEvents && e.getStateChange() == ItemEvent.SELECTED
                    && e.getItem() instanceof Owner owner) {
                selectOwner(owner);
            }
        });
        Component editor = ownerCombo.getEditor().getEditorComponent();
        if (editor instanceof JTextField textField) {
            ownerEditorField = textField;
            ownerSearchTimer = new Timer(250, e -> searchOwnersAsync(textField.getText(), true));
            ownerSearchTimer.setRepeats(false);
            textField.getDocument().addDocumentListener(documentListener(this::scheduleOwnerSearch));
        }
        petCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof Pet pet) {
                syncPetDetails(pet);
            }
        });
        ChangeListener agendaListener = (ChangeEvent e) -> {
            updateEndTime();
            checkAvailabilityAsync();
        };
        dateSpinner.addChangeListener(agendaListener);
        hourSpinner.addChangeListener(agendaListener);
        minuteSpinner.addChangeListener(agendaListener);
        durationSpinner.addChangeListener(agendaListener);
    }

    private void scheduleOwnerSearch() {
        if (suppressOwnerEvents || ownerSearchTimer == null) {
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
                return ownerService.searchOwners(normalizedQuery).stream().limit(10).toList();
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
                } catch (ExecutionException ex) {
                    showError("No se pudieron cargar clientes: " + ex.getMessage());
                }
            }
        };
        ownerSearchWorker.execute();
    }

    private void updateOwnerSuggestions(List<Owner> owners, String editorText, boolean showPopup) {
        suppressOwnerEvents = true;
        try {
            DefaultComboBoxModel<Owner> model = new DefaultComboBoxModel<>();
            owners.forEach(model::addElement);
            ownerCombo.setModel(model);
            ownerCombo.getEditor().setItem(editorText == null ? "" : editorText);
        } finally {
            suppressOwnerEvents = false;
        }
        if (showPopup && ownerCombo.isShowing() && ownerEditorField != null && ownerEditorField.hasFocus()
                && !owners.isEmpty() && editorText != null && !editorText.isBlank()) {
            ownerCombo.setPopupVisible(true);
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
        selectedOwner = owner;
        suppressOwnerEvents = true;
        try {
            ownerCombo.getEditor().setItem(owner.getId() + " - " + owner.getNombreCompleto());
            ownerCombo.setSelectedItem(owner);
        } finally {
            suppressOwnerEvents = false;
        }
        phoneField.setText(empty(owner.getTelefono()));
        emailField.setText(empty(owner.getCorreo()));
        summaryOwnerLabel.setText(owner.getNombreCompleto());
        loadPetsAsync(owner.getId());
    }

    private void loadPetsAsync(int ownerId) {
        if (petSearchWorker != null && !petSearchWorker.isDone()) {
            petSearchWorker.cancel(true);
        }
        petCombo.setModel(new DefaultComboBoxModel<>());
        clearPetDetails();
        petSearchWorker = new SwingWorker<>() {
            @Override
            protected List<Pet> doInBackground() {
                return petService.findPetsByOwner(ownerId);
            }

            @Override
            protected void done() {
                if (isCancelled()) {
                    return;
                }
                try {
                    DefaultComboBoxModel<Pet> model = new DefaultComboBoxModel<>();
                    get().forEach(model::addElement);
                    petCombo.setModel(model);
                    if (model.getSize() > 0) {
                        petCombo.setSelectedIndex(0);
                        syncPetDetails(model.getElementAt(0));
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    showError("No se pudieron cargar mascotas: " + ex.getMessage());
                }
            }
        };
        petSearchWorker.execute();
    }

    private void createOwnerInline() {
        JTextField name = new JTextField();
        JTextField phone = new JTextField();
        JTextField email = new JTextField();
        JTextField address = new JTextField();
        Object[] fields = {"Nombre", name, "Teléfono", phone, "Correo", email, "Dirección", address};
        int option = JOptionPane.showConfirmDialog(this, fields, "Nuevo cliente", JOptionPane.OK_CANCEL_OPTION);
        if (option != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            Owner owner = new Owner(0, name.getText().trim(), phone.getText().trim(), email.getText().trim(),
                    address.getText().trim());
            Owner created = ownerService.createOwner(owner);
            searchOwnersAsync(created.getNombreCompleto(), false);
            selectOwner(created);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cliente", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void createPetInline() {
        Owner owner;
        try {
            owner = selectedOwner();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Mascota", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JTextField name = new JTextField();
        JTextField breed = new JTextField();
        JTextField age = new JTextField();
        JTextField weight = new JTextField("1");
        JComboBox<String> sex = new JComboBox<>(new String[]{"Macho", "Hembra"});
        Object[] fields = {"Nombre", name, "Raza", breed, "Edad", age, "Peso", weight, "Sexo", sex};
        int option = JOptionPane.showConfirmDialog(this, fields, "Nueva mascota", JOptionPane.OK_CANCEL_OPTION);
        if (option != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            Pet pet = new Pet();
            pet.setOwnerId(owner.getId());
            pet.setNombreMascota(name.getText().trim());
            pet.setRaza(breed.getText().trim());
            pet.setEdad(age.getText().trim());
            pet.setPeso(Double.parseDouble(weight.getText().trim().replace(',', '.')));
            pet.setSexo(String.valueOf(sex.getSelectedItem()));
            petService.registerPet(pet);
            loadPetsAsync(owner.getId());
        } catch (IOException | NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Mascota", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void saveAppointment(boolean finalizingSale) {
        try {
            Owner owner = selectedOwner();
            Pet pet = selectedPet();
            LocalDate date = selectedDate();
            LocalTime start = selectedTime();
            LocalTime end = start.plusMinutes(selectedDurationMinutes());
            if (agendaService.hasConflict(date, start, end)) {
                showConflictSuggestions(date);
                return;
            }
            CitaServicio cita = new CitaServicio(null, owner.getNombreCompleto(), pet.getNombreMascota(),
                    empty(pet.getRaza()), required(phoneField, "Teléfono"), required(serviceField, "Servicio"),
                    date, start, end, notesArea.getText().trim(), "AGENDADA");
            agendaService.registrarCita(cita);
            saved = true;
            finalizeSale = finalizingSale;
            JOptionPane.showMessageDialog(this, "Agenda guardada correctamente.");
            dispose();
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Revisa el formato de fecha u hora.", "Datos inválidos",
                    JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Datos incompletos", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            showError("No se pudo registrar la agenda: " + ex.getMessage());
        }
    }

    private void checkAvailabilityAsync() {
        LocalDate date;
        LocalTime start;
        LocalTime end;
        try {
            date = selectedDate();
            start = selectedTime();
            end = start.plusMinutes(selectedDurationMinutes());
        } catch (RuntimeException ex) {
            availabilityLabel.setText("Revisa fecha y hora");
            availabilityLabel.setForeground(Color.RED.darker());
            return;
        }
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return agendaService.hasConflict(date, start, end);
            }

            @Override
            protected void done() {
                try {
                    boolean conflict = get();
                    availabilityLabel.setText(conflict ? "Conflicto detectado" : "Disponible");
                    availabilityLabel.setForeground(conflict ? Color.RED.darker() : UiTheme.FOREST_GREEN);
                } catch (InterruptedException | ExecutionException ex) {
                    availabilityLabel.setText("No verificada");
                    availabilityLabel.setForeground(UiTheme.TEXT_SECONDARY);
                }
            }
        }.execute();
    }

    private void showConflictSuggestions(LocalDate date) throws SQLException {
        List<LocalTime> suggestions = agendaService.suggestAvailableTimes(date, selectedDurationMinutes(), 6);
        String message = "Ya existe una cita activa en ese horario.";
        if (!suggestions.isEmpty()) {
            message += "\nHorarios disponibles: " + suggestions;
        }
        JOptionPane.showMessageDialog(this, message, "Conflicto de agenda", JOptionPane.WARNING_MESSAGE);
    }

    private void updateEndTime() {
        try {
            endTimeField.setText(selectedTime().plusMinutes(selectedDurationMinutes()).toString());
        } catch (RuntimeException ex) {
            endTimeField.setText("");
        }
        updateSummaryVisual();
    }

    private Owner selectedOwner() {
        if (selectedOwner != null) {
            return selectedOwner;
        }
        Object selected = ownerCombo.getSelectedItem();
        if (selected instanceof Owner owner) {
            return owner;
        }
        throw new IllegalArgumentException("Selecciona un cliente registrado desde las sugerencias.");
    }

    private Pet selectedPet() {
        Object selected = petCombo.getSelectedItem();
        if (selected instanceof Pet pet) {
            return pet;
        }
        throw new IllegalArgumentException("Selecciona o crea una mascota para el cliente.");
    }

    private void syncPetDetails(Pet pet) {
        speciesField.setText("Canino");
        breedField.setText(empty(pet.getRaza()));
        ageField.setText(empty(pet.getEdad()));
        summaryPetLabel.setText(empty(pet.getNombreMascota()));
        updateSummaryVisual();
    }

    private void clearOwnerDetails() {
        phoneField.setText("");
        emailField.setText("");
        petCombo.setModel(new DefaultComboBoxModel<>());
        summaryOwnerLabel.setText("-");
        clearPetDetails();
    }

    private void clearPetDetails() {
        speciesField.setText("Canino");
        breedField.setText("");
        ageField.setText("");
        summaryPetLabel.setText("-");
    }

    private void updateSummaryVisual() {
        summaryServiceLabel.setText(empty(serviceField.getText()));
        summaryDurationLabel.setText(selectedDurationMinutes() + " minutos");
        try {
            summaryDateLabel.setText(selectedDate().toString());
            summaryStartLabel.setText(selectedTime().toString());
        } catch (RuntimeException ex) {
            summaryDateLabel.setText("-");
            summaryStartLabel.setText("-");
        }
        summaryEndLabel.setText(endTimeField.getText().isBlank() ? "-" : endTimeField.getText());
    }

    private LocalDate selectedDate() {
        Date selected = (Date) dateSpinner.getValue();
        LocalDate date = selected.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("No se permiten fechas pasadas para la cita.");
        }
        return date;
    }

    private LocalTime selectedTime() {
        int hour = ((Number) hourSpinner.getValue()).intValue();
        int minute = ((Number) minuteSpinner.getValue()).intValue();
        LocalTime time = LocalTime.of(hour, minute);
        if (LocalDateTime.of(selectedDate(), time).isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("No se permiten horas pasadas para la cita.");
        }
        return time;
    }

    private int selectedDurationMinutes() {
        return ((Number) durationSpinner.getValue()).intValue();
    }

    private String required(JTextField field, String label) {
        String value = field.getText().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Completa el campo: " + label);
        }
        return value;
    }

    private JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        UiTheme.styleButton(button, UiTheme.BTN_DARK, UiTheme.BTN_DARK_HOVER, UiTheme.TEXT_LIGHT, 132, 32, 12,
                true, false, 8);
        return button;
    }

    private void styleField(JTextField field, boolean editable) {
        field.setEditable(editable);
        field.setFont(UiTheme.BODY_FONT);
        field.setBackground(editable ? Color.WHITE : new Color(248, 250, 252));
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiTheme.BORDER_COLOR),
                new EmptyBorder(6, 9, 6, 9)));
    }

    private void styleInfoLabel(JLabel label) {
        label.setOpaque(true);
        label.setFont(UiTheme.BODY_FONT.deriveFont(java.awt.Font.BOLD, 14f));
        label.setForeground(UiTheme.TEXT_PRIMARY);
        label.setBackground(new Color(248, 250, 252));
        label.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiTheme.BORDER_COLOR),
                new EmptyBorder(6, 10, 6, 10)));
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setFont(UiTheme.BODY_FONT);
        Component editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor defaultEditor) {
            defaultEditor.getTextField().setEditable(false);
            defaultEditor.getTextField().setFont(UiTheme.BODY_FONT);
        }
    }

    private DocumentListener documentListener(Runnable action) {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                action.run();
            }
        };
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
    }

    private String empty(String value) {
        return value == null ? "" : value;
    }

    private static LocalTime nextBookableTime() {
        LocalTime candidate = LocalTime.now().plusHours(1);
        int roundedMinute = ((candidate.getMinute() + 4) / 5) * 5;
        if (roundedMinute >= 60) {
            candidate = candidate.plusHours(1);
            roundedMinute = 0;
        }
        return candidate.withMinute(roundedMinute).withSecond(0).withNano(0);
    }

    private static final class OwnerRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Owner owner) {
                setText(owner.getId() + " - " + owner.getNombreCompleto() + " - " + empty(owner.getTelefono()));
            }
            return this;
        }

        private String empty(String value) {
            return value == null ? "" : value;
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