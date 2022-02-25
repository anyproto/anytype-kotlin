package com.anytypeio.anytype.core_models.exceptions

sealed class CreateAccountException : Exception() {
    object BadInviteCode : CreateAccountException()
    object NetworkError: CreateAccountException()
    object OfflineDevice: CreateAccountException()
}