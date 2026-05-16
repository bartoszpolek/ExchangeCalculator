package com.example.exchange.feature.exchange.data.rates

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.example.exchange.core.common.result.Result
import com.example.exchange.core.network.NetworkError
import com.example.exchange.feature.exchange.data.api.ExchangeRatesApi
import com.example.exchange.feature.exchange.data.dto.ExchangeRateDto
import com.example.exchange.feature.exchange.domain.model.CurrencyCode
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException

class RetrofitExchangeRatesRemoteDataSourceTest {

    private val api = FakeExchangeRatesApi()
    private val dataSource = RetrofitExchangeRatesRemoteDataSource(api)

    @Test fun `passes comma separated currencies to API`() = runTest {
        api.nextResponse = listOf(
            exchangeRateDto(book = "usdc_mxn"),
            exchangeRateDto(book = "usdc_ars"),
        )

        dataSource.fetch(listOf(CurrencyCode("MXN"), CurrencyCode("ARS")))

        assertThat(api.lastCurrencies).isEqualTo("MXN,ARS")
    }

    @Test fun `maps response DTOs to exchange rates`() = runTest {
        api.nextResponse = listOf(
            exchangeRateDto(book = "usdc_mxn"),
            exchangeRateDto(book = "usdc_ars"),
        )

        val result = dataSource.fetch(listOf(CurrencyCode("MXN"), CurrencyCode("ARS")))
        val rates = (result as Result.Success).value

        assertThat(rates.map { rate -> rate?.currencyCode }).containsExactly(
            CurrencyCode("MXN"),
            CurrencyCode("ARS"),
        )
    }

    @Test fun `keeps null response entries as null rates`() = runTest {
        api.nextResponse = listOf(
            exchangeRateDto(book = "usdc_mxn"),
            null,
        )

        val result = dataSource.fetch(listOf(CurrencyCode("MXN"), CurrencyCode("ARS")))
        val rates = (result as Result.Success).value

        assertThat(rates.map { rate -> rate?.currencyCode }).containsExactly(
            CurrencyCode("MXN"),
            null,
        )
    }

    @Test fun `maps IO exception to connection failure`() = runTest {
        api.nextError = IOException("offline")

        val result = dataSource.fetch(listOf(CurrencyCode("MXN")))

        assertThat(result).isEqualTo(Result.Failure(NetworkError.ConnectionFailure))
    }

    @Test fun `maps unknown exception to unknown network error`() = runTest {
        val cause = IllegalStateException("boom")
        api.nextError = cause

        val result = dataSource.fetch(listOf(CurrencyCode("MXN")))
        val error = (result as Result.Failure).error

        assertThat(error).isInstanceOf(NetworkError.Unknown::class)
    }

    private class FakeExchangeRatesApi : ExchangeRatesApi {
        var nextResponse: List<ExchangeRateDto?> = emptyList()
        var nextError: Exception? = null
        var lastCurrencies: String? = null

        override suspend fun getTickers(currencies: String): List<ExchangeRateDto?> {
            lastCurrencies = currencies
            nextError?.let { error -> throw error }
            return nextResponse
        }
    }

    private fun exchangeRateDto(book: String): ExchangeRateDto =
        ExchangeRateDto(
            ask = "18.4105000000",
            bid = "18.4069700000",
            book = book,
            date = "2025-10-20T20:14:57.361483956",
        )
}
