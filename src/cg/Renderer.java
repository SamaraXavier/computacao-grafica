package cg;

import java.awt.Color;
import java.io.IOException;
import static java.lang.Math.floor;

/**
 Reune o Canvas (framebuffer/resolucao) e a Camera para executar o
 pipeline de renderizacao: mundo -> vista -> projecao -> normalizado
 -> tela -> rasterizacao.
 */
public class Renderer {

    private final Canvas canvas;
    private Camera camera;
    private Mesh mesh;

    public Renderer(Canvas canvas, Camera camera, Mesh mesh) {
        this.canvas = canvas;
        this.camera = camera;
        this.mesh = mesh;
    }

    /**
     * Converte um ponto normalizado (xs_bar, ys_bar), em [-1,1] x [-1,1],
     * para coordenadas inteiras de tela:
     *   i = floor( (xs_bar + 1) / 2 * resX + 0.5 )
     *   j = floor( resY - (ys_bar + 1) / 2 * resY + 0.5 )
     * resX e resY sao a largura/altura do canvas
     */
    public int[] coordenadasDeTela(Vec2 pontoNormalizado) {
        int i = (int) floor((pontoNormalizado.getX() + 1)/2 * canvas.getWidth2() + 0.5);
        int j = (int) floor(canvas.getHeight2() - (pontoNormalizado.getY()+1)/2 * canvas.getHeight2() + 0.5);
        return new int[]{i, j};
    }

    /**
     * Executa o pipeline inteiro para um unico vertice: mundo -> vista ->
     * projecao -> normalizado -> tela.
     */
    public int[] transformarVertice(Vec3 verticeMundo) {
        Vec3 pontoVista = camera.mundoParaVista(verticeMundo);
        Vec2 pontoProjetado = camera.projetar(pontoVista);
        Vec2 pontoNormalizado = camera.normalizar(pontoProjetado);
        return coordenadasDeTela(pontoNormalizado);
    }

    /**
     * Calcula o x da aresta que
     * vai do ponto de tela (xa,ya) ate (xb,yb), na linha de varredura j:
     *   x(j) = xa + (j - ya) * (xb - xa) / (yb - ya)
     */
    private double xNaAresta(int xa, int ya, int xb, int yb, int j) {
        if (ya == yb) {
            return xa;
        }
        return xa + (j-ya) * ((double) (xb-xa) / (yb-ya));
    }

    /**
     * Rasteriza (preenche de branco) um triangulo cujos 3 vertices ja
     * estao em coordenadas de tela: p0, p1, p2, cada um no formato {i, j}.
     */
    public void rasterizarTriangulo(int[] p0, int[] p1, int[] p2) {
        int[] topo = p0;
        int[] meio = p1;
        int[] base = p2;
        int[] tmp;
        if (topo[1] > meio[1]) {
            tmp = topo; topo = meio; meio = tmp;
        }
        if (meio[1] > base[1]) {
            tmp = meio; meio = base; base = tmp;
        }
        if (topo[1] > meio[1]) {
            tmp = topo; topo = meio; meio = tmp;
        }

        for (int j = topo[1]; j <= base[1]; j++) {
            double xLonga = xNaAresta(topo[0], topo[1], base[0], base[1], j);
            double xCurta = (j <= meio[1])
                    ? xNaAresta(topo[0], topo[1], meio[0], meio[1], j)
                    : xNaAresta(meio[0], meio[1], base[0], base[1], j);

            int xMin = (int) Math.round(Math.min(xLonga, xCurta));
            int xMax = (int) Math.round(Math.max(xLonga, xCurta));

            for (int i = xMin; i <= xMax; i++) {
                canvas.setPixel(i, j, Color.WHITE);
            }
        }
    }

    /**
     * Limpa o canvas de preto, e para cada triangulo da malha atual,
     * transforma seus 3 vertices (mundo -> tela) e rasteriza o triangulo resultante
     */
    public void desenharMalha() {
        canvas.clear(Color.BLACK);

        for (int[] triangulo : mesh.triangulos) {
            int[] p0 = transformarVertice(mesh.vertices[triangulo[0]]);
            int[] p1 = transformarVertice(mesh.vertices[triangulo[1]]);
            int[] p2 = transformarVertice(mesh.vertices[triangulo[2]]);
            rasterizarTriangulo(p0, p1, p2);
        }

        canvas.repaint();
    }

    /**
     * Recarrega os parametros de camera a partir do arquivo, substituindo
     * a camera atualmente usada pelo renderer
     */
    public void recarregarCamera(String caminhoArquivoCamera) throws IOException {
        this.camera = Camera.loadFromFile(caminhoArquivoCamera);
    }

    /**
     * Recarrega a malha a partir do arquivo, substituindo a malha
     * atualmente usada pelo renderer
     */
    public void recarregarMalha(String caminhoArquivoMalha) throws IOException {
        this.mesh = Mesh.loadFromFile(caminhoArquivoMalha);
    }

    /**
     * Normal de um triangulo, com os vertices ja em coordenadas de vista:
     * n = (v2 - v1) x (v3 - v1), normalizada.
     */
    public Vec3 normalDoTriangulo(Vec3 v1, Vec3 v2, Vec3 v3) {
        Vec3 V2SubV1 = v2.sub(v1);
        Vec3 V3SubV1 = v3.sub(v1);

        Vec3 cross = V2SubV1.cross(V3SubV1);

        return cross.normalize();
    }

    /**
     * Normal de cada vertice: soma das normais dos
     * triangulos que possuem aquele vertice, depois normaliza o resultado.
     *
     * verticesVista[i] eh o vertice i da malha ja em coordenadas de vista.
     */
    public Vec3[] normaisDosVertices(Vec3[] verticesVista) {
        Vec3[] somas = new Vec3[verticesVista.length];
        for (int i = 0; i < somas.length; i++) {
            somas[i] = new Vec3(0, 0, 0);
        }

        for (int[] t : mesh.triangulos) {
            Vec3 n = normalDoTriangulo(verticesVista[t[0]], verticesVista[t[1]], verticesVista[t[2]]);
            somas[t[0]] = somas[t[0]].add(n);
            somas[t[1]] = somas[t[1]].add(n);
            somas[t[2]] = somas[t[2]].add(n);
        }

        for (int i = 0; i < somas.length; i++) {
            somas[i] = somas[i].normalize();
        }

        return somas;
    }

    // Dados por vertice, recalculados a cada quadro em prepararCena()
    private Vec3[] verticesVista; // vertice i em coordenadas de vista
    private Vec3[] normaisVertices;// normal unitaria do vertice i (em vista)
    private int[][] verticesTela; // vertice i na tela, no formato {i, j}

    /**
     * Converte um ponto ja em coordenadas de vista para a tela:
     * projecao em perspectiva -> normalizacao -> coordenadas de tela.
     */
    private int[] vistaParaTela(Vec3 pontoVista) {
        Vec2 pontoProjetado = camera.projetar(pontoVista);
        Vec2 pontoNormalizado = camera.normalizar(pontoProjetado);
        return coordenadasDeTela(pontoNormalizado);
    }

    /**
     * Prepara os dados da cena para o quadro atual:
     *   1. verticesVista[i] = camera.mundoParaVista(mesh.vertices[i])
     *   2. normaisVertices = normaisDosVertices(verticesVista)
     *   3. verticesTela[i] = vistaParaTela(verticesVista[i])
     */
    private void prepararCena() {
        int n = mesh.vertices.length;

        verticesVista = new Vec3[n];
        for (int i = 0; i < n; i++) {
            verticesVista[i] = camera.mundoParaVista(mesh.vertices[i]);
        }

        normaisVertices = normaisDosVertices(verticesVista);

        verticesTela = new int[n][];
        for (int i = 0; i < n; i++) {
            verticesTela[i] = vistaParaTela(verticesVista[i]);
        }
    }

    /**
     * Coordenadas baricentricas {alfa, beta, gama} do pixel (px, py) em
     * relacao ao triangulo de tela (p1, p2, p3), cada um no formato {i, j}:
     *
     *   a = x1-x3   b = x2-x3   c = y1-y3   d = y2-y3
     *   det  = a*d - b*c
     *   alfa = (d*(px-x3) - b*(py-y3)) / det
     *   beta = (-c*(px-x3) + a*(py-y3)) / det
     *   gama = 1 - alfa - beta
     *
     */
    public double[] coordenadasBaricentricas(int px, int py, int[] p1, int[] p2, int[] p3) {
        // TODO
        return null;
    }
}
