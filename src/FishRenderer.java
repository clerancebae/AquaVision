import java.awt.*;
import java.awt.geom.*;

public class FishRenderer {

    public static void drawFish(Graphics2D g2d, int x, int y, int width, int height, Color color, boolean facingRight) {
        AffineTransform originalTransform = g2d.getTransform();
        Color originalColor = g2d.getColor();

        // ÖLÇEKLENDİRME
        double scale = 0.85;
        int newW = (int)(width * scale);
        int newH = (int)(height * scale);
        int offsetX = (width - newW) / 2;
        int offsetY = (height - newH) / 2;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.translate(x + offsetX, y + offsetY);

        if (facingRight) {
            g2d.translate(newW, 0);
            g2d.scale(-1, 1);
        }

        g2d.setColor(color);

        // KUYRUK (TAIL)
        Path2D.Double tail = new Path2D.Double();
        tail.moveTo(newW * 0.75, newH * 0.5);
        tail.lineTo(newW, 0);
        tail.quadTo(newW * 0.85, newH * 0.5, newW, newH);
        tail.lineTo(newW * 0.75, newH * 0.5);
        g2d.fill(tail);

        // ÜST YÜZGEÇ
        Path2D.Double topFin = new Path2D.Double();
        topFin.moveTo(newW * 0.45, newH * 0.2);
        topFin.quadTo(newW * 0.7, -newH * 0.3, newW * 0.13, newH * 0.3);
        topFin.lineTo(newW * 0.45, newH * 0.2);
        g2d.fill(topFin);

        // ALT YÜZGEÇ
        Path2D.Double botFin = new Path2D.Double();
        botFin.moveTo(newW * 0.45, newH * 0.8);
        botFin.quadTo(newW * 0.6, newH * 1.1, newW * 0.11, newH * 0.7);
        g2d.fill(botFin);

        // GÖVDE (BODY)
        g2d.fillOval(0, (int)(newH * 0.15), (int)(newW * 0.8), (int)(newH * 0.7));

        g2d.setTransform(originalTransform);
        g2d.setColor(originalColor);
    }
}