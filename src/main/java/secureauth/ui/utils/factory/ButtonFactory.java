package secureauth.ui.utils.factory;

import javax.swing.JButton;

import secureauth.ui.utils.UiTheme;

/** Fábrica de botones consistente para toda la UI. */
public final class ButtonFactory {

    private ButtonFactory() { }

    public static JButton primary(String text, int width) {
        JButton button = new JButton(text);
        UiTheme.styleButton(button, UiTheme.themePrimary(), UiTheme.themePrimaryHover(), UiTheme.themeSecondary(), width, 34, 12, true, false, 8);
        return button;
    }

    public static JButton dark(String text, int width) {
        JButton button = new JButton(text);
        UiTheme.styleButton(button, UiTheme.BTN_DARK, UiTheme.BTN_DARK_HOVER, UiTheme.themePrimary(), width, 34, 12, true, false, 8);
        return button;
    }
}
