package com.md.grpc.studies.usecase

import com.md.grpc.studies.dtos.Financiamento
import com.md.grpc.studies.dtos.Parcela
import com.md.grpc.studies.enums.SistemaAmortizacao
import org.springframework.stereotype.Service
import java.time.LocalDate
import kotlin.math.pow

@Service
class CalcularFinanciamento {
    fun execute(
        tipo: SistemaAmortizacao,
        valor: Double,
        taxa: Double,
        prazo: Int,
        dataPrimeiroVencimento: LocalDate
    ): Financiamento {
        return when (tipo) {
            SistemaAmortizacao.SAC -> calcularTabelaSac(valor, taxa, prazo, dataPrimeiroVencimento)
            SistemaAmortizacao.PRICE -> calcularTabelaPrice(valor, taxa, prazo, dataPrimeiroVencimento)
        }
    }

    private fun calcularTabelaSac(
        valor: Double,
        taxa: Double,
        prazo: Int,
        dataPrimeiroVencimento: LocalDate
    ): Financiamento {
        var saldoDevedor = valor
        val amortizacao = valor / prazo
        val parcelas = mutableListOf<Parcela>()

        for (i in 1..prazo) {
            val juros = saldoDevedor * (taxa / 100)
            val parcela = amortizacao + juros
            saldoDevedor -= amortizacao

            parcelas.add(
                Parcela(
                    parcela = i,
                    dataVencimento = dataPrimeiroVencimento.plusMonths((i - 1).toLong()),
                    valorParcela = parcela,
                    amortizacao = amortizacao,
                    juros = juros,
                    saldoDevedor = saldoDevedor
                )
            )
        }

        return Financiamento(SistemaAmortizacao.SAC, valor, taxa, prazo, dataPrimeiroVencimento, parcelas)
    }

    private fun calcularTabelaPrice(
        valor: Double,
        taxa: Double,
        prazo: Int,
        dataPrimeiroVencimento: LocalDate
    ): Financiamento {
        var saldo = valor
        val taxaDecimal = taxa / 100
        val pmt = valor * (taxaDecimal * (1 + taxaDecimal).pow(prazo)) / ((1 + taxaDecimal).pow(prazo) - 1)
        val parcelas = mutableListOf<Parcela>()

        for (i in 1..prazo) {
            val juros = saldo * taxaDecimal
            val amortizacao = pmt - juros
            saldo -= amortizacao

            parcelas.add(
                Parcela(
                    parcela = i,
                    dataVencimento = dataPrimeiroVencimento.plusMonths((i - 1).toLong()),
                    valorParcela = pmt,
                    juros = juros,
                    amortizacao = amortizacao,
                    saldoDevedor = saldo
                )
            )
        }
        return Financiamento(SistemaAmortizacao.PRICE, valor, taxa, prazo, dataPrimeiroVencimento, parcelas)
    }
}