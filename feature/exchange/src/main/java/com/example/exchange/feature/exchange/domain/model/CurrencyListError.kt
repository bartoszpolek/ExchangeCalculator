package com.example.exchange.feature.exchange.domain.model

sealed interface CurrencyListError {
    data object NotImplemented : CurrencyListError
    data object Network : CurrencyListError
    data class Server(val code: Int) : CurrencyListError
    data class Unknown(val cause: Throwable) : CurrencyListError
}
