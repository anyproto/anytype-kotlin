package com.anytypeio.anytype.presentation.profile

import com.anytypeio.anytype.core_models.ui.ProfileIconView

sealed class AccountProfile {
    data object Idle : AccountProfile()
    class Data(
        val name: String,
        val icon: ProfileIconView,
        val identity: String? = null,
        val globalName: String? = null
    ) : AccountProfile()
}