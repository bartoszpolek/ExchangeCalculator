package com.example.exchange.feature.exchange.data.rates

import com.example.exchange.feature.exchange.domain.model.CurrencyCode
import com.example.exchange.feature.exchange.domain.model.ExchangeRate
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.util.Locale

@Serializable
data class ExchangeRateDto(
    val ask: String,
    val bid: String,
    val book: String,
    val date: String,
)

fun ExchangeRateDto.toExchangeRate(): ExchangeRate? {
    val currencyCode = book.toCurrencyCodeOrNull() ?: return null
    val askDecimal = ask.toBigDecimalOrNull() ?: return null
    val bidDecimal = bid.toBigDecimalOrNull() ?: return null
    if (askDecimal <= BigDecimal.ZERO || bidDecimal <= BigDecimal.ZERO) return null

    return ExchangeRate(
        currencyCode = currencyCode,
        ask = askDecimal,
        bid = bidDecimal,
    )
}

private fun String.toCurrencyCodeOrNull(): CurrencyCode? {
    val normalized = lowercase(Locale.US)
    if (!normalized.startsWith(BOOK_PREFIX)) return null

    val code = normalized.removePrefix(BOOK_PREFIX).uppercase(Locale.US)
    return if (code.isBlank()) null else CurrencyCode(code)
}

private const val BOOK_PREFIX = "usdc_"
