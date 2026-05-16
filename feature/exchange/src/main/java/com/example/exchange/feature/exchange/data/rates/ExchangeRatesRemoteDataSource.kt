package com.example.exchange.feature.exchange.data.rates

import com.example.exchange.core.common.result.Result
import com.example.exchange.core.network.NetworkError
import com.example.exchange.feature.exchange.domain.model.CurrencyCode
import com.example.exchange.feature.exchange.domain.model.ExchangeRate

interface ExchangeRatesRemoteDataSource {

    suspend fun fetch(currencies: List<CurrencyCode>): Result<List<ExchangeRate?>, NetworkError>
}
