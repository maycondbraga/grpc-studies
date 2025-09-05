package com.md.grpc.studies.usecase

import com.md.grpc.studies.dtos.Financiamento
import com.md.grpc.studies.dtos.Parcela
import com.md.grpc.studies.enums.SistemaAmortizacao
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate

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
        val principal = BigDecimal.valueOf(valor)
        val taxaDecimal = BigDecimal.valueOf(taxa).divide(BigDecimal.valueOf(100), SCALE_INT, RoundingMode.HALF_EVEN)
        val parcelas = mutableListOf<Parcela>()

        var saldo = principal
        val amortizacaoExata = principal.divide(BigDecimal.valueOf(prazo.toLong()), SCALE_INT, RoundingMode.HALF_EVEN)

        for (i in 1..prazo) {
            val jurosExato = saldo.multiply(taxaDecimal)
            val amortizacaoAtualExata = if (i < prazo) amortizacaoExata else saldo
            val parcelaExata = amortizacaoAtualExata.add(jurosExato)

            saldo = saldo.subtract(amortizacaoAtualExata)

            parcelas.add(
                Parcela(
                    parcela = i,
                    dataVencimento = dataPrimeiroVencimento.plusMonths((i - 1).toLong()),
                    valorParcela = parcelaExata.toDouble(),
                    juros = jurosExato.toDouble(),
                    amortizacao = amortizacaoAtualExata.toDouble(),
                    saldoDevedor = if (i == prazo) 0.0 else saldo.toDouble()
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
        val principal = BigDecimal.valueOf(valor)
        val taxaDecimal = BigDecimal.valueOf(taxa).divide(BigDecimal.valueOf(100), SCALE_INT, RoundingMode.HALF_EVEN)

        val onePlus = BigDecimal.ONE.add(taxaDecimal)
        val factor = onePlus.pow(prazo)
        val numerator = principal.multiply(taxaDecimal.multiply(factor))
        val denominator = factor.subtract(BigDecimal.ONE)
        val pmtExato = numerator.divide(denominator, SCALE_INT, RoundingMode.HALF_EVEN)

        val parcelas = mutableListOf<Parcela>()
        var saldo = principal

        for (i in 1..prazo) {
            val jurosExato = saldo.multiply(taxaDecimal)
            val amortizacaoExata = if (i < prazo) {
                pmtExato.subtract(jurosExato)
            } else {
                saldo
            }
            val parcelaExata = jurosExato.add(amortizacaoExata)

            saldo = saldo.subtract(amortizacaoExata)

            parcelas.add(
                Parcela(
                    parcela = i,
                    dataVencimento = dataPrimeiroVencimento.plusMonths((i - 1).toLong()),
                    valorParcela = parcelaExata.toDouble(),
                    juros = jurosExato.toDouble(),
                    amortizacao = amortizacaoExata.toDouble(),
                    saldoDevedor = if (i == prazo) 0.0 else saldo.toDouble()
                )
            )
        }

        return Financiamento(SistemaAmortizacao.PRICE, valor, taxa, prazo, dataPrimeiroVencimento, parcelas)
    }

    companion object {
        private const val SCALE_INT = 15
    }
}
