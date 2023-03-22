package com.anytypeio.anytype.presentation.spaces

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.domain.misc.UrlBuilder

sealed class SpaceIcon {
    object Placeholder : SpaceIcon()
    data class Emoji(val unicode: String) : SpaceIcon()
    data class Image(val url: Url) : SpaceIcon()
}

fun ObjectWrapper.Basic.spaceIcon(builder: UrlBuilder): SpaceIcon = when {
    !iconEmoji.isNullOrEmpty() -> {
        val emoji = checkNotNull(iconEmoji)
        SpaceIcon.Emoji(emoji)
    }
    !iconImage.isNullOrEmpty() -> {
        val hash = checkNotNull(iconImage)
        SpaceIcon.Image(builder.thumbnail(hash))
    }
    else -> SpaceIcon.Placeholder
}