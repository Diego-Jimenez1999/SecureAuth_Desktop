
package secureauth.ui;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;

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