package secureauth.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import secureauth.ui.sales.SalesModuleSettings;
import secureauth.ui.utils.UiTheme;

/** Diálogo para configuración visual y parámetros de ventas. */
public class SalesModuleConfigDialog extends JDialog {

    private final SalesModuleSettings settings;
    private final JTextField txtBranding;
    private final JTextField txtTax;
    private final JTextField txtCurrency;
    private final JTextField txtFormat;
    private final JTextField txtSizes;

    public SalesModuleConfigDialog(JFrame parent) {
        super(parent, "Configuración del Módulo Ventas", true);
        this.settings = SalesModuleSettings.getInstance();
        this.txtBranding = new JTextField(settings.getBrandingName());
        this.txtTax = new JTextField(String.valueOf(settings.getTaxRate()));
        this.txtCurrency = new JTextField(settings.getCurrency());
        this.txtFormat = new JTextField(settings.getPriceFormat());
        this.txtSizes = new JTextField(String.join(",", settings.getDefaultSizes()));
        init();
    }

    private void init() {
        setSize(580, 320);
        setLocationRelativeTo(getParent());

        JPanel form = new JPanel(new GridLayout(10, 1, 0, 6));
        form.setBackground(UiTheme.PANEL_WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        form.add(new JLabel("Branding")); form.add(txtBranding);
        form.add(new JLabel("Impuesto (ej: 0.19)")); form.add(txtTax);
        form.add(new JLabel("Moneda")); form.add(txtCurrency);
        form.add(new JLabel("Formato precios")); form.add(txtFormat);
        form.add(new JLabel("Tamaños por defecto (coma)")); form.add(txtSizes);

        JPanel actions = new JPanel();
        JButton save = new JButton("Guardar");
        UiTheme.styleButton(save, UiTheme.BTN_DARK, UiTheme.BTN_DARK_HOVER, UiTheme.TEXT_LIGHT, 120, 34, 12, true, false, 8);
        save.addActionListener(e -> onSave());
        actions.add(save);

        JPanel root = new JPanel(new BorderLayout());
        root.add(form, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void onSave() {
        try {
            List<String> sizes = Arrays.stream(txtSizes.getText().split(",")).map(String::trim).filter(s -> !s.isBlank()).toList();
            settings.update(txtBranding.getText().trim(), Double.parseDouble(txtTax.getText().trim()),
                    txtCurrency.getText().trim(), txtFormat.getText().trim(), sizes);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Datos inválidos.", "Validación", JOptionPane.WARNING_MESSAGE);
        }
    }
}
