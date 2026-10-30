package br.edu.uepb.eda.util;

/**
 * Utilitário responsável por verificar a correção da ordenação fora do tempo medido.
 * Confere se o tamanho foi preservado, se a soma (em long) de todos os elementos é
 * idêntica à soma da entrada original e se os elementos estão em ordem não-decrescente.
 */
public final class Verificador {

    private Verificador() {
        // Classe utilitária não instanciável
    }

    /**
     * Calcula a soma de todos os elementos de um vetor usando acumulador long
     * para evitar estouro aritmético (overflow).
     *
     * @param vetor vetor de entrada
     * @return soma dos elementos em long
     */
    public static long calcularSoma(int[] vetor) {
        long soma = 0L;
        for (int valor : vetor) {
            soma += valor;
        }
        return soma;
    }

    /**
     * Verifica se o vetor ordenado está íntegro e em ordem não-decrescente.
     * Caso alguma condição falhe, lança IllegalStateException detalhando
     * o algoritmo, o cenário e o tamanho n.
     *
     * @param vetorOrdenado   vetor resultante após a execução do algoritmo
     * @param tamanhoEsperado tamanho da entrada original
     * @param somaEsperada    soma (long) dos elementos da entrada original
     * @param algoritmo       nome do algoritmo avaliado
     * @param cenario         cenário da entrada
     * @param n               tamanho configurado do experimento
     */
    public static void verificarOrdenacao(
            int[] vetorOrdenado,
            int tamanhoEsperado,
            long somaEsperada,
            String algoritmo,
            String cenario,
            int n) {

        if (vetorOrdenado == null) {
            throw new IllegalStateException(String.format(
                    "Falha na verificação [algoritmo=%s, cenario=%s, n=%d]: vetor resultante é nulo.",
                    algoritmo, cenario, n));
        }

        if (vetorOrdenado.length != tamanhoEsperado) {
            throw new IllegalStateException(String.format(
                    "Falha na verificação [algoritmo=%s, cenario=%s, n=%d]: tamanho divergente (esperado=%d, obtido=%d).",
                    algoritmo, cenario, n, tamanhoEsperado, vetorOrdenado.length));
        }

        long somaObtida = 0L;
        for (int i = 0; i < vetorOrdenado.length; i++) {
            somaObtida += vetorOrdenado[i];
            if (i > 0 && vetorOrdenado[i - 1] > vetorOrdenado[i]) {
                throw new IllegalStateException(String.format(
                        "Falha na verificação [algoritmo=%s, cenario=%s, n=%d]: fora de ordem na posição %d (%d > %d).",
                        algoritmo, cenario, n, i, vetorOrdenado[i - 1], vetorOrdenado[i]));
            }
        }

        if (somaObtida != somaEsperada) {
            throw new IllegalStateException(String.format(
                    "Falha na verificação [algoritmo=%s, cenario=%s, n=%d]: soma divergente (esperada=%d, obtida=%d).",
                    algoritmo, cenario, n, somaEsperada, somaObtida));
        }
    }
}
