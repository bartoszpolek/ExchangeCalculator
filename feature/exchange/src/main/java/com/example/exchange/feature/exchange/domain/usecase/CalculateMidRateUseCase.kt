package com.example.exchange.feature.exchange.domain.usecase

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import javax.inject.Inject

class CalculateMidRateUseCase @Inject constructor() {

    operator fun invoke(
        ask: BigDecimal,
        bid: BigDecimal,
    ): BigDecimal =
        ask.add(bid)
            .divide(TWO, RATE_MATH_CONTEXT)

    private companion object {
        val TWO: BigDecimal = BigDecimal("2")
        val RATE_MATH_CONTEXT = MathContext(20, RoundingMode.HALF_EVEN)
    }
}
