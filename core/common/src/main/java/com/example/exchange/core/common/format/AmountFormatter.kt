package com.example.exchange.core.common.format

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale

const val ZERO_AMOUNT = "0"

class AmountFormatter(
    private val locale: Locale = Locale.getDefault(),
) {

    fun parse(input: String): BigDecimal? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null

        val symbols = DecimalFormatSymbols.getInstance(locale)
        val normalized = trimmed.replace(symbols.decimalSeparator, '.')

        val isValid = Regex("""\d+(\.\d*)?|\.\d+""").matches(normalized)
        if (!isValid) return null

        return normalized.toBigDecimalOrNull()
    }

    fun formatDisplayAmount(amount: BigDecimal): String =
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            ZERO_AMOUNT
        } else {
            amount.setScale(DISPLAY_SCALE, RoundingMode.HALF_EVEN)
                .stripTrailingZeros()
                .toPlainString()
        }

    fun formatRate(amount: BigDecimal): String {
        val format = NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = RATE_MAX_FRACTION_DIGITS
            isGroupingUsed = true
            roundingMode = RoundingMode.HALF_EVEN
        }
        return format.format(amount)
    }

    private companion object {
        const val DISPLAY_SCALE = 2
        const val RATE_MAX_FRACTION_DIGITS = 4
    }
}
