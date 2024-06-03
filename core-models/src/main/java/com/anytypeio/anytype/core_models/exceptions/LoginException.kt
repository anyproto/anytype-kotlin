package com.anytypeio.anytype.core_models.exceptions

sealed class LoginException : Exception() {
    class InvalidMnemonic : LoginException()
    class NetworkIdMismatch: LoginException()
}