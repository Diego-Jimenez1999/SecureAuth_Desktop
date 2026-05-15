package secureauth.model;

/**
 * DTO simple para métricas del dashboard de reportes.
 */
public class ReportMetrics {
    private final double totalVentasHoy;
    private final int citasHoy;
    private final int nuevosClientesMes;

    public ReportMetrics(double totalVentasHoy, int citasHoy, int nuevosClientesMes) {
        this.totalVentasHoy = totalVentasHoy;
        this.citasHoy = citasHoy;
        this.nuevosClientesMes = nuevosClientesMes;
    }

    public double getTotalVentasHoy() {
        return totalVentasHoy;
    }

    public int getCitasHoy() {
        return citasHoy;
    }

    public int getNuevosClientesMes() {
        return nuevosClientesMes;
    }
}
