package com.anytypeio.anytype.feature_allcontent.models

import androidx.compose.runtime.Immutable
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.MarketplaceObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.Relations.SOURCE_OBJECT
import com.anytypeio.anytype.core_models.ext.DateParser
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.domain.all_content.RestoreAllContentState
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.getTypeOfObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.feature_allcontent.presentation.AllContentViewModel.Companion.DEFAULT_INITIAL_TAB
import com.anytypeio.anytype.core_models.ui.objectIcon
import com.anytypeio.anytype.presentation.objects.MenuSortsItem
import com.anytypeio.anytype.presentation.objects.ObjectsListSort
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.presentation.objects.getDescriptionOrSnippet
import com.anytypeio.anytype.presentation.objects.getProperType

//region STATE
@Immutable
enum class AllContentTab {
    PAGES, LISTS, MEDIA, BOOKMARKS, FILES
}

sealed class AllContentMenuMode {
    abstract val isSelected: Boolean

    data class AllContent(
        override val isSelected: Boolean = false
    ) : AllContentMenuMode()

    data class Unlinked(
        override val isSelected: Boolean = false
    ) : AllContentMenuMode()
}
//endregion

//region VIEW STATES

//TITLE
sealed class UiTitleState {
    data object AllContent : UiTitleState()
    data object OnlyUnlinked : UiTitleState()
}

// TABS
@Immutable
data class UiTabsState(
    val tabs: List<AllContentTab> = AllContentTab.entries,
    val selectedTab: AllContentTab = DEFAULT_INITIAL_TAB
)

// CONTENT
sealed class UiContentState {
    data class Idle(val scrollToTop: Boolean = false) : UiContentState()
    data object InitLoading : UiContentState()
    data object Paging : UiContentState()
    data object Empty : UiContentState()
    data class Error(
        val message: String,
    ) : UiContentState()
}

sealed class UiItemsState{
    data object Empty : UiItemsState()
    data class Content(val items: List<UiContentItem>) : UiItemsState()
}

// ITEMS
sealed class UiContentItem {
    abstract val id: String

    sealed class Group : UiContentItem() {
        data class Today(override val id: String = TODAY_ID) : Group()
        data class Yesterday(override val id: String = YESTERDAY_ID) : Group()
        data class Previous7Days(override val id: String = PREVIOUS_7_DAYS_ID) : Group()
        data class Previous14Days(override val id: String = PREVIOUS_14_DAYS_ID) : Group()
        data class Month(override val id: String, val title: String) : Group()
        data class MonthAndYear(override val id: String, val title: String) : Group()
    }

    data class Item(
        override val id: Id,
        val obj: ObjectWrapper.Basic,
        val space: SpaceId,
        val name: String = "",
        val description: String? = null,
        val type: Id? = null,
        val typeName: String? = null,
        val layout: ObjectType.Layout? = null,
        val icon: ObjectIcon = ObjectIcon.None,
        val lastModifiedDate: Long = 0L,
        val createdDate: Long = 0L,
        val isPossibleToDelete: Boolean = false
    ) : UiContentItem()

    data class Type(
        override val id: Id,
        val name: String,
        val icon: ObjectIcon? = null,
        val sourceObject: Id? = null,
        val uniqueKey: Key? = null,
        val readOnly: Boolean = true,
        val editable: Boolean = true
    ) : UiContentItem()

    data class Relation(
        override val id: Id,
        val name: String,
        val format: RelationFormat,
        val sourceObject: Id? = null,
        val readOnly: Boolean = true,
        val editable: Boolean = true,
    ) : UiContentItem()

    data object NewRelation : UiContentItem() {
        override val id: String = "NewRelation"
    }

    data object NewType : UiContentItem() {
        override val id: String = "NewType"
    }

    data object UnlinkedDescription : UiContentItem() {
        override val id: String = "UnlinkedDescription"
    }

    companion object {
        const val TODAY_ID = "TodayId"
        const val YESTERDAY_ID = "YesterdayId"
        const val PREVIOUS_7_DAYS_ID = "Previous7DaysId"
        const val PREVIOUS_14_DAYS_ID = "Previous14DaysId"
    }
}

// MENU
@Immutable
sealed class UiMenuState {

    data object Hidden : UiMenuState()

    @Immutable
    data class Visible(
        val mode: List<AllContentMenuMode>,
        val container: MenuSortsItem.Container,
        val sorts: List<MenuSortsItem.Sort>,
        val types: List<MenuSortsItem.SortType>,
        val showBin: Boolean = true
    ) : UiMenuState()
}
//endregion

//region BOTTOM_MENU
data class AllContentBottomMenu(val isOwnerOrEditor: Boolean = true)
//endregion

//region SNACKBAR
sealed class UiSnackbarState {
    data object Hidden : UiSnackbarState()
    data class Visible(val message: String, val objId: Id) : UiSnackbarState()
}
//endregion

//region MAPPING

fun RestoreAllContentState.Response.Success.mapToSort(): ObjectsListSort {
    val sortType = if (isAsc) DVSortType.ASC else DVSortType.DESC
    return when (activeSort) {
        Relations.CREATED_DATE -> ObjectsListSort.ByDateCreated(sortType = sortType)
        Relations.LAST_MODIFIED_DATE -> ObjectsListSort.ByDateUpdated(sortType = sortType)
        Relations.NAME -> ObjectsListSort.ByName(sortType = sortType)
        Relations.LAST_USED_DATE -> ObjectsListSort.ByDateUsed(sortType = sortType)
        else -> ObjectsListSort.ByName(sortType = DVSortType.ASC)
    }
}

suspend fun ObjectWrapper.Basic.toAllContentItem(
    space: SpaceId,
    urlBuilder: UrlBuilder,
    objectTypes: List<ObjectWrapper.Type>,
    isOwnerOrEditor: Boolean,
    fieldParser: FieldParser,
    storeOfObjectTypes: StoreOfObjectTypes
): UiContentItem.Item {
    val obj = this
    val typeUrl = obj.getProperType()
    val isProfile = typeUrl == MarketplaceObjectTypeIds.PROFILE
    val layout = obj.layout ?: ObjectType.Layout.BASIC
    val isType = obj.layout == ObjectType.Layout.OBJECT_TYPE
    return UiContentItem.Item(
        id = obj.id,
        space = space,
        obj = obj,
        name = fieldParser.getObjectNameOrPluralsForTypes(obj),
        description = getDescriptionOrSnippet(),
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
        lastModifiedDate = DateParser.parse(obj.getValue(Relations.LAST_MODIFIED_DATE)) ?: 0L,
        createdDate = DateParser.parse(obj.getValue(Relations.CREATED_DATE)) ?: 0L,
        isPossibleToDelete = isOwnerOrEditor && !isType
    )
}

fun List<ObjectWrapper.Type>.toUiContentTypes(
    urlBuilder: UrlBuilder,
    isOwnerOrEditor: Boolean
): List<UiContentItem.Type> {
    return map { it.toAllContentType(urlBuilder, isOwnerOrEditor) }
}

fun ObjectWrapper.Type.toAllContentType(
    urlBuilder: UrlBuilder,
    isOwnerOrEditor: Boolean
): UiContentItem.Type {
    val obj = this
    return UiContentItem.Type(
        id = obj.id,
        name = obj.name.orEmpty(),
        icon = obj.objectIcon(),
        sourceObject = obj.map[SOURCE_OBJECT]?.toString(),
        uniqueKey = obj.uniqueKey,
        readOnly = obj.restrictions.contains(ObjectRestriction.DELETE) || !isOwnerOrEditor,
        editable = !obj.restrictions.contains(ObjectRestriction.DETAILS)
    )
}

fun List<ObjectWrapper.Basic>.toUiContentRelations(isOwnerOrEditor: Boolean): List<UiContentItem.Relation> {
    return map { it.toAllContentRelation(isOwnerOrEditor) }
}

fun ObjectWrapper.Basic.toAllContentRelation(
    isOwnerOrEditor: Boolean
): UiContentItem.Relation {
    val relation = ObjectWrapper.Relation(map)
    val obj = this
    return UiContentItem.Relation(
        id = relation.id,
        name = obj.name.orEmpty(),
        format = relation.format,
        sourceObject = map[SOURCE_OBJECT]?.toString(),
        readOnly = obj.restrictions.contains(ObjectRestriction.DELETE) || !isOwnerOrEditor,
        editable = !obj.restrictions.contains(ObjectRestriction.DETAILS)
    )
}

fun ObjectsListSort.toAnalyticsSortType(): Pair<String, String> {
    return when (this) {
        is ObjectsListSort.ByName -> "Name" to sortType.toAnalyticsSortType()
        is ObjectsListSort.ByDateUpdated -> "Updated" to sortType.toAnalyticsSortType()
        is ObjectsListSort.ByDateCreated -> "Created" to sortType.toAnalyticsSortType()
        is ObjectsListSort.ByDateUsed -> "Used" to sortType.toAnalyticsSortType()
    }
}

fun DVSortType.toAnalyticsSortType(): String {
    return when (this) {
        DVSortType.ASC -> "Asc"
        DVSortType.DESC -> "Desc"
        DVSortType.CUSTOM -> "Custom"
    }
}

fun AllContentTab.toAnalyticsTabType(): String {
    return when (this) {
        AllContentTab.PAGES -> "Pages"
        AllContentTab.LISTS -> "Lists"
        AllContentTab.MEDIA -> "Media"
        AllContentTab.BOOKMARKS -> "Bookmarks"
        AllContentTab.FILES -> "Files"
    }
}

fun AllContentMenuMode.toAnalyticsModeType(): String {
    return when (this) {
        is AllContentMenuMode.AllContent -> "All"
        is AllContentMenuMode.Unlinked -> "Unlinked"
    }
}
//endregion