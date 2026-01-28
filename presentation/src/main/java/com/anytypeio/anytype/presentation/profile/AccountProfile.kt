package com.anytypeio.anytype.presentation.profile

sealed class AccountProfile {
    data object Idle : AccountProfile()
    class Data(
        val name: String,
        val icon: ProfileIconView,
        val identity: String? = null,
        val globalName: String? = null
    ) : AccountProfile()
}