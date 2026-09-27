package cg;

import java.awt.Color;
import java.io.IOException;
import javax.swing.JFrame;

public class Main {

    public static void main(String[] args) throws IOException {
        Mesh mesh = Mesh.loadFromFile("malha_exemplo.txt");
        for (Vec3 v : mesh.vertices) {
            System.out.println(v);
        }
        for (int[] t : mesh.triangulos) {
            System.out.println(t[0] + " " + t[1] + " " + t[2]);
        }

        Camera camera = Camera.loadFromFile("camera_exemplo.txt");
        System.out.println(camera.getC());
        System.out.println(camera.getN());
        System.out.println(camera.getV());
        System.out.println(camera.getD());
        System.out.println(camera.getHx());
        System.out.println(camera.getHy());

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
