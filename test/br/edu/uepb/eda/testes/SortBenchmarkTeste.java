package br.edu.uepb.eda.testes;

import br.edu.uepb.eda.bench.SortBenchmark;
import br.edu.uepb.eda.bench.SortBenchmark.AlgoritmoOrdenacao;
import br.edu.uepb.eda.util.Cenario;
import br.edu.uepb.eda.util.DataGenerator;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Testa o protocolo de medição exigido pelo enunciado com vetores pequenos e pastas temporárias:
 * 25 execuções, 5 de warm-up, cópias da mesma entrada e verificação de cada saída.
 */
public class SortBenchmarkTeste {

    private static final Cenario[] ALEATORIO = {Cenario.ALEATORIO};

    @Teste
    public void configuracaoSegueOEnunciado() {
        Apoio.igual(25, SortBenchmark.TOTAL_EXECUCOES_PADRAO, "total de execuções");
        Apoio.igual(5, SortBenchmark.WARMUP_EXECUCOES_PADRAO, "execuções de warm-up");
        Apoio.igual(new int[]{10_000, 25_000, 50_000}, SortBenchmark.TAMANHOS_GRUPO_A_PADRAO, "tamanhos do Grupo A");
        Apoio.igual(new int[]{50_000, 150_000, 500_000}, SortBenchmark.TAMANHOS_GRUPO_B_PADRAO, "tamanhos do Grupo B");
    }

    @Teste
    public void gruposContemOsAlgoritmosEsperados() {
        Apoio.igual(List.of("BubbleSort.slide", "BubbleSort.otimizado", "SelectionSort.slide",
                        "SelectionSort.estavel", "InsertionSort", "MergeSort.classico", "QuickSort.slide",
                        "QuickSort.slideShuffle", "CountingSort", "Arrays.sort"),
                nomes(SortBenchmark.ALGORITMOS_GRUPO_A), "algoritmos do Grupo A");
        Apoio.igual(List.of("MergeSort.classico", "QuickSort.slideShuffle", "CountingSort", "Arrays.sort"),
                nomes(SortBenchmark.ALGORITMOS_GRUPO_B), "algoritmos do Grupo B");
    }

    @Teste
    public void todosOsAlgoritmosRegistradosOrdenam() {
        for (AlgoritmoOrdenacao alg : SortBenchmark.ALGORITMOS_GRUPO_A) {
            int[] vetor = {5, 3, 9, 1, 3};
            alg.ordenador().accept(vetor);
            Apoio.igual(new int[]{1, 3, 3, 5, 9}, vetor, alg.nome());
        }
    }

    @Teste
    public void execucaoGeraOsCsvsComAsLinhasEsperadas() throws Throwable {
        Path pasta = Files.createTempDirectory("teste-ordenacao");
        try {
            int total = 5;
            int warmup = 2;
            Apoio.capturarSaida(() -> SortBenchmark.executar(pasta, new int[]{100, 200}, new int[]{300}, total, warmup));

            List<String> bruto = Files.readAllLines(pasta.resolve("ordenacao_bruto.csv"));
            List<String> resumo = Files.readAllLines(pasta.resolve("ordenacao_resumo.csv"));
            Apoio.igual("grupo,algoritmo,cenario,n,execucao,warmup,tempo_ns", bruto.get(0), "cabeçalho do bruto");
            Apoio.igual("grupo,algoritmo,cenario,n,media_ms,desvio_ms,mediana_ms,min_ms,max_ms,status",
                    resumo.get(0), "cabeçalho do resumo");

            int combinacoes = (2 * 4 * 10) + (1 * 4 * 4);
            Apoio.igual(1 + combinacoes * total, bruto.size(), "linhas do bruto");
            Apoio.igual(1 + combinacoes, resumo.size(), "linhas do resumo");
        } finally {
            apagar(pasta);
        }
    }

    @Teste
    public void somenteAsPrimeirasExecucoesSaoMarcadasComoWarmup() throws Throwable {
        Path pasta = Files.createTempDirectory("teste-warmup");
        try {
            int warmup = 2;
            Apoio.capturarSaida(() -> SortBenchmark.executar(pasta, new int[]{100}, new int[]{100}, 5, warmup));
            List<String> bruto = Files.readAllLines(pasta.resolve("ordenacao_bruto.csv"));
            for (String linha : bruto.subList(1, bruto.size())) {
                String[] campos = linha.split(",");
                boolean esperado = Integer.parseInt(campos[4]) <= warmup;
                Apoio.igual(esperado, Boolean.parseBoolean(campos[5]), "marcação de warm-up: " + linha);
            }
        } finally {
            apagar(pasta);
        }
    }

    @Teste
    public void resumoIgnoraOsTemposDeWarmup() throws Throwable {
        Path pasta = Files.createTempDirectory("teste-resumo");
        try {
            Apoio.capturarSaida(() -> SortBenchmark.executar(pasta, new int[]{500}, new int[]{500}, 6, 2));

            Map<String, List<Double>> validos = new HashMap<>();
            List<String> bruto = Files.readAllLines(pasta.resolve("ordenacao_bruto.csv"));
            for (String linha : bruto.subList(1, bruto.size())) {
                String[] c = linha.split(",");
                if (!Boolean.parseBoolean(c[5])) {
                    validos.computeIfAbsent(c[0] + c[1] + c[2] + c[3], k -> new ArrayList<>())
                            .add(Long.parseLong(c[6]) / 1_000_000.0);
                }
            }

            List<String> resumo = Files.readAllLines(pasta.resolve("ordenacao_resumo.csv"));
            for (String linha : resumo.subList(1, resumo.size())) {
                String[] c = linha.split(",");
                List<Double> tempos = validos.get(c[0] + c[1] + c[2] + c[3]);
                Apoio.igual(4, tempos.size(), "amostras válidas de " + linha);
                double media = tempos.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
                Apoio.proximo(media, Double.parseDouble(c[4]), 1e-4, "média de " + linha);
                Apoio.igual("OK", c[9], "status de " + linha);
            }
        } finally {
            apagar(pasta);
        }
    }

    @Teste
    public void warmupLentoNaoAfetaAsEstatisticas() throws Throwable {
        int[] chamadas = {0};
        AlgoritmoOrdenacao alg = new AlgoritmoOrdenacao("Lento", vetor -> {
            chamadas[0]++;
            if (chamadas[0] <= 2) {
                try {
                    Thread.sleep(40);
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                }
            }
            Arrays.sort(vetor);
        });
        StringWriter bruto = new StringWriter();
        StringWriter resumo = new StringWriter();
        executarGrupo(List.of(alg), new int[]{100}, ALEATORIO, 5, 2, bruto, resumo);

        String[] campos = resumo.toString().trim().split(",");
        Apoio.verdadeiro(Double.parseDouble(campos[8]) < 30.0, "o máximo incluiu o warm-up: " + resumo);
        String[] linhas = bruto.toString().trim().split("\\R");
        Apoio.verdadeiro(Long.parseLong(linhas[0].split(",")[6]) >= 30_000_000L, "warm-up 1 não foi registrado");
        Apoio.verdadeiro(Long.parseLong(linhas[1].split(",")[6]) >= 30_000_000L, "warm-up 2 não foi registrado");
    }

    @Teste
    public void todasAsExecucoesRecebemCopiasDaMesmaEntrada() throws Throwable {
        int n = 200;
        List<int[]> instancias = new ArrayList<>();
        List<int[]> conteudos = new ArrayList<>();
        AlgoritmoOrdenacao gravador = new AlgoritmoOrdenacao("Gravador", vetor -> {
            instancias.add(vetor);
            conteudos.add(vetor.clone());
            Arrays.sort(vetor);
        });
        AlgoritmoOrdenacao segundo = new AlgoritmoOrdenacao("Segundo", vetor -> {
            instancias.add(vetor);
            conteudos.add(vetor.clone());
            Arrays.sort(vetor);
        });

        executarGrupo(List.of(gravador, segundo), new int[]{n}, ALEATORIO, 3, 1, new StringWriter(), new StringWriter());

        int[] esperado = DataGenerator.gerar(Cenario.ALEATORIO, n);
        Apoio.igual(6, conteudos.size(), "quantidade de execuções");
        for (int[] conteudo : conteudos) {
            Apoio.igual(esperado, conteudo, "entrada diferente da original: saída de outro algoritmo vazou");
        }
        for (int i = 0; i < instancias.size(); i++) {
            for (int j = i + 1; j < instancias.size(); j++) {
                Apoio.verdadeiro(instancias.get(i) != instancias.get(j), "duas execuções usaram o mesmo vetor");
            }
        }
    }

    @Teste
    public void saidaForaDeOrdemInterrompeOBenchmark() {
        AlgoritmoOrdenacao errado = new AlgoritmoOrdenacao("NaoOrdena", vetor -> { });
        IllegalStateException erro = Apoio.lanca(IllegalStateException.class,
                () -> executarGrupo(List.of(errado), new int[]{100}, ALEATORIO, 2, 1, new StringWriter(), new StringWriter()),
                "a saída errada deveria interromper");
        Apoio.verdadeiro(erro.getMessage().contains("NaoOrdena"), "mensagem sem o algoritmo: " + erro.getMessage());
    }

