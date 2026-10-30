package br.edu.uepb.eda.sorting;

/**
 * Implementação da versão estável clássica do algoritmo Counting Sort
 * para arrays de inteiros (int[]) cujos valores pertencem ao intervalo [1, 100000].
 */
public final class CountingSort {

    public static final int VALOR_MAXIMO_PADRAO = 100_000;

    private CountingSort() {
        // Classe utilitária não instanciável
    }

    /**
     * Ordena o vetor utilizando a versão estável clássica do Counting Sort:
     * 1. Contagem de frequência com k = 100000;
     * 2. Soma acumulada (prefix sum) no vetor de contagem;
     * 3. Preenchimento do array de saída percorrendo a entrada de trás para frente;
     * 4. Cópia do array de saída de volta para o array original.
     * Complexidade de tempo Theta(n + k) e espaço auxiliar Theta(n + k).
     *
     * @param a vetor de inteiros no intervalo [1, 100000] a ser ordenado in-place
     */
    public static void sort(int[] a) {
        if (a == null || a.length <= 1) {
            return;
        }

        int n = a.length;
        int k = VALOR_MAXIMO_PADRAO;

        // 1. Contagem das ocorrências de cada valor
        int[] contagem = new int[k + 1];
        for (int i = 0; i < n; i++) {
            contagem[a[i]]++;
        }

        // 2. Acumulado das frequências para determinar as posições finais
        for (int v = 1; v <= k; v++) {
            contagem[v] += contagem[v - 1];
        }

        // 3. Preenchimento do array de saída percorrendo a entrada de trás para frente (garante estabilidade)
        int[] saida = new int[n];
        for (int i = n - 1; i >= 0; i--) {
            int valor = a[i];
            int posicao = contagem[valor] - 1;
            saida[posicao] = valor;
            contagem[valor]--;
        }

        // 4. Cópia de volta para o vetor original
        System.arraycopy(saida, 0, a, 0, n);
    }
}
