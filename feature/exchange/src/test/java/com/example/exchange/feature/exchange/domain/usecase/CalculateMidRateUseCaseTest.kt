package com.example.exchange.feature.exchange.domain.usecase

import assertk.assertThat
import assertk.assertions.isEqualByComparingTo
import org.junit.Test
import java.math.BigDecimal

class CalculateMidRateUseCaseTest {

    private val calculateMidRate = CalculateMidRateUseCase()

    @Test fun `calculates midpoint between ask and bid`() {
        val result = calculateMidRate(
            ask = BigDecimal("18.4105000000"),
            bid = BigDecimal("18.4069700000"),
        )

        assertThat(result).isEqualByComparingTo("18.4087350000")
    }

    @Test fun `rounds midpoint using half even precision`() {
        val result = calculateMidRate(
            ask = BigDecimal("1.23456789012345678904"),
            bid = BigDecimal("1.23456789012345678906"),
        )

        assertThat(result).isEqualByComparingTo("1.2345678901234567890")
    }

    @Test fun `returns same rate when ask and bid are equal`() {
        val result = calculateMidRate(
            ask = BigDecimal("18.4105000000"),
            bid = BigDecimal("18.4105000000"),
        )

        assertThat(result).isEqualByComparingTo("18.4105000000")
    }
}
