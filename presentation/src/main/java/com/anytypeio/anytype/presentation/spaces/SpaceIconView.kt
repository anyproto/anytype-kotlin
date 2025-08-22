package com.anytypeio.anytype.presentation.spaces

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.SystemColor
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.domain.misc.UrlBuilder

sealed class SpaceIconView {
    data object Loading : SpaceIconView()

    sealed class ChatSpace: SpaceIconView(){
        data class Image(val url: Url) : ChatSpace()
        data class Placeholder(
            val color: SystemColor = SystemColor.SKY,
            val name: String = ""
        ) : ChatSpace()
    }

    sealed class DataSpace: SpaceIconView() {
        data class Image(val url: Url) : DataSpace()
        data class Placeholder(
            val color: SystemColor = SystemColor.SKY,
            val name: String = ""
        ) : DataSpace()
    }
}

private val DEFAULT_PLACEHOLDER_COLOR = SystemColor.SKY

fun ObjectWrapper.SpaceView.spaceIcon(
    builder: UrlBuilder,
) : SpaceIconView {
    val isChat = spaceUxType == SpaceUxType.CHAT

    // Helpers to eliminate duplication between Chat and Data branches
    val makeImage: (Url) -> SpaceIconView = { url ->
        if (isChat) {
            SpaceIconView.ChatSpace.Image(url)
        } else {
            SpaceIconView.DataSpace.Image(url)
        }
    }
    val makePlaceholder: (SystemColor, String) -> SpaceIconView = { color, title ->
        if (isChat) {
            SpaceIconView.ChatSpace.Placeholder(color = color, name = title)
        } else {
            SpaceIconView.DataSpace.Placeholder(color = color, name = title)
        }
    }

    // Prefer image if we have a non-empty hash
    iconImage?.takeIf { it.isNotEmpty() }?.let { hash ->
        return makeImage(builder.medium(hash))
    }

    // Derive placeholder color & name with safe defaults
    val placeholderColor = iconOption
        ?.toInt()
        ?.let { SystemColor.color(idx = it) }
        ?: DEFAULT_PLACEHOLDER_COLOR

    val displayName = name.orEmpty()

    return makePlaceholder(placeholderColor, displayName)
}