package secureauth.domain.sales;

import java.util.Locale;

public enum SaleItemType {
    PRODUCT,
    SERVICE;

    public boolean requiresAppointment() {
        return this == SERVICE;
    }

    public static SaleItemType fromCatalogValue(String value) {
        if (value == null) {
            return PRODUCT;
        }
        return switch (value.trim().toUpperCase(Locale.ROOT)) {
            case "SERVICE", "SERVICIO" -> SERVICE;
            default -> PRODUCT;
        };
    }

    public String displayName() {
        return this == SERVICE ? "Servicio" : "Producto";
    }
}
