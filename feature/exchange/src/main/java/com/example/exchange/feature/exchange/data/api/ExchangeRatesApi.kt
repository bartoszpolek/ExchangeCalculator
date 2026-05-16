package com.example.exchange.feature.exchange.data.api

import com.example.exchange.feature.exchange.data.dto.ExchangeRateDto
import retrofit2.http.GET
import retrofit2.http.Query

interface ExchangeRatesApi {
    @GET("tickers")
    suspend fun getTickers(
        @Query(value = "currencies", encoded = true) currencies: String,
    ): List<ExchangeRateDto?>
}
