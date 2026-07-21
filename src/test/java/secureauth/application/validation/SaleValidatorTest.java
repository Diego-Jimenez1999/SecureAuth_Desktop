package secureauth.application.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import secureauth.application.dto.SaleDTO;
import secureauth.application.dto.SaleItemDTO;
import secureauth.domain.sales.SaleItemType;

class SaleValidatorTest {

    private final SaleValidator validator = new SaleValidator();

    @Test
    void validatesCompleteSale() {
        SaleDTO sale = new SaleDTO(null, LocalDateTime.now(), "Mostrador", 11900d, "Efectivo", "Sistema",
                List.of(new SaleItemDTO("Shampoo", 10000d, 1, 7, null, SaleItemType.PRODUCT, "Inventario", null,
                        3000d, 1)));

        assertDoesNotThrow(() -> validator.validate(sale));
    }

    @Test
    void rejectsEmptySale() {
        SaleDTO sale = new SaleDTO(null, LocalDateTime.now(), "Mostrador", 0d, "Efectivo", "Sistema", List.of());

        assertThrows(IllegalArgumentException.class, () -> validator.validate(sale));
    }

    @Test
    void rejectsServiceWithoutConfiguredAppointment() {
        SaleDTO sale = new SaleDTO(null, LocalDateTime.now(), "Mostrador", 41650d, "Efectivo", "Sistema",
                List.of(new SaleItemDTO("Consulta", 35000d, 1, null, null, SaleItemType.SERVICE, "Veterinaria",
                        null, 10000d, 1)));

        assertThrows(IllegalArgumentException.class, () -> validator.validate(sale));
    }
}
