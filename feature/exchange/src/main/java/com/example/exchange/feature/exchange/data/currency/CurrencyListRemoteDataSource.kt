package com.example.exchange.feature.exchange.data.currency

import com.example.exchange.core.common.result.Result
import com.example.exchange.feature.exchange.domain.model.CurrencyCode
import com.example.exchange.feature.exchange.domain.model.CurrencyListError

interface CurrencyListRemoteDataSource {
    suspend fun fetch(): Result<List<CurrencyCode>, CurrencyListError>
}
