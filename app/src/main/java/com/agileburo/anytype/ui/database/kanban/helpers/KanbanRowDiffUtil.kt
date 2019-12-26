package com.agileburo.anytype.ui.database.kanban.helpers

import androidx.recyclerview.widget.DiffUtil
import com.agileburo.anytype.presentation.databaseview.models.KanbanRowView

class KanbanRowDiffUtil(
    private val old: List<KanbanRowView>,
    private val new: List<KanbanRowView>
) : DiffUtil.Callback() {

    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ) = old[oldItemPosition].id == new[newItemPosition].id

    override fun areContentsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ) = old[oldItemPosition] == new[newItemPosition]

    override fun getOldListSize(): Int = old.size
    override fun getNewListSize(): Int = new.size
}