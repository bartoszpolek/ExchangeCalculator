package com.example.exchange.core.common.result

sealed interface Result<out S, out E> {
    data class Success<out S>(val value: S) : Result<S, Nothing>
    data class Failure<out E>(val error: E) : Result<Nothing, E>
}
