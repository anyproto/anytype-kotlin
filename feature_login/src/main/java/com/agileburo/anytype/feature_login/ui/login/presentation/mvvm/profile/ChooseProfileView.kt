package com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.profile

import com.agileburo.anytype.feature_login.ui.login.presentation.ui.common.ViewType
import com.agileburo.anytype.feature_login.ui.login.presentation.ui.profile.SelectAccountAdapter.Companion.ADD_NEW_PROFILE
import com.agileburo.anytype.feature_login.ui.login.presentation.ui.profile.SelectAccountAdapter.Companion.PROFILE

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

}