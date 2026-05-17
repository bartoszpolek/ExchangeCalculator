package com.example.exchange.feature.exchange.domain.repository

import com.example.exchange.feature.exchange.domain.model.CurrencyCode

interface CurrencyListRepository {
    suspend fun getCurrencies(): List<CurrencyCode>
}
