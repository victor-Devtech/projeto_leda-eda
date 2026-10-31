package br.edu.uepb.eda.testes;

import br.edu.uepb.eda.util.Verificador;

/**
 * Testa se a verificação pós-execução aceita saídas corretas e denuncia cada tipo de saída errada.
 */
public class VerificadorTeste {

    private static IllegalStateException falha(int[] vetor, int tamanho, long soma) {
        return Apoio.lanca(IllegalStateException.class,
                () -> Verificador.verificarOrdenacao(vetor, tamanho, soma, "AlgoritmoX", "CENARIOY", 123),
                "a verificação deveria falhar");
    }

    @Teste
    public void somaUsaLongSemEstourar() {
        int[] vetor = {Integer.MAX_VALUE, Integer.MAX_VALUE, 2};
        Apoio.igual(2L * Integer.MAX_VALUE + 2L, Verificador.calcularSoma(vetor), "soma além do limite de int");
        Apoio.igual(0L, Verificador.calcularSoma(new int[]{}), "soma de vetor vazio");
    }

    @Teste
    public void aceitaVetorOrdenadoComRepeticoes() {
        Verificador.verificarOrdenacao(new int[]{1, 2, 2, 3}, 4, 8L, "A", "C", 4);
        Verificador.verificarOrdenacao(new int[]{}, 0, 0L, "A", "C", 0);
    }

    @Teste
    public void rejeitaVetorNulo() {
        Apoio.verdadeiro(falha(null, 3, 6L).getMessage().contains("nulo"), "mensagem de vetor nulo");
    }

    @Teste
    public void rejeitaTamanhoDivergente() {
        Apoio.verdadeiro(falha(new int[]{1, 2}, 3, 3L).getMessage().contains("tamanho"), "mensagem de tamanho");
    }

    @Teste
    public void rejeitaVetorForaDeOrdem() {
        Apoio.verdadeiro(falha(new int[]{1, 3, 2}, 3, 6L).getMessage().contains("fora de ordem"), "mensagem de ordem");
    }

    @Teste
    public void rejeitaSomaDivergente() {
        Apoio.verdadeiro(falha(new int[]{1, 2, 4}, 3, 6L).getMessage().contains("soma"), "mensagem de soma");
    }

    @Teste
    public void mensagemIdentificaOExperimento() {
        String mensagem = falha(new int[]{2, 1}, 2, 3L).getMessage();
        Apoio.verdadeiro(mensagem.contains("AlgoritmoX"), "mensagem sem o algoritmo: " + mensagem);
        Apoio.verdadeiro(mensagem.contains("CENARIOY"), "mensagem sem o cenário: " + mensagem);
        Apoio.verdadeiro(mensagem.contains("123"), "mensagem sem o tamanho: " + mensagem);
    }
}
