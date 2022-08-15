package com.anytypeio.anytype.core_ui.features.relations

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemRelationValueActionBinding
import com.anytypeio.anytype.presentation.sets.RelationValueAction

class RelationActionAdapter(
    private val onActionClicked: (RelationValueAction) -> Unit
) : ListAdapter<RelationValueAction, RelationActionAdapter.VH>(Differ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        binding = ItemRelationValueActionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    ).apply {
        itemView.setOnClickListener {
            val pos = bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                onActionClicked(getItem(pos))
            }
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(action = getItem(position))
    }

    class VH(val binding: ItemRelationValueActionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(action: RelationValueAction) {
            when (action) {
                is RelationValueAction.Url.Reload -> {
                    binding.tvTitle.setText(R.string.reload_object_content)
                    binding.ivActionIcon.setImageResource(R.drawable.ic_relation_action_reload)
                }
                is RelationValueAction.Url.Copy -> {
                    binding.tvTitle.setText(R.string.copy_link)
                    binding.ivActionIcon.setImageResource(R.drawable.ic_relation_action_copy)
                }
                is RelationValueAction.Url.Browse -> {
                    binding.tvTitle.setText(R.string.open_link)
                    binding.ivActionIcon.setImageResource(R.drawable.ic_relation_action_browse_url)
                }
                is RelationValueAction.Email.Copy -> {
                    binding.tvTitle.setText(R.string.copy_email)
                    binding.ivActionIcon.setImageResource(R.drawable.ic_relation_action_copy)
                }
                is RelationValueAction.Email.Mail -> {
                    binding.tvTitle.setText(R.string.send_email)
                    binding.ivActionIcon.setImageResource(R.drawable.ic_relation_action_mail_to)
                }
                is RelationValueAction.Phone.Call -> {
                    binding.tvTitle.setText(R.string.call_phone_number)
                    binding.ivActionIcon.setImageResource(R.drawable.ic_relation_action_dial)
                }
                is RelationValueAction.Phone.Copy -> {
                    binding.tvTitle.setText(R.string.copy_phone_number)
                    binding.ivActionIcon.setImageResource(R.drawable.ic_relation_action_copy)
                }
            }
        }
    }
}


object Differ : DiffUtil.ItemCallback<RelationValueAction>() {
    override fun areItemsTheSame(
        oldItem: RelationValueAction,
        newItem: RelationValueAction
    ): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: RelationValueAction,
        newItem: RelationValueAction
    ): Boolean {
        return oldItem == newItem
    }
}