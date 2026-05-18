package com.example.exchange.core.common.format

import assertk.assertThat
import assertk.assertions.isEqualByComparingTo
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import org.junit.Test
import java.math.BigDecimal
import java.util.Locale

class AmountFormatterTest {

    private val enUs = AmountFormatter(Locale.US)
    private val plPl = AmountFormatter(Locale.forLanguageTag("pl-PL"))

    @Test
    fun `parse returns null for empty input`() {
        assertThat(enUs.parse("")).isNull()
    }

    @Test
    fun `parse returns null for whitespace only`() {
        assertThat(enUs.parse("   ")).isNull()
    }

    @Test
    fun `parse returns null for non-numeric input`() {
        assertThat(enUs.parse("abc")).isNull()
    }

    @Test
    fun `parse returns null for multiple decimal separators`() {
        assertThat(enUs.parse("1.2.3")).isNull()
    }

    @Test
    fun `parse handles US decimal point`() {
        assertThat(enUs.parse("1.23")).isNotNull().isEqualByComparingTo("1.23")
    }

    @Test
    fun `parse rejects US grouping comma`() {
        // UI filter is expected to strip grouping separators before parse.
        assertThat(enUs.parse("1,234.56")).isNull()
    }

    @Test
    fun `parse rejects negative number`() {
        assertThat(enUs.parse("-10")).isNull()
    }

    @Test
    fun `parse rejects explicit positive sign`() {
        assertThat(enUs.parse("+10")).isNull()
    }

    @Test
    fun `parse rejects scientific notation`() {
        assertThat(enUs.parse("1e3")).isNull()
    }

    @Test
    fun `parse handles leading decimal separator`() {
        assertThat(enUs.parse(".50")).isNotNull().isEqualByComparingTo("0.50")
    }

    @Test
    fun `parse handles PL decimal comma`() {
        assertThat(plPl.parse("1,23")).isNotNull().isEqualByComparingTo("1.23")
    }

    @Test
    fun `parse handles integer without decimal`() {
        assertThat(enUs.parse("42")).isNotNull().isEqualByComparingTo("42")
    }

    @Test
    fun `parse handles trailing decimal separator`() {
        assertThat(enUs.parse("1.")).isNotNull().isEqualByComparingTo("1")
    }

    @Test
    fun `parse handles leading zero`() {
        assertThat(enUs.parse("0.50")).isNotNull().isEqualByComparingTo("0.50")
    }

    @Test
    fun `formatDisplayAmount omits redundant zero fraction`() {
        assertThat(enUs.formatDisplayAmount(BigDecimal("1234.00"))).isEqualTo("1234")
    }

    @Test
    fun `formatDisplayAmount returns plain zero`() {
        assertThat(enUs.formatDisplayAmount(BigDecimal.ZERO)).isEqualTo("0")
    }

    @Test
    fun `formatDisplayAmount rounds half-even down to even neighbour`() {
        assertThat(enUs.formatDisplayAmount(BigDecimal("1.245"))).isEqualTo("1.24")
    }

    @Test
    fun `formatDisplayAmount rounds half-even up to even neighbour`() {
        assertThat(enUs.formatDisplayAmount(BigDecimal("1.235"))).isEqualTo("1.24")
    }

    @Test
    fun `formatRate US locale groups thousands and limits fraction`() {
        assertThat(enUs.formatRate(BigDecimal("1234.56789"))).isEqualTo("1,234.5679")
    }

    @Test
    fun `formatRate omits zero fraction`() {
        assertThat(enUs.formatRate(BigDecimal("2.0000"))).isEqualTo("2")
    }
}
