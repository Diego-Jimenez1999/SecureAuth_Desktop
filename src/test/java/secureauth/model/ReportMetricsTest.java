package secureauth.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class ReportMetricsTest {

    @Test
    void exposesTheProvidedMetricValues() {
        ReportMetrics metrics = new ReportMetrics(125000.75, 8, 14);

        assertEquals(125000.75, metrics.getTotalVentasHoy());
        assertEquals(8, metrics.getCitasHoy());
        assertEquals(14, metrics.getNuevosClientesMes());
    }
}
