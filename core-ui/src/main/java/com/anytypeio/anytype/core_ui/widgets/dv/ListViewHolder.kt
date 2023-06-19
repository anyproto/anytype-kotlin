package com.anytypeio.anytype.core_ui.widgets.dv

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemDvListViewDefaultBinding
import com.anytypeio.anytype.core_ui.databinding.ItemDvListViewProfileSmallBinding
import com.anytypeio.anytype.core_ui.databinding.ItemDvListViewTaskBinding
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.sets.model.Viewer

sealed class ListViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val tvPrimary: TextView get() = itemView.findViewById(R.id.tvPrimary)
    private val tvSecondary: TextView get() = itemView.findViewById(R.id.tvSecondary)
    protected val relations: ListViewItemRelationGroupWidget get() = itemView.findViewById(R.id.relationsContainer)

    fun updateName(item: Viewer.ListView.Item) {
        tvPrimary.text = item.name
    }

    fun updateDescription(item: Viewer.ListView.Item) {
        if (item.description.isNullOrBlank()) {
            tvSecondary.gone()
        } else {
            tvSecondary.visible()
            tvSecondary.text = item.description
        }
    }

    fun updateRelations(item: Viewer.ListView.Item) {
        if (item.relations.isEmpty()) {
            relations.gone()
            relations.clear()
        } else {
            relations.visible()
            relations.set(item.relations)
        }
    }

    class Default(binding: ItemDvListViewDefaultBinding) : ListViewHolder(binding.root) {

        private val icon = binding.icon

        fun bind(item: Viewer.ListView.Item.Default) {
            updateIcon(item)
            updateName(item)
            updateDescription(item)
            updateRelations(item)
        }

        fun bind(item: Viewer.ListView.Item.Default, payloads: List<Int>) {
            payloads.forEach { payload ->
                when (payload) {
                    ListViewDiffer.PAYLOAD_NAME -> {
                        updateName(item)
                    }
                    ListViewDiffer.PAYLOAD_DESCRIPTION -> {
                        updateDescription(item)
                    }
                    ListViewDiffer.PAYLOAD_RELATION -> {
                        updateRelations(item)
                    }
                    ListViewDiffer.PAYLOAD_ICON -> {
                        updateIcon(item)
                    }
                }
            }
        }

        private fun updateIcon(item: Viewer.ListView.Item.Default) {
            icon.apply {
                if (item.hideIcon || item.icon is ObjectIcon.None) {
                    gone()
                } else {
                    visible()
                    setIcon(item.icon)
                }
            }
        }
    }

    class Profile(binding: ItemDvListViewProfileSmallBinding) : ListViewHolder(binding.root) {

        private val icon = binding.icon

        fun bind(item: Viewer.ListView.Item.Profile) {
            updateIcon(item)
            updateName(item)
            updateDescription(item)
            updateRelations(item)
        }

        fun bind(item: Viewer.ListView.Item.Profile, payloads: List<Int>) {
            payloads.forEach { payload ->
                when (payload) {
                    ListViewDiffer.PAYLOAD_NAME -> {
                        updateName(item)
                    }
                    ListViewDiffer.PAYLOAD_DESCRIPTION -> {
                        updateDescription(item)
                    }
                    ListViewDiffer.PAYLOAD_RELATION -> {
                        updateRelations(item)
                    }
                    ListViewDiffer.PAYLOAD_ICON -> {
                        updateIcon(item)
                    }
                }
            }
        }

        private fun updateIcon(item: Viewer.ListView.Item.Profile) {
            icon.apply {
                when {
                    item.hideIcon -> gone()
                    item.icon is ObjectIcon.Profile -> {
                        visible()
                        setIcon(item.icon)
                    }
                    else -> gone()
                }
            }
        }
    }

    class Task(binding: ItemDvListViewTaskBinding) : ListViewHolder(binding.root) {

        val icon: ImageView = binding.icon

        fun bind(item: Viewer.ListView.Item.Task) {
            updateTaskChecked(item)
            updateName(item)
            updateDescription(item)
            updateRelations(item)
        }

        fun bind(item: Viewer.ListView.Item.Task, payloads: List<Int>) {
            payloads.forEach { payload ->
                when (payload) {
                    ListViewDiffer.PAYLOAD_CHECKED -> {
                        updateTaskChecked(item)
                    }
                    ListViewDiffer.PAYLOAD_NAME -> {
                        updateName(item)
                    }
                    ListViewDiffer.PAYLOAD_DESCRIPTION -> {
                        updateDescription(item)
                    }
                    ListViewDiffer.PAYLOAD_RELATION -> {
                        updateRelations(item)
                    }
                }
            }
        }

        private fun updateTaskChecked(item: Viewer.ListView.Item.Task) {
            icon.isSelected = item.done
        }
    }
}