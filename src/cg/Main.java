package cg;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Scanner;
import javax.swing.JFrame;

public class Main {

    private static final String ARQUIVO_MALHA = "objetos/calice2.byu";
    private static final String ARQUIVO_CAMERA = "camera_exemplo.txt";

    public static void main(String[] args) throws IOException {
        int largura = 512;
        int altura = 512;

        Canvas canvas = new Canvas(largura, altura);

        JFrame frame = new JFrame("Computacao Grafica Basica - 1a VA");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(canvas);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        canvas.requestFocusInWindow();

        Mesh mesh = Mesh.loadFromFile(ARQUIVO_MALHA);
        Camera camera = Camera.loadFromFile(ARQUIVO_CAMERA);
        Renderer renderer = new Renderer(canvas, camera, mesh);
        renderer.desenharMalha();

        // Tecla R recarrega os parametros de camera do arquivo e redesenha,
        // sem precisar fechar/reabrir a aplicacao (atalho rapido pela janela).
        canvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_R) {
                    try {
                        renderer.recarregarCamera(ARQUIVO_CAMERA);
                        renderer.desenharMalha();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        // Menu no console, rodando na thread principal em paralelo com a janela grafica
        Scanner scanner = new Scanner(System.in);
        boolean rodando = true;
        while (rodando) {
            System.out.println("\nMENU");
            System.out.println("0 - Recarregar camera e redesenhar");
            System.out.println("1 - Recarregar objeto e redesenhar");
            System.out.println("2 - Sair");
            System.out.print("Comando: ");

            if (!scanner.hasNextInt()) {
                scanner.next(); // descarta entrada invalida
                System.out.println("Comando invalido.");
                continue;
            }
            int comando = scanner.nextInt();

            switch (comando) {
                case 0:
                    try {
                        renderer.recarregarCamera(ARQUIVO_CAMERA);
                        renderer.desenharMalha();
                        System.out.println("Camera recarregada!");
                    } catch (IOException ex) {
                        System.out.println("Erro ao recarregar camera: " + ex.getMessage());
                    }
                    break;
                case 1:
                    try {
                        renderer.recarregarMalha(ARQUIVO_MALHA);
                        renderer.desenharMalha();
                        System.out.println("Objeto recarregado!");
                    } catch (IOException ex) {
                        System.out.println("Erro ao recarregar objeto: " + ex.getMessage());
                    }
                    break;
                case 2:
                    rodando = false;
                    break;
                default:
                    System.out.println("Comando nao identificado.");
            }
        }

        scanner.close();
        System.exit(0);
    }
}
