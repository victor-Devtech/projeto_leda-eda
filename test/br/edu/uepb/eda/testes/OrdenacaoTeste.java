package br.edu.uepb.eda.testes;

import br.edu.uepb.eda.sorting.BubbleSort;
import br.edu.uepb.eda.sorting.CountingSort;
import br.edu.uepb.eda.sorting.InsertionSort;
import br.edu.uepb.eda.sorting.MergeSort;
import br.edu.uepb.eda.sorting.QuickSort;
import br.edu.uepb.eda.sorting.SelectionSort;
import br.edu.uepb.eda.util.Cenario;
import br.edu.uepb.eda.util.DataGenerator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Testa as nove versões de ordenação implementadas e os exemplos do slide da disciplina.
 * A estabilidade não aparece aqui: em int[], valores iguais são indistinguíveis.
 */
public class OrdenacaoTeste {

    private static final Map<String, Consumer<int[]>> ALGORITMOS = criarAlgoritmos();

    private static Map<String, Consumer<int[]>> criarAlgoritmos() {
        Map<String, Consumer<int[]>> mapa = new LinkedHashMap<>();
        mapa.put("BubbleSort.slide", BubbleSort::slide);
        mapa.put("BubbleSort.otimizado", BubbleSort::otimizado);
        mapa.put("SelectionSort.slide", SelectionSort::slide);
        mapa.put("SelectionSort.estavel", SelectionSort::estavel);
        mapa.put("InsertionSort", InsertionSort::sort);
        mapa.put("MergeSort.classico", MergeSort::classico);
        mapa.put("QuickSort.slide", QuickSort::slide);
        mapa.put("QuickSort.slideShuffle", QuickSort::slideShuffle);
        mapa.put("CountingSort", CountingSort::sort);
        return mapa;
    }

    private static void conferir(int[] entrada, String caso) {
        int[] esperado = entrada.clone();
        Arrays.sort(esperado);
        for (Map.Entry<String, Consumer<int[]>> alg : ALGORITMOS.entrySet()) {
            int[] copia = entrada.clone();
            alg.getValue().accept(copia);
            Apoio.igual(esperado, copia, alg.getKey() + " falhou em " + caso);
        }
    }

    @Teste
    public void aceitaVetorNuloSemLancarExcecao() {
        for (Map.Entry<String, Consumer<int[]>> alg : ALGORITMOS.entrySet()) {
            alg.getValue().accept(null);
        }
    }

    @Teste
    public void ordenaVetoresMinimos() {
        conferir(new int[]{}, "vetor vazio");
        conferir(new int[]{7}, "um elemento");
        conferir(new int[]{1, 2}, "dois elementos em ordem");
        conferir(new int[]{2, 1}, "dois elementos invertidos");
        conferir(new int[]{5, 5}, "dois elementos iguais");
    }

    @Teste
    public void ordenaExemplosDoSlide() {
        conferir(new int[]{8, 4, 7, 1}, "exemplo [8,4,7,1]");
        conferir(new int[]{2, 2, 1, 3}, "exemplo [2,2,1,3]");
        conferir(new int[]{2, 8, 7, 1, 3, 5, 6, 4}, "exemplo do Quick Sort");
        conferir(new int[]{3, 1, 4, 1}, "exemplo do Counting Sort");
    }

    @Teste
    public void ordenaTodasAsCombinacoesPequenasComRepeticao() {
        int tamanho = 6;
        int valores = 3;
        int total = (int) Math.pow(valores, tamanho);
        for (int codigo = 0; codigo < total; codigo++) {
            int[] entrada = new int[tamanho];
            int resto = codigo;
            for (int i = 0; i < tamanho; i++) {
                entrada[i] = (resto % valores) + 1;
                resto /= valores;
            }
            conferir(entrada, Arrays.toString(entrada));
        }
    }

    @Teste
    public void ordenaTodosOsTamanhosAte64() {
        Random random = new Random(7L);
        for (int n = 0; n <= 64; n++) {
            int[] entrada = random.ints(n, 1, 100_001).toArray();
            conferir(entrada, "n=" + n);
        }
    }

