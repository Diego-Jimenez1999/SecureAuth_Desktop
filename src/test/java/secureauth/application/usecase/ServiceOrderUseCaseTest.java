package secureauth.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import secureauth.application.dto.ServiceOrderDTO;
import secureauth.application.dto.ServiceOrderItemDTO;
import secureauth.application.dto.ServiceProductDTO;
import secureauth.domain.services.ServiceOrderStatus;

class ServiceOrderUseCaseTest {

    @Test
    void createsServiceOrderWithUsedAndSuggestedProducts() {
        CreateServiceOrderUseCase useCase = new CreateServiceOrderUseCase();

        ServiceOrderDTO order = useCase.create(1, "Cliente", 2, "Mascota", item(),
                List.of(product(10, "Shampoo", 2, 12000d)), List.of(product(11, "Gasa", 1, 3000d)), 1000d);

        assertEquals(ServiceOrderStatus.SCHEDULED, order.status());
        assertEquals(1, order.products().size());
        assertEquals(1, order.suggestedProducts().size());
        assertEquals(24000d, order.summary().productsAmount());
        assertEquals(74000d, order.summary().subtotal());
        assertEquals(86870d, order.summary().total());
    }

    @Test
    void updatesUsedProductsAndRecalculatesSummary() {
        CreateServiceOrderUseCase create = new CreateServiceOrderUseCase();
        UpdateServiceOrderUseCase update = new UpdateServiceOrderUseCase();
        ServiceOrderDTO order = create.create(1, "Cliente", 2, "Mascota", item(), List.of(), List.of(), 0d);

        ServiceOrderDTO updated = update.updateProducts(order, List.of(product(10, "Shampoo", 3, 12000d)), 2000d);

        assertEquals(1, updated.products().size());
        assertEquals(36000d, updated.summary().productsAmount());
        assertEquals(99960d, updated.summary().total());
    }

    @Test
    void validatesRequiredFieldsAndDuplicateProducts() {
        ValidateServiceOrderUseCase validator = new ValidateServiceOrderUseCase();
        ServiceProductDTO duplicate = product(10, "Shampoo", 1, 12000d);
        ServiceOrderDTO order = new ServiceOrderDTO(null, 1, "Cliente", 2, "Mascota",
                ServiceOrderStatus.SCHEDULED, item(), List.of(duplicate, duplicate), List.of(), null);

        assertThrows(IllegalArgumentException.class, () -> validator.validate(order));
        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(new ServiceOrderDTO(null, null, "", 2, "Mascota",
                        ServiceOrderStatus.SCHEDULED, item(), List.of(), List.of(), null)));
    }

    @Test
    void rejectsInvalidQuantitiesAndNegativeTotals() {
        ValidateServiceOrderUseCase validator = new ValidateServiceOrderUseCase();

        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(new ServiceOrderDTO(null, 1, "Cliente", 2, "Mascota",
                        ServiceOrderStatus.SCHEDULED, item(), List.of(product(10, "Shampoo", 0, 12000d)),
                        List.of(), null)));
        assertThrows(IllegalArgumentException.class,
                () -> validator.validate(new ServiceOrderDTO(null, 1, "Cliente", 2, "Mascota",
                        ServiceOrderStatus.SCHEDULED,
                        new ServiceOrderItemDTO(5, "Consulta", "Vet", LocalDate.now(), LocalTime.of(9, 0), 60,
                                "", -1d),
                        List.of(), List.of(), null)));
    }

    private ServiceOrderItemDTO item() {
        return new ServiceOrderItemDTO(5, "Consulta", "Vet", LocalDate.now(), LocalTime.of(9, 0), 60, "", 50000d);
    }

    private ServiceProductDTO product(Integer id, String name, int quantity, double price) {
        return new ServiceProductDTO(id, "SKU-" + id, name, quantity, price);
    }
}
