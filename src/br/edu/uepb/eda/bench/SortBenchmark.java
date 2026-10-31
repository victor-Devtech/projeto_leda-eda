package br.edu.uepb.eda.bench;

import br.edu.uepb.eda.sorting.BubbleSort;
import br.edu.uepb.eda.sorting.CountingSort;
import br.edu.uepb.eda.sorting.InsertionSort;
import br.edu.uepb.eda.sorting.MergeSort;
import br.edu.uepb.eda.sorting.QuickSort;
import br.edu.uepb.eda.sorting.SelectionSort;
import br.edu.uepb.eda.util.Cenario;
import br.edu.uepb.eda.util.DataGenerator;
import br.edu.uepb.eda.util.Estatisticas;
import br.edu.uepb.eda.util.Verificador;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Executa o benchmark experimental dos algoritmos de ordenação nos Grupos A e B,
 * coletando tempos com System.nanoTime(), descartando as execuções de warm-up
 * das estatísticas, verificando a correção de cada saída fora do tempo medido
 * e exportando resultados/ordenacao_bruto.csv e resultados/ordenacao_resumo.csv.
 */
public final class SortBenchmark {

    /** Limite máximo por execução individual: 120 segundos em nanossegundos. */
    public static final long LIMITE_EXECUCAO_NS = 120_000_000_000L;

    public static final int[] TAMANHOS_GRUPO_A_PADRAO = {10_000, 25_000, 50_000};
    public static final int[] TAMANHOS_GRUPO_B_PADRAO = {50_000, 150_000, 500_000};
    public static final int TOTAL_EXECUCOES_PADRAO = 25;
    public static final int WARMUP_EXECUCOES_PADRAO = 5;

    /**
     * Representa um algoritmo de ordenação registrado no benchmark.
     */
    public record AlgoritmoOrdenacao(String nome, Consumer<int[]> ordenador) {
    }

    public static final List<AlgoritmoOrdenacao> ALGORITMOS_GRUPO_A = List.of(
            new AlgoritmoOrdenacao("BubbleSort.slide", BubbleSort::slide),
            new AlgoritmoOrdenacao("BubbleSort.otimizado", BubbleSort::otimizado),
            new AlgoritmoOrdenacao("SelectionSort.slide", SelectionSort::slide),
            new AlgoritmoOrdenacao("SelectionSort.estavel", SelectionSort::estavel),
            new AlgoritmoOrdenacao("InsertionSort", InsertionSort::sort),
            new AlgoritmoOrdenacao("MergeSort.classico", MergeSort::classico),
            new AlgoritmoOrdenacao("QuickSort.slide", QuickSort::slide),
            new AlgoritmoOrdenacao("QuickSort.slideShuffle", QuickSort::slideShuffle),
            new AlgoritmoOrdenacao("CountingSort", CountingSort::sort),
            new AlgoritmoOrdenacao("Arrays.sort", Arrays::sort)
    );

    public static final List<AlgoritmoOrdenacao> ALGORITMOS_GRUPO_B = List.of(
            new AlgoritmoOrdenacao("MergeSort.classico", MergeSort::classico),
            new AlgoritmoOrdenacao("QuickSort.slideShuffle", QuickSort::slideShuffle),
            new AlgoritmoOrdenacao("CountingSort", CountingSort::sort),
            new AlgoritmoOrdenacao("Arrays.sort", Arrays::sort)
    );

    private SortBenchmark() {
        // Classe utilitária não instanciável
    }

    /**
     * Executa o benchmark completo de ordenação com os parâmetros padrão do enunciado.
     *
     * @param pastaResultados diretório onde os arquivos CSV serão salvos
     * @throws IOException em caso de erro de escrita
     */
    public static void executarPadrao(Path pastaResultados) throws IOException {
        executar(
                pastaResultados,
                TAMANHOS_GRUPO_A_PADRAO,
                TAMANHOS_GRUPO_B_PADRAO,
                TOTAL_EXECUCOES_PADRAO,
                WARMUP_EXECUCOES_PADRAO
        );
    }

    /**
     * Executa o benchmark de ordenação parametrizado (usado tanto no modo completo quanto no modo rápido).
     *
     * @param pastaResultados diretório de saída
     * @param tamanhosGrupoA  tamanhos n para o Grupo A
     * @param tamanhosGrupoB  tamanhos n para o Grupo B
     * @param totalExecucoes  total de execuções por combinação
     * @param warmupExecucoes quantidade de execuções iniciais descartadas como warm-up
     * @throws IOException em caso de falha na escrita dos arquivos CSV
     */
    public static void executar(
            Path pastaResultados,
            int[] tamanhosGrupoA,
            int[] tamanhosGrupoB,
            int totalExecucoes,
            int warmupExecucoes) throws IOException {

        Files.createDirectories(pastaResultados);
        Path arquivoBruto = pastaResultados.resolve("ordenacao_bruto.csv");
        Path arquivoResumo = pastaResultados.resolve("ordenacao_resumo.csv");

        Cenario[] cenarios = Cenario.values();
        int totalCombinacoes = (tamanhosGrupoA.length * cenarios.length * ALGORITMOS_GRUPO_A.size())
                + (tamanhosGrupoB.length * cenarios.length * ALGORITMOS_GRUPO_B.size());
        int totalPassos = totalCombinacoes * totalExecucoes;

        long inicioGlobalNs = System.nanoTime();
        int passosConcluidos = 0;

        try (PrintWriter outBruto = new PrintWriter(Files.newBufferedWriter(arquivoBruto, StandardCharsets.UTF_8));
             PrintWriter outResumo = new PrintWriter(Files.newBufferedWriter(arquivoResumo, StandardCharsets.UTF_8))) {

            outBruto.println("grupo,algoritmo,cenario,n,execucao,warmup,tempo_ns");
            outResumo.println("grupo,algoritmo,cenario,n,media_ms,desvio_ms,mediana_ms,min_ms,max_ms,status");

            passosConcluidos = executarGrupo(
                    "A",
                    ALGORITMOS_GRUPO_A,
                    tamanhosGrupoA,
                    cenarios,
                    totalExecucoes,
                    warmupExecucoes,
                    outBruto,
                    outResumo,
                    inicioGlobalNs,
                    passosConcluidos,
                    totalPassos
            );

            executarGrupo(
                    "B",
                    ALGORITMOS_GRUPO_B,
                    tamanhosGrupoB,
                    cenarios,
                    totalExecucoes,
                    warmupExecucoes,
                    outBruto,
                    outResumo,
                    inicioGlobalNs,
                    passosConcluidos,
                    totalPassos
            );
        }

        System.out.println("[ORDENACAO] Benchmark finalizado. Arquivos gerados em: " + pastaResultados.toAbsolutePath());
    }

    /**
     * Executa uma única combinação de algoritmo, cenário e tamanho, usada para medir cada
     * combinação em uma JVM própria.
     *
     * @param pastaResultados diretório de saída
     * @param grupo           grupo do algoritmo ("A" ou "B")
     * @param nomeAlgoritmo   nome registrado do algoritmo
     * @param cenario         cenário da entrada
     * @param n               tamanho do vetor
     * @param totalExecucoes  total de execuções
     * @param warmupExecucoes execuções iniciais descartadas como warm-up
     * @throws IOException em caso de falha na escrita dos arquivos CSV
     */
    public static void executarCombinacao(
            Path pastaResultados,
            String grupo,
            String nomeAlgoritmo,
            Cenario cenario,
            int n,
            int totalExecucoes,
            int warmupExecucoes) throws IOException {

        List<AlgoritmoOrdenacao> algoritmos = switch (grupo) {
            case "A" -> ALGORITMOS_GRUPO_A;
            case "B" -> ALGORITMOS_GRUPO_B;
            default -> throw new IllegalArgumentException("Grupo desconhecido: " + grupo);
        };
        AlgoritmoOrdenacao algoritmo = algoritmos.stream()
                .filter(alg -> alg.nome().equals(nomeAlgoritmo))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Algoritmo desconhecido no grupo " + grupo + ": " + nomeAlgoritmo));

        Files.createDirectories(pastaResultados);
        Path arquivoBruto = pastaResultados.resolve("ordenacao_bruto.csv");
        Path arquivoResumo = pastaResultados.resolve("ordenacao_resumo.csv");

        try (PrintWriter outBruto = new PrintWriter(Files.newBufferedWriter(arquivoBruto, StandardCharsets.UTF_8));
             PrintWriter outResumo = new PrintWriter(Files.newBufferedWriter(arquivoResumo, StandardCharsets.UTF_8))) {

            outBruto.println("grupo,algoritmo,cenario,n,execucao,warmup,tempo_ns");
            outResumo.println("grupo,algoritmo,cenario,n,media_ms,desvio_ms,mediana_ms,min_ms,max_ms,status");

            executarGrupo(
                    grupo,
                    List.of(algoritmo),
                    new int[]{n},
                    new Cenario[]{cenario},
                    totalExecucoes,
                    warmupExecucoes,
                    outBruto,
                    outResumo,
                    System.nanoTime(),
                    0,
                    totalExecucoes
            );
        }
    }

