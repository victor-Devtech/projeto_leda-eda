package br.edu.uepb.eda.testes;

import br.edu.uepb.eda.bench.SearchBenchmark;
import br.edu.uepb.eda.bench.SearchBenchmark.AlgoritmoBusca;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Testa a configuração e o protocolo do benchmark de busca com vetores pequenos e pastas temporárias.
 */
public class SearchBenchmarkTeste {

    @Teste
    public void configuracaoSegueOEnunciado() {
        Apoio.igual(-1, SearchBenchmark.ALVO_AUSENTE, "alvo ausente");
        Apoio.igual(25, SearchBenchmark.TOTAL_EXECUCOES_PADRAO, "total de execuções");
        Apoio.igual(5, SearchBenchmark.WARMUP_EXECUCOES_PADRAO, "execuções de warm-up");
        Apoio.igual(new int[]{50_000, 150_000, 500_000}, SearchBenchmark.TAMANHOS_PADRAO, "tamanhos");
    }

    @Teste
    public void cincoBuscasComOsLotesCorretos() {
        Map<String, Integer> lotes = new HashMap<>();
        for (AlgoritmoBusca alg : SearchBenchmark.ALGORITMOS_BUSCA) {
            lotes.put(alg.nome(), alg.tamanhoLote());
        }
        Apoio.igual(Map.of(
                "linearIterativa", 1,
                "linearRecursiva", 1,
                "linearDuasPontas", 1,
                "binariaIterativa", 10_000,
                "binariaRecursiva", 10_000), lotes, "buscas e tamanhos de lote");
    }

    @Teste
    public void execucaoGeraOsCsvsEIgnoraOWarmupNoResumo() throws Throwable {
        Path pasta = Files.createTempDirectory("teste-busca");
        try {
            int total = 5;
            int warmup = 2;
            Apoio.capturarSaida(() -> SearchBenchmark.executar(pasta, new int[]{100, 200}, total, warmup));

            List<String> bruto = Files.readAllLines(pasta.resolve("busca_bruto.csv"));
            List<String> resumo = Files.readAllLines(pasta.resolve("busca_resumo.csv"));
            Apoio.igual("algoritmo,cenario,n,execucao,warmup,tempo_ns", bruto.get(0), "cabeçalho do bruto");
            Apoio.igual("algoritmo,cenario,n,media_ns,desvio_ns,mediana_ns,min_ns,max_ns,status",
                    resumo.get(0), "cabeçalho do resumo");
            Apoio.igual(1 + 2 * 5 * total, bruto.size(), "linhas do bruto");
            Apoio.igual(1 + 2 * 5, resumo.size(), "linhas do resumo");

            Map<String, List<Double>> validos = new HashMap<>();
            for (String linha : bruto.subList(1, bruto.size())) {
                String[] c = linha.split(",");
                Apoio.igual("ORDENADO", c[1], "as buscas usam vetor ordenado");
                boolean esperado = Integer.parseInt(c[3]) <= warmup;
                Apoio.igual(esperado, Boolean.parseBoolean(c[4]), "marcação de warm-up: " + linha);
                if (!esperado) {
                    validos.computeIfAbsent(c[0] + c[2], k -> new ArrayList<>()).add(Double.parseDouble(c[5]));
                }
            }
            for (String linha : resumo.subList(1, resumo.size())) {
                String[] c = linha.split(",");
                List<Double> tempos = validos.get(c[0] + c[2]);
                Apoio.igual(3, tempos.size(), "amostras válidas de " + linha);
                double media = tempos.stream().mapToDouble(Double::doubleValue).average().orElseThrow();
                Apoio.proximo(media, Double.parseDouble(c[3]), 1e-3, "média de " + linha);
                Apoio.igual("OK", c[8], "status de " + linha);
            }
        } finally {
            apagar(pasta);
        }
    }

    /**
     * Um lote tem 10 mil chamadas e leva dezenas de microssegundos. Um registro abaixo de 5 µs
     * só é possível se o tempo do lote tiver sido dividido pelo número de chamadas.
     */
    @Teste
    public void tempoDaBinariaEhPorBuscaENaoPorLote() throws Throwable {
        Path pasta = Files.createTempDirectory("teste-lote");
        try {
            Apoio.capturarSaida(() -> SearchBenchmark.executar(pasta, new int[]{100}, 10, 5));
            double menor = Double.MAX_VALUE;
            for (String linha : Files.readAllLines(pasta.resolve("busca_bruto.csv"))) {
                if (linha.startsWith("binariaIterativa")) {
                    menor = Math.min(menor, Double.parseDouble(linha.split(",")[5]));
                }
            }
            Apoio.verdadeiro(menor < 5_000.0, "o tempo parece ser do lote inteiro: " + menor + " ns");
        } finally {
            apagar(pasta);
        }
    }

    @Teste
    public void combinacaoUnicaMedeSoOQueFoiPedido() throws Throwable {
        Path pasta = Files.createTempDirectory("teste-combinacao-busca");
        try {
            Apoio.capturarSaida(() -> SearchBenchmark.executarCombinacao(pasta, "binariaRecursiva", 200, 5, 2));
            List<String> resumo = Files.readAllLines(pasta.resolve("busca_resumo.csv"));
            Apoio.igual(2, resumo.size(), "uma combinação no resumo");
            Apoio.verdadeiro(resumo.get(1).startsWith("binariaRecursiva,ORDENADO,200,"), "linha: " + resumo.get(1));
            Apoio.igual(6, Files.readAllLines(pasta.resolve("busca_bruto.csv")).size(), "uma linha por execução");
            Apoio.lanca(IllegalArgumentException.class,
                    () -> SearchBenchmark.executarCombinacao(pasta, "inexistente", 10, 2, 1), "busca inexistente");
        } finally {
            apagar(pasta);
        }
    }

    @Teste
    public void buscaQueEncontraOAlvoAusenteInterrompeOBenchmark() throws Throwable {
        Path pasta = Files.createTempDirectory("teste-busca-errada");
        try {
            AlgoritmoBusca errada = new AlgoritmoBusca("BuscaErrada", (vetor, alvo) -> 0, 1);
            Method metodo = SearchBenchmark.class.getDeclaredMethod(
                    "executar", Path.class, List.class, int[].class, int.class, int.class);
            metodo.setAccessible(true);
            IllegalStateException erro = Apoio.lanca(IllegalStateException.class, () -> Apoio.capturarSaida(() -> {
                try {
                    metodo.invoke(null, pasta, List.of(errada), new int[]{100}, 2, 1);
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            }), "a busca errada deveria interromper");
            Apoio.verdadeiro(erro.getMessage().contains("BuscaErrada"), "mensagem sem a busca: " + erro.getMessage());
        } finally {
            apagar(pasta);
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
