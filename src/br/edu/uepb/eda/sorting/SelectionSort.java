package br.edu.uepb.eda.sorting;

/**
 * Implementações do algoritmo Selection Sort para arrays de inteiros (int[]).
 * Contém a versão clássica do slide (troca direta com o mínimo, não estável)
 * e a versão estável (desloca os elementos com shift em vez de trocar diretamente).
 */
public final class SelectionSort {

    private SelectionSort() {
        // Classe utilitária não instanciável
    }

    /**
     * Versão do slide: localiza o menor elemento do sufixo não ordenado
     * e realiza uma troca direta com a posição atual i.
     * Complexidade Theta(n^2) em todos os cenários, não estável.
     *
     * @param a vetor de inteiros a ser ordenado in-place
     */
    public static void slide(int[] a) {
        if (a == null || a.length <= 1) {
            return;
        }
        int n = a.length;
        for (int i = 0; i < n - 1; i++) {
            int indiceMinimo = i;
            for (int j = i + 1; j < n; j++) {
                if (a[j] < a[indiceMinimo]) {
                    indiceMinimo = j;
                }
            }
            if (indiceMinimo != i) {
                int temp = a[i];
                a[i] = a[indiceMinimo];
                a[indiceMinimo] = temp;
            }
        }
    }

    /**
     * Versão estável: localiza a primeira ocorrência do menor elemento do sufixo
     * não ordenado, guarda seu valor e desloca (shift) todos os elementos entre
     * i e indiceMinimo - 1 uma posição à direita antes de inserir o mínimo em a[i].
     * Preserva a ordem relativa dos elementos iguais. Complexidade Theta(n^2).
     *
     * @param a vetor de inteiros a ser ordenado in-place
     */
    public static void estavel(int[] a) {
        if (a == null || a.length <= 1) {
            return;
        }
        int n = a.length;
        for (int i = 0; i < n - 1; i++) {
            int indiceMinimo = i;
            for (int j = i + 1; j < n; j++) {
                if (a[j] < a[indiceMinimo]) {
                    indiceMinimo = j;
                }
            }
            int valorMinimo = a[indiceMinimo];
            while (indiceMinimo > i) {
                a[indiceMinimo] = a[indiceMinimo - 1];
                indiceMinimo--;
            }
            a[i] = valorMinimo;
        }
    }

    /**
     * Alias para {@link #slide(int[])}.
     */
    public static void sortSlide(int[] a) {
        slide(a);
    }

    /**
     * Alias para {@link #estavel(int[])}.
     */
    public static void sortEstavel(int[] a) {
        estavel(a);
    }
}
