package com.agileburo.anytype.presentation.auth.model

import com.agileburo.anytype.core_utils.ui.ViewType

sealed class ChooseProfileView : ViewType {

    data class ProfileView(
        val id: String,
        val name: String
    ) : ChooseProfileView(), ViewType {
        override fun getViewType(): Int = PROFILE
    }

    object AddNewProfile : ChooseProfileView(), ViewType {
        override fun getViewType(): Int = ADD_NEW_PROFILE
    }

    companion object {
        const val PROFILE = 0
        const val ADD_NEW_PROFILE = 1
    }

}