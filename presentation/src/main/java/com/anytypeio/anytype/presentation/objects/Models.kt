package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.MarketplaceObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.RelationKey
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.presentation.mapper.objectIcon

sealed class AllContentSort {
    abstract val relationKey: RelationKey
    abstract val sortType: DVSortType
    abstract val canGroupByDate: Boolean
    abstract val isSelected: Boolean

    data class ByName(
        override val relationKey: RelationKey = RelationKey(Relations.NAME),
        override val sortType: DVSortType = DVSortType.ASC,
        override val canGroupByDate: Boolean = false,
        override val isSelected: Boolean = false
    ) : AllContentSort()

    data class ByDateUpdated(
        override val relationKey: RelationKey = RelationKey(Relations.LAST_MODIFIED_DATE),
        override val sortType: DVSortType = DVSortType.DESC,
        override val canGroupByDate: Boolean = true,
        override val isSelected: Boolean = false
    ) : AllContentSort()

    data class ByDateCreated(
        override val relationKey: RelationKey = RelationKey(Relations.CREATED_DATE),
        override val sortType: DVSortType = DVSortType.DESC,
        override val canGroupByDate: Boolean = true,
        override val isSelected: Boolean = false
    ) : AllContentSort()

    data class ByDateUsed(
        override val relationKey: RelationKey = RelationKey(Relations.LAST_USED_DATE),
        override val sortType: DVSortType = DVSortType.DESC,
        override val canGroupByDate: Boolean = false,
        override val isSelected: Boolean = false
    ) : AllContentSort()
}

sealed class UiObjectsListItem {

    abstract val id: String

    data class Loading(override val id: String) : UiObjectsListItem()

    data class Item(
        override val id: String,
        val name: String,
        val space: SpaceId,
        val type: String? = null,
        val typeName: String? = null,
        val createdBy: String? = null,
        val layout: ObjectType.Layout? = null,
        val icon: ObjectIcon = ObjectIcon.None,
        val isPossibleToDelete: Boolean = false
    ) : UiObjectsListItem()
}

fun ObjectWrapper.Basic.toUiObjectsListItem(
    space: SpaceId,
    urlBuilder: UrlBuilder,
    objectTypes: List<ObjectWrapper.Type>,
    fieldParser: FieldParser,
    isOwnerOrEditor: Boolean
): UiObjectsListItem {
    val obj = this
    val typeUrl = obj.getProperType()
    val isProfile = typeUrl == MarketplaceObjectTypeIds.PROFILE
    val layout = obj.layout ?: ObjectType.Layout.BASIC
    return UiObjectsListItem.Item(
        id = obj.id,
        space = space,
        name = fieldParser.getObjectName(obj),
        type = typeUrl,
        typeName = objectTypes.firstOrNull { type ->
            if (isProfile) {
                type.uniqueKey == ObjectTypeUniqueKeys.PROFILE
            } else {
                type.id == typeUrl
            }
        }?.name,
        layout = layout,
        icon = obj.objectIcon(builder = urlBuilder),
        isPossibleToDelete = isOwnerOrEditor
    )
}

fun ObjectWrapper.Basic.toUiObjectsListItem(
    space: SpaceId,
    urlBuilder: UrlBuilder,
    typeName: String?,
    fieldParser: FieldParser,
    isOwnerOrEditor: Boolean
): UiObjectsListItem {
    val obj = this
    val typeUrl = obj.getProperType()
    return UiObjectsListItem.Item(
        id = obj.id,
        space = space,
        name = fieldParser.getObjectName(obj),
        type = typeUrl,
        typeName = typeName,
        layout = obj.layout,
        icon = obj.objectIcon(builder = urlBuilder),
        isPossibleToDelete = isOwnerOrEditor
    )
}