package br.edu.uepb.eda.testes;

import br.edu.uepb.eda.util.Cenario;
import br.edu.uepb.eda.util.DataGenerator;

import java.util.Arrays;

/**
 * Testa se os quatro cenários seguem o enunciado: valores em [1, 100000], repetições
 * permitidas, reprodutibilidade e 15% das posições embaralhadas no cenário parcial.
 */
public class DataGeneratorTeste {

    private static final int[] TAMANHOS = {1_000, 50_000, 150_000};

    @Teste
    public void existemExatamenteOsQuatroCenarios() {
        Apoio.igual(4, Cenario.values().length, "quantidade de cenários");
    }

    @Teste
    public void tamanhoNegativoLancaExcecaoETamanhoZeroFicaVazio() {
        Apoio.lanca(IllegalArgumentException.class, () -> DataGenerator.aleatorio(-1), "tamanho negativo");
        for (Cenario cenario : Cenario.values()) {
            Apoio.igual(0, DataGenerator.gerar(cenario, 0).length, "n=0 em " + cenario);
        }
    }

    @Teste
    public void todosOsCenariosRespeitamTamanhoEIntervalo() {
        for (Cenario cenario : Cenario.values()) {
            for (int n : TAMANHOS) {
                int[] vetor = DataGenerator.gerar(cenario, n);
                Apoio.igual(n, vetor.length, "tamanho em " + cenario);
                for (int valor : vetor) {
                    Apoio.verdadeiro(valor >= 1 && valor <= 100_000, "valor fora do intervalo: " + valor);
                }
            }
        }
    }

    @Teste
    public void repeticoesSaoPermitidas() {
        int[] ordenado = DataGenerator.ordenado(50_000);
        boolean repetiu = false;
        for (int i = 1; i < ordenado.length && !repetiu; i++) {
            repetiu = ordenado[i] == ordenado[i - 1];
        }
        Apoio.verdadeiro(repetiu, "o vetor deveria conter valores repetidos");
    }

    @Teste
    public void geracaoEReprodutivel() {
        for (Cenario cenario : Cenario.values()) {
            Apoio.igual(DataGenerator.gerar(cenario, 10_000), DataGenerator.gerar(cenario, 10_000),
                    "duas gerações de " + cenario + " devem ser idênticas");
        }
    }

    @Teste
    public void gerarDespachaParaOCenarioCorreto() {
        int n = 2_000;
        Apoio.igual(DataGenerator.aleatorio(n), DataGenerator.gerar(Cenario.ALEATORIO, n), "ALEATORIO");
        Apoio.igual(DataGenerator.ordenado(n), DataGenerator.gerar(Cenario.ORDENADO, n), "ORDENADO");
        Apoio.igual(DataGenerator.inverso(n), DataGenerator.gerar(Cenario.INVERSO, n), "INVERSO");
        Apoio.igual(DataGenerator.parcialmenteOrdenado(n), DataGenerator.gerar(Cenario.PARCIAL, n), "PARCIAL");
    }

    @Teste
    public void aleatorioNaoEstaOrdenado() {
        int[] vetor = DataGenerator.aleatorio(10_000);
        int[] ordenado = vetor.clone();
        Arrays.sort(ordenado);
        Apoio.verdadeiro(!Arrays.equals(vetor, ordenado), "o cenário aleatório veio ordenado");
    }

    @Teste
    public void ordenadoEstaEmOrdemNaoDecrescente() {
        for (int n : TAMANHOS) {
            int[] vetor = DataGenerator.ordenado(n);
            for (int i = 1; i < n; i++) {
                Apoio.verdadeiro(vetor[i - 1] <= vetor[i], "fora de ordem na posição " + i);
            }
        }
    }

    @Teste
    public void inversoEOOrdenadoAoContrario() {
        for (int n : TAMANHOS) {
            int[] ordenado = DataGenerator.ordenado(n);
            int[] inverso = DataGenerator.inverso(n);
            for (int i = 0; i < n; i++) {
                Apoio.igual(ordenado[n - 1 - i], inverso[i], "posição " + i);
            }
        }
    }

    @Teste
    public void todosOsCenariosTemOsMesmosElementos() {
        for (int n : TAMANHOS) {
            int[] esperado = DataGenerator.ordenado(n);
            for (Cenario cenario : Cenario.values()) {
                int[] vetor = DataGenerator.gerar(cenario, n);
                Arrays.sort(vetor);
                Apoio.igual(esperado, vetor, "elementos de " + cenario + " n=" + n);
            }
        }
    }

    @Teste
    public void parcialAlteraAproximadamente15PorCentoDasPosicoes() {
        for (int n : TAMANHOS) {
            int[] ordenado = DataGenerator.ordenado(n);
            int[] parcial = DataGenerator.parcialmenteOrdenado(n);
            int alteradas = 0;
            for (int i = 0; i < n; i++) {
                if (ordenado[i] != parcial[i]) {
                    alteradas++;
                }
            }
            int m = (int) Math.round(0.15 * n);
            Apoio.verdadeiro(alteradas <= m, "alterou mais de 15%: " + alteradas + " de " + n);
            // Uma posição sorteada pode manter o mesmo valor, então a margem inferior é de 10%
            Apoio.verdadeiro(alteradas >= 0.9 * m, "alterou menos do que o esperado: " + alteradas + " de " + n);
        }
    }

    @Teste
    public void parcialEspalhaAsPosicoesAlteradasPeloVetor() {
        int n = 50_000;
        int[] ordenado = DataGenerator.ordenado(n);
        int[] parcial = DataGenerator.parcialmenteOrdenado(n);
        int faixas = 10;
        int[] alteradasPorFaixa = new int[faixas];
        for (int i = 0; i < n; i++) {
            if (ordenado[i] != parcial[i]) {
                alteradasPorFaixa[i * faixas / n]++;
            }
        }
        for (int quantidade : alteradasPorFaixa) {
            Apoio.verdadeiro(quantidade > 0, "faixa sem posições alteradas: " + Arrays.toString(alteradasPorFaixa));
        }
    }

    @Teste
    public void parcialEmVetorMuitoPequenoPermaneceOrdenado() {
        Apoio.igual(DataGenerator.ordenado(5), DataGenerator.parcialmenteOrdenado(5), "n=5 sorteia só 1 posição");
    }
}
