package com.agileburo.anytype.core_ui.features.page

import android.text.Editable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_BOOKMARK
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_BULLET
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_CHECKBOX
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_CODE_SNIPPET
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_CONTACT
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_DIVIDER
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_FILE
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_HEADER_ONE
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_HEADER_THREE
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_HEADER_TWO
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_HIGHLIGHT
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_NUMBERED
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_PAGE
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_PARAGRAPH
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_PICTURE
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_TASK
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_TITLE
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_TOGGLE
import com.agileburo.anytype.core_utils.ext.typeOf
import timber.log.Timber

/**
 * Adapter for rendering list of blocks.
 * @property blocks mutable list of blocks
 * @see BlockView
 * @see BlockViewHolder
 * @see BlockViewDiffUtil
 */
class BlockAdapter(
    private var blocks: List<BlockView>,
    private val onTextChanged: (String, Editable) -> Unit,
    private val onSelectionChanged: (String, IntRange) -> Unit,
    private val onCheckboxClicked: (String) -> Unit,
    private val onFocusChanged: (String, Boolean) -> Unit,
    private val onEmptyBlockBackspaceClicked: (String) -> Unit,
    private val onNonEmptyBlockBackspaceClicked: (String) -> Unit,
    private val onSplitLineEnterClicked: (String) -> Unit,
    private val onEndLineEnterClicked: (String, Editable) -> Unit
) : RecyclerView.Adapter<BlockViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            HOLDER_PARAGRAPH -> {
                BlockViewHolder.Paragraph(
                    view = inflater.inflate(
                        R.layout.item_block_text,
                        parent,
                        false
                    )
                )
            }
            HOLDER_TITLE -> {
                BlockViewHolder.Title(
                    view = inflater.inflate(
                        R.layout.item_block_title,
                        parent,
                        false
                    )
                )
            }
            HOLDER_HEADER_ONE -> {
                BlockViewHolder.HeaderOne(
                    view = inflater.inflate(
                        R.layout.item_block_header_one,
                        parent,
                        false
                    )
                )
            }
            HOLDER_HEADER_TWO -> {
                BlockViewHolder.HeaderTwo(
                    view = inflater.inflate(
                        R.layout.item_block_header_two,
                        parent,
                        false
                    )
                )
            }
            HOLDER_HEADER_THREE -> {
                BlockViewHolder.HeaderThree(
                    view = inflater.inflate(
                        R.layout.item_block_header_three,
                        parent,
                        false
                    )
                )
            }
            HOLDER_CODE_SNIPPET -> {
                BlockViewHolder.Code(
                    view = inflater.inflate(
                        R.layout.item_block_code_snippet,
                        parent,
                        false
                    )
                )
            }
            HOLDER_CHECKBOX -> {
                BlockViewHolder.Checkbox(
                    view = inflater.inflate(
                        R.layout.item_block_checkbox,
                        parent,
                        false
                    )
                )
            }
            HOLDER_TASK -> {
                BlockViewHolder.Task(
                    view = inflater.inflate(
                        R.layout.item_block_task,
                        parent,
                        false
                    )
                )
            }
            HOLDER_BULLET -> {
                BlockViewHolder.Bulleted(
                    view = inflater.inflate(
                        R.layout.item_block_bulleted,
                        parent,
                        false
                    )
                )
            }
            HOLDER_NUMBERED -> {
                BlockViewHolder.Numbered(
                    view = inflater.inflate(
                        R.layout.item_block_numbered,
                        parent,
                        false
                    )
                )
            }
            HOLDER_TOGGLE -> {
                BlockViewHolder.Toggle(
                    view = inflater.inflate(
                        R.layout.item_block_toggle,
                        parent,
                        false
                    )
                )
            }
            HOLDER_CONTACT -> {
                BlockViewHolder.Contact(
                    view = inflater.inflate(
                        R.layout.item_block_contact,
                        parent,
                        false
                    )
                )
            }
            HOLDER_FILE -> {
                BlockViewHolder.File(
                    view = inflater.inflate(
                        R.layout.item_block_file,
                        parent,
                        false
                    )
                )
            }
            HOLDER_PAGE -> {
                BlockViewHolder.Page(
                    view = inflater.inflate(
                        R.layout.item_block_page,
                        parent,
                        false
                    )
                )
            }
            HOLDER_BOOKMARK -> {
                BlockViewHolder.Bookmark(
                    view = inflater.inflate(
                        R.layout.item_block_bookmark,
                        parent,
                        false
                    )
                )
            }
            HOLDER_PICTURE -> {
                BlockViewHolder.Picture(
                    view = inflater.inflate(
                        R.layout.item_block_picture,
                        parent,
                        false
                    )
                )
            }
            HOLDER_DIVIDER -> {
                BlockViewHolder.Divider(
                    view = inflater.inflate(
                        R.layout.item_block_divider,
                        parent,
                        false
                    )
                )
            }
            HOLDER_HIGHLIGHT -> {
                BlockViewHolder.Highlight(
                    view = inflater.inflate(
                        R.layout.item_block_highlight,
                        parent,
                        false
                    )
                )
            }
            else -> throw IllegalStateException("Unexpected view type: $viewType")
        }
    }

    override fun getItemViewType(position: Int) = blocks[position].getViewType()

    override fun getItemCount(): Int = blocks.size

    override fun onBindViewHolder(
        holder: BlockViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty())
            onBindViewHolder(holder, position)
        else
            when (holder) {
                is BlockViewHolder.Paragraph -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position]
                    )
                }
                is BlockViewHolder.Bulleted -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position]
                    )
                }
                is BlockViewHolder.Checkbox -> {
                    holder.processChangePayload(
                        payloads = payloads.typeOf(),
                        item = blocks[position]
                    )
                }
                is BlockViewHolder.Title -> {
                    holder.processPayloads(
                        payloads = payloads.typeOf(),
                        item = blocks[position] as BlockView.Title
                    )
                }
                else -> TODO()
            }
    }

    override fun onBindViewHolder(holder: BlockViewHolder, position: Int) {
        when (holder) {
            is BlockViewHolder.Paragraph -> {
                holder.bind(
                    item = blocks[position] as BlockView.Paragraph,
                    onTextChanged = onTextChanged,
                    onSelectionChanged = onSelectionChanged,
                    onFocusChanged = onFocusChanged
                )
            }
            is BlockViewHolder.Title -> {
                holder.bind(
                    item = blocks[position] as BlockView.Title,
                    onTextChanged = onTextChanged,
                    onFocusChanged = onFocusChanged
                )
            }
            is BlockViewHolder.HeaderOne -> {
                holder.bind(
                    item = blocks[position] as BlockView.HeaderOne,
                    onTextChanged = onTextChanged,
                    onFocusChanged = onFocusChanged
                )
            }
            is BlockViewHolder.HeaderTwo -> {
                holder.bind(
                    item = blocks[position] as BlockView.HeaderTwo,
                    onTextChanged = onTextChanged,
                    onFocusChanged = onFocusChanged
                )
            }
            is BlockViewHolder.HeaderThree -> {
                holder.bind(
                    item = blocks[position] as BlockView.HeaderThree,
                    onTextChanged = onTextChanged,
                    onFocusChanged = onFocusChanged
                )
            }
            is BlockViewHolder.Code -> {
                holder.bind(
                    item = blocks[position] as BlockView.Code
                )
            }
            is BlockViewHolder.Checkbox -> {
                holder.bind(
                    item = blocks[position] as BlockView.Checkbox,
                    onTextChanged = onTextChanged,
                    onCheckboxClicked = onCheckboxClicked,
                    onSelectionChanged = onSelectionChanged,
                    onFocusChanged = onFocusChanged
                )
            }
            is BlockViewHolder.Task -> {
                holder.bind(
                    item = blocks[position] as BlockView.Task
                )
            }
            is BlockViewHolder.Bulleted -> {
                holder.bind(
                    item = blocks[position] as BlockView.Bulleted,
                    onTextChanged = onTextChanged,
                    onSelectionChanged = onSelectionChanged,
                    onFocusChanged = onFocusChanged
                )
            }
            is BlockViewHolder.Numbered -> {
                holder.bind(
                    item = blocks[position] as BlockView.Numbered
                )
            }
            is BlockViewHolder.Toggle -> {
                holder.bind(
                    item = blocks[position] as BlockView.Toggle
                )
            }
            is BlockViewHolder.Contact -> {
                holder.bind(
                    item = blocks[position] as BlockView.Contact
                )
            }
            is BlockViewHolder.File -> {
                holder.bind(
                    item = blocks[position] as BlockView.File
                )
            }
            is BlockViewHolder.Page -> {
                holder.bind(
                    item = blocks[position] as BlockView.Page
                )
            }
            is BlockViewHolder.Bookmark -> {
                holder.bind(
                    item = blocks[position] as BlockView.Bookmark
                )
            }
            is BlockViewHolder.Picture -> {
                holder.bind(
                    item = blocks[position] as BlockView.Picture
                )
            }
            is BlockViewHolder.Highlight -> {
                holder.bind(
                    item = blocks[position] as BlockView.Highlight,
                    onTextChanged = onTextChanged,
                    onFocusChanged = onFocusChanged
                )
            }
        }

        if (holder is BlockViewHolder.TextHolder) {
            holder.enableEnterKeyDetector(
                onEndLineEnterClicked = { editable ->
                    onEndLineEnterClicked(blocks[holder.adapterPosition].id, editable)
                },
                onSplitLineEnterClicked = {
                    onSplitLineEnterClicked(blocks[holder.adapterPosition].id)
                }
            )
            holder.enableBackspaceDetector(
                onEmptyBlockBackspaceClicked = { onEmptyBlockBackspaceClicked(blocks[holder.adapterPosition].id) },
                onNonEmptyBlockBackspaceClicked = { onNonEmptyBlockBackspaceClicked(blocks[holder.adapterPosition].id) }
            )
        }
    }

    // Bug workaround for losing text selection ability, see:
    // https://code.google.com/p/android/issues/detail?id=208169
    override fun onViewAttachedToWindow(holder: BlockViewHolder) {
        super.onViewAttachedToWindow(holder)
        if (holder is BlockViewHolder.Paragraph) {
            holder.content.isEnabled = false
            holder.content.isEnabled = true
        }
    }

    @Deprecated(
        level = DeprecationLevel.WARNING,
        message = "Consider RecyclerView's AsyncListDiffer instead. Or implement it with Kotlin coroutines."
    )
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
}
