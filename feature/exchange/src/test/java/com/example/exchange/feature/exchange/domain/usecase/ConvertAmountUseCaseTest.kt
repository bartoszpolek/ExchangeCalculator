package com.example.exchange.feature.exchange.domain.usecase

import assertk.assertThat
import assertk.assertions.isEqualByComparingTo
import org.junit.Test
import java.math.BigDecimal

class ConvertAmountUseCaseTest {

    private val convertAmount = ConvertAmountUseCase()

    @Test fun `converts USDC amount to fiat amount`() {
        val result = convertAmount(
            amount = BigDecimal("12.345"),
            midRate = BigDecimal("2"),
            direction = ConvertDirection.USDC_TO_FIAT,
        )

        assertThat(result).isEqualByComparingTo("24.690")
    }

    @Test fun `converts fiat amount to USDC amount`() {
        val result = convertAmount(
            amount = BigDecimal("10"),
            midRate = BigDecimal("3"),
            direction = ConvertDirection.FIAT_TO_USDC,
        )

        assertThat(result).isEqualByComparingTo("3.3333333333333333333")
    }
}
