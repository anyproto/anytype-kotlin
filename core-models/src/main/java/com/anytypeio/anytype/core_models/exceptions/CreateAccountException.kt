package com.anytypeio.anytype.core_models.exceptions

sealed class CreateAccountException : Exception() {
    // Account creation partial failures
    data object AccountCreatedButFailedToStartNode: CreateAccountException()
    data object AccountCreatedButFailedToSetName: CreateAccountException()
    data object FailedToStopRunningNode: CreateAccountException()
    data object FailedToWriteConfig: CreateAccountException()
    data object FailedToCreateLocalRepo: CreateAccountException()
    data object AccountCreationCanceled: CreateAccountException()
    
    // Config file errors
    data object ConfigFileNotFound: CreateAccountException()
    data object ConfigFileInvalid: CreateAccountException()
    data object ConfigFileNetworkIdMismatch: CreateAccountException()
}