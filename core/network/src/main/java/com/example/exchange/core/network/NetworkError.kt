package com.example.exchange.core.network

import retrofit2.HttpException
import java.io.IOException

sealed interface NetworkError {
    data class Client(val code: Int) : NetworkError
    data class Server(val code: Int) : NetworkError
    data object ConnectionFailure : NetworkError
    data class Unknown(val cause: Throwable) : NetworkError
}

fun Throwable.toNetworkError(): NetworkError = when (this) {
    is HttpException -> when (val code = code()) {
        in 400..499 -> NetworkError.Client(code)
        in 500..599 -> NetworkError.Server(code)
        else -> NetworkError.Unknown(this)
    }
    is IOException -> NetworkError.ConnectionFailure
    else -> NetworkError.Unknown(this)
}
