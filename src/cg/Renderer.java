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
    private Iluminacao iluminacao;
    private Vec3 plVista; // posicao da luz em coordenadas de vista (atribuida em prepararCena)

    public Renderer(Canvas canvas, Camera camera, Mesh mesh, Iluminacao iluminacao) {
        this.canvas = canvas;
        this.camera = camera;
        this.mesh = mesh;
        this.iluminacao = iluminacao;
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
            return xb;
        }
        return xa + (j-ya) * ((double) (xb-xa) / (yb-ya));
    }

    /**
     * Rasteriza o triangulo formado pelos vertices de indices i0, i1, i2
     * Usa os dados preparados em prepararCena():
     * verticesTela (posicao na tela), verticesVista (posicao em vista).
     */
    public void rasterizarTriangulo(int i0, int i1, int i2) {
        Vec3 v0 = verticesVista[i0];
        Vec3 v1 = verticesVista[i1];
        Vec3 v2 = verticesVista[i2];

        // vertice atras da camera (ou no plano dela)
        if (v0.getZ() <= 0 || v1.getZ() <= 0 || v2.getZ() <= 0) {
            return;
        }

        int[] p0 = verticesTela[i0];
        int[] p1 = verticesTela[i1];
        int[] p2 = verticesTela[i2];

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

        int largura = canvas.getWidth2();
        int altura = canvas.getHeight2();

        // so as linhas e colunas dentro da tela interessam
        int jInicio = Math.max(topo[1], 0);
        int jFim = Math.min(base[1], altura - 1);

        for (int j = jInicio; j <= jFim; j++) {
            double xLonga = xNaAresta(topo[0], topo[1], base[0], base[1], j);
            double xCurta = (j <= meio[1])
                    ? xNaAresta(topo[0], topo[1], meio[0], meio[1], j)
                    : xNaAresta(meio[0], meio[1], base[0], base[1], j);

            // o limite eh aplicado antes do (int)
            long xMinL = Math.round(Math.min(xLonga, xCurta));
            long xMaxL = Math.round(Math.max(xLonga, xCurta));
            int xMin = (int) Math.max(0, Math.min(xMinL, largura));
            int xMax = (int) Math.min(largura - 1, Math.max(xMaxL, -1));

            for (int i = xMin; i <= xMax; i++) {
                double[] bar = coordenadasBaricentricas(i, j, p0, p1, p2);
                if (bar != null && bar[0] >= 0 && bar[1] >= 0 && bar[2] >= 0) {
                    if (i < 0 || i >= canvas.getWidth2() || j < 0 || j >= canvas.getHeight2()) {
                        continue;
                    }

                    Vec3 p = v0.scale(bar[0]).add(v1.scale(bar[1])).add(v2.scale(bar[2]));
                    double z = p.getZ();

                    if (z < zBuffer[j][i]) {
                        zBuffer[j][i] = z;

                        Vec3 n = normaisVertices[i0].scale(bar[0])
                                .add(normaisVertices[i1].scale(bar[1]))
                                .add(normaisVertices[i2].scale(bar[2]));

                        canvas.setPixel(i, j, calcularCor(p, n));
                    }
                }
            }
        }
    }

    // z-buffer: zBuffer[j][i] = menor profundidade (z em vista) ja desenhada no pixel (i, j)
    private double[][] zBuffer;

    /** Aloca o z-buffer do tamanho da tela e inicializa tudo com +infinito. */
    private void prepararZBuffer() {
        zBuffer = new double[canvas.getHeight2()][canvas.getWidth2()];
        for (double[] linha : zBuffer) {
            java.util.Arrays.fill(linha, Double.POSITIVE_INFINITY);
        }
    }

    /**
     * Ordem em que os triangulos serao rasterizados:
     * pelo z do baricentro de cada triangulo em coordenadas de vista, do mais
     * proximo da camera (menor z) para o mais distante.
     *
     *   zBaricentro(t) = (z do vertice 0 + z do vertice 1 + z do vertice 2) / 3
     *
     * Retorna os INDICES dos triangulos (posicoes em mesh.triangulos) nessa ordem.
     * Usa verticesVista, preparado em prepararCena().
     */
    private Integer[] ordenarTriangulosPeloBaricentro() {
        int n = mesh.triangulos.length;

        double[] zBaricentro = new double[n];
        Integer[] ordem = new Integer[n];
        for (int i = 0; i < n; i++) {
            int[] t = mesh.triangulos[i];
            zBaricentro[i] = (verticesVista[t[0]].getZ() + verticesVista[t[1]].getZ() + verticesVista[t[2]].getZ()) / 3.0;
            ordem[i] = i;
        }

        java.util.Arrays.sort(ordem, (a, b) -> Double.compare(zBaricentro[a], zBaricentro[b]));
        return ordem;
    }

    /**
     * Limpa o canvas de preto, prepara a cena e o z-buffer do quadro, e
     * rasteriza os triangulos da malha atual do mais proximo ao mais distante.
     */
    public synchronized void desenharMalha() {
        canvas.clear(Color.BLACK);
        prepararCena();
        prepararZBuffer();

        for (int idx : ordenarTriangulosPeloBaricentro()) {
            int[] triangulo = mesh.triangulos[idx];
            rasterizarTriangulo(triangulo[0], triangulo[1], triangulo[2]);
        }

        canvas.repaint();
    }

    /**
     * Cor RGB do ponto p (coordenadas de vista) com normal n, pelo modelo de
     * iluminacao de Phong:   I = Ia + Id + Is
     *
     *   Ia = Ka * Iamb
     *   Id = <N,L> * (Kd * Od * Il)  ('*' entre vetores = componente a componente)
     *   Is = <R,V>^eta * Ks * Il
     *
     * Vetores (todos normalizados): N = n, L = plVista - p, V = -p (a camera
     * esta na origem em coordenadas de vista), R = 2<N,L>N - L.
     *
     * Casos especiais:
     *   - se <N,L> < 0: se <V,N> < 0 entao N = -N (e recalcule <N,L>);
     *                   senao a luz eh desconsiderada (Id = Is = 0)
     *   - se <V,R> < 0: a componente especular eh desconsiderada (Is = 0)
     *   - cada canal de I que passar de 255 vira 255
     */
    public Color calcularCor(Vec3 p, Vec3 n) {
        Vec3 N = n.normalize();
        Vec3 L = plVista.sub(p).normalize();
        Vec3 V = p.scale(-1).normalize();

        // Ia = Ka * Iamb
        Vec3 I = iluminacao.getIamb().scale(iluminacao.getKa());

        double nl = N.dot(L);
        if (nl < 0) {
            if (V.dot(N) < 0) {
                N = N.scale(-1);
                nl = -nl;
            } else {
                return paraCor(I); // luz desconsiderada: so ambiente
            }
        }

        // Id = <N,L> * (Kd * Od * Il)
        Vec3 Id = iluminacao.getKd().mul(iluminacao.getOd()).mul(iluminacao.getIl()).scale(nl);
        I = I.add(Id);

        // Is = <R,V>^eta * Ks * Il   (so se <V,R> >= 0)
        Vec3 R = N.scale(2 * nl).sub(L);
        double rv = R.dot(V);
        if (rv >= 0) {
            Vec3 Is = iluminacao.getIl().scale(iluminacao.getKs() * Math.pow(rv, iluminacao.getEta()));
            I = I.add(Is);
        }

        return paraCor(I);
    }

    /** Limita cada canal a 255 e arredonda para o inteiro mais proximo. */
    private Color paraCor(Vec3 i) {
        int r = (int) Math.round(Math.min(255, i.getX()));
        int g = (int) Math.round(Math.min(255, i.getY()));
        int b = (int) Math.round(Math.min(255, i.getZ()));
        return new Color(r, g, b);
    }

    /**
     * Recarrega os parametros de camera e de iluminacao do arquivo. Le os dois
     * antes de substituir qualquer um, entao se o arquivo tiver erro nada muda.
     */
    public synchronized void recarregarParametros(String caminhoArquivo) throws IOException {
        Camera novaCamera = Camera.loadFromFile(caminhoArquivo);
        Iluminacao novaIluminacao = Iluminacao.loadFromFile(caminhoArquivo);
        this.camera = novaCamera;
        this.iluminacao = novaIluminacao;
    }

    /**
     * Recarrega a malha a partir do arquivo, substituindo a malha
     * atualmente usada pelo renderer
     */
    public synchronized void recarregarMalha(String caminhoArquivoMalha) throws IOException {
        Mesh nova = Mesh.loadFromFile(caminhoArquivoMalha);

        // indice de triangulo fora de 1..n quebraria o desenho: nesse caso a malha atual eh mantida
        for (int[] t : nova.triangulos) {
            for (int indice : t) {
                if (indice < 0 || indice >= nova.vertices.length) {
                    throw new IOException("triangulo com indice de vertice invalido (" + (indice + 1)
                            + "); os vertices vao de 1 a " + nova.vertices.length);
                }
            }
        }

        this.mesh = nova;
    }

    /**
     * Normal de um triangulo, com os vertices ja em coordenadas de vista,
     * apontando para FORA do objeto: n = (v3 - v1) x (v2 - v1), normalizada.
     *
     * A ordem eh essa porque a mudanca mundo -> vista inverte a orientacao
     * (det[U V N] = -1, como num espelho): em vista, (v2 - v1) x (v3 - v1)
     * apontaria para dentro nos objetos fornecidos.
     */
    public Vec3 normalDoTriangulo(Vec3 v1, Vec3 v2, Vec3 v3) {
        Vec3 V2SubV1 = v2.sub(v1);
        Vec3 V3SubV1 = v3.sub(v1);

        Vec3 cross = V3SubV1.cross(V2SubV1);

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

        // Pl ja eh dado em coordenadas de vista: eh usado direto, sem conversao
        plVista = iluminacao.getPl();
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
        double a = p1[0] - p3[0];
        double b = p2[0] - p3[0];
        double c = p1[1] - p3[1];
        double d = p2[1] - p3[1];

        double det = a * d - b * c;
        if (det == 0) {
            return null;
        }

        double dx = px - p3[0];
        double dy = py - p3[1];

        double alfa = (d * dx - b * dy) / det;
        double beta = (-c * dx + a * dy) / det;
        double gama = 1 - alfa - beta;

        return new double[]{alfa, beta, gama};
    }
}
