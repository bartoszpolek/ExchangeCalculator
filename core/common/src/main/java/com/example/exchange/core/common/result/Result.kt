package com.example.exchange.core.common.result

sealed interface Result<out S, out E> {
    data class Success<out S>(val value: S) : Result<S, Nothing>
    data class Failure<out E>(val error: E) : Result<Nothing, E>
}

inline fun <S, E, R> Result<S, E>.map(transform: (S) -> R): Result<R, E> = when (this) {
    is Result.Success -> Result.Success(transform(value))
    is Result.Failure -> this
}

inline fun <S, E, F> Result<S, E>.mapError(transform: (E) -> F): Result<S, F> = when (this) {
    is Result.Success -> this
    is Result.Failure -> Result.Failure(transform(error))
}

inline fun <S, E, R> Result<S, E>.fold(
    onSuccess: (S) -> R,
    onFailure: (E) -> R,
): R = when (this) {
    is Result.Success -> onSuccess(value)
    is Result.Failure -> onFailure(error)
}

fun <S, E> Result<S, E>.getOrNull(): S? = (this as? Result.Success)?.value
fun <S, E> Result<S, E>.errorOrNull(): E? = (this as? Result.Failure)?.error
