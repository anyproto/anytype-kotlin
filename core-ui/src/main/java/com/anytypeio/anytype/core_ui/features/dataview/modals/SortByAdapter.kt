package com.anytypeio.anytype.core_ui.features.dataview.modals

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.databinding.ItemViewerSortAddBinding
import com.anytypeio.anytype.core_ui.databinding.ItemViewerSortApplyBinding
import com.anytypeio.anytype.core_ui.databinding.ItemViewerSortSetBinding
import com.anytypeio.anytype.core_ui.extensions.formatIcon
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.sets.SortClick
import com.anytypeio.anytype.presentation.sets.model.SortingView
import com.anytypeio.anytype.presentation.sets.model.SortingView.Companion.HOLDER_ADD
import com.anytypeio.anytype.presentation.sets.model.SortingView.Companion.HOLDER_APPLY
import com.anytypeio.anytype.presentation.sets.model.SortingView.Companion.HOLDER_SET
import com.google.android.material.chip.Chip

class SortByAdapter(
    private var items: List<SortingView> = listOf(),
    private var click: (SortClick) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun update(newItems: List<SortingView>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            HOLDER_SET -> {
                ItemHolder(
                    ItemViewerSortSetBinding.inflate(
                        inflater, parent, false
                    )
                )
            }
            HOLDER_ADD -> {
                AddItemHolder(
                    ItemViewerSortAddBinding.inflate(
                        inflater, parent, false
                    )
                )
            }
            HOLDER_APPLY -> {
                ButtonHolder(
                    ItemViewerSortApplyBinding.inflate(
                        inflater, parent, false
                    )
                )
            }
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ItemHolder -> {
                holder.bind(items[position] as SortingView.Set, click)
            }
            is AddItemHolder -> {
                if (items.any { it is SortingView.Set }) {
                    holder.bind(click)
                } else {
                    holder.bindAsSingleButton(click)
                }
            }
            is ButtonHolder -> {
                holder.bind(click)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is SortingView.Set -> HOLDER_SET
        SortingView.Apply -> HOLDER_APPLY
        SortingView.Add -> HOLDER_ADD
    }


    class AddItemHolder(val binding: ItemViewerSortAddBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(click: (SortClick) -> Unit) {
            itemView.setOnClickListener {
                click(SortClick.Add)
            }
        }

        fun bindAsSingleButton(click: (SortClick) -> Unit) {
            val context = itemView.context
            itemView.setOnClickListener {
                click(SortClick.Add)
            }
        }
    }

    class ButtonHolder(val binding: ItemViewerSortApplyBinding) : RecyclerView.ViewHolder(binding.root) {

        val button: TextView = binding.btnApply

        fun bind(click: (SortClick) -> Unit) {
            button.setOnClickListener {
                click(SortClick.Apply)
            }
        }
    }

    class ItemHolder(val binding: ItemViewerSortSetBinding) : RecyclerView.ViewHolder(binding.root) {

        val close: ImageView = binding.ivDelete
        private val prefix: Chip = binding.tvPrefix
        val key: Chip = binding.tvKey
        val type: Chip = binding.tvType

        fun bind(
            item: SortingView.Set,
            click: (SortClick) -> Unit
        ) {

            close.setOnClickListener {
                click(SortClick.Remove(item.key))
            }
            key.setOnClickListener {
                click(SortClick.ItemKey(item.key))
            }
            type.setOnClickListener {
                click(SortClick.ItemType(item))
            }

            key.text = item.title
            key.chipIcon = itemView.context.formatIcon(item.format)
            type.text = item.type.name

            if (item.isWithPrefix) {
                prefix.visible()
            } else {
                prefix.gone()
            }
        }
    }
}