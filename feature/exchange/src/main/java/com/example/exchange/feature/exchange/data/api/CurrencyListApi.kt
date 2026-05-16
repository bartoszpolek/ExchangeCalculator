package com.example.exchange.feature.exchange.data.api

import retrofit2.http.GET

interface CurrencyListApi {
    @GET("tickers-currencies")
    suspend fun getCurrencies(): List<String>
}
