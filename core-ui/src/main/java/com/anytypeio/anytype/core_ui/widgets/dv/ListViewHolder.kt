package com.anytypeio.anytype.core_ui.widgets.dv

import android.text.SpannableString
import android.text.style.LeadingMarginSpan
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

    protected val tvPrimary: TextView get() = itemView.findViewById(R.id.tvPrimary)
    protected val tvSecondary: TextView get() = itemView.findViewById(R.id.tvSecondary)
    protected val relations: ListViewItemRelationGroupWidget get() = itemView.findViewById(R.id.relationsContainer)
    val firstLineMargin = itemView.resources.getDimensionPixelOffset(R.dimen.default_dv_list_first_line_margin_start)
    val untitled = itemView.resources.getString(R.string.untitled)

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
            updateTitleAndIcon(item)
            updateDescription(item)
            updateRelations(item)
        }

        fun bind(item: Viewer.ListView.Item.Default, payloads: List<Int>) {
            payloads.forEach { payload ->
                when (payload) {
                    ListViewDiffer.PAYLOAD_NAME, ListViewDiffer.PAYLOAD_ICON -> {
                        updateTitleAndIcon(item)
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

        private fun updateTitleAndIcon(item: Viewer.ListView.Item.Default) {
            if (!item.hideIcon && item.icon != ObjectIcon.None) {
                icon.visible()
                icon.setIcon(item.icon)
                val sb = SpannableString(item.name.ifEmpty { untitled })
                sb.setSpan(
                    LeadingMarginSpan.Standard(firstLineMargin, 0), 0, sb.length, 0
                )
                tvPrimary.text = sb
            } else {
                icon.gone()
                tvPrimary.text = when {
                    item.name.isEmpty() -> SpannableString(untitled)
                    else -> SpannableString(item.name)
                }
            }
        }
    }

    class Profile(binding: ItemDvListViewProfileSmallBinding) : ListViewHolder(binding.root) {

        private val icon = binding.icon

        fun bind(item: Viewer.ListView.Item.Profile) {
            updateTitleAndIcon(item)
            updateDescription(item)
            updateRelations(item)
        }

        fun bind(item: Viewer.ListView.Item.Profile, payloads: List<Int>) {
            payloads.forEach { payload ->
                when (payload) {
                    ListViewDiffer.PAYLOAD_NAME, ListViewDiffer.PAYLOAD_ICON -> {
                        updateTitleAndIcon(item)
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

        private fun updateTitleAndIcon(item: Viewer.ListView.Item.Profile) {
            if (!item.hideIcon && item.icon != ObjectIcon.None) {
                icon.visible()
                icon.setIcon(item.icon)
                val sb = SpannableString(item.name.ifEmpty { untitled })
                sb.setSpan(
                    LeadingMarginSpan.Standard(firstLineMargin, 0), 0, sb.length, 0
                )
                tvPrimary.text = sb
            } else {
                icon.gone()
                tvPrimary.text = when {
                    item.name.isEmpty() -> SpannableString(untitled)
                    else -> SpannableString(item.name)
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

        private fun updateName(item: Viewer.ListView.Item) {
            tvPrimary.text = item.name
        }
    }
}