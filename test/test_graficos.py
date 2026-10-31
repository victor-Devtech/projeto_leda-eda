#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Testes unitários do pós-processamento em scripts/graficos.py, com CSVs mínimos em pasta temporária.
"""

import math
import os
import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent / "scripts"))

import graficos  # noqa: E402

CABECALHO_ORDENACAO = "grupo,algoritmo,cenario,n,media_ms,desvio_ms,mediana_ms,min_ms,max_ms,status"
CABECALHO_BUSCA = "algoritmo,cenario,n,media_ns,desvio_ns,mediana_ns,min_ns,max_ns,status"


def linhas_ordenacao(grupo, algoritmo, tamanhos):
    linhas = []
    for cenario in graficos.CENARIOS_ORDEM:
        for n in tamanhos:
            linhas.append(f"{grupo},{algoritmo},{cenario},{n},1.5000,0.2500,1.4000,1.0000,2.0000,OK")
    return linhas


def linhas_busca(algoritmo, tamanhos):
    return [f"{algoritmo},ORDENADO,{n},10.5000,0.5000,10.0000,9.0000,12.0000,OK" for n in tamanhos]


class TesteFormatacao(unittest.TestCase):
    def test_formata_media_e_desvio_com_quatro_casas(self):
        self.assertEqual(graficos.formatar_celula(1.5, 0.25, "OK"), "1.5000 ± 0.2500")

    def test_combinacao_inviavel_mostra_o_status(self):
        self.assertEqual(graficos.formatar_celula(math.nan, math.nan, "INVIAVEL"), "INVIAVEL")

    def test_status_ok_sem_valor_nao_vira_numero(self):
        self.assertEqual(graficos.formatar_celula(math.nan, math.nan, "OK"), "OK")


class TesteArquivos(unittest.TestCase):
    def setUp(self):
        self.pasta_original = os.getcwd()
        self.temporaria = tempfile.TemporaryDirectory()
        os.chdir(self.temporaria.name)
        Path("resultados").mkdir()

    def tearDown(self):
        os.chdir(self.pasta_original)
        self.temporaria.cleanup()

    def escrever(self, ordenacao, busca):
        Path("resultados/ordenacao_resumo.csv").write_text(
            "\n".join([CABECALHO_ORDENACAO] + ordenacao) + "\n", encoding="utf-8")
        Path("resultados/busca_resumo.csv").write_text(
            "\n".join([CABECALHO_BUSCA] + busca) + "\n", encoding="utf-8")

    def cenario_padrao(self):
        ordenacao = (linhas_ordenacao("A", "BubbleSort.slide", [10000, 25000, 50000])
                     + linhas_ordenacao("B", "MergeSort.classico", [50000, 150000, 500000]))
        busca = linhas_busca("linearIterativa", [50000, 150000, 500000])
        self.escrever(ordenacao, busca)

    def test_sem_csv_retorna_erro(self):
        self.assertEqual(graficos.main(), 1)

    def test_gera_todos_os_graficos_e_a_tabela(self):
        self.cenario_padrao()
        self.assertEqual(graficos.main(), 0)

        esperados = {"busca_tempo_por_n.png", "ordenacao_barras_n50000.png"}
        for grupo in ("A", "B"):
            for cenario in graficos.CENARIOS_ORDEM:
                esperados.add(f"ordenacao_grupo_{grupo}_{cenario.lower()}.png")
        self.assertEqual({p.name for p in Path("resultados/graficos").iterdir()}, esperados)
        for grafico in Path("resultados/graficos").iterdir():
            self.assertGreater(grafico.stat().st_size, 0, grafico.name)

    def test_tamanho_real_nao_gera_grafico_extra_do_modo_rapido(self):
        self.cenario_padrao()
        graficos.main()
        self.assertFalse(Path("resultados/graficos/ordenacao_barras_n5000.png").exists())

    def test_modo_rapido_usa_o_maior_n_do_grupo_a(self):
        self.escrever(linhas_ordenacao("A", "BubbleSort.slide", [1000, 5000]), linhas_busca("linearIterativa", [1000]))
        graficos.main()
        self.assertTrue(Path("resultados/graficos/ordenacao_barras_n5000.png").exists())

    def test_tabela_tem_cenarios_grupos_e_buscas(self):
        self.cenario_padrao()
        graficos.main()
        tabela = Path("resultados/tabelas.md").read_text(encoding="utf-8")
        for cenario in graficos.CENARIOS_ORDEM:
            self.assertIn(f"### Cenário: `{cenario}`", tabela)
        self.assertIn("#### Grupo A", tabela)
        self.assertIn("#### Grupo B", tabela)
        self.assertIn("n = 500.000", tabela)
        self.assertIn("| `linearIterativa` | 10.5000 ± 0.5000", tabela)

    def test_combinacao_inviavel_aparece_na_tabela_e_nao_derruba_os_graficos(self):
        ordenacao = linhas_ordenacao("A", "BubbleSort.slide", [10000, 50000])
        ordenacao.append("A,QuickSort.slide,ORDENADO,10000,,,,,,INVIAVEL")
        ordenacao.append("A,QuickSort.slide,ORDENADO,50000,,,,,,INVIAVEL")
        self.escrever(ordenacao, linhas_busca("linearIterativa", [50000]))
        self.assertEqual(graficos.main(), 0)
        tabela = Path("resultados/tabelas.md").read_text(encoding="utf-8")
        self.assertIn("| `QuickSort.slide` | INVIAVEL | INVIAVEL |", tabela)


if __name__ == "__main__":
    unittest.main()
