package com.anytypeio.anytype.core_ui.widgets.toolbar.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemMentionNewPageBinding
import com.anytypeio.anytype.core_ui.databinding.ItemMentionSectionDateBinding
import com.anytypeio.anytype.core_ui.databinding.ItemMentionSectionObjectsBinding
import com.anytypeio.anytype.core_ui.features.navigation.DefaultObjectViewAdapter.Differ
import com.anytypeio.anytype.core_ui.features.navigation.DefaultObjectViewAdapter.ObjectItemViewHolder
import com.anytypeio.anytype.core_ui.features.navigation.DefaultObjectViewAdapter.ObjectViewHolder
import com.anytypeio.anytype.presentation.editor.editor.mention.MentionConst.MENTION_PREFIX
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.presentation.navigation.DefaultSearchItem
import com.anytypeio.anytype.presentation.navigation.NewObject
import com.anytypeio.anytype.presentation.navigation.SectionDates
import com.anytypeio.anytype.presentation.navigation.SectionObjects
import com.anytypeio.anytype.presentation.navigation.SelectDateItem

class MentionAdapter(
    private var mentionFilter: String = "",
    private val onClicked: (DefaultSearchItem, String, Int) -> Unit,
    private val newClicked: (String) -> Unit,
    private val onCurrentListChanged: (Int, Int) -> Unit = { prevSize, newSize -> },
) : ListAdapter<DefaultSearchItem, ObjectViewHolder>(Differ) {

    fun updateFilter(filter: String) {
        mentionFilter = filter
        val newPagePosition = currentList.indexOfFirst { it is NewObject }
        if (newPagePosition != -1) {
            notifyItemChanged(newPagePosition)
        }
    }

    fun clear() {
        submitList(emptyList())
        mentionFilter = ""
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObjectViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_NEW_PAGE -> NewPageViewHolder(
                binding = ItemMentionNewPageBinding.inflate(inflater, parent, false)
            ).apply {
                itemView.setOnClickListener {
                    newClicked(mentionFilter)
                }
            }
            TYPE_MENTION, TYPE_SELECT_DATE ->
                ObjectItemViewHolder(
                    view = inflater.inflate(R.layout.item_list_object_small, parent, false)
                ).apply {
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            val item = getItem(pos)
                            onClicked(item, mentionFilter, pos)
                        }
                    }
                }
            TYPE_SECTION_DATES -> SectionDatesViewHolder(
                binding = ItemMentionSectionDateBinding.inflate(inflater, parent, false)
            )
            TYPE_SECTION_OBJECTS -> SectionObjectsViewHolder(
                binding = ItemMentionSectionObjectsBinding.inflate(inflater, parent, false)
            )
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun onCurrentListChanged(
        previousList: MutableList<DefaultSearchItem>,
        currentList: MutableList<DefaultSearchItem>
    ) {
        super.onCurrentListChanged(previousList, currentList)
        onCurrentListChanged(previousList.size, currentList.size)
    }

    override fun getItemViewType(position: Int): Int = when (val item = getItem(position)) {
        is DefaultObjectView -> TYPE_MENTION
        is SectionDates -> TYPE_SECTION_DATES
        is SectionObjects -> TYPE_SECTION_OBJECTS
        is NewObject -> TYPE_NEW_PAGE
        is SelectDateItem -> TYPE_SELECT_DATE
        else -> throw IllegalStateException("Unexpected item type: ${item.javaClass.name}")
    }

    override fun onBindViewHolder(holder: ObjectViewHolder, position: Int) {
        val item = getItem(position)
        if (holder is ObjectItemViewHolder && item is DefaultObjectView) {
            holder.bind(item)
        }
        if (holder is NewPageViewHolder) {
            holder.bind(filter = mentionFilter.removePrefix(MENTION_PREFIX))
        }
        if (holder is ObjectItemViewHolder && item is SelectDateItem) {
            holder.bindSelectDateItem()
        }
    }
    class NewPageViewHolder(binding: ItemMentionNewPageBinding) :
        ObjectViewHolder(binding.root) {

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

    class SectionDatesViewHolder(binding: ItemMentionSectionDateBinding) :
        ObjectViewHolder(binding.root)

    class SectionObjectsViewHolder(binding: ItemMentionSectionObjectsBinding) :
        ObjectViewHolder(binding.root)

    companion object {
        const val TYPE_NEW_PAGE = 1
        const val TYPE_MENTION = 2
        const val TYPE_SECTION_DATES = 3
        const val TYPE_SECTION_OBJECTS = 4
        const val TYPE_SELECT_DATE = 5
    }
}