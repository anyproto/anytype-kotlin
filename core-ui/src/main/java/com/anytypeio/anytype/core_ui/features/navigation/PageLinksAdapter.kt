package com.anytypeio.anytype.core_ui.features.navigation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemListObjectBinding
import com.anytypeio.anytype.core_ui.databinding.ItemSearchNewObjectBinding
import com.anytypeio.anytype.core_ui.widgets.ObjectIconWidget
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.setOnThrottleClickListener
import com.anytypeio.anytype.presentation.navigation.DefaultObjectView
import com.anytypeio.anytype.presentation.navigation.DefaultSearchItem
import com.anytypeio.anytype.presentation.navigation.NewObject
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.presentation.search.ObjectSearchSection
import com.anytypeio.anytype.presentation.widgets.source.BundledWidgetSourceView
import com.anytypeio.anytype.presentation.widgets.source.SuggestWidgetObjectType

class DefaultObjectViewAdapter(
    private val onDefaultObjectClicked: (DefaultObjectView) -> Unit,
    private val onBundledWidgetSourceClicked: (BundledWidgetSourceView) -> Unit = {},
    private val onCurrentListChanged: (Int, Int) -> Unit = { prevSize, newSize -> },
    private val onCreateNewObject: () -> Unit = {},
    private val onSuggestedWidgetObjectTypeClicked: (SuggestWidgetObjectType) -> Unit = {}
) : ListAdapter<DefaultSearchItem, DefaultObjectViewAdapter.ObjectViewHolder>(Differ) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ObjectViewHolder = when (viewType) {
        TYPE_ITEM -> ObjectItemViewHolder(inflate(parent, R.layout.item_list_object)).apply {
            itemView.setOnThrottleClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val item = getItem(pos)
                    if (item is DefaultObjectView) {
                        onDefaultObjectClicked(item)
                    }
                }
            }
        }
        TYPE_BUNDLED_WIDGET_SOURCE -> BundledWidgetSourceHolder(
            ItemListObjectBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        ).apply {
            itemView.setOnThrottleClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val item = getItem(pos)
                    if (item is BundledWidgetSourceView) {
                        onBundledWidgetSourceClicked(item)
                    }
                }
            }
        }
        TYPE_SECTION -> SectionViewHolder(inflate(parent, R.layout.item_object_search_section))
        TYPE_NEW_OBJECT -> NewObjectViewHolder(
            ItemSearchNewObjectBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        ).apply {
            itemView.setOnThrottleClickListener {
                onCreateNewObject()
            }
        }
        TYPE_SUGGESTED_WIDGET_OBJECT_TYPE -> SuggestWidgetObjectTypeViewHolder(
            ItemListObjectBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        ).apply {
            itemView.setOnThrottleClickListener {
                val pos = bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val item = getItem(pos)
                    if (item is SuggestWidgetObjectType) {
                        onSuggestedWidgetObjectTypeClicked(item)
                    }
                }
            }
        }
        else -> throw IllegalStateException("Unexpected view type: $viewType")
    }

    override fun onBindViewHolder(holder: ObjectViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is ObjectItemViewHolder -> {
                check(item is DefaultObjectView)
                holder.bind(item)
            }
            is SectionViewHolder -> {
                check(item is ObjectSearchSection)
                when(item) {
                    ObjectSearchSection.RecentlyOpened -> {
                        holder.title.setText(R.string.object_search_recently_opened_section_title)
                    }
                    ObjectSearchSection.SelectWidgetSource.FromLibrary -> {
                        holder.title.setText(R.string.widget_source_anytype_library)
                    }
                    ObjectSearchSection.SelectWidgetSource.FromMyObjects -> {
                        holder.title.setText(R.string.your_objects)
                    }
                    ObjectSearchSection.SelectWidgetSource.System -> {
                        holder.title.setText(R.string.widget_source_system)
                    }
                    ObjectSearchSection.SelectWidgetSource.Suggested -> {
                        holder.title.setText(R.string.widget_source_suggested)
                    }
                }
            }
            is BundledWidgetSourceHolder -> {
                check(item is BundledWidgetSourceView)
                holder.bind(item)
            }
            is SuggestWidgetObjectTypeViewHolder -> {
                check(item is SuggestWidgetObjectType)
                holder.bind(item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int = when (val item = getItem(position)) {
        is DefaultObjectView -> TYPE_ITEM
        is ObjectSearchSection -> TYPE_SECTION
        is BundledWidgetSourceView -> TYPE_BUNDLED_WIDGET_SOURCE
        is NewObject -> TYPE_NEW_OBJECT
        is SuggestWidgetObjectType -> TYPE_SUGGESTED_WIDGET_OBJECT_TYPE
        else -> throw IllegalStateException("Unexpected item type: ${item.javaClass.name}")
    }

    override fun onCurrentListChanged(
        previousList: MutableList<DefaultSearchItem>,
        currentList: MutableList<DefaultSearchItem>
    ) {
        super.onCurrentListChanged(previousList, currentList)
        onCurrentListChanged(previousList.size, currentList.size)
    }

    open class ObjectViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    inner class SectionViewHolder(view: View) : ObjectViewHolder(view) {
        val title : TextView get() = view.findViewById(R.id.tvTitle)
    }

class ObjectItemViewHolder(view: View) : ObjectViewHolder(view) {

        private val title = itemView.findViewById<TextView>(R.id.tvTitle)
        private val subtitle = itemView.findViewById<TextView>(R.id.tvSubtitle)
        private val icon = itemView.findViewById<ObjectIconWidget>(R.id.ivIcon)

        fun bind(link: DefaultObjectView) {
            title.text = link.name
            if (link.typeName != null) {
                subtitle.visible()
                subtitle.text = link.typeName
            } else {
                subtitle.gone()
            }
            icon.setIcon(link.icon)
        }

        fun bindSelectDateItem() {
            title.setText(R.string.select_date)
            subtitle.gone()
            icon.setIcon(ObjectIcon.TypeIcon.Default.DATE)
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
        ): Boolean {
            return when {
                oldItem is DefaultObjectView && newItem is DefaultObjectView -> {
                    oldItem.id == newItem.id
                }
                oldItem is SuggestWidgetObjectType && newItem is SuggestWidgetObjectType -> {
                    oldItem.id == newItem.id
                }
                oldItem is BundledWidgetSourceView && newItem is BundledWidgetSourceView -> {
                    oldItem.id == newItem.id
                }
                else -> {
                    oldItem == newItem
                }
            }
        }

        override fun areContentsTheSame(
            oldItem: DefaultSearchItem,
            newItem: DefaultSearchItem
        ): Boolean {
            return when {
                oldItem is DefaultObjectView && newItem is DefaultObjectView -> {
                    oldItem == newItem
                }
                oldItem is SuggestWidgetObjectType && newItem is SuggestWidgetObjectType -> {
                    oldItem == newItem
                }
                oldItem is BundledWidgetSourceView && newItem is BundledWidgetSourceView -> {
                    oldItem == newItem
                }
                else -> false
            }
        }

    }
}

class BundledWidgetSourceHolder(
    private val binding: ItemListObjectBinding
) : DefaultObjectViewAdapter.ObjectViewHolder(binding.root) {

    init {
        binding.ivIcon.binding.emojiContainer.invisible()
    }

    fun bind(item: BundledWidgetSourceView) {
        when (item) {
            BundledWidgetSourceView.Favorites -> {
                with(binding) {
                    tvTitle.setText(R.string.favorites)
                    tvSubtitle.gone()
                    ivIcon.setBackgroundResource(R.drawable.ic_widget_system_favorites)
                }
            }
            BundledWidgetSourceView.Recent -> {
                with(binding) {
                    tvTitle.setText(R.string.recent)
                    tvSubtitle.gone()
                    ivIcon.setBackgroundResource(R.drawable.ic_widget_system_recently_edited,)
                }
            }
            BundledWidgetSourceView.RecentLocal -> {
                with(binding) {
                    tvTitle.setText(R.string.recently_opened)
                    tvSubtitle.visible()
                    tvSubtitle.setText(R.string.on_this_device)
                    ivIcon.setBackgroundResource(R.drawable.ic_widget_system_recently_opened)
                }
            }
            BundledWidgetSourceView.Bin -> {
                with(binding) {
                    tvTitle.setText(R.string.bin)
                    tvSubtitle.gone()
                    ivIcon.setBackgroundResource(R.drawable.ic_widget_system_bin)
                }
            }
            BundledWidgetSourceView.AllObjects -> {
                with(binding) {
                    tvTitle.setText(R.string.all_content)
                    tvSubtitle.gone()
                    ivIcon.setBackgroundResource(R.drawable.ic_widget_system_all_objects)
                }
            }
            BundledWidgetSourceView.Chat -> {
                with(binding) {
                    tvTitle.setText(R.string.chat)
                    tvSubtitle.gone()
                    ivIcon.setBackgroundResource(R.drawable.ic_widget_system_chat)
                }
            }
        }
    }
}

class SuggestWidgetObjectTypeViewHolder(
    private val binding: ItemListObjectBinding
) : DefaultObjectViewAdapter.ObjectViewHolder(binding.root) {

    init {
        binding.tvSubtitle.gone()
        binding.ivIcon.binding.emojiContainer.background = null

    }

    fun bind(source: SuggestWidgetObjectType) {
        binding.tvTitle.text = source.name
        binding.ivIcon.setIcon(source.objectIcon)
    }
}

class NewObjectViewHolder(
    binding: ItemSearchNewObjectBinding
) :  DefaultObjectViewAdapter.ObjectViewHolder(binding.root)

private const val TYPE_ITEM = 0
private const val TYPE_SECTION = 1
private const val TYPE_BUNDLED_WIDGET_SOURCE = 2
private const val TYPE_NEW_OBJECT = 3
private const val TYPE_SUGGESTED_WIDGET_OBJECT_TYPE = 4