package br.edu.uepb.eda;

import br.edu.uepb.eda.bench.SearchBenchmark;
import br.edu.uepb.eda.bench.SortBenchmark;
import br.edu.uepb.eda.util.AmbienteUtil;
import br.edu.uepb.eda.util.Cenario;
import br.edu.uepb.eda.util.DataGenerator;
import br.edu.uepb.eda.util.Verificador;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Ponto de entrada principal do projeto da Unidade 1 de EDA/LEDA (UEPB).
 * Executa os testes e benchmarks dentro de uma thread dedicada com pilha de 1 GB
 * (1L << 30) para suportar recursão profunda (QuickSort.slide em ORDENADO/INVERSO
 * e Busca.linearRecursiva em vetores grandes).
 */
public final class Main {

    private static final long TAMANHO_PILHA_BYTES = 1L << 30; // 1 GB de pilha
    private static final Path PASTA_RESULTADOS = Paths.get("resultados");

    private Main() {
        // Classe principal não instanciável
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        String modo = (args != null && args.length > 0) ? args[0].trim() : "--tudo";

        AtomicReference<Throwable> erroCapturado = new AtomicReference<>();

        Runnable tarefa = () -> {
            try {
                executarModo(modo);
            } catch (Throwable t) {
                erroCapturado.set(t);
            }
        };

        Thread benchThread = new Thread(null, tarefa, "bench", TAMANHO_PILHA_BYTES);
        benchThread.start();
        try {
            benchThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Execução interrompida: " + e.getMessage());
            System.exit(1);
        }

        if (erroCapturado.get() != null) {
            System.err.println("FALHOU: erro durante a execução do modo " + modo);
            erroCapturado.get().printStackTrace(System.err);
            System.exit(1);
        }
    }

    private static void executarModo(String modo) throws Exception {
        switch (modo) {
            case "--teste" -> executarTestes();
            case "--rapido" -> {
                System.out.println("=== MODO RÁPIDO (n = 1000, 2000, 5000 | 5 execuções, 1 warm-up) ===");
                AmbienteUtil.salvarAmbiente(PASTA_RESULTADOS);
                int[] tamanhosRapidos = {1_000, 2_000, 5_000};
                SortBenchmark.executar(PASTA_RESULTADOS, tamanhosRapidos, tamanhosRapidos, 5, 1);
                SearchBenchmark.executar(PASTA_RESULTADOS, tamanhosRapidos, 5, 1);
                System.out.println("=== MODO RÁPIDO CONCLUÍDO COM SUCESSO ===");
            }
            case "--ordenacao" -> {
                System.out.println("=== BENCHMARK DE ORDENAÇÃO COMPLETO ===");
                AmbienteUtil.salvarAmbiente(PASTA_RESULTADOS);
                SortBenchmark.executarPadrao(PASTA_RESULTADOS);
            }
            case "--busca" -> {
                System.out.println("=== BENCHMARK DE BUSCA COMPLETO ===");
                AmbienteUtil.salvarAmbiente(PASTA_RESULTADOS);
                SearchBenchmark.executarPadrao(PASTA_RESULTADOS);
            }
            case "--tudo" -> {
                System.out.println("=== BENCHMARK COMPLETO (ORDENAÇÃO + BUSCA) ===");
                AmbienteUtil.salvarAmbiente(PASTA_RESULTADOS);
                SortBenchmark.executarPadrao(PASTA_RESULTADOS);
                SearchBenchmark.executarPadrao(PASTA_RESULTADOS);
                System.out.println("=== BENCHMARK COMPLETO CONCLUÍDO COM SUCESSO ===");
            }
            default -> {
                imprimirUso();
                throw new IllegalArgumentException("Argumento desconhecido: " + modo);
            }
        }
    }

    /**
     * Modo --teste: exercita todos os algoritmos de ordenação em vetores pequenos
     * (n = 0, 1, 2, 10, 1000) nos 4 cenários, compara com Arrays.sort e testa
     * as 5 buscas com alvos presentes e ausentes. Imprime OK ou FALHOU.
     */
    private static void executarTestes() {
        System.out.println("=== INICIANDO BATERIA DE TESTES (--teste) ===");
        int[] tamanhosTeste = {0, 1, 2, 10, 1_000};
        Cenario[] cenarios = Cenario.values();
        boolean tudoOk = true;

        // 1. Testes de ordenação
        for (SortBenchmark.AlgoritmoOrdenacao alg : SortBenchmark.ALGORITMOS_GRUPO_A) {
            boolean algoritmoOk = true;
            for (int n : tamanhosTeste) {
                for (Cenario cenario : cenarios) {
                    try {
                        int[] original = DataGenerator.gerar(cenario, n);
                        long somaEsperada = Verificador.calcularSoma(original);

                        int[] esperado = original.clone();
                        Arrays.sort(esperado);

                        int[] copia = original.clone();
                        alg.ordenador().accept(copia);

                        Verificador.verificarOrdenacao(copia, n, somaEsperada, alg.nome(), cenario.name(), n);
                        if (!Arrays.equals(copia, esperado)) {
                            algoritmoOk = false;
                            tudoOk = false;
                            System.out.printf("  [FALHOU] Ordenação %s (cenário=%s, n=%d): conteúdo diverge de Arrays.sort%n",
                                    alg.nome(), cenario.name(), n);
                        }
                    } catch (Throwable t) {
                        algoritmoOk = false;
                        tudoOk = false;
                        System.out.printf("  [FALHOU] Ordenação %s (cenário=%s, n=%d): %s%n",
                                alg.nome(), cenario.name(), n, t.getMessage());
                    }
                }
            }
            System.out.printf("[ORDENACAO] %-24s : %s%n", alg.nome(), algoritmoOk ? "OK" : "FALHOU");
        }

        // 2. Testes de busca
        for (SearchBenchmark.AlgoritmoBusca alg : SearchBenchmark.ALGORITMOS_BUSCA) {
            boolean buscaOk = true;
            for (int n : tamanhosTeste) {
                try {
                    int[] ordenado = DataGenerator.ordenado(n);

                    // Testa alvo ausente (-1 e 100001)
                    int idxAusente1 = alg.buscador().applyAsInt(ordenado, -1);
                    int idxAusente2 = alg.buscador().applyAsInt(ordenado, 100_001);
                    if (idxAusente1 != -1 || idxAusente2 != -1) {
                        buscaOk = false;
                        tudoOk = false;
                        System.out.printf("  [FALHOU] Busca %s (n=%d): alvo ausente retornou %d / %d%n",
                                alg.nome(), n, idxAusente1, idxAusente2);
                    }

                    // Testa alvos presentes (início, meio e fim) quando n > 0
                    if (n > 0) {
                        int[] posicoesAlvo = {0, n / 2, n - 1};
                        for (int pos : posicoesAlvo) {
                            int valorAlvo = ordenado[pos];
                            int idxEncontrado = alg.buscador().applyAsInt(ordenado, valorAlvo);
                            if (idxEncontrado < 0 || idxEncontrado >= n || ordenado[idxEncontrado] != valorAlvo) {
                                buscaOk = false;
                                tudoOk = false;
                                System.out.printf("  [FALHOU] Busca %s (n=%d): alvo presente %d retornou índice inválido %d%n",
                                        alg.nome(), n, valorAlvo, idxEncontrado);
                            }
                        }
                    }
                } catch (Throwable t) {
                    buscaOk = false;
                    tudoOk = false;
                    System.out.printf("  [FALHOU] Busca %s (n=%d): %s%n", alg.nome(), n, t.getMessage());
                }
            }
            System.out.printf("[BUSCA]     %-24s : %s%n", alg.nome(), buscaOk ? "OK" : "FALHOU");
        }

        System.out.println("---------------------------------------------");
        if (tudoOk) {
            System.out.println("RESULTADO GERAL: OK");
        } else {
            System.out.println("RESULTADO GERAL: FALHOU");
            throw new IllegalStateException("A bateria de testes falhou.");
        }
    }

    private static void imprimirUso() {
        System.out.println("Uso: java -Xmx4g -cp out br.edu.uepb.eda.Main <modo>");
        System.out.println("Modos disponíveis:");
        System.out.println("  --teste      Roda testes de corretude em vetores pequenos (n = 0, 1, 2, 10, 1000)");
        System.out.println("  --rapido     Roda pipeline completo reduzido (n = 1000, 2000, 5000; 5 execuções)");
        System.out.println("  --ordenacao  Roda apenas o benchmark real de ordenação (Grupos A e B)");
        System.out.println("  --busca      Roda apenas o benchmark real de busca");
        System.out.println("  --tudo       Roda todos os benchmarks reais (ordenação + busca)");
    }
}
