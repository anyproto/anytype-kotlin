package com.anytypeio.anytype.core_ui.widgets.dv

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.databinding.ItemDvListViewDefaultBinding
import com.anytypeio.anytype.core_ui.databinding.ItemDvListViewProfileSmallBinding
import com.anytypeio.anytype.core_ui.databinding.ItemDvListViewTaskBinding
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.presentation.sets.model.Viewer.ListView.Item.Default
import com.anytypeio.anytype.presentation.sets.model.Viewer.ListView.Item.Profile
import com.anytypeio.anytype.presentation.sets.model.Viewer.ListView.Item.Task

class ListViewAdapter(
    private val onListItemClicked: (Id) -> Unit,
    private val onTaskCheckboxClicked: (Id) -> Unit
) : ListAdapter<Viewer.ListView.Item, ListViewHolder>(ListViewDiffer) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            VIEW_TYPE_DEFAULT -> {
                val binding = ItemDvListViewDefaultBinding.inflate(inflater, parent, false)
                return ListViewHolder.Default(binding).apply {
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
                val binding = ItemDvListViewProfileSmallBinding.inflate(inflater, parent, false)
                return ListViewHolder.Profile(binding).apply {
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
                val binding = ItemDvListViewTaskBinding.inflate(inflater, parent, false)
                return ListViewHolder.Task(binding).apply {
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
                            if (item is Task) {
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
            is ListViewHolder.Default -> holder.bind(getItem(position) as Default)
            is ListViewHolder.Profile -> holder.bind(getItem(position) as Profile)
            is ListViewHolder.Task -> holder.bind(getItem(position) as Task)
        }
    }

    override fun onBindViewHolder(
        holder: ListViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            payloads.forEach { payload ->
                if (payload is List<*>) {
                    when (holder) {
                        is ListViewHolder.Task -> {
                            holder.bind(
                                item = getItem(position) as Task,
                                payloads = payload.typeOf()
                            )
                        }
                        is ListViewHolder.Default -> {
                            holder.bind(
                                item = getItem(position) as Default,
                                payloads = payload.typeOf()
                            )
                        }
                        is ListViewHolder.Profile -> {
                            holder.bind(
                                item = getItem(position) as Profile,
                                payloads = payload.typeOf()
                            )
                        }
                    }
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int = when (getItem(position)) {
        is Default -> VIEW_TYPE_DEFAULT
        is Profile -> VIEW_TYPE_PROFILE
        is Task -> VIEW_TYPE_TASK
    }

    companion object {
        const val VIEW_TYPE_DEFAULT = 0
        const val VIEW_TYPE_PROFILE = 1
        const val VIEW_TYPE_TASK = 2
    }
}