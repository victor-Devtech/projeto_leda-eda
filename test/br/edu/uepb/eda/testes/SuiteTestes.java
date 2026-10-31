package br.edu.uepb.eda.testes;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Executor dos testes unitários do projeto. Roda cada método anotado com {@link Teste}
 * em uma instância nova, imprime o resultado e encerra com código 1 se algo falhar.
 */
public final class SuiteTestes {

    private static final List<Class<?>> CLASSES = List.of(
            OrdenacaoTeste.class,
            BuscaTeste.class,
            DataGeneratorTeste.class,
            VerificadorTeste.class,
            EstatisticasTeste.class,
            SortBenchmarkTeste.class,
            SearchBenchmarkTeste.class,
            AmbienteUtilTeste.class,
            MainTeste.class
    );

    private SuiteTestes() {
        // Classe principal não instanciável
    }

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.US);
        int total = 0;
        List<String> falhas = new ArrayList<>();

        for (Class<?> classe : CLASSES) {
            List<Method> metodos = new ArrayList<>();
            for (Method metodo : classe.getDeclaredMethods()) {
                if (metodo.isAnnotationPresent(Teste.class)) {
                    metodos.add(metodo);
                }
            }
            metodos.sort(Comparator.comparing(Method::getName));

            System.out.printf("%n%s%n", classe.getSimpleName());
            for (Method metodo : metodos) {
                total++;
                long inicio = System.nanoTime();
                Throwable erro = executar(classe, metodo);
                long ms = (System.nanoTime() - inicio) / 1_000_000L;
                if (erro == null) {
                    System.out.printf("  [OK]     %-52s %6d ms%n", metodo.getName(), ms);
                } else {
                    System.out.printf("  [FALHOU] %-52s %6d ms%n           %s%n", metodo.getName(), ms, erro);
                    falhas.add(classe.getSimpleName() + "." + metodo.getName());
                }
            }
        }

        System.out.printf("%n---------------------------------------------%n");
        System.out.printf("Testes: %d | Passaram: %d | Falharam: %d%n", total, total - falhas.size(), falhas.size());
        if (!falhas.isEmpty()) {
            falhas.forEach(falha -> System.out.println("  - " + falha));
            System.out.println("RESULTADO GERAL: FALHOU");
            System.exit(1);
        }
        System.out.println("RESULTADO GERAL: OK");
    }

    private static Throwable executar(Class<?> classe, Method metodo) {
        try {
            Object instancia = classe.getDeclaredConstructor().newInstance();
            metodo.invoke(instancia);
            return null;
        } catch (InvocationTargetException e) {
            return e.getCause();
        } catch (Throwable t) {
            return t;
        }
    }
}
