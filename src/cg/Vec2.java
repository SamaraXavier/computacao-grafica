package cg;

/**
Usado para representar pontos ja projetados: (xs, ys), coordenadas
 normalizadas e coordenadas de tela.
 */
public class Vec2 {

    private final double x, y;

    public Vec2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
