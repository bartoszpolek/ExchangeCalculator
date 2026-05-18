package com.example.exchange.feature.exchange.data.rates

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import com.example.exchange.core.common.result.Result
import com.example.exchange.core.network.NetworkError
import com.example.exchange.feature.exchange.domain.model.CurrencyCode
import com.example.exchange.feature.exchange.domain.model.ExchangeRate
import com.example.exchange.feature.exchange.domain.model.RateFetchResult
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.math.BigDecimal

class RemoteExchangeRatesRepositoryTest {

    private val remoteDataSource = FakeExchangeRatesRemoteDataSource()
    private val repository = RemoteExchangeRatesRepository(remoteDataSource)

    @Test
    fun `returns available rate when remote succeeds`() = runTest {
        val mxn = CurrencyCode("MXN")
        val mxnRate = exchangeRate(mxn)
        remoteDataSource.nextResult = Result.Success(listOf(mxnRate))

        val result = repository.getRate(mxn)

        assertThat(result).isEqualTo(RateFetchResult.Available(mxnRate))
        assertThat(remoteDataSource.requests).containsExactly(listOf(mxn))
    }

    @Test
    fun `returns unavailable when remote returns null entry`() = runTest {
        val mxn = CurrencyCode("MXN")
        remoteDataSource.nextResult = Result.Success(listOf(null))

        val result = repository.getRate(mxn)

        assertThat(result).isEqualTo(RateFetchResult.Unavailable)
    }

    @Test
    fun `returns unavailable when remote response has no matching rate`() = runTest {
        val mxn = CurrencyCode("MXN")
        remoteDataSource.nextResult = Result.Success(listOf(exchangeRate(CurrencyCode("ARS"))))

        val result = repository.getRate(mxn)

        assertThat(result).isEqualTo(RateFetchResult.Unavailable)
    }

    @Test
    fun `non connection failure maps to unavailable`() = runTest {
        val mxn = CurrencyCode("MXN")
        remoteDataSource.nextResult = Result.Failure(NetworkError.Server(500))

        val result = repository.getRate(mxn)

        assertThat(result).isEqualTo(RateFetchResult.Unavailable)
    }

    @Test
    fun `connection failure maps to network failure`() = runTest {
        val mxn = CurrencyCode("MXN")
        remoteDataSource.nextResult = Result.Failure(NetworkError.ConnectionFailure)

        val result = repository.getRate(mxn)

        assertThat(result).isEqualTo(RateFetchResult.NetworkFailure)
    }

    private fun exchangeRate(
        currencyCode: CurrencyCode,
        ask: String = "18.4105000000",
        bid: String = "18.4069700000",
    ): ExchangeRate =
        ExchangeRate(
            currencyCode = currencyCode,
            ask = BigDecimal(ask),
            bid = BigDecimal(bid),
        )
}
