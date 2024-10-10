package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Marketplace
import com.anytypeio.anytype.core_models.MarketplaceObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.Relations.SOURCE_OBJECT
import com.anytypeio.anytype.core_models.ext.DateParser
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.core_utils.ext.readableFileSize
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.presentation.extension.getProperObjectName
import com.anytypeio.anytype.presentation.library.LibraryView
import com.anytypeio.anytype.presentation.linking.LinkToItemView
import com.anytypeio.anytype.presentation.mapper.objectIcon
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.presentation.relations.RelationValueView
import com.anytypeio.anytype.presentation.sets.filter.CreateFilterView
import com.anytypeio.anytype.presentation.widgets.collection.CollectionView

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
            icon = obj.objectIcon(urlBuilder),
            space = requireNotNull(obj.spaceId)
        )
    }

fun List<ObjectWrapper.Basic>.toViews(
    urlBuilder: UrlBuilder,
    objectTypes: List<ObjectWrapper.Type>
): List<DefaultObjectView> = map { obj ->
    obj.toView(urlBuilder, objectTypes)
}

fun ObjectWrapper.Basic.toView(
    urlBuilder: UrlBuilder,
    objectTypes: List<ObjectWrapper.Type>
): DefaultObjectView {
    val obj = this
    val typeUrl = obj.getProperType()
    val isProfile = typeUrl == MarketplaceObjectTypeIds.PROFILE
    val layout = obj.getProperLayout()
    return DefaultObjectView(
        id = obj.id,
        name = obj.getProperName(),
        description = obj.description,
        type = typeUrl,
        typeName = objectTypes.firstOrNull { type ->
            if (isProfile) {
                type.uniqueKey == ObjectTypeUniqueKeys.PROFILE
            } else {
                type.id == typeUrl
            }
        }?.name,
        layout = layout,
        icon = obj.objectIcon(urlBuilder),
        lastModifiedDate = DateParser.parseInMillis(obj.lastModifiedDate) ?: 0L,
        lastOpenedDate = DateParser.parseInMillis(obj.lastOpenedDate) ?: 0L,
        isFavorite = obj.isFavorite ?: false,
        space = requireNotNull(obj.spaceId)
    )
}

fun List<ObjectWrapper.Basic>.toLibraryViews(
    urlBuilder: UrlBuilder
): List<LibraryView> = map { obj ->
    val space = obj.getValue<Id?>(Relations.SPACE_ID)
    when (obj.layout) {
        ObjectType.Layout.OBJECT_TYPE -> {
            if (space == Marketplace.MARKETPLACE_SPACE_ID) {
                LibraryView.LibraryTypeView(
                    id = obj.id,
                    name = obj.name.orEmpty(),
                    icon = obj.objectIcon(urlBuilder),
                    uniqueKey = obj.uniqueKey
                )
            } else {
                LibraryView.MyTypeView(
                    id = obj.id,
                    name = obj.name.orEmpty(),
                    icon = obj.objectIcon(urlBuilder),
                    sourceObject = obj.map[SOURCE_OBJECT]?.toString(),
                    uniqueKey = obj.uniqueKey,
                    readOnly = obj.restrictions.contains(ObjectRestriction.DELETE),
                    editable = !obj.restrictions.contains(ObjectRestriction.DETAILS)
                )
            }
        }
        ObjectType.Layout.RELATION -> {
            val relation = ObjectWrapper.Relation(obj.map)
            if (space == Marketplace.MARKETPLACE_SPACE_ID) {
                LibraryView.LibraryRelationView(
                    id = obj.id,
                    name = obj.name.orEmpty(),
                    format = relation.format
                )
            } else {
                LibraryView.MyRelationView(
                    id = obj.id,
                    name = obj.name.orEmpty(),
                    format = relation.format,
                    sourceObject = obj.map[SOURCE_OBJECT]?.toString(),
                    readOnly = obj.restrictions.contains(ObjectRestriction.DELETE),
                    editable = !obj.restrictions.contains(ObjectRestriction.DETAILS)
                )
            }
        }
        else -> LibraryView.UnknownView(
            id = obj.id,
            name = obj.name.orEmpty()
        )
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
            icon = obj.objectIcon(urlBuilder),
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
        icon = objectIcon(urlBuilder),
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
            icon = obj.objectIcon(urlBuilder),
            isSelected = ids?.contains(obj.id) ?: false
        )
    }.sortedByDescending { it.isSelected }

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
                    space = requireNotNull(obj.spaceId),
                    name = obj.getProperName(),
                    typeName = getProperTypeName(
                        id = typeUrl,
                        types = objectTypes
                    ),
                    type = typeUrl,
                    layout = layout,
                    icon = obj.objectIcon(urlBuilder),
                    isSelected = false,
                    removable = false
                )
            } else {
                RelationValueView.Object.NonExistent(
                    id = obj.id,
                    space = requireNotNull(obj.spaceId),
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
                space = requireNotNull(obj.spaceId),
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
fun ObjectWrapper.Basic.getProperType() = type.firstOrNull()
private fun ObjectWrapper.Basic.getProperFileExt() = fileExt.orEmpty()
private fun ObjectWrapper.Basic.getProperFileMime() = fileMimeType.orEmpty()

private fun getProperTypeName(id: Id?, types: List<ObjectWrapper.Type>) =
    types.find { it.id == id }?.name.orEmpty()

private fun ObjectWrapper.Basic.getProperFileImage(urlBuilder: UrlBuilder): String? =
    iconImage?.let { if (it.isBlank()) null else urlBuilder.thumbnail(it) }

fun ObjectWrapper.Basic.getProperName(): String {
    return getProperObjectName().orEmpty()
}

fun ObjectWrapper.Basic.mapFileObjectToView(): CollectionView.ObjectView {
    val fileIcon = getFileObjectIcon()
    val defaultObjectView = DefaultObjectView(
        id = id,
        name = getProperName(),
        description = sizeInBytes?.toLong()?.readableFileSize().orEmpty(),
        layout = layout,
        icon = fileIcon,
        space = requireNotNull(spaceId)
    )
    return CollectionView.ObjectView(defaultObjectView)
}

private fun ObjectWrapper.Basic.getFileObjectIcon(): ObjectIcon {
    return when (layout) {
        ObjectType.Layout.FILE, ObjectType.Layout.IMAGE ->
            ObjectIcon.File(
                mime = fileMimeType,
                fileName = getProperName(),
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

fun List<ObjectWrapper.Basic>.toSpaceView() =
    if (isNotEmpty()) {
        val spaceMap = first().map
        if (spaceMap.isEmpty()) {
            null
        } else {
            ObjectWrapper.SpaceView(spaceMap)
        }
    } else {
        null
    }