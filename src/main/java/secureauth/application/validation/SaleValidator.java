package secureauth.application.validation;

import secureauth.application.dto.SaleDTO;
import secureauth.application.dto.SaleItemDTO;

public class SaleValidator {

    public void validate(SaleDTO sale) {
        if (sale == null || sale.items().isEmpty()) {
            throw new IllegalArgumentException("No se permiten ventas vacias.");
        }
        if (sale.paymentMethod() == null || sale.paymentMethod().isBlank()) {
            throw new IllegalArgumentException("Selecciona un metodo de pago.");
        }
        for (SaleItemDTO item : sale.items()) {
            validateItem(item);
        }
    }

    public void validateItem(SaleItemDTO item) {
        if (item == null) {
            throw new IllegalArgumentException("La linea de venta es invalida.");
        }
        if (item.name() == null || item.name().isBlank()) {
            throw new IllegalArgumentException("El producto o servicio debe tener nombre.");
        }
        if (item.price() <= 0d) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero.");
        }
        if (item.type() == null) {
            throw new IllegalArgumentException("El item debe tener un tipo de venta.");
        }
        if (item.quantity() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        }
        if (item.stockAvailable() != null && item.quantity() > item.stockAvailable()) {
            throw new IllegalArgumentException("No hay suficiente inventario disponible.");
        }
        if (item.requiresAppointment() && item.appointment() == null) {
            throw new IllegalArgumentException("El servicio debe tener una cita configurada.");
        }
    }
}
