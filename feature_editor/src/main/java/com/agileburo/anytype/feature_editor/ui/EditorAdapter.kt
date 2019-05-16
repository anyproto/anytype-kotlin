package com.agileburo.anytype.feature_editor.ui

import android.text.*
import android.text.style.StyleSpan
import android.view.*
import android.widget.EditText
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
import timber.log.Timber

class EditorAdapter(
    val blocks: MutableList<BlockView>,
    private val blockContentListener: (BlockView) -> Unit,
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

    // Bug workaround for losing text selection ability, see:
    // https://code.google.com/p/android/issues/detail?id=208169
    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        when (holder) {
            is ViewHolder.ParagraphHolder -> setIsEnabled(holder.itemView.textEditable)
            is ViewHolder.HeaderOneHolder -> setIsEnabled(holder.itemView.textHeaderOne)
            is ViewHolder.HeaderTwoHolder -> setIsEnabled(holder.itemView.textHeaderTwo)
            is ViewHolder.HeaderThreeHolder -> setIsEnabled(holder.itemView.textHeaderThree)
            is ViewHolder.HeaderFourHolder -> setIsEnabled(holder.itemView.textHeaderFour)
            is ViewHolder.QuoteHolder -> setIsEnabled(holder.itemView.textQuote)
            is ViewHolder.CheckBoxHolder -> setIsEnabled(holder.itemView.textCheckBox)
            is ViewHolder.CodeSnippetHolder -> setIsEnabled(holder.itemView.textCode)
            is ViewHolder.BulletHolder -> setIsEnabled(holder.itemView.textBullet)
            is ViewHolder.NumberedHolder -> setIsEnabled(holder.itemView.contentText)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            HOLDER_PARAGRAPH -> {
                val view = inflater.inflate(R.layout.item_block_editable, parent, false)
                ViewHolder.ParagraphHolder(view, MyEditorTextWatcher(blockContentListener))
            }
            HOLDER_HEADER_ONE -> {
                val view = inflater.inflate(R.layout.item_block_header_one, parent, false)
                ViewHolder.HeaderOneHolder(view, MyEditorTextWatcher(blockContentListener))
            }
            HOLDER_HEADER_TWO -> {
                val view = inflater.inflate(R.layout.item_block_header_two, parent, false)
                ViewHolder.HeaderTwoHolder(view, MyEditorTextWatcher(blockContentListener))
            }
            HOLDER_HEADER_THREE -> {
                val view = inflater.inflate(R.layout.item_block_header_three, parent, false)
                ViewHolder.HeaderThreeHolder(view, MyEditorTextWatcher(blockContentListener))
            }
            HOLDER_HEADER_FOUR -> {
                val view = inflater.inflate(R.layout.item_block_header_four, parent, false)
                ViewHolder.HeaderFourHolder(view, MyEditorTextWatcher(blockContentListener))
            }
            HOLDER_QUOTE -> {
                val view = inflater.inflate(R.layout.item_block_quote, parent, false)
                ViewHolder.QuoteHolder(view, MyEditorTextWatcher(blockContentListener))
            }
            HOLDER_CHECKBOX -> {
                val view = inflater.inflate(R.layout.item_block_checkbox, parent, false)
                ViewHolder.CheckBoxHolder(view, MyEditorTextWatcher(blockContentListener))
            }
            HOLDER_CODE_SNIPPET -> {
                val view = inflater.inflate(R.layout.item_block_code_snippet, parent, false)
                ViewHolder.CodeSnippetHolder(view, MyEditorTextWatcher(blockContentListener))
            }
            HOLDER_BULLET -> {
                val view = inflater.inflate(R.layout.item_block_bullet, parent, false)
                ViewHolder.BulletHolder(view, MyEditorTextWatcher(blockContentListener))
            }
            HOLDER_NUMBERED -> {
                val view = inflater.inflate(R.layout.item_number_list_item, parent, false)
                ViewHolder.NumberedHolder(view, MyEditorTextWatcher(blockContentListener))
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
            is ViewHolder.ParagraphHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    clickListener = listener,
                    linksListener = linksListener,
                    focusListener = focusListener
                )
            }
            is ViewHolder.HeaderOneHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    clickListener = listener,
                    linksListener = linksListener,
                    focusListener = focusListener
                )
            }
            is ViewHolder.HeaderTwoHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    clickListener = listener,
                    linksListener = linksListener,
                    focusListener = focusListener
                )
            }
            is ViewHolder.HeaderThreeHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    clickListener = listener,
                    linksListener = linksListener,
                    focusListener = focusListener
                )
            }
            is ViewHolder.HeaderFourHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    clickListener = listener,
                    linksListener = linksListener,
                    focusListener = focusListener
                )
            }
            is ViewHolder.QuoteHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    clickListener = listener,
                    linksListener = linksListener,
                    focusListener = focusListener
                )
            }
            is ViewHolder.CheckBoxHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    clickListener = listener,
                    linksListener = linksListener,
                    focusListener = focusListener
                )
            }
            is ViewHolder.CodeSnippetHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    clickListener = listener,
                    linksListener = linksListener,
                    focusListener = focusListener
                )
            }
            is ViewHolder.BulletHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    clickListener = listener,
                    linksListener = linksListener,
                    focusListener = focusListener
                )
            }
            is ViewHolder.NumberedHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    clickListener = listener,
                    linksListener = linksListener,
                    focusListener = focusListener
                )
            }
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

    private fun setIsEnabled(editText: EditText) {
        editText.isEnabled = false
        editText.isEnabled = true
    }

    sealed class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        class ParagraphHolder(itemView: View, val editTextWatcher: MyEditorTextWatcher) :
            ViewHolder(itemView) {

            init {
                itemView.textEditable.addTextChangedListener(editTextWatcher)
            }

            fun bind(
                block: BlockView,
                clickListener: (BlockView) -> Unit,
                linksListener: (String) -> Unit,
                focusListener: (Int) -> Unit
            ) = with(block) {
                itemView.textEditable.customSelectionActionModeCallback =
                    TextStyleCallback(itemView.textEditable)
                itemView.textEditable.setText(content.text)
                setFocusListener(
                    editText = itemView.textEditable,
                    focusListener = focusListener
                )
                itemView.btnEditable.setOnClickListener { clickListener(block) }
            }
        }

        class HeaderOneHolder(itemView: View, val editTextWatcher: MyEditorTextWatcher) :
            ViewHolder(itemView) {

            init {
                itemView.textHeaderOne.addTextChangedListener(editTextWatcher)
            }

            fun bind(
                block: BlockView,
                clickListener: (BlockView) -> Unit,
                linksListener: (String) -> Unit,
                focusListener: (Int) -> Unit
            ) = with(block) {
                itemView.textHeaderOne.setText(content.text)
                setFocusListener(
                    editText = itemView.textHeaderOne,
                    focusListener = focusListener
                )
                itemView.btnHeaderOne.setOnClickListener { clickListener(this) }
            }
        }

        class HeaderTwoHolder(itemView: View, val editTextWatcher: MyEditorTextWatcher) :
            ViewHolder(itemView) {

            init {
                itemView.textHeaderTwo.addTextChangedListener(editTextWatcher)
            }

            fun bind(
                block: BlockView,
                clickListener: (BlockView) -> Unit,
                linksListener: (String) -> Unit,
                focusListener: (Int) -> Unit
            ) = with(block) {
                itemView.textHeaderTwo.setText(content.text)
                setFocusListener(
                    editText = itemView.textHeaderTwo,
                    focusListener = focusListener
                )
                itemView.btnHeaderTwo.setOnClickListener { clickListener(this) }
            }
        }

        class HeaderThreeHolder(itemView: View, val editTextWatcher: MyEditorTextWatcher) :
            ViewHolder(itemView) {

            init {
                itemView.textHeaderThree.addTextChangedListener(editTextWatcher)
            }

            fun bind(
                block: BlockView,
                clickListener: (BlockView) -> Unit,
                linksListener: (String) -> Unit,
                focusListener: (Int) -> Unit
            ) = with(block) {
                itemView.textHeaderThree.setText(content.text)
                setFocusListener(
                    editText = itemView.textHeaderThree,
                    focusListener = focusListener
                )
                itemView.btnHeaderThree.setOnClickListener { clickListener(this) }
            }
        }

        class HeaderFourHolder(itemView: View, val editTextWatcher: MyEditorTextWatcher) :
            ViewHolder(itemView) {

            init {
                itemView.textHeaderFour.addTextChangedListener(editTextWatcher)
            }

            fun bind(
                block: BlockView,
                clickListener: (BlockView) -> Unit,
                linksListener: (String) -> Unit,
                focusListener: (Int) -> Unit
            ) = with(block) {
                itemView.textHeaderFour.setText(content.text)
                setFocusListener(
                    editText = itemView.textHeaderFour,
                    focusListener = focusListener
                )
                itemView.btnHeaderFour.setOnClickListener { clickListener(this) }
            }
        }

        class QuoteHolder(itemView: View, val editTextWatcher: MyEditorTextWatcher) :
            ViewHolder(itemView) {

            init {
                itemView.textQuote.addTextChangedListener(editTextWatcher)
            }

            fun bind(
                block: BlockView,
                clickListener: (BlockView) -> Unit,
                linksListener: (String) -> Unit,
                focusListener: (Int) -> Unit
            ) = with(block) {
                itemView.textQuote.customSelectionActionModeCallback =
                    TextStyleCallback(itemView.textQuote)
                itemView.textQuote.setText(content.text)
                setFocusListener(
                    editText = itemView.textQuote,
                    focusListener = focusListener
                )
                itemView.btnQuote.setOnClickListener { clickListener(this) }
            }
        }

        class CheckBoxHolder(itemView: View, val editTextWatcher: MyEditorTextWatcher) :
            ViewHolder(itemView) {

            init {
                itemView.textCheckBox.addTextChangedListener(editTextWatcher)
            }

            fun bind(
                block: BlockView,
                clickListener: (BlockView) -> Unit,
                linksListener: (String) -> Unit,
                focusListener: (Int) -> Unit
            ) = with(block) {
                itemView.textCheckBox.customSelectionActionModeCallback =
                    TextStyleCallback(itemView.textCheckBox)
                itemView.textCheckBox.setText(content.text)
                setFocusListener(
                    editText = itemView.textCheckBox,
                    focusListener = focusListener
                )
                itemView.btnCheckboxBlock.setOnClickListener { clickListener(this) }
            }
        }

        class CodeSnippetHolder(itemView: View, val editTextWatcher: MyEditorTextWatcher) :
            ViewHolder(itemView) {

            init {
                itemView.textCode.addTextChangedListener(editTextWatcher)
            }

            fun bind(
                block: BlockView,
                clickListener: (BlockView) -> Unit,
                linksListener: (String) -> Unit,
                focusListener: (Int) -> Unit
            ) = with(block) {
                itemView.textCode.setText(content.text)
                setFocusListener(
                    editText = itemView.textCode,
                    focusListener = focusListener
                )
                itemView.btnCode.setOnClickListener { clickListener(this) }
            }
        }

        class BulletHolder(itemView: View, val editTextWatcher: MyEditorTextWatcher) :
            ViewHolder(itemView) {

            init {
                itemView.textBullet.addTextChangedListener(editTextWatcher)
            }

            fun bind(
                block: BlockView,
                clickListener: (BlockView) -> Unit,
                linksListener: (String) -> Unit,
                focusListener: (Int) -> Unit
            ) = with(block) {
                itemView.textBullet.customSelectionActionModeCallback =
                    TextStyleCallback(itemView.textBullet)

                itemView.textBullet.setText(
                    SpannableString(content.text)
                        .withBulletSpan(gapWidth = 40, start = 0),
                    TextView.BufferType.SPANNABLE
                )
                setFocusListener(
                    editText = itemView.textBullet,
                    focusListener = focusListener
                )
                itemView.btnBullet.setOnClickListener { clickListener(this) }
            }
        }

        class NumberedHolder(itemView: View, val editTextWatcher: MyEditorTextWatcher) :
            ViewHolder(itemView) {

            init {
                itemView.contentText.addTextChangedListener(editTextWatcher)
            }

            fun bind(
                block: BlockView,
                clickListener: (BlockView) -> Unit,
                linksListener: (String) -> Unit,
                focusListener: (Int) -> Unit
            ) {
                with(itemView) {
                    contentText.customSelectionActionModeCallback =
                        TextStyleCallback(contentText)
                    positionText.text = "${block.content.param.number}."
                    contentText.setText(
                        block.content.text
                    )
                }
                setFocusListener(
                    editText = itemView.contentText,
                    focusListener = focusListener
                )
                itemView.btnNumbered.setOnClickListener { clickListener(block) }
            }
        }

        fun setFocusListener(
            editText: EditText,
            focusListener: (Int) -> Unit
        ) {
            editText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    focusListener.invoke(adapterPosition)
                }
            }
        }
    }

    inner class MyEditorTextWatcher(private val contentListener: (BlockView) -> Unit) :
        TextWatcher {

        var position = 0

        override fun afterTextChanged(s: Editable?) {}
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            s?.let {
                blocks[position].content.text = s
                contentListener.invoke(blocks[position])
            }
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

