package com.anytypeio.anytype.presentation.auth.model

import com.anytypeio.anytype.core_utils.ui.ViewType
import com.anytypeio.anytype.domain.common.Url

sealed class SelectAccountView : ViewType {

    data class AccountView(
        val id: String,
        val name: String,
        val image: Url? = null
    ) : SelectAccountView(), ViewType {
        override fun getViewType(): Int = PROFILE
    }

    companion object {
        const val PROFILE = 0
        const val ADD_NEW_PROFILE = 1
    }
}