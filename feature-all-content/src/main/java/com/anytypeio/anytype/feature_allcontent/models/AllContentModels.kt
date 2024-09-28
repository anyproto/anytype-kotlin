package com.anytypeio.anytype.feature_allcontent.models

import androidx.compose.runtime.Immutable
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.RelationKey
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView

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
        data class Today(override val id: String) : Group()
        data class Yesterday(override val id: String) : Group()
        data class Previous7Days(override val id: String) : Group()
        data class Previous14Days(override val id: String) : Group()
        data class Month(override val id: String, val title: String) : Group()
        data class MonthAndYear(override val id: String, val title: String) : Group()
    }

    data class Object(override val id: String, val obj: DefaultObjectView) : UiContentItem()
}


// MENU
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
//endregion