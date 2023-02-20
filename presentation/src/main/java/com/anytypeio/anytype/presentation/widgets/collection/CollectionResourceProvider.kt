package com.anytypeio.anytype.presentation.widgets.collection

import android.content.Context
import com.anytypeio.anytype.presentation.R
import javax.inject.Inject

class CollectionResourceProvider @Inject constructor(
    private val context: Context
) {
    fun actionModeName(actionMode: ActionMode): String {
        return when (actionMode) {
            ActionMode.SelectAll -> context.getString(R.string.select_all)
            ActionMode.UnselectAll -> context.getString(R.string.unselect_all)
            ActionMode.Edit -> context.getString(R.string.edit)
            ActionMode.Done -> context.getString(R.string.done)
        }
    }

    fun subscriptionName(subscription: Subscription): String {
        return when (subscription) {
            Subscription.Recent -> context.getString(R.string.recent)
            Subscription.Sets -> context.getString(R.string.sets)
            Subscription.Favorites -> context.getString(R.string.favorites)
            Subscription.Bin -> context.getString(R.string.bin)
            Subscription.None -> ""
        }
    }
}