package br.edu.uepb.eda.sorting;

/**
 * Implementação clássica top-down recursiva do algoritmo Merge Sort
 * para arrays de inteiros (int[]). Utiliza array auxiliar e comparação <=
 * na etapa de intercalação (merge) para garantir estabilidade.
 */
public final class MergeSort {

    private MergeSort() {
        // Classe utilitária não instanciável
    }

    /**
     * Ordena o vetor utilizando o Merge Sort clássico top-down recursivo.
     * Complexidade Theta(n log n) em todos os cenários e espaço auxiliar Theta(n).
     *
     * @param a vetor de inteiros a ser ordenado in-place
     */
    public static void classico(int[] a) {
        if (a == null || a.length <= 1) {
            return;
        }
        int[] aux = new int[a.length];
        mergeSort(a, aux, 0, a.length - 1);
    }

    private static void mergeSort(int[] a, int[] aux, int esq, int dir) {
        if (esq >= dir) {
            return;
        }
        int meio = esq + (dir - esq) / 2;
        mergeSort(a, aux, esq, meio);
        mergeSort(a, aux, meio + 1, dir);
        intercalar(a, aux, esq, meio, dir);
    }

    private static void intercalar(int[] a, int[] aux, int esq, int meio, int dir) {
        for (int k = esq; k <= dir; k++) {
            aux[k] = a[k];
        }

        int i = esq;
        int j = meio + 1;
        for (int k = esq; k <= dir; k++) {
            if (i > meio) {
                a[k] = aux[j++];
            } else if (j > dir) {
                a[k] = aux[i++];
            } else if (aux[i] <= aux[j]) {
                // Usa <= para preservar a estabilidade entre elementos iguais
                a[k] = aux[i++];
            } else {
                a[k] = aux[j++];
            }
        }
    }

    /**
     * Alias para {@link #classico(int[])}.
     */
    public static void sort(int[] a) {
        classico(a);
    }
}
