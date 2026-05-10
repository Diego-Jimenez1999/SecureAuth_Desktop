package secureauth.ui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Panel reutilizable para mostrar información del usuario.
 */
public class UserButtonPanel extends JPanel {

    private static final Color COLOR_BG = new Color(21, 35, 56);
    private static final Color COLOR_BORDER = new Color(52, 67, 92);
    private static final Color COLOR_TEXT = new Color(240, 245, 255);
    private static final Color COLOR_SUBTEXT = new Color(178, 191, 214);
    private static final Color COLOR_ONLINE = new Color(70, 220, 120);
    private static final Color COLOR_OFFLINE = new Color(227, 57, 57);

    public UserButtonPanel(String userName, String statusText) {
        setLayout(new BorderLayout(0, 8));
        setOpaque(false);

        JPanel mainRow = new JPanel(new BorderLayout(10, 0));
        mainRow.setPreferredSize(new Dimension(50, 100));
        mainRow.setOpaque(true);
        mainRow.setBackground(COLOR_BG);
        mainRow.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        mainRow.setPreferredSize(new Dimension(220, 86));

        mainRow.add(buildAvatar(), BorderLayout.WEST);
        mainRow.add(buildUserInfo(userName, statusText), BorderLayout.CENTER);

        add(mainRow, BorderLayout.NORTH);
    }

    /**
     * Construye el panel de avatar del usuario.
     * @return el panel de avatar del usuario
     */
    private JPanel buildAvatar() {
        JLabel avatar = new JLabel("👤", JLabel.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_OFFLINE);
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.setColor(new Color(255, 255, 255, 50));
                g2.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setOpaque(false);
        avatar.setForeground(Color.WHITE);
        avatar.setFont(new Font("SansSerif", Font.PLAIN, 20));
        avatar.setPreferredSize(new Dimension(44, 44));
        
        // Envolver el avatar en un panel para centrarlo
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrapper.setOpaque(false);
        wrapper.add(avatar);
        return wrapper;
    }

    /**
     * Construye el panel de información del usuario.
     * @param userName el nombre del usuario
     * @param statusText el texto del estado
     * @return el panel de información del usuario
     */
    private JPanel buildUserInfo(String userName, String statusText) {
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setOpaque(false);

        JLabel lblName = new JLabel(userName == null ? "Usuario" : userName);
        lblName.setForeground(COLOR_TEXT);
        lblName.setFont(new Font("SansSerif", Font.BOLD, 13));

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        statusPanel.setOpaque(false);
        
        // Indicador de estado (conectado verde o rojo)
        JLabel dot = new JLabel("●");
        dot.setForeground(userName == null ? COLOR_OFFLINE : COLOR_ONLINE);
        dot.setFont(new Font("SansSerif", Font.PLAIN, 10));

        // Texto del estado (conectado, desconectado, etc.)
        JLabel lblStatus = new JLabel(statusText == null ? "Conectado" : statusText);
        lblStatus.setForeground(COLOR_SUBTEXT);
        lblStatus.setFont(new Font("SansSerif", Font.PLAIN, 12));

        statusPanel.add(dot);
        statusPanel.add(lblStatus);

        infoPanel.add(lblName);
        infoPanel.add(statusPanel);

        return infoPanel;
    }
}
