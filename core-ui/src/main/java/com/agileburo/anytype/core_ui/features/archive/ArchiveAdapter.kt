package com.agileburo.anytype.core_ui.features.archive

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.features.editor.holders.other.Page
import com.agileburo.anytype.core_ui.features.editor.holders.other.Title
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.BlockViewDiffUtil
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_ARCHIVE_TITLE
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_PAGE
import com.agileburo.anytype.core_ui.features.page.ListenerType
import com.agileburo.anytype.core_utils.ext.typeOf
import timber.log.Timber

class ArchiveAdapter(
    private var blocks: List<BlockView>,
    private val onClickListener: (ListenerType) -> Unit
) : RecyclerView.Adapter<BlockViewHolder>() {

    val views: List<BlockView> get() = blocks

    fun update(items: List<BlockView>) {
        logDataSetUpdateEvent(items)
        val result = DiffUtil.calculateDiff(BlockViewDiffUtil(old = blocks, new = items))
        blocks = items
        result.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            HOLDER_ARCHIVE_TITLE -> {
                Title.Archive(
                    view = inflater.inflate(
                        R.layout.item_block_title,
                        parent,
                        false
                    )
                )
            }
            HOLDER_PAGE -> {
                Page(
                    view = inflater.inflate(
                        R.layout.item_block_page,
                        parent,
                        false
                    )
                )
            }
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int = blocks[position].getViewType()

    override fun onBindViewHolder(holder: BlockViewHolder, position: Int) {
        when (holder) {
            is Page -> {
                holder.bind(
                    item = blocks[position] as BlockView.Page,
                    clicked = onClickListener
                )
            }
            is Title.Archive -> {
                holder.apply {
                    bind(
                        item = blocks[position] as BlockView.Title.Archive,
                        onTitleTextChanged = {},
                        onFocusChanged = { _, _ -> }
                    )
                }
            }
        }
    }

    override fun onBindViewHolder(
        holder: BlockViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            when (holder) {
                is Page -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position]
                    )
                }
                is Title.Document -> {
                    holder.processPayloads(
                        payloads = payloads.typeOf(),
                        item = blocks[position] as BlockView.Title
                    )
                }
                else -> throw IllegalStateException("Unexpected view holder: $holder")
            }
        }
    }

    override fun getItemCount(): Int = blocks.size

    private fun logDataSetUpdateEvent(items: List<BlockView>) {
        Timber.d("----------Updating------------")
        items.forEach { Timber.d(it.toString()) }
        Timber.d("----------Finished------------")
    }
}