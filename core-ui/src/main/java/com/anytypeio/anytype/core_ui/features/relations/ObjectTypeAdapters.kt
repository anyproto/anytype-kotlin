package com.anytypeio.anytype.core_ui.features.relations

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.databinding.ItemObjectTypePickerBinding
import com.anytypeio.anytype.core_ui.databinding.ItemObjectTypePickerWithoutDescriptionBinding
import com.anytypeio.anytype.core_ui.widgets.ObjectIconWidget
import com.anytypeio.anytype.presentation.relations.model.DefaultObjectTypeView
import com.anytypeio.anytype.presentation.relations.model.SelectLimitObjectTypeView

class ObjectTypeAddAdapter(
    val onObjectTypeClicked: (SelectLimitObjectTypeView) -> Unit
) : ListAdapter<SelectLimitObjectTypeView, ObjectTypeAddAdapter.ViewHolder>(
    LimitObjectTypeDiffer
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = when(viewType) {
        DEFAULT_TYPE -> {
            ViewHolder.Default(
                binding = ItemObjectTypePickerBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            ).apply {
                itemView.setOnClickListener {
                    val pos = bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        onObjectTypeClicked(getItem(pos))
                    }
                }
            }
        }
        WITHOUT_DESCRIPTION_TYPE -> {
            ViewHolder.WithoutDescription(
                binding = ItemObjectTypePickerWithoutDescriptionBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            ).apply {
                itemView.setOnClickListener {
                    val pos = bindingAdapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        onObjectTypeClicked(getItem(pos))
                    }
                }
            }
        }
        else -> throw IllegalStateException("Unexpected view type: $viewType")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item = item.item)
        holder.checkbox.isSelected = item.isSelected
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            payloads.forEach { change ->
                if (change is Int && change == LimitObjectTypeDiffer.CHECKBOX_CHANGED) {
                    holder.checkbox.isSelected = getItem(position).isSelected
                }
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return if (item.item.subtitle.isNullOrEmpty())
            WITHOUT_DESCRIPTION_TYPE
        else
            DEFAULT_TYPE
    }

    sealed class ViewHolder(view: View) : DefaultObjectTypeViewHolder(view) {

        abstract val checkbox : View

        class Default(
            val binding: ItemObjectTypePickerBinding
        ) : ViewHolder(binding.root) {
            override val title: TextView = binding.tvTitle
            override val subtitle: TextView = binding.tvSubtitle
            override val icon: ObjectIconWidget = binding.iconWidget
            override val checkbox = binding.objectSelectionIndex
        }

        class WithoutDescription(
            val binding: ItemObjectTypePickerWithoutDescriptionBinding
        ) : ViewHolder(binding.root) {
            override val title: TextView = binding.tvTitle
            override val subtitle: TextView? = null
            override val icon: ObjectIconWidget = binding.iconWidget
            override val checkbox = binding.objectSelectionIndex
        }
    }

    companion object {
        const val DEFAULT_TYPE = 0
        const val WITHOUT_DESCRIPTION_TYPE = 1
    }
}

object LimitObjectTypeDiffer : DiffUtil.ItemCallback<SelectLimitObjectTypeView>() {
    override fun areItemsTheSame(
        oldItem: SelectLimitObjectTypeView,
        newItem: SelectLimitObjectTypeView
    ): Boolean = newItem.item.id == oldItem.item.id

    override fun areContentsTheSame(
        oldItem: SelectLimitObjectTypeView,
        newItem: SelectLimitObjectTypeView
    ): Boolean = newItem == oldItem

    override fun getChangePayload(
        oldItem: SelectLimitObjectTypeView,
        newItem: SelectLimitObjectTypeView
    ): Any? {
        return if (newItem.item == oldItem.item) {
            if (newItem.isSelected != oldItem.isSelected)
                CHECKBOX_CHANGED
            else
                super.getChangePayload(oldItem, newItem)
        } else {
            super.getChangePayload(oldItem, newItem)
        }
    }

    const val CHECKBOX_CHANGED = 1
}

abstract class DefaultObjectTypeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    abstract val title: TextView
    abstract val subtitle: TextView?
    abstract val icon: ObjectIconWidget

    fun bind(item: DefaultObjectTypeView) {
        title.text = item.title
        subtitle?.text = item.subtitle
        icon.setIcon(item.icon)
    }
}