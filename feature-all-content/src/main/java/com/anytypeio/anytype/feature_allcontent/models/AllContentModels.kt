package com.anytypeio.anytype.feature_allcontent.models

import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.RelationKey

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

enum class AllContentTab {
    OBJECTS, FILES, MEDIA, BOOKMARKS, TYPES, RELATIONS
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


//region VIEW STATES
sealed class AllContentTitleViewState {
    data object Hidden : AllContentTitleViewState()
    data object AllContent : AllContentTitleViewState()
    data object OnlyUnlinked : AllContentTitleViewState()
}

data class TopBarViewState(
    val titleState: AllContentTitleViewState,
    val menuButtonState: MenuButtonViewState
)

sealed class MenuButtonViewState {
    data object Hidden : MenuButtonViewState()
    data object Visible : MenuButtonViewState()
}

data class TabViewState(
    val tab: AllContentTab,
    val isSelected: Boolean
)

sealed class TabsViewState {
    data class Hidden(val hidden: Boolean) : TabsViewState()
    data class Visible(val tabs: List<TabViewState>) : TabsViewState()
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