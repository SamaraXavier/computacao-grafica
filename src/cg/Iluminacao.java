package cg;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

/**
 * Parametros de iluminacao, lidos de um arquivo de texto,
 * uma linha por parametro, nesta ordem:
 *
 * Iamb_r Iamb_g Iamb_b (cor da luz ambiental, 0..255)
 * Ka (coeficiente ambiental, 0..1)
 * Il_r Il_g Il_b (cor da luz, 0..255)
 * Plx Ply Plz (posicao da fonte de luz, em coordenadas de vista)
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

    /**
     * Le os parametros de iluminacao. Aceita o formato com rotulos ("Iamb = 100 100 100",
     * em qualquer ordem, ignorando parametros que nao sao da iluminacao) ou o formato
     * posicional descrito acima.
     */
    public static Iluminacao loadFromFile(String caminho) throws IOException {
        Map<String, double[]> rotulado = ArquivoParametros.ler(caminho);
        if (!rotulado.isEmpty()) {
            double[] Iamb = ArquivoParametros.exigir(rotulado, "Iamb", 3);
            double Ka = ArquivoParametros.exigir(rotulado, "Ka", 1)[0];
            double[] Il = ArquivoParametros.exigir(rotulado, "Il", 3);
            double[] Pl = ArquivoParametros.exigir(rotulado, "Pl", 3);
            double[] Kd = ArquivoParametros.exigir(rotulado, "Kd", 3);
            double[] Od = ArquivoParametros.exigir(rotulado, "Od", 3);
            double Ks = ArquivoParametros.exigir(rotulado, "Ks", 1)[0];
            double eta = ArquivoParametros.exigir(rotulado, "eta", 1)[0];
            return new Iluminacao(new Vec3(Iamb[0], Iamb[1], Iamb[2]), Ka, new Vec3(Il[0], Il[1], Il[2]),
                    new Vec3(Pl[0], Pl[1], Pl[2]), new Vec3(Kd[0], Kd[1], Kd[2]), new Vec3(Od[0], Od[1], Od[2]), Ks, eta);
        }

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
