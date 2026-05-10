
package secureauth.ui.utils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JPanel;

/**
 * Clase personalizada de JPanel con bordes redondeados y fondo personalizado.
 * Permite establecer el color de fondo, el radio de los bordes y una imagen de fondo opcional.
 * @author Diego Alexander Gaviria Jimenez
 */
public class JpanelR extends JPanel {
    private Color backgroundColor = Color.WHITE;
    private int arcWidth = 20;
    private int arcHeight = 20;
    private Image image = null;

    public JpanelR() {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        RoundRectangle2D.Float shape = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, arcWidth, arcHeight);

        // Dibujar Fondo
        g2.setColor(backgroundColor);
        g2.fill(shape);

        // Dibujar Imagen si existe
        if (image != null) {
            Shape oldClip = g2.getClip();
            g2.setClip(shape);
            g2.drawImage(image, 0, 0, getWidth(), getHeight(), null);
            g2.setClip(oldClip);
        }

        g2.dispose();
        super.paintComponent(g);
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        repaint();
    }
    
    public void setArc(int arc) {
        this.arcWidth = arc;
        this.arcHeight = arc;
        repaint();
    }
}
