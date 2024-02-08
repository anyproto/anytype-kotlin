package com.anytypeio.anytype.core_models.exceptions

sealed class LoginException : Exception() {
    object InvalidMnemonic : LoginException()
}