package com.anytypeio.anytype.core_ui.features.sets

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.databinding.ItemDvViewerFilterConditionBinding
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.sets.model.Viewer

class PickFilterConditionAdapter(
    private val conditions: List<Viewer.Filter.Condition>,
    private val picked: Viewer.Filter.Condition?,
    private val click: (Viewer.Filter.Condition) -> Unit
) : RecyclerView.Adapter<PickFilterConditionAdapter.ConditionHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConditionHolder {
        return ConditionHolder(
            binding = ItemDvViewerFilterConditionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        ).apply {
            binding.root.setOnClickListener {
                click.invoke(conditions[bindingAdapterPosition])
            }
        }
    }

    override fun onBindViewHolder(holder: ConditionHolder, position: Int) {
        holder.bind(
            isChecked = conditions[position].title == picked?.title,
            condition = conditions[position]
        )
    }

    override fun getItemCount(): Int = conditions.size

    inner class ConditionHolder(
        val binding: ItemDvViewerFilterConditionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            isChecked: Boolean,
            condition: Viewer.Filter.Condition,
        ) {
            if (isChecked) {
                binding.iconCheck.visible()
            } else {
                binding.iconCheck.invisible()
            }
            binding.tvCondition.text = condition.title
        }
    }
}