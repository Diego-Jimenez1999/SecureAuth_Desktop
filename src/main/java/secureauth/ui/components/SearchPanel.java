package secureauth.ui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

import secureauth.ui.utils.JpanelR;

/**
 * Componente de búsqueda rápida para el dashboard.
 *
 * @author Diego
 * @version 1.0
 */
public class SearchPanel extends JpanelR {

    /**
     * Constructor del panel de consulta rápida.
     *
     * @param onSearch callback que recibe el texto de búsqueda
     */
    public SearchPanel(Consumer<String> onSearch) {
        setBackgroundColor(Color.WHITE);
        setPreferredSize(new Dimension(0, 80));
        setLayout(new FlowLayout(FlowLayout.LEFT, 20, 20));

        JLabel lblTitle = new JLabel("Consultas rápidas");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 14));

        JLabel lblHint = new JLabel("Buscar por nombre o email");
        lblHint.setForeground(Color.GRAY);

        JTextField txtBusquedaRapida = new JTextField(30);
        txtBusquedaRapida.setPreferredSize(new Dimension(300, 35));

        JButton btnConsultar = new JButton("Consultar");
        btnConsultar.setBackground(new Color(30, 36, 48));
        btnConsultar.setForeground(Color.WHITE);
        btnConsultar.setFocusPainted(false);
        btnConsultar.setPreferredSize(new Dimension(120, 35));

        btnConsultar.addActionListener(e -> onSearch.accept(txtBusquedaRapida.getText()));
        txtBusquedaRapida.addActionListener(e -> onSearch.accept(txtBusquedaRapida.getText()));

        add(lblTitle);
        add(lblHint);
        add(txtBusquedaRapida);
        add(btnConsultar);
    }
}
