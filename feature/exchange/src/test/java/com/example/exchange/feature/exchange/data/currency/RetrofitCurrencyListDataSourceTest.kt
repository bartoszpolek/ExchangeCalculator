package com.example.exchange.feature.exchange.data.currency

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.example.exchange.core.common.result.Result
import com.example.exchange.feature.exchange.data.api.CurrencyListApi
import com.example.exchange.feature.exchange.domain.model.CurrencyCode
import com.example.exchange.feature.exchange.domain.model.CurrencyListError
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

class RetrofitCurrencyListDataSourceTest {

    private lateinit var server: MockWebServer
    private lateinit var dataSource: RetrofitCurrencyListDataSource

    @Before
    fun setUp() {
        server = MockWebServer()
        dataSource = RetrofitCurrencyListDataSource(createApi())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `200 returns currency codes`() = runTest {
        server.enqueue(jsonResponse("""["MXN","ARS","BRL","COP"]"""))

        val result = dataSource.fetch()

        val codes = (result as Result.Success).value
        assertThat(codes).containsExactly(
            CurrencyCode("MXN"),
            CurrencyCode("ARS"),
            CurrencyCode("BRL"),
            CurrencyCode("COP"),
        )
        assertThat(server.takeRequest().path).isEqualTo("/tickers-currencies")
    }

    @Test
    fun `403 maps to not implemented`() = runTest {
        server.enqueue(MockResponse().setResponseCode(403))

        assertThat(dataSource.fetch())
            .isEqualTo(Result.Failure(CurrencyListError.NotImplemented))
    }

    @Test
    fun `404 maps to not implemented`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))

        assertThat(dataSource.fetch())
            .isEqualTo(Result.Failure(CurrencyListError.NotImplemented))
    }

    @Test
    fun `other 4xx maps to unknown error`() = runTest {
        server.enqueue(MockResponse().setResponseCode(400))

        val error = (dataSource.fetch() as Result.Failure).error

        assertThat(error).isInstanceOf(CurrencyListError.Unknown::class)
    }

    @Test
    fun `500 maps to server error`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        assertThat(dataSource.fetch())
            .isEqualTo(Result.Failure(CurrencyListError.Server(500)))
    }

    @Test
    fun `disconnect maps to network error`() = runTest {
        server.enqueue(
            MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START),
        )

        assertThat(dataSource.fetch())
            .isEqualTo(Result.Failure(CurrencyListError.Network))
    }

    @Test
    fun `malformed JSON maps to unknown error`() = runTest {
        server.enqueue(jsonResponse("[\"MXN\",\"ARS\""))

        val error = (dataSource.fetch() as Result.Failure).error

        assertThat(error).isInstanceOf(CurrencyListError.Unknown::class)
    }

    private fun createApi(): CurrencyListApi {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            explicitNulls = false
        }
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(CurrencyListApi::class.java)
    }

    private fun jsonResponse(body: String): MockResponse =
        MockResponse()
            .setResponseCode(200)
            .setHeader("Content-Type", "application/json")
            .setBody(body)
}
