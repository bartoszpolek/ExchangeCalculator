package com.example.exchange.core.designsystem.components.amount

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.example.exchange.core.common.format.ZERO_AMOUNT

internal fun TextFieldValue.normalizeAmountTextFieldValue(
    previousValue: TextFieldValue = TextFieldValue(),
): TextFieldValue {
    if (
        previousValue.text.hasDecimalSeparator() &&
        text.decimalSeparatorCount() > previousValue.text.decimalSeparatorCount()
    ) {
        return previousValue
    }
    if (isEditingExistingFraction(previousValue)) {
        return previousValue
    }

    val normalizedAmount = text.normalizeAmountInput(previousInput = previousValue.text)
    if (
        normalizedAmount == previousValue.text &&
        isCollapsedInsertion(previousValue)
    ) {
        return previousValue
    }

    val normalizedSelection = selection.end.coerceIn(0, normalizedAmount.length)
    return copy(
        text = normalizedAmount,
        selection = TextRange(normalizedSelection),
    )
}

internal fun CharSequence.finalizeAmountInput(): String =
    toString()
        .trimTrailingFractionZeros()
        .ifEmpty { ZERO_AMOUNT }

internal fun CharSequence.normalizeAmountInput(
    previousInput: CharSequence = "",
): String {
    val normalized = StringBuilder()
    var hasDecimalSeparator = false

    forEach { char ->
        when {
            char.isDigit() -> normalized.append(char)
            char.isDecimalSeparator() && !hasDecimalSeparator -> {
                normalized.append(DECIMAL_SEPARATOR)
                hasDecimalSeparator = true
            }
        }
    }

    if (normalized.isEmpty()) return ZERO_AMOUNT

    val decimalIndex = normalized.indexOf(DECIMAL_SEPARATOR)
    val integerEnd = if (decimalIndex == -1) normalized.length else decimalIndex
    val integerPart = normalized
        .substring(0, integerEnd)
        .trimStart('0')
        .ifEmpty { ZERO_AMOUNT }

    // Let the startup "0" behave like a placeholder when the first digit is typed before it.
    if (
        previousInput.toString() == ZERO_AMOUNT &&
        decimalIndex == -1 &&
        normalized.length == 2 &&
        normalized.last() == '0' &&
        normalized.first() in '1'..'9'
    ) {
        return normalized.first().toString()
    }

    return if (decimalIndex == -1) {
        integerPart
    } else {
        val decimalPart = normalized.substring(decimalIndex + 1)
            .take(MAX_DECIMAL_DIGITS)
        integerPart + DECIMAL_SEPARATOR + decimalPart
    }
}

private fun String.trimTrailingFractionZeros(): String {
    if (!contains(DECIMAL_SEPARATOR)) return this

    return trimEnd('0')
        .removeSuffix(DECIMAL_SEPARATOR)
}

private fun TextFieldValue.isEditingExistingFraction(previousValue: TextFieldValue): Boolean {
    val previousText = previousValue.text
    val decimalIndex = previousText.indexOfFirst { char -> char.isDecimalSeparator() }
    if (decimalIndex == -1) return false

    val fractionLength = previousText.length - decimalIndex - 1
    if (fractionLength < MAX_DECIMAL_DIGITS) return false

    val editStart = previousValue.selection.min
    val isInsideExistingFraction = editStart in (decimalIndex + 1) until previousText.length
    if (!isInsideExistingFraction) return false

    val selectedLength = previousValue.selection.max - previousValue.selection.min
    val insertedLength = text.length - (previousText.length - selectedLength)
    return insertedLength > 0
}

private fun TextFieldValue.isCollapsedInsertion(previousValue: TextFieldValue): Boolean =
    previousValue.selection.start == previousValue.selection.end &&
            text.length > previousValue.text.length

private fun Char.isDecimalSeparator(): Boolean =
    this == '.' || this == ','

private fun CharSequence.hasDecimalSeparator(): Boolean =
    any { char -> char.isDecimalSeparator() }

private fun CharSequence.decimalSeparatorCount(): Int =
    count { char -> char.isDecimalSeparator() }

private const val DECIMAL_SEPARATOR = "."
private const val MAX_DECIMAL_DIGITS = 2
