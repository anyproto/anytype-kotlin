package com.anytypeio.anytype.presentation.profile

import com.anytypeio.anytype.domain.common.Url

data class ProfileView(
    val name: String,
    val avatar: Url? = null,
)