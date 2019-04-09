package com.agileburo.anytype.feature_editor.ui

import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.core_utils.swap
import com.agileburo.anytype.feature_editor.R
import com.agileburo.anytype.feature_editor.domain.ContentType
import com.agileburo.anytype.feature_editor.domain.Mark
import com.agileburo.anytype.feature_editor.presentation.model.BlockView
import com.agileburo.anytype.feature_editor.presentation.util.BlockViewDiffUtil
import com.agileburo.anytype.feature_editor.presentation.util.SwapRequest
import kotlinx.android.synthetic.main.item_block_checkbox.view.*
import kotlinx.android.synthetic.main.item_block_code_snippet.view.*
import kotlinx.android.synthetic.main.item_block_editable.view.*
import kotlinx.android.synthetic.main.item_block_header_four.view.*
import kotlinx.android.synthetic.main.item_block_header_one.view.*
import kotlinx.android.synthetic.main.item_block_header_three.view.*
import kotlinx.android.synthetic.main.item_block_header_two.view.*
import kotlinx.android.synthetic.main.item_block_quote.view.*
import kotlinx.android.synthetic.main.item_number_list_item.view.*

class EditorAdapter(
    private val blocks: MutableList<BlockView>,
    private val listener: (BlockView) -> Unit,
    private val linksListener: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun setBlocks(items: List<BlockView>) {
        blocks.addAll(items)
        notifyDataSetChanged()
    }

    fun updateBlock(block: BlockView) {
        val index = blocks.indexOfFirst { it.id == block.id }
        if (index >= 0 && index < blocks.size) {
            blocks[index] = block
            notifyItemChanged(index)
        }
    }

    fun update(items : List<BlockView>) {
        val callback = BlockViewDiffUtil(old = blocks, new = items)
        val result = DiffUtil.calculateDiff(callback)
        blocks.clear()
        blocks.addAll(items)
        result.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            HOLDER_PARAGRAPH -> {
                val view = inflater.inflate(R.layout.item_block_editable, parent, false)
                ViewHolder.ParagraphHolder(view)
            }
            HOLDER_HEADER_ONE -> {
                val view = inflater.inflate(R.layout.item_block_header_one, parent, false)
                ViewHolder.HeaderOneHolder(view)
            }
            HOLDER_HEADER_TWO -> {
                val view = inflater.inflate(R.layout.item_block_header_two, parent, false)
                ViewHolder.HeaderTwoHolder(view)
            }
            HOLDER_HEADER_THREE -> {
                val view = inflater.inflate(R.layout.item_block_header_three, parent, false)
                ViewHolder.HeaderThreeHolder(view)
            }
            HOLDER_HEADER_FOUR -> {
                val view = inflater.inflate(R.layout.item_block_header_four, parent, false)
                ViewHolder.HeaderFourHolder(view)
            }
            HOLDER_QUOTE -> {
                val view = inflater.inflate(R.layout.item_block_quote, parent, false)
                ViewHolder.QuoteHolder(view)
            }
            HOLDER_CHECKBOX -> {
                val view = inflater.inflate(R.layout.item_block_checkbox, parent, false)
                ViewHolder.CheckBoxHolder(view)
            }
            HOLDER_CODE_SNIPPET -> {
                val view = inflater.inflate(R.layout.item_block_code_snippet, parent, false)
                ViewHolder.CodeSnippetHolder(view)
            }
            HOLDER_BULLET -> {
                val view = inflater.inflate(R.layout.item_block_editable, parent, false)
                ViewHolder.BulletHolder(view)
            }
            HOLDER_NUMBERED -> {
                val view = inflater.inflate(R.layout.item_number_list_item, parent, false)
                ViewHolder.NumberedHolder(view)
            }

            else -> TODO()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (blocks[position].contentType) {
            is ContentType.P -> HOLDER_PARAGRAPH
            is ContentType.H1 -> HOLDER_HEADER_ONE
            is ContentType.H2 -> HOLDER_HEADER_TWO
            is ContentType.H3 -> HOLDER_HEADER_THREE
            is ContentType.H4 -> HOLDER_HEADER_FOUR
            is ContentType.Quote -> HOLDER_QUOTE
            is ContentType.Check -> HOLDER_CHECKBOX
            is ContentType.Code -> HOLDER_CODE_SNIPPET
            is ContentType.NumberedList -> HOLDER_NUMBERED
            is ContentType.UL -> HOLDER_BULLET
            else -> throw IllegalStateException("Implement Toggle!!!")
        }
    }

    override fun getItemCount() = blocks.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.ParagraphHolder -> holder.bind(blocks[position], listener, linksListener)
            is ViewHolder.HeaderOneHolder -> holder.bind(blocks[position], listener, linksListener)
            is ViewHolder.HeaderTwoHolder -> holder.bind(blocks[position], listener, linksListener)
            is ViewHolder.HeaderThreeHolder -> holder.bind(
                block = blocks[position],
                clickListener = listener,
                linksListener = linksListener
            )
            is ViewHolder.HeaderFourHolder -> holder.bind(blocks[position], listener, linksListener)
            is ViewHolder.QuoteHolder -> holder.bind(blocks[position], listener, linksListener)
            is ViewHolder.CheckBoxHolder -> holder.bind(blocks[position], listener, linksListener)
            is ViewHolder.CodeSnippetHolder -> holder.bind(
                blocks[position],
                listener,
                linksListener
            )
            is ViewHolder.BulletHolder -> holder.bind(blocks[position], listener, linksListener)
            is ViewHolder.NumberedHolder -> holder.bind(blocks[position], listener)
        }
    }

    fun onItemMoved(fromPosition: Int, toPosition: Int): Boolean {
        swapPosition(fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    fun swap(request : SwapRequest) {
        swapPosition(request.from, request.to)
        notifyItemMoved(request.from, request.to)
    }

    private fun swapPosition(fromPosition: Int, toPosition: Int) =
        blocks.swap(fromPosition, toPosition)

    sealed class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        class ParagraphHolder(itemView: View) : ViewHolder(itemView) {

            fun bind(
                block: BlockView,
                clickListener: (BlockView) -> Unit,
                linksListener: (String) -> Unit
            ) = with(block) {
                setContentMarks(
                    tvContent = itemView.tvContent,
                    content = content.text,
                    marks = content.marks,
                    linksListener = linksListener
                )
                itemView.setOnClickListener { clickListener(this) }
            }
        }

        class HeaderOneHolder(itemView: View) : ViewHolder(itemView) {

            fun bind(
                block: BlockView,
                clickListener: (BlockView) -> Unit,
                linksListener: (String) -> Unit
            ) = with(block) {
                setContentMarks(
                    tvContent = itemView.headerContentText,
                    content = content.text,
                    marks = content.marks,
                    linksListener = linksListener
                )
                itemView.setOnClickListener { clickListener(this) }
            }
        }

        class HeaderTwoHolder(itemView: View) : ViewHolder(itemView) {

            fun bind(
                block: BlockView,
                clickListener: (BlockView) -> Unit,
                linksListener: (String) -> Unit
            ) = with(block) {
                setContentMarks(
                    tvContent = itemView.headerTwoContentText,
                    content = content.text,
                    marks = content.marks,
                    linksListener = linksListener
                )
                itemView.setOnClickListener { clickListener(this) }
            }
        }

        class HeaderThreeHolder(itemView: View) : ViewHolder(itemView) {

            fun bind(
                block: BlockView,
                clickListener: (BlockView) -> Unit,
                linksListener: (String) -> Unit
            ) = with(block) {
                setContentMarks(
                    tvContent = itemView.headerThreeContentText,
                    content = content.text,
                    marks = content.marks,
                    linksListener = linksListener
                )
                itemView.setOnClickListener { clickListener(this) }
            }
        }

        class HeaderFourHolder(itemView: View) : ViewHolder(itemView) {

            fun bind(
                block: BlockView,
                clickListener: (BlockView) -> Unit,
                linksListener: (String) -> Unit
            ) = with(block) {
                setContentMarks(
                    tvContent = itemView.headerFourContentText,
                    content = content.text,
                    marks = content.marks,
                    linksListener = linksListener
                )
                itemView.setOnClickListener { clickListener(this) }
            }
        }

        class QuoteHolder(itemView: View) : ViewHolder(itemView) {

            fun bind(
                block: BlockView,
                clickListener: (BlockView) -> Unit,
                linksListener: (String) -> Unit
            ) = with(block) {
                setContentMarks(
                    tvContent = itemView.quoteContent,
                    content = content.text,
                    marks = content.marks,
                    linksListener = linksListener
                )
                itemView.setOnClickListener { clickListener(this) }
            }
        }

        class CheckBoxHolder(itemView: View) : ViewHolder(itemView) {

            fun bind(
                block: BlockView,
                clickListener: (BlockView) -> Unit,
                linksListener: (String) -> Unit
            ) = with(block) {
                setContentMarks(
                    tvContent = itemView.checkBoxContent,
                    content = content.text,
                    marks = content.marks,
                    linksListener = linksListener
                )
                itemView.checkBoxContent.setOnClickListener { clickListener(this) }
            }
        }

        class CodeSnippetHolder(itemView: View) : ViewHolder(itemView) {

            fun bind(
                block: BlockView,
                clickListener: (BlockView) -> Unit,
                linksListener: (String) -> Unit
            ) = with(block) {
                setContentMarks(
                    tvContent = itemView.codeSnippetContent,
                    content = content.text,
                    marks = content.marks,
                    linksListener = linksListener
                )
                itemView.codeSnippetContent.setOnClickListener { clickListener(this) }
            }
        }

        class BulletHolder(itemView: View) : ViewHolder(itemView) {

            fun bind(
                block: BlockView,
                clickListener: (BlockView) -> Unit,
                linksListener: (String) -> Unit
            ) = with(block) {
                itemView.tvContent.text =
                    SpannableString(content.text)
                        .withBulletSpan(gapWidth = 40, start = 0)
                        .addMarks(
                            marks = content.marks,
                            textView = itemView.tvContent,
                            click = { linksListener(it) },
                            itemView = itemView
                        )
                itemView.setOnClickListener { clickListener(this) }
            }
        }

        class NumberedHolder(itemView: View) : ViewHolder(itemView) {

            fun bind(block: BlockView, clickListener: (BlockView) -> Unit) {
                with(itemView) {
                    positionText.text = "${block.content.param.number}."
                    contentText.text = block.content.text
                    setOnClickListener { clickListener(block) }
                }
                itemView.setOnClickListener { clickListener(block) }
            }
        }

        fun setContentMarks(
            tvContent: TextView,
            content: CharSequence,
            marks: List<Mark>,
            linksListener: (String) -> Unit
        ) =
            if (marks.isNotEmpty()) {
                tvContent.text =
                    SpannableString(content)
                        .addMarks(
                            marks = marks,
                            textView = tvContent,
                            click = { linksListener(it) },
                            itemView = itemView
                        )
            } else {
                tvContent.text = content
            }
    }

    companion object {
        const val HOLDER_PARAGRAPH = 0
        const val HOLDER_HEADER_ONE = 1
        const val HOLDER_HEADER_TWO = 2
        const val HOLDER_HEADER_THREE = 3
        const val HOLDER_QUOTE = 4
        const val HOLDER_CHECKBOX = 5
        const val HOLDER_CODE_SNIPPET = 6
        const val HOLDER_NUMBERED = 7
        const val HOLDER_HEADER_FOUR = 8
        const val HOLDER_BULLET = 9
    }
}