    private static int executarGrupo(
            String nomeGrupo,
            List<AlgoritmoOrdenacao> algoritmos,
            int[] tamanhos,
            Cenario[] cenarios,
            int totalExecucoes,
            int warmupExecucoes,
            PrintWriter outBruto,
            PrintWriter outResumo,
            long inicioGlobalNs,
            int passosConcluidos,
            int totalPassos) {

        int execucoesValidas = totalExecucoes - warmupExecucoes;

        // Loop: grupo -> n -> cenario
        for (int n : tamanhos) {
            for (Cenario cenario : cenarios) {
                // Gera a entrada original UMA única vez para esta combinação (n, cenário)
                int[] entradaOriginal = DataGenerator.gerar(cenario, n);
                long somaEsperada = Verificador.calcularSoma(entradaOriginal);

                for (AlgoritmoOrdenacao alg : algoritmos) {
                    // Sugere coleta de lixo entre algoritmos, fora da medição
                    System.gc();

                    double[] temposValidosMs = new double[execucoesValidas];
                    int indiceValido = 0;
                    boolean inviavel = false;

                    for (int exec = 1; exec <= totalExecucoes; exec++) {
                        boolean isWarmup = (exec <= warmupExecucoes);

                        // A cópia é feita FORA do tempo medido a partir da mesma entrada original
                        int[] copia = entradaOriginal.clone();

                        long tempoNs;
                        try {
                            long t0 = System.nanoTime();
                            alg.ordenador().accept(copia);
                            long t1 = System.nanoTime();
                            tempoNs = t1 - t0;
                        } catch (StackOverflowError err) {
                            inviavel = true;
                            int restantes = totalExecucoes - exec + 1;
                            passosConcluidos += restantes;
                            System.out.printf(
                                    Locale.US,
                                    "[Grupo %s | n=%d | %s | %s] Exec %d/%d -> INVIAVEL (StackOverflowError)%n",
                                    nomeGrupo, n, cenario.name(), alg.nome(), exec, totalExecucoes
                            );
                            break;
                        }

                        passosConcluidos++;

                        // Verifica a correção FORA do tempo medido
                        Verificador.verificarOrdenacao(
                                copia,
                                entradaOriginal.length,
                                somaEsperada,
                                alg.nome(),
                                cenario.name(),
                                n
                        );

                        outBruto.printf(
                                Locale.US,
                                "%s,%s,%s,%d,%d,%b,%d%n",
                                nomeGrupo, alg.nome(), cenario.name(), n, exec, isWarmup, tempoNs
                        );
                        outBruto.flush();

                        double tempoMs = tempoNs / 1_000_000.0;
                        String eta = calcularEstimativaRestante(inicioGlobalNs, passosConcluidos, totalPassos);

                        System.out.printf(
                                Locale.US,
                                "[Grupo %s | n=%d | %-9s | %-22s] Exec %2d/%2d (%s) | Tempo: %9.3f ms | Restante est.: %s%n",
                                nomeGrupo,
                                n,
                                cenario.name(),
                                alg.nome(),
                                exec,
                                totalExecucoes,
                                isWarmup ? "warmup" : "valido",
                                tempoMs,
                                eta
                        );

                        // Proteção: se uma única execução ultrapassar 120 s, aborta a combinação como INVIAVEL
                        if (tempoNs > LIMITE_EXECUCAO_NS) {
                            inviavel = true;
                            int restantes = totalExecucoes - exec;
                            passosConcluidos += restantes;
                            System.out.printf(
                                    Locale.US,
                                    "[Grupo %s | n=%d | %s | %s] Limite de 120s excedido (%.2f s). Marcando como INVIAVEL.%n",
                                    nomeGrupo, n, cenario.name(), alg.nome(), tempoNs / 1_000_000_000.0
                            );
                            break;
                        }

                        if (!isWarmup) {
                            temposValidosMs[indiceValido++] = tempoMs;
                        }
                    }

                    if (inviavel) {
                        outResumo.printf(
                                Locale.US,
                                "%s,%s,%s,%d,,,,,,INVIAVEL%n",
                                nomeGrupo, alg.nome(), cenario.name(), n
                        );
                    } else {
                        Estatisticas est = Estatisticas.calcular(temposValidosMs);
                        outResumo.printf(
                                Locale.US,
                                "%s,%s,%s,%d,%.4f,%.4f,%.4f,%.4f,%.4f,OK%n",
                                nomeGrupo,
                                alg.nome(),
                                cenario.name(),
                                n,
                                est.getMedia(),
                                est.getDesvioPadrao(),
                                est.getMediana(),
                                est.getMinimo(),
                                est.getMaximo()
                        );
                    }
                    outResumo.flush();
                }
            }
        }
        return passosConcluidos;
    }

    private static String calcularEstimativaRestante(long inicioGlobalNs, int passosConcluidos, int totalPassos) {
        if (passosConcluidos <= 0) {
            return "--:--";
        }
        long decorridoNs = System.nanoTime() - inicioGlobalNs;
        double mediaPorPassoNs = (double) decorridoNs / passosConcluidos;
        int passosRestantes = Math.max(0, totalPassos - passosConcluidos);
        long restanteSegundos = Math.round((mediaPorPassoNs * passosRestantes) / 1_000_000_000.0);
        long minutos = restanteSegundos / 60;
        long segundos = restanteSegundos % 60;
        return String.format(Locale.US, "%02dm%02ds", minutos, segundos);
    }
}