    @Teste
    public void ordenaVetorComTodosOsElementosIguais() {
        int[] entrada = new int[1_000];
        Arrays.fill(entrada, 42);
        conferir(entrada, "todos iguais");
    }

    @Teste
    public void ordenaOsExtremosDoIntervalo() {
        conferir(new int[]{100_000, 1, 100_000, 1, 50_000}, "extremos 1 e 100000");
        conferir(new int[]{100_000, 100_000, 100_000}, "somente o maior valor");
        conferir(new int[]{1, 1, 1}, "somente o menor valor");
    }

    @Teste
    public void ordenaOsQuatroCenariosEmTamanhosPequenos() {
        for (Cenario cenario : Cenario.values()) {
            for (int n : new int[]{0, 1, 2, 3, 10, 100, 1_000, 5_000}) {
                conferir(DataGenerator.gerar(cenario, n), cenario + " n=" + n);
            }
        }
    }

    @Teste
    public void countingSortRejeitaValorForaDoIntervalo() {
        Apoio.lanca(ArrayIndexOutOfBoundsException.class,
                () -> CountingSort.sort(new int[]{1, 100_001}), "valor acima de k deveria falhar");
        Apoio.lanca(ArrayIndexOutOfBoundsException.class,
                () -> CountingSort.sort(new int[]{-1, 5}), "valor negativo deveria falhar");
    }

    @Teste
    public void particaoReproduzOSlide() throws Exception {
        int[] vetor = {2, 8, 7, 1, 3, 5, 6, 4};
        int posicao = particionar(vetor, 0, vetor.length - 1);
        Apoio.igual(new int[]{1, 2, 7, 8, 3, 5, 6, 4}, vetor, "resultado da partição do slide");
        Apoio.igual(1, posicao, "posição do pivô no slide");
    }

    @Teste
    public void particaoUsaOPrimeiroElementoComoPivo() throws Exception {
        Random random = new Random(11L);
        for (int rodada = 0; rodada < 2_000; rodada++) {
            int n = 1 + random.nextInt(50);
            int[] vetor = random.ints(n, 1, 20).toArray();
            int pivo = vetor[0];
            int posicao = particionar(vetor, 0, n - 1);
            Apoio.igual(pivo, vetor[posicao], "o pivô deve ser o primeiro elemento");
            for (int i = 0; i < posicao; i++) {
                Apoio.verdadeiro(vetor[i] <= pivo, "elemento maior que o pivô à esquerda");
            }
            for (int i = posicao + 1; i < n; i++) {
                Apoio.verdadeiro(vetor[i] > pivo, "elemento menor ou igual ao pivô à direita");
            }
        }
    }

    @Teste
    public void particaoRespeitaOsLimitesDoIntervalo() throws Exception {
        int[] vetor = {9, 9, 3, 1, 2, 9, 9};
        int posicao = particionar(vetor, 2, 4);
        Apoio.igual(new int[]{9, 9, 2, 1, 3, 9, 9}, vetor, "elementos fora do intervalo não podem mudar");
        Apoio.igual(4, posicao, "posição do pivô dentro do intervalo");
    }

    @Teste
    public void embaralhamentoEDeterministicoEPreservaOsElementos() throws Exception {
        Method metodo = QuickSort.class.getDeclaredMethod("embaralharFisherYates", int[].class);
        metodo.setAccessible(true);
        int[] original = new int[1_000];
        for (int i = 0; i < original.length; i++) {
            original[i] = i + 1;
        }
        int[] primeiro = original.clone();
        int[] segundo = original.clone();
        metodo.invoke(null, (Object) primeiro);
        metodo.invoke(null, (Object) segundo);
        Apoio.igual(primeiro, segundo, "a semente fixa deve repetir a permutação");
        Apoio.verdadeiro(!Arrays.equals(original, primeiro), "o vetor deveria mudar de ordem");
        int[] conferencia = primeiro.clone();
        Arrays.sort(conferencia);
        Apoio.igual(original, conferencia, "o embaralhamento não pode perder elementos");
    }

