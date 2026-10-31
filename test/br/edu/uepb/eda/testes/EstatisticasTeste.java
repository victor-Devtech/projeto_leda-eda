package br.edu.uepb.eda.testes;

import br.edu.uepb.eda.util.Estatisticas;

/**
 * Testa as estatísticas reportadas, com valores calculados à mão.
 */
public class EstatisticasTeste {

    private static final double TOLERANCIA = 1e-9;

    @Teste
    public void rejeitaAmostraNulaOuVazia() {
        Apoio.lanca(IllegalArgumentException.class, () -> Estatisticas.calcular(null), "amostra nula");
        Apoio.lanca(IllegalArgumentException.class, () -> Estatisticas.calcular(new double[]{}), "amostra vazia");
    }

    @Teste
    public void calculaAmostraConhecidaComTamanhoPar() {
        Estatisticas est = Estatisticas.calcular(new double[]{9, 2, 4, 4, 5, 7, 4, 5});
        Apoio.proximo(5.0, est.getMedia(), TOLERANCIA, "média");
        Apoio.proximo(4.5, est.getMediana(), TOLERANCIA, "mediana com tamanho par");
        Apoio.proximo(2.0, est.getMinimo(), TOLERANCIA, "mínimo");
        Apoio.proximo(9.0, est.getMaximo(), TOLERANCIA, "máximo");
        // Soma dos quadrados dos desvios = 32, dividida por n - 1 = 7 (desvio padrão amostral)
        Apoio.proximo(Math.sqrt(32.0 / 7.0), est.getDesvioPadrao(), TOLERANCIA, "desvio padrão amostral");
    }

    @Teste
    public void calculaMedianaComTamanhoImpar() {
        Apoio.proximo(2.0, Estatisticas.calcular(new double[]{3, 1, 2}).getMediana(), TOLERANCIA, "mediana");
    }

    @Teste
    public void amostraUnicaTemDesvioZero() {
        Estatisticas est = Estatisticas.calcular(new double[]{7.5});
        Apoio.proximo(7.5, est.getMedia(), TOLERANCIA, "média");
        Apoio.proximo(7.5, est.getMediana(), TOLERANCIA, "mediana");
        Apoio.proximo(0.0, est.getDesvioPadrao(), TOLERANCIA, "desvio");
    }

    @Teste
    public void naoAlteraAAmostraOriginal() {
        double[] amostras = {3, 1, 2};
        Estatisticas.calcular(amostras);
        Apoio.proximo(3.0, amostras[0], TOLERANCIA, "a amostra foi reordenada");
        Apoio.proximo(1.0, amostras[1], TOLERANCIA, "a amostra foi reordenada");
    }
}
