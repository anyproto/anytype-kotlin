package com.anytypeio.anytype.core_ui.widgets.dv

import android.content.Context
import android.text.SpannableString
import android.text.style.LeadingMarginSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.*
import com.anytypeio.anytype.core_ui.layout.SpacingItemDecoration
import com.anytypeio.anytype.core_ui.widgets.ObjectIconWidget
import com.anytypeio.anytype.core_utils.ext.containsItemDecoration
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_utils.ui.GalleryViewItemDecoration
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.sets.model.Viewer

class GalleryViewWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : RecyclerView(context, attrs) {

    private val galleryViewAdapter = GalleryViewAdapter(
        onGalleryItemClicked = { id ->
            onGalleryItemClicked(id)
        },
        onTaskCheckboxClicked = { id ->
            onTaskCheckboxClicked(id)
        }
    )

    var onGalleryItemClicked: (Id) -> Unit = {}
    var onTaskCheckboxClicked: (Id) -> Unit = {}

    private val lm = GridLayoutManager(context, SMALL_CARDS_COLUMN_COUNT)
    private val smallCardsItemDecoration = GalleryViewItemDecoration(
        spacing = resources.getDimension(R.dimen.dp_10).toInt()
    )
    private val largeCardsItemDecoration = SpacingItemDecoration(
        spacingStart = resources.getDimension(R.dimen.dp_10).toInt(),
        spacingEnd = resources.getDimension(R.dimen.dp_10).toInt(),
        spacingBottom = resources.getDimension(R.dimen.dp_10).toInt()
    )

    init {
        adapter = galleryViewAdapter
        addItemDecoration(smallCardsItemDecoration)
        layoutManager = lm
    }

    fun clear() {
        galleryViewAdapter.submitList(emptyList())
    }

    fun setViews(
        views: List<Viewer.GalleryView.Item>,
        largeCards: Boolean = false
    ) {
        galleryViewAdapter.submitList(views)
        setupCardSize(largeCards)
    }

    private fun setupCardSize(largeCards: Boolean) {
        if (largeCards) {
            if (lm.spanCount != LARGE_CARDS_COLUMN_COUNT) {
                lm.spanCount = LARGE_CARDS_COLUMN_COUNT
                if (containsItemDecoration(smallCardsItemDecoration)) {
                    removeItemDecoration(smallCardsItemDecoration)
                }
                addItemDecoration(largeCardsItemDecoration)
            }
        } else {
            if (lm.spanCount != SMALL_CARDS_COLUMN_COUNT) {
                lm.spanCount = SMALL_CARDS_COLUMN_COUNT
                if (containsItemDecoration(largeCardsItemDecoration)) {
                    removeItemDecoration(largeCardsItemDecoration)
                }
                addItemDecoration(smallCardsItemDecoration)
            }
        }
    }

