package cg;

import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Leitor do formato com rotulos do exemplo da especificacao, uma linha por
 * parametro e em qualquer ordem:
 *
 *   N = 0 1 -1
 *   Iamb = 100 100 100
 *   eta = 1
 *
 * Os nomes nao diferenciam maiusculas de minusculas e ignoram "_" e espacos
 * ("I_amb" e "Iamb" sao o mesmo parametro); o simbolo "η" vale "eta".
 * Linhas vazias ou sem "=" sao ignoradas, e parametros desconhecidos tambem
 * (assim camera e iluminacao podem ficar no mesmo arquivo).
 */
public class ArquivoParametros {

    /** Retorna nome normalizado -> valores. Mapa vazio se o arquivo nao tiver nenhuma linha com "=". */
    public static Map<String, double[]> ler(String caminho) throws IOException {
        Map<String, double[]> parametros = new HashMap<>();

        for (String linha : lerLinhas(caminho)) {
            int igual = linha.indexOf('=');
            if (igual < 0) {
                continue;
            }

            String nome = normalizar(linha.substring(0, igual));
            String[] partes = linha.substring(igual + 1).trim().split("\\s+");
            double[] valores = new double[partes.length];
            try {
                for (int i = 0; i < partes.length; i++) {
                    valores[i] = Double.parseDouble(partes[i]);
                }
            } catch (NumberFormatException e) {
                throw new IOException("valor invalido na linha \"" + linha.trim() + "\"");
            }
            parametros.put(nome, valores);
        }

        return parametros;
    }

    /** Devolve os n valores do parametro, ou lanca IOException se faltar ou tiver tamanho errado. */
    public static double[] exigir(Map<String, double[]> parametros, String nome, int n) throws IOException {
        double[] valores = parametros.get(normalizar(nome));
        if (valores == null) {
            throw new IOException("parametro \"" + nome + "\" nao encontrado no arquivo");
        }
        if (valores.length != n) {
            throw new IOException("parametro \"" + nome + "\" deveria ter " + n + " valor(es), mas tem " + valores.length);
        }
        return valores;
    }

    private static String normalizar(String nome) {
        return nome.replace("﻿", "")
                .trim()
                .toLowerCase()
                .replace("_", "")
                .replace(" ", "")
                .replace("η", "eta");
    }

    private static List<String> lerLinhas(String caminho) throws IOException {
        try {
            return Files.readAllLines(Paths.get(caminho), StandardCharsets.UTF_8);
        } catch (CharacterCodingException e) {
            // arquivo salvo em outra codificacao (ex: ANSI): relê sem validar
            return Files.readAllLines(Paths.get(caminho), StandardCharsets.ISO_8859_1);
        }
    }
}
