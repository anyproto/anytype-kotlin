package com.anytypeio.anytype.presentation.widgets.collection

import android.content.Context
import com.anytypeio.anytype.core_models.RelativeDate
import com.anytypeio.anytype.presentation.R
import javax.inject.Inject

interface ResourceProvider {
    fun actionModeName(
        actionMode: ActionMode,
        isResultEmpty: Boolean
    ): String

    fun subscriptionName(subscription: Subscription): String

    fun toFormattedString(relativeDate: RelativeDate?): String

    fun getNonExistentObjectTitle(): String

    fun getUntitledTitle(): String
}

class ResourceProviderImpl @Inject constructor(
    private val context: Context
) : ResourceProvider {
    override fun actionModeName(
        actionMode: ActionMode,
        isResultEmpty: Boolean
    ): String {
        return when (actionMode) {
            ActionMode.SelectAll -> {
                if (isResultEmpty)
                    ""
                else
                    context.getString(R.string.select_all)
            }

            ActionMode.UnselectAll -> {
                if (isResultEmpty) {
                    ""
                } else {
                    context.getString(R.string.unselect_all)
                }
            }

            ActionMode.Edit -> context.getString(R.string.edit)
            ActionMode.Done -> context.getString(R.string.done)
        }
    }

    override fun subscriptionName(subscription: Subscription): String {
        return when (subscription) {
            Subscription.Recent -> context.getString(R.string.recent)
            Subscription.RecentLocal -> context.getString(R.string.recently_opened)
            Subscription.Sets -> context.getString(R.string.sets)
            Subscription.Favorites -> context.getString(R.string.favorites)
            Subscription.Bin -> context.getString(R.string.bin)
            Subscription.Collections -> context.getString(R.string.collections)
            Subscription.None -> ""
            Subscription.Files -> context.getString(R.string.synced_files)
        }
    }

    override fun toFormattedString(relativeDate: RelativeDate?): String {
        return when (relativeDate) {
            RelativeDate.Empty -> ""
            is RelativeDate.Other -> relativeDate.formattedDate
            is RelativeDate.Today -> context.getString(R.string.today)
            is RelativeDate.Tomorrow -> context.getString(R.string.tomorrow)
            is RelativeDate.Yesterday -> context.getString(R.string.yesterday)
            else -> ""
        }
    }

    override fun getNonExistentObjectTitle(): String {
        return context.getString(R.string.non_existent_object)
    }

    override fun getUntitledTitle(): String {
        return context.getString(R.string.untitled)
    }
}