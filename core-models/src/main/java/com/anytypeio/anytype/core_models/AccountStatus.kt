package com.anytypeio.anytype.core_models

sealed class AccountStatus {
    object Unknown : AccountStatus()
    object Active : AccountStatus()
    data class PendingDeletion(val deadline: Long) : AccountStatus()
    object Deleted : AccountStatus()
}