package com.anytypeio.anytype.presentation.widgets.collection

import com.anytypeio.anytype.presentation.objects.ObjectAction
import com.anytypeio.anytype.presentation.objects.ObjectAction.ADD_TO_FAVOURITE
import com.anytypeio.anytype.presentation.objects.ObjectAction.DELETE
import com.anytypeio.anytype.presentation.objects.ObjectAction.DELETE_FILES
import com.anytypeio.anytype.presentation.objects.ObjectAction.MOVE_TO_BIN
import com.anytypeio.anytype.presentation.objects.ObjectAction.REMOVE_FROM_FAVOURITE
import com.anytypeio.anytype.presentation.objects.ObjectAction.RESTORE
import javax.inject.Inject

class ActionObjectFilter @Inject constructor() {

    fun filter(subscription: Subscription, views: List<CollectionObjectView>): List<ObjectAction> {
        return when (subscription) {
            Subscription.Recent, Subscription.RecentLocal, Subscription.Sets, Subscription.Collections ->
                buildList {
                    if (views.someNonFavorites()) add(ADD_TO_FAVOURITE)
                    if (views.someFavorites()) add(REMOVE_FROM_FAVOURITE)
                    add(MOVE_TO_BIN)
                }

            Subscription.Bin -> listOf(DELETE, RESTORE)
            Subscription.Favorites -> listOf(REMOVE_FROM_FAVOURITE, MOVE_TO_BIN)
            Subscription.None -> emptyList()
            Subscription.Files -> listOf(DELETE_FILES)
        }
    }

    private fun List<CollectionObjectView>.someFavorites() = any { it.obj.isFavorite }

    private fun List<CollectionObjectView>.someNonFavorites() = any { !it.obj.isFavorite }
}