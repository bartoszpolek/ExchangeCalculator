package com.example.exchange.feature.exchange.domain.model

sealed interface RateFetchResult {
    data class Available(val rate: ExchangeRate) : RateFetchResult
    data object Unavailable : RateFetchResult
    data object NetworkFailure : RateFetchResult
}
