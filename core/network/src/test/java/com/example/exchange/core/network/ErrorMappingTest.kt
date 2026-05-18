package com.example.exchange.core.network

import assertk.assertThat
import assertk.assertions.isEqualTo
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

class ErrorMappingTest {

    @Test
    fun `400 maps to Client`() {
        assertThat(httpException(400).toNetworkError())
            .isEqualTo(NetworkError.Client(400))
    }

    @Test
    fun `499 maps to Client`() {
        assertThat(httpException(499).toNetworkError())
            .isEqualTo(NetworkError.Client(499))
    }

    @Test
    fun `500 maps to Server`() {
        assertThat(httpException(500).toNetworkError())
            .isEqualTo(NetworkError.Server(500))
    }

    @Test
    fun `599 maps to Server`() {
        assertThat(httpException(599).toNetworkError())
            .isEqualTo(NetworkError.Server(599))
    }

    @Test
    fun `IOException maps to ConnectionFailure`() {
        assertThat(IOException("disconnected").toNetworkError())
            .isEqualTo(NetworkError.ConnectionFailure)
    }

    @Test
    fun `SocketTimeoutException maps to ConnectionFailure`() {
        assertThat(SocketTimeoutException().toNetworkError())
            .isEqualTo(NetworkError.ConnectionFailure)
    }

    @Test
    fun `RuntimeException maps to Unknown wrapping the cause`() {
        val cause = RuntimeException("?")
        assertThat(cause.toNetworkError()).isEqualTo(NetworkError.Unknown(cause))
    }

    private fun httpException(code: Int): HttpException {
        val body = "".toResponseBody("text/plain".toMediaType())
        val response = Response.error<Any>(code, body)
        return HttpException(response)
    }
}
