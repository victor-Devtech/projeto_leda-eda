package br.edu.uepb.eda.bench;

import br.edu.uepb.eda.search.Busca;
import br.edu.uepb.eda.util.DataGenerator;
import br.edu.uepb.eda.util.Estatisticas;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.function.ToIntBiFunction;

/**
 * Executa o benchmark experimental dos algoritmos de busca sobre vetores ordenados,
 * procurando sempre pelo elemento ausente (alvo = -1).
 * Mede lotes de chamadas (1 para buscas lineares, 10000 para buscas binárias),
 * acumula os retornos em variável sink para evitar eliminação de código morto,
 * verifica que todas as buscas retornam -1 e exporta busca_bruto.csv e busca_resumo.csv.
 */
public final class SearchBenchmark {

    public static final int ALVO_AUSENTE = -1;
    public static final int LOTE_LINEAR = 1;
    public static final int LOTE_BINARIA = 10_000;

    public static final int[] TAMANHOS_PADRAO = {50_000, 150_000, 500_000};
    public static final int TOTAL_EXECUCOES_PADRAO = 25;
    public static final int WARMUP_EXECUCOES_PADRAO = 5;

    /**
     * Representa um algoritmo de busca avaliado no benchmark.
     */
    public record AlgoritmoBusca(String nome, ToIntBiFunction<int[], Integer> buscador, int tamanhoLote) {
    }

    public static final List<AlgoritmoBusca> ALGORITMOS_BUSCA = List.of(
            new AlgoritmoBusca("linearIterativa", Busca::linearIterativa, LOTE_LINEAR),
            new AlgoritmoBusca("linearRecursiva", Busca::linearRecursiva, LOTE_LINEAR),
            new AlgoritmoBusca("linearDuasPontas", Busca::linearDuasPontas, LOTE_LINEAR),
            new AlgoritmoBusca("binariaIterativa", Busca::binariaIterativa, LOTE_BINARIA),
            new AlgoritmoBusca("binariaRecursiva", Busca::binariaRecursiva, LOTE_BINARIA)
    );

    private SearchBenchmark() {
        // Classe utilitária não instanciável
    }

    /**
     * Executa o benchmark de busca com os tamanhos e repetições padrão do enunciado.
     *
     * @param pastaResultados diretório onde os arquivos CSV serão salvos
     * @throws IOException em caso de falha de escrita
     */
    public static void executarPadrao(Path pastaResultados) throws IOException {
        executar(
                pastaResultados,
                TAMANHOS_PADRAO,
                TOTAL_EXECUCOES_PADRAO,
                WARMUP_EXECUCOES_PADRAO
        );
    }

    /**
     * Executa o benchmark de busca parametrizado.
     *
     * @param pastaResultados diretório de saída
     * @param tamanhos        tamanhos n de vetores ordenados a avaliar
     * @param totalExecucoes  total de execuções por combinação
     * @param warmupExecucoes execuções iniciais descartadas como warm-up
     * @throws IOException em caso de erro de escrita nos CSVs
     */
    public static void executar(
            Path pastaResultados,
            int[] tamanhos,
            int totalExecucoes,
            int warmupExecucoes) throws IOException {

        Files.createDirectories(pastaResultados);
        Path arquivoBruto = pastaResultados.resolve("busca_bruto.csv");
        Path arquivoResumo = pastaResultados.resolve("busca_resumo.csv");

        int execucoesValidas = totalExecucoes - warmupExecucoes;
        int totalPassos = tamanhos.length * ALGORITMOS_BUSCA.size() * totalExecucoes;
        int passosConcluidos = 0;
        long inicioGlobalNs = System.nanoTime();
        long sink = 0L;

        try (PrintWriter outBruto = new PrintWriter(Files.newBufferedWriter(arquivoBruto, StandardCharsets.UTF_8));
             PrintWriter outResumo = new PrintWriter(Files.newBufferedWriter(arquivoResumo, StandardCharsets.UTF_8))) {

            outBruto.println("algoritmo,cenario,n,execucao,warmup,tempo_ns");
            outResumo.println("algoritmo,cenario,n,media_ns,desvio_ns,mediana_ns,min_ns,max_ns,status");

            for (int n : tamanhos) {
                // Gera o vetor ordenado UMA vez para o tamanho n
                int[] vetorOrdenado = DataGenerator.ordenado(n);

                for (AlgoritmoBusca alg : ALGORITMOS_BUSCA) {
                    System.gc();

                    double[] temposValidosNs = new double[execucoesValidas];
                    int indiceValido = 0;
                    int lote = alg.tamanhoLote();

                    for (int exec = 1; exec <= totalExecucoes; exec++) {
                        boolean isWarmup = (exec <= warmupExecucoes);
                        int retornoInvalido = -1;

                        long t0 = System.nanoTime();
                        for (int b = 0; b < lote; b++) {
                            int indice = alg.buscador().applyAsInt(vetorOrdenado, ALVO_AUSENTE);
                            sink += indice;
                            if (indice != -1) {
                                retornoInvalido = indice;
                            }
                        }
                        long t1 = System.nanoTime();

                        // Verifica fora do tempo medido se todas as buscas retornaram -1
                        if (retornoInvalido != -1) {
                            throw new IllegalStateException(String.format(
                                    "Falha na verificação de busca [algoritmo=%s, n=%d]: esperado -1 para alvo ausente, obtido %d.",
                                    alg.nome(), n, retornoInvalido
                            ));
                        }

                        double tempoPorBuscaNs = (double) (t1 - t0) / lote;
                        passosConcluidos++;

                        outBruto.printf(
                                Locale.US,
                                "%s,ORDENADO,%d,%d,%b,%.4f%n",
                                alg.nome(), n, exec, isWarmup, tempoPorBuscaNs
                        );
                        outBruto.flush();

                        if (!isWarmup) {
                            temposValidosNs[indiceValido++] = tempoPorBuscaNs;
                        }

                        String eta = calcularEstimativaRestante(inicioGlobalNs, passosConcluidos, totalPassos);
                        System.out.printf(
                                Locale.US,
                                "[BUSCA | n=%d | ORDENADO | %-17s | lote=%5d] Exec %2d/%2d (%s) | Tempo/busca: %10.2f ns | Restante est.: %s%n",
                                n,
                                alg.nome(),
                                lote,
                                exec,
                                totalExecucoes,
                                isWarmup ? "warmup" : "valido",
                                tempoPorBuscaNs,
                                eta
                        );
                    }

                    Estatisticas est = Estatisticas.calcular(temposValidosNs);
                    outResumo.printf(
                            Locale.US,
                            "%s,ORDENADO,%d,%.4f,%.4f,%.4f,%.4f,%.4f,OK%n",
                            alg.nome(),
                            n,
                            est.getMedia(),
                            est.getDesvioPadrao(),
                            est.getMediana(),
                            est.getMinimo(),
                            est.getMaximo()
                    );
                    outResumo.flush();
                }
            }
        }

        System.out.printf(Locale.US, "[BUSCA] Benchmark finalizado (sink=%d). Arquivos gerados em: %s%n",
                sink, pastaResultados.toAbsolutePath());
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
