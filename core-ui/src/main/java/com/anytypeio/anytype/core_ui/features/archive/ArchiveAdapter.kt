package com.anytypeio.anytype.core_ui.features.archive

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.databinding.ItemBlockObjectLinkArchiveBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockTitleBinding
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.holders.other.LinkToObjectArchive
import com.anytypeio.anytype.core_ui.features.editor.holders.other.Title
import com.anytypeio.anytype.core_utils.ext.typeOf
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_ARCHIVE_TITLE
import com.anytypeio.anytype.presentation.editor.editor.model.types.Types.HOLDER_OBJECT_LINK_ARCHIVE
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
                    ItemBlockTitleBinding.inflate(inflater, parent, false)
                )
            }
            HOLDER_OBJECT_LINK_ARCHIVE -> {
                LinkToObjectArchive(
                    ItemBlockObjectLinkArchiveBinding.inflate(inflater, parent, false)
                )
            }
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int = blocks[position].getViewType()

    override fun onBindViewHolder(holder: BlockViewHolder, position: Int) {
        when (holder) {
            is LinkToObjectArchive -> {
                holder.bind(
                    item = blocks[position] as BlockView.LinkToObject.Archived,
                    clicked = onClickListener
                )
            }
            is Title.Archive -> {
                holder.apply {
                    bind(
                        item = blocks[position] as BlockView.Title.Archive
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
                is LinkToObjectArchive -> {
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