package com.anytypeio.anytype.core_ui.features.navigation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemListObjectBinding
import com.anytypeio.anytype.core_ui.widgets.ObjectIconWidget
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.navigation.DefaultSearchItem
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.presentation.navigation.ObjectView
import com.anytypeio.anytype.presentation.search.ObjectSearchSection

@Deprecated("LEGACY SUSPECT")
class PageLinksAdapter(
    private var data: List<ObjectView>,
    private val onClick: (Id, ObjectType.Layout?) -> Unit
) : RecyclerView.Adapter<PageLinksAdapter.PageLinkHolder>() {

    fun updateLinks(links: List<ObjectView>) {
        data = links
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PageLinkHolder = PageLinkHolder(
        binding = ItemListObjectBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    ).apply {
        itemView.setOnClickListener {
            val pos = bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                onClick(data[pos].id, data[pos].layout)
            }
        }
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holderLink: PageLinkHolder, position: Int) {
        holderLink.bind(data[position])
    }

    class PageLinkHolder(val binding: ItemListObjectBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val untitled = itemView.resources.getString(R.string.untitled)
        private val title = binding.tvTitle
        private val subtitle = binding.tvSubtitle
        private val icon = binding.ivIcon

        fun bind(link: ObjectView) {
            title.text = link.title.ifEmpty { untitled }
            if (link.subtitle.isBlank()) {
                subtitle.gone()
            } else {
                subtitle.visible()
                subtitle.text = link.subtitle
            }
            icon.setIcon(link.icon)
        }
    }
}

class DefaultObjectViewAdapter(
    private val onClick: (DefaultObjectView) -> Unit,
) : ListAdapter<DefaultSearchItem, DefaultObjectViewAdapter.ObjectViewHolder>(Differ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ObjectViewHolder {
        return when (viewType) {
            TYPE_ITEM -> ObjectItemViewHolder(inflate(parent, R.layout.item_list_object), onClick)
            TYPE_SECTION_RECENTLY_OPENED -> RecentlyOpenedHolder(inflate(parent, R.layout.item_search_section_recently_opened))
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: ObjectViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is ObjectItemViewHolder -> {
                check(item is DefaultObjectView)
                holder.bind(item)
            }
            else -> {}
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = getItem(position)) {
            is DefaultObjectView -> TYPE_ITEM
            is ObjectSearchSection.RecentlyOpened -> TYPE_SECTION_RECENTLY_OPENED
            else -> throw IllegalStateException("Unexpected item type: ${item.javaClass.name}")
        }
    }

    open class ObjectViewHolder(val view: View) : RecyclerView.ViewHolder(view)
    inner class RecentlyOpenedHolder(view: View) : ObjectViewHolder(view)

    inner class ObjectItemViewHolder(
        view: View,
        private val onClick: (DefaultObjectView) -> Unit
    ) : ObjectViewHolder(view) {

        private val title = itemView.findViewById<TextView>(R.id.tvTitle)
        private val subtitle = itemView.findViewById<TextView>(R.id.tvSubtitle)
        private val icon = itemView.findViewById<ObjectIconWidget>(R.id.ivIcon)

        fun bind(link: DefaultObjectView) {
            title.text = link.name
            subtitle.text = link.typeName
            icon.setIcon(link.icon)
            itemView.setOnClickListener {
                onClick.invoke(link)
            }
        }
    }

    private fun inflate(
        parent: ViewGroup,
        res: Int,
        attachToRoot: Boolean = false
    ): View = LayoutInflater.from(parent.context).inflate(
        res,
        parent,
        attachToRoot
    )

    object Differ : DiffUtil.ItemCallback<DefaultSearchItem>() {

        override fun areItemsTheSame(
            oldItem: DefaultSearchItem,
            newItem: DefaultSearchItem
        ): Boolean = (oldItem as? DefaultObjectView)?.id == (newItem as? DefaultObjectView)?.id

        override fun areContentsTheSame(
            oldItem: DefaultSearchItem,
            newItem: DefaultSearchItem
        ): Boolean = (oldItem as? DefaultObjectView) == (newItem as? DefaultObjectView)

    }

}

private const val TYPE_ITEM = 0
private const val TYPE_SECTION_RECENTLY_OPENED = 1