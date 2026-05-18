package com.example.exchange.feature.exchange.data.currency

import com.example.exchange.core.common.result.Result
import com.example.exchange.feature.exchange.domain.model.CurrencyCode
import com.example.exchange.feature.exchange.domain.repository.CurrencyListRepository
import javax.inject.Inject

class NetworkFallbackCurrencyListRepository @Inject constructor(
    private val remoteDataSource: CurrencyListRemoteDataSource,
    private val staticDataSource: StaticCurrencyListDataSource,
) : CurrencyListRepository {

    override suspend fun getCurrencies(): List<CurrencyCode> =
        when (val result = remoteDataSource.fetch()) {
            is Result.Success -> result.value.ifEmpty { staticDataSource.codes() }
            is Result.Failure -> staticDataSource.codes()
        }
}
