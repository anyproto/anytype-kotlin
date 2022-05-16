package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.linking.LinkToItemView
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.presentation.relations.RelationValueView
import com.anytypeio.anytype.presentation.sets.filter.CreateFilterView

fun List<ObjectWrapper.Basic>.toView(
    urlBuilder: UrlBuilder,
    objectTypes: List<ObjectType>
): List<DefaultObjectView> =
    this.map { obj ->
        val typeUrl = obj.getProperType()
        val layout = obj.getProperLayout()
        DefaultObjectView(
            id = obj.id,
            name = obj.getProperName(),
            type = typeUrl,
            typeName = getProperTypeName(url = typeUrl, types = objectTypes),
            layout = layout,
            icon = ObjectIcon.from(
                obj = obj,
                layout = layout,
                builder = urlBuilder
            )
        )
    }

fun List<ObjectWrapper.Basic>.toLinkToView(
    urlBuilder: UrlBuilder,
    objectTypes: List<ObjectType>
): List<LinkToItemView.Object> =
    this.mapIndexed { index, obj ->
        val typeUrl = obj.getProperType()
        val layout = obj.getProperLayout()
        LinkToItemView.Object(
            id = obj.id,
            title = obj.getProperName(),
            subtitle = getProperTypeName(url = typeUrl, types = objectTypes),
            type = typeUrl,
            layout = layout,
            icon = ObjectIcon.from(
                obj = obj,
                layout = layout,
                builder = urlBuilder
            ),
            position = index
        )
    }

fun List<ObjectWrapper.Basic>.toCreateFilterObjectView(
    ids: List<*>? = null,
    urlBuilder: UrlBuilder,
    objectTypes: List<ObjectType>
): List<CreateFilterView.Object> =
    this.map { obj ->
        CreateFilterView.Object(
            id = obj.id,
            typeName = getProperTypeName(
                url = obj.getProperType(),
                types = objectTypes
            ),
            name = obj.getProperName(),
            icon = ObjectIcon.from(
                obj = obj,
                layout = obj.getProperLayout(),
                builder = urlBuilder
            ),
            isSelected = ids?.contains(obj.id) ?: false
        )
    }

fun List<ObjectWrapper.Basic>.toRelationObjectValueView(
    ids: List<String>,
    urlBuilder: UrlBuilder,
    objectTypes: List<ObjectType>
): List<RelationValueView.Object> =
    this.mapNotNull { obj ->
        val typeUrl = obj.getProperType()
        val layout = obj.getProperLayout()
        if (obj.id !in ids) {
            if (obj.isDeleted == null || obj.isDeleted == false) {
                RelationValueView.Object.Default(
                    id = obj.id,
                    name = obj.getProperName(),
                    typeName = getProperTypeName(
                        url = typeUrl,
                        types = objectTypes
                    ),
                    type = typeUrl,
                    layout = layout,
                    icon = ObjectIcon.from(
                        obj = obj,
                        layout = layout,
                        builder = urlBuilder
                    ),
                    isSelected = false,
                    removable = false
                )
            } else {
                RelationValueView.Object.NonExistent(
                    id = obj.id,
                    isSelected = false,
                    removable = false
                )
            }
        } else {
            null
        }
    }

fun List<ObjectWrapper.Basic>.toRelationFileValueView(
    ids: List<String>,
    urlBuilder: UrlBuilder
): List<RelationValueView.File> =
    this.mapNotNull { obj ->
        val image = obj.getProperFileImage(urlBuilder)
        if (obj.id !in ids) {
            RelationValueView.File(
                id = obj.id,
                name = obj.getProperName(),
                ext = obj.getProperFileExt(),
                mime = obj.getProperFileMime(),
                image = image,
                isSelected = false
            )
        } else {
            null
        }
    }

private fun ObjectWrapper.Basic.getProperLayout() = layout ?: ObjectType.Layout.BASIC
private fun ObjectWrapper.Basic.getProperType() = type.firstOrNull()
private fun ObjectWrapper.Basic.getProperFileExt() = fileExt.orEmpty()
private fun ObjectWrapper.Basic.getProperFileMime() = fileMimeType.orEmpty()

private fun getProperTypeName(url: String?, types: List<ObjectType>) =
    types.find { it.url == url }?.name.orEmpty()

private fun ObjectWrapper.Basic.getProperFileImage(urlBuilder: UrlBuilder): String? =
    iconImage?.let { if (it.isBlank()) null else urlBuilder.thumbnail(it) }

fun ObjectWrapper.Basic.getProperName(): String {
    return if (layout == ObjectType.Layout.NOTE) {
        snippet?.replace("\n", " ")?.take(30).orEmpty()
    } else {
        name.orEmpty()
    }
}