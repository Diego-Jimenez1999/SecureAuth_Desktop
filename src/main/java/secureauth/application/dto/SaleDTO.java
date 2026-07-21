package secureauth.application.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO de cabecera de venta. La UI trabaja con este objeto en lugar de construir
 * entidades de dominio directamente.
 */
public record SaleDTO(
        Integer id,
        LocalDateTime date,
        String customerName,
        double total,
        String paymentMethod,
        String sellerName,
        List<SaleItemDTO> items) {

    public SaleDTO {
        items = List.copyOf(items == null ? List.of() : items);
    }
}
