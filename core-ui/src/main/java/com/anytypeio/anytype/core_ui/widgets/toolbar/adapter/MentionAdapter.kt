package com.anytypeio.anytype.core_ui.widgets.toolbar.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemMentionNewPageBinding
import com.anytypeio.anytype.core_ui.features.navigation.DefaultObjectViewAdapter.ObjectItemViewHolder
import com.anytypeio.anytype.presentation.editor.editor.mention.MentionConst.MENTION_PREFIX
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView

class MentionAdapter(
    private var data: ArrayList<DefaultObjectView>,
    private var mentionFilter: String = "",
    private val onClicked: (DefaultObjectView, String, Int) -> Unit,
    private val newClicked: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun setData(mentions: List<DefaultObjectView>) {
        if (mentions.isEmpty()) {
            data.clear()
            notifyDataSetChanged()
        } else {
            data.clear()
            data.addAll(mentions)
            notifyDataSetChanged()
        }
    }

    fun updateFilter(filter: String) {
        mentionFilter = filter
    }

    fun clear() {
        mentionFilter = ""
        val size = data.size
        data.clear()
        notifyItemRangeRemoved(0, size + 1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_NEW_PAGE -> NewPageViewHolder(
                binding = ItemMentionNewPageBinding.inflate(inflater, parent, false)
            ).apply {
                itemView.setOnClickListener {
                    newClicked(mentionFilter)
                }
            }
            TYPE_MENTION ->
                ObjectItemViewHolder(
                    view = inflater.inflate(R.layout.item_list_object_small, parent, false)
                ).apply {
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            onClicked(data[pos], mentionFilter, pos)
                        }
                    }
                }
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun getItemCount(): Int = data.size + 1

    override fun getItemViewType(position: Int): Int {
        return if (position > data.lastIndex) TYPE_NEW_PAGE else TYPE_MENTION
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ObjectItemViewHolder) {
            holder.bind(data[position])
        }
        if (holder is NewPageViewHolder) {
            holder.bind(filter = mentionFilter.removePrefix(MENTION_PREFIX))
        }
    }

    class NewPageViewHolder(binding: ItemMentionNewPageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val tvTitle = binding.text

        fun bind(filter: String) {
            val res = itemView.resources
            if (filter.isEmpty()) {
                tvTitle.text = res.getString(R.string.mention_suggester_new_page)
            } else {
                tvTitle.text =
                    "${res.getString(R.string.mention_suggester_create_object)} \"$filter\""
            }
        }

    }

    companion object {
        const val TYPE_NEW_PAGE = 1
        const val TYPE_MENTION = 2
    }
}