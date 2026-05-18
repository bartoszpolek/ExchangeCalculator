package com.example.exchange.core.designsystem.components.amount

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

internal class AmountVisualTransformation(
    private val prefix: String,
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val groupedText = text.text.withGroupingSeparators()
        val transformedText = prefix + groupedText
        return TransformedText(
            text = AnnotatedString(transformedText),
            offsetMapping = AmountOffsetMapping(
                originalText = text.text,
                groupedText = groupedText,
                prefixLength = prefix.length,
            ),
        )
    }
}

private class AmountOffsetMapping(
    originalText: String,
    groupedText: String,
    private val prefixLength: Int,
) : OffsetMapping {

    private val groupingOffsetMapping = AmountGroupingOffsetMapping(
        originalText = originalText,
        transformedText = groupedText,
    )

    override fun originalToTransformed(offset: Int): Int =
        prefixLength + groupingOffsetMapping.originalToTransformed(offset)

    override fun transformedToOriginal(offset: Int): Int =
        if (offset <= prefixLength) {
            0
        } else {
            groupingOffsetMapping.transformedToOriginal(offset - prefixLength)
        }
}

private class AmountGroupingOffsetMapping(
    private val originalText: String,
    private val transformedText: String,
) : OffsetMapping {

    override fun originalToTransformed(offset: Int): Int {
        val clampedOffset = offset.coerceIn(0, originalText.length)
        var originalOffset = 0

        transformedText.forEachIndexed { index, char ->
            if (originalOffset == clampedOffset) return index
            if (char != GROUPING_SEPARATOR) {
                originalOffset++
            }
        }

        return transformedText.length
    }

    override fun transformedToOriginal(offset: Int): Int {
        val clampedOffset = offset.coerceIn(0, transformedText.length)
        return transformedText
            .take(clampedOffset)
            .count { char -> char != GROUPING_SEPARATOR }
            .coerceIn(0, originalText.length)
    }
}

private fun String.withGroupingSeparators(): String {
    if (isEmpty()) return this

    val decimalIndex = indexOf(DECIMAL_SEPARATOR)
    val integerPart = if (decimalIndex == -1) {
        this
    } else {
        substring(0, decimalIndex)
    }
    val fractionPart = if (decimalIndex == -1) {
        ""
    } else {
        substring(decimalIndex)
    }

    val groupedIntegerPart = integerPart
        .reversed()
        .chunked(GROUP_SIZE)
        .joinToString(GROUPING_SEPARATOR.toString())
        .reversed()

    return groupedIntegerPart + fractionPart
}

private const val DECIMAL_SEPARATOR = "."
private const val GROUP_SIZE = 3
private const val GROUPING_SEPARATOR = ','
