package br.edu.uepb.eda.sorting;

/**
 * Implementações do algoritmo Bubble Sort para arrays de inteiros (int[]).
 * Contém a versão clássica do slide (n-1 passadas completas sem flag)
 * e a versão otimizada (interrompe caso nenhuma troca ocorra na passada).
 */
public final class BubbleSort {

    private BubbleSort() {
        // Classe utilitária não instanciável
    }

    /**
     * Versão do slide: realiza sempre n-1 passadas completas pelo vetor,
     * sem flag de parada antecipada. Complexidade Theta(n^2) em todos os casos.
     *
     * @param a vetor de inteiros a ser ordenado in-place
     */
    public static void slide(int[] a) {
        if (a == null || a.length <= 1) {
            return;
        }
        int n = a.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - 1 - i; j++) {
                if (a[j] > a[j + 1]) {
                    int temp = a[j];
                    a[j] = a[j + 1];
                    a[j + 1] = temp;
                }
            }
        }
    }

    /**
     * Versão otimizada: utiliza a flag booleana "trocou" para encerrar o laço
     * externo assim que uma passada inteira terminar sem nenhuma troca.
     * Melhor caso O(n) (vetor já ordenado), pior e médio caso O(n^2).
     *
     * @param a vetor de inteiros a ser ordenado in-place
     */
    public static void otimizado(int[] a) {
        if (a == null || a.length <= 1) {
            return;
        }
        int n = a.length;
        for (int i = 0; i < n - 1; i++) {
            boolean trocou = false;
            for (int j = 0; j < n - 1 - i; j++) {
                if (a[j] > a[j + 1]) {
                    int temp = a[j];
                    a[j] = a[j + 1];
                    a[j + 1] = temp;
                    trocou = true;
                }
            }
            if (!trocou) {
                break;
            }
        }
    }

    /**
     * Alias para {@link #slide(int[])}.
     */
    public static void sortSlide(int[] a) {
        slide(a);
    }

    /**
     * Alias para {@link #otimizado(int[])}.
     */
    public static void sortOtimizado(int[] a) {
        otimizado(a);
    }
}
