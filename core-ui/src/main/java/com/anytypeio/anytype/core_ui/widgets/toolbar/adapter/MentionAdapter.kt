package com.anytypeio.anytype.core_ui.widgets.toolbar.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.navigation.DefaultObjectViewAdapter
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.presentation.page.editor.mention.filterMentionsBy

class MentionAdapter(
    private var data: ArrayList<DefaultObjectView>,
    private var mentionFilter: String = "",
    private val onClicked: (DefaultObjectView, String) -> Unit,
    private val newClicked: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val filteredData = mutableListOf<DefaultObjectView>()

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
        filteredData.clear()
        val filteredMentions = data.filterMentionsBy(text = mentionFilter)
        filteredData.addAll(filteredMentions)
        notifyDataSetChanged()
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
                    inflater.inflate(R.layout.item_list_object, parent, false)
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

    override fun getItemCount(): Int = filteredData.size + 1

    override fun getItemViewType(position: Int): Int = when (position) {
        POSITION_NEW_PAGE -> TYPE_NEW_PAGE
        else -> TYPE_MENTION
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DefaultObjectViewAdapter.ObjectViewHolder) {
            holder.bind(filteredData[position - 1])
        }
    }

    class NewPageViewHolder(view: View) : RecyclerView.ViewHolder(view)

    companion object {
        const val POSITION_NEW_PAGE = 0
        const val TYPE_NEW_PAGE = 1
        const val TYPE_MENTION = 2
    }
}