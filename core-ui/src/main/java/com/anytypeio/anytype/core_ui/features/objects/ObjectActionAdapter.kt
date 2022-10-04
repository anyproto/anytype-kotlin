package com.anytypeio.anytype.core_ui.features.objects

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemObjectMenuActionBinding
import com.anytypeio.anytype.presentation.objects.ObjectAction

class ObjectActionAdapter(
    val onObjectActionClicked: (ObjectAction) -> Unit
) : ListAdapter<ObjectAction, ObjectActionAdapter.ViewHolder>(Differ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        binding = ItemObjectMenuActionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ).apply {
        binding.btnContainer.setOnClickListener {
            val pos = bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION)
                onObjectActionClicked(getItem(pos))
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    class ViewHolder(val binding: ItemObjectMenuActionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ObjectAction) = with(binding) {
            when (item) {
                ObjectAction.DELETE -> {
                    ivActionIcon.setImageResource(R.drawable.ic_object_action_archive)
                    tvActionTitle.setText(R.string.action_bar_delete)
                }
                ObjectAction.DUPLICATE -> {
                    ivActionIcon.setImageResource(R.drawable.ic_object_action_duplicate)
                    tvActionTitle.setText(R.string.object_action_duplicate)
                }
                ObjectAction.UNDO_REDO -> {
                    ivActionIcon.setImageResource(R.drawable.ic_object_action_undoredo)
                    tvActionTitle.setText(R.string.undoredo)
                }
                ObjectAction.ADD_TO_FAVOURITE -> {
                    ivActionIcon.setImageResource(R.drawable.ic_object_action_add_to_favorites)
                    tvActionTitle.setText(R.string.favourite)
                }
                ObjectAction.REMOVE_FROM_FAVOURITE -> {
                    ivActionIcon.setImageResource(R.drawable.ic_object_action_unfavorite)
                    tvActionTitle.setText(R.string.unfavorite)
                }
                ObjectAction.SEARCH_ON_PAGE -> {
                    ivActionIcon.setImageResource(R.drawable.ic_object_action_search)
                    tvActionTitle.setText(R.string.search)
                }
                ObjectAction.RESTORE -> {
                    ivActionIcon.setImageResource(R.drawable.ic_object_action_restore)
                    tvActionTitle.setText(R.string.restore)
                }
                ObjectAction.LOCK -> {
                    ivActionIcon.setImageResource(R.drawable.ic_object_action_lock)
                    tvActionTitle.setText(R.string.lock)
                }
                ObjectAction.UNLOCK -> {
                    ivActionIcon.setImageResource(R.drawable.ic_object_action_unlock)
                    tvActionTitle.setText(R.string.unlock)
                }
                ObjectAction.MOVE_TO -> {

                }
                else -> {}
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