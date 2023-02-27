package com.anytypeio.anytype.presentation.widgets.collection

import com.anytypeio.anytype.presentation.objects.ObjectAction
import com.anytypeio.anytype.presentation.objects.ObjectAction.ADD_TO_FAVOURITE
import com.anytypeio.anytype.presentation.objects.ObjectAction.DELETE
import com.anytypeio.anytype.presentation.objects.ObjectAction.MOVE_TO_BIN
import com.anytypeio.anytype.presentation.objects.ObjectAction.REMOVE_FROM_FAVOURITE
import com.anytypeio.anytype.presentation.objects.ObjectAction.RESTORE
import javax.inject.Inject

class ActionObjectFilter @Inject constructor() {

    fun filter(subscription: Subscription, views: List<CollectionView>): List<ObjectAction> {
        return when (subscription) {
            Subscription.Recent, Subscription.Sets -> listOf(
                ADD_TO_FAVOURITE,
                REMOVE_FROM_FAVOURITE,
                MOVE_TO_BIN
            )
            Subscription.Bin -> listOf(DELETE, RESTORE)
            Subscription.Favorites -> listOf(REMOVE_FROM_FAVOURITE, MOVE_TO_BIN)
            Subscription.None -> emptyList()
        }
    }
}