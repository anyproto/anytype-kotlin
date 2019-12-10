package com.agileburo.anytype.core_ui.features.page

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
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_PICTURE
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_TASK
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_TEXT
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_TITLE
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder.Companion.HOLDER_TOGGLE

/**
 * Adapter for rendering list of blocks.
 * @property blocks mutable list of blocks
 * @see BlockView
 * @see BlockViewHolder
 * @see BlockViewDiffUtil
 */
class BlockAdapter(
    private val blocks: MutableList<BlockView>,
    private val onTextChanged: (String, String) -> Unit
) : RecyclerView.Adapter<BlockViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            HOLDER_TEXT -> {
                BlockViewHolder.Text(
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

    override fun onBindViewHolder(holder: BlockViewHolder, position: Int) {
        when (holder) {
            is BlockViewHolder.Text -> {
                holder.bind(
                    item = blocks[position] as BlockView.Text,
                    onTextChanged = onTextChanged
                )
            }
            is BlockViewHolder.Title -> {
                holder.bind(
                    item = blocks[position] as BlockView.Title,
                    onTextChanged = onTextChanged
                )
            }
            is BlockViewHolder.HeaderOne -> {
                holder.bind(
                    item = blocks[position] as BlockView.HeaderOne
                )
            }
            is BlockViewHolder.HeaderTwo -> {
                holder.bind(
                    item = blocks[position] as BlockView.HeaderTwo
                )
            }
            is BlockViewHolder.HeaderThree -> {
                holder.bind(
                    item = blocks[position] as BlockView.HeaderThree
                )
            }
            is BlockViewHolder.Code -> {
                holder.bind(
                    item = blocks[position] as BlockView.Code
                )
            }
            is BlockViewHolder.Checkbox -> {
                holder.bind(
                    item = blocks[position] as BlockView.Checkbox
                )
            }
            is BlockViewHolder.Task -> {
                holder.bind(
                    item = blocks[position] as BlockView.Task
                )
            }
            is BlockViewHolder.Bulleted -> {
                holder.bind(
                    item = blocks[position] as BlockView.Bulleted
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
                    item = blocks[position] as BlockView.Highlight
                )
            }
        }
    }

    @Deprecated(
        level = DeprecationLevel.WARNING,
        message = "Consider RecyclerView's AsyncListDiffer instead. Or implement it with Kotlin coroutines."
    )
    fun updateWithDiffUtil(items: List<BlockView>) {
        val callback = BlockViewDiffUtil(old = blocks, new = items)
        val result = DiffUtil.calculateDiff(callback)
        blocks.clear()
        blocks.addAll(items)
        result.dispatchUpdatesTo(this)
    }

    fun update(items: List<BlockView>) {
        blocks.clear()
        blocks.addAll(items)
        notifyDataSetChanged()
    }
}
