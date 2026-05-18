package com.example.exchange.feature.exchange.presentation

import androidx.annotation.DrawableRes
import com.example.exchange.feature.exchange.R
import com.example.exchange.feature.exchange.domain.model.CurrencyCode

object CurrencyFlag {

    @DrawableRes
    fun resourceFor(code: CurrencyCode): Int =
        flagsByCurrency[code] ?: R.drawable.ic_flag_unknown

    private val flagsByCurrency = mapOf(
        CurrencyCode("MXN") to R.drawable.ic_flag_mxn,
        CurrencyCode("ARS") to R.drawable.ic_flag_ars,
        CurrencyCode("BRL") to R.drawable.ic_flag_brl,
        CurrencyCode("COP") to R.drawable.ic_flag_cop,
        CurrencyCode("USDC") to R.drawable.ic_flag_usdc,
    )
}
