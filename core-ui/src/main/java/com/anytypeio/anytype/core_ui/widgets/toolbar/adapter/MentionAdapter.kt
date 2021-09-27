package com.anytypeio.anytype.core_ui.widgets.toolbar.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.navigation.DefaultObjectViewAdapter
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView

class MentionAdapter(
    private var data: ArrayList<DefaultObjectView>,
    private var mentionFilter: String = "",
    private val onClicked: (DefaultObjectView, String) -> Unit,
    private val newClicked: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun setData(mentions: List<DefaultObjectView>) {
        if (mentions.isEmpty()) {
            data.clear()
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
        notifyItemRangeRemoved(0, size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_NEW_PAGE -> NewPageViewHolder(
                inflater.inflate(R.layout.item_mention_new_page, parent, false)
            ).apply {
                itemView.setOnClickListener {
                    newClicked(mentionFilter)
                }
            }
            TYPE_MENTION ->
                DefaultObjectViewAdapter.ObjectViewHolder(
                    inflater.inflate(R.layout.item_list_object_small, parent, false)
                ).apply {
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            onClicked(data[pos - 1], mentionFilter)
                        }
                    }
                }
            else -> throw RuntimeException("Wrong viewType")
        }
    }

    override fun getItemCount(): Int = data.size + 1

    override fun getItemViewType(position: Int): Int = when (position) {
        POSITION_NEW_PAGE -> TYPE_NEW_PAGE
        else -> TYPE_MENTION
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DefaultObjectViewAdapter.ObjectViewHolder) {
            holder.bind(data[position - 1])
        }
    }

    class NewPageViewHolder(view: View) : RecyclerView.ViewHolder(view)

    companion object {
        const val POSITION_NEW_PAGE = 0
        const val TYPE_NEW_PAGE = 1
        const val TYPE_MENTION = 2
    }
}