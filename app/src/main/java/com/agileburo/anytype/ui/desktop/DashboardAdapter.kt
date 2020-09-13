package com.agileburo.anytype.ui.desktop

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.R
import com.agileburo.anytype.core_ui.tools.SupportDragAndDropBehavior
import com.agileburo.anytype.core_utils.ext.shift
import com.agileburo.anytype.emojifier.Emojifier
import com.agileburo.anytype.presentation.desktop.DashboardView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.item_desktop_archive.view.*
import kotlinx.android.synthetic.main.item_desktop_page.view.*
import timber.log.Timber

class DashboardAdapter(
    private var data: MutableList<DashboardView>,
    private val onDocumentClicked: (DashboardView.Document) -> Unit,
    private val onArchiveClicked: (DashboardView.Archive) -> Unit
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
                holder.bind(
                    doc = data[position] as DashboardView.Document,
                    onClick = onDocumentClicked
                )
            }
            is ViewHolder.ArchiveHolder -> {
                holder.bind(
                    archive = data[position] as DashboardView.Archive,
                    onClick = onArchiveClicked
                )
            }
        }
    }

    sealed class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        class ArchiveHolder(itemView: View) : ViewHolder(itemView) {

            fun bind(
                archive: DashboardView.Archive,
                onClick: (DashboardView.Archive) -> Unit
            ) {
                if (archive.text.isNotEmpty()) {
                    itemView.archiveTitle.text = archive.text
                }
                itemView.setOnClickListener { onClick(archive) }
            }
        }

        class DocumentHolder(itemView: View) : ViewHolder(itemView) {

            private val title = itemView.title
            private val emoji = itemView.emojiIcon
            private val image = itemView.image

            fun bind(
                doc: DashboardView.Document,
                onClick: (DashboardView.Document) -> Unit
            ) {
                itemView.setOnClickListener { onClick(doc) }

                if (doc.title.isNullOrEmpty())
                    title.setText(R.string.untitled)
                else
                    title.text = doc.title

                try {
                    doc.emoji?.let { unicode ->
                        Glide
                            .with(emoji)
                            .load(Emojifier.uri(unicode))
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(emoji)
                    }
                } catch (e: Throwable) {
                    Timber.e(e, "Could not set emoji icon")
                }

                doc.image?.let { url ->
                    Glide
                        .with(image)
                        .load(url)
                        .centerInside()
                        .circleCrop()
                        .into(image)
                }
            }
        }
    }

    fun update(views: List<DashboardView>) {
        val callback = DesktopDiffUtil(
            old = data,
            new = views
        )
        val result = DiffUtil.calculateDiff(callback)

        data.apply {
            clear()
            addAll(views)
        }

        result.dispatchUpdatesTo(this)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        val update = data.shift(fromPosition, toPosition)
        data.clear()
        data.addAll(update)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    fun provideAdapterData(): List<DashboardView> = data.toList()
}