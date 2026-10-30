package br.edu.uepb.eda.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Utilitário responsável por coletar e registrar as informações de hardware,
 * sistema operacional e máquina virtual Java em resultados/ambiente.txt.
 */
public final class AmbienteUtil {

    private AmbienteUtil() {
        // Classe utilitária não instanciável
    }

    /**
     * Grava o arquivo resultados/ambiente.txt com os metadados do ambiente de execução.
     *
     * @param pastaResultados caminho para o diretório resultados/
     * @throws IOException caso ocorra falha de escrita em disco
     */
    public static void salvarAmbiente(Path pastaResultados) throws IOException {
        Files.createDirectories(pastaResultados);
        Path arquivo = pastaResultados.resolve("ambiente.txt");

        String soNome = System.getProperty("os.name", "Desconhecido");
        String soVersao = System.getProperty("os.version", "Desconhecida");
        String arquitetura = System.getProperty("os.arch", "Desconhecida");
        int nucleos = Runtime.getRuntime().availableProcessors();
        long memoriaMaxBytes = Runtime.getRuntime().maxMemory();
        double memoriaMaxMb = memoriaMaxBytes / (1024.0 * 1024.0);
        String javaVersao = System.getProperty("java.version", "Desconhecida");
        String jvmNome = System.getProperty("java.vm.name", "Desconhecida");
        String javaVendor = System.getProperty("java.vendor", "Desconhecido");
        String dataHora = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(arquivo, StandardCharsets.UTF_8))) {
            out.println("=== AMBIENTE DE EXECUÇÃO DO BENCHMARK (EDA/LEDA - UEPB) ===");
            out.printf(Locale.US, "Sistema Operacional : %s (%s)%n", soNome, soVersao);
            out.printf(Locale.US, "Arquitetura         : %s%n", arquitetura);
            out.printf(Locale.US, "Numero de Nucleos   : %d%n", nucleos);
            out.printf(Locale.US, "Memoria Max. da JVM : %.2f MB (%d bytes)%n", memoriaMaxMb, memoriaMaxBytes);
            out.printf(Locale.US, "Versao do Java      : %s (%s - %s)%n", javaVersao, jvmNome, javaVendor);
            out.printf(Locale.US, "Data/Hora           : %s%n", dataHora);
        }
    }
}
