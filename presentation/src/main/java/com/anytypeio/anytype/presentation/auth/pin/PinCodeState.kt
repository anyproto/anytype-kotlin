package com.anytypeio.anytype.presentation.auth.pin

data class PinCodeState(val digits: List<Int>) {
    val completed: Boolean = digits.size == PIN_CODE_MAX_LENGTH

    companion object {
        const val PIN_CODE_MAX_LENGTH = 6
    }
}