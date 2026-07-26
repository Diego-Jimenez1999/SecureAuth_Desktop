package secureauth.ui.components.charts;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.text.Format;
import java.util.List;

import javax.swing.JPanel;

import secureauth.model.ReportChartData;
import secureauth.model.ReportChartPoint;

/**
 * Componente de gráfico reutilizable para el módulo de Reportes.
 *
 * <p>Dibuja series de datos ({@link ReportChartData}) obtenidas desde la capa
 * de negocio (Service/Repository, nunca por SQL directo desde la vista) como
 * barras o líneas. El mismo componente sirve para cualquier reporte —ventas
 * por día, productos más vendidos, citas por estado, inventario por
 * categoría, etc.— sin necesidad de crear una clase de gráfico distinta por
 * cada caso: solo cambia la {@link ReportChartData} recibida en
 * {@link #setData(ReportChartData)}.</p>
 *
 * <p>No consulta la base de datos: es puramente de presentación. El
 * controlador (por ejemplo {@code ReportController}) es responsable de
 * obtener los datos en un hilo de fondo ({@code SwingWorker}) y entregarlos
 * ya calculados mediante {@link #setData(ReportChartData)}.</p>
 */
public class ReportChartPanel extends JPanel {

    /** Tipo de trazo soportado por el panel. */
    public enum ChartType { BAR, LINE }

    private static final Color AXIS = new Color(203, 213, 225);
    private static final Color GRID = new Color(241, 245, 249);
    private static final Color MUTED_TEXT = new Color(148, 163, 184);

    private final ChartType type;
    private final Color accent;
    private final Format valueFormat;

    private ReportChartData data = new ReportChartData("", List.of());

    /**
     * @param type tipo de gráfico a dibujar (barras o líneas)
     * @param accent color principal de la serie
     * @param valueFormat formato usado para los valores del eje Y (moneda, entero, etc.)
     */
    public ReportChartPanel(ChartType type, Color accent, Format valueFormat) {
        this.type = type;
        this.accent = accent;
        this.valueFormat = valueFormat;
        setOpaque(false);
        setPreferredSize(new Dimension(380, 240));
    }

    /**
     * Reemplaza la serie graficada y solicita el repintado del componente.
     * Debe invocarse desde el Event Dispatch Thread (por ejemplo, dentro de
     * {@code SwingWorker#done()}) para evitar tocar Swing desde un hilo de fondo.
     *
     * @param newData datos ya calculados por la capa de negocio; puede venir vacío
     */
    public void setData(ReportChartData newData) {
        this.data = newData == null ? new ReportChartData("", List.of()) : newData;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        List<ReportChartPoint> points = data.points();
        int width = getWidth();
        int height = getHeight();

        if (points.isEmpty()) {
            drawEmptyState(g2, width, height);
            g2.dispose();
            return;
        }

        int marginLeft = 60;
        int marginRight = 20;
        int marginTop = 15;
        int marginBottom = 38;

        int plotWidth = Math.max(10, width - marginLeft - marginRight);
        int plotHeight = Math.max(10, height - marginTop - marginBottom);

        double maxValue = points.stream().mapToDouble(ReportChartPoint::value).max().orElse(0);
        maxValue = maxValue <= 0 ? 1 : maxValue * 1.15;

        drawGridAndAxis(g2, marginLeft, marginTop, plotWidth, plotHeight, maxValue);

        if (type == ChartType.BAR) {
            drawBars(g2, points, marginLeft, marginTop, plotWidth, plotHeight, maxValue);
        } else {
            drawLine(g2, points, marginLeft, marginTop, plotWidth, plotHeight, maxValue);
        }

        g2.dispose();
    }

    private void drawEmptyState(Graphics2D g2, int width, int height) {
        g2.setColor(MUTED_TEXT);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        FontMetrics fm = g2.getFontMetrics();
        String msg = "Sin datos disponibles para este periodo";
        int x = (width - fm.stringWidth(msg)) / 2;
        g2.drawString(msg, Math.max(10, x), height / 2);
    }

    private void drawGridAndAxis(Graphics2D g2, int marginLeft, int marginTop, int plotWidth, int plotHeight,
            double maxValue) {
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        FontMetrics fm = g2.getFontMetrics();
        int lines = 4;
        for (int i = 0; i <= lines; i++) {
            int y = marginTop + plotHeight - (int) (plotHeight * (i / (double) lines));
            g2.setColor(i == 0 ? AXIS : GRID);
            g2.draw(new Line2D.Double(marginLeft, y, marginLeft + plotWidth, y));

            String label = formatValue(maxValue * (i / (double) lines));
            g2.setColor(MUTED_TEXT);
            g2.drawString(label, marginLeft - fm.stringWidth(label) - 8, y + fm.getAscent() / 2 - 2);
        }
    }

    private void drawBars(Graphics2D g2, List<ReportChartPoint> points, int marginLeft, int marginTop, int plotWidth,
            int plotHeight, double maxValue) {
        int n = points.size();
        double slot = plotWidth / (double) n;
        double barWidth = Math.min(slot * 0.55, 48);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        FontMetrics fm = g2.getFontMetrics();

        for (int i = 0; i < n; i++) {
            ReportChartPoint point = points.get(i);
            double barHeight = plotHeight * (point.value() / maxValue);
            double x = marginLeft + slot * i + (slot - barWidth) / 2.0;
            double y = marginTop + plotHeight - barHeight;

            g2.setColor(accent);
            g2.fill(new Rectangle2D.Double(x, y, barWidth, Math.max(0, barHeight)));

            String label = truncate(point.label(), fm, (int) slot);
            int labelX = (int) (marginLeft + slot * i + (slot - fm.stringWidth(label)) / 2.0);
            g2.setColor(MUTED_TEXT);
            g2.drawString(label, labelX, marginTop + plotHeight + fm.getAscent() + 6);
        }
    }

    private void drawLine(Graphics2D g2, List<ReportChartPoint> points, int marginLeft, int marginTop, int plotWidth,
            int plotHeight, double maxValue) {
        int n = points.size();
        double slot = n > 1 ? plotWidth / (double) (n - 1) : 0;

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        FontMetrics fm = g2.getFontMetrics();

        double[] xs = new double[n];
        double[] ys = new double[n];
        for (int i = 0; i < n; i++) {
            xs[i] = n == 1 ? marginLeft + plotWidth / 2.0 : marginLeft + slot * i;
            ys[i] = marginTop + plotHeight - plotHeight * (points.get(i).value() / maxValue);
        }

        g2.setColor(accent);
        g2.setStroke(new BasicStroke(2.4f));
        for (int i = 0; i < n - 1; i++) {
            g2.draw(new Line2D.Double(xs[i], ys[i], xs[i + 1], ys[i + 1]));
        }

        boolean labelEveryPoint = n <= 8;
        int step = labelEveryPoint ? 1 : (int) Math.ceil(n / 8.0);

        for (int i = 0; i < n; i++) {
            g2.setColor(accent);
            g2.fill(new Ellipse2D.Double(xs[i] - 3, ys[i] - 3, 6, 6));

            if (i % step == 0 || i == n - 1) {
                String label = truncate(points.get(i).label(), fm, 60);
                int labelX = (int) (xs[i] - fm.stringWidth(label) / 2.0);
                g2.setColor(MUTED_TEXT);
                g2.drawString(label, labelX, marginTop + plotHeight + fm.getAscent() + 6);
            }
        }
    }

    private String truncate(String text, FontMetrics fm, int maxWidth) {
        if (text == null) {
            return "";
        }
        String result = text;
        while (fm.stringWidth(result) > maxWidth && result.length() > 1) {
            result = result.substring(0, result.length() - 1);
        }
        if (!result.equals(text) && result.length() > 1) {
            result = result.substring(0, result.length() - 1) + "…";
        }
        return result;
    }

    private String formatValue(double value) {
        return valueFormat != null ? valueFormat.format(value) : String.valueOf(Math.round(value));
    }
}
