package com.example.exchange.feature.exchange.data.rates

import com.example.exchange.core.common.result.Result
import com.example.exchange.core.network.NetworkError
import com.example.exchange.feature.exchange.domain.model.CurrencyCode
import com.example.exchange.feature.exchange.domain.model.ExchangeRate

class FakeExchangeRatesRemoteDataSource(
    var nextResult: Result<List<ExchangeRate?>, NetworkError> = Result.Success(emptyList()),
) : ExchangeRatesRemoteDataSource {

    val requests = mutableListOf<List<CurrencyCode>>()

    override suspend fun fetch(
        currencies: List<CurrencyCode>,
    ): Result<List<ExchangeRate?>, NetworkError> {
        requests += currencies
        return nextResult
    }
}
