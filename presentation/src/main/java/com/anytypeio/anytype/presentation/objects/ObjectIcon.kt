package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.Hash
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.domain.misc.UrlBuilder

sealed class ObjectIcon {

    data object None :ObjectIcon()

    sealed class Empty : ObjectIcon() {
        data object Page : Empty()
        data object List : Empty()
        data object Bookmark : Empty()
        data object Discussion : Empty()
        data object ObjectType : Empty()
    }

    sealed class Basic : ObjectIcon() {
        data class Image(val hash: Hash, val emptyState: Empty = Empty.Page) : Basic()
        data class Emoji(val unicode: String, val emptyState: Empty = Empty.Page) : Basic()
    }

    sealed class Profile : ObjectIcon() {
        data class Avatar(val name: String) : Profile()
        data class Image(val hash: Hash) : Profile()
    }

    data class Task(val isChecked: Boolean) : ObjectIcon()

    data class Bookmark(val image: Url) : ObjectIcon()

    data class File(
        val mime: String?,
        val fileName: String?,
        val extensions: String? = null
    ) : ObjectIcon()

    data object Deleted : ObjectIcon()

    data class Checkbox(val isChecked: Boolean) : ObjectIcon()
}

sealed class SpaceMemberIconView {
    data class Placeholder(val name: String) : SpaceMemberIconView()
    data class Image(val url: String, val name: String) : SpaceMemberIconView()

    companion object {
        fun icon(obj: ObjectWrapper.SpaceMember, urlBuilder: UrlBuilder) : SpaceMemberIconView {
            val icon = obj.iconImage
            return when {
                !icon.isNullOrEmpty() -> Image(
                    url = urlBuilder.thumbnail(icon),
                    name = obj.name.orEmpty()
                )
                else -> Placeholder(name = obj.name.orEmpty())
            }
        }
    }
}