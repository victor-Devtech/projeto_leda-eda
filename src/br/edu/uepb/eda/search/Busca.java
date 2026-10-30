package br.edu.uepb.eda.search;

/**
 * Implementações dos cinco algoritmos de busca avaliados sobre vetores ordenados (int[]).
 * Todos os métodos retornam o índice onde o elemento alvo foi encontrado ou -1 caso não exista.
 */
public final class Busca {

    private Busca() {
        // Classe utilitária não instanciável
    }

    /**
     * Busca Linear Iterativa.
     * IMPORTANTE: Esta implementação propositalmente NÃO possui parada antecipada
     * baseada na ordenação do vetor (como interromper quando a[i] > alvo).
     * Ela deve varrer todo o vetor quando o alvo não existe, permitindo medir
     * experimentalmente o comportamento assintótico O(n) real no pior caso.
     *
     * @param a    vetor de inteiros (previamente ordenado)
     * @param alvo valor procurado
     * @return índice do elemento encontrado ou -1 se ausente
     */
    public static int linearIterativa(int[] a, int alvo) {
        if (a == null) {
            return -1;
        }
        // Varre todos os elementos sem parada antecipada por ordenação para medir o O(n) real
        for (int i = 0; i < a.length; i++) {
            if (a[i] == alvo) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Busca Linear Recursiva.
     * IMPORTANTE: Não possui parada antecipada pelo fato de o vetor estar ordenado.
     * Percorre todos os elementos até o final quando o alvo não existe para medir o O(n) real.
     * Requer pilha de execução dimensionada adequadamente para vetores grandes (profundidade n).
     *
     * @param a    vetor de inteiros (previamente ordenado)
     * @param alvo valor procurado
     * @return índice do elemento encontrado ou -1 se ausente
     */
    public static int linearRecursiva(int[] a, int alvo) {
        if (a == null) {
            return -1;
        }
        return linearRecursivaAux(a, alvo, 0);
    }

    private static int linearRecursivaAux(int[] a, int alvo, int indice) {
        if (indice >= a.length) {
            return -1;
        }
        // Sem parada antecipada por ordenação (a[indice] > alvo) para medir o O(n) real
        if (a[indice] == alvo) {
            return indice;
        }
        return linearRecursivaAux(a, alvo, indice + 1);
    }

    /**
     * Busca Linear Iterativa de Duas Pontas.
     * IMPORTANTE: Não possui parada antecipada pelo fato de o vetor estar ordenado.
     * Inspeciona simultaneamente as extremidades esquerda e direita caminhando rumo ao centro,
     * varrendo todas as posições quando o alvo não existe para medir o O(n) real.
     *
     * @param a    vetor de inteiros (previamente ordenado)
     * @param alvo valor procurado
     * @return índice do elemento encontrado ou -1 se ausente
     */
    public static int linearDuasPontas(int[] a, int alvo) {
        if (a == null) {
            return -1;
        }
        int esq = 0;
        int dir = a.length - 1;
        // Sem parada antecipada por ordenação para medir o O(n) real
        while (esq <= dir) {
            if (a[esq] == alvo) {
                return esq;
            }
            if (a[dir] == alvo) {
                return dir;
            }
            esq++;
            dir--;
        }
        return -1;
    }

    /**
     * Busca Binária Iterativa.
     * Requer vetor previamente ordenado. Calcula o ponto médio de forma segura
     * contra overflow usando meio = esq + (dir - esq) / 2. Complexidade O(log n).
     *
     * @param a    vetor de inteiros ordenado em ordem não-decrescente
     * @param alvo valor procurado
     * @return índice do elemento encontrado ou -1 se ausente
     */
    public static int binariaIterativa(int[] a, int alvo) {
        if (a == null) {
            return -1;
        }
        int esq = 0;
        int dir = a.length - 1;
        while (esq <= dir) {
            int meio = esq + (dir - esq) / 2;
            if (a[meio] == alvo) {
                return meio;
            } else if (a[meio] < alvo) {
                esq = meio + 1;
            } else {
                dir = meio - 1;
            }
        }
        return -1;
    }

    /**
     * Busca Binária Recursiva.
     * Requer vetor previamente ordenado. Calcula o ponto médio de forma segura
     * contra overflow usando meio = esq + (dir - esq) / 2. Complexidade O(log n).
     *
     * @param a    vetor de inteiros ordenado em ordem não-decrescente
     * @param alvo valor procurado
     * @return índice do elemento encontrado ou -1 se ausente
     */
    public static int binariaRecursiva(int[] a, int alvo) {
        if (a == null) {
            return -1;
        }
        return binariaRecursivaAux(a, alvo, 0, a.length - 1);
    }

    private static int binariaRecursivaAux(int[] a, int alvo, int esq, int dir) {
        if (esq > dir) {
            return -1;
        }
        int meio = esq + (dir - esq) / 2;
        if (a[meio] == alvo) {
            return meio;
        } else if (a[meio] < alvo) {
            return binariaRecursivaAux(a, alvo, meio + 1, dir);
        } else {
            return binariaRecursivaAux(a, alvo, esq, meio - 1);
        }
    }
}
