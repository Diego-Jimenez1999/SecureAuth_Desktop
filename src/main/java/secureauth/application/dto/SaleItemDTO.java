package secureauth.application.dto;

import secureauth.domain.sales.SaleItemType;

/**
 * DTO de una linea de venta usado por UI y casos de uso.
 */
public record SaleItemDTO(
        String name,
        double price,
        int catalogItemId,
        Integer inventoryItemId,
        String sku,
        SaleItemType type,
        String category,
        Integer stockAvailable,
        double gainPerUnit,
        int quantity,
        AppointmentDTO appointment,
        ServiceOrderDTO serviceOrder) {

    public SaleItemDTO(String name, double price, int catalogItemId, Integer inventoryItemId, String sku,
            SaleItemType type, String category, Integer stockAvailable, double gainPerUnit, int quantity) {
        this(name, price, catalogItemId, inventoryItemId, sku, type, category, stockAvailable, gainPerUnit, quantity,
                null, null);
    }

    public SaleItemDTO(String name, double price, int catalogItemId, Integer inventoryItemId, String sku,
            SaleItemType type, String category, Integer stockAvailable, double gainPerUnit, int quantity,
            AppointmentDTO appointment) {
        this(name, price, catalogItemId, inventoryItemId, sku, type, category, stockAvailable, gainPerUnit, quantity,
                appointment, null);
    }

    public SaleItemDTO withQuantity(int newQuantity) {
        return new SaleItemDTO(name, price, catalogItemId, inventoryItemId, sku, type, category, stockAvailable,
                gainPerUnit, newQuantity, appointment, serviceOrder);
    }

    public SaleItemDTO withAppointment(AppointmentDTO newAppointment) {
        return new SaleItemDTO(name, price, catalogItemId, inventoryItemId, sku, type, category, stockAvailable,
                gainPerUnit, quantity, newAppointment, serviceOrder);
    }

    public SaleItemDTO withServiceOrder(ServiceOrderDTO newServiceOrder) {
        return new SaleItemDTO(name, price, catalogItemId, inventoryItemId, sku, type, category, stockAvailable,
                gainPerUnit, quantity, appointment, newServiceOrder);
    }

    public double subtotal() {
        return price * quantity;
    }

    public boolean inventoryBacked() {
        return inventoryItemId != null;
    }

    public boolean requiresAppointment() {
        return type != null && type.requiresAppointment();
    }
}
