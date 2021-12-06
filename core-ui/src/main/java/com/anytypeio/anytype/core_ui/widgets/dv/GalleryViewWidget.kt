package com.anytypeio.anytype.core_ui.widgets.dv

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.layout.SpacingItemDecoration
import com.anytypeio.anytype.core_utils.ext.containsItemDecoration
import com.anytypeio.anytype.core_utils.ui.GalleryViewItemDecoration
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.sets.model.Viewer
import kotlinx.android.synthetic.main.item_dv_gallery_view_big_icon.view.*
import kotlinx.android.synthetic.main.item_dv_gallery_view_default.view.contentContainer
import kotlinx.android.synthetic.main.item_dv_gallery_view_default.view.tvTitle
import kotlinx.android.synthetic.main.item_dv_gallery_view_small_icon.view.*
import kotlinx.android.synthetic.main.item_dv_gallery_view_with_cover.view.*

class GalleryViewWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : RecyclerView(context, attrs) {

    private val galleryViewAdapter = GalleryViewAdapter(
        onGalleryItemClicked = { id ->
            onGalleryItemClicked(id)
        }
    )

    var onGalleryItemClicked: (Id) -> Unit = {}

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
        private val onGalleryItemClicked: (Id) -> Unit
    ) : ListAdapter<Viewer.GalleryView.Item, GalleryViewHolder>(Differ) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
            when (viewType) {
                VIEW_TYPE_SMALL_ICON -> {
                    return GalleryViewHolder.SmallIcon(parent).apply {
                        itemView.setOnClickListener {
                            val pos = bindingAdapterPosition
                            if (pos != NO_POSITION) {
                                val item = getItem(pos)
                                if (item is Viewer.GalleryView.Item) {
                                    onGalleryItemClicked(item.objectId)
                                }
                            }
                        }
                    }
                }
                VIEW_TYPE_BIG_ICON -> {
                    return GalleryViewHolder.BigIcon(parent).apply {
                        itemView.setOnClickListener {
                            val pos = bindingAdapterPosition
                            if (pos != NO_POSITION) {
                                val item = getItem(pos)
                                if (item is Viewer.GalleryView.Item) {
                                    onGalleryItemClicked(item.objectId)
                                }
                            }
                        }
                    }
                }
                VIEW_TYPE_WITH_COVER -> {
                    return GalleryViewHolder.WithCover(parent).apply {
                        itemView.setOnClickListener {
                            val pos = bindingAdapterPosition
                            if (pos != NO_POSITION) {
                                val item = getItem(pos)
                                if (item is Viewer.GalleryView.Item) {
                                    onGalleryItemClicked(item.objectId)
                                }
                            }
                        }
                    }
                }
                VIEW_TYPE_WITH_COVER_BIG -> {
                    return GalleryViewHolder.WithCoverBig(parent).apply {
                        itemView.setOnClickListener {
                            val pos = bindingAdapterPosition
                            if (pos != NO_POSITION) {
                                val item = getItem(pos)
                                if (item is Viewer.GalleryView.Item) {
                                    onGalleryItemClicked(item.objectId)
                                }
                            }
                        }
                    }
                }
                VIEW_TYPE_WITH_COVER_AND_ICON -> {
                    return GalleryViewHolder.WithCoverAndIcon(parent).apply {
                        itemView.setOnClickListener {
                            val pos = bindingAdapterPosition
                            if (pos != NO_POSITION) {
                                val item = getItem(pos)
                                if (item is Viewer.GalleryView.Item) {
                                    onGalleryItemClicked(item.objectId)
                                }
                            }
                        }
                    }
                }
                else -> {
                    return GalleryViewHolder.Default(parent).apply {
                        itemView.setOnClickListener {
                            val pos = bindingAdapterPosition
                            if (pos != NO_POSITION) {
                                val item = getItem(pos)
                                if (item is Viewer.GalleryView.Item) {
                                    onGalleryItemClicked(item.objectId)
                                }
                            }
                        }
                    }
                }
            }
        }

        override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
            when (holder) {
                is GalleryViewHolder.Default -> {
                    holder.bind(getItem(position))
                }
                is GalleryViewHolder.SmallIcon -> {
                    holder.bind(getItem(position))
                }
                is GalleryViewHolder.BigIcon -> {
                    holder.bind(getItem(position))
                }
                is GalleryViewHolder.WithCover -> {
                    holder.bind(getItem(position))
                }
                is GalleryViewHolder.WithCoverBig -> {
                    holder.bind(getItem(position))
                }
                is GalleryViewHolder.WithCoverAndIcon -> {
                    holder.bind(getItem(position))
                }
            }
        }

        override fun getItemViewType(position: Int): Int {
            return when (val item = getItem(position)) {
                is Viewer.GalleryView.Item.Cover -> {
                    when {
                        item.isCoverLarge -> VIEW_TYPE_WITH_COVER_BIG
                        item.hideIcon -> {
                            VIEW_TYPE_WITH_COVER
                        }
                        item.icon is ObjectIcon.None || item.icon is ObjectIcon.Basic.Avatar -> {
                            VIEW_TYPE_WITH_COVER
                        }
                        else -> {
                            VIEW_TYPE_WITH_COVER_AND_ICON
                        }
                    }
                }
                is Viewer.GalleryView.Item.Default -> {
                    when {
                        item.icon is ObjectIcon.Basic.Avatar -> {
                            VIEW_TYPE_DEFAULT
                        }
                        item.icon != ObjectIcon.None -> {
                            when {
                                item.hideIcon -> {
                                    VIEW_TYPE_DEFAULT
                                }
                                item.bigIcon -> {
                                    VIEW_TYPE_BIG_ICON
                                }
                                else -> {
                                    VIEW_TYPE_SMALL_ICON
                                }
                            }
                        }
                        else -> {
                            VIEW_TYPE_DEFAULT
                        }
                    }
                }
            }
        }
    }

    sealed class GalleryViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        class Default(parent: ViewGroup) : GalleryViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_dv_gallery_view_default,
                parent,
                false
            )
        ) {

            private val container get() = itemView.contentContainer
            private val title get() = itemView.tvTitle

            fun bind(item: Viewer.GalleryView.Item) {
                title.text = item.name
                container.setItems(item.relations)
            }
        }

        class SmallIcon(parent: ViewGroup) : GalleryViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_dv_gallery_view_small_icon,
                parent,
                false
            )
        ) {

            private val container get() = itemView.contentContainer
            private val title get() = itemView.tvTitle
            private val icon get() = itemView.smallIconContainer

            fun bind(item: Viewer.GalleryView.Item) {
                title.text = item.name
                container.setItems(item.relations)
                icon.bind(item.icon)
            }
        }

        class BigIcon(parent: ViewGroup) : GalleryViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_dv_gallery_view_big_icon,
                parent,
                false
            )
        ) {

            private val container get() = itemView.contentContainer
            private val title get() = itemView.tvTitle
            private val icon get() = itemView.objectIcon

            fun bind(item: Viewer.GalleryView.Item) {
                title.text = item.name
                container.setItems(item.relations)
                icon.setIcon(item.icon)
            }
        }

        class WithCover(parent: ViewGroup) : GalleryViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_dv_gallery_view_with_cover,
                parent,
                false
            )
        ) {

            private val container get() = itemView.contentContainer
            private val title get() = itemView.tvTitle
            private val cover get() = itemView.cover

            fun bind(item: Viewer.GalleryView.Item) {
                check(item is Viewer.GalleryView.Item.Cover)
                title.text = item.name
                container.setItems(item.relations)
                cover.bind(cover = item.cover, fitImage = item.fitImage)
            }
        }

        class WithCoverBig(parent: ViewGroup) : GalleryViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_dv_gallery_view_with_cover_big,
                parent,
                false
            )
        ) {

            private val container get() = itemView.contentContainer
            private val title get() = itemView.tvTitle
            private val cover get() = itemView.cover

            fun bind(item: Viewer.GalleryView.Item) {
                check(item is Viewer.GalleryView.Item.Cover)
                title.text = item.name
                container.setItems(item.relations)
                cover.bind(cover = item.cover, fitImage = item.fitImage)
            }
        }

        class WithCoverAndIcon(parent: ViewGroup) : GalleryViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_dv_gallery_view_with_cover_and_icon,
                parent,
                false
            )
        ) {

            private val container get() = itemView.contentContainer
            private val title get() = itemView.tvTitle
            private val cover get() = itemView.cover
            private val icon get() = itemView.smallIconContainer

            fun bind(item: Viewer.GalleryView.Item) {
                check(item is Viewer.GalleryView.Item.Cover)
                title.text = item.name
                container.setItems(item.relations)
                cover.bind(cover = item.cover, fitImage = item.fitImage)
                icon.bind(item.icon)
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
        ): Boolean {
            // TODO
            return false
        }
    }

    companion object {
        const val SMALL_CARDS_COLUMN_COUNT = 2
        const val LARGE_CARDS_COLUMN_COUNT = 1

        const val VIEW_TYPE_DEFAULT = 0
        const val VIEW_TYPE_SMALL_ICON = 1
        const val VIEW_TYPE_BIG_ICON = 2
        const val VIEW_TYPE_WITH_COVER = 3
        const val VIEW_TYPE_WITH_COVER_BIG = 4
        const val VIEW_TYPE_WITH_COVER_AND_ICON = 5
    }
}