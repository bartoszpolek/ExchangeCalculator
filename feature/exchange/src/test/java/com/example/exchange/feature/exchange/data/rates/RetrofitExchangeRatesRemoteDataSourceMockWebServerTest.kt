package com.example.exchange.feature.exchange.data.rates

import assertk.assertThat
import assertk.assertions.isEqualByComparingTo
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.example.exchange.core.common.result.Result
import com.example.exchange.core.network.NetworkError
import com.example.exchange.feature.exchange.data.api.ExchangeRatesApi
import com.example.exchange.feature.exchange.domain.model.CurrencyCode
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit

class RetrofitExchangeRatesRemoteDataSourceMockWebServerTest {

    private lateinit var server: MockWebServer
    private lateinit var dataSource: RetrofitExchangeRatesRemoteDataSource
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    @Before fun setUp() {
        server = MockWebServer()
        dataSource = RetrofitExchangeRatesRemoteDataSource(createApi())
    }

    @After fun tearDown() {
        server.shutdown()
    }

    @Test fun `200 maps response body to exchange rates`() = runTest {
        server.enqueue(
            jsonResponse(
                """
                [{
                  "ask": "18.4105000000",
                  "bid": "18.4069700000",
                  "book": "usdc_mxn",
                  "date": "2025-10-20T20:14:57.361483956"
                }]
                """.trimIndent(),
            ),
        )

        val result = dataSource.fetch(listOf(CurrencyCode("MXN")))
        val rate = (result as Result.Success).value.first()

        assertThat(rate?.currencyCode).isEqualTo(CurrencyCode("MXN"))
        assertThat(rate?.ask).isNotNull().isEqualByComparingTo("18.4105000000")
        assertThat(rate?.bid).isNotNull().isEqualByComparingTo("18.4069700000")
        assertThat(server.takeRequest().path).isEqualTo("/tickers?currencies=MXN")
    }

    @Test fun `500 maps to server error`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        val result = dataSource.fetch(listOf(CurrencyCode("MXN")))

        assertThat(result).isEqualTo(Result.Failure(NetworkError.Server(500)))
    }

    @Test fun `disconnect maps to connection failure`() = runTest {
        server.enqueue(
            MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START),
        )

        val result = dataSource.fetch(listOf(CurrencyCode("MXN")))

        assertThat(result).isEqualTo(Result.Failure(NetworkError.ConnectionFailure))
    }

    private fun createApi(): ExchangeRatesApi {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(ExchangeRatesApi::class.java)
    }

    private fun jsonResponse(body: String): MockResponse =
        MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(body)
}
