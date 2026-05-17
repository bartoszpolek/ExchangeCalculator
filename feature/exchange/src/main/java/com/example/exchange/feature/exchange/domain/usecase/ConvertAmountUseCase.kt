package com.example.exchange.feature.exchange.domain.usecase

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import javax.inject.Inject

class ConvertAmountUseCase @Inject constructor() {

    operator fun invoke(
        amount: BigDecimal,
        midRate: BigDecimal,
        direction: ConvertDirection,
    ): BigDecimal =
        when (direction) {
            ConvertDirection.USDC_TO_FIAT ->
                amount.multiply(midRate)

            ConvertDirection.FIAT_TO_USDC ->
                amount.divide(midRate, CONVERSION_MATH_CONTEXT)
        }

    private companion object {
        val CONVERSION_MATH_CONTEXT = MathContext(20, RoundingMode.HALF_EVEN)
    }
}