    @Teste
    public void saidaOrdenadaComElementosPerdidosInterrompeOBenchmark() {
        AlgoritmoOrdenacao errado = new AlgoritmoOrdenacao("PerdeElementos", vetor -> Arrays.fill(vetor, 1));
        IllegalStateException erro = Apoio.lanca(IllegalStateException.class,
                () -> executarGrupo(List.of(errado), new int[]{100}, ALEATORIO, 2, 1, new StringWriter(), new StringWriter()),
                "a perda de elementos deveria interromper");
        Apoio.verdadeiro(erro.getMessage().contains("soma"), "mensagem inesperada: " + erro.getMessage());
    }

    @Teste
    public void estouroDePilhaEhRegistradoComoInviavel() throws Throwable {
        AlgoritmoOrdenacao estoura = new AlgoritmoOrdenacao("Estoura", vetor -> {
            throw new StackOverflowError();
        });
        StringWriter bruto = new StringWriter();
        StringWriter resumo = new StringWriter();
        executarGrupo(List.of(estoura), new int[]{100}, ALEATORIO, 3, 1, bruto, resumo);
        Apoio.igual("", bruto.toString().trim(), "nenhum tempo deveria ser registrado");
        Apoio.igual("T,Estoura,ALEATORIO,100,,,,,,INVIAVEL", resumo.toString().trim(), "linha do resumo");
    }

    @Teste
    public void combinacaoUnicaMedeSoOQueFoiPedido() throws Throwable {
        Path pasta = Files.createTempDirectory("teste-combinacao");
        try {
            Apoio.capturarSaida(() -> SortBenchmark.executarCombinacao(
                    pasta, "B", "MergeSort.classico", Cenario.INVERSO, 300, 5, 2));
            List<String> bruto = Files.readAllLines(pasta.resolve("ordenacao_bruto.csv"));
            List<String> resumo = Files.readAllLines(pasta.resolve("ordenacao_resumo.csv"));
            Apoio.igual("grupo,algoritmo,cenario,n,execucao,warmup,tempo_ns", bruto.get(0), "cabeçalho do bruto");
            Apoio.igual(6, bruto.size(), "uma linha por execução");
            Apoio.igual(2, resumo.size(), "uma combinação no resumo");
            Apoio.verdadeiro(resumo.get(1).startsWith("B,MergeSort.classico,INVERSO,300,"), "linha: " + resumo.get(1));
        } finally {
            apagar(pasta);
        }
    }

    @Teste
    public void combinacaoUnicaRejeitaGrupoOuAlgoritmoInvalidos() throws Exception {
        Path pasta = Files.createTempDirectory("teste-combinacao-invalida");
        try {
            Apoio.lanca(IllegalArgumentException.class, () -> SortBenchmark.executarCombinacao(
                    pasta, "C", "MergeSort.classico", Cenario.INVERSO, 10, 2, 1), "grupo inexistente");
            Apoio.lanca(IllegalArgumentException.class, () -> SortBenchmark.executarCombinacao(
                    pasta, "B", "BubbleSort.slide", Cenario.INVERSO, 10, 2, 1), "algoritmo fora do Grupo B");
        } finally {
            apagar(pasta);
        }
    }

    private static List<String> nomes(List<AlgoritmoOrdenacao> algoritmos) {
        List<String> nomes = new ArrayList<>();
        for (AlgoritmoOrdenacao alg : algoritmos) {
            nomes.add(alg.nome());
        }
        return nomes;
    }

    /**
     * Chama o método privado que executa um grupo, permitindo injetar algoritmos de teste.
     */
    private static void executarGrupo(
            List<AlgoritmoOrdenacao> algoritmos,
            int[] tamanhos,
            Cenario[] cenarios,
            int total,
            int warmup,
            StringWriter bruto,
            StringWriter resumo) throws Throwable {

        Method metodo = SortBenchmark.class.getDeclaredMethod(
                "executarGrupo", String.class, List.class, int[].class, Cenario[].class, int.class, int.class,
                PrintWriter.class, PrintWriter.class, long.class, int.class, int.class);
        metodo.setAccessible(true);

        try (PrintWriter outBruto = new PrintWriter(bruto); PrintWriter outResumo = new PrintWriter(resumo)) {
            Apoio.capturarSaida(() -> {
                try {
                    metodo.invoke(null, "T", algoritmos, tamanhos, cenarios, total, warmup,
                            outBruto, outResumo, System.nanoTime(), 0, total * algoritmos.size());
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            });
        }
    }

    private static void apagar(Path pasta) throws IOException {
        try (Stream<Path> caminhos = Files.list(pasta)) {
            for (Path caminho : (Iterable<Path>) caminhos::iterator) {
                Files.delete(caminho);
            }
        }
        Files.delete(pasta);
    }
}
