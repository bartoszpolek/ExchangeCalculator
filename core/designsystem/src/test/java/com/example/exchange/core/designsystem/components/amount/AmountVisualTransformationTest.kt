package com.example.exchange.core.designsystem.components.amount

import androidx.compose.ui.text.AnnotatedString
import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.Test

class AmountVisualTransformationTest {

    @Test
    fun `adds prefix and grouping separators`() {
        val transformedText = AmountVisualTransformation(prefix = "$")
            .filter(AnnotatedString("1234.56"))
            .text
            .text

        assertThat(transformedText).isEqualTo("$1,234.56")
    }

    @Test
    fun `maps original offsets through prefix and grouping separators`() {
        val offsetMapping = AmountVisualTransformation(prefix = "$")
            .filter(AnnotatedString("1234.56"))
            .offsetMapping

        assertThat(offsetMapping.originalToTransformed(0)).isEqualTo(1)
        assertThat(offsetMapping.originalToTransformed(1)).isEqualTo(2)
        assertThat(offsetMapping.originalToTransformed(2)).isEqualTo(4)
        assertThat(offsetMapping.originalToTransformed(7)).isEqualTo(9)
    }

    @Test
    fun `maps transformed prefix and grouping offsets back to original`() {
        val offsetMapping = AmountVisualTransformation(prefix = "$")
            .filter(AnnotatedString("1234.56"))
            .offsetMapping

        assertThat(offsetMapping.transformedToOriginal(0)).isEqualTo(0)
        assertThat(offsetMapping.transformedToOriginal(1)).isEqualTo(0)
        assertThat(offsetMapping.transformedToOriginal(2)).isEqualTo(1)
        assertThat(offsetMapping.transformedToOriginal(3)).isEqualTo(1)
        assertThat(offsetMapping.transformedToOriginal(4)).isEqualTo(2)
        assertThat(offsetMapping.transformedToOriginal(9)).isEqualTo(7)
    }
}
