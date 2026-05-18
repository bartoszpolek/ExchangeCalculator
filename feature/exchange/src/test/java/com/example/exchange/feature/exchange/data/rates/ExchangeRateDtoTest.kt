package com.example.exchange.feature.exchange.data.rates

import assertk.assertThat
import assertk.assertions.isEqualByComparingTo
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.example.exchange.feature.exchange.domain.model.CurrencyCode
import org.junit.Test

class ExchangeRateDtoTest {

    @Test
    fun `maps DTO to domain exchange rate`() {
        val rate = ExchangeRateDto(
            ask = "18.4105000000",
            bid = "18.4069700000",
            book = "usdc_mxn",
            date = "2025-10-20T20:14:57.361483956",
        ).toExchangeRate()

        assertThat(rate).isNotNull()
        assertThat(rate?.currencyCode).isEqualTo(CurrencyCode("MXN"))
        assertThat(rate?.ask).isNotNull().isEqualByComparingTo("18.4105000000")
        assertThat(rate?.bid).isNotNull().isEqualByComparingTo("18.4069700000")
    }

    @Test
    fun `returns null when ask is malformed`() {
        val rate = validDto().copy(ask = "not-a-number").toExchangeRate()

        assertThat(rate).isNull()
    }

    @Test
    fun `returns null when bid is malformed`() {
        val rate = validDto().copy(bid = "not-a-number").toExchangeRate()

        assertThat(rate).isNull()
    }

    @Test
    fun `returns null when ask is zero`() {
        val rate = validDto().copy(ask = "0").toExchangeRate()

        assertThat(rate).isNull()
    }

    @Test
    fun `returns null when bid is zero`() {
        val rate = validDto().copy(bid = "0").toExchangeRate()

        assertThat(rate).isNull()
    }

    @Test
    fun `returns null when ask is negative`() {
        val rate = validDto().copy(ask = "-1").toExchangeRate()

        assertThat(rate).isNull()
    }

    @Test
    fun `returns null when bid is negative`() {
        val rate = validDto().copy(bid = "-1").toExchangeRate()

        assertThat(rate).isNull()
    }

    @Test
    fun `returns null when book does not use expected prefix`() {
        val rate = validDto().copy(book = "mxn_usdc").toExchangeRate()

        assertThat(rate).isNull()
    }

    private fun validDto(): ExchangeRateDto =
        ExchangeRateDto(
            ask = "18.4105000000",
            bid = "18.4069700000",
            book = "usdc_mxn",
            date = "2025-10-20T20:14:57.361483956",
        )
}
