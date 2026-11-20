package cg;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * Parametros de iluminacao, lidos de um arquivo de texto,
 * uma linha por parametro, nesta ordem:
 *
 * Iamb_r Iamb_g Iamb_b (cor da luz ambiental, 0..255)
 * Ka (coeficiente ambiental, 0..1)
 * Il_r Il_g Il_b (cor da luz, 0..255)
 * Plx Ply Plz (posicao da fonte de luz, coordenadas do mundo)
 * Kd_r Kd_g Kd_b (coeficiente difuso, 0..1)
 * Od_r Od_g Od_b (cor difusa do objeto, 0..1)
 * Ks (coeficiente especular, 0..1)
 * eta (expoente especular, >= 1)
 */
public class Iluminacao {

    private final Vec3 Iamb;
    private final double Ka;
    private final Vec3 Il;
    private final Vec3 Pl;
    private final Vec3 Kd;
    private final Vec3 Od;
    private final double Ks;
    private final double eta;

    public Iluminacao(Vec3 Iamb, double Ka, Vec3 Il, Vec3 Pl, Vec3 Kd, Vec3 Od, double Ks, double eta) {
        this.Iamb = Iamb;
        this.Ka = Ka;
        this.Il = Il;
        this.Pl = Pl;
        this.Kd = Kd;
        this.Od = Od;
        this.Ks = Ks;
        this.eta = eta;
    }

    /** Le o arquivo */
    public static Iluminacao loadFromFile(String caminho) throws IOException {
        Scanner scanner = new Scanner(new File(caminho));

        Vec3 Iamb = new Vec3(scanner.nextDouble(), scanner.nextDouble(), scanner.nextDouble());

        double Ka = scanner.nextDouble();

        Vec3 Il = new Vec3(scanner.nextDouble(), scanner.nextDouble(), scanner.nextDouble());
        Vec3 Pl = new Vec3(scanner.nextDouble(), scanner.nextDouble(), scanner.nextDouble());
        Vec3 Kd = new Vec3(scanner.nextDouble(), scanner.nextDouble(), scanner.nextDouble());
        Vec3 Od = new Vec3(scanner.nextDouble(), scanner.nextDouble(), scanner.nextDouble());

        double Ks = scanner.nextDouble();
        double eta = scanner.nextDouble();

        scanner.close();

        return new Iluminacao(Iamb, Ka, Il, Pl, Kd, Od, Ks, eta);
    }

    public Vec3 getIamb() { return Iamb; }
    public double getKa() { return Ka; }
    public Vec3 getIl() { return Il; }
    public Vec3 getPl() { return Pl; }
    public Vec3 getKd() { return Kd; }
    public Vec3 getOd() { return Od; }
    public double getKs() { return Ks; }
    public double getEta() { return eta; }
}
