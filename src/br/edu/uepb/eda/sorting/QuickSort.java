package br.edu.uepb.eda.sorting;

import java.util.Random;

/**
 * Implementações do algoritmo Quick Sort para arrays de inteiros (int[]).
 * Contém a versão do slide (particionamento "à la Hoare" com pivô fixo no primeiro elemento)
 * e a versão slideShuffle (embaralhamento Fisher-Yates com Random(42) dentro do tempo
 * medido, seguido do mesmo particionamento do slide).
 */
public final class QuickSort {

    private static final long SEMENTE_SHUFFLE = 42L;

    private QuickSort() {
        // Classe utilitária não instanciável
    }

    /**
     * Versão do slide: Quick Sort recursivo utilizando particionamento "à la Hoare"
     * com pivô fixo no primeiro elemento da partição (a[esq]).
     * Degenera para O(n^2) e profundidade de recursão O(n) em vetores já ordenados ou em ordem inversa.
     *
     * @param a vetor de inteiros a ser ordenado in-place
     */
    public static void slide(int[] a) {
        if (a == null || a.length <= 1) {
            return;
        }
        quickSort(a, 0, a.length - 1);
    }

    /**
     * Versão slide + shuffle: realiza o embaralhamento de Fisher-Yates com semente fixa (42)
     * dentro do tempo medido antes de invocar o mesmo Quick Sort do slide.
     * Evita a degeneração sistemática em entradas ordenadas ou inversas.
     *
     * @param a vetor de inteiros a ser ordenado in-place
     */
    public static void slideShuffle(int[] a) {
        if (a == null || a.length <= 1) {
            return;
        }
        embaralharFisherYates(a);
        quickSort(a, 0, a.length - 1);
    }

    private static void embaralharFisherYates(int[] a) {
        Random random = new Random(SEMENTE_SHUFFLE);
        for (int i = a.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = a[i];
            a[i] = a[j];
            a[j] = temp;
        }
    }

    private static void quickSort(int[] a, int esq, int dir) {
        if (esq < dir) {
            int indicePivo = particionar(a, esq, dir);
            quickSort(a, esq, indicePivo - 1);
            quickSort(a, indicePivo + 1, dir);
        }
    }

    /**
     * Particionamento do slide ("à la Hoare") utilizando o primeiro elemento (a[esq]) como pivô.
     */
    private static int particionar(int[] a, int esq, int dir) {
        int pivo = a[esq];
        int i = esq + 1;
        int j = dir;
        while (i <= j) {
            if (a[i] <= pivo) {
                i++;
            } else if (a[j] > pivo) {
                j--;
            } else {
                int temp = a[i];
                a[i] = a[j];
                a[j] = temp;
            }
        }
        int temp = a[esq];
        a[esq] = a[j];
        a[j] = temp;
        return j;
    }

    /**
     * Alias para {@link #slide(int[])}.
     */
    public static void sortSlide(int[] a) {
        slide(a);
    }

    /**
     * Alias para {@link #slideShuffle(int[])}.
     */
    public static void sortSlideShuffle(int[] a) {
        slideShuffle(a);
    }
}
