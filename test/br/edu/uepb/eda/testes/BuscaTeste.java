package br.edu.uepb.eda.testes;

import br.edu.uepb.eda.search.Busca;
import br.edu.uepb.eda.util.DataGenerator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.ToIntBiFunction;

/**
 * Testa as cinco buscas com alvos presentes e ausentes, inclusive o alvo -1 usado no benchmark.
 */
public class BuscaTeste {

    private static final Map<String, ToIntBiFunction<int[], Integer>> TODAS = criarTodas();
    private static final Map<String, ToIntBiFunction<int[], Integer>> LINEARES = criarLineares();

    private static Map<String, ToIntBiFunction<int[], Integer>> criarLineares() {
        Map<String, ToIntBiFunction<int[], Integer>> mapa = new LinkedHashMap<>();
        mapa.put("linearIterativa", Busca::linearIterativa);
        mapa.put("linearRecursiva", Busca::linearRecursiva);
        mapa.put("linearDuasPontas", Busca::linearDuasPontas);
        return mapa;
    }

    private static Map<String, ToIntBiFunction<int[], Integer>> criarTodas() {
        Map<String, ToIntBiFunction<int[], Integer>> mapa = new LinkedHashMap<>(criarLineares());
        mapa.put("binariaIterativa", Busca::binariaIterativa);
        mapa.put("binariaRecursiva", Busca::binariaRecursiva);
        return mapa;
    }

    @Teste
    public void vetorNuloOuVazioRetornaMenosUm() {
        for (Map.Entry<String, ToIntBiFunction<int[], Integer>> busca : TODAS.entrySet()) {
            Apoio.igual(-1, busca.getValue().applyAsInt(null, 5), busca.getKey() + " com vetor nulo");
            Apoio.igual(-1, busca.getValue().applyAsInt(new int[]{}, 5), busca.getKey() + " com vetor vazio");
        }
    }

    @Teste
    public void encontraOuNaoEncontraEmTodosOsTamanhosAte40() {
        for (int n = 0; n <= 40; n++) {
            // Valores repetidos e com lacunas: 1, 1, 1, 3, 3, 3, 5, ...
            int[] vetor = new int[n];
            for (int i = 0; i < n; i++) {
                vetor[i] = (i / 3) * 2 + 1;
            }
            int maior = n == 0 ? 0 : vetor[n - 1];
            for (int alvo = -1; alvo <= maior + 2; alvo++) {
                boolean presente = contem(vetor, alvo);
                for (Map.Entry<String, ToIntBiFunction<int[], Integer>> busca : TODAS.entrySet()) {
                    int indice = busca.getValue().applyAsInt(vetor, alvo);
                    String caso = busca.getKey() + " n=" + n + " alvo=" + alvo;
                    if (presente) {
                        Apoio.verdadeiro(indice >= 0 && indice < n, caso + ": índice inválido " + indice);
                        Apoio.igual(alvo, vetor[indice], caso + ": índice aponta para outro valor");
                    } else {
                        Apoio.igual(-1, indice, caso + ": alvo ausente");
                    }
                }
            }
        }
    }

    @Teste
    public void encontraNasPosicoesDeBorda() {
        int[] vetor = {1, 2, 3, 4, 5};
        for (Map.Entry<String, ToIntBiFunction<int[], Integer>> busca : TODAS.entrySet()) {
            Apoio.igual(0, busca.getValue().applyAsInt(vetor, 1), busca.getKey() + " no início");
            Apoio.igual(2, busca.getValue().applyAsInt(vetor, 3), busca.getKey() + " no meio");
            Apoio.igual(4, busca.getValue().applyAsInt(vetor, 5), busca.getKey() + " no fim");
        }
    }

    @Teste
    public void linearesSimplesRetornamAPrimeiraOcorrencia() {
        int[] vetor = {4, 4, 4, 7, 7};
        Apoio.igual(0, Busca.linearIterativa(vetor, 4), "linearIterativa");
        Apoio.igual(0, Busca.linearRecursiva(vetor, 4), "linearRecursiva");
        Apoio.igual(3, Busca.linearIterativa(vetor, 7), "linearIterativa");
        Apoio.igual(3, Busca.linearRecursiva(vetor, 7), "linearRecursiva");
    }

    @Teste
    public void duasPontasAlcancaOMeioEmTamanhoParEImpar() {
        Apoio.igual(1, Busca.linearDuasPontas(new int[]{1, 2, 3}, 2), "tamanho ímpar");
        Apoio.igual(1, Busca.linearDuasPontas(new int[]{1, 2, 3, 4}, 2), "tamanho par, metade esquerda");
        Apoio.igual(2, Busca.linearDuasPontas(new int[]{1, 2, 3, 4}, 3), "tamanho par, metade direita");
    }

    /**
     * As lineares não podem parar ao ver um valor maior que o alvo, senão o custo O(n) deixaria
     * de ser medido. Um vetor fora de ordem denuncia essa parada antecipada.
     */
    @Teste
    public void linearesNaoFazemParadaAntecipadaPorOrdenacao() {
        int[] vetor = {9, 8, 1, 7, 6, 5, 4};
        for (Map.Entry<String, ToIntBiFunction<int[], Integer>> busca : LINEARES.entrySet()) {
            Apoio.igual(2, busca.getValue().applyAsInt(vetor, 1), busca.getKey() + " parou antes de encontrar");
        }
    }

    @Teste
    public void alvoMenosUmEstaAusenteNosTamanhosDoBenchmark() throws Throwable {
        for (int n : new int[]{50_000, 150_000, 500_000}) {
            int[] vetor = DataGenerator.ordenado(n);
            for (Map.Entry<String, ToIntBiFunction<int[], Integer>> busca : TODAS.entrySet()) {
                Apoio.comPilhaDoBenchmark(() ->
                        Apoio.igual(-1, busca.getValue().applyAsInt(vetor, -1), busca.getKey() + " n=" + n));
            }
        }
    }

    @Teste
    public void linearRecursivaAlcancaOFimDe500Mil() throws Throwable {
        int[] vetor = new int[500_000];
        for (int i = 0; i < vetor.length; i++) {
            vetor[i] = i + 1;
        }
        Apoio.comPilhaDoBenchmark(() ->
                Apoio.igual(499_999, Busca.linearRecursiva(vetor, 500_000), "último elemento"));
    }

    private static boolean contem(int[] vetor, int alvo) {
        for (int valor : vetor) {
            if (valor == alvo) {
                return true;
            }
        }
        return false;
    }
}
