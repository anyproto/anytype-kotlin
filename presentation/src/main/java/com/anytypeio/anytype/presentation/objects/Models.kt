package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.MarketplaceObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.RelationKey
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.getTypeOfObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.core_models.ui.objectIcon

sealed class MenuSortsItem {
    data class Container(val sort: ObjectsListSort) : MenuSortsItem()
    data class Sort(val sort: ObjectsListSort) : MenuSortsItem()
    data object Spacer : MenuSortsItem()
    data class SortType(
        val sort: ObjectsListSort,
        val sortType: DVSortType,
        val isSelected: Boolean
    ) : MenuSortsItem()
}

sealed class ObjectsListSort {
    abstract val relationKey: RelationKey
    abstract val sortType: DVSortType
    abstract val canGroupByDate: Boolean
    abstract val isSelected: Boolean

    data class ByName(
        override val relationKey: RelationKey = RelationKey(Relations.NAME),
        override val sortType: DVSortType = DVSortType.ASC,
        override val canGroupByDate: Boolean = false,
        override val isSelected: Boolean = false
    ) : ObjectsListSort()

    data class ByDateUpdated(
        override val relationKey: RelationKey = RelationKey(Relations.LAST_MODIFIED_DATE),
        override val sortType: DVSortType = DVSortType.DESC,
        override val canGroupByDate: Boolean = true,
        override val isSelected: Boolean = false
    ) : ObjectsListSort()

    data class ByDateCreated(
        override val relationKey: RelationKey = RelationKey(Relations.CREATED_DATE),
        override val sortType: DVSortType = DVSortType.DESC,
        override val canGroupByDate: Boolean = true,
        override val isSelected: Boolean = false
    ) : ObjectsListSort()

    data class ByDateUsed(
        override val relationKey: RelationKey = RelationKey(Relations.LAST_USED_DATE),
        override val sortType: DVSortType = DVSortType.DESC,
        override val canGroupByDate: Boolean = false,
        override val isSelected: Boolean = false
    ) : ObjectsListSort()
}

fun ObjectsListSort.toMenuSortContainer(): MenuSortsItem.Container =
    MenuSortsItem.Container(sort = this)

fun ObjectsListSort.toSortOptions(): List<MenuSortsItem.Sort> = listOf(
    MenuSortsItem.Sort(
        sort = ObjectsListSort.ByDateUpdated(isSelected = this is ObjectsListSort.ByDateUpdated)
    ),
    MenuSortsItem.Sort(
        sort = ObjectsListSort.ByDateCreated(isSelected = this is ObjectsListSort.ByDateCreated)
    ),
    MenuSortsItem.Sort(
        sort = ObjectsListSort.ByName(isSelected = this is ObjectsListSort.ByName)
    )
)

fun ObjectsListSort.toSortTypeOptions(): List<MenuSortsItem.SortType> = listOf(
    MenuSortsItem.SortType(
        sort = this,
        sortType = DVSortType.ASC,
        isSelected = this.sortType == DVSortType.ASC
    ),
    MenuSortsItem.SortType(
        sort = this,
        sortType = DVSortType.DESC,
        isSelected = this.sortType == DVSortType.DESC
    )
)

fun ObjectsListSort.toDVSort(): DVSort {
    return when (this) {
        is ObjectsListSort.ByDateCreated -> DVSort(
            relationKey = relationKey.key,
            type = sortType,
            relationFormat = RelationFormat.DATE,
            includeTime = true,
        )

        is ObjectsListSort.ByDateUpdated -> DVSort(
            relationKey = relationKey.key,
            type = sortType,
            relationFormat = RelationFormat.DATE,
            includeTime = true,
        )

        is ObjectsListSort.ByName -> DVSort(
            relationKey = relationKey.key,
            type = sortType,
            relationFormat = RelationFormat.LONG_TEXT,
            includeTime = false
        )

        is ObjectsListSort.ByDateUsed -> DVSort(
            relationKey = relationKey.key,
            type = sortType,
            relationFormat = RelationFormat.DATE,
            includeTime = true,
        )
    }
}

sealed class UiObjectsListItem {

    abstract val id: String

    data class Loading(override val id: String) : UiObjectsListItem()

    data class Item(
        override val id: String,
        val obj: ObjectWrapper.Basic,
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

suspend fun ObjectWrapper.Basic.toUiObjectsListItem(
    space: SpaceId,
    urlBuilder: UrlBuilder,
    objectTypes: List<ObjectWrapper.Type>,
    fieldParser: FieldParser,
    isOwnerOrEditor: Boolean,
    storeOfObjectTypes: StoreOfObjectTypes
): UiObjectsListItem {
    val obj = this
    val typeUrl = obj.getProperType()
    val isProfile = typeUrl == MarketplaceObjectTypeIds.PROFILE
    val layout = obj.layout ?: ObjectType.Layout.BASIC
    return UiObjectsListItem.Item(
        id = obj.id,
        obj = obj,
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
        icon = obj.objectIcon(
            builder = urlBuilder,
            objType = storeOfObjectTypes.getTypeOfObject(obj)
        ),
        isPossibleToDelete = isOwnerOrEditor
    )
}