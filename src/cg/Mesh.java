package cg;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * Malha de triangulos carregada do arquivo de texto:
 *
 * <n vertices> <n triangulos>
 * x1 y1 z1
 * ...
 * xn yn zn
 * v1 v2 v3
 * ...
 */
public class Mesh {

    public final Vec3[] vertices;
    public final int[][] triangulos; // cada linha: {i0, i1, i2}, indices 0-baseados apontando para "vertices"

    public Mesh(Vec3[] vertices, int[][] triangulos) {
        this.vertices = vertices;
        this.triangulos = triangulos;
    }

    /**
     * Le o arquivo no formato da especificacao e retorna a malha carregada.
     */
    public static Mesh loadFromFile(String caminho) throws IOException {
        Scanner scanner = new Scanner(new File(caminho));

        int n = scanner.nextInt();
        int k = scanner.nextInt();

        final Vec3[] vertices = new Vec3[n];

        for(int i = 0; i < n; i++) {
            double x = scanner.nextDouble();
            double y = scanner.nextDouble();
            double z = scanner.nextDouble();
            vertices[i] = new Vec3(x, y, z);
        }

        final int[][] triangulos = new int[k][3];
        for(int i = 0; i < k; i++) {
            for(int j = 0; j < 3; j++) {
                triangulos[i][j] = scanner.nextInt() - 1;
            }
        }

        scanner.close();

        return new Mesh(vertices, triangulos);
    }
}
