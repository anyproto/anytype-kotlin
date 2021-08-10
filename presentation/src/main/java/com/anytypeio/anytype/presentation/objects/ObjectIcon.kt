package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.Hash
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.misc.UrlBuilder

sealed class ObjectIcon {
    object None : ObjectIcon()
    sealed class Basic : ObjectIcon() {
        data class Avatar(val name: String) : Basic()
        data class Image(val hash: Hash) : Basic()
        data class Emoji(val unicode: String) : Basic()
    }

    sealed class Profile : ObjectIcon() {
        data class Avatar(val name: String) : Profile()
        data class Image(val hash: Hash) : Profile()
    }

    data class Task(val isChecked: Boolean) : ObjectIcon()

    companion object {
        fun from(
            obj: ObjectWrapper.Basic,
            layout: ObjectType.Layout?,
            builder: UrlBuilder
        ): ObjectIcon = when (layout) {
            ObjectType.Layout.BASIC -> {
                val img = obj.iconImage
                val emoji = obj.iconEmoji
                when {
                    !img.isNullOrBlank() -> {
                        Basic.Image(hash = builder.thumbnail(img))
                    }
                    !emoji.isNullOrBlank() -> {
                        Basic.Emoji(unicode = emoji)
                    }
                    else -> {
                        Basic.Avatar(obj.name.orEmpty())
                    }
                }
            }
            ObjectType.Layout.PROFILE -> {
                val img = obj.iconImage
                if (!img.isNullOrBlank()) {
                    Profile.Image(hash = builder.thumbnail(img))
                } else {
                    Profile.Avatar(name = obj.name.orEmpty())
                }
            }
            ObjectType.Layout.TODO -> {
                Task(isChecked = obj.done ?: false)
            }
            ObjectType.Layout.SET -> {
                val img = obj.iconImage
                val emoji = obj.iconEmoji
                if (!img.isNullOrBlank()) {
                    Basic.Image(hash = builder.thumbnail(img))
                } else if (!emoji.isNullOrBlank()) {
                    Basic.Emoji(unicode = emoji)
                } else {
                    None
                }
            }
            else -> None
        }
    }
}