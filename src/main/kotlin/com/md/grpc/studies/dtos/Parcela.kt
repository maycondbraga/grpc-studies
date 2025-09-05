package com.md.grpc.studies.dtos

import java.time.LocalDate

data class Parcela(
    val parcela: Int,
    val dataVencimento: LocalDate,
    val valorParcela: Double,
    val juros: Double,
    val amortizacao: Double,
    val saldoDevedor: Double
)