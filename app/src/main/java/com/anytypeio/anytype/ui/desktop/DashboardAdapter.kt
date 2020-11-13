package com.anytypeio.anytype.ui.desktop

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_ui.tools.SupportDragAndDropBehavior
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.shift
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.domain.common.Id
import com.anytypeio.anytype.emojifier.Emojifier
import com.anytypeio.anytype.presentation.desktop.DashboardView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.item_desktop_archive.view.*
import kotlinx.android.synthetic.main.item_desktop_page.view.*
import timber.log.Timber

class DashboardAdapter(
    private var data: List<DashboardView>,
    private val onDocumentClicked: (Id, Boolean) -> Unit,
    private val onArchiveClicked: (Id) -> Unit
) : RecyclerView.Adapter<DashboardAdapter.ViewHolder>(), SupportDragAndDropBehavior {

    companion object {
        const val VIEW_TYPE_DOCUMENT = 0
        const val VIEW_TYPE_ARCHIVE = 1
        const val UNEXPECTED_TYPE_ERROR_MESSAGE = "Unexpected type"
        const val EMPTY_EMOJI = ""
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_DOCUMENT -> {
                inflater.inflate(R.layout.item_desktop_page, parent, false).let {
                    ViewHolder.DocumentHolder(it)
                }
            }
            VIEW_TYPE_ARCHIVE -> {
                inflater.inflate(R.layout.item_desktop_archive, parent, false).let {
                    ViewHolder.ArchiveHolder(it)
                }
            }
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (data[position]) {
            is DashboardView.Document -> VIEW_TYPE_DOCUMENT
            is DashboardView.Archive -> VIEW_TYPE_ARCHIVE
            else -> throw IllegalStateException(UNEXPECTED_TYPE_ERROR_MESSAGE)
        }
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.DocumentHolder -> {
                with(holder) {
                    val item = data[position] as DashboardView.Document
                    bindClick(item.target) { id ->
                        val d = data[bindingAdapterPosition] as DashboardView.Document
                        onDocumentClicked(id, d.isLoading)
                    }
                    bindTitle(item.title)
                    bindEmoji(item.emoji)
                    bindImage(item.image)
                    bindLoading(item.isLoading)
                }
            }
            is ViewHolder.ArchiveHolder -> {
                with(holder) {
                    val item = data[position] as DashboardView.Archive
                    bindTitle(item.title)
                    bindClick(item.target, onArchiveClicked)
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
            if (payload.targetChanged()) {
                bindClick(item.target, onArchiveClicked)
            }
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
            if (payload.targetChanged()) {
                bindClick(item.target) { id ->
                    val d = data[bindingAdapterPosition] as DashboardView.Document
                    onDocumentClicked(id, d.isLoading)
                }
            }
            if (payload.titleChanged()) {
                bindTitle(item.title)
            }
            if (payload.emojiChanged()) {
                bindEmoji(item.emoji)
            }
            if (payload.imageChanged()) {
                bindImage(item.image)
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

            fun bindClick(
                target: Id,
                onClick: (Id) -> Unit
            ) {
                itemView.setOnClickListener { onClick(target) }
            }
        }

        class DocumentHolder(itemView: View) : ViewHolder(itemView) {

            private val tvTitle = itemView.title
            private val ivEmoji = itemView.emojiIcon
            private val ivImage = itemView.image
            private val shimmer = itemView.shimmer

            fun bindClick(
                target: Id,
                onClick: (Id) -> Unit
            ) {
                itemView.setOnClickListener { onClick(target) }
            }

            fun bindTitle(title: String?) {
                if (title.isNullOrEmpty())
                    tvTitle.setText(R.string.untitled)
                else
                    tvTitle.text = title
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
                    }
                } catch (e: Throwable) {
                    Timber.e(e, "Could not set emoji icon")
                }
            }

            fun bindImage(image: String?) {
                image?.let { url ->
                    Glide
                        .with(ivImage)
                        .load(url)
                        .centerInside()
                        .circleCrop()
                        .into(ivImage)
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