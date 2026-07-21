package secureauth.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import secureauth.application.dto.AppointmentDTO;
import secureauth.application.dto.SaleItemDTO;
import secureauth.application.dto.ServiceOrderDTO;
import secureauth.application.dto.ServiceOrderItemDTO;
import secureauth.application.dto.ServiceProductDTO;
import secureauth.application.mapper.AppointmentMapper;
import secureauth.application.mapper.SaleMapper;
import secureauth.application.usecase.CreateServiceOrderUseCase;
import secureauth.domain.services.ServiceSummary;
import secureauth.model.Appointment;
import secureauth.model.AppointmentStatus;
import secureauth.model.Owner;
import secureauth.model.Pet;
import secureauth.model.SaleItem;
import secureauth.model.enterprise.InventoryItem;
import secureauth.service.OwnerService;
import secureauth.service.enterprise.AppointmentService;
import secureauth.service.enterprise.InventoryService;
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

    private final SaleItemDTO saleItem;
    private final AppointmentService appointmentService;
    private final OwnerService ownerService;
    private final InventoryService inventoryService;
    private final CreateServiceOrderUseCase createServiceOrderUseCase = new CreateServiceOrderUseCase();
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.of("es", "CO"));

    private final JComboBox<Owner> ownerCombo = new JComboBox<>();
    private final JComboBox<Pet> petCombo = new JComboBox<>();
    private final JTextField documentField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JTextField speciesField = new JTextField();
    private final JTextField breedField = new JTextField();
    private final JTextField ageField = new JTextField();
    private final JTextField veterinarianField = new JTextField("Sistema");
    private final JSpinner dateSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null,
            Calendar.DAY_OF_MONTH));
    private final JSpinner hourSpinner = new JSpinner(new SpinnerNumberModel(nextBookableTime().getHour(), 0, 23, 1));
    private final JSpinner minuteSpinner = new JSpinner(new SpinnerNumberModel(nextBookableTime().getMinute(), 0, 59,
            5));
    private final JSpinner durationSpinner = new JSpinner(new SpinnerNumberModel(ESTIMATED_DURATION_MINUTES, 1, 480,
            5));
    private final JTextField discountField = new JTextField("0");
    private final JTextArea notesArea = new JTextArea(4, 24);
    private final JLabel endTimeLabel = new JLabel();
    private final JLabel summaryOwnerLabel = new JLabel("-");
    private final JLabel summaryPetLabel = new JLabel("-");
    private final JLabel summaryServiceLabel = new JLabel("-");
    private final JLabel summaryDurationLabel = new JLabel("-");
    private final JLabel summaryDateLabel = new JLabel("-");
    private final JLabel summaryStartLabel = new JLabel("-");
    private final JLabel summaryEndLabel = new JLabel("-");
    private final DefaultTableModel productsModel = new DefaultTableModel(
            new String[]{"Producto", "Cantidad", "Precio unitario", "Subtotal"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable productsTable = new JTable(productsModel);
    private final List<ServiceProductDTO> usedProducts = new ArrayList<>();
    private final List<ServiceProductDTO> suggestedProducts = new ArrayList<>();
    private JTextField serviceAmountField;
    private JTextField productsAmountField;
    private JTextField subtotalField;
    private JTextField taxField;
    private JTextField totalField;
    private JTextField ownerEditorField;
    private Timer ownerSearchTimer;
    private SwingWorker<List<Owner>, Void> ownerSearchWorker;
    private SwingWorker<List<Pet>, Void> petSearchWorker;
    private Owner selectedOwner;
    private AppointmentDTO preparedAppointment;
    private ServiceOrderDTO preparedServiceOrder;
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
        this(owner, SaleMapper.toDTO(saleItem), appointmentService, new OwnerService(new secureauth.dao.OwnerDAO()));
    }

    public ServiceAppointmentDialog(java.awt.Window owner, SaleItem saleItem, AppointmentService appointmentService,
            OwnerService ownerService) {
        this(owner, SaleMapper.toDTO(saleItem), appointmentService, ownerService);
    }

    public ServiceAppointmentDialog(java.awt.Window owner, SaleItemDTO saleItem, AppointmentService appointmentService,
            OwnerService ownerService) {
        this(owner, saleItem, appointmentService, ownerService, new InventoryService());
    }

    public ServiceAppointmentDialog(java.awt.Window owner, SaleItemDTO saleItem, AppointmentService appointmentService,
            OwnerService ownerService, InventoryService inventoryService) {
        super(owner, "Agendar servicio veterinario", ModalityType.APPLICATION_MODAL);
        this.saleItem = saleItem;
        this.appointmentService = appointmentService;
        this.ownerService = ownerService;
        this.inventoryService = inventoryService;
        build();
        wireAutocomplete();
        searchOwnersAsync("", false);
        loadSuggestedProducts();
        updateSummary();
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
    public AppointmentDTO getPreparedAppointment() {
        return preparedAppointment;
    }

    public ServiceOrderDTO getPreparedServiceOrder() {
        return preparedServiceOrder;
    }

    private void build() {
        setLayout(new BorderLayout(0, 0));
        getContentPane().setBackground(new Color(7, 21, 40));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);
        setPreferredSize(new Dimension(1180, 760));
        pack();
        setLocationRelativeTo(getOwner());
    }

    private JPanel buildHeader() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 0));
        wrapper.setBackground(new Color(7, 21, 40));

        JLabel title = new JLabel("  Agendar servicio vendido");
        title.setFont(UiTheme.TITLE_FONT_SECTION.deriveFont(java.awt.Font.BOLD, 18f));
        title.setForeground(UiTheme.TEXT_LIGHT);
        title.setBorder(new EmptyBorder(12, 14, 12, 14));
        wrapper.add(title, BorderLayout.NORTH);

        JPanel header = new JPanel(new GridBagLayout());
        header.setBackground(UiTheme.PANEL_WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiTheme.BORDER_COLOR),
                new EmptyBorder(18, 22, 18, 22)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 0, 24);

        JLabel serviceBadge = new JLabel("Servicio");
        serviceBadge.setOpaque(true);
        serviceBadge.setHorizontalAlignment(SwingConstants.CENTER);
        serviceBadge.setForeground(UiTheme.TEXT_LIGHT);
        serviceBadge.setBackground(UiTheme.FOREST_GREEN);
        serviceBadge.setFont(UiTheme.BODY_FONT.deriveFont(java.awt.Font.BOLD, 14f));
        serviceBadge.setPreferredSize(new Dimension(70, 70));
        gbc.gridx = 0;
        gbc.weightx = 0;
        header.add(serviceBadge, gbc);

        JPanel nameBlock = new JPanel(new BorderLayout(0, 4));
        nameBlock.setOpaque(false);
        JLabel serviceTitle = new JLabel("Servicio: " + saleItem.name());
        serviceTitle.setFont(UiTheme.TITLE_FONT_SECTION.deriveFont(java.awt.Font.BOLD, 20f));
        serviceTitle.setForeground(UiTheme.TEXT_PRIMARY);
        JLabel sold = new JLabel("VENDIDO");
        sold.setFont(UiTheme.BODY_FONT.deriveFont(java.awt.Font.BOLD, 11f));
        sold.setForeground(UiTheme.FOREST_GREEN);
        nameBlock.add(serviceTitle, BorderLayout.NORTH);
        nameBlock.add(sold, BorderLayout.CENTER);
        gbc.gridx = 1;
        gbc.weightx = 0.35;
        header.add(nameBlock, gbc);

        JPanel facts = new JPanel(new GridBagLayout());
        facts.setOpaque(false);
        GridBagConstraints factGbc = new GridBagConstraints();
        factGbc.fill = GridBagConstraints.HORIZONTAL;
        factGbc.weightx = 1;
        factGbc.insets = new Insets(0, 0, 0, 24);
        factGbc.gridx = 0;
        facts.add(infoBlock("Precio", currency.format(saleItem.price())), factGbc);
        factGbc.gridx = 1;
        facts.add(infoBlock("Duración", ESTIMATED_DURATION_MINUTES + " minutos"), factGbc);
        factGbc.gridx = 2;
        factGbc.insets = new Insets(0, 0, 0, 0);
        facts.add(infoBlock("Fecha de venta", LocalDateTime.now().toLocalDate().toString()), factGbc);
        gbc.gridx = 2;
        gbc.weightx = 1;
        header.add(facts, gbc);

        JPanel total = new JPanel(new BorderLayout(0, 4));
        total.setBackground(new Color(237, 250, 242));
        total.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(190, 225, 205)),
                new EmptyBorder(12, 24, 12, 24)));
        JLabel totalTitle = smallLabel("Total a pagar");
        totalTitle.setHorizontalAlignment(SwingConstants.CENTER);
        JLabel totalValue = new JLabel(currency.format(saleItem.price()));
        totalValue.setHorizontalAlignment(SwingConstants.CENTER);
        totalValue.setFont(UiTheme.TITLE_FONT_SECTION.deriveFont(java.awt.Font.BOLD, 20f));
        totalValue.setForeground(UiTheme.FOREST_GREEN);
        total.add(totalTitle, BorderLayout.NORTH);
        total.add(totalValue, BorderLayout.CENTER);
        gbc.gridx = 3;
        gbc.weightx = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        header.add(total, gbc);

        JPanel inset = new JPanel(new BorderLayout());
        inset.setOpaque(false);
        inset.setBorder(new EmptyBorder(0, 12, 10, 12));
        inset.add(header, BorderLayout.CENTER);
        wrapper.add(inset, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildForm() {
        ownerCombo.setEditable(true);
        ownerCombo.setRenderer(new OwnerRenderer());
        petCombo.setRenderer(new PetRenderer());
        documentField.setEditable(true);
        phoneField.setEditable(false);
        emailField.setEditable(false);
        speciesField.setEditable(false);
        breedField.setEditable(false);
        ageField.setEditable(false);
        styleField(phoneField);
        styleField(emailField);
        styleField(documentField);
        styleField(speciesField);
        styleField(breedField);
        styleField(ageField);
        styleField(veterinarianField);
        styleSpinner(dateSpinner);
        styleSpinner(hourSpinner);
        styleSpinner(minuteSpinner);
        styleSpinner(durationSpinner);
        styleField(discountField);
        dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setFont(UiTheme.BODY_FONT);
        notesArea.setRows(3);
        styleInfoLabel(endTimeLabel);
        updateEndTimeLabel();

        documentField.addActionListener(e -> searchOwnersAsync(documentField.getText(), true));
        petCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED && e.getItem() instanceof Pet pet) {
                syncPetDetails(pet);
            }
        });

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UiTheme.BG_PAGE);
        form.setBorder(new EmptyBorder(4, 12, 10, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.insets = new Insets(0, 0, 10, 8);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        form.add(buildClientCard(), gbc);
        gbc.gridx = 2;
        gbc.insets = new Insets(0, 8, 10, 0);
        form.add(buildPetCard(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.insets = new Insets(0, 0, 10, 8);
        form.add(buildScheduleCard(), gbc);
        gbc.gridx = 3;
        gbc.gridwidth = 1;
        gbc.gridheight = 3;
        gbc.weightx = 0.35;
        gbc.insets = new Insets(0, 8, 10, 0);
        form.add(buildAppointmentSummaryCard(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.gridheight = 1;
        gbc.weightx = 1;
        gbc.insets = new Insets(0, 0, 10, 8);
        form.add(buildNotesCard(), gbc);

        gbc.gridy = 3;
        form.add(buildProductsSection(), gbc);
        discountField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateSummary();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateSummary();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateSummary();
            }
        });
        refreshAppointmentSummary();
        return form;
    }

    private JPanel buildActions() {
        JPanel actions = new JPanel(new GridLayout(1, 3, 18, 0));
        actions.setBackground(UiTheme.BG_PAGE);
        actions.setBorder(new EmptyBorder(0, 22, 12, 22));
        JButton cancel = new JButton("Cancelar");
        JButton save = new JButton("Guardar agenda");
        JButton register = new JButton("Finalizar venta");
        UiTheme.styleButton(cancel, UiTheme.BTN_DARK, UiTheme.BTN_DARK_HOVER, UiTheme.TEXT_LIGHT, 180, 42, 13,
                true, false, 8);
        UiTheme.styleButton(save, UiTheme.BTN_DARK, UiTheme.BTN_DARK_HOVER, UiTheme.TEXT_LIGHT, 180, 42,
                13, true, false, 8);
        UiTheme.styleButton(register, UiTheme.FOREST_GREEN, UiTheme.FOREST_GREEN_HOVER, UiTheme.TEXT_LIGHT, 180, 42,
                13, true, false, 8);
        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> saveAppointment());
        register.addActionListener(e -> saveAppointment());
        actions.add(cancel);
        actions.add(save);
        actions.add(register);
        return actions;
    }

    private JPanel buildClientCard() {
        JPanel panel = cardPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(accentCardBorder());
        addHeader(panel, "Cliente");
        addFullField(panel, 1, "Buscar cliente (Documento, nombre o teléfono)", documentField);
        addHalfFields(panel, 3, "Nombre", ownerCombo, "Teléfono", phoneField);
        addFullField(panel, 5, "Correo", emailField);
        return panel;
    }

    private JPanel buildPetCard() {
        JPanel panel = cardPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(cardBorder());
        addHeader(panel, "Mascota");
        addFullField(panel, 1, "Seleccionar mascota", petCombo);
        addThirdFields(panel, 3, "Especie", speciesField, "Raza", breedField, "Edad", ageField);
        return panel;
    }

    private JPanel buildScheduleCard() {
        JPanel panel = cardPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(cardBorder());
        addHeader(panel, "Agenda");
        addFourthFields(panel, 1, "Fecha", dateSpinner, "Hora de inicio", hourSpinner, "Duración",
                durationSpinner, "Hora de finalización", endTimeLabel);
        return panel;
    }

    private JPanel buildNotesCard() {
        JPanel panel = cardPanel();
        panel.setLayout(new BorderLayout(0, 8));
        panel.setBorder(cardBorder());
        panel.add(sectionTitle("Observaciones"), BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(notesArea);
        scroll.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER_COLOR));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildAppointmentSummaryCard() {
        serviceAmountField = readOnlyField(currency.format(saleItem.price()));
        productsAmountField = readOnlyField(currency.format(0));
        subtotalField = readOnlyField(currency.format(0));
        taxField = readOnlyField(currency.format(0));
        totalField = readOnlyField(currency.format(0));

        JPanel panel = cardPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBorder(accentCardBorder());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(sectionTitle("Resumen de la cita"), gbc);
        addSummaryRow(panel, 1, "Cliente:", summaryOwnerLabel);
        addSummaryRow(panel, 2, "Mascota:", summaryPetLabel);
        addSummaryRow(panel, 3, "Servicio:", summaryServiceLabel);
        addSummaryRow(panel, 4, "Duración:", summaryDurationLabel);
        addSummaryRow(panel, 5, "Fecha:", summaryDateLabel);
        addSummaryRow(panel, 6, "Hora inicio:", summaryStartLabel);
        addSummaryRow(panel, 7, "Hora finalización:", summaryEndLabel);
        addDivider(panel, 8);
        addFinancialRow(panel, 9, "Servicio", serviceAmountField);
        addFinancialRow(panel, 10, "Productos", productsAmountField);
        addFinancialRow(panel, 11, "Subtotal", subtotalField);
        addFinancialRow(panel, 12, "IVA", taxField);
        addFinancialRow(panel, 13, "Descuento", discountField);
        addFinancialRow(panel, 14, "Total", totalField);
        return panel;
    }

    private JPanel cardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(UiTheme.PANEL_WHITE);
        return panel;
    }

    private javax.swing.border.Border cardBorder() {
        return BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiTheme.BORDER_COLOR),
                new EmptyBorder(14, 16, 16, 16));
    }

    private javax.swing.border.Border accentCardBorder() {
        return BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, UiTheme.FOREST_GREEN),
                cardBorder());
    }

    private JLabel sectionTitle(String text) {
        JLabel title = new JLabel(text);
        title.setFont(UiTheme.TITLE_FONT_SECTION.deriveFont(java.awt.Font.BOLD, 16f));
        title.setForeground(UiTheme.TEXT_PRIMARY);
        return title;
    }

    private JLabel smallLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UiTheme.BODY_FONT.deriveFont(12f));
        label.setForeground(UiTheme.TEXT_SECONDARY);
        return label;
    }

    private JPanel infoBlock(String label, String value) {
        JPanel item = new JPanel(new BorderLayout(0, 4));
        item.setOpaque(false);
        JLabel title = smallLabel(label);
        JLabel val = new JLabel(value);
        val.setFont(UiTheme.BODY_FONT.deriveFont(java.awt.Font.BOLD, 15f));
        val.setForeground(label.equals("Precio") ? UiTheme.FOREST_GREEN : UiTheme.TEXT_PRIMARY);
        item.add(title, BorderLayout.NORTH);
        item.add(val, BorderLayout.CENTER);
        return item;
    }

    private void addHeader(JPanel panel, String title) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        panel.add(sectionTitle(title), gbc);
    }

    private void addFullField(JPanel panel, int row, String label, Component field) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 4;
        gbc.insets = new Insets(12, 0, 4, 0);
        panel.add(smallLabel(label), gbc);
        gbc.gridy = row + 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        panel.add(field, gbc);
    }

    private void addHalfFields(JPanel panel, int row, String label1, Component field1, String label2,
            Component field2) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 0, 4, 8);
        panel.add(smallLabel(label1), gbc);
        gbc.gridx = 2;
        gbc.insets = new Insets(12, 8, 4, 0);
        panel.add(smallLabel(label2), gbc);
        gbc.gridy = row + 1;
        gbc.gridx = 0;
        gbc.insets = new Insets(0, 0, 0, 8);
        panel.add(field1, gbc);
        gbc.gridx = 2;
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
        panel.add(smallLabel(label1), gbc);
        gbc.gridx = 1;
        gbc.insets = new Insets(12, 8, 4, 8);
        panel.add(smallLabel(label2), gbc);
        gbc.gridx = 2;
        gbc.insets = new Insets(12, 8, 4, 0);
        panel.add(smallLabel(label3), gbc);
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

    private void addFourthFields(JPanel panel, int row, String label1, Component field1, String label2,
            Component field2, String label3, Component field3, String label4, Component field4) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        String[] labels = {label1, label2, label3, label4};
        Component[] fields = {field1, field2, field3, field4};
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = i;
            gbc.gridy = row;
            gbc.insets = new Insets(14, i == 0 ? 0 : 10, 4, 0);
            panel.add(smallLabel(labels[i]), gbc);
            gbc.gridy = row + 1;
            gbc.insets = new Insets(0, i == 0 ? 0 : 10, 0, 0);
            panel.add(fields[i], gbc);
        }
    }

    private void addSummaryRow(JPanel panel, int row, String label, JLabel value) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.insets = new Insets(10, 0, 0, 12);
        JLabel left = smallLabel(label);
        left.setFont(UiTheme.BODY_FONT.deriveFont(java.awt.Font.BOLD, 13f));
        panel.add(left, gbc);
        value.setFont(UiTheme.BODY_FONT.deriveFont(13f));
        value.setForeground(UiTheme.TEXT_PRIMARY);
        value.setHorizontalAlignment(SwingConstants.RIGHT);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.insets = new Insets(10, 0, 0, 0);
        panel.add(value, gbc);
    }

    private void addDivider(JPanel panel, int row) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(14, 0, 6, 0);
        JPanel line = new JPanel();
        line.setPreferredSize(new Dimension(1, 1));
        line.setBackground(UiTheme.BORDER_COLOR);
        panel.add(line, gbc);
    }

    private void addFinancialRow(JPanel panel, int row, String label, Component value) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.insets = new Insets(6, 0, 0, 10);
        panel.add(smallLabel(label), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.insets = new Insets(6, 0, 0, 0);
        panel.add(value, gbc);
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
        documentField.setText(String.valueOf(owner.getId()));
        summaryOwnerLabel.setText(owner.getNombreCompleto());
        refreshAppointmentSummary();
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
                        syncPetDetails(petsModel.getElementAt(0));
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
        speciesField.setText("");
        breedField.setText("");
        ageField.setText("");
    }

    private void saveAppointment() {
        try {
            Owner owner = selectedOwner();
            Pet pet = selectedPet();
            LocalDate date = selectedDate();
            LocalTime time = selectedTime();
            Appointment appointment = new Appointment(null, saleItem.catalogItemId(), saleItem.name(),
                    owner.getId(), owner.getNombreCompleto(), pet.getId(), pet.getNombreMascota(), date, time,
                    AppointmentStatus.PENDING.databaseValue(), notesArea.getText().trim(), LocalDateTime.now(),
                    veterinarianField.getText().isBlank() ? "Sistema" : veterinarianField.getText().trim());
            preparedAppointment = AppointmentMapper.toDTO(appointmentService.prepareForRegistration(appointment));
            preparedServiceOrder = createServiceOrderUseCase.create(owner.getId(), owner.getNombreCompleto(),
                    pet.getId(), pet.getNombreMascota(), buildServiceOrderItem(date, time), currentProducts(),
                    suggestedProducts, parseDiscount());
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

    private JPanel section(String title, String[] labels, Component[] components) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UiTheme.PANEL_WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiTheme.BORDER_COLOR),
                new EmptyBorder(12, 12, 12, 12)));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel sectionTitle = new JLabel(title);
        sectionTitle.setFont(UiTheme.TITLE_FONT_SECTION);
        panel.add(sectionTitle, gbc);
        gbc.gridwidth = 1;
        for (int i = 0; i < labels.length; i++) {
            gbc.gridy++;
            gbc.gridx = 0;
            gbc.weightx = 0;
            JLabel label = new JLabel(labels[i]);
            label.setFont(UiTheme.BODY_FONT);
            panel.add(label, gbc);
            gbc.gridx = 1;
            gbc.weightx = 1;
            panel.add(components[i], gbc);
        }
        return panel;
    }

    private JPanel buildProductsSection() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(UiTheme.PANEL_WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiTheme.BORDER_COLOR),
                new EmptyBorder(12, 12, 12, 12)));
        JLabel title = new JLabel("Productos utilizados");
        title.setFont(UiTheme.TITLE_FONT_SECTION);
        productsTable.setRowHeight(28);
        productsTable.setFont(UiTheme.BODY_FONT);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        JButton add = new JButton("Agregar producto");
        JButton edit = new JButton("Editar");
        JButton remove = new JButton("Eliminar");
        UiTheme.styleButton(add, UiTheme.BTN_DARK, UiTheme.BTN_DARK_HOVER, UiTheme.TEXT_LIGHT, 150, 32, 12, true,
                false, 8);
        UiTheme.styleButton(edit, UiTheme.BTN_DARK, UiTheme.BTN_DARK_HOVER, UiTheme.TEXT_LIGHT, 100, 32, 12, true,
                false, 8);
        UiTheme.styleButton(remove, UiTheme.BTN_DARK, UiTheme.BTN_DARK_HOVER, UiTheme.TEXT_LIGHT, 100, 32, 12, true,
                false, 8);
        add.addActionListener(e -> addInventoryProduct());
        edit.addActionListener(e -> editSelectedProduct());
        remove.addActionListener(e -> removeSelectedProduct());
        actions.add(add);
        actions.add(edit);
        actions.add(remove);
        panel.add(title, BorderLayout.NORTH);
        panel.add(new JScrollPane(productsTable), BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildSummarySection() {
        serviceAmountField = readOnlyField(currency.format(saleItem.price()));
        productsAmountField = readOnlyField(currency.format(0));
        subtotalField = readOnlyField(currency.format(0));
        taxField = readOnlyField(currency.format(0));
        totalField = readOnlyField(currency.format(0));
        return section("Resumen", new String[]{"Servicio", "Productos", "Subtotal", "IVA", "Descuento", "Total"},
                new Component[]{serviceAmountField, productsAmountField, subtotalField, taxField, discountField,
                        totalField});
    }

    private void addInventoryProduct() {
        InventoryOption option = chooseInventoryProduct();
        if (option == null) {
            return;
        }
        int quantity = requestQuantity(1);
        if (quantity <= 0) {
            return;
        }
        addOrReplaceProduct(new ServiceProductDTO(option.item().id(), option.item().sku(), option.item().name(),
                quantity, option.item().price()));
    }

    private void editSelectedProduct() {
        int row = productsTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un producto para editar.", "Productos utilizados",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int quantity = requestQuantity(Integer.parseInt(String.valueOf(productsModel.getValueAt(row, 1))));
        if (quantity <= 0) {
            return;
        }
        productsModel.setValueAt(quantity, row, 1);
        double price = parseCurrencyValue(String.valueOf(productsModel.getValueAt(row, 2)));
        productsModel.setValueAt(currency.format(quantity * price), row, 3);
        ServiceProductDTO current = usedProducts.get(row);
        usedProducts.set(row, new ServiceProductDTO(current.productId(), current.sku(), current.name(), quantity,
                current.unitPrice()));
        updateSummary();
    }

    private void removeSelectedProduct() {
        int row = productsTable.getSelectedRow();
        if (row >= 0) {
            usedProducts.remove(row);
            productsModel.removeRow(row);
            updateSummary();
        }
    }

    private InventoryOption chooseInventoryProduct() {
        try {
            List<InventoryOption> options = inventoryService.findAll("").stream().map(InventoryOption::new).toList();
            if (options.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay productos de inventario disponibles.");
                return null;
            }
            Object selected = JOptionPane.showInputDialog(this, "Producto", "Agregar producto",
                    JOptionPane.PLAIN_MESSAGE, null, options.toArray(), options.get(0));
            return selected instanceof InventoryOption option ? option : null;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar inventario: " + ex.getMessage(),
                    "Inventario", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    private int requestQuantity(int currentValue) {
        String value = JOptionPane.showInputDialog(this, "Cantidad", String.valueOf(currentValue));
        if (value == null) {
            return 0;
        }
        try {
            int quantity = Integer.parseInt(value.trim());
            if (quantity <= 0) {
                throw new NumberFormatException();
            }
            return quantity;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor que cero.", "Cantidad inválida",
                    JOptionPane.WARNING_MESSAGE);
            return 0;
        }
    }

    private void addOrReplaceProduct(ServiceProductDTO product) {
        for (int row = 0; row < usedProducts.size(); row++) {
            ServiceProductDTO current = usedProducts.get(row);
            if (current.productId() != null && current.productId().equals(product.productId())) {
                int quantity = current.quantity() + product.quantity();
                ServiceProductDTO updated = new ServiceProductDTO(current.productId(), current.sku(), current.name(),
                        quantity, current.unitPrice());
                usedProducts.set(row, updated);
                productsModel.setValueAt(quantity, row, 1);
                productsModel.setValueAt(currency.format(updated.subtotal()), row, 3);
                updateSummary();
                return;
            }
        }
        usedProducts.add(product);
        productsModel.addRow(new Object[]{product.name(), product.quantity(), currency.format(product.unitPrice()),
                currency.format(product.subtotal())});
        updateSummary();
    }

    private void loadSuggestedProducts() {
        try {
            List<InventoryItem> suggestions = inventoryService.findAll(saleItem.category()).stream().limit(3).toList();
            for (InventoryItem item : suggestions) {
                ServiceProductDTO product = new ServiceProductDTO(item.id(), item.sku(), item.name(), 1, item.price());
                suggestedProducts.add(product);
                addOrReplaceProduct(product);
            }
        } catch (Exception ex) {
            // Las sugerencias son auxiliares; no deben bloquear el agendamiento.
        }
    }

    private List<ServiceProductDTO> currentProducts() {
        return List.copyOf(usedProducts);
    }

    private ServiceOrderItemDTO buildServiceOrderItem(LocalDate date, LocalTime time) {
        return new ServiceOrderItemDTO(saleItem.catalogItemId(), saleItem.name(),
                veterinarianField.getText().isBlank() ? "Sistema" : veterinarianField.getText().trim(), date, time,
                parseDuration(), notesArea.getText().trim(), saleItem.price());
    }

    private int parseDuration() {
        return ((Number) durationSpinner.getValue()).intValue();
    }

    private void updateSummary() {
        if (serviceAmountField == null) {
            return;
        }
        ServiceSummary summary = ServiceSummary.calculate(saleItem.price(), currentProductsAsDomain(), parseDiscount());
        productsAmountField.setText(currency.format(summary.productsAmount()));
        subtotalField.setText(currency.format(summary.subtotal()));
        taxField.setText(currency.format(summary.tax()));
        totalField.setText(currency.format(summary.total()));
        refreshAppointmentSummary();
    }

    private List<secureauth.domain.services.ServiceProduct> currentProductsAsDomain() {
        return currentProducts().stream()
                .map(product -> new secureauth.domain.services.ServiceProduct(product.productId(), product.sku(),
                        product.name(), product.quantity(), product.unitPrice()))
                .toList();
    }

    private double parseCurrencyValue(String value) {
        if (value == null) {
            return 0d;
        }
        String normalized = value.replace("$", "").replace("\u00a0", "").replace(" ", "").replace(".", "")
                .replace(",", ".");
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException ex) {
            return 0d;
        }
    }

    private JTextField readOnlyField(String value) {
        JTextField field = new JTextField(value);
        field.setEditable(false);
        styleField(field);
        return field;
    }

    private void syncPetDetails(Pet pet) {
        speciesField.setText("Canino");
        breedField.setText(empty(pet.getRaza()));
        ageField.setText(empty(pet.getEdad()));
        summaryPetLabel.setText(empty(pet.getNombreMascota()));
        refreshAppointmentSummary();
    }

    private double parseDiscount() {
        try {
            return Math.max(0d, Double.parseDouble(discountField.getText().trim().replace(',', '.')));
        } catch (RuntimeException ex) {
            return 0d;
        }
    }

    private void styleField(JTextField field) {
        field.setFont(UiTheme.BODY_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiTheme.BORDER_COLOR),
                new EmptyBorder(6, 10, 6, 10)));
        field.setBackground(Color.WHITE);
    }

    private void styleInfoLabel(JLabel label) {
        label.setOpaque(true);
        label.setFont(UiTheme.BODY_FONT.deriveFont(java.awt.Font.BOLD, 14f));
        label.setForeground(UiTheme.FOREST_GREEN);
        label.setBackground(new Color(237, 250, 242));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(190, 225, 205)),
                new EmptyBorder(7, 10, 7, 10)));
    }

    private void updateEndTimeLabel() {
        endTimeLabel.setText(selectedTime().plusMinutes(parseDuration()).toString());
    }

    private void refreshAppointmentSummary() {
        summaryServiceLabel.setText(saleItem.name());
        summaryDurationLabel.setText(parseDuration() + " minutos");
        try {
            updateEndTimeLabel();
            summaryDateLabel.setText(selectedDate().toString());
            summaryStartLabel.setText(selectedTime().toString());
            summaryEndLabel.setText(endTimeLabel.getText());
        } catch (RuntimeException ex) {
            summaryDateLabel.setText("-");
            summaryStartLabel.setText("-");
            summaryEndLabel.setText("-");
        }
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setFont(UiTheme.BODY_FONT);
        spinner.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UiTheme.BORDER_COLOR),
                new EmptyBorder(4, 8, 4, 8)));
        Component editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor defaultEditor) {
            defaultEditor.getTextField().setEditable(false);
            defaultEditor.getTextField().setFont(UiTheme.BODY_FONT);
            defaultEditor.getTextField().setBackground(Color.WHITE);
        }
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
        return LocalTime.of(hour, minute);
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

    private record InventoryOption(InventoryItem item) {
        @Override
        public String toString() {
            return item.name() + " - " + item.sku() + " - $" + String.format("%,.0f", item.price());
        }
    }
}
