package secureauth.ui.config;

import java.awt.Color;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/** Configuración visual y operativa persistida en TXT local. */
public final class ApplicationVisualSettings {

    public static final Path CONFIG_PATH = Path.of("secureauth.config.txt");

    public static final String DEFAULT_BRANDING = "SecureAuth";
    public static final String DEFAULT_LOGOTIPO_TEXT = "SecureAuth";
    public static final String DEFAULT_PRIMARY_COLOR = "#1F2937";
    public static final String DEFAULT_SECONDARY_COLOR = "#2563EB";
    public static final String DEFAULT_TERTIARY_COLOR = "#7C3AED";
    public static final String DEFAULT_SLOGAN = "Sistema inteligente de gestión";
    public static final String DEFAULT_LOGIN_TITLE = "SecureAuth";
    public static final String DEFAULT_LOGIN_SUBTITLE = "Sistema inteligente de gestión";
    public static final double DEFAULT_TAX = 0.19d;
    public static final String DEFAULT_CURRENCY = "COP";
    public static final String DEFAULT_FORMAT = "#,##0.00";
    public static final List<String> DEFAULT_SIZES = List.of("Pequeña", "Mediana", "Grande");

    private String branding = DEFAULT_BRANDING;
    private String logotipoText = DEFAULT_LOGOTIPO_TEXT;
    private String primaryColor = DEFAULT_PRIMARY_COLOR;
    private String secondaryColor = DEFAULT_SECONDARY_COLOR;
    private String tertiaryColor = DEFAULT_TERTIARY_COLOR;
    private String slogan = DEFAULT_SLOGAN;
    private String loginTitle = DEFAULT_LOGIN_TITLE;
    private String loginSubtitle = DEFAULT_LOGIN_SUBTITLE;
    private double tax = DEFAULT_TAX;
    private String currency = DEFAULT_CURRENCY;
    private String format = DEFAULT_FORMAT;
    private List<String> sizes = DEFAULT_SIZES;
    private String mainLogoPath = "";
    private String logotipoImagePath = "";

    public static ApplicationVisualSettings load() {
        ApplicationVisualSettings settings = new ApplicationVisualSettings();
        if (!Files.exists(CONFIG_PATH)) {
            return settings;
        }

        Properties props = new Properties();
        try (var reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
            props.load(reader);
        } catch (IOException ignored) {
            return settings;
        }

        settings.branding = text(props.getProperty("branding"), DEFAULT_BRANDING);
        settings.logotipoText = text(props.getProperty("logotipo_text"), DEFAULT_LOGOTIPO_TEXT);
        settings.primaryColor = text(props.getProperty("primary_color"), DEFAULT_PRIMARY_COLOR);
        settings.secondaryColor = text(props.getProperty("secondary_color"), DEFAULT_SECONDARY_COLOR);
        settings.tertiaryColor = text(props.getProperty("tertiary_color"), DEFAULT_TERTIARY_COLOR);
        settings.slogan = text(props.getProperty("slogan"), DEFAULT_SLOGAN);
        settings.loginTitle = text(props.getProperty("login_title"), DEFAULT_LOGIN_TITLE);
        settings.loginSubtitle = text(props.getProperty("login_subtitle"), DEFAULT_LOGIN_SUBTITLE);
        settings.tax = parseDouble(props.getProperty("tax"), DEFAULT_TAX);
        settings.currency = text(props.getProperty("currency"), DEFAULT_CURRENCY);
        settings.format = text(props.getProperty("format"), DEFAULT_FORMAT);
        settings.sizes = parseSizes(props.getProperty("sizes"));
        settings.mainLogoPath = text(props.getProperty("main_logo_path"), "");
        settings.logotipoImagePath = text(props.getProperty("logotipo_image_path"), "");
        return settings;
    }

    public void save() throws IOException {
        StringBuilder content = new StringBuilder();
        content.append("# SecureAuth Desktop - Configuracion visual y operativa\n");
        content.append("# Archivo TXT local. No depende de base de datos.\n\n");
        append(content, "branding", branding);
        append(content, "logotipo_text", logotipoText);
        append(content, "primary_color", primaryColor);
        append(content, "secondary_color", secondaryColor);
        append(content, "tertiary_color", tertiaryColor);
        append(content, "slogan", slogan);
        append(content, "login_title", loginTitle);
        append(content, "login_subtitle", loginSubtitle);
        append(content, "tax", String.valueOf(tax));
        append(content, "currency", currency);
        append(content, "format", format);
        append(content, "sizes", String.join(",", sizes));
        append(content, "main_logo_path", mainLogoPath);
        append(content, "logotipo_image_path", logotipoImagePath);
        Files.writeString(CONFIG_PATH, content.toString(), StandardCharsets.UTF_8);
    }

    public static Color parseColor(String value, Color fallback) {
        String hex = text(value, "");
        if (hex.isEmpty()) {
            return fallback;
        }
        try {
            return Color.decode(hex.startsWith("#") ? hex : "#" + hex);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    public static String text(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    private static void append(StringBuilder content, String key, String value) {
        content.append(key).append("=").append(value == null ? "" : value.trim()).append("\n");
    }

    private static double parseDouble(String value, double fallback) {
        try {
            return Double.parseDouble(text(value, String.valueOf(fallback)));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static List<String> parseSizes(String value) {
        List<String> parsed = Arrays.stream(text(value, "").split(","))
                .map(String::trim)
                .filter(size -> !size.isEmpty())
                .toList();
        return parsed.isEmpty() ? DEFAULT_SIZES : parsed;
    }

    public String getBranding() { return branding; }
    public void setBranding(String branding) { this.branding = branding; }
    public String getLogotipoText() { return logotipoText; }
    public void setLogotipoText(String logotipoText) { this.logotipoText = logotipoText; }
    public String getPrimaryColor() { return primaryColor; }
    public void setPrimaryColor(String primaryColor) { this.primaryColor = primaryColor; }
    public String getSecondaryColor() { return secondaryColor; }
    public void setSecondaryColor(String secondaryColor) { this.secondaryColor = secondaryColor; }
    public String getTertiaryColor() { return tertiaryColor; }
    public void setTertiaryColor(String tertiaryColor) { this.tertiaryColor = tertiaryColor; }
    public String getSlogan() { return slogan; }
    public void setSlogan(String slogan) { this.slogan = slogan; }
    public String getLoginTitle() { return loginTitle; }
    public void setLoginTitle(String loginTitle) { this.loginTitle = loginTitle; }
    public String getLoginSubtitle() { return loginSubtitle; }
    public void setLoginSubtitle(String loginSubtitle) { this.loginSubtitle = loginSubtitle; }
    public double getTax() { return tax; }
    public void setTax(double tax) { this.tax = tax; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public List<String> getSizes() { return sizes; }
    public void setSizes(List<String> sizes) { this.sizes = sizes == null || sizes.isEmpty() ? DEFAULT_SIZES : List.copyOf(sizes); }
    public String getMainLogoPath() { return mainLogoPath; }
    public void setMainLogoPath(String mainLogoPath) { this.mainLogoPath = mainLogoPath; }
    public String getLogotipoImagePath() { return logotipoImagePath; }
    public void setLogotipoImagePath(String logotipoImagePath) { this.logotipoImagePath = logotipoImagePath; }
}
