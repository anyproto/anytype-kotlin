package com.anytypeio.anytype.presentation.profile

import com.anytypeio.anytype.core_models.Url

data class ProfileView(
    val name: String,
    val avatar: Url? = null,
)