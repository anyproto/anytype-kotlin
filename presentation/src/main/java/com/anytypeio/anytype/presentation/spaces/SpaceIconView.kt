package com.anytypeio.anytype.presentation.spaces

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.domain.misc.UrlBuilder

sealed class SpaceIconView {
    data object Loading : SpaceIconView()
    data object Placeholder : SpaceIconView()
    class Gradient(val from: String, val to: String) : SpaceIconView()
    data class Image(val url: Url) : SpaceIconView()
}

fun ObjectWrapper.Basic.spaceIcon(
    builder: UrlBuilder,
    spaceGradientProvider: SpaceGradientProvider
) = when {
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

fun ObjectWrapper.SpaceView.spaceIcon(
    builder: UrlBuilder,
    spaceGradientProvider: SpaceGradientProvider,
) = when {
    !iconImage.isNullOrEmpty() -> {
        val hash = checkNotNull(iconImage)
        SpaceIconView.Image(builder.medium(hash))
    }
    iconOption != null -> {
        iconOption?.let {
            val gradient = spaceGradientProvider.get(it)
            SpaceIconView.Gradient(gradient.from, gradient.to)
        } ?: SpaceIconView.Placeholder
    }
    else -> SpaceIconView.Placeholder
}