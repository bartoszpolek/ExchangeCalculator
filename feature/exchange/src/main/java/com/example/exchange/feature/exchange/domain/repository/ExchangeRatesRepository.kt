package com.example.exchange.feature.exchange.domain.repository

import com.example.exchange.feature.exchange.domain.model.CurrencyCode
import com.example.exchange.feature.exchange.domain.model.RateFetchResult

interface ExchangeRatesRepository {
    suspend fun fetch(currency: CurrencyCode): RateFetchResult
}
