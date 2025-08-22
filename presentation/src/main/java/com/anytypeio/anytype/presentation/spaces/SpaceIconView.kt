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

fun ObjectWrapper.SpaceView.spaceIcon(
    builder: UrlBuilder,
) : SpaceIconView {
    val isChatSpace = this.spaceUxType == SpaceUxType.CHAT
    return when {
        !iconImage.isNullOrEmpty() -> {
            val hash = checkNotNull(iconImage)
            if (isChatSpace) {
                SpaceIconView.ChatSpace.Image(builder.medium(hash))
            } else {
                SpaceIconView.DataSpace.Image(builder.medium(hash))
            }
        }
        iconOption != null -> {
            val name = this.name
            val color = iconOption?.let {
                SystemColor.color(
                    idx = it.toInt()
                )
            }
            if (isChatSpace) {
                SpaceIconView.ChatSpace.Placeholder(
                    name = name.orEmpty(),
                    color =color ?: SystemColor.SKY
                )
            } else {
                SpaceIconView.DataSpace.Placeholder(
                    name = name.orEmpty(),
                    color =color ?: SystemColor.SKY
                )
            }
        }
        else -> {
            if (isChatSpace) {
                SpaceIconView.ChatSpace.Placeholder(
                    name = this.name.orEmpty(),
                    color = SystemColor.SKY
                )
            } else {
                SpaceIconView.DataSpace.Placeholder(
                    name = this.name.orEmpty(),
                    color = SystemColor.SKY
                )
            }
        }
    }
}