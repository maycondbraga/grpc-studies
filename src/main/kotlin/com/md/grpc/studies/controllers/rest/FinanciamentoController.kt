package com.md.grpc.studies.controllers.rest

import com.md.grpc.studies.dtos.Financiamento
import com.md.grpc.studies.dtos.FinanciamentoRequest
import com.md.grpc.studies.usecase.CalcularFinanciamento
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/financiamentos")
class FinanciamentoController(private val calcularFinanciamento: CalcularFinanciamento) {

    @PostMapping
    fun calcular(@RequestBody request: FinanciamentoRequest): Financiamento {
        return calcularFinanciamento.execute(
            request.tipo,
            request.valor,
            request.taxa,
            request.prazo,
            request.dataPrimeiroVencimento
        )
    }
}