package br.edu.uepb.eda.testes;

import br.edu.uepb.eda.Main;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Testa a seleção de modos da classe Main e a pilha usada na recursão profunda.
 */
public class MainTeste {

    private static void executarModo(String modo) throws Throwable {
        Method metodo = Main.class.getDeclaredMethod("executarModo", String.class);
        metodo.setAccessible(true);
        try {
            metodo.invoke(null, modo);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    @Teste
    public void combinacaoComArgumentosInvalidosEhRejeitada() throws Throwable {
        Method metodo = Main.class.getDeclaredMethod("executarCombinacao", String[].class);
        metodo.setAccessible(true);
        Apoio.capturarSaida(() -> Apoio.lanca(IllegalArgumentException.class, () -> {
            try {
                metodo.invoke(null, (Object) new String[]{"--combinacao", "ordenacao", "A"});
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }, "argumentos incompletos"));
    }

    @Teste
    public void pilhaTemUmGigabyte() throws Exception {
        Field campo = Main.class.getDeclaredField("TAMANHO_PILHA_BYTES");
        campo.setAccessible(true);
        Apoio.igual(1L << 30, campo.getLong(null), "tamanho da pilha");
    }

    @Teste
    public void modoDesconhecidoEhRejeitado() throws Throwable {
        Apoio.capturarSaida(() -> Apoio.lanca(IllegalArgumentException.class,
                () -> executarModo("--inexistente"), "modo desconhecido"));
    }

    @Teste
    public void modoTesteTerminaComSucesso() throws Throwable {
        String saida = Apoio.capturarSaida(() -> executarModo("--teste"));
        Apoio.verdadeiro(saida.contains("RESULTADO GERAL: OK"), "a bateria --teste não terminou com OK");
    }
}
