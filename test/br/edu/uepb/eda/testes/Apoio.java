package br.edu.uepb.eda.testes;

import br.edu.uepb.eda.Main;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Asserções e utilitários compartilhados pelos testes, sem dependências externas.
 */
final class Apoio {

    @FunctionalInterface
    interface Executavel {
        void executar() throws Throwable;
    }

    private Apoio() {
        // Classe utilitária não instanciável
    }

    static void verdadeiro(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }

    static void igual(Object esperado, Object obtido, String mensagem) {
        if (!java.util.Objects.equals(esperado, obtido)) {
            throw new AssertionError(mensagem + " (esperado=" + esperado + ", obtido=" + obtido + ")");
        }
    }

    static void igual(int[] esperado, int[] obtido, String mensagem) {
        if (!Arrays.equals(esperado, obtido)) {
            throw new AssertionError(mensagem + " (esperado=" + resumir(esperado) + ", obtido=" + resumir(obtido) + ")");
        }
    }

    static void proximo(double esperado, double obtido, double tolerancia, String mensagem) {
        if (Math.abs(esperado - obtido) > tolerancia) {
            throw new AssertionError(mensagem + " (esperado=" + esperado + ", obtido=" + obtido + ")");
        }
    }

    /**
     * Exige que o trecho lance a exceção indicada e a devolve para conferências adicionais.
     */
    static <T extends Throwable> T lanca(Class<T> tipo, Executavel trecho, String mensagem) {
        try {
            trecho.executar();
        } catch (Throwable t) {
            if (tipo.isInstance(t)) {
                return tipo.cast(t);
            }
            throw new AssertionError(mensagem + " (lançou " + t + " em vez de " + tipo.getSimpleName() + ")", t);
        }
        throw new AssertionError(mensagem + " (nenhuma exceção lançada)");
    }

    /**
     * Executa o trecho com a saída padrão desviada e devolve o que foi impresso.
     */
    static String capturarSaida(Executavel trecho) throws Throwable {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8));
        try {
            trecho.executar();
        } finally {
            System.setOut(original);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }

    /**
     * Executa o trecho em uma thread com a mesma pilha usada pela classe Main no benchmark,
     * garantindo que a recursão profunda seja testada nas condições reais.
     */
    static void comPilhaDoBenchmark(Executavel trecho) throws Throwable {
        Field campo = Main.class.getDeclaredField("TAMANHO_PILHA_BYTES");
        campo.setAccessible(true);
        long tamanhoPilha = campo.getLong(null);

        AtomicReference<Throwable> erro = new AtomicReference<>();
        Thread thread = new Thread(null, () -> {
            try {
                trecho.executar();
            } catch (Throwable t) {
                erro.set(t);
            }
        }, "teste-pilha", tamanhoPilha);
        thread.start();
        thread.join();
        if (erro.get() != null) {
            throw erro.get();
        }
    }

    private static String resumir(int[] vetor) {
        if (vetor == null) {
            return "null";
        }
        if (vetor.length <= 20) {
            return Arrays.toString(vetor);
        }
        return "int[" + vetor.length + "]";
    }
}
