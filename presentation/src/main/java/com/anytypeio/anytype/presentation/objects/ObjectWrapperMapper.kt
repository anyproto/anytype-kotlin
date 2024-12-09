package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.ext.DateParser
import com.anytypeio.anytype.core_utils.ext.readableFileSize
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.presentation.linking.LinkToItemView
import com.anytypeio.anytype.presentation.mapper.objectIcon
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.presentation.sets.filter.CreateFilterView
import com.anytypeio.anytype.presentation.widgets.collection.CollectionView

fun List<ObjectWrapper.Basic>.toViews(
    urlBuilder: UrlBuilder,
    objectTypes: List<ObjectWrapper.Type>,
    fieldParser: FieldParser
): List<DefaultObjectView> = map { obj ->
    obj.toView(urlBuilder, objectTypes, fieldParser = fieldParser)
}

fun ObjectWrapper.Basic.toView(
    urlBuilder: UrlBuilder,
    objectTypes: List<ObjectWrapper.Type>,
    fieldParser: FieldParser
): DefaultObjectView {
    val obj = this
    val (objTypeId, objTypeName) = fieldParser.getObjectTypeIdAndName(
        objectWrapper = obj,
        types = objectTypes
    )
    val layout = obj.getProperLayout()
    return DefaultObjectView(
        id = obj.id,
        name = fieldParser.getObjectName(obj),
        description = obj.description,
        type = objTypeId,
        typeName = objTypeName,
        layout = layout,
        icon = obj.objectIcon(urlBuilder),
        lastModifiedDate = DateParser.parseInMillis(obj.lastModifiedDate) ?: 0L,
        lastOpenedDate = DateParser.parseInMillis(obj.lastOpenedDate) ?: 0L,
        isFavorite = obj.isFavorite == true,
        space = requireNotNull(obj.spaceId)
    )
}

fun List<ObjectWrapper.Basic>.toLinkToView(
    urlBuilder: UrlBuilder,
    objectTypes: List<ObjectWrapper.Type>,
    fieldParser: FieldParser,
): List<LinkToItemView.Object> =
    this.mapIndexed { index, obj ->
        val typeUrl = obj.getProperType()
        val layout = obj.getProperLayout()
        LinkToItemView.Object(
            id = obj.id,
            title = fieldParser.getObjectName(obj),
            subtitle = getProperTypeName(id = typeUrl, types = objectTypes),
            type = typeUrl,
            layout = layout,
            icon = obj.objectIcon(urlBuilder),
            position = index
        )
    }

fun ObjectWrapper.Basic.toLinkToObjectView(
    urlBuilder: UrlBuilder,
    objectTypes: List<ObjectWrapper.Type>,
    fieldParser: FieldParser,
): LinkToItemView.LinkedTo.Object {
    val typeUrl = this.getProperType()
    val layout = this.getProperLayout()
    return LinkToItemView.LinkedTo.Object(
        id = this.id,
        title = fieldParser.getObjectName(this),
        subtitle = getProperTypeName(id = typeUrl, types = objectTypes),
        type = typeUrl,
        layout = layout,
        icon = objectIcon(urlBuilder),
    )
}

fun List<ObjectWrapper.Basic>.toCreateFilterObjectView(
    ids: List<*>? = null,
    urlBuilder: UrlBuilder,
    objectTypes: List<ObjectWrapper.Type>,
    fieldParser: FieldParser
): List<CreateFilterView.Object> =
    this.map { obj ->
        CreateFilterView.Object(
            id = obj.id,
            typeName = getProperTypeName(
                id = obj.getProperType(),
                types = objectTypes
            ),
            name = fieldParser.getObjectName(obj),
            icon = obj.objectIcon(urlBuilder),
            isSelected = ids?.contains(obj.id) ?: false
        )
    }.sortedByDescending { it.isSelected }

private fun ObjectWrapper.Basic.getProperLayout() = layout ?: ObjectType.Layout.BASIC
fun ObjectWrapper.Basic.getProperType() = type.firstOrNull()

private fun getProperTypeName(id: Id?, types: List<ObjectWrapper.Type>) =
    types.find { it.id == id }?.name.orEmpty()

fun ObjectWrapper.Basic.mapFileObjectToView(fieldParser: FieldParser): CollectionView.ObjectView {
    val fileIcon = getFileObjectIcon(fieldParser)
    val defaultObjectView = DefaultObjectView(
        id = id,
        name = fieldParser.getObjectName(this),
        description = sizeInBytes?.toLong()?.readableFileSize().orEmpty(),
        layout = layout,
        icon = fileIcon,
        space = requireNotNull(spaceId)
    )
    return CollectionView.ObjectView(defaultObjectView)
}

private fun ObjectWrapper.Basic.getFileObjectIcon(fieldParser: FieldParser): ObjectIcon {
    return when (layout) {
        ObjectType.Layout.FILE, ObjectType.Layout.IMAGE ->
            ObjectIcon.File(
                mime = fileMimeType,
                fileName = fieldParser.getObjectName(this),
                extensions = fileExt
            )

        else -> ObjectIcon.None
    }
}

fun List<ObjectWrapper.Basic>.toSpaceMembers(): List<ObjectWrapper.SpaceMember> =
    mapNotNull { basic ->
        if (basic.map.isEmpty()) {
            null
        } else {
            ObjectWrapper.SpaceMember(basic.map)
        }
    }