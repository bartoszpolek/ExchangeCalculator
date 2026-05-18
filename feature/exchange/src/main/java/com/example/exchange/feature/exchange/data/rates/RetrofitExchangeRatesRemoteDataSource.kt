package com.example.exchange.feature.exchange.data.rates

import com.example.exchange.core.common.result.Result
import com.example.exchange.core.network.NetworkError
import com.example.exchange.core.network.toNetworkError
import com.example.exchange.feature.exchange.data.api.ExchangeRatesApi
import com.example.exchange.feature.exchange.domain.model.CurrencyCode
import com.example.exchange.feature.exchange.domain.model.ExchangeRate
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class RetrofitExchangeRatesRemoteDataSource @Inject constructor(
    private val api: ExchangeRatesApi,
) : ExchangeRatesRemoteDataSource {

    override suspend fun fetch(
        currencies: List<CurrencyCode>,
    ): Result<List<ExchangeRate?>, NetworkError> {
        return try {
            val rates = api.getTickers(currencies.toQuery())
                .map { dto -> dto?.toExchangeRate() }
            Result.Success(rates)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.Failure(e.toNetworkError())
        }
    }

    private fun List<CurrencyCode>.toQuery(): String =
        joinToString(separator = ",") { code -> code.code }
}
