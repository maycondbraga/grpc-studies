package com.md.grpc.studies.dtos

import com.md.grpc.studies.enums.SistemaAmortizacao
import java.time.LocalDate

data class FinanciamentoRequest(
    val tipo: SistemaAmortizacao,
    val valor: Double,
    val taxa: Double,
    val prazo: Int,
    val dataPrimeiroVencimento: LocalDate
)