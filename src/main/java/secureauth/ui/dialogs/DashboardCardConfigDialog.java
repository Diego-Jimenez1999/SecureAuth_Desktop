package secureauth.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import secureauth.shared.events.DashboardEventBus;
import secureauth.ui.components.dashboard.DashboardCard;
import secureauth.ui.components.dashboard.DashboardCardConfig;
import secureauth.ui.components.dashboard.DashboardCardRegistry;
import secureauth.ui.utils.UiTheme;

/**
 * Diálogo interactivo para configurar la visibilidad y los títulos de las tarjetas del Dashboard.
 * Permite cumplir con OCP de forma dinámica y visual.
 */
public final class DashboardCardConfigDialog extends JDialog {

    private final Map<String, JCheckBox> visibilityMap = new HashMap<>();
    private final Map<String, JTextField> titleMap = new HashMap<>();

    public DashboardCardConfigDialog(JDialog parent) {
        super(parent, "Configurar Tarjetas del Dashboard", true);
        init();
    }

    public DashboardCardConfigDialog(java.awt.Window parent) {
        super(parent, "Configurar Tarjetas del Dashboard", DEFAULT_MODALITY_TYPE);
        init();
    }

    private void init() {
        setSize(580, 480);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(0, 10));
        getContentPane().setBackground(Color.WHITE);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(31, 41, 55));
        headerPanel.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel titleLabel = new JLabel("Personalizar Tarjetas del Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Habilita/deshabilita tarjetas y personaliza sus títulos.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(209, 213, 219));

        JPanel headerTexts = new JPanel(new GridLayout(2, 1, 0, 2));
        headerTexts.setOpaque(false);
        headerTexts.add(titleLabel);
        headerTexts.add(subtitleLabel);

        headerPanel.add(headerTexts, BorderLayout.CENTER);
        add(headerPanel, BorderLayout.NORTH);

        // Cards List Panel inside ScrollPane
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);
        listPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        List<DashboardCard> cards = DashboardCardRegistry.getCards();
        for (DashboardCard card : cards) {
            boolean visible = DashboardCardConfig.isVisible(card.getId(), true);
            String title = DashboardCardConfig.getTitle(card.getId(), card.getDefaultTitle());

            JPanel rowPanel = new JPanel(new BorderLayout(10, 0));
            rowPanel.setBackground(Color.WHITE);
            rowPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)),
                    new EmptyBorder(10, 5, 10, 5)
            ));

            JCheckBox chkVisible = new JCheckBox("Visible", visible);
            chkVisible.setFont(new Font("Segoe UI", Font.BOLD, 12));
            chkVisible.setOpaque(false);
            chkVisible.setForeground(new Color(55, 65, 81));
            visibilityMap.put(card.getId(), chkVisible);

            JTextField txtTitle = new JTextField(title, 18);
            txtTitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            titleMap.put(card.getId(), txtTitle);

            JLabel lblDefault = new JLabel(" (" + (card.isSummaryCard() ? "Día" : "Mes") + ")");
            lblDefault.setFont(new Font("Segoe UI", Font.ITALIC, 11));
            lblDefault.setForeground(new Color(156, 163, 175));

            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            rightPanel.setOpaque(false);
            rightPanel.add(new JLabel("Título:"));
            rightPanel.add(txtTitle);
            rightPanel.add(lblDefault);

            rowPanel.add(chkVisible, BorderLayout.WEST);
            rowPanel.add(rightPanel, BorderLayout.EAST);

            listPanel.add(rowPanel);
        }

        javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(listPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // Actions Panel
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        actionsPanel.setBackground(new Color(249, 250, 251));
        actionsPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(229, 231, 235)));

        JButton btnSave = new JButton("Guardar Ajustes");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSave.setBackground(new Color(16, 185, 129));
        btnSave.setForeground(Color.WHITE);
        btnSave.setBorderPainted(false);
        btnSave.setFocusPainted(false);
        btnSave.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSave.setBorder(new EmptyBorder(8, 16, 8, 16));
        btnSave.addActionListener(e -> {
            for (DashboardCard card : cards) {
                JCheckBox chk = visibilityMap.get(card.getId());
                JTextField txt = titleMap.get(card.getId());
                if (chk != null && txt != null) {
                    DashboardCardConfig.saveConfig(card.getId(), chk.isSelected(), txt.getText().trim());
                }
            }
            DashboardEventBus.notifyDataChanged();
            JOptionPane.showMessageDialog(this, "Configuración de tarjetas guardada con éxito.", "Guardado", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });

        JButton btnCancel = new JButton("Cancelar");
        btnCancel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCancel.setBackground(new Color(107, 114, 128));
        btnCancel.setForeground(Color.WHITE);
        btnCancel.setBorderPainted(false);
        btnCancel.setFocusPainted(false);
        btnCancel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCancel.setBorder(new EmptyBorder(8, 16, 8, 16));
        btnCancel.addActionListener(e -> dispose());

        actionsPanel.add(btnCancel);
        actionsPanel.add(btnSave);
        add(actionsPanel, BorderLayout.SOUTH);
    }
}
