package com.agileburo.anytype.ui.desktop

import androidx.recyclerview.widget.DiffUtil
import com.agileburo.anytype.presentation.desktop.DashboardView

class DesktopDiffUtil(
    private val old: List<DashboardView>,
    private val new: List<DashboardView>
) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {

        val oldItem = old[oldItemPosition]
        val newItem = new[newItemPosition]

        if (oldItem is DashboardView.Document && newItem is DashboardView.Document)
            return oldItem.id == newItem.id
        else
            throw IllegalStateException("Unexpected state")
    }

    override fun getOldListSize(): Int = old.size
    override fun getNewListSize(): Int = new.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = old[oldItemPosition]
        val newItem = new[newItemPosition]

        return (oldItem as DashboardView.Document) == (newItem as DashboardView.Document)
    }
}