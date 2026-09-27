package cg;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public class Canvas extends JPanel {

    private final int width;
    private final int height;
    private final BufferedImage buffer;

    public Canvas(int width, int height) {
        this.width = width;
        this.height = height;
        this.buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        clear(Color.BLACK);
        setPreferredSize(new java.awt.Dimension(width, height));
        setFocusable(true); // necessario para receber eventos de teclado
    }

    public int getWidth2() {
        return width;
    }

    public int getHeight2() {
        return height;
    }

    /** Pinta o fundo inteiro de uma cor (usado antes de redesenhar um frame). */
    public void clear(Color cor) {
        int rgb = cor.getRGB();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                buffer.setRGB(x, y, rgb);
            }
        }
    }

    /** Pinta um pixel colorido na tela. */
    public void setPixel(int x, int y, Color cor) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return; // fora da tela, ignora
        }
        buffer.setRGB(x, y, cor.getRGB());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Isto so exibe o buffer ja pronto.
        g.drawImage(buffer, 0, 0, null);
    }
}
