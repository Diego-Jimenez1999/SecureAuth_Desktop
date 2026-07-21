package secureauth.domain.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class ServiceSummaryTest {

    @Test
    void calculatesServiceProductsTaxDiscountAndTotal() {
        ServiceSummary summary = ServiceSummary.calculate(50000d,
                List.of(new ServiceProduct(1, "SKU-1", "Shampoo", 2, 12000d),
                        new ServiceProduct(2, "SKU-2", "Gasa", 1, 3000d)),
                1000d);

        assertEquals(27000d, summary.productsAmount());
        assertEquals(77000d, summary.subtotal());
        assertEquals(14440d, summary.tax());
        assertEquals(90440d, summary.total());
    }

    @Test
    void clampsDiscountWhenItExceedsSubtotal() {
        ServiceSummary summary = ServiceSummary.calculate(10000d, List.of(), 50000d);

        assertEquals(10000d, summary.subtotal());
        assertEquals(0d, summary.tax());
        assertEquals(0d, summary.total());
    }
}
