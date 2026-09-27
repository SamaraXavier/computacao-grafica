package cg;

import java.awt.Color;
import java.io.IOException;
import javax.swing.JFrame;

public class Main {

    public static void main(String[] args) throws IOException {
        int largura = 800;
        int altura = 600;

        Canvas canvas = new Canvas(largura, altura);

        JFrame frame = new JFrame("Computacao Grafica Basica - 1a VA");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(canvas);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        canvas.repaint();
    }
}
