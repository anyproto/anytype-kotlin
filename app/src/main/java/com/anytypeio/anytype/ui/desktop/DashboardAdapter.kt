package com.anytypeio.anytype.ui.desktop

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_ui.tools.SupportDragAndDropBehavior
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.shift
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.desktop.DashboardView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.android.synthetic.main.item_desktop_archive.view.*
import kotlinx.android.synthetic.main.item_desktop_page.view.*
import timber.log.Timber

class DashboardAdapter(
    private var data: List<DashboardView>,
    private val onDocumentClicked: (Id, Boolean) -> Unit,
    private val onArchiveClicked: (Id) -> Unit,
    private val onObjectSetClicked: (Id) -> Unit
) : RecyclerView.Adapter<DashboardAdapter.ViewHolder>(), SupportDragAndDropBehavior {

    companion object {
        const val VIEW_TYPE_DOCUMENT = 0
        const val VIEW_TYPE_DOCUMENT_WITHOUT_ICON = 1
        const val VIEW_TYPE_ARCHIVE = 2
        const val VIEW_TYPE_SET = 3

        const val UNEXPECTED_TYPE_ERROR_MESSAGE = "Unexpected type"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_DOCUMENT -> {
                ViewHolder.DocumentHolder(
                    inflater.inflate(R.layout.item_desktop_page, parent, false)
                ).apply {
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            val item = data[pos]
                            check(item is DashboardView.Document)
                            onDocumentClicked(item.target, item.isLoading)
                        }
                    }
                }
            }
            VIEW_TYPE_DOCUMENT_WITHOUT_ICON -> {
                ViewHolder.DocumentWithoutIconViewHolder(parent).apply {
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            val item = data[pos]
                            check(item is DashboardView.Document)
                            onDocumentClicked(item.target, item.isLoading)
                        }
                    }
                }
            }
            VIEW_TYPE_ARCHIVE -> {
                ViewHolder.ArchiveHolder(
                    inflater.inflate(R.layout.item_desktop_archive, parent, false)
                ).apply {
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            val item = data[pos]
                            check(item is DashboardView.Archive)
                            onArchiveClicked(item.target)
                        }
                    }
                }
            }
            VIEW_TYPE_SET -> {
                ViewHolder.ObjectSetHolder(
                    inflater.inflate(R.layout.item_desktop_object_set, parent, false)
                ).apply {
                    itemView.setOnClickListener {
                        val pos = bindingAdapterPosition
                        if (pos != RecyclerView.NO_POSITION) {
                            val item = data[pos]
                            check(item is DashboardView.ObjectSet)
                            onObjectSetClicked(item.target)
                        }
                    }
                }
            }
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (val item = data[position]) {
            is DashboardView.Document -> {
                if (item.hasIcon || item.layout == ObjectType.Layout.PROFILE)
                    VIEW_TYPE_DOCUMENT
                else
                    VIEW_TYPE_DOCUMENT_WITHOUT_ICON
            }
            is DashboardView.Archive -> VIEW_TYPE_ARCHIVE
            is DashboardView.ObjectSet -> VIEW_TYPE_SET
            else -> throw IllegalStateException("$UNEXPECTED_TYPE_ERROR_MESSAGE:\n$item")
        }
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.DocumentHolder -> {
                val item = data[position] as DashboardView.Document
                with(holder) {
                    bindTitle(item.title)
                    bindSubtitle(item.typeName)
                    bindEmoji(item.emoji)
                    bindImage(item.image, item.layout, item.title)
                    bindLoading(item.isLoading)
                }
            }
            is ViewHolder.DocumentWithoutIconViewHolder -> {
                val item = data[position] as DashboardView.Document
                holder.bindTitle(item.title)
                holder.bindSubtitle(item.typeName)
                holder.bindLoading(item.isLoading)
            }
            is ViewHolder.ObjectSetHolder -> {
                with(holder) {
                    val item = data[position] as DashboardView.ObjectSet
                    bindTitle(item.title)
                    bindEmoji(item.emoji)
                }
            }
            is ViewHolder.ArchiveHolder -> {
                with(holder) {
                    val item = data[position] as DashboardView.Archive
                    bindTitle(item.title)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            payloads.typeOf<DesktopDiffUtil.Payload>().forEach { payload ->
                when (holder) {
                    is ViewHolder.ArchiveHolder -> {
                        bindByPayloads(holder, position, payload)
                    }
                    is ViewHolder.DocumentHolder -> {
                        bindByPayloads(holder, position, payload)
                    }
                    is ViewHolder.DocumentWithoutIconViewHolder -> {
                        bindByPayloads(holder, position, payload)
                    }
                    else -> Timber.d("Skipping payload update.")
                }
            }
        }
    }

    private fun bindByPayloads(
        holder: ViewHolder.ArchiveHolder,
        position: Int,
        payload: DesktopDiffUtil.Payload
    ) {
        with(holder) {
            val item = data[position] as DashboardView.Archive
            if (payload.titleChanged()) {
                bindTitle(item.title)
            }
        }
    }

    private fun bindByPayloads(
        holder: ViewHolder.DocumentHolder,
        position: Int,
        payload: DesktopDiffUtil.Payload
    ) {
        with(holder) {
            val item = data[position] as DashboardView.Document
            if (payload.titleChanged()) {
                bindTitle(item.title)
            }
            if (payload.emojiChanged()) {
                bindEmoji(item.emoji)
            }
            if (payload.imageChanged()) {
                bindImage(item.image, item.layout, item.title)
            }
            if (payload.isLoadingChanged) {
                bindLoading(item.isLoading)
            }
        }
    }

    private fun bindByPayloads(
        holder: ViewHolder.DocumentWithoutIconViewHolder,
        position: Int,
        payload: DesktopDiffUtil.Payload
    ) {
        with(holder) {
            val item = data[position] as DashboardView.Document
            if (payload.titleChanged()) {
                bindTitle(item.title)
            }
            if (payload.isLoadingChanged) {
                bindLoading(item.isLoading)
            }
        }
    }

    sealed class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        class ArchiveHolder(itemView: View) : ViewHolder(itemView) {

            fun bindTitle(title: String) {
                if (title.isNotEmpty()) {
                    itemView.archiveTitle.text = title
                }
            }
        }

        class DocumentHolder(itemView: View) : ViewHolder(itemView) {

            private val tvTitle = itemView.title
            private val tvSubtitle = itemView.typeTitle
            private val ivEmoji = itemView.emojiIcon
            private val circleImage = itemView.circleImage
            private val rectangleImage = itemView.rectangleImage
            private val avatar = itemView.avatar
            private val shimmer = itemView.shimmer

            fun bindTitle(title: String?) {
                if (title.isNullOrEmpty())
                    tvTitle.setText(R.string.untitled)
                else
                    tvTitle.text = title
            }

            fun bindSubtitle(subtitle: String?) {
                tvSubtitle.text = subtitle
            }

            fun bindLoading(isLoading: Boolean) {
                if (isLoading) {
                    tvTitle.invisible()
                    shimmer.startShimmer()
                    shimmer.visible()
                } else {
                    shimmer.stopShimmer()
                    shimmer.invisible()
                    tvTitle.visible()
                }
            }

            fun bindEmoji(emoji: String?) {
                try {
                    emoji?.let { unicode ->
                        Glide
                            .with(ivEmoji)
                            .load(Emojifier.uri(unicode))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(ivEmoji)
                    } ?: run {
                        ivEmoji.setImageDrawable(null)
                    }
                } catch (e: Throwable) {
                    Timber.e(e, "Could not set emoji icon")
                }
            }

            fun bindImage(
                image: String?,
                layout: ObjectType.Layout?,
                name: String?
            ) {
                when (layout) {
                    ObjectType.Layout.BASIC -> bindRectangleImage(image)
                    ObjectType.Layout.PROFILE -> {
                        if (image != null) {
                            avatar.invisible()
                            bindCircleImage(image)
                        } else {
                            rectangleImage.invisible()
                            avatar.visible()
                            avatar.bind(name.orEmpty())
                        }
                    }
                    else -> Timber.d("Skipping image bound")
                }
            }

            private fun bindCircleImage(image: String?) {
                image?.let { url ->
                    Glide
                        .with(circleImage)
                        .load(url)
                        .centerInside()
                        .circleCrop()
                        .into(circleImage)
                } ?: run { circleImage.setImageDrawable(null) }
            }

            private fun bindRectangleImage(image: String?) {
                Timber.d("Binding rectangle image: $image")
                image?.let { url ->
                    Glide
                        .with(rectangleImage)
                        .load(url)
                        .centerCrop()
                        .into(rectangleImage)
                } ?: run { rectangleImage.setImageDrawable(null) }
            }
        }

        class DocumentWithoutIconViewHolder(parent: ViewGroup) : ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_desktop_page_without_icon,
                parent,
                false
            )
        ) {

            private val tvTitle = itemView.findViewById<TextView>(R.id.tvDocTitle)
            private val tvSubtitle = itemView.findViewById<TextView>(R.id.tvDocTypeName)
            private val shimmer = itemView.findViewById<ShimmerFrameLayout>(R.id.shimmer)

            fun bindTitle(title: String?) {
                tvTitle.text = title
            }

            fun bindSubtitle(type: String?) {
                tvSubtitle.text = type
            }

            fun bindLoading(isLoading: Boolean) {
                if (isLoading) {
                    tvTitle.invisible()
                    tvSubtitle.invisible()
                    shimmer.startShimmer()
                    shimmer.visible()
                } else {
                    shimmer.stopShimmer()
                    shimmer.invisible()
                    tvTitle.visible()
                    tvSubtitle.visible()
                }
            }
        }

        class ObjectSetHolder(itemView: View) : ViewHolder(itemView) {

            private val tvTitle = itemView.title
            private val ivEmoji = itemView.emojiIcon

            fun bindTitle(title: String?) {
                if (title.isNullOrEmpty())
                    tvTitle.setText(R.string.untitled)
                else
                    tvTitle.text = title
            }

            fun bindEmoji(emoji: String?) {
                try {
                    emoji?.let { unicode ->
                        Glide
                            .with(ivEmoji)
                            .load(Emojifier.uri(unicode))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(ivEmoji)
                    }
                } catch (e: Throwable) {
                    Timber.e(e, "Could not set emoji icon")
                }
            }
        }
    }

    fun update(views: List<DashboardView>) {
        try {
            val old = ArrayList(data)
            val callback = DesktopDiffUtil(old = old, new = views)
            val result = DiffUtil.calculateDiff(callback)
            data = views
            result.dispatchUpdatesTo(this)
        } catch (e: Exception) {
            Timber.e(e, "Error while updating dashboard adapter")
        }
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        val update = ArrayList(data).shift(fromPosition, toPosition)
        data = update
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    fun provideAdapterData(): List<DashboardView> = data.toList()
}