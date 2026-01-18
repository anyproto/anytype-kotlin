package com.anytypeio.anytype.core_models.ui

import com.anytypeio.anytype.core_models.Url

/**
 * Represents the visual icon for a user profile in the UI.
 */
sealed class ProfileIconView {
    data object Loading : ProfileIconView()
    data class Placeholder(val name: String?) : ProfileIconView()
    data class Image(val url: Url) : ProfileIconView()
}

/**
 * Represents the current user's account profile state.
 */
sealed class AccountProfile {
    data object Idle : AccountProfile()
    data class Data(
        val name: String,
        val icon: ProfileIconView,
        val identity: String? = null,
        val globalName: String? = null
    ) : AccountProfile()
}
