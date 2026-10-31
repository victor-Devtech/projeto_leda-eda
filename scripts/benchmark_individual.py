#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Executa o benchmark completo com uma JVM nova para cada combinação, uma por vez.

Na execução compartilhada, todos os algoritmos dividem a mesma JVM, e o JIT de um pode
influenciar o tempo do seguinte. Aqui cada combinação começa sem esse histórico, com o mesmo
protocolo (25 execuções, 5 de warm-up) e as mesmas entradas. Os resultados ficam em
resultados_individual/, e comparativo.csv compara as médias com as de resultados/.

Uso, a partir da raiz do repositório e após compilar em out/:
    python3 scripts/benchmark_individual.py
"""

import csv
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

CENARIOS = ["ALEATORIO", "ORDENADO", "INVERSO", "PARCIAL"]
GRUPO_A = ["BubbleSort.slide", "BubbleSort.otimizado", "SelectionSort.slide", "SelectionSort.estavel",
           "InsertionSort", "MergeSort.classico", "QuickSort.slide", "QuickSort.slideShuffle",
           "CountingSort", "Arrays.sort"]
GRUPO_B = ["MergeSort.classico", "QuickSort.slideShuffle", "CountingSort", "Arrays.sort"]
TAMANHOS_A = [10000, 25000, 50000]
TAMANHOS_B = [50000, 150000, 500000]
BUSCAS = ["linearIterativa", "linearRecursiva", "linearDuasPontas", "binariaIterativa", "binariaRecursiva"]
TAMANHOS_BUSCA = [50000, 150000, 500000]


def combinacoes_ordenacao():
    """Lista as combinações na mesma ordem da execução compartilhada."""
    lista = []
    for grupo, algoritmos, tamanhos in (("A", GRUPO_A, TAMANHOS_A), ("B", GRUPO_B, TAMANHOS_B)):
        for n in tamanhos:
            for cenario in CENARIOS:
                for algoritmo in algoritmos:
                    lista.append((grupo, algoritmo, cenario, n))
    return lista


def combinacoes_busca():
    return [(algoritmo, n) for n in TAMANHOS_BUSCA for algoritmo in BUSCAS]


def comparar(compartilhado, individual, chaves, coluna):
    """Compara as médias das duas execuções. A razão é individual dividido por compartilhado."""
    por_chave = {tuple(linha[c] for c in chaves): linha for linha in individual}
    comparativo = []
    for linha in compartilhado:
        chave = tuple(linha[c] for c in chaves)
        outra = por_chave.get(chave)
        if outra is None:
            raise ValueError(f"Combinação sem correspondente: {chave}")
        media_compartilhada = float(linha[coluna])
        media_individual = float(outra[coluna])
        item = {c: linha[c] for c in chaves}
        item["media_compartilhada"] = f"{media_compartilhada:.4f}"
        item["media_individual"] = f"{media_individual:.4f}"
        item["razao"] = f"{media_individual / media_compartilhada:.4f}"
        comparativo.append(item)
    return comparativo


def ler_csv(caminho):
    with open(caminho, newline="", encoding="utf-8") as arquivo:
        return list(csv.DictReader(arquivo))


def escrever_csv(caminho, linhas):
    with open(caminho, "w", newline="", encoding="utf-8") as arquivo:
        escritor = csv.DictWriter(arquivo, fieldnames=list(linhas[0].keys()), lineterminator="\n")
        escritor.writeheader()
        escritor.writerows(linhas)


def medir(classes, argumentos, prefixo):
    """Roda uma JVM nova em pasta temporária e devolve as linhas de resumo e bruto."""
    with tempfile.TemporaryDirectory() as temporaria:
        comando = ["java", "-Xmx4g", "-cp", str(classes), "br.edu.uepb.eda.Main", "--combinacao"] + argumentos
        subprocess.run(comando, cwd=temporaria, check=True, stdout=subprocess.DEVNULL)
        pasta = Path(temporaria) / "resultados"
        return (ler_csv(pasta / f"{prefixo}_resumo.csv"),
                ler_csv(pasta / f"{prefixo}_bruto.csv"),
                (pasta / "ambiente.txt").read_text(encoding="utf-8"))


def executar_todas(classes, combinacoes, montar_argumentos, prefixo, destino):
    resumos = []
    brutos = []
    ambiente = ""
    for indice, combinacao in enumerate(combinacoes, start=1):
        print(f"[{prefixo}] {indice}/{len(combinacoes)} {combinacao}", flush=True)
        resumo, bruto, ambiente = medir(classes, montar_argumentos(combinacao), prefixo)
        resumos.extend(resumo)
        brutos.extend(bruto)
    escrever_csv(destino / f"{prefixo}_resumo.csv", resumos)
    escrever_csv(destino / f"{prefixo}_bruto.csv", brutos)
    (destino / "ambiente.txt").write_text(ambiente, encoding="utf-8")
    return resumos


def main():
    classes = Path("out").resolve()
    if not (classes / "br" / "edu" / "uepb" / "eda" / "Main.class").exists():
        print("Erro: compile o projeto em out/ antes de executar.", file=sys.stderr)
        return 1

    destino = Path("resultados_individual")
    if destino.exists():
        shutil.rmtree(destino)
    destino.mkdir()

    ordenacao = executar_todas(
        classes, combinacoes_ordenacao(),
        lambda c: ["ordenacao", c[0], c[1], c[2], str(c[3])], "ordenacao", destino)
    busca = executar_todas(
        classes, combinacoes_busca(),
        lambda c: ["busca", c[0], str(c[1])], "busca", destino)

    referencia = Path("resultados")
    comparativo_ordenacao = comparar(
        ler_csv(referencia / "ordenacao_resumo.csv"), ordenacao, ["grupo", "algoritmo", "cenario", "n"], "media_ms")
    comparativo_busca = comparar(
        ler_csv(referencia / "busca_resumo.csv"), busca, ["algoritmo", "cenario", "n"], "media_ns")
    escrever_csv(destino / "comparativo_ordenacao.csv", comparativo_ordenacao)
    escrever_csv(destino / "comparativo_busca.csv", comparativo_busca)
    print("Benchmark individual concluído em resultados_individual/", flush=True)
    return 0


if __name__ == "__main__":
    sys.exit(main())
