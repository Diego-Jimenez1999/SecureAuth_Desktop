package secureauth.domain.services;

public record ServiceProduct(Integer productId, String sku, String name, int quantity, double unitPrice) {

    public ServiceProduct {
        if (quantity <= 0) {
            throw new IllegalArgumentException("La cantidad del producto debe ser mayor que cero.");
        }
        if (unitPrice < 0d) {
            throw new IllegalArgumentException("El precio del producto no puede ser negativo.");
        }
    }

    public double subtotal() {
        return quantity * unitPrice;
    }
}
