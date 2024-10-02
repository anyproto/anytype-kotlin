package com.anytypeio.anytype.feature_allcontent.models

import androidx.compose.runtime.Immutable
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.MarketplaceObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.ext.DateParser
import com.anytypeio.anytype.core_models.primitives.RelationKey
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.feature_allcontent.presentation.AllContentViewModel.Companion.DEFAULT_INITIAL_SORT
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.objects.getProperName
import com.anytypeio.anytype.presentation.objects.getProperType

//region STATE
@Immutable
enum class AllContentTab {
    PAGES, LISTS, MEDIA, BOOKMARKS, FILES, TYPES
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
}
//endregion

//region VIEW STATES

//TITLE
sealed class UiTitleState {
    data object Hidden : UiTitleState()
    data object AllContent : UiTitleState()
    data object OnlyUnlinked : UiTitleState()
}

// TABS
@Immutable
sealed class UiTabsState {
    data object Hidden : UiTabsState()

    @Immutable
    data class Default(
        val tabs: List<AllContentTab>,
        val selectedTab: AllContentTab
    ) : UiTabsState()
}

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
        override val id: String,
        val name: String,
        val space: SpaceId,
        val type: String? = null,
        val typeName: String? = null,
        val description: String? = null,
        val layout: ObjectType.Layout? = null,
        val icon: ObjectIcon = ObjectIcon.None,
        val lastModifiedDate: Long = 0L,
        val createdDate: Long = 0L,
    ) : UiContentItem()

    companion object {
        const val TODAY_ID = "TodayId"
        const val YESTERDAY_ID = "YesterdayId"
        const val PREVIOUS_7_DAYS_ID = "Previous7DaysId"
        const val PREVIOUS_14_DAYS_ID = "Previous14DaysId"
    }
}

// MENU
@Immutable
sealed class UiMenuState{

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


sealed class MenuSortsItem {
    data class Container(val sort: AllContentSort) : MenuSortsItem()
    data class Sort(val sort: AllContentSort) : MenuSortsItem()
    data object Spacer : MenuSortsItem()
    data class SortType(
        val sort: AllContentSort,
        val sortType: DVSortType,
        val isSelected: Boolean
    ) : MenuSortsItem()
}
//endregion

//region MAPPING
fun Key?.mapRelationKeyToSort(): AllContentSort {
    return when (this) {
        Relations.CREATED_DATE -> AllContentSort.ByDateCreated()
        Relations.LAST_MODIFIED_DATE -> AllContentSort.ByDateUpdated()
        Relations.NAME -> AllContentSort.ByName()
        else -> DEFAULT_INITIAL_SORT
    }
}

fun List<ObjectWrapper.Basic>.toUiContentItems(
    space: SpaceId,
    urlBuilder: UrlBuilder,
    objectTypes: List<ObjectWrapper.Type>
): List<UiContentItem.Item> {
    return map { it.toAllContentItem(space, urlBuilder, objectTypes) }
}

fun ObjectWrapper.Basic.toAllContentItem(
    space: SpaceId,
    urlBuilder: UrlBuilder,
    objectTypes: List<ObjectWrapper.Type>
): UiContentItem.Item {
    val obj = this
    val typeUrl = obj.getProperType()
    val isProfile = typeUrl == MarketplaceObjectTypeIds.PROFILE
    val layout = layout ?: ObjectType.Layout.BASIC
    return UiContentItem.Item(
        id = obj.id,
        space = space,
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
        icon = ObjectIcon.from(
            obj = obj,
            layout = layout,
            builder = urlBuilder
        ),
        lastModifiedDate = DateParser.parse(obj.getValue(Relations.LAST_MODIFIED_DATE)) ?: 0L,
        createdDate = DateParser.parse(obj.getValue(Relations.CREATED_DATE)) ?: 0L
    )
}
//endregion