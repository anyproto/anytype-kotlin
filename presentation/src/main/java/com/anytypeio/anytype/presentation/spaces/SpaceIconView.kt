package com.anytypeio.anytype.presentation.spaces

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.domain.misc.UrlBuilder

sealed class SpaceIconView {
    object Loading : SpaceIconView()
    object Placeholder : SpaceIconView()
    class Gradient(val from: String, val to: String) : SpaceIconView()
    data class Emoji(val unicode: String) : SpaceIconView()
    data class Image(val url: Url) : SpaceIconView()
}

fun ObjectWrapper.Basic.spaceIcon(
    builder: UrlBuilder,
    spaceGradientProvider: SpaceGradientProvider
) = when {
    !iconEmoji.isNullOrEmpty() -> {
        val emoji = checkNotNull(iconEmoji)
        SpaceIconView.Emoji(emoji)
    }
    !iconImage.isNullOrEmpty() -> {
        val hash = checkNotNull(iconImage)
        SpaceIconView.Image(builder.thumbnail(hash))
    }
    iconOption != null -> {
        iconOption?.let {
            val gradient = spaceGradientProvider.get(it)
            SpaceIconView.Gradient(gradient.from, gradient.to)
        } ?: SpaceIconView.Placeholder
    }
    else -> SpaceIconView.Placeholder
}