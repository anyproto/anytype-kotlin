package com.anytypeio.anytype.presentation.spaces

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.SystemColor
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.domain.misc.UrlBuilder

sealed class SpaceIconView {
    data object Loading : SpaceIconView()
    data class Placeholder(
        val color: SystemColor = SystemColor.YELLOW,
        val name: String = ""
    ): SpaceIconView()
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
        SpaceIconView.Placeholder(
            name = name.orEmpty(),
            color = iconOption?.let {
                SystemColor.color(
                    idx = it.toInt()
                )
            } ?: SystemColor.YELLOW
        )
    }
    else -> SpaceIconView.Placeholder(
        name = name.orEmpty(),
        color = SystemColor.YELLOW
    )
}

// TODO delete provider
fun ObjectWrapper.SpaceView.spaceIcon(
    builder: UrlBuilder,
    spaceGradientProvider: SpaceGradientProvider,
) = when {
    !iconImage.isNullOrEmpty() -> {
        val hash = checkNotNull(iconImage)
        SpaceIconView.Image(builder.medium(hash))
    }
    iconOption != null -> {
        SpaceIconView.Placeholder(
            name = name.orEmpty(),
            color = iconOption?.let {
                SystemColor.color(
                    idx = it.toInt()
                )
            } ?: SystemColor.YELLOW
        )
    }
    else -> SpaceIconView.Placeholder(
        name = name.orEmpty(),
        color = SystemColor.YELLOW
    )
}