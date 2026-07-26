package secureauth.model;

import java.util.List;

/** Serie de datos lista para renderizarse en un componente grafico reusable. */
public record ReportChartData(String title, List<ReportChartPoint> points) {
    public ReportChartData {
        points = List.copyOf(points == null ? List.of() : points);
    }
}
