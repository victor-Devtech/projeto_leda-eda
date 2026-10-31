#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Testes unitários da consolidação das buscas executadas em várias JVMs.
"""

import statistics
import sys
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent / "scripts"))

import busca_jvms  # noqa: E402


def linha(media, mediana=None, minimo=None, maximo=None, status="OK", algoritmo="binariaIterativa", n="50000"):
    return {
        "algoritmo": algoritmo,
        "cenario": "ORDENADO",
        "n": n,
        "media_ns": str(media),
        "desvio_ns": "1.0",
        "mediana_ns": str(media if mediana is None else mediana),
        "min_ns": str(media if minimo is None else minimo),
        "max_ns": str(media if maximo is None else maximo),
        "status": status,
    }


class TesteConsolidacao(unittest.TestCase):
    def test_usa_a_mediana_e_nao_e_afetada_por_jvm_lenta(self):
        resumos = [[linha(17.0)], [linha(18.0)], [linha(135.0)]]
        final = busca_jvms.consolidar_resumos(resumos)[0]
        self.assertEqual(final["media_ns"], "18.0000")

    def test_desvio_e_amostral_entre_as_medias_das_jvms(self):
        final = busca_jvms.consolidar_resumos([[linha(10.0)], [linha(20.0)], [linha(30.0)]])[0]
        self.assertEqual(final["desvio_ns"], f"{statistics.stdev([10.0, 20.0, 30.0]):.4f}")

    def test_extremos_e_mediana_cobrem_todas_as_jvms(self):
        resumos = [
            [linha(20.0, mediana=19.0, minimo=15.0, maximo=25.0)],
            [linha(21.0, mediana=20.0, minimo=16.0, maximo=90.0)],
            [linha(22.0, mediana=21.0, minimo=14.0, maximo=30.0)],
        ]
        final = busca_jvms.consolidar_resumos(resumos)[0]
        self.assertEqual(final["mediana_ns"], "20.0000")
        self.assertEqual(final["min_ns"], "14.0000")
        self.assertEqual(final["max_ns"], "90.0000")

    def test_mantem_a_ordem_das_combinacoes(self):
        primeira = [linha(1.0, algoritmo="a"), linha(2.0, algoritmo="b")]
        segunda = [linha(3.0, algoritmo="a"), linha(4.0, algoritmo="b")]
        final = busca_jvms.consolidar_resumos([primeira, segunda])
        self.assertEqual([item["algoritmo"] for item in final], ["a", "b"])

    def test_rejeita_uma_jvm_so(self):
        with self.assertRaises(ValueError):
            busca_jvms.consolidar_resumos([[linha(1.0)]])

    def test_rejeita_combinacoes_diferentes(self):
        with self.assertRaises(ValueError):
            busca_jvms.consolidar_resumos([[linha(1.0, n="50000")], [linha(1.0, n="150000")]])

    def test_rejeita_status_diferente_de_ok(self):
        with self.assertRaises(ValueError):
            busca_jvms.consolidar_resumos([[linha(1.0)], [linha(1.0, status="INVIAVEL")]])


if __name__ == "__main__":
    unittest.main()
