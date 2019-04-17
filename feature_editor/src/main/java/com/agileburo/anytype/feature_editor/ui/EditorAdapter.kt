package com.agileburo.anytype.feature_editor.ui

import android.graphics.Typeface
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
import kotlinx.android.synthetic.main.item_block_bullet.view.*
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
    private val blockContentListener: (String, CharSequence) -> Unit,
    private val listener: (BlockView) -> Unit,
    private val linksListener: (String) -> Unit,
    private val focusListener: (Int) -> Unit
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

    fun update(items: List<BlockView>) {
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
                val view = inflater.inflate(R.layout.item_block_bullet, parent, false)
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
            is ViewHolder.ParagraphHolder -> holder.bind(
                block = blocks[position],
                clickListener = listener,
                linksListener = linksListener,
                contentListener = blockContentListener,
                focusListener = focusListener
            )
            is ViewHolder.HeaderOneHolder -> holder.bind(
                block = blocks[position],
                clickListener = listener,
                linksListener = linksListener,
                contentListener = blockContentListener
            )
            is ViewHolder.HeaderTwoHolder -> holder.bind(
                block = blocks[position],
                clickListener = listener,
                linksListener = linksListener,
                contentListener = blockContentListener
            )
            is ViewHolder.HeaderThreeHolder -> holder.bind(
                block = blocks[position],
                clickListener = listener,
                linksListener = linksListener,
                contentListener = blockContentListener
            )
            is ViewHolder.HeaderFourHolder -> holder.bind(
                block = blocks[position],
                clickListener = listener,
                linksListener = linksListener,
                contentListener = blockContentListener
            )
            is ViewHolder.QuoteHolder -> holder.bind(
                block = blocks[position],
                clickListener = listener,
                linksListener = linksListener,
                contentListener = blockContentListener
            )
            is ViewHolder.CheckBoxHolder -> holder.bind(
                block = blocks[position],
                clickListener = listener,
                linksListener = linksListener,
                contentListener = blockContentListener
            )
            is ViewHolder.CodeSnippetHolder -> holder.bind(
                block = blocks[position],
                clickListener = listener,
                linksListener = linksListener,
                contentListener = blockContentListener
            )
            is ViewHolder.BulletHolder -> holder.bind(
                block = blocks[position],
                clickListener = listener,
                linksListener = linksListener,
                contentListener = blockContentListener
            )
            is ViewHolder.NumberedHolder -> holder.bind(
                block = blocks[position],
                clickListener = listener,
                linksListener = linksListener,
                contentListener = blockContentListener
            )
        }
    }

    fun onItemMoved(fromPosition: Int, toPosition: Int): Boolean {
        swapPosition(fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    fun swap(request: SwapRequest) {
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
                linksListener: (String) -> Unit,
                contentListener: (String, CharSequence) -> Unit,
                focusListener: (Int) -> Unit
            ) = with(block) {
                setContentMarks(
                    tvContent = itemView.textEditable,
                    content = content.text,
                    marks = content.marks,
                    linksListener = linksListener
                )
                itemView.textEditable.addTextChangedListener(
                    EditorTextWatcher(
                        this.id,
                        contentListener,
                        Typeface.DEFAULT
                    )
                )
                itemView.textEditable.setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        focusListener.invoke(adapterPosition)
                    }
                }
                itemView.btnEditable.setOnClickListener { clickListener(this) }
            }
        }

        class HeaderOneHolder(itemView: View) : ViewHolder(itemView) {

            fun bind(
                block: BlockView,
                clickListener: (BlockView) -> Unit,
                linksListener: (String) -> Unit,
                contentListener: (String, CharSequence) -> Unit
            ) = with(block) {
                setContentMarks(
                    tvContent = itemView.textHeaderOne,
                    content = content.text,
                    marks = content.marks,
                    linksListener = linksListener
                )
                itemView.textHeaderOne.addTextChangedListener(
                    EditorTextWatcher(
                        this.id,
                        contentListener,
                        Typeface.DEFAULT
                    )
                )
                itemView.btnHeaderOne.setOnClickListener { clickListener(this) }
            }
        }

        class HeaderTwoHolder(itemView: View) : ViewHolder(itemView) {

            fun bind(
                block: BlockView,
                clickListener: (BlockView) -> Unit,
                linksListener: (String) -> Unit,
                contentListener: (String, CharSequence) -> Unit
            ) = with(block) {
                setContentMarks(
                    tvContent = itemView.textHeaderTwo,
                    content = content.text,
                    marks = content.marks,
                    linksListener = linksListener
                )
                itemView.textHeaderTwo.addTextChangedListener(
                    EditorTextWatcher(
                        this.id,
                        contentListener,
                        Typeface.DEFAULT
                    )
                )
                itemView.btnHeaderTwo.setOnClickListener { clickListener(this) }
            }
        }

        class HeaderThreeHolder(itemView: View) : ViewHolder(itemView) {

            fun bind(
                block: BlockView,
                clickListener: (BlockView) -> Unit,
                linksListener: (String) -> Unit,
                contentListener: (String, CharSequence) -> Unit
            ) = with(block) {
                setContentMarks(
                    tvContent = itemView.textHeaderThree,
                    content = content.text,
                    marks = content.marks,
                    linksListener = linksListener
                )
                itemView.textHeaderThree.addTextChangedListener(
                    EditorTextWatcher(
                        this.id,
                        contentListener,
                        Typeface.DEFAULT
                    )
                )
                itemView.btnHeaderThree.setOnClickListener { clickListener(this) }
            }
        }

        class HeaderFourHolder(itemView: View) : ViewHolder(itemView) {

            fun bind(
                block: BlockView,
                clickListener: (BlockView) -> Unit,
                linksListener: (String) -> Unit,
                contentListener: (String, CharSequence) -> Unit
            ) = with(block) {
                setContentMarks(
                    tvContent = itemView.textHeaderFour,
                    content = content.text,
                    marks = content.marks,
                    linksListener = linksListener
                )
                itemView.textHeaderFour.addTextChangedListener(
                    EditorTextWatcher(
                        this.id,
                        contentListener,
                        Typeface.DEFAULT
                    )
                )
                itemView.btnHeaderFour.setOnClickListener { clickListener(this) }
            }
        }

        class QuoteHolder(itemView: View) : ViewHolder(itemView) {

            fun bind(
                block: BlockView,
                clickListener: (BlockView) -> Unit,
                linksListener: (String) -> Unit,
                contentListener: (String, CharSequence) -> Unit
            ) = with(block) {
                setContentMarks(
                    tvContent = itemView.textQuote,
                    content = content.text,
                    marks = content.marks,
                    linksListener = linksListener
                )
                itemView.textQuote.addTextChangedListener(
                    EditorTextWatcher(
                        this.id,
                        contentListener,
                        Typeface.DEFAULT
                    )
                )
                itemView.btnQuote.setOnClickListener { clickListener(this) }
            }
        }

        class CheckBoxHolder(itemView: View) : ViewHolder(itemView) {

            fun bind(
                block: BlockView,
                clickListener: (BlockView) -> Unit,
                linksListener: (String) -> Unit,
                contentListener: (String, CharSequence) -> Unit
            ) = with(block) {
                setContentMarks(
                    tvContent = itemView.textCheckBox,
                    content = content.text,
                    marks = content.marks,
                    linksListener = linksListener
                )
                itemView.textCheckBox.addTextChangedListener(
                    EditorTextWatcher(
                        this.id,
                        contentListener,
                        Typeface.DEFAULT
                    )
                )
                itemView.btnCheckboxBlock.setOnClickListener { clickListener(this) }
            }
        }

        class CodeSnippetHolder(itemView: View) : ViewHolder(itemView) {

            fun bind(
                block: BlockView,
                clickListener: (BlockView) -> Unit,
                linksListener: (String) -> Unit,
                contentListener: (String, CharSequence) -> Unit
            ) = with(block) {
                setContentMarks(
                    tvContent = itemView.textCode,
                    content = content.text,
                    marks = content.marks,
                    linksListener = linksListener
                )
                itemView.textCode.addTextChangedListener(
                    EditorTextWatcher(
                        this.id,
                        contentListener,
                        Typeface.DEFAULT
                    )
                )
                itemView.btnCode.setOnClickListener { clickListener(this) }
            }
        }

        class BulletHolder(itemView: View) : ViewHolder(itemView) {

            fun bind(
                block: BlockView,
                clickListener: (BlockView) -> Unit,
                linksListener: (String) -> Unit,
                contentListener: (String, CharSequence) -> Unit
            ) = with(block) {
                itemView.textBullet.setText(
                    SpannableString(content.text)
                        .withBulletSpan(gapWidth = 40, start = 0)
                        .addMarks(
                            marks = content.marks,
                            textView = itemView.textBullet,
                            click = { linksListener(it) },
                            itemView = itemView
                        ), TextView.BufferType.SPANNABLE
                )
                itemView.textBullet.addTextChangedListener(
                    EditorTextWatcher(
                        this.id,
                        contentListener,
                        Typeface.DEFAULT
                    )
                )
                itemView.btnBullet.setOnClickListener { clickListener(this) }
            }
        }

        class NumberedHolder(itemView: View) : ViewHolder(itemView) {

            fun bind(
                block: BlockView,
                clickListener: (BlockView) -> Unit,
                linksListener: (String) -> Unit,
                contentListener: (String, CharSequence) -> Unit
            ) {
                with(itemView) {
                    positionText.text = "${block.content.param.number}."
                    contentText.setText(
                        SpannableString(block.content.text)
                            .addMarks(
                                marks = block.content.marks,
                                textView = itemView.contentText,
                                click = linksListener,
                                itemView = itemView
                            ), TextView.BufferType.SPANNABLE
                    )
                    contentText.addTextChangedListener(
                        EditorTextWatcher(
                            block.id,
                            contentListener,
                            Typeface.DEFAULT
                        )
                    )
                }
                itemView.btnNumbered.setOnClickListener { clickListener(block) }
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