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
sealed class AllContentState {
    data object Initial : AllContentState()
    data class Default(
        val activeTab: AllContentTab,
        val activeMode: AllContentMode,
        val activeSort: AllContentSort,
        val filter: String,
        val limit: Int
    ) : AllContentState()
}

@Immutable
enum class AllContentTab {
    PAGES, LISTS, MEDIA, BOOKMARKS, FILES, TYPES, RELATIONS
}

sealed class AllContentMode {
    data object AllContent : AllContentMode()
    data object Unlinked : AllContentMode()
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

//MENU BUTTON
sealed class MenuButtonViewState {
    data object Hidden : MenuButtonViewState()
    data object Visible : MenuButtonViewState()
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

    data object Hidden : UiContentState()

    data object Loading : UiContentState()

    data class Error(
        val message: String,
    ) : UiContentState()

    @Immutable
    data class Content(
        val items: List<UiContentItem>,
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

sealed class UiMenuState {
    data object Hidden : UiMenuState()

    @Immutable
    data class Content(
        val sorts: List<MenuSortsItem>,
        val mode: List<AllContentMenuMode>,
    ) : UiMenuState()
}

sealed class MenuSortsItem {
    abstract val id: String

    data class Container(override val id: String = CONTAINER_ID, val sort: AllContentSort) :
        MenuSortsItem()

    data class Sort(override val id: String, val sort: AllContentSort) : MenuSortsItem()

    data class Spacer(override val id: String = SPACER_ID) : MenuSortsItem()

    data class SortType(
        val sort: AllContentSort,
        val sortType: DVSortType,
        val isSelected: Boolean
    ) : MenuSortsItem() {
        override val id: String
            get() = sortType.name
    }

    companion object {
        const val CONTAINER_ID = "container_id"
        const val SPACER_ID = "spacer_id"
    }
}
//endregion

//region MAPPING
fun AllContentState.Default.toMenuMode(): AllContentMenuMode {
    return when (activeMode) {
        AllContentMode.AllContent -> AllContentMenuMode.AllContent(isSelected = true)
        AllContentMode.Unlinked -> AllContentMenuMode.Unlinked(isSelected = true)
    }
}

fun AllContentMode.view(): UiTitleState {
    return when (this) {
        AllContentMode.AllContent -> UiTitleState.AllContent
        AllContentMode.Unlinked -> UiTitleState.OnlyUnlinked
    }
}

fun Key?.mapRelationKeyToSort(): AllContentSort {
    return when (this) {
        Relations.CREATED_DATE -> AllContentSort.ByDateCreated()
        Relations.LAST_OPENED_DATE -> AllContentSort.ByDateUpdated()
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
        lastModifiedDate = DateParser.parseInMillis(obj.lastModifiedDate) ?: 0L,
        createdDate = DateParser.parse(obj.getValue(Relations.CREATED_DATE)) ?: 0L
    )
}
//endregion