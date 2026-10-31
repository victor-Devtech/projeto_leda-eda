#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Testes unitários da execução individual: a lista de combinações precisa ser igual à da execução
compartilhada, senão a comparação entre as duas perde o sentido.
"""

import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent / "scripts"))

import benchmark_individual as individual  # noqa: E402


class TesteCombinacoes(unittest.TestCase):
    def test_quantidade_de_combinacoes_de_ordenacao(self):
        self.assertEqual(len(individual.combinacoes_ordenacao()), 3 * 4 * 10 + 3 * 4 * 4)

    def test_quantidade_de_combinacoes_de_busca(self):
        self.assertEqual(len(individual.combinacoes_busca()), 3 * 5)

    def test_combinacoes_nao_se_repetem(self):
        combinacoes = individual.combinacoes_ordenacao()
        self.assertEqual(len(combinacoes), len(set(combinacoes)))

    def test_ordem_acompanha_a_execucao_compartilhada(self):
        combinacoes = individual.combinacoes_ordenacao()
        self.assertEqual(combinacoes[0], ("A", "BubbleSort.slide", "ALEATORIO", 10000))
        self.assertEqual(combinacoes[-1], ("B", "Arrays.sort", "PARCIAL", 500000))

    def test_grupo_b_nao_tem_algoritmo_quadratico(self):
        for grupo, algoritmo, _, _ in individual.combinacoes_ordenacao():
            if grupo == "B":
                self.assertNotIn(algoritmo.split(".")[0], ("BubbleSort", "SelectionSort", "InsertionSort"))


class TesteComparacao(unittest.TestCase):
    def test_razao_e_individual_dividido_por_compartilhado(self):
        compartilhado = [{"algoritmo": "x", "n": "10", "media": "2.0"}]
        medido = [{"algoritmo": "x", "n": "10", "media": "3.0"}]
        linha = individual.comparar(compartilhado, medido, ["algoritmo", "n"], "media")[0]
        self.assertEqual(linha["media_compartilhada"], "2.0000")
        self.assertEqual(linha["media_individual"], "3.0000")
        self.assertEqual(linha["razao"], "1.5000")

    def test_pareia_pela_chave_e_nao_pela_posicao(self):
        compartilhado = [{"a": "x", "media": "1.0"}, {"a": "y", "media": "2.0"}]
        medido = [{"a": "y", "media": "4.0"}, {"a": "x", "media": "5.0"}]
        linhas = individual.comparar(compartilhado, medido, ["a"], "media")
        self.assertEqual([linha["razao"] for linha in linhas], ["5.0000", "2.0000"])

    def test_combinacao_faltando_e_erro(self):
        with self.assertRaises(ValueError):
            individual.comparar([{"a": "x", "media": "1.0"}], [], ["a"], "media")


if __name__ == "__main__":
    unittest.main()
