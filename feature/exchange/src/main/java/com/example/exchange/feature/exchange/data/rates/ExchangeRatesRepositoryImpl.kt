package com.example.exchange.feature.exchange.data.rates

import com.example.exchange.core.common.result.Result
import com.example.exchange.core.network.NetworkError
import com.example.exchange.feature.exchange.domain.model.CurrencyCode
import com.example.exchange.feature.exchange.domain.model.ExchangeRate
import com.example.exchange.feature.exchange.domain.model.RateFetchResult
import com.example.exchange.feature.exchange.domain.repository.ExchangeRatesRepository
import javax.inject.Inject

class ExchangeRatesRepositoryImpl @Inject constructor(
    private val remoteDataSource: ExchangeRatesRemoteDataSource,
) : ExchangeRatesRepository {

    override suspend fun getRate(
        currency: CurrencyCode,
    ): RateFetchResult {
        return when (val result = remoteDataSource.fetch(listOf(currency))) {
            is Result.Success -> result.value.toRateFetchResult(currency)
            is Result.Failure -> result.error.toRateFetchResult()
        }
    }

    private fun List<ExchangeRate?>.toRateFetchResult(
        currency: CurrencyCode,
    ): RateFetchResult {
        val rate = firstOrNull { rate -> rate?.currencyCode == currency }
        return if (rate != null) {
            RateFetchResult.Available(rate)
        } else {
            RateFetchResult.Unavailable
        }
    }

    private fun NetworkError.toRateFetchResult(): RateFetchResult =
        when (this) {
            NetworkError.ConnectionFailure -> RateFetchResult.NetworkFailure
            is NetworkError.Client,
            is NetworkError.Server,
            is NetworkError.Unknown -> RateFetchResult.Unavailable
        }
}
