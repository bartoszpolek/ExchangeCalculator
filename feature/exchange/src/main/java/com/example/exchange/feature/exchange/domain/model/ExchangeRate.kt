package com.example.exchange.feature.exchange.domain.model

import java.math.BigDecimal

data class ExchangeRate(
    val currencyCode: CurrencyCode,
    val ask: BigDecimal,
    val bid: BigDecimal,
)
