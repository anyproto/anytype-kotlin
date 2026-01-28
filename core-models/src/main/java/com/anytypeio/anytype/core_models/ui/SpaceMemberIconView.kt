package com.anytypeio.anytype.core_models.ui

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.UrlBuilder

sealed class SpaceMemberIconView {
    data class Placeholder(val name: String) : SpaceMemberIconView()
    data class Image(val url: String, val name: String) : SpaceMemberIconView()

    companion object {
        fun icon(obj: ObjectWrapper.SpaceMember, urlBuilder: UrlBuilder): SpaceMemberIconView {
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