package com.example.exchange.feature.exchange.data.currency

import com.example.exchange.core.common.result.Result
import com.example.exchange.feature.exchange.domain.model.CurrencyCode
import com.example.exchange.feature.exchange.domain.repository.CurrencyListRepository
import javax.inject.Inject

class CurrencyListRepositoryImpl @Inject constructor(
    private val remoteDataSource: CurrencyListRemoteDataSource,
    private val localDataSource: LocalCurrencyListDataSource,
) : CurrencyListRepository {

    override suspend fun load(): List<CurrencyCode> =
        when (val result = remoteDataSource.fetch()) {
            is Result.Success -> result.value.ifEmpty { localDataSource.codes() }
            is Result.Failure -> localDataSource.codes()
        }
}
