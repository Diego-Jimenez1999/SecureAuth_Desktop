package secureauth.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;

import secureauth.config.DatabaseConnection;
import secureauth.model.ReportMetrics;
import secureauth.ui.components.PanelReports;

/**
 * Controlador MVC del módulo de reportes.
 */
public class ReportController {

    private final PanelReports view;
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.of("es", "CO"));

    public ReportController(PanelReports view) {
        this.view = view;
        this.view.setOnRefresh(this::loadMetrics);
    }

    public void loadMetrics() {
        ReportMetrics metrics = queryMetrics();
        view.updateMetrics(
                currency.format(metrics.getTotalVentasHoy()),
                String.valueOf(metrics.getCitasHoy()),
                String.valueOf(metrics.getNuevosClientesMes()));
    }

    private ReportMetrics queryMetrics() {
        String sqlVentas = "SELECT COALESCE(SUM(total), 0) FROM sales WHERE DATE(fecha_venta) = CURRENT_DATE()";
        String sqlNuevosClientes = "SELECT COUNT(*) FROM owners WHERE YEAR(fecha_registro) = YEAR(CURRENT_DATE()) AND MONTH(fecha_registro) = MONTH(CURRENT_DATE())";

        try (Connection conn = DatabaseConnection.getConnection()) {
            double totalVentasHoy = singleDouble(conn, sqlVentas);
            int nuevosClientesMes = singleInt(conn, sqlNuevosClientes);
            // TODO: conectar con tabla real de citas cuando esté implementada.
            int citasHoy = 0;
            return new ReportMetrics(totalVentasHoy, citasHoy, nuevosClientesMes);
        } catch (SQLException ex) {
            return new ReportMetrics(0, 0, 0);
        }
    }

    private double singleDouble(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0;
        }
    }

    private int singleInt(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }
}
