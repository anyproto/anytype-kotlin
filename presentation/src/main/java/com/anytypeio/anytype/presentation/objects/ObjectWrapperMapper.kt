package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.MarketplaceObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations.SOURCE_OBJECT
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.library.LibraryView
import com.anytypeio.anytype.presentation.linking.LinkToItemView
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.presentation.relations.RelationValueView
import com.anytypeio.anytype.presentation.sets.filter.CreateFilterView
import timber.log.Timber

@Deprecated("To be deleted")
fun List<ObjectWrapper.Basic>.toView(
    urlBuilder: UrlBuilder,
    objectTypes: List<ObjectWrapper.Type>
): List<DefaultObjectView> =
    this.map { obj ->
        val typeUrl = obj.getProperType()
        val layout = obj.getProperLayout()
        DefaultObjectView(
            id = obj.id,
            name = obj.getProperName(),
            type = typeUrl,
            typeName = getProperTypeName(
                id = typeUrl,
                types = objectTypes
            ),
            description = obj.description,
            layout = layout,
            icon = ObjectIcon.from(
                obj = obj,
                layout = layout,
                builder = urlBuilder
            )
        )
    }

fun List<ObjectWrapper.Basic>.toViews(
    urlBuilder: UrlBuilder,
    objectTypes: List<ObjectWrapper.Type>
): List<DefaultObjectView> = map { obj ->
    val typeUrl = obj.getProperType()
    val layout = obj.getProperLayout()
    DefaultObjectView(
        id = obj.id,
        name = obj.getProperName(),
        description = obj.description,
        type = typeUrl,
        typeName = objectTypes.firstOrNull { it.id == typeUrl }?.name,
        layout = layout,
        icon = ObjectIcon.from(
            obj = obj,
            layout = layout,
            builder = urlBuilder
        )
    )
}

fun List<ObjectWrapper.Basic>.toLibraryViews(
    urlBuilder: UrlBuilder,
): List<LibraryView> = map { obj ->
    when (obj.getProperType()) {
        MarketplaceObjectTypeIds.OBJECT_TYPE -> {
            LibraryView.LibraryTypeView(
                id = obj.id,
                name = obj.name ?: "",
                icon = ObjectIcon.from(
                    obj = obj,
                    layout = obj.getProperLayout(),
                    builder = urlBuilder
                ),
            )
        }
        ObjectTypeIds.OBJECT_TYPE -> {
            LibraryView.MyTypeView(
                id = obj.id,
                name = obj.name ?: "",
                icon = ObjectIcon.from(
                    obj = obj,
                    layout = obj.getProperLayout(),
                    builder = urlBuilder
                ),
                sourceObject = obj.map[SOURCE_OBJECT]?.toString(),
                readOnly = obj.restrictions.contains(ObjectRestriction.DELETE),
                editable = !obj.restrictions.contains(ObjectRestriction.DETAILS)
            )
        }
        ObjectTypeIds.RELATION -> {
            val relation = ObjectWrapper.Relation(obj.map)
            LibraryView.MyRelationView(
                id = obj.id,
                name = obj.name ?: "",
                format = relation.format,
                sourceObject = obj.map[SOURCE_OBJECT]?.toString(),
                readOnly = obj.restrictions.contains(ObjectRestriction.DELETE),
                editable = !obj.restrictions.contains(ObjectRestriction.DETAILS)
            )
        }
        MarketplaceObjectTypeIds.RELATION -> {
            val relation = ObjectWrapper.Relation(obj.map)
            LibraryView.LibraryRelationView(
                id = obj.id,
                name = obj.name ?: "",
                format = relation.format
            )
        }
        else -> {
            Timber.e("Unknown type: ${obj.getProperType()}")
            LibraryView.UnknownView()
        }
    }
}

fun List<ObjectWrapper.Basic>.toLinkToView(
    urlBuilder: UrlBuilder,
    objectTypes: List<ObjectWrapper.Type>
): List<LinkToItemView.Object> =
    this.mapIndexed { index, obj ->
        val typeUrl = obj.getProperType()
        val layout = obj.getProperLayout()
        LinkToItemView.Object(
            id = obj.id,
            title = obj.getProperName(),
            subtitle = getProperTypeName(id = typeUrl, types = objectTypes),
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

fun ObjectWrapper.Basic.toLinkToObjectView(
    urlBuilder: UrlBuilder,
    objectTypes: List<ObjectWrapper.Type>
): LinkToItemView.LinkedTo.Object {
    val typeUrl = this.getProperType()
    val layout = this.getProperLayout()
    return LinkToItemView.LinkedTo.Object(
        id = this.id,
        title = this.getProperName(),
        subtitle = getProperTypeName(id = typeUrl, types = objectTypes),
        type = typeUrl,
        layout = layout,
        icon = ObjectIcon.from(
            obj = this,
            layout = layout,
            builder = urlBuilder
        )
    )
}

fun List<ObjectWrapper.Basic>.toCreateFilterObjectView(
    ids: List<*>? = null,
    urlBuilder: UrlBuilder,
    objectTypes: List<ObjectWrapper.Type>
): List<CreateFilterView.Object> =
    this.map { obj ->
        CreateFilterView.Object(
            id = obj.id,
            typeName = getProperTypeName(
                id = obj.getProperType(),
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
    excluded: List<Id>,
    urlBuilder: UrlBuilder,
    objectTypes: List<ObjectWrapper.Type>
): List<RelationValueView.Object> =
    this.mapNotNull { obj ->
        val typeUrl = obj.getProperType()
        val layout = obj.getProperLayout()
        if (obj.id !in excluded) {
            if (obj.isDeleted == null || obj.isDeleted == false) {
                RelationValueView.Object.Default(
                    id = obj.id,
                    name = obj.getProperName(),
                    typeName = getProperTypeName(
                        id = typeUrl,
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

private fun getProperTypeName(id: Id?, types: List<ObjectWrapper.Type>) =
    types.find { it.id == id }?.name.orEmpty()

private fun ObjectWrapper.Basic.getProperFileImage(urlBuilder: UrlBuilder): String? =
    iconImage?.let { if (it.isBlank()) null else urlBuilder.thumbnail(it) }

fun ObjectWrapper.Basic.getProperName(): String {
    return if (layout == ObjectType.Layout.NOTE) {
        snippet?.replace("\n", " ")?.take(30).orEmpty()
    } else {
        name.orEmpty()
    }
}