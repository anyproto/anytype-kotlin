package com.anytypeio.anytype.presentation.profile

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.domain.misc.UrlBuilder

sealed class ProfileIconView {
    object Loading : ProfileIconView()
    object Placeholder : ProfileIconView()
    data class Emoji(val unicode: String) : ProfileIconView()
    data class Image(val url: Url) : ProfileIconView()
}

fun ObjectWrapper.Basic.profileIcon(builder: UrlBuilder): ProfileIconView = when {
    !iconImage.isNullOrEmpty() -> {
        val hash = checkNotNull(iconImage)
        ProfileIconView.Image(builder.thumbnail(hash))
    }
    else -> ProfileIconView.Placeholder
}