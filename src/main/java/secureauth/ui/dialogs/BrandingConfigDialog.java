package secureauth.ui.dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import secureauth.service.enterprise.BrandingService;
import secureauth.ui.utils.UiTheme;
import secureauth.ui.utils.factory.ButtonFactory;
import secureauth.ui.utils.factory.DialogFactory;

/** Configuración completa de branding (colores + logo + banner + título). */
public class BrandingConfigDialog extends JDialog {

    private final JTextField txtPrimary = new JTextField(10);
    private final JTextField txtSecondary = new JTextField(10);
    private final JTextField txtTertiary = new JTextField(10);
    private final JTextField txtTitle = new JTextField(20);
    private final JTextField txtLogo = new JTextField(24);
    private final JTextField txtBanner = new JTextField(24);
    private final BrandingService brandingService = new BrandingService();

    public BrandingConfigDialog(Frame parent) {
        super(parent, "Branding del Negocio", true);
        init();
    }

    private void init() {
        setSize(760, 360);
        setLocationRelativeTo(getParent());

        txtPrimary.setText(toHex(UiTheme.themePrimary()));
        txtSecondary.setText(toHex(UiTheme.themeSecondary()));
        txtTertiary.setText(toHex(UiTheme.themeTertiary()));
        txtTitle.setText(UiTheme.themeAppTitle());

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(javax.swing.BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel form = new JPanel(new java.awt.GridLayout(7, 1, 0, 8));
        form.add(row("Color primario", txtPrimary, chooseColor(txtPrimary)));
        form.add(row("Color secundario", txtSecondary, chooseColor(txtSecondary)));
        form.add(row("Color terciario", txtTertiary, chooseColor(txtTertiary)));
        form.add(row("Título App", txtTitle, new JLabel("")));
        form.add(row("Logo", txtLogo, chooseFile(txtLogo)));
        form.add(row("Banner", txtBanner, chooseFile(txtBanner)));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton save = ButtonFactory.primary("Guardar y Aplicar", 180);
        save.addActionListener(e -> save());
        actions.add(save);

        root.add(form, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private JPanel row(String label, java.awt.Component center, java.awt.Component right) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.add(new JLabel(label), BorderLayout.WEST);
        row.add(center, BorderLayout.CENTER);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    private JButton chooseColor(JTextField target) {
        JButton btn = ButtonFactory.dark("Elegir", 90);
        btn.addActionListener(e -> {
            java.awt.Color selected = JColorChooser.showDialog(this, "Selecciona color", java.awt.Color.decode(target.getText()));
            if (selected != null) target.setText(toHex(selected));
        });
        return btn;
    }

    private JButton chooseFile(JTextField target) {
        JButton btn = ButtonFactory.dark("Buscar", 90);
        btn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                target.setText(file.getAbsolutePath());
            }
        });
        return btn;
    }

    private void save() {
        try {
            brandingService.saveBranding(txtPrimary.getText().trim(), txtSecondary.getText().trim(), txtTertiary.getText().trim(),
                    txtLogo.getText().trim(), txtBanner.getText().trim(), txtTitle.getText().trim());

            java.awt.Window top = SwingUtilities.getWindowAncestor(this);
            if (top != null) SwingUtilities.updateComponentTreeUI(top);
            DialogFactory.info(this, "Branding aplicado correctamente.");
            dispose();
        } catch (Exception ex) {
            DialogFactory.error(this, "No se pudo guardar branding: " + ex.getMessage());
        }
    }

    private String toHex(java.awt.Color c) {
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }
}
