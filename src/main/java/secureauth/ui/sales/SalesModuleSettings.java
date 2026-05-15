package secureauth.ui.sales;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Configuración global del módulo de ventas/servicios. */
public final class SalesModuleSettings {

    private static final SalesModuleSettings INSTANCE = new SalesModuleSettings();
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    private String brandingName = "SecureAuth";
    private double taxRate = 0.19d;
    private String currency = "COP";
    private String priceFormat = "#,##0.00";
    private final List<String> defaultSizes = new ArrayList<>(Arrays.asList("Pequeña", "Mediana", "Grande"));

    private SalesModuleSettings() { }

    public static SalesModuleSettings getInstance() { return INSTANCE; }

    public void addListener(PropertyChangeListener listener) { support.addPropertyChangeListener("settings", listener); }
    public void removeListener(PropertyChangeListener listener) { support.removePropertyChangeListener("settings", listener); }

    public String getBrandingName() { return brandingName; }
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
}