    @Teste
    public void quickSortDoSlideSuportaRecursaoProfundaEm50Mil() throws Throwable {
        for (Cenario cenario : Cenario.values()) {
            int[] entrada = DataGenerator.gerar(cenario, 50_000);
            int[] esperado = entrada.clone();
            Arrays.sort(esperado);
            Apoio.comPilhaDoBenchmark(() -> QuickSort.slide(entrada));
            Apoio.igual(esperado, entrada, "QuickSort.slide em " + cenario);
        }
    }

    /**
     * A profundidade da recursão separa as duas versões: em um vetor ordenado, a do slide chega
     * a n chamadas aninhadas e estoura uma pilha pequena, enquanto o shuffle a mantém baixa.
     */
    @Teste
    public void shuffleEvitaARecursaoProfundaNoVetorOrdenado() throws Throwable {
        int[] entrada = DataGenerator.ordenado(50_000);

        Throwable semShuffle = comPilhaPequena(() -> QuickSort.slide(entrada.clone()));
        Apoio.verdadeiro(semShuffle instanceof StackOverflowError,
                "QuickSort.slide deveria degenerar no vetor ordenado, mas terminou com " + semShuffle);

        Throwable comShuffle = comPilhaPequena(() -> QuickSort.slideShuffle(entrada.clone()));
        Apoio.verdadeiro(comShuffle == null, "QuickSort.slideShuffle não deveria degenerar: " + comShuffle);
    }

    /**
     * Com n = 1 milhão, um custo quadrático levaria horas. Terminar em segundos só é possível
     * se o algoritmo for linear no vetor ordenado, como o slide afirma para o melhor caso.
     */
    @Teste
    public void melhorCasoLinearNoVetorOrdenado() {
        int[] ordenado = new int[1_000_000];
        for (int i = 0; i < ordenado.length; i++) {
            ordenado[i] = (i / 10) + 1;
        }
        conferirTempoLinear("BubbleSort.otimizado", BubbleSort::otimizado, ordenado.clone());
        conferirTempoLinear("InsertionSort", InsertionSort::sort, ordenado.clone());
    }

    private static void conferirTempoLinear(String nome, Consumer<int[]> algoritmo, int[] vetor) {
        long inicio = System.nanoTime();
        algoritmo.accept(vetor);
        long ms = (System.nanoTime() - inicio) / 1_000_000L;
        Apoio.verdadeiro(ms < 5_000L, nome + " não foi linear no vetor ordenado: " + ms + " ms");
    }

    private static Throwable comPilhaPequena(Runnable trecho) throws InterruptedException {
        AtomicReference<Throwable> erro = new AtomicReference<>();
        Thread thread = new Thread(null, () -> {
            try {
                trecho.run();
            } catch (Throwable t) {
                erro.set(t);
            }
        }, "teste-pilha-pequena", 256L * 1024L);
        thread.start();
        thread.join();
        return erro.get();
    }

    @Teste
    public void algoritmosDoGrupoBOrdenam500Mil() throws Throwable {
        Map<String, Consumer<int[]>> grupoB = new LinkedHashMap<>();
        grupoB.put("MergeSort.classico", MergeSort::classico);
        grupoB.put("QuickSort.slideShuffle", QuickSort::slideShuffle);
        grupoB.put("CountingSort", CountingSort::sort);
        for (Cenario cenario : Cenario.values()) {
            int[] entrada = DataGenerator.gerar(cenario, 500_000);
            int[] esperado = entrada.clone();
            Arrays.sort(esperado);
            for (Map.Entry<String, Consumer<int[]>> alg : grupoB.entrySet()) {
                int[] copia = entrada.clone();
                Apoio.comPilhaDoBenchmark(() -> alg.getValue().accept(copia));
                Apoio.igual(esperado, copia, alg.getKey() + " em " + cenario);
            }
        }
    }

    private static int particionar(int[] vetor, int esq, int dir) throws Exception {
        Method metodo = QuickSort.class.getDeclaredMethod("particionar", int[].class, int.class, int.class);
        metodo.setAccessible(true);
        try {
            return (int) metodo.invoke(null, vetor, esq, dir);
        } catch (InvocationTargetException e) {
            throw new AssertionError("a partição lançou " + e.getCause(), e.getCause());
        }
    }
}
