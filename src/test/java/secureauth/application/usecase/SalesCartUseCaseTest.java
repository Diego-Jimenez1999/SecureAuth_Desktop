package secureauth.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import secureauth.application.dto.SaleItemDTO;
import secureauth.domain.sales.SaleItemType;

class SalesCartUseCaseTest {

    @Test
    void addsCompatibleItemsAsSingleLineAndCalculatesTotals() {
        SalesCartUseCase cart = new SalesCartUseCase();

        cart.addItem(item("Bano", 10000d, 1, null, 4));
        cart.addItem(item("Bano", 10000d, 1, null, 4));

        assertEquals(1, cart.getItems().size());
        assertEquals(2, cart.getItems().get(0).quantity());
        assertEquals(20000d, cart.subtotal());
        assertEquals(3800d, cart.tax());
        assertEquals(23800d, cart.total());
    }

    @Test
    void rejectsQuantityAboveVisibleStock() {
        SalesCartUseCase cart = new SalesCartUseCase();
        cart.addItem(item("Shampoo", 12000d, 2, 9, 1));

        assertThrows(IllegalArgumentException.class, () -> cart.incrementQuantity(0));
    }

    private SaleItemDTO item(String name, double price, int catalogId, Integer inventoryId, Integer stock) {
        return new SaleItemDTO(name, price, catalogId, inventoryId, "SKU", SaleItemType.PRODUCT, "Inventario", stock,
                3000d, 1);
    }
}
