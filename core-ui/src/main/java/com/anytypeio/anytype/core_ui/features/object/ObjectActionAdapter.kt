package com.anytypeio.anytype.core_ui.features.`object`

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.`object`.ObjectAction
import kotlinx.android.synthetic.main.item_object_menu_action.view.*

class ObjectActionAdapter(
    val onObjectActionClicked: (ObjectAction) -> Unit
) : ListAdapter<ObjectAction, ObjectActionAdapter.ViewHolder>(Differ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent).apply {
        itemView.btnContainer.setOnClickListener {
            val pos = bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION)
                onObjectActionClicked(getItem(pos))
        }
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.item_object_menu_action, parent, false
        )
    ) {
        fun bind(item: ObjectAction) = with(itemView) {
            when (item) {
                ObjectAction.DELETE -> {
                    ivActionIcon.setImageResource(R.drawable.ic_object_action_archive)
                    tvActionTitle.setText(R.string.archive)
                }
                ObjectAction.ADD_TO_FAVOURITE -> {
                    ivActionIcon.setImageResource(R.drawable.ic_object_action_add_to_favorites)
                    tvActionTitle.setText(R.string.favourite)
                }
                ObjectAction.REMOVE_FROM_FAVOURITE -> {
                    ivActionIcon.setImageResource(R.drawable.ic_object_action_add_to_favorites)
                    tvActionTitle.setText(R.string.unfavorite)
                }
                ObjectAction.SEARCH_ON_PAGE -> {
                    ivActionIcon.setImageResource(R.drawable.ic_object_action_search)
                    tvActionTitle.setText(R.string.search)
                }
                ObjectAction.USE_AS_TEMPLATE -> {
                    ivActionIcon.setImageResource(R.drawable.ic_object_action_template)
                    tvActionTitle.setText(R.string.template)
                }
                ObjectAction.RESTORE -> {
                    ivActionIcon.setImageResource(R.drawable.ic_object_action_archive)
                    tvActionTitle.setText(R.string.restore)
                }
            }
        }
    }

    object Differ : DiffUtil.ItemCallback<ObjectAction>() {
        override fun areItemsTheSame(
            oldItem: ObjectAction,
            newItem: ObjectAction
        ): Boolean = oldItem.name == newItem.name

        override fun areContentsTheSame(
            oldItem: ObjectAction,
            newItem: ObjectAction
        ): Boolean = oldItem == newItem
    }
}