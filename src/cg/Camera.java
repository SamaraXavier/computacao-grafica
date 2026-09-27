package cg;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 Parametros da camera virtual, lidos de um arquivo de texto:
 Cx Cy Cz
 Nx Ny Nz
 Vx Vy Vz
 d
 hx
 hy
 */
public class Camera {

    private final Vec3 C;
    private Vec3 N; // sera sobrescrito pela versao ortonormal
    private Vec3 V; // idem
    private Vec3 U; // terceiro vetor da base, calculado (nao vem do arquivo)
    private final double d;
    private final double hx;
    private final double hy;

    public Camera(Vec3 C, Vec3 N, Vec3 V, double d, double hx, double hy) {
        this.C = C;
        this.N = N;
        this.V = V;
        this.d = d;
        this.hx = hx;
        this.hy = hy;
        ortonormalizarBase();
    }

    /**
     * Recalcula a base ortonormal {U, V, N} a partir dos vetores N e V
     * atualmente guardados:
     *
     * 1. Ortogonalizar V em relacao a N (Gram-Schmidt):
     *    V' = V - (<V,N> / <N,N>) * N
     * 2. U = N x V'
     * 3. Normalizar os tres vetores independentemente e reatribuir
     *    this.N, this.V, this.U com as versoes normalizadas.
     */
    public void ortonormalizarBase() {
        double dotProduct1 = this.V.dot(this.N);
        double dotProduct2 = this.N.dot(this.N);
        double div =dotProduct1/dotProduct2;

        Vec3 vLinha = this.V.sub(this.N.scale(div));

        Vec3 uNaoNormalizado = this.N.cross(vLinha);

        // Normalizacao
        this.N = this.N.normalize();
        this.V = vLinha.normalize();
        this.U = uNaoNormalizado.normalize();
    }

    /**
     * Converte um ponto de coordenadas mundiais (p) para coordenadas de vista:
     *   xv = <P - C, U>
     *   yv = <P - C, V>
     *   zv = <P - C, N>
     */
    public Vec3 mundoParaVista(Vec3 p) {
        double xv = p.sub(this.C).dot(this.U);
        double yv = p.sub(this.C).dot(this.V);
        double zv = p.sub(this.C).dot(this.N);
        return new Vec3(xv, yv, zv);
    }

    /**
     * Projeta um ponto em coordenadas de vista (xv, yv, zv) sobre o plano
     * de projecao a distancia d do foco:
     *   xs = d * (xv / zv)
     *   ys = d * (yv / zv)
     */
    public Vec2 projetar(Vec3 pontoVista) {
        double xs = this.d * (pontoVista.getX() / pontoVista.getZ());
        double ys = this.d * (pontoVista.getY() / pontoVista.getZ());
        return new Vec2(xs, ys);
    }

    public Vec2 normalizar(Vec2 pontoProjetado) {
        double xs = pontoProjetado.getX() / this.hx;
        double ys = pontoProjetado.getY() / this.hy;
        return new Vec2(xs, ys);
    }

    /**
     Le o arquivo de parametros de camera no formato descrito acima.
     */
    public static Camera loadFromFile(String caminho) throws IOException {
        Scanner scanner = new Scanner(new File(caminho));

        Vec3 C = new Vec3(scanner.nextDouble(), scanner.nextDouble(), scanner.nextDouble());
        Vec3 N = new Vec3(scanner.nextDouble(), scanner.nextDouble(), scanner.nextDouble());
        Vec3 V = new Vec3(scanner.nextDouble(), scanner.nextDouble(), scanner.nextDouble());

        double d = scanner.nextDouble();
        double hx = scanner.nextDouble();
        double hy = scanner.nextDouble();

        scanner.close();

        return new Camera(C, N, V, d, hx, hy);
    }

    public Vec3 getC() {
        return this.C;
    }

    public Vec3 getN() {
        return this.N;
    }

    public void setN(Vec3 n) {
        this.N = n;
    }

    public Vec3 getV() {
        return this.V;
    }

    public void setV(Vec3 v) {
        this.V = v;
    }

    public Vec3 getU() {
        return this.U;
    }

    public void setU(Vec3 u) {
        this.U = u;
    }

    public double getD() {
        return this.d;
    }

    public double getHx() {
        return this.hx;
    }

    public double getHy() {
        return this.hy;
    }
}