    class GalleryViewAdapter(
        private val onGalleryItemClicked: (Id) -> Unit,
        private val onTaskCheckboxClicked: (Id) -> Unit
    ) : ListAdapter<Viewer.GalleryView.Item, GalleryViewHolder>(Differ) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            when (viewType) {
                VIEW_TYPE_WITH_COVER -> {
                    return GalleryViewHolder.WithCover(
                        binding = ItemDvGalleryItemCoverBinding.inflate(
                            inflater, parent, false
                        )
                    ).apply {
                        setClicks()
                    }
                }
                VIEW_TYPE_DEFAULT -> {
                    return GalleryViewHolder.Default(
                        binding = ItemDvGalleryViewDefaultBinding.inflate(
                            inflater, parent, false
                        )
                    ).apply {
                        setClicks()
                    }
                }
                else -> throw RuntimeException("Unsupported view type")
            }
        }

        private fun GalleryViewHolder.setClicks() {
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != NO_POSITION) {
                    onGalleryItemClicked(getItem(pos).objectId)
                }
            }
            checkboxView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != NO_POSITION) {
                    val item = getItem(pos)
                    if (item.icon is ObjectIcon.Task) {
                        onTaskCheckboxClicked(item.objectId)
                    }
                }
            }
        }

        override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
            when (holder) {
                is GalleryViewHolder.Default -> {
                    holder.bind(getItem(position) as Viewer.GalleryView.Item.Default)
                }
                is GalleryViewHolder.WithCover -> {
                    holder.bind(getItem(position) as Viewer.GalleryView.Item.Cover)
                }
            }
        }

        override fun getItemViewType(position: Int): Int = when (val item = getItem(position)) {
            is Viewer.GalleryView.Item.Cover -> VIEW_TYPE_WITH_COVER
            is Viewer.GalleryView.Item.Default -> VIEW_TYPE_DEFAULT
        }
    }

    sealed class GalleryViewHolder(view: View) : ViewHolder(view) {

        private val untitled = itemView.resources.getString(R.string.untitled)
        private val firstLineMargin =
            itemView.resources.getDimensionPixelOffset(R.dimen.default_dv_gallery_first_line_margin_start)
        abstract val iconView: ObjectIconWidget
        abstract val title: TextView
        abstract val contentContainer: GalleryViewContentWidget
        abstract val checkboxView: View

        class Default(val binding: ItemDvGalleryViewDefaultBinding) :
            GalleryViewHolder(binding.root) {

            override val title = binding.tvTitle
            override val iconView = binding.cardIcon
            override val contentContainer = binding.contentContainer
            override val checkboxView = binding.cardIcon.checkbox

            fun bind(item: Viewer.GalleryView.Item.Default) {
                applyTextAndIcon(item)
                applyContentItems(item)
            }

            fun processChangePayload(
                payload: List<Int>,
                item: Viewer.GalleryView.Item.Default
            ) {
                payload(payload, item)
                if (payload.contains(CONTENT_CHANGED)) {
                    setupHolderHeight(item = item)
                    applyContentItems(item)
                }
            }

            private fun setupHolderHeight(item: Viewer.GalleryView.Item.Default) {
                itemView.updateLayoutParams<GridLayoutManager.LayoutParams> {
                    height = calculateHolderHeight(
                        withCover = false,
                        relationsSize = item.relations.size
                    )
                }
            }
        }

        class WithCover(val binding: ItemDvGalleryItemCoverBinding) :
            GalleryViewHolder(binding.root) {

            override val title = binding.tvTitle
            override val iconView = binding.cardIcon
            override val contentContainer = binding.contentContainer
            private val cover get() = binding.cover
            override val checkboxView = binding.cardIcon.checkbox

            fun bind(item: Viewer.GalleryView.Item.Cover) {
                setupHolderHeight(item = item)
                applyTextAndIcon(item)
                applyContentItems(item)
                cover.bind(item = item)
            }

            fun processChangePayload(
                payload: List<Int>,
                item: Viewer.GalleryView.Item.Cover
            ) {
                payload(payload, item)
                if (
                    payload.contains(COVER_CHANGED) ||
                    payload.contains(FIT_IMAGE_CHANGED) ||
                    payload.contains(LARGE_SIZE_CHANGED)
                ) {
                    cover.bind(item = item)
                }
                if (payload.contains(CONTENT_CHANGED)) {
                    setupHolderHeight(item = item)
                    applyContentItems(item)
                }
            }

            private fun setupHolderHeight(item: Viewer.GalleryView.Item.Cover) {
                itemView.updateLayoutParams<GridLayoutManager.LayoutParams> {
                    if (item.isLargeSize) {
                        height = GridLayoutManager.LayoutParams.WRAP_CONTENT
                        binding.rootConstraint.setPadding(0,0,0, dimen(R.dimen.dp_16))
                    } else {
                        height = calculateHolderHeight(
                            withCover = true,
                            relationsSize = item.relations.size
                        )
                    }
                }
            }
        }

        fun calculateHolderHeight(withCover: Boolean, relationsSize: Int): Int {
            var itemHeight = 0
            itemHeight += if (withCover) {
                dimen(R.dimen.dv_gallery_cover_height) + dimen(R.dimen.dp_12) + dimen(R.dimen.dv_gallery_title_min_height)
            } else {
                dimen(R.dimen.dp_16) + dimen(R.dimen.dv_gallery_title_min_height)
            }
            if (relationsSize > 0) {
                itemHeight += dimen(R.dimen.dv_gallery_relation_height) * relationsSize + dimen(R.dimen.dv_gallery_relation_margin_top) * relationsSize
            }
            itemHeight += dimen(R.dimen.dp_12)
            return itemHeight
        }

        protected fun payload(
            payload: List<Int>,
            item: Viewer.GalleryView.Item
        ) {
            if (payload.contains(TEXT_ICON_CHANGED)) {
                applyTextAndIcon(item)
            }
        }

        protected fun applyContentItems(item: Viewer.GalleryView.Item) {
            contentContainer.setItems(item.relations)
        }

        protected fun applyTextAndIcon(item: Viewer.GalleryView.Item) {
            if (!item.hideIcon && item.icon != ObjectIcon.None) {
                iconView.visible()
                iconView.setIcon(item.icon)
                val sb = SpannableString(item.name.ifEmpty { untitled })
                sb.setSpan(
                    LeadingMarginSpan.Standard(firstLineMargin, 0), 0, sb.length, 0
                )
                title.text = sb
            } else {
                iconView.gone()
                title.text = when {
                    item.name.isEmpty() -> SpannableString(untitled)
                    else -> SpannableString(item.name)
                }
            }
        }
    }

    object Differ : DiffUtil.ItemCallback<Viewer.GalleryView.Item>() {
        override fun areItemsTheSame(
            oldItem: Viewer.GalleryView.Item,
            newItem: Viewer.GalleryView.Item
        ): Boolean {
            return newItem.objectId == oldItem.objectId
        }

        override fun areContentsTheSame(
            oldItem: Viewer.GalleryView.Item,
            newItem: Viewer.GalleryView.Item
        ): Boolean = oldItem == newItem

        override fun getChangePayload(
            oldItem: Viewer.GalleryView.Item,
            newItem: Viewer.GalleryView.Item
        ): Any? {

            if (oldItem::class != newItem::class) {
                return super.getChangePayload(oldItem, newItem)
            }

            val changes = mutableListOf<Int>()

            if (oldItem.name != newItem.name
                || oldItem.icon != newItem.icon
                || oldItem.hideIcon != newItem.hideIcon
            ) {
                changes.add(TEXT_ICON_CHANGED)
            }

            if (oldItem.relations != newItem.relations) {
                changes.add(CONTENT_CHANGED)
            }

            if (oldItem is Viewer.GalleryView.Item.Cover && newItem is Viewer.GalleryView.Item.Cover) {
                if (oldItem.cover != newItem.cover) {
                    changes.add(COVER_CHANGED)
                }
                if (oldItem.fitImage != newItem.fitImage) {
                    changes.add(FIT_IMAGE_CHANGED)
                }
                if (oldItem.isLargeSize != newItem.isLargeSize) {
                    changes.add(LARGE_SIZE_CHANGED)
                }
            }

            return changes.ifEmpty {
                super.getChangePayload(oldItem, newItem)
            }
        }
    }

    companion object {
        const val SMALL_CARDS_COLUMN_COUNT = 2
        const val LARGE_CARDS_COLUMN_COUNT = 1

        const val VIEW_TYPE_DEFAULT = 0
        const val VIEW_TYPE_WITH_COVER = 1

        const val TEXT_ICON_CHANGED = 0
        const val CONTENT_CHANGED = 1
        const val COVER_CHANGED = 2
        const val FIT_IMAGE_CHANGED = 3
        const val LARGE_SIZE_CHANGED = 4
    }
}