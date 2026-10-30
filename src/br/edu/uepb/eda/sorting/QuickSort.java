// TODO: Conferir o particionamento com o slide do professor da disciplina.
package br.edu.uepb.eda.sorting;

import java.util.Random;

/**
 * Implementações do algoritmo Quick Sort para arrays de inteiros (int[]).
 * Contém a versão do slide (particionamento de Lomuto com pivô fixo no último elemento)
 * e a versão slideShuffle (embaralhamento Fisher-Yates com Random(42) dentro do tempo
 * medido, seguido do mesmo particionamento de Lomuto).
 */
public final class QuickSort {

    private static final long SEMENTE_SHUFFLE = 42L;

    private QuickSort() {
        // Classe utilitária não instanciável
    }

    /**
     * Versão do slide: Quick Sort recursivo utilizando particionamento de Lomuto
     * com pivô fixo no último elemento da partição (a[dir]).
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
     * dentro do tempo medido antes de invocar o mesmo Quick Sort de Lomuto.
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
            int indicePivo = particionarLomuto(a, esq, dir);
            quickSort(a, esq, indicePivo - 1);
            quickSort(a, indicePivo + 1, dir);
        }
    }

    /**
     * Particionamento de Lomuto utilizando o último elemento (a[dir]) como pivô.
     */
    private static int particionarLomuto(int[] a, int esq, int dir) {
        int pivo = a[dir];
        int i = esq - 1;
        for (int j = esq; j < dir; j++) {
            if (a[j] <= pivo) {
                i++;
                int temp = a[i];
                a[i] = a[j];
                a[j] = temp;
            }
        }
        int temp = a[i + 1];
        a[i + 1] = a[dir];
        a[dir] = temp;
        return i + 1;
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
