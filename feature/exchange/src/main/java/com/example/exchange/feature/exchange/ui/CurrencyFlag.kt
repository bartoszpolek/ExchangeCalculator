package com.example.exchange.feature.exchange.ui

import androidx.annotation.DrawableRes
import com.example.exchange.feature.exchange.R
import com.example.exchange.feature.exchange.domain.model.CurrencyCode

object CurrencyFlag {

    @DrawableRes
    fun resourceFor(code: CurrencyCode): Int =
        when (code.code) {
            "MXN" -> R.drawable.ic_flag_mxn
            "ARS" -> R.drawable.ic_flag_ars
            "BRL" -> R.drawable.ic_flag_brl
            "COP" -> R.drawable.ic_flag_cop
            "USDC" -> R.drawable.ic_flag_usdc
            else -> R.drawable.ic_flag_unknown
        }
}
