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
            if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                onObjectActionClicked(getItem(bindingAdapterPosition))
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(getItem(position))

    class ViewHolder(val binding: ItemObjectMenuActionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ObjectAction) = with(binding) {
            when (item) {
                ObjectAction.DELETE, ObjectAction.DELETE_FILES -> {
                    ivActionIcon.setImageResource(R.drawable.ic_object_action_archive)
                    tvActionTitle.setText(R.string.action_bar_delete)
                }
                ObjectAction.MOVE_TO_BIN -> {
                    ivActionIcon.setImageResource(R.drawable.ic_object_action_archive)
                    tvActionTitle.setText(R.string.action_bar_to_bin)
                }
                ObjectAction.REMOVE_FROM_FAVOURITE -> {
                    ivActionIcon.setImageResource(R.drawable.ic_object_action_unfavorite)
                    tvActionTitle.setText(R.string.unfavorite)
                }
                ObjectAction.DUPLICATE -> {
                    ivActionIcon.setImageResource(R.drawable.ic_object_action_duplicate)
                    tvActionTitle.setText(R.string.object_action_duplicate)
                }
                ObjectAction.UNDO_REDO -> {
                    ivActionIcon.setImageResource(R.drawable.ic_object_action_undoredo)
                    tvActionTitle.setText(R.string.undoredo)
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
                ObjectAction.LINK_TO -> {
                    ivActionIcon.setImageResource(R.drawable.ic_object_action_link_to)
                    tvActionTitle.setText(R.string.link_to)
                }
                ObjectAction.COPY_LINK -> {
                    ivActionIcon.setImageResource(R.drawable.ic_object_action_copy_link)
                    tvActionTitle.setText(R.string.copy_link)
                }
                ObjectAction.USE_AS_TEMPLATE -> {
                    ivActionIcon.setImageResource(R.drawable.ic_object_action_template)
                    tvActionTitle.setText(R.string.make_template)
                }
                ObjectAction.SET_AS_DEFAULT -> {
                    ivActionIcon.setImageResource(R.drawable.ic_set_as_default_24)
                    tvActionTitle.setText(R.string.set_as_default)
                }
                ObjectAction.PIN -> {
                    ivActionIcon.setImageResource(R.drawable.ic_state_pin_24)
                    tvActionTitle.setText(R.string.object_action_pin)
                }
                ObjectAction.UNPIN -> {
                    ivActionIcon.setImageResource(R.drawable.ic_state_unpin_24)
                    tvActionTitle.setText(R.string.object_action_unpin)
                }
                ObjectAction.DOWNLOAD_FILE -> {
                    ivActionIcon.setImageResource(R.drawable.ic_object_action_download)
                    tvActionTitle.setText(R.string.object_action_download)
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