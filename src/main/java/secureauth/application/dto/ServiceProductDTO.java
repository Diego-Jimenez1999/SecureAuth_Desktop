package secureauth.application.dto;

public record ServiceProductDTO(Integer productId, String sku, String name, int quantity, double unitPrice) {
    public double subtotal() {
        return quantity * unitPrice;
    }
}
