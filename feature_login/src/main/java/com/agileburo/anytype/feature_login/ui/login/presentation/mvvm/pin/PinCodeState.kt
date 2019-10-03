package com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.pin

data class PinCodeState(val digits: List<Int>) {
    val completed: Boolean = digits.size == PIN_CODE_MAX_LENGTH

    companion object {
        const val PIN_CODE_MAX_LENGTH = 6
    }
}