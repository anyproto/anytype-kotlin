package com.anytypeio.anytype.core_ui.widgets.dv

import android.content.Context
import android.text.SpannableString
import android.text.style.LeadingMarginSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relations
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
                        binding.titleDescContainer.updateLayoutParams<ConstraintLayout.LayoutParams> {
                            height = dimen(R.dimen.default_dv_gallery_title_height)
                        }
                        setClicks()
                    }
                }
                VIEW_TYPE_WITH_COVER_DESC -> {
                    return GalleryViewHolder.WithCover(
                        binding = ItemDvGalleryItemCoverBinding.inflate(
                            inflater, parent, false
                        )
                    ).apply {
                        binding.titleDescContainer.updateLayoutParams<ConstraintLayout.LayoutParams> {
                            height = dimen(R.dimen.default_dv_gallery_title_desc_height)
                        }
                        setClicks()
                    }
                }
                VIEW_TYPE_DEFAULT -> {
                    return GalleryViewHolder.Default(
                        binding = ItemDvGalleryViewDefaultBinding.inflate(
                            inflater, parent, false
                        )
                    ).apply {
                        binding.titleDescContainer.updateLayoutParams<ConstraintLayout.LayoutParams> {
                            height = dimen(R.dimen.default_dv_gallery_title_height)
                        }
                        setClicks()
                    }
                }
                VIEW_TYPE_DEFAULT_DESC -> {
                    return GalleryViewHolder.Default(
                        binding = ItemDvGalleryViewDefaultBinding.inflate(
                            inflater, parent, false
                        )
                    ).apply {
                        binding.titleDescContainer.updateLayoutParams<ConstraintLayout.LayoutParams> {
                            height = dimen(R.dimen.default_dv_gallery_title_desc_height)
                        }
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
            is Viewer.GalleryView.Item.Cover -> {
                if (item.withDescription) VIEW_TYPE_WITH_COVER_DESC else VIEW_TYPE_WITH_COVER
            }
            is Viewer.GalleryView.Item.Default -> {
                if (item.withDescription) VIEW_TYPE_DEFAULT_DESC else VIEW_TYPE_DEFAULT
            }
        }
    }

    sealed class GalleryViewHolder(view: View) : ViewHolder(view) {

        private val untitled = itemView.resources.getString(R.string.untitled)
        private val firstLineMargin =
            itemView.resources.getDimensionPixelOffset(R.dimen.default_dv_gallery_first_line_margin_start)
        abstract val iconView: ObjectIconWidget
        abstract val titleDescContainer: GalleryViewTitleDescriptionWidget
        abstract val contentContainer: GalleryViewContentWidget
        abstract val checkboxView: View

        class Default(val binding: ItemDvGalleryViewDefaultBinding) :
            GalleryViewHolder(binding.root) {

            override val titleDescContainer = binding.titleDescContainer
            override val iconView = binding.cardIcon
            override val contentContainer = binding.contentContainer
            override val checkboxView = binding.cardIcon.checkbox

            fun bind(item: Viewer.GalleryView.Item.Default) {
                applyTextAndIcon(item)
                titleDescContainer.setupDescription(item = item, space = null)
                applyContentItems(item)
            }

            fun processChangePayload(
                payload: List<Int>,
                item: Viewer.GalleryView.Item
            ) {
                payload(payload, item)
            }
        }

        class WithCover(val binding: ItemDvGalleryItemCoverBinding) :
            GalleryViewHolder(binding.root) {

            override val titleDescContainer = binding.titleDescContainer
            override val iconView = binding.cardIcon
            override val contentContainer = binding.contentContainer
            private val cover get() = binding.cover
            override val checkboxView = binding.cardIcon.checkbox

            fun bind(item: Viewer.GalleryView.Item.Cover) {
                applyTextAndIcon(item)
                titleDescContainer.setupDescription(item = item, space = binding.titleDescContainerSpace)
                applyContentItems(item)
                cover.bind(cover = item.cover, fitImage = item.fitImage)
                updateConstraints(item = item)
            }

            fun processChangePayload(
                payload: List<Int>,
                item: Viewer.GalleryView.Item.Cover
            ) {
                payload(payload, item)
                if (payload.contains(COVER_CHANGED) || payload.contains(FIT_IMAGE_CHANGED)) {
                    cover.bind(cover = item.cover, fitImage = item.fitImage)
                    updateConstraints(item = item)
                }
            }

            private fun updateConstraints(item: Viewer.GalleryView.Item.Cover) {
                if (item.cover == null) {
                    updateViewConstraints(
                        parentLayout = binding.rootConstraint,
                        view = titleDescContainer,
                        toParent = true
                    )
                    updateViewConstraints(
                        parentLayout = binding.rootConstraint,
                        view = iconView,
                        toParent = true
                    )
                    binding.titleDescContainer.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        topMargin = dimen(R.dimen.dp_16)
                    }
                    binding.cardIcon.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        topMargin = dimen(R.dimen.dp_17)
                    }
                } else {
                    updateViewConstraints(
                        parentLayout = binding.rootConstraint,
                        view = titleDescContainer,
                        toParent = false
                    )
                    updateViewConstraints(
                        parentLayout = binding.rootConstraint,
                        view = iconView,
                        toParent = false
                    )
                    binding.titleDescContainer.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        topMargin = dimen(R.dimen.dp_12)
                    }
                    binding.cardIcon.updateLayoutParams<ConstraintLayout.LayoutParams> {
                        topMargin = dimen(R.dimen.dp_13)
                    }
                }
            }

            private fun updateViewConstraints(
                parentLayout: ConstraintLayout,
                view: View,
                toParent: Boolean
            ) {
                val constraintSet = ConstraintSet()
                constraintSet.clone(parentLayout)
                if (toParent) {
                    constraintSet.connect(
                        view.id,
                        ConstraintLayout.LayoutParams.TOP,
                        parentLayout.id,
                        ConstraintLayout.LayoutParams.TOP
                    )
                } else {
                    constraintSet.connect(
                        view.id,
                        ConstraintLayout.LayoutParams.TOP,
                        R.id.cover,
                        ConstraintLayout.LayoutParams.BOTTOM
                    )
                }
                constraintSet.applyTo(parentLayout)
            }
        }

        protected fun payload(
            payload: List<Int>,
            item: Viewer.GalleryView.Item
        ) {
            if (payload.contains(TEXT_ICON_CHANGED)) {
                applyTextAndIcon(item)
            }
            if (payload.contains(CONTENT_CHANGED)) {
                applyContentItems(item)
            }
        }

        protected fun applyContentItems(item: Viewer.GalleryView.Item) {
            val filtered = item.relations.filter { it.relationKey != Relations.DESCRIPTION }
            contentContainer.setItems(filtered)
        }

        protected fun applyTextAndIcon(item: Viewer.GalleryView.Item) {
            if (!item.hideIcon && item.icon != ObjectIcon.None) {
                iconView.visible()
                iconView.setIcon(item.icon)
                val sb = SpannableString(item.name.ifEmpty { untitled })
                sb.setSpan(
                    LeadingMarginSpan.Standard(firstLineMargin, 0), 0, sb.length, 0
                )
                titleDescContainer.setupTitle(sb)
            } else {
                iconView.gone()
                when {
                    item.name.isEmpty() -> titleDescContainer.setupTitle(SpannableString(untitled))
                    else -> titleDescContainer.setupTitle(SpannableString(item.name))
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
        const val VIEW_TYPE_WITH_COVER = 3

        const val VIEW_TYPE_DEFAULT_DESC = 10
        const val VIEW_TYPE_WITH_COVER_DESC = 13

        const val TEXT_ICON_CHANGED = 0
        const val CONTENT_CHANGED = 2

        const val COVER_CHANGED = 4
        const val FIT_IMAGE_CHANGED = 5
    }
}