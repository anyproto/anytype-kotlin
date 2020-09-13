package com.agileburo.anytype.ui.desktop

import androidx.recyclerview.widget.DiffUtil
import com.agileburo.anytype.presentation.desktop.DashboardView

class DesktopDiffUtil(
    private val old: List<DashboardView>,
    private val new: List<DashboardView>
) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        new[newItemPosition].id == old[oldItemPosition].id

    override fun getOldListSize(): Int = old.size
    override fun getNewListSize(): Int = new.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        new[newItemPosition] == old[oldItemPosition]
}