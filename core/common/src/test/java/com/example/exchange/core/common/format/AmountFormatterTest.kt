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

    @Test fun `parse returns null for empty input`() {
        assertThat(enUs.parse("")).isNull()
    }

    @Test fun `parse returns null for whitespace only`() {
        assertThat(enUs.parse("   ")).isNull()
    }

    @Test fun `parse returns null for non-numeric input`() {
        assertThat(enUs.parse("abc")).isNull()
    }

    @Test fun `parse returns null for multiple decimal separators`() {
        assertThat(enUs.parse("1.2.3")).isNull()
    }

    @Test fun `parse handles US decimal point`() {
        assertThat(enUs.parse("1.23")).isNotNull().isEqualByComparingTo("1.23")
    }

    @Test fun `parse rejects US grouping comma`() {
        // UI filter is expected to strip grouping separators before parse.
        assertThat(enUs.parse("1,234.56")).isNull()
    }

    @Test fun `parse rejects negative number`() {
        assertThat(enUs.parse("-10")).isNull()
    }

    @Test fun `parse rejects explicit positive sign`() {
        assertThat(enUs.parse("+10")).isNull()
    }

    @Test fun `parse rejects scientific notation`() {
        assertThat(enUs.parse("1e3")).isNull()
    }

    @Test fun `parse rejects mal-positioned grouping`() {
        assertThat(enUs.parse("1,2,3")).isNull()
    }

    @Test fun `parse handles leading decimal separator`() {
        assertThat(enUs.parse(".50")).isNotNull().isEqualByComparingTo("0.50")
    }

    @Test fun `parse handles PL decimal comma`() {
        assertThat(plPl.parse("1,23")).isNotNull().isEqualByComparingTo("1.23")
    }

    @Test fun `parse handles integer without decimal`() {
        assertThat(enUs.parse("42")).isNotNull().isEqualByComparingTo("42")
    }

    @Test fun `parse handles trailing decimal separator`() {
        assertThat(enUs.parse("1.")).isNotNull().isEqualByComparingTo("1")
    }

    @Test fun `parse handles leading zero`() {
        assertThat(enUs.parse("0.50")).isNotNull().isEqualByComparingTo("0.50")
    }

    @Test fun `format US locale pads scale and groups thousands`() {
        assertThat(enUs.format(BigDecimal("1234.5"), scale = 2)).isEqualTo("1,234.50")
    }

    @Test fun `format zero pads to scale`() {
        assertThat(enUs.format(BigDecimal.ZERO, scale = 2)).isEqualTo("0.00")
    }

    @Test fun `format rounds half-even down to even neighbour`() {
        assertThat(enUs.format(BigDecimal("1.245"), scale = 2)).isEqualTo("1.24")
    }

    @Test fun `format rounds half-even up to even neighbour`() {
        assertThat(enUs.format(BigDecimal("1.235"), scale = 2)).isEqualTo("1.24")
    }

    @Test fun `format with scale six keeps full precision`() {
        assertThat(enUs.format(BigDecimal("0.123456"), scale = 6)).isEqualTo("0.123456")
    }
}
