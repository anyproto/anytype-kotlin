package com.anytypeio.anytype.presentation.widgets.collection

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.search.Subscriptions

sealed class Subscription(
    val id: Id,
    val keys: List<String>,
    val sorts: List<DVSort>,
    val limit: Int,
    val filters: (List<Id>) -> List<DVFilter>
) {
    object Recent : Subscription(
        Subscriptions.SUBSCRIPTION_RECENT,
        SUBSCRIPTION_DEFAULT_KEYS + Relations.LAST_MODIFIED_DATE,
        ObjectSearchConstants.sortTabRecent,
        ObjectSearchConstants.limitTabRecent,
        filters = { spaces -> ObjectSearchConstants.filterTabRecent(spaces) }
    )

    object RecentLocal : Subscription(
        Subscriptions.SUBSCRIPTION_RECENT_LOCAL,
        SUBSCRIPTION_DEFAULT_KEYS + Relations.LAST_OPENED_DATE,
        ObjectSearchConstants.sortTabRecentLocal,
        ObjectSearchConstants.limitTabRecent,
        filters = { spaces -> ObjectSearchConstants.filterTabRecentLocal(spaces) }
    )

    object Bin : Subscription(
        Subscriptions.SUBSCRIPTION_ARCHIVED,
        SUBSCRIPTION_DEFAULT_KEYS,
        ObjectSearchConstants.sortTabArchive,
        0,
        filters = { spaces -> ObjectSearchConstants.filterTabArchive(spaces) }
    )

    object Sets : Subscription(
        Subscriptions.SUBSCRIPTION_SETS,
        SUBSCRIPTION_DEFAULT_KEYS,
        ObjectSearchConstants.sortTabSets,
        0,
        filters = { spaces -> ObjectSearchConstants.filterTabSets(spaces) }
    )

    object Collections : Subscription(
        Subscriptions.SUBSCRIPTION_COLLECTIONS,
        SUBSCRIPTION_DEFAULT_KEYS,
        ObjectSearchConstants.sortTabSets,
        0,
        filters = { spaces -> ObjectSearchConstants.collectionFilters(spaces) }
    )

    object Favorites : Subscription(
        Subscriptions.SUBSCRIPTION_FAVORITES,
        SUBSCRIPTION_DEFAULT_KEYS,
        emptyList(),
        0,
        filters = { spaces -> ObjectSearchConstants.filterTabFavorites(spaces) }
    )

    object Files : Subscription(
        id = Subscriptions.SUBSCRIPTION_FILES,
        keys = SUBSCRIPTION_DEFAULT_KEYS + Relations.SIZE_IN_BYTES + Relations.FILE_MIME_TYPE + Relations.FILE_EXT + Relations.FILE_SYNC_STATUS,
        sorts = listOf(
            DVSort(
                relationKey = Relations.SIZE_IN_BYTES,
                type = DVSortType.DESC
            )
        ),
        limit = 0,
        filters = { spaces -> ObjectSearchConstants.filesFilters(spaces) }
    )

    object None : Subscription("", emptyList(), emptyList(), 0, filters = { emptyList() })
}

class SubscriptionMapper {

    fun map(id: Id): Subscription {
        return when (id) {
            Subscriptions.SUBSCRIPTION_RECENT -> Subscription.Recent
            Subscriptions.SUBSCRIPTION_RECENT_LOCAL -> Subscription.RecentLocal
            Subscriptions.SUBSCRIPTION_ARCHIVED -> Subscription.Bin
            Subscriptions.SUBSCRIPTION_SETS -> Subscription.Sets
            Subscriptions.SUBSCRIPTION_FAVORITES -> Subscription.Favorites
            Subscriptions.SUBSCRIPTION_COLLECTIONS -> Subscription.Collections
            Subscriptions.SUBSCRIPTION_FILES -> Subscription.Files
            else -> Subscription.None
        }
    }
}

private val SUBSCRIPTION_DEFAULT_KEYS = ObjectSearchConstants.defaultKeys + Relations.IS_FAVORITE + Relations.DESCRIPTION