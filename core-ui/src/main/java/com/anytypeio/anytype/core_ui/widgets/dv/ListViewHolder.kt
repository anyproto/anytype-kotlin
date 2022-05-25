package com.anytypeio.anytype.core_ui.widgets.dv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.widgets.ObjectIconWidget
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.sets.model.Viewer

sealed class ListViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    class Default(parent: ViewGroup) : ListViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.item_dv_list_view_default,
            parent,
            false
        )
    ) {

        private val icon get() = itemView.findViewById<ObjectIconWidget>(R.id.icon)
        private val tvPrimary get() = itemView.findViewById<TextView>(R.id.tvPrimary)
        private val tvSecondary get() = itemView.findViewById<TextView>(R.id.tvSecondary)
        private val relations get() = itemView.findViewById<ListViewItemRelationGroupWidget>(R.id.relationsContainer)

        fun bind(item: Viewer.ListView.Item) {
            when {
                item.hideIcon -> icon.gone()
                item.icon is ObjectIcon.Basic -> {
                    icon.visible()
                    icon.setIcon(item.icon)
                }
                else -> icon.gone()
            }
            tvPrimary.text = item.name
            if (item.description.isNullOrBlank()) {
                tvSecondary.gone()
            } else {
                tvSecondary.visible()
                tvSecondary.text = item.description
            }
            if (item.relations.isEmpty()) {
                relations.gone()
                relations.clear()
            } else {
                relations.visible()
                relations.set(item.relations)
            }
        }
    }

    class Profile(parent: ViewGroup) : ListViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.item_dv_list_view_profile_small,
            parent,
            false
        )
    ) {

        private val icon get() = itemView.findViewById<ObjectIconWidget>(R.id.icon)
        private val tvPrimary get() = itemView.findViewById<TextView>(R.id.tvPrimary)
        private val tvSecondary get() = itemView.findViewById<TextView>(R.id.tvSecondary)
        private val relations get() = itemView.findViewById<ListViewItemRelationGroupWidget>(R.id.relationsContainer)

        fun bind(item: Viewer.ListView.Item) {
            when {
                item.hideIcon -> icon.gone()
                item.icon is ObjectIcon.Profile -> {
                    icon.visible()
                    icon.setIcon(item.icon)
                }
                else -> icon.gone()
            }
            tvPrimary.text = item.name
            if (item.description.isNullOrBlank()) {
                tvSecondary.gone()
            } else {
                tvSecondary.visible()
                tvSecondary.text = item.description
            }
            if (item.relations.isEmpty()) {
                relations.gone()
                relations.clear()
            } else {
                relations.visible()
                relations.set(item.relations)
            }
        }
    }

    class Task(parent: ViewGroup) : ListViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.item_dv_list_view_task,
            parent,
            false
        )
    ) {

        val icon get() = itemView.findViewById<ObjectIconWidget>(R.id.icon)
        private val tvPrimary get() = itemView.findViewById<TextView>(R.id.tvPrimary)
        private val tvSecondary get() = itemView.findViewById<TextView>(R.id.tvSecondary)
        private val relations get() = itemView.findViewById<ListViewItemRelationGroupWidget>(R.id.relationsContainer)

        fun bind(item: Viewer.ListView.Item) {
            when {
                item.hideIcon -> icon.setIcon(ObjectIcon.None)
                else -> icon.setIcon(item.icon)
            }
            tvPrimary.text = item.name
            if (item.description.isNullOrBlank()) {
                tvSecondary.gone()
            } else {
                tvSecondary.visible()
                tvSecondary.text = item.description
            }
            if (item.relations.isEmpty()) {
                relations.gone()
                relations.clear()
            } else {
                relations.visible()
                relations.set(item.relations)
            }
        }
    }
}