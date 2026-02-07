package com.anytypeio.anytype.core_models.ui

/**
 * Represents the current user's account profile state.
 */
sealed class AccountProfile {
    data object Idle : AccountProfile()
    class Data(
        val name: String,
        val icon: ProfileIconView,
        val identity: String? = null,
        val globalName: String? = null
    ) : AccountProfile()
}