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
            return when (viewType) {
                VIEW_TYPE_ONLY_COVER -> GalleryViewHolder.OnlyCover(
                    ItemDvGalleryOnlyCoverBinding.inflate(inflater, parent, false)
                ).apply {
                    setClicks()
                }

                VIEW_TYPE_WITH_COVER -> GalleryViewHolder.WithCover(
                    ItemDvGalleryItemCoverBinding.inflate(inflater, parent, false)
                ).apply {
                    setClicks()
                }

                VIEW_TYPE_DEFAULT -> GalleryViewHolder.Default(
                    ItemDvGalleryViewDefaultBinding.inflate(inflater, parent, false)
                ).apply {
                    setClicks()
                }

                else -> throw IllegalArgumentException("Unsupported view type: $viewType")
            }
        }

        private fun GalleryViewHolder.setClicks() {
            itemView.setOnClickListener {
                val pos = bindingAdapterPosition
                if (pos != NO_POSITION) {
                    onGalleryItemClicked(getItem(pos).objectId)
                }
            }
            when (this) {
                is GalleryViewHolder.OnlyCover -> {
                    //do nothing, no checkbox in this view holder
                }

                is GalleryViewHolder.WithCover -> {
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

                is GalleryViewHolder.Default -> {
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
            }
        }

        override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
            when (holder) {
                is GalleryViewHolder.OnlyCover -> {
                    holder.bind(getItem(position) as Viewer.GalleryView.Item.Cover)
                }

                is GalleryViewHolder.WithCover -> {
                    holder.bind(getItem(position) as Viewer.GalleryView.Item.Cover)
                }

                is GalleryViewHolder.Default -> {
                    holder.bind(getItem(position) as Viewer.GalleryView.Item.Default)
                }
            }
        }

        override fun getItemViewType(position: Int): Int {
            val item = getItem(position)
            return when (item) {
                is Viewer.GalleryView.Item.Cover -> {
                    val showName = !item.hideName
                    if (!showName && item.relations.isEmpty()) {
                        VIEW_TYPE_ONLY_COVER
                    } else {
                        VIEW_TYPE_WITH_COVER
                    }
                }

                is Viewer.GalleryView.Item.Default -> VIEW_TYPE_DEFAULT
            }
        }
    }

    sealed class GalleryViewHolder(view: View) : ViewHolder(view) {

        private val untitled = itemView.resources.getString(R.string.untitled)
        private val firstLineMargin =
            itemView.resources.getDimensionPixelOffset(R.dimen.default_dv_gallery_first_line_margin_start)

        class Default(val binding: ItemDvGalleryViewDefaultBinding) :
            GalleryViewHolder(binding.root) {

            val title = binding.rootDvItemContent.tvTitle
            val iconView = binding.rootDvItemContent.cardIcon
            val contentContainer = binding.rootDvItemContent.contentContainer
            val checkboxView = binding.rootDvItemContent.cardIcon.checkbox

            fun bind(item: Viewer.GalleryView.Item.Default) {
                applyTextAndIcon(item, title, iconView)
                applyContentItems(item, contentContainer)
            }

            fun processChangePayload(
                payload: List<Int>,
                item: Viewer.GalleryView.Item.Default
            ) {
                payload(payload, item, title, iconView)
                if (payload.contains(CONTENT_CHANGED)) {
                    applyContentItems(item, contentContainer)
                }
            }
        }

        class WithCover(val binding: ItemDvGalleryItemCoverBinding) :
            GalleryViewHolder(binding.root) {

            val title = binding.rootConstraint.tvTitle
            val iconView = binding.rootConstraint.cardIcon
            val contentContainer = binding.rootConstraint.contentContainer
            val cover get() = binding.cover
            val checkboxView = binding.rootConstraint.cardIcon.checkbox

            fun bind(item: Viewer.GalleryView.Item.Cover) {
                applyTextAndIcon(item, title, iconView)
                applyContentItems(item, contentContainer)
                cover.bind(item = item)
            }

            fun processChangePayload(
                payload: List<Int>,
                item: Viewer.GalleryView.Item.Cover
            ) {
                payload(payload, item, title, iconView)
                if (
                    payload.contains(COVER_CHANGED) ||
                    payload.contains(FIT_IMAGE_CHANGED) ||
                    payload.contains(LARGE_SIZE_CHANGED)
                ) {
                    cover.bind(item = item)
                }
                if (payload.contains(CONTENT_CHANGED)) {
                    applyContentItems(item, contentContainer)
                }
            }
        }

        class OnlyCover(val binding: ItemDvGalleryOnlyCoverBinding) :
            GalleryViewHolder(binding.root) {

            val cover get() = binding.cover

            fun bind(item: Viewer.GalleryView.Item.Cover) {
                cover.bind(item)
            }

            fun processChangePayload(
                payload: List<Int>,
                item: Viewer.GalleryView.Item.Cover
            ) {
                if (
                    payload.contains(COVER_CHANGED) ||
                    payload.contains(FIT_IMAGE_CHANGED) ||
                    payload.contains(LARGE_SIZE_CHANGED)
                ) {
                    cover.bind(item = item)
                }
            }
        }

        protected fun payload(
            payload: List<Int>,
            item: Viewer.GalleryView.Item,
            title: TextView,
            iconView: ObjectIconWidget
        ) {
            if (payload.contains(TEXT_ICON_CHANGED)) {
                applyTextAndIcon(item, title, iconView)
            }
        }

        protected fun applyContentItems(
            item: Viewer.GalleryView.Item,
            contentContainer: GalleryViewContentWidget
        ) {
            if (item.relations.isEmpty()) {
                contentContainer.gone()
                return
            } else {
                contentContainer.visible()
                contentContainer.setItems(item.relations)
            }
        }

        /**
         * Refactored: Simplified name/icon visibility and styling logic
         */
        protected fun applyTextAndIcon(
            item: Viewer.GalleryView.Item,
            title: TextView,
            iconView: ObjectIconWidget
        ) {
            val name = item.name.ifEmpty { untitled }
            val showName = !item.hideName
            val showIcon = showName && !item.hideIcon && item.icon != ObjectIcon.None

            // Title visibility and text
            if (showName) {
                title.visible()
                val titleText = if (showIcon) {
                    SpannableString(name).apply {
                        setSpan(
                            LeadingMarginSpan.Standard(firstLineMargin, 0),
                            0, length, 0
                        )
                    }
                } else {
                    SpannableString(name)
                }
                title.text = titleText
            } else {
                title.gone()
            }

            // Icon visibility
            if (showIcon) {
                iconView.visible()
                iconView.setIcon(item.icon)
                iconView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = dimen(R.dimen.dp_12)
                }
            } else {
                iconView.gone()
                iconView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = 0
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
        const val VIEW_TYPE_ONLY_COVER = 2

        const val TEXT_ICON_CHANGED = 0
        const val CONTENT_CHANGED = 1
        const val COVER_CHANGED = 2
        const val FIT_IMAGE_CHANGED = 3
        const val LARGE_SIZE_CHANGED = 4
    }
}