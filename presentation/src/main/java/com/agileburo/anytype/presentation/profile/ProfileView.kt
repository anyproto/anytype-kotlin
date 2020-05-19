package com.agileburo.anytype.presentation.profile

import com.agileburo.anytype.domain.common.Url

data class ProfileView(
    val name: String,
    val avatar: Url? = null
)