package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.Hash
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Url
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

    data class Bookmark(val image: Url) : ObjectIcon()

    data class File(val mime: String?, val fileName: String?) : ObjectIcon()

    companion object {
        fun from(
            obj: ObjectWrapper.Basic,
            layout: ObjectType.Layout?,
            builder: UrlBuilder,
            objectTypeNoIcon: Boolean = false
        ): ObjectIcon {
            val img = obj.iconImage
            val emoji = obj.iconEmoji
            return when (layout) {
                ObjectType.Layout.BASIC -> when {
                    !img.isNullOrBlank() -> Basic.Image(hash = builder.thumbnail(img))
                    !emoji.isNullOrBlank() -> Basic.Emoji(unicode = emoji)
                    else -> Basic.Avatar(obj.name.orEmpty())
                }
                ObjectType.Layout.OBJECT_TYPE -> when {
                    objectTypeNoIcon -> None
                    !img.isNullOrBlank() -> Basic.Image(hash = builder.thumbnail(img))
                    !emoji.isNullOrBlank() -> Basic.Emoji(unicode = emoji)
                    else -> Basic.Avatar(obj.name.orEmpty())
                }
                ObjectType.Layout.PROFILE -> if (!img.isNullOrBlank()) {
                    Profile.Image(hash = builder.thumbnail(img))
                } else {
                    Profile.Avatar(name = obj.name.orEmpty())
                }
                ObjectType.Layout.SET, ObjectType.Layout.COLLECTION -> if (!img.isNullOrBlank()) {
                    Basic.Image(hash = builder.thumbnail(img))
                } else if (!emoji.isNullOrBlank()) {
                    Basic.Emoji(unicode = emoji)
                } else {
                    Basic.Avatar(name = obj.name.orEmpty())
                }
                ObjectType.Layout.IMAGE -> if (!img.isNullOrBlank()) {
                    Basic.Image(hash = builder.thumbnail(img))
                } else {
                    None
                }
                ObjectType.Layout.TODO -> Task(isChecked = obj.done ?: false)
                ObjectType.Layout.NOTE -> Basic.Avatar(obj.snippet.orEmpty())
                ObjectType.Layout.FILE -> Basic.Avatar(obj.name.orEmpty())
                ObjectType.Layout.BOOKMARK -> when {
                    !img.isNullOrBlank() -> Bookmark(image = builder.thumbnail(img))
                    !emoji.isNullOrBlank() -> Basic.Emoji(unicode = emoji)
                    else -> Basic.Avatar(obj.name.orEmpty())
                }
                else -> None
            }
        }

        fun getEditorLinkToObjectIcon(
            obj: ObjectWrapper.Basic,
            layout: ObjectType.Layout?,
            builder: UrlBuilder
        ): ObjectIcon {
            val img = obj.iconImage
            val emoji = obj.iconEmoji
            return when (layout) {
                ObjectType.Layout.BASIC -> when {
                    !img.isNullOrBlank() -> Basic.Image(hash = builder.thumbnail(img))
                    !emoji.isNullOrBlank() -> Basic.Emoji(unicode = emoji)
                    else -> None
                }
                ObjectType.Layout.OBJECT_TYPE -> when {
                    !img.isNullOrBlank() -> Basic.Image(hash = builder.thumbnail(img))
                    !emoji.isNullOrBlank() -> Basic.Emoji(unicode = emoji)
                    else -> Basic.Avatar(obj.name.orEmpty())
                }
                ObjectType.Layout.PROFILE -> if (!img.isNullOrBlank()) {
                    Profile.Image(hash = builder.thumbnail(img))
                } else {
                    Profile.Avatar(name = obj.name.orEmpty())
                }
                ObjectType.Layout.SET, ObjectType.Layout.COLLECTION -> if (!img.isNullOrBlank()) {
                    Basic.Image(hash = builder.thumbnail(img))
                } else if (!emoji.isNullOrBlank()) {
                    Basic.Emoji(unicode = emoji)
                } else {
                    None
                }
                ObjectType.Layout.IMAGE -> if (!img.isNullOrBlank()) {
                    Basic.Image(hash = builder.thumbnail(img))
                } else {
                    None
                }
                ObjectType.Layout.TODO -> Task(isChecked = obj.done ?: false)
                ObjectType.Layout.NOTE -> Basic.Avatar(obj.snippet.orEmpty())
                ObjectType.Layout.FILE -> Basic.Avatar(obj.name.orEmpty())
                ObjectType.Layout.BOOKMARK -> when {
                    !img.isNullOrBlank() -> Bookmark(image = builder.thumbnail(img))
                    !emoji.isNullOrBlank() -> Basic.Emoji(unicode = emoji)
                    else -> None
                }
                ObjectType.Layout.RELATION -> None
                ObjectType.Layout.DASHBOARD -> None
                ObjectType.Layout.SPACE -> None
                ObjectType.Layout.DATABASE -> None
                else -> None
            }
        }
    }
}