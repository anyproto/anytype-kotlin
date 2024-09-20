package com.anytypeio.anytype.feature_allcontent.models

import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.RelationKey

enum class AllContentTab {
    OBJECTS, FILES, MEDIA, BOOKMARKS, TYPES, RELATIONS
}

sealed class AllContentMode {
    data object AllContent : AllContentMode()
    data object Unlinked : AllContentMode()
}

sealed class AllContentSort {
    abstract val relationKey: RelationKey
    abstract val sortType: DVSortType
    abstract val canGroupByDate: Boolean

    data class ByName(
        override val relationKey: RelationKey = RelationKey(Relations.NAME),
        override val sortType: DVSortType = DVSortType.ASC,
        override val canGroupByDate: Boolean = false
    ) : AllContentSort()

    data class ByDateUpdated(
        override val relationKey: RelationKey = RelationKey(Relations.LAST_MODIFIED_DATE),
        override val sortType: DVSortType = DVSortType.DESC,
        override val canGroupByDate: Boolean = true
    ) : AllContentSort()

    data class ByDateCreated(
        override val relationKey: RelationKey = RelationKey(Relations.CREATED_DATE),
        override val sortType: DVSortType = DVSortType.DESC,
        override val canGroupByDate: Boolean = true
    ) : AllContentSort()
}