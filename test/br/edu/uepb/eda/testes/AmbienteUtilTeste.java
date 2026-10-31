package br.edu.uepb.eda.testes;

import br.edu.uepb.eda.util.AmbienteUtil;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Testa o registro do ambiente de execução, que entra na metodologia do relatório.
 */
public class AmbienteUtilTeste {

    @Teste
    public void gravaOsDadosDoAmbiente() throws Exception {
        Path pasta = Files.createTempDirectory("teste-ambiente");
        Path arquivo = pasta.resolve("ambiente.txt");
        try {
            AmbienteUtil.salvarAmbiente(pasta);
            String conteudo = Files.readString(arquivo);
            Apoio.verdadeiro(conteudo.contains("Sistema Operacional"), "sem o sistema operacional");
            Apoio.verdadeiro(conteudo.contains("Numero de Nucleos"), "sem o número de núcleos");
            Apoio.verdadeiro(conteudo.contains("Memoria Max. da JVM"), "sem a memória da JVM");
            Apoio.verdadeiro(conteudo.contains(System.getProperty("java.version")), "sem a versão do Java");
        } finally {
            Files.deleteIfExists(arquivo);
            Files.deleteIfExists(pasta);
        }
    }
}
