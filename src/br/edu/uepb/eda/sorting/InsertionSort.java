package br.edu.uepb.eda.sorting;

/**
 * Implementação do algoritmo Insertion Sort para arrays de inteiros (int[]).
 * Insere cada elemento na posição adequada do prefixo já ordenado por meio
 * de deslocamentos sucessivos à direita, parando quando a[j] <= chave.
 */
public final class InsertionSort {

    private InsertionSort() {
        // Classe utilitária não instanciável
    }

    /**
     * Ordena o vetor in-place usando Insertion Sort com deslocamento.
     * Estável, O(n) no melhor caso (vetor ordenado) e O(n^2) no caso médio e pior caso.
     *
     * @param a vetor de inteiros a ser ordenado in-place
     */
    public static void sort(int[] a) {
        if (a == null || a.length <= 1) {
            return;
        }
        int n = a.length;
        for (int i = 1; i < n; i++) {
            int chave = a[i];
            int j = i - 1;
            while (j >= 0 && a[j] > chave) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = chave;
        }
    }
}
