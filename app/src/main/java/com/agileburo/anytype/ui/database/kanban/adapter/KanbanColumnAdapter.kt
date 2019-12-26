package com.agileburo.anytype.ui.database.kanban.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.agileburo.anytype.R
import com.agileburo.anytype.presentation.databaseview.models.KanbanRowView
import com.agileburo.anytype.ui.database.kanban.viewholder.KanbanRowViewHolder
import com.woxthebox.draglistview.DragItemAdapter
import java.util.*

class KanbanColumnAdapter(
    private val column: MutableList<KanbanRowView>
) : DragItemAdapter<KanbanRowView, KanbanRowViewHolder>() {

    init {
        itemList = column
    }

    private val TYPE_TASK = 1
    private val TYPE_PAGE = 2
    private val TYPE_PERSON = 3
    private val TYPE_BOOKMARK = 4
    private val TYPE_FILE = 5
    private val TYPE_NEW_ITEM = 6
    private val TYPE_DEMO = 7

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KanbanRowViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_TASK -> {
                val view = inflater.inflate(R.layout.item_kanban_task, parent, false)
                KanbanRowViewHolder.KanbanTaskViewHolder(
                    view,
                    R.id.taskContainer
                )
            }
            TYPE_PAGE -> {
                val view = inflater.inflate(R.layout.item_kanban_page, parent, false)
                KanbanRowViewHolder.KanbanPageViewHolder(
                    view,
                    R.id.pageContainer
                )
            }
            TYPE_PERSON -> {
                val view = inflater.inflate(R.layout.item_kanban_person, parent, false)
                KanbanRowViewHolder.KanbanPeopleViewHolder(
                    view,
                    R.id.personContainer
                )
            }
            TYPE_BOOKMARK -> {
                val view = inflater.inflate(R.layout.item_kanban_bookmark, parent, false)
                KanbanRowViewHolder.KanbanBookmarkViewHolder(
                    view,
                    R.id.bookmarkContainer
                )
            }
            TYPE_FILE -> {
                val view = inflater.inflate(R.layout.item_kanban_file, parent, false)
                KanbanRowViewHolder.KanbanFileViewHolder(
                    view,
                    R.id.fileContainer
                )
            }
            TYPE_NEW_ITEM -> {
                val view = inflater.inflate(R.layout.item_kanban_add_new_item, parent, false)
                KanbanRowViewHolder.KanbanAddNewItemViewHolder(
                    view,
                    R.id.newItemContainer
                )
            }
            TYPE_DEMO -> {
                val view = inflater.inflate(R.layout.item_kanban_demo, parent, false)
                KanbanRowViewHolder.KanbanDemoViewHolder(
                    view,
                    R.id.demoContainer
                )
            }
            else -> throw IllegalStateException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(
        holder: KanbanRowViewHolder,
        position: Int
    ) {
        super.onBindViewHolder(holder, position)
        when (holder) {
            is KanbanRowViewHolder.KanbanTaskViewHolder -> holder.bind(
                model = column[position] as KanbanRowView.KanbanTaskView
            )
            is KanbanRowViewHolder.KanbanBookmarkViewHolder -> holder.bind(
                model = column[position] as KanbanRowView.KanbanBookmarkView
            )
            is KanbanRowViewHolder.KanbanPeopleViewHolder -> holder.bind(
                model = column[position] as KanbanRowView.KanbanPeopleView
            )
            is KanbanRowViewHolder.KanbanFileViewHolder -> holder.bind(
                model = column[position] as KanbanRowView.KanbanFileView
            )
            is KanbanRowViewHolder.KanbanPageViewHolder -> holder.bind(
                model = column[position] as KanbanRowView.KanbanPageView
            )
            is KanbanRowViewHolder.KanbanDemoViewHolder -> holder.bind(
                model = column[position] as KanbanRowView.KanbanDemoView
            )
        }
    }

    override fun getItemCount(): Int = column.size
    override fun getItemViewType(position: Int): Int =
        when (column[position]) {
            is KanbanRowView.KanbanFileView -> TYPE_FILE
            is KanbanRowView.KanbanPeopleView -> TYPE_PERSON
            is KanbanRowView.KanbanBookmarkView -> TYPE_BOOKMARK
            is KanbanRowView.KanbanPageView -> TYPE_PAGE
            is KanbanRowView.KanbanTaskView -> TYPE_TASK
            is KanbanRowView.KanbanAddNewItemView -> TYPE_NEW_ITEM
            is KanbanRowView.KanbanDemoView -> TYPE_DEMO
        }

    override fun getUniqueItemId(position: Int): Long =
        UUID.fromString(column[position].id).mostSignificantBits
}