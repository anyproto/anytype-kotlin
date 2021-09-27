package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView

fun List<Map<String, Any?>>.toDefaultObjectView(
    urlBuilder: UrlBuilder,
    objectTypes: List<ObjectType>
): List<DefaultObjectView> =
    this.map { record ->
        val obj = ObjectWrapper.Basic(record)
        val type = obj.type.firstOrNull()
        val layout = obj.layout ?: ObjectType.Layout.BASIC
        DefaultObjectView(
            id = obj.id,
            name = obj.name.orEmpty(),
            typeName = objectTypes.find { it.url == type }?.name.orEmpty(),
            typeLayout = layout,
            icon = ObjectIcon.from(
                obj = obj,
                layout = layout,
                builder = urlBuilder
            ),
            type = type
        )
    }