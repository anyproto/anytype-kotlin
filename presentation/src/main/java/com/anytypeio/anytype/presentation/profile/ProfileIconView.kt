package com.anytypeio.anytype.presentation.profile

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_models.UrlBuilder

sealed class ProfileIconView {
    object Loading : ProfileIconView()
    data class Placeholder(val name: String?) : ProfileIconView()
    data class Image(val url: Url) : ProfileIconView()
}

fun ObjectWrapper.Basic.profileIcon(builder: UrlBuilder): ProfileIconView = when {
    !iconImage.isNullOrEmpty() -> {
        val hash = checkNotNull(iconImage)
        ProfileIconView.Image(builder.medium(hash))
    }
    else -> ProfileIconView.Placeholder(
        name = name?.trim()?.ifEmpty { null }
    )
}

