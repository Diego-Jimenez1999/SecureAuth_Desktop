package secureauth.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import secureauth.ui.utils.JpanelR;

/**
 * Vista de reportes con KPIs listos para poblarse desde controlador.
 */
public class PanelReports extends JPanel {

    private static final Color BG_PAGE = new Color(0xF5F7F9);
    private static final Color BG_CARD = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(0xE5E7EB);
    private static final Color TEXT_DARK = new Color(0x111827);
    private static final Color TEXT_GRAY = new Color(0x6B7280);
    private static final Color PRIMARY_BLUE = new Color(0x2563EB);
    private static final Color SUCCESS_GREEN = new Color(0x16A34A);

    private final JLabel ventasValue = new JLabel("$0");
    private final JLabel citasValue = new JLabel("0");
    private final JLabel nuevosClientesValue = new JLabel("0");
    private final JButton btnRefresh = new JButton("Actualizar");

    public PanelReports() {
        setLayout(new BorderLayout(0, 24));
        setBackground(BG_PAGE);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildKpiGrid(), BorderLayout.CENTER);
    }

    private JPanel buildHeader() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        JPanel titleGroup = new JPanel();
        titleGroup.setLayout(new BoxLayout(titleGroup, BoxLayout.Y_AXIS));
        titleGroup.setOpaque(false);

        JLabel title = new JLabel("Reportes del Negocio");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT_DARK);

        JLabel subtitle = new JLabel("Resumen de ventas, citas y clientes nuevos");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_GRAY);

        titleGroup.add(title);
        titleGroup.add(Box.createVerticalStrut(4));
        titleGroup.add(subtitle);

        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRefresh.setBackground(PRIMARY_BLUE);
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBorderPainted(false);

        wrapper.add(titleGroup, BorderLayout.WEST);
        wrapper.add(btnRefresh, BorderLayout.EAST);
        return wrapper;
    }

    private JPanel buildKpiGrid() {
        JPanel grid = new JPanel(new GridLayout(1, 3, 20, 0));
        grid.setOpaque(false);

        grid.add(createCard("Ventas (Hoy)", ventasValue, PRIMARY_BLUE));
        grid.add(createCard("Citas (Hoy)", citasValue, SUCCESS_GREEN));
        grid.add(createCard("Nuevos Clientes (Mes)", nuevosClientesValue, TEXT_DARK));
        return grid;
    }

    private JPanel createCard(String labelText, JLabel valueLabel, Color accent) {
        JpanelR card = new JpanelR();
        card.setBackgroundColor(BG_CARD);
        card.setArc(14);
        card.setBorderConfig(BORDER_COLOR, 1.0f);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TEXT_GRAY);

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        valueLabel.setForeground(accent);

        card.add(label, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    public void setOnRefresh(Runnable handler) {
        btnRefresh.addActionListener(e -> handler.run());
    }

    public void updateMetrics(String ventas, String citas, String nuevosClientes) {
        ventasValue.setText(ventas);
        citasValue.setText(citas);
        nuevosClientesValue.setText(nuevosClientes);
    }
}
