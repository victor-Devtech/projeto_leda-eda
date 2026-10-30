package br.edu.uepb.eda.util;

import java.util.Arrays;
import java.util.Random;

/**
 * Gerador determinístico de vetores de inteiros para os experimentos.
 * Trabalha exclusivamente com int[] contendo valores no intervalo [1, 100000]
 * e utiliza semente fixa (42) para garantir total reprodutibilidade.
 */
public final class DataGenerator {

    public static final long SEMENTE_PADRAO = 42L;
    public static final int VALOR_MINIMO = 1;
    public static final int VALOR_MAXIMO = 100_000;

    private DataGenerator() {
        // Classe utilitária não instanciável
    }

    /**
     * Gera um vetor conforme o cenário especificado.
     *
     * @param cenario cenário desejado
     * @param n       tamanho do vetor
     * @return novo vetor de inteiros correspondente ao cenário
     */
    public static int[] gerar(Cenario cenario, int n) {
        return switch (cenario) {
            case ALEATORIO -> aleatorio(n);
            case ORDENADO -> ordenado(n);
            case INVERSO -> inverso(n);
            case PARCIAL -> parcialmenteOrdenado(n);
        };
    }

    /**
     * Gera um vetor de tamanho n com valores aleatórios no intervalo [1, 100000].
     * Repetições são permitidas.
     *
     * @param n tamanho do vetor
     * @return vetor preenchido aleatoriamente com semente 42
     */
    public static int[] aleatorio(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("O tamanho do vetor não pode ser negativo: " + n);
        }
        Random random = new Random(SEMENTE_PADRAO);
        int[] vetor = new int[n];
        for (int i = 0; i < n; i++) {
            vetor[i] = random.nextInt(VALOR_MAXIMO) + VALOR_MINIMO;
        }
        return vetor;
    }

    /**
     * Gera um vetor de tamanho n ordenado em ordem não-decrescente,
     * partindo do vetor aleatório padrão ordenado com Arrays.sort.
     *
     * @param n tamanho do vetor
     * @return vetor ordenado
     */
    public static int[] ordenado(int n) {
        int[] vetor = aleatorio(n);
        Arrays.sort(vetor);
        return vetor;
    }

    /**
     * Gera um vetor de tamanho n em ordem inversa (não-crescente),
     * invertendo o vetor obtido por ordenado(n).
     *
     * @param n tamanho do vetor
     * @return vetor em ordem inversa
     */
    public static int[] inverso(int n) {
        int[] vetor = ordenado(n);
        for (int i = 0, j = n - 1; i < j; i++, j--) {
            int temp = vetor[i];
            vetor[i] = vetor[j];
            vetor[j] = temp;
        }
        return vetor;
    }

    /**
     * Gera um vetor parcialmente ordenado de tamanho n.
     * Parte de ordenado(n), escolhe m = round(0.15 * n) posições distintas
     * aleatórias embaralhando um array de índices 0..n-1 com Fisher-Yates e
     * tomando os m primeiros índices, e em seguida embaralha com Fisher-Yates
     * apenas os valores dessas m posições entre si.
     *
     * @param n tamanho do vetor
     * @return vetor parcialmente ordenado
     */
    public static int[] parcialmenteOrdenado(int n) {
        int[] vetor = ordenado(n);
        int m = (int) Math.round(0.15 * n);
        if (m <= 1) {
            return vetor;
        }

        Random random = new Random(SEMENTE_PADRAO);
        int[] indices = new int[n];
        for (int i = 0; i < n; i++) {
            indices[i] = i;
        }

        // Embaralha todos os índices 0..n-1 com Fisher-Yates para selecionar m posições distintas
        for (int i = n - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = indices[i];
            indices[i] = indices[j];
            indices[j] = temp;
        }

        // Embaralha com Fisher-Yates apenas os valores das m primeiras posições sorteadas entre si
        for (int i = m - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int posI = indices[i];
            int posJ = indices[j];
            int temp = vetor[posI];
            vetor[posI] = vetor[posJ];
            vetor[posJ] = temp;
        }

        return vetor;
    }
}
