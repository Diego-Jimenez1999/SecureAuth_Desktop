package secureauth.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class SaleItemTest {

    @Test
    void defaultConstructorStartsWithUnitQuantityAndMetadata() {
        SaleItem item = new SaleItem("Shampoo", 15000.0);

        assertEquals("Shampoo", item.getName());
        assertEquals(15000.0, item.getPrice());
        assertEquals(1, item.getQuantity());
        assertEquals(15000.0, item.getSubtotal());
        assertEquals(0, item.getCatalogItemId());
        assertFalse(item.isInventoryBacked());
    }

    @Test
    void quantityChangesAndStockValidationAreEnforced() {
        SaleItem item = new SaleItem("Jabón", 5000.0, 3, 10, "SKU-3", "Producto", "Aseo", 4);

        item.setQuantity(2);
        assertEquals(10000.0, item.getSubtotal());

        assertThrows(IllegalArgumentException.class, () -> item.setQuantity(0));
        assertThrows(IllegalArgumentException.class, () -> item.setQuantity(5));

        item.incrementQuantity();
        assertEquals(3, item.getQuantity());
        assertEquals(15000.0, item.getSubtotal());

        item.decrementQuantity();
        assertEquals(2, item.getQuantity());

        assertTrue(item.isInventoryBacked());
        assertEquals(10, item.getInventoryItemId());
        assertEquals("SKU-3", item.getSku());
        assertEquals("Producto", item.getType());
        assertEquals("Aseo", item.getCategory());
        assertEquals(4, item.getStockAvailable());
        assertTrue(item.toString().contains("Jabón"));
    }
}
