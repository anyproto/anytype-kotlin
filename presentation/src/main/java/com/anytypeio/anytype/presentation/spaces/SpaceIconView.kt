package com.anytypeio.anytype.presentation.spaces

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.domain.misc.UrlBuilder

sealed class SpaceIconView {
    object Loading : SpaceIconView()
    object Placeholder : SpaceIconView()
    data class Emoji(val unicode: String) : SpaceIconView()
    data class Image(val url: Url) : SpaceIconView()
}

fun ObjectWrapper.Basic.spaceIcon(builder: UrlBuilder): SpaceIconView = when {
    !iconEmoji.isNullOrEmpty() -> {
        val emoji = checkNotNull(iconEmoji)
        SpaceIconView.Emoji(emoji)
    }
    !iconImage.isNullOrEmpty() -> {
        val hash = checkNotNull(iconImage)
        SpaceIconView.Image(builder.thumbnail(hash))
    }
    else -> SpaceIconView.Placeholder
}