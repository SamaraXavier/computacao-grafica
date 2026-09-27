package cg;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 Ponto/vetor em R3. Usado tanto para vertices da malha quanto
 para os vetores da camera (C, N, V, U).
 */
public class Vec3 {

    private final double x, y, z;

    public Vec3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // Soma de vetores: (x1,y1,z1) + (x2,y2,z2) = (x1+x2, y1+y2, z1+z2)
    public Vec3 add(Vec3 outro) {
        return new Vec3((this.x + outro.x), (this.y + outro.y), (this.z + outro.z));
    }

    // Subtracao de pontos/vetores: this - outro
    public Vec3 sub(Vec3 outro) {
        return new Vec3((this.x - outro.x), (this.y - outro.y), (this.z - outro.z));
    }

    // Multiplicacao por escalar: k * (x,y,z)
    public Vec3 scale(double k) {
        return new Vec3((this.x * k), (this.y * k), (this.z * k));
    }

    // Produto escalar <this, outro>
    public double dot(Vec3 outro) {
        return (this.x * outro.x) + (this.y * outro.y) + (this.z * outro.z);
    }

    // Produto vetorial this x outro
    public Vec3 cross(Vec3 outro) {
        double newX = (this.y * outro.z) - (this.z * outro.y);
        double newY = (this.z * outro.x) - (this.x * outro.z);
        double newZ = (this.x * outro.y) - (this.y * outro.x);
        return new Vec3(newX, newY, newZ);
    }

    /** Norma: ||v|| = sqrt(<v,v>) */
    public double length() {
        return sqrt(pow(this.x, 2) + pow(this.y, 2) + pow(this.z, 2));
    }

    /** Vetor normalizado (norma 1): v / ||v|| */
    public Vec3 normalize() {
        double length = length();
        return new Vec3(this.x / length, this.y / length, this.z / length);
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }
}
