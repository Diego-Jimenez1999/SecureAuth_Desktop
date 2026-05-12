
package secureauth.ui.utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JPanel;

/**
 * Clase JPanel con bordes redondeados, bordes opcionales e imágenes.
 * Optimizado para SecureAuth Desktop.
 * @author Diego Alexander Gaviria Jimenez
 */
public class JpanelR extends JPanel {
    private Color backgroundColor = Color.WHITE;
    private Color borderColor = new Color(230, 230, 230); // Gris muy claro para bordes
    private int arcWidth = 20;
    private int arcHeight = 20;
    private Image image = null;
    private boolean showBorder = false;
    private float borderThickness = 1.0f;

    public JpanelR() {
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // Definir la forma redondeada
        RoundRectangle2D.Float shape = new RoundRectangle2D.Float(
            0, 0, getWidth() - 1, getHeight() - 1, arcWidth, arcHeight);

        // 1. Dibujar Fondo
        g2.setColor(backgroundColor);
        g2.fill(shape);

        // 2. Dibujar Imagen si existe
        if (image != null) {
            Shape oldClip = g2.getClip();
            g2.setClip(shape);
            g2.drawImage(image, 0, 0, getWidth(), getHeight(), null);
            g2.setClip(oldClip);
        }

        // 3. Dibujar Borde (Opcional)
        if (showBorder) {
            g2.setStroke(new BasicStroke(borderThickness));
            g2.setColor(borderColor);
            g2.draw(shape);
        }

        g2.dispose();
    }

    // --- Getters y Setters ---

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        repaint();
    }
    
    public void setArc(int arc) {
        this.arcWidth = arc;
        this.arcHeight = arc;
        repaint();
    }

    public void setImage(Image image) {
        this.image = image;
        repaint();
    }

    public void setBorderConfig(Color color, float thickness) {
        this.borderColor = color;
        this.borderThickness = thickness;
        this.showBorder = true;
        repaint();
    }
    
    public void setShowBorder(boolean show) {
        this.showBorder = show;
        repaint();
    }
}