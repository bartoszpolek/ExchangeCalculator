package com.example.exchange.feature.exchange.data.currency

import com.example.exchange.feature.exchange.domain.model.CurrencyCode
import javax.inject.Inject

class LocalCurrencyListDataSource @Inject constructor() {

    fun codes(): List<CurrencyCode> = CODES

    private companion object {
        private val CODES: List<CurrencyCode> = listOf(
            CurrencyCode("MXN"),
            CurrencyCode("ARS"),
            CurrencyCode("BRL"),
            CurrencyCode("COP"),
        )
    }
}
