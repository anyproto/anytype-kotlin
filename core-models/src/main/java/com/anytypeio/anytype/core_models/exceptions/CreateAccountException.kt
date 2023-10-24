package com.anytypeio.anytype.core_models.exceptions

sealed class CreateAccountException : Exception() {
    @Deprecated("To be delete")
    object BadInviteCode : CreateAccountException()
    object NetworkError: CreateAccountException()
    object OfflineDevice: CreateAccountException()
}