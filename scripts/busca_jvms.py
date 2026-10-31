#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Executa o benchmark de busca em várias JVMs separadas e consolida os resultados.

A compilação do JIT varia de uma JVM para outra, e essa variação pode superar a diferença
prevista pela teoria para a busca binária. Por isso, cada JVM executa o protocolo completo
(25 execuções, 5 de warm-up) e o resumo final usa a mediana entre as JVMs.

Colunas de resultados/busca_resumo.csv após a consolidação:
- media_ns: mediana das médias de cada JVM;
- desvio_ns: desvio padrão amostral entre as médias das JVMs;
- mediana_ns: mediana das medianas de cada JVM;
- min_ns e max_ns: menor mínimo e maior máximo entre as JVMs.

Uso, a partir da raiz do repositório e após compilar em out/:
    python3 scripts/busca_jvms.py [quantidade de JVMs, padrão 5]
"""

import csv
import shutil
import statistics
import subprocess
import sys
import tempfile
from pathlib import Path

JVMS_PADRAO = 5
CABECALHO_RESUMO = ["algoritmo", "cenario", "n", "media_ns", "desvio_ns", "mediana_ns", "min_ns", "max_ns", "status"]


def consolidar_resumos(resumos):
    """Recebe uma lista de resumos (um por JVM, cada um com linhas em dicionário) e devolve as linhas finais."""
    if len(resumos) < 2:
        raise ValueError("A consolidação exige ao menos 2 JVMs.")

    chaves = [(linha["algoritmo"], linha["cenario"], linha["n"]) for linha in resumos[0]]
    for resumo in resumos[1:]:
        if [(linha["algoritmo"], linha["cenario"], linha["n"]) for linha in resumo] != chaves:
            raise ValueError("As JVMs não mediram as mesmas combinações.")

    consolidado = []
    for indice, (algoritmo, cenario, n) in enumerate(chaves):
        linhas = [resumo[indice] for resumo in resumos]
        if any(linha["status"] != "OK" for linha in linhas):
            raise ValueError(f"Status diferente de OK em {algoritmo}, n={n}.")
        medias = [float(linha["media_ns"]) for linha in linhas]
        consolidado.append({
            "algoritmo": algoritmo,
            "cenario": cenario,
            "n": n,
            "media_ns": f"{statistics.median(medias):.4f}",
            "desvio_ns": f"{statistics.stdev(medias):.4f}",
            "mediana_ns": f"{statistics.median(float(linha['mediana_ns']) for linha in linhas):.4f}",
            "min_ns": f"{min(float(linha['min_ns']) for linha in linhas):.4f}",
            "max_ns": f"{max(float(linha['max_ns']) for linha in linhas):.4f}",
            "status": "OK",
        })
    return consolidado


def ler_csv(caminho):
    with open(caminho, newline="", encoding="utf-8") as arquivo:
        return list(csv.DictReader(arquivo))


def escrever_csv(caminho, cabecalho, linhas):
    with open(caminho, "w", newline="", encoding="utf-8") as arquivo:
        escritor = csv.DictWriter(arquivo, fieldnames=cabecalho, lineterminator="\n")
        escritor.writeheader()
        escritor.writerows(linhas)


def executar_jvm(classes, destino_resumo, destino_bruto):
    """Executa uma JVM em pasta temporária, para não misturar com a saída das demais."""
    with tempfile.TemporaryDirectory() as temporaria:
        subprocess.run(
            ["java", "-Xmx4g", "-cp", str(classes), "br.edu.uepb.eda.Main", "--busca"],
            cwd=temporaria, check=True, stdout=subprocess.DEVNULL,
        )
        shutil.copyfile(Path(temporaria) / "resultados" / "busca_resumo.csv", destino_resumo)
        shutil.copyfile(Path(temporaria) / "resultados" / "busca_bruto.csv", destino_bruto)


def main():
    quantidade = int(sys.argv[1]) if len(sys.argv) > 1 else JVMS_PADRAO
    classes = Path("out").resolve()
    if not (classes / "br" / "edu" / "uepb" / "eda" / "Main.class").exists():
        print("Erro: compile o projeto em out/ antes de executar.", file=sys.stderr)
        return 1

    resultados = Path("resultados")
    pasta_jvms = resultados / "busca_jvms"
    pasta_jvms.mkdir(parents=True, exist_ok=True)

    resumos = []
    brutos = []
    for jvm in range(1, quantidade + 1):
        print(f"[BUSCA] JVM {jvm}/{quantidade}", flush=True)
        destino_resumo = pasta_jvms / f"jvm{jvm}_resumo.csv"
        destino_bruto = pasta_jvms / f"jvm{jvm}_bruto.csv"
        executar_jvm(classes, destino_resumo, destino_bruto)
        resumos.append(ler_csv(destino_resumo))
        brutos.extend({"jvm": jvm, **linha} for linha in ler_csv(destino_bruto))

    escrever_csv(resultados / "busca_resumo.csv", CABECALHO_RESUMO, consolidar_resumos(resumos))
    escrever_csv(resultados / "busca_bruto.csv", list(brutos[0].keys()), brutos)
    print("[BUSCA] Consolidação concluída em resultados/busca_resumo.csv", flush=True)
    return 0


if __name__ == "__main__":
    sys.exit(main())
