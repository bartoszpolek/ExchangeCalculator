package com.example.exchange.core.common.format

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.util.Locale

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

    fun format(amount: BigDecimal, scale: Int): String {
        val rounded = amount.setScale(scale, RoundingMode.HALF_EVEN)
        val format = NumberFormat.getNumberInstance(locale).apply {
            minimumFractionDigits = scale
            maximumFractionDigits = scale
            isGroupingUsed = true
        }
        return format.format(rounded)
    }
}
