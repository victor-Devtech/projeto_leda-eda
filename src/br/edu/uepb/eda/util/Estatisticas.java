package br.edu.uepb.eda.util;

import java.util.Arrays;

/**
 * Representa e calcula as métricas estatísticas descritivas (média, desvio padrão,
 * mediana, mínimo e máximo) a partir das amostras válidas (pós-warm-up).
 */
public final class Estatisticas {

    private final double media;
    private final double desvioPadrao;
    private final double mediana;
    private final double minimo;
    private final double maximo;

    public Estatisticas(double media, double desvioPadrao, double mediana, double minimo, double maximo) {
        this.media = media;
        this.desvioPadrao = desvioPadrao;
        this.mediana = mediana;
        this.minimo = minimo;
        this.maximo = maximo;
    }

    /**
     * Calcula as estatísticas descritivas de um conjunto de valores.
     *
     * @param amostras vetor contendo apenas os tempos válidos (sem warm-up)
     * @return objeto Estatisticas com média, desvio padrão amostral, mediana, mínimo e máximo
     */
    public static Estatisticas calcular(double[] amostras) {
        if (amostras == null || amostras.length == 0) {
            throw new IllegalArgumentException("O vetor de amostras não pode ser vazio.");
        }

        int n = amostras.length;
        double[] ordenadas = Arrays.copyOf(amostras, n);
        Arrays.sort(ordenadas);

        double min = ordenadas[0];
        double max = ordenadas[n - 1];

        double med;
        if (n % 2 == 1) {
            med = ordenadas[n / 2];
        } else {
            med = (ordenadas[(n / 2) - 1] + ordenadas[n / 2]) / 2.0;
        }

        double soma = 0.0;
        for (double v : ordenadas) {
            soma += v;
        }
        double media = soma / n;

        double somaQuadrados = 0.0;
        for (double v : ordenadas) {
            double diff = v - media;
            somaQuadrados += diff * diff;
        }
        double desvio = (n > 1) ? Math.sqrt(somaQuadrados / (n - 1)) : 0.0;

        return new Estatisticas(media, desvio, med, min, max);
    }

    public double getMedia() {
        return media;
    }

    public double getDesvioPadrao() {
        return desvioPadrao;
    }

    public double getMediana() {
        return mediana;
    }

    public double getMinimo() {
        return minimo;
    }

    public double getMaximo() {
        return maximo;
    }
}
