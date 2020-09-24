package com.anytypeio.anytype.core_ui.features.page.models

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.page.*
import com.anytypeio.anytype.core_ui.features.page.holders.*
import com.anytypeio.anytype.core_ui.tools.*
import com.anytypeio.anytype.core_utils.ext.typeOf
import timber.log.Timber

class BlockTextAdapter(
    private var blocks: List<BlockView>,
    private val event: (BlockTextEvent) -> Unit,
    private val click: (ListenerType) -> Unit
) : RecyclerView.Adapter<BlockTextViewHolder>() {

    val views: List<BlockView> get() = blocks

    override fun getItemViewType(position: Int) = blocks[position].getViewType()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockTextViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            BlockViewHolder.HOLDER_PARAGRAPH -> {
                ParagraphViewHolder(
                    view = inflater.inflate(R.layout.item_block_text, parent, false),
                    textWatcher = BlockTextWatcher(),
                    mentionWatcher = BlockTextMentionWatcher(),
                    backspaceWatcher = BlockTextBackspaceWatcher(),
                    enterWatcher = BlockTextEnterWatcher(),
                    actionMenu = BlockTextMenu(ContextMenuType.TEXT)
                )
            }
            BlockViewHolder.HOLDER_CHECKBOX -> {
                CheckboxViewHolder(
                    view = inflater.inflate(R.layout.item_block_checkbox, parent, false),
                    textWatcher = BlockTextWatcher(),
                    mentionWatcher = BlockTextMentionWatcher(),
                    backspaceWatcher = BlockTextBackspaceWatcher(),
                    enterWatcher = BlockTextEnterWatcher(),
                    actionMenu = BlockTextMenu(ContextMenuType.TEXT)
                )
            }
            BlockViewHolder.HOLDER_HEADER_ONE -> {
                HeaderOneViewHolder(
                    view = inflater.inflate(R.layout.item_block_header_one, parent, false),
                    textWatcher = BlockTextWatcher(),
                    mentionWatcher = BlockTextMentionWatcher(),
                    backspaceWatcher = BlockTextBackspaceWatcher(),
                    enterWatcher = BlockTextEnterWatcher(),
                    actionMenu = BlockTextMenu(ContextMenuType.HEADER)
                )
            }
            BlockViewHolder.HOLDER_HEADER_TWO -> {
                HeaderTwoViewHolder(
                    view = inflater.inflate(R.layout.item_block_header_two, parent, false),
                    textWatcher = BlockTextWatcher(),
                    mentionWatcher = BlockTextMentionWatcher(),
                    backspaceWatcher = BlockTextBackspaceWatcher(),
                    enterWatcher = BlockTextEnterWatcher(),
                    actionMenu = BlockTextMenu(ContextMenuType.HEADER)
                )
            }
            BlockViewHolder.HOLDER_HEADER_THREE -> {
                HeaderThreeViewHolder(
                    view = inflater.inflate(R.layout.item_block_header_three, parent, false),
                    textWatcher = BlockTextWatcher(),
                    mentionWatcher = BlockTextMentionWatcher(),
                    backspaceWatcher = BlockTextBackspaceWatcher(),
                    enterWatcher = BlockTextEnterWatcher(),
                    actionMenu = BlockTextMenu(ContextMenuType.HEADER)
                )
            }
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun getItemCount(): Int = blocks.size

    override fun onBindViewHolder(holder: BlockTextViewHolder, position: Int) {

        val block = blocks[position]

        if (holder is Holder.Indentable && block is BlockView.Indentable) {
            holder.indentize(block.indent)
        }

        if (holder is Holder.Selectable && block is BlockView.Selectable) {
            holder.select(block.isSelected)
        }

        if (holder is Holder.Alignable && block is BlockView.Alignable) {
            holder.align(block.alignment)
        }
    }

    // Bug workaround for losing text selection ability, see:
    // https://code.google.com/p/android/issues/detail?id=208169
    override fun onViewAttachedToWindow(holder: BlockTextViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.content.isEnabled = false
        holder.content.isEnabled = true
    }

    fun updateWithDiffUtil(items: List<BlockView>) {
        logDataSetUpdateEvent(items)
        val result = DiffUtil.calculateDiff(BlockViewDiffUtil(old = blocks, new = items))
        blocks = items
        result.dispatchUpdatesTo(this)
    }

    private fun logDataSetUpdateEvent(items: List<BlockView>) {
        Timber.d("----------Updating------------")
        items.forEach { Timber.d(it.toString()) }
        Timber.d("----------Finished------------")
    }

    override fun onBindViewHolder(
        holder: BlockTextViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty())
            onBindViewHolder(holder, position)
        else {
            val block = blocks[position]
            if (block is Item) {
                holder.payload(
                    payloads = payloads.typeOf(),
                    clicked = click,
                    item = block
                )
            }
        }
    }
}