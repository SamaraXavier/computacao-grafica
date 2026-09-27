package cg;

import static java.lang.Math.floor;

/**
 Reune o Canvas (framebuffer/resolucao) e a Camera para executar o
 pipeline de renderizacao: mundo -> vista -> projecao -> normalizado
 -> tela -> rasterizacao.
 */
public class Renderer {

    private final Canvas canvas;
    private final Camera camera;

    public Renderer(Canvas canvas, Camera camera) {
        this.canvas = canvas;
        this.camera = camera;
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
}
