package secureauth.ui.utils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

/**
 * Utilidades centralizadas para aplicar estilos modernos a componentes Swing.
 */
public class ComponentUtils {

    private static final Color DEFAULT_BORDER_COLOR = new Color(196, 196, 196);
    private static final Color TEXT_COLOR = new Color(35, 35, 35);
    private static final Color BG_COLOR = Color.WHITE;

    public static void styleTextField(JTextField field, Dimension size, Font font, Color focusColor) {
        field.setFont(font);
        field.setBackground(BG_COLOR);
        field.setForeground(TEXT_COLOR);
        field.setCaretColor(TEXT_COLOR);
        field.setPreferredSize(size);
        field.setMinimumSize(size);
        field.setMaximumSize(size);
        field.setBorder(new CompoundBorder(new RoundedLineBorder(DEFAULT_BORDER_COLOR, 14, 1), new EmptyBorder(8, 12, 8, 12)));

        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                field.setBorder(new CompoundBorder(new RoundedLineBorder(focusColor, 14, 2), new EmptyBorder(8, 12, 8, 12)));
            }

            @Override
            public void focusLost(FocusEvent e) {
                field.setBorder(new CompoundBorder(new RoundedLineBorder(DEFAULT_BORDER_COLOR, 14, 1), new EmptyBorder(8, 12, 8, 12)));
            }
        });
    }

    public static void styleComboBox(JComboBox<?> comboBox, Dimension size, Font font) {
        comboBox.setFont(font);
        comboBox.setBackground(BG_COLOR);
        comboBox.setFocusable(false);
        if (size != null) {
            comboBox.setPreferredSize(size);
            comboBox.setMinimumSize(size);
            comboBox.setMaximumSize(size);
        }
        comboBox.setBorder(new CompoundBorder(new RoundedLineBorder(DEFAULT_BORDER_COLOR, 14, 1), new EmptyBorder(8, 12, 8, 12)));
    }
}