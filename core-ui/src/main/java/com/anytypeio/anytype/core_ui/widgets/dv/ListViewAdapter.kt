package com.anytypeio.anytype.core_ui.widgets.dv

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.sets.model.Viewer

class ListViewAdapter(
    private val onListItemClicked: (Id) -> Unit,
    private val onTaskCheckboxClicked: (Id) -> Unit
) : ListAdapter<Viewer.ListView.Item, ListViewHolder>(Differ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        when (viewType) {
            VIEW_TYPE_DEFAULT -> {
                return ListViewHolder.Default(parent).apply {
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != NO_POSITION) {
                            val item = getItem(pos)
                            if (item is Viewer.ListView.Item) {
                                onListItemClicked(item.objectId)
                            }
                        }
                    }

                }
            }
            VIEW_TYPE_PROFILE -> {
                return ListViewHolder.Profile(parent).apply {
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != NO_POSITION) {
                            val item = getItem(pos)
                            if (item is Viewer.ListView.Item) {
                                onListItemClicked(item.objectId)
                            }
                        }
                    }
                }
            }
            VIEW_TYPE_TASK -> {
                return ListViewHolder.Task(parent).apply {
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != NO_POSITION) {
                            val item = getItem(pos)
                            if (item is Viewer.ListView.Item) {
                                onListItemClicked(item.objectId)
                            }
                        }
                    }
                    icon.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != NO_POSITION) {
                            val item = getItem(pos)
                            if (item is Viewer.ListView.Item && item.icon is ObjectIcon.Task) {
                                onTaskCheckboxClicked(item.objectId)
                            }
                        }
                    }
                }
            }
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        when (holder) {
            is ListViewHolder.Default -> {
                holder.bind(getItem(position) as Viewer.ListView.Item)
            }
            is ListViewHolder.Profile -> {
                holder.bind(getItem(position) as Viewer.ListView.Item)
            }
            is ListViewHolder.Task -> {
                holder.bind(getItem(position) as Viewer.ListView.Item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is Viewer.ListView.Item.Default -> VIEW_TYPE_DEFAULT
        is Viewer.ListView.Item.Profile -> VIEW_TYPE_PROFILE
        is Viewer.ListView.Item.Task -> VIEW_TYPE_TASK
    }

    object Differ : DiffUtil.ItemCallback<Viewer.ListView.Item>() {
        override fun areItemsTheSame(
            oldItem: Viewer.ListView.Item,
            newItem: Viewer.ListView.Item
        ): Boolean {
            return newItem == oldItem
        }

        override fun areContentsTheSame(
            oldItem: Viewer.ListView.Item,
            newItem: Viewer.ListView.Item
        ): Boolean = oldItem == newItem
    }

    companion object {
        const val VIEW_TYPE_DEFAULT = 0
        const val VIEW_TYPE_PROFILE = 1
        const val VIEW_TYPE_TASK = 2
    }
}