package secureauth.service.enterprise;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import secureauth.ui.config.ApplicationVisualSettings;

/** Configuración global del módulo de ventas/servicios. */
public final class SalesModuleSettings {

    private static final SalesModuleSettings INSTANCE = new SalesModuleSettings();
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    private String brandingName = "SecureAuth";
    private String logotipoText = "SecureAuth";
    private double taxRate = 0.19d;
    private String currency = "COP";
    private String priceFormat = "#,##0.00";
    private final List<String> defaultSizes = new ArrayList<>(Arrays.asList("Pequeña", "Mediana", "Grande"));

    private SalesModuleSettings() {
        apply(ApplicationVisualSettings.load());
    }

    public static SalesModuleSettings getInstance() { return INSTANCE; }

    public void addListener(PropertyChangeListener listener) { support.addPropertyChangeListener("settings", listener); }
    public void removeListener(PropertyChangeListener listener) { support.removePropertyChangeListener("settings", listener); }

    public String getBrandingName() { return brandingName; }
    public String getLogotipoText() { return logotipoText; }
    public double getTaxRate() { return taxRate; }
    public String getCurrency() { return currency; }
    public String getPriceFormat() { return priceFormat; }
    public List<String> getDefaultSizes() { return List.copyOf(defaultSizes); }

    public void update(String brandingName, double taxRate, String currency, String priceFormat, List<String> sizes) {
        this.brandingName = brandingName;
        this.taxRate = taxRate;
        this.currency = currency;
        this.priceFormat = priceFormat;
        this.defaultSizes.clear();
        this.defaultSizes.addAll(sizes);
        support.firePropertyChange("settings", null, null);
    }

    public void update(String brandingName, String logotipoText, double taxRate, String currency,
                       String priceFormat, List<String> sizes) {
        this.logotipoText = logotipoText;
        update(brandingName, taxRate, currency, priceFormat, sizes);
    }

    public void updateBrandingName(String brandingName) {
        this.brandingName = brandingName;
        support.firePropertyChange("settings", null, null);
    }

    public void updateLogotipoText(String logotipoText) {
        this.logotipoText = logotipoText;
        support.firePropertyChange("settings", null, null);
    }

    public void updateTaxRate(double taxRate) {
        this.taxRate = taxRate;
        support.firePropertyChange("settings", null, null);
    }

    public void updateCurrency(String currency) {
        this.currency = currency;
        support.firePropertyChange("settings", null, null);
    }

    public void updatePriceFormat(String priceFormat) {
        this.priceFormat = priceFormat;
        support.firePropertyChange("settings", null, null);
    }

    public void updateDefaultSizes(List<String> sizes) {
        this.defaultSizes.clear();
        this.defaultSizes.addAll(sizes);
        support.firePropertyChange("settings", null, null);
    }

    public void loadDefaultSettings() {
        apply(new ApplicationVisualSettings());
        support.firePropertyChange("settings", null, null);
    }

    private void apply(ApplicationVisualSettings visualSettings) {
        this.brandingName = visualSettings.getBranding();
        this.logotipoText = visualSettings.getLogotipoText();
        this.taxRate = visualSettings.getTax();
        this.currency = visualSettings.getCurrency();
        this.priceFormat = visualSettings.getFormat();
        this.defaultSizes.clear();
        this.defaultSizes.addAll(visualSettings.getSizes());
    }
}
