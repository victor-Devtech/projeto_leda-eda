# Projeto da Unidade 1 de EDA/LEDA (UEPB)

Implementação experimental e análise de desempenho de algoritmos de ordenação e busca em Java 17+, sem gerenciadores de dependências externos e operando exclusivamente sobre vetores primitivos `int[]`.

## Estrutura do Projeto

- `src/br/edu/uepb/eda/sorting/`: Algoritmos de ordenação (`BubbleSort`, `SelectionSort`, `InsertionSort`, `MergeSort`, `QuickSort`, `CountingSort`).
- `src/br/edu/uepb/eda/search/`: Algoritmos de busca (`Busca` com versões linear iterativa, linear recursiva, linear de duas pontas, binária iterativa e binária recursiva).
- `src/br/edu/uepb/eda/util/`: Geração determinística de dados (`DataGenerator`), verificação de integridade (`Verificador`), cálculos estatísticos (`Estatisticas`) e coleta de informações de ambiente (`AmbienteUtil`).
- `src/br/edu/uepb/eda/bench/`: Motores de medição de ordenação (`SortBenchmark`) e de busca (`SearchBenchmark`).
- `src/br/edu/uepb/eda/Main.java`: Ponto de entrada com suporte a múltiplos modos de execução em thread dedicada com pilha de 1 GB.
- `scripts/graficos.py`: Script Python (`pandas` e `matplotlib`) para geração automática de gráficos em escala logarítmica e tabelas em Markdown.

## Como Compilar e Executar

Para compilar diretamente com `javac` puro e executar todo o benchmark com 4 GB de memória máxima na JVM:

```bash
mkdir -p out
javac -d out $(find src -name "*.java") && java -Xmx4g -cp out br.edu.uepb.eda.Main --tudo
```

Também é possível utilizar os scripts de automação na raiz do repositório:

```bash
# Linux / macOS
chmod +x run.sh
./run.sh --teste
./run.sh --rapido
./run.sh --tudo

# Windows (Prompt de Comando)
run.bat --tudo
```

### Modos Disponíveis na Classe `Main`

- `--teste`: Executa todos os algoritmos de ordenação em vetores pequenos (`n = 0, 1, 2, 10, 1000`) nos quatro cenários, confere o resultado contra `Arrays.sort` e exercita as cinco buscas com alvos presentes e ausentes.
- `--rapido`: Executa todo o fluxo de medição com tamanhos reduzidos (`n = 1000, 2000, 5000`) e 5 repetições (descartando 1 de aquecimento), permitindo conferir rapidamente a geração de arquivos CSV e gráficos.
- `--ordenacao`: Executa apenas o benchmark completo de ordenação (Grupos A e B).
- `--busca`: Executa apenas o benchmark completo de busca.
- `--tudo`: Executa os benchmarks reais completos de ordenação e busca em sequência.

## Como Gerar Gráficos e Tabelas

Após a execução de `--rapido` ou `--tudo`, gere os gráficos em `resultados/graficos/` e o relatório tabular em `resultados/tabelas.md` executando:

```bash
python3 scripts/graficos.py
```

Arquivos gerados na pasta `resultados/`:

- `ambiente.txt`: Especificações do sistema operacional, arquitetura, núcleos de CPU, memória da JVM, versão do Java e carimbo de data/hora.
- `ordenacao_bruto.csv` e `ordenacao_resumo.csv`: Tempos individuais por execução e estatísticas consolidadas (média, desvio padrão, mediana, mínimo e máximo em milissegundos).
- `busca_bruto.csv` e `busca_resumo.csv`: Tempos por busca individual e estatísticas consolidadas em nanossegundos.
- `tabelas.md`: Tabelas formatadas em Markdown com `média ± desvio` para cada cenário e para as buscas.
- `graficos/`: Gráficos de linha por grupo e cenário, gráfico de barras agrupadas comparando os quatro cenários e gráfico de desempenho das buscas.

## Metodologia Resumida

1. **Dados de Entrada Reprodutíveis**: Todos os experimentos operam exclusivamente sobre arrays `int[]` com valores inteiros pertencentes ao intervalo `[1, 100000]`, permitindo repetições. O gerador utiliza `Random(42)` com semente fixa para assegurar total reprodutibilidade entre execuções.
2. **Quatro Cenários de Avaliação**:
   - `ALEATORIO`: Elementos sorteados uniformemente em `[1, 100000]`.
   - `ORDENADO`: Vetor aleatório ordenado em ordem não-decrescente.
   - `INVERSO`: Vetor ordenado invertido em ordem não-crescente.
   - `PARCIAL`: Parte do vetor ordenado, sorteia $m = \text{round}(0{,}15 \cdot n)$ posições distintas por meio de um embaralhamento Fisher-Yates de índices e embaralha com Fisher-Yates apenas os valores dessas $m$ posições entre si.
3. **Grupos de Ordenação**:
   - **Grupo A** (`n = 10000, 25000, 50000`): Todos os 10 algoritmos avaliados (`BubbleSort.slide`, `BubbleSort.otimizado`, `SelectionSort.slide`, `SelectionSort.estavel`, `InsertionSort`, `MergeSort.classico`, `QuickSort.slide`, `QuickSort.slideShuffle`, `CountingSort` e `Arrays.sort`).
   - **Grupo B** (`n = 50000, 150000, 500000`): Algoritmos eficientes para grandes volumes (`MergeSort.classico`, `QuickSort.slideShuffle`, `CountingSort` e `Arrays.sort`).
4. **Protocolo de Medição e Isolamento**:
   - Cada combinação de algoritmo, tamanho e cenário é executada 25 vezes. As 5 primeiras execuções são destinadas ao aquecimento da JVM (*warm-up*) e descartadas do cálculo das estatísticas.
   - Antes de cada execução, o algoritmo recebe um clone independente (`entradaOriginal.clone()`) produzido fora do intervalo medido por `System.nanoTime()`.
   - Após cada execução, fora do tempo medido, confere-se se o vetor resultante possui o mesmo comprimento, a mesma soma total acumulada em `long` e se está em ordem não-decrescente.
   - Se uma única execução ultrapassar 120 segundos, a combinação é interrompida e registrada como `INVIAVEL`.
5. **Experimentos de Busca em Lote**:
   - Executados sempre sobre vetores ordenados (`n = 50000, 150000, 500000`) buscando o valor `-1` (ausente do domínio `[1, 100000]`), forçando o pior caso em todas as estratégias.
   - As buscas lineares não realizam parada antecipada por ordenação para refletir fielmente o custo assintótico $O(n)$.
   - Em razão do tempo sub-microssegundo das buscas binárias, cada medição executa um lote de 10000 chamadas consecutivas para as versões binárias e 1 chamada para as versões lineares, dividindo o tempo total pelo tamanho do lote e acumulando os retornos em uma variável `sink` para impedir otimizações de eliminação de código morto pelo compilador JIT.
6. **Suporte a Recursão Profunda**: Todo o pipeline roda dentro de uma thread dedicada instanciada com pilha de 1 GB (`1L << 30`), evitando estouro de pilha em chamadas recursivas de profundidade $O(n)$.
