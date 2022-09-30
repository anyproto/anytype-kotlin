package com.anytypeio.anytype.presentation.number

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Converts relation {format NUMBER} value {Any?} to string representation or null
 */
object NumberParser {

    fun parse(value: Any?): String? {
        val doubleValue = when (value) {
            is String -> value.toDouble()
            is Number -> value.toDouble()
            else -> null
        }
        return if (doubleValue != null) {
            val decimal = BigDecimal.valueOf(doubleValue)
            if (decimal.isWhole) {
                try {
                    decimal.longValueExact().toString()
                } catch (e: ArithmeticException) {
                    decimal.toPlainString()
                }
            } else {
                decimal.toPlainString()
            }
        } else {
            return null
        }
    }

    private val BigDecimal.isWhole: Boolean
        get() {
            // fast-path
            if (scale() <= 0) return true
            val digitsAfterDot = this - this.setScale(0, RoundingMode.DOWN)
            // [BigDecimal.equals] check `scale` and other properties
            return digitsAfterDot.compareTo(BigDecimal.ZERO) == 0
        }
}