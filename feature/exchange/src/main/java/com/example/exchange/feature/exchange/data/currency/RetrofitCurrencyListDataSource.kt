package com.example.exchange.feature.exchange.data.currency

import com.example.exchange.core.common.result.Result
import com.example.exchange.core.network.NetworkError
import com.example.exchange.core.network.toNetworkError
import com.example.exchange.feature.exchange.data.api.CurrencyListApi
import com.example.exchange.feature.exchange.domain.model.CurrencyCode
import com.example.exchange.feature.exchange.domain.model.CurrencyListError
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class RetrofitCurrencyListDataSource @Inject constructor(
    private val api: CurrencyListApi,
) : CurrencyListRemoteDataSource {

    override suspend fun fetch(): Result<List<CurrencyCode>, CurrencyListError> = try {
        val codes = api.getCurrencies().map(::CurrencyCode)
        Result.Success(codes)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.Failure(e.toCurrencyListError())
    }
}

private fun Throwable.toCurrencyListError(): CurrencyListError =
    when (val networkError = toNetworkError()) {
        is NetworkError.Client ->
            if (networkError.code == HTTP_FORBIDDEN || networkError.code == HTTP_NOT_FOUND) {
                CurrencyListError.NotImplemented
            } else {
                CurrencyListError.Unknown(this)
            }
        is NetworkError.Server -> CurrencyListError.Server(networkError.code)
        NetworkError.ConnectionFailure -> CurrencyListError.Network
        is NetworkError.Unknown -> CurrencyListError.Unknown(networkError.cause)
    }

private const val HTTP_FORBIDDEN = 403
private const val HTTP_NOT_FOUND = 404
