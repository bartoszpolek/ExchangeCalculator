package com.example.exchange.core.designsystem.components.amount

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test

class AmountInputNormalizerTest {

    @Test
    fun `empty input normalizes to zero`() {
        assertThat("".normalizeAmountInput()).isEqualTo("0")
    }

    @Test
    fun `decimal separators are normalized and fraction is limited`() {
        assertThat("12,34".normalizeAmountInput()).isEqualTo("12.34")
        assertThat("12.3,4.5".normalizeAmountInput()).isEqualTo("12.34")
        assertThat("44.444".normalizeAmountInput()).isEqualTo("44.44")
    }

    @Test
    fun `leading decimal separator gets zero prefix`() {
        assertThat(".50".normalizeAmountInput()).isEqualTo("0.50")
    }

    @Test
    fun `leading zeros are collapsed`() {
        assertThat("00012.30".normalizeAmountInput()).isEqualTo("12.30")
    }

    @Test
    fun `trailing decimal separator is preserved while editing and removed on finalize`() {
        assertThat("44.".normalizeAmountInput()).isEqualTo("44.")
        assertThat("44.".finalizeAmountInput()).isEqualTo("44")
    }

    @Test
    fun `finalize trims redundant fraction zeros`() {
        assertThat("22.00".finalizeAmountInput()).isEqualTo("22")
        assertThat("22.0".finalizeAmountInput()).isEqualTo("22")
        assertThat("22.30".finalizeAmountInput()).isEqualTo("22.3")
        assertThat(".".finalizeAmountInput()).isEqualTo("0")
    }

    @Test
    fun `first typed digit replaces initial zero even when inserted before zero`() {
        assertThat("10".normalizeAmountInput(previousInput = "0")).isEqualTo("1")
    }

    @Test
    fun `non amount characters are ignored`() {
        assertThat("$1a2 3".normalizeAmountInput()).isEqualTo("123")
    }

    @Test
    fun `ignored leading zero insertion keeps previous cursor position`() {
        val previousValue = TextFieldValue(
            text = "12",
            selection = TextRange(0),
        )
        val proposedValue = TextFieldValue(
            text = "012",
            selection = TextRange(1),
        )

        val normalizedValue = proposedValue.normalizeAmountTextFieldValue(
            previousValue = previousValue,
        )

        assertThat(normalizedValue.text).isEqualTo("12")
        assertThat(normalizedValue.selection).isEqualTo(TextRange(0))
    }

    @Test
    fun `duplicate decimal separator keeps previous cursor position`() {
        val previousValue = TextFieldValue(
            text = "12.34",
            selection = TextRange(1),
        )
        val proposedValue = TextFieldValue(
            text = "1.2.34",
            selection = TextRange(2),
        )

        val normalizedValue = proposedValue.normalizeAmountTextFieldValue(
            previousValue = previousValue,
        )

        assertThat(normalizedValue.text).isEqualTo("12.34")
        assertThat(normalizedValue.selection).isEqualTo(TextRange(1))
    }

    @Test
    fun `digit inserted before first fraction digit is rejected when fraction is full`() {
        val previousValue = TextFieldValue(
            text = "12.33",
            selection = TextRange(3),
        )
        val proposedValue = TextFieldValue(
            text = "12.433",
            selection = TextRange(4),
        )

        val normalizedValue = proposedValue.normalizeAmountTextFieldValue(
            previousValue = previousValue,
        )

        assertThat(normalizedValue.text).isEqualTo("12.33")
        assertThat(normalizedValue.selection).isEqualTo(TextRange(3))
    }

    @Test
    fun `digit inserted before only fraction digit is accepted`() {
        val previousValue = TextFieldValue(
            text = "1.6",
            selection = TextRange(2),
        )
        val proposedValue = TextFieldValue(
            text = "1.56",
            selection = TextRange(3),
        )

        val normalizedValue = proposedValue.normalizeAmountTextFieldValue(
            previousValue = previousValue,
        )

        assertThat(normalizedValue.text).isEqualTo("1.56")
        assertThat(normalizedValue.selection).isEqualTo(TextRange(3))
    }

    @Test
    fun `fraction digit can be appended after existing fraction digit`() {
        val proposedValue = TextFieldValue(
            text = "12.34",
            selection = TextRange(5),
        )

        val normalizedValue = proposedValue.normalizeAmountTextFieldValue(
            previousValue = TextFieldValue("12.3", TextRange(4)),
        )

        assertThat(normalizedValue.text).isEqualTo("12.34")
        assertThat(normalizedValue.selection).isEqualTo(TextRange(5))
    }

    @Test
    fun `fraction digit can be deleted from full fraction`() {
        val previousValue = TextFieldValue(
            text = "1.23",
            selection = TextRange(4),
        )
        val proposedValue = TextFieldValue(
            text = "1.2",
            selection = TextRange(3),
        )

        val normalizedValue = proposedValue.normalizeAmountTextFieldValue(
            previousValue = previousValue,
        )

        assertThat(normalizedValue.text).isEqualTo("1.2")
        assertThat(normalizedValue.selection).isEqualTo(TextRange(3))
    }

}
