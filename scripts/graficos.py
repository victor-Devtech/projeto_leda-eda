#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Script de pós-processamento para geração dos gráficos e tabelas em Markdown
a partir dos arquivos resultados/ordenacao_resumo.csv e resultados/busca_resumo.csv.
"""

from pathlib import Path
import sys
import pandas as pd
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np


PASTA_RESULTADOS = Path("resultados")
PASTA_GRAFICOS = PASTA_RESULTADOS / "graficos"
ARQUIVO_ORDENACAO = PASTA_RESULTADOS / "ordenacao_resumo.csv"
ARQUIVO_BUSCA = PASTA_RESULTADOS / "busca_resumo.csv"
ARQUIVO_TABELAS = PASTA_RESULTADOS / "tabelas.md"

CENARIOS_ORDEM = ["ALEATORIO", "ORDENADO", "INVERSO", "PARCIAL"]


def gerar_graficos_linha_ordenacao(df_ord: pd.DataFrame) -> None:
    """
    1. Gera um gráfico de linha por cenário e por grupo:
       tempo médio (ms) x n, uma linha por algoritmo, eixo y em escala log.
    """
    grupos = sorted(df_ord["grupo"].dropna().unique())
    marcadores = ["o", "s", "^", "D", "v", "P", "X", "*", "<", ">"]

    for grupo in grupos:
        df_g = df_ord[df_ord["grupo"] == grupo]
        for cenario in CENARIOS_ORDEM:
            df_gc = df_g[(df_g["cenario"] == cenario) & (df_g["status"] == "OK")].copy()
            if df_gc.empty:
                continue

            fig, ax = plt.subplots(figsize=(9, 5.5))
            algoritmos = df_gc["algoritmo"].unique()

            for idx, alg in enumerate(algoritmos):
                sub = df_gc[df_gc["algoritmo"] == alg].sort_values("n")
                marcador = marcadores[idx % len(marcadores)]
                ax.plot(
                    sub["n"],
                    sub["media_ms"],
                    marker=marcador,
                    linewidth=2,
                    markersize=6,
                    label=alg,
                )

            tamanhos = sorted(df_gc["n"].unique())
            ax.set_xticks(tamanhos)
            ax.set_xticklabels([f"{int(n):,}".replace(",", ".") for n in tamanhos])
            ax.set_yscale("log")
            ax.set_xlabel("Tamanho do vetor (n)", fontsize=11)
            ax.set_ylabel("Tempo médio (ms) [escala log]", fontsize=11)
            ax.set_title(f"Ordenação - Grupo {grupo} | Cenário: {cenario}", fontsize=13, fontweight="bold")
            ax.grid(True, which="both", linestyle="--", alpha=0.45)
            ax.legend(bbox_to_anchor=(1.02, 1), loc="upper left", fontsize=9)

            fig.tight_layout()
            caminho_saida = PASTA_GRAFICOS / f"ordenacao_grupo_{grupo}_{cenario.lower()}.png"
            fig.savefig(caminho_saida, dpi=150, bbox_inches="tight")
            plt.close(fig)
            print(f"[GRAFICO] Gerado: {caminho_saida}")


def gerar_grafico_barras_agrupadas(df_ord: pd.DataFrame) -> None:
    """
    2. Gera um gráfico de barras agrupadas em n = 50000 (ou maior n do Grupo A no modo rápido)
       comparando os 4 cenários para todos os algoritmos (eixo y em escala log).
    """
    df_a = df_ord[(df_ord["grupo"] == "A") & (df_ord["status"] == "OK")].copy()
    if df_a.empty:
        return

    tamanhos_disponiveis = sorted(df_a["n"].unique())
    n_alvo = 50000 if 50000 in tamanhos_disponiveis else int(max(tamanhos_disponiveis))
    df_n = df_a[df_a["n"] == n_alvo]

    algoritmos = list(df_a["algoritmo"].unique())
    cenarios = [c for c in CENARIOS_ORDEM if c in df_n["cenario"].unique()]

    x = np.arange(len(algoritmos))
    largura = 0.8 / max(1, len(cenarios))

    fig, ax = plt.subplots(figsize=(12, 6))

    for idx, cenario in enumerate(cenarios):
        valores = []
        for alg in algoritmos:
            linha = df_n[(df_n["algoritmo"] == alg) & (df_n["cenario"] == cenario)]
            if not linha.empty and pd.notna(linha["media_ms"].iloc[0]):
                valores.append(float(linha["media_ms"].iloc[0]))
            else:
                valores.append(np.nan)

        deslocamento = (idx - (len(cenarios) - 1) / 2.0) * largura
        ax.bar(x + deslocamento, valores, width=largura, label=cenario)

    ax.set_yscale("log")
    ax.set_xticks(x)
    ax.set_xticklabels(algoritmos, rotation=30, ha="right", fontsize=9.5)
    ax.set_xlabel("Algoritmo de Ordenação", fontsize=11)
    ax.set_ylabel("Tempo médio (ms) [escala log]", fontsize=11)
    n_formatado = f"{n_alvo:,}".replace(",", ".")
    ax.set_title(f"Comparação dos 4 Cenários por Algoritmo (n = {n_formatado})", fontsize=13, fontweight="bold")
    ax.grid(True, axis="y", which="both", linestyle="--", alpha=0.45)
    ax.legend(title="Cenário", fontsize=9.5)

    fig.tight_layout()
    caminho_padrao = PASTA_GRAFICOS / "ordenacao_barras_n50000.png"
    fig.savefig(caminho_padrao, dpi=150, bbox_inches="tight")
    print(f"[GRAFICO] Gerado: {caminho_padrao}")

    if n_alvo != 50000:
        caminho_especifico = PASTA_GRAFICOS / f"ordenacao_barras_n{n_alvo}.png"
        fig.savefig(caminho_especifico, dpi=150, bbox_inches="tight")
        print(f"[GRAFICO] Gerado: {caminho_especifico}")

    plt.close(fig)


def gerar_grafico_buscas(df_busca: pd.DataFrame) -> None:
    """
    3. Gera o gráfico das buscas: tempo por busca (ns) x n, eixo y em escala log.
    """
    df_ok = df_busca[df_busca["status"] == "OK"].copy()
    if df_ok.empty:
        return

    marcadores = ["o", "s", "^", "D", "v"]
    fig, ax = plt.subplots(figsize=(9, 5.5))

    algoritmos = df_ok["algoritmo"].unique()
    for idx, alg in enumerate(algoritmos):
        sub = df_ok[df_ok["algoritmo"] == alg].sort_values("n")
        marcador = marcadores[idx % len(marcadores)]
        ax.plot(
            sub["n"],
            sub["media_ns"],
            marker=marcador,
            linewidth=2,
            markersize=6,
            label=alg,
        )

    tamanhos = sorted(df_ok["n"].unique())
    ax.set_xticks(tamanhos)
    ax.set_xticklabels([f"{int(n):,}".replace(",", ".") for n in tamanhos])
    ax.set_yscale("log")
    ax.set_xlabel("Tamanho do vetor ordenado (n)", fontsize=11)
    ax.set_ylabel("Tempo médio por busca (ns) [escala log]", fontsize=11)
    ax.set_title("Desempenho dos Algoritmos de Busca (Alvo = -1, Pior Caso)", fontsize=13, fontweight="bold")
    ax.grid(True, which="both", linestyle="--", alpha=0.45)
    ax.legend(bbox_to_anchor=(1.02, 1), loc="upper left", fontsize=9.5)

    fig.tight_layout()
    caminho_saida = PASTA_GRAFICOS / "busca_tempo_por_n.png"
    fig.savefig(caminho_saida, dpi=150, bbox_inches="tight")
    plt.close(fig)
    print(f"[GRAFICO] Gerado: {caminho_saida}")


def formatar_celula(media: float, desvio: float, status: str) -> str:
    if status != "OK" or pd.isna(media) or pd.isna(desvio):
        return status if isinstance(status, str) and status else "N/A"
    return f"{media:.4f} ± {desvio:.4f}"


def gerar_tabelas_markdown(df_ord: pd.DataFrame, df_busca: pd.DataFrame) -> None:
    """
    Gera resultados/tabelas.md com uma tabela por cenário (linhas = algoritmos,
    colunas = tamanhos, células = "média ± desvio" em ms) e uma tabela de busca.
    """
    linhas_md = [
        "# Tabelas de Resultados Experimentais (EDA/LEDA - Unidade 1)",
        "",
        "Valores expressos como **média ± desvio padrão** das execuções válidas (após descarte do *warm-up*).",
        "",
        "## 1. Algoritmos de Ordenação (tempos em ms)",
        "",
    ]

    algoritmos_ordem = list(df_ord["algoritmo"].unique())

    for cenario in CENARIOS_ORDEM:
        df_c = df_ord[df_ord["cenario"] == cenario]
        if df_c.empty:
            continue

        linhas_md.append(f"### Cenário: `{cenario}`")
        linhas_md.append("")

        for grupo in sorted(df_c["grupo"].unique()):
            df_cg = df_c[df_c["grupo"] == grupo]
            tamanhos = sorted(df_cg["n"].unique())
            algoritmos_grupo = [a for a in algoritmos_ordem if a in df_cg["algoritmo"].values]

            linhas_md.append(f"#### Grupo {grupo}")
            linhas_md.append("")

            cabecalho_cols = [f"n = {int(n):,}".replace(",", ".") for n in tamanhos]
            linhas_md.append("| Algoritmo | " + " | ".join(cabecalho_cols) + " |")
            linhas_md.append("| :--- | " + " | ".join([":---:" for _ in tamanhos]) + " |")

            for alg in algoritmos_grupo:
                celulas = []
                for n in tamanhos:
                    reg = df_cg[(df_cg["algoritmo"] == alg) & (df_cg["n"] == n)]
                    if reg.empty:
                        celulas.append("N/A")
                    else:
                        row = reg.iloc[0]
                        celulas.append(formatar_celula(row["media_ms"], row["desvio_ms"], str(row["status"])))
                linhas_md.append(f"| `{alg}` | " + " | ".join(celulas) + " |")

            linhas_md.append("")

    linhas_md.append("## 2. Algoritmos de Busca (tempos por busca em ns, alvo = -1)")
    linhas_md.append("")

    if not df_busca.empty:
        tamanhos_b = sorted(df_busca["n"].unique())
        algoritmos_b = list(df_busca["algoritmo"].unique())
        cabecalho_b = [f"n = {int(n):,}".replace(",", ".") for n in tamanhos_b]

        linhas_md.append("| Algoritmo de Busca | " + " | ".join(cabecalho_b) + " |")
        linhas_md.append("| :--- | " + " | ".join([":---:" for _ in tamanhos_b]) + " |")

        for alg in algoritmos_b:
            celulas = []
            for n in tamanhos_b:
                reg = df_busca[(df_busca["algoritmo"] == alg) & (df_busca["n"] == n)]
                if reg.empty:
                    celulas.append("N/A")
                else:
                    row = reg.iloc[0]
                    celulas.append(formatar_celula(row["media_ns"], row["desvio_ns"], str(row["status"])))
            linhas_md.append(f"| `{alg}` | " + " | ".join(celulas) + " |")
        linhas_md.append("")

    ARQUIVO_TABELAS.write_text("\n".join(linhas_md), encoding="utf-8")
    print(f"[TABELA]  Gerado: {ARQUIVO_TABELAS}")


def main() -> int:
    if not ARQUIVO_ORDENACAO.exists():
        print(f"Erro: arquivo {ARQUIVO_ORDENACAO} não encontrado.", file=sys.stderr)
        return 1
    if not ARQUIVO_BUSCA.exists():
        print(f"Erro: arquivo {ARQUIVO_BUSCA} não encontrado.", file=sys.stderr)
        return 1

    PASTA_GRAFICOS.mkdir(parents=True, exist_ok=True)

    df_ord = pd.read_csv(ARQUIVO_ORDENACAO)
    df_busca = pd.read_csv(ARQUIVO_BUSCA)

    gerar_graficos_linha_ordenacao(df_ord)
    gerar_grafico_barras_agrupadas(df_ord)
    gerar_grafico_buscas(df_busca)
    gerar_tabelas_markdown(df_ord, df_busca)

    print("Pós-processamento de gráficos e tabelas concluído com sucesso.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
