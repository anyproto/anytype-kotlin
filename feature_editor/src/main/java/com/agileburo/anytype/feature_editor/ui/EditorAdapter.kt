package com.agileburo.anytype.feature_editor.ui

import android.content.Context
import android.text.Editable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.core_utils.swap
import com.agileburo.anytype.feature_editor.R
import com.agileburo.anytype.feature_editor.presentation.model.BlockView
import com.agileburo.anytype.feature_editor.presentation.model.BlockView.*
import com.agileburo.anytype.feature_editor.presentation.model.BlockView.HeaderView.HeaderType
import com.agileburo.anytype.feature_editor.presentation.util.BlockViewDiffUtil
import com.agileburo.anytype.feature_editor.presentation.util.SwapRequest
import kotlinx.android.synthetic.main.item_block_bullet.view.*
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
    val blocks: MutableList<BlockView>,
    private val blockContentListener: (BlockView) -> Unit,
    private val menuListener: (BlockMenuAction) -> Unit,
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

        val block = blocks[position]


        return when (block) {
            is BookmarkView -> HOLDER_BOOKMARK
            is LinkToPageView -> HOLDER_PAGE
            is ParagraphView -> HOLDER_PARAGRAPH
            is QuoteView -> HOLDER_QUOTE
            is CodeSnippetView -> HOLDER_CODE_SNIPPET
            is CheckboxView -> HOLDER_CHECKBOX
            is NumberListItemView -> HOLDER_NUMBERED
            is BulletView -> HOLDER_BULLET
            is HeaderView -> {
                when (block.type) {
                    HeaderType.ONE -> HOLDER_HEADER_ONE
                    HeaderType.TWO -> HOLDER_HEADER_TWO
                    HeaderType.THREE -> HOLDER_HEADER_THREE
                    HeaderType.FOUR -> HOLDER_HEADER_FOUR
                }
            }
            is DividerView -> HOLDER_DIVIDER
            is PictureView -> HOLDER_PICTURE
        }

    }

    override fun getItemCount() = blocks.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.ParagraphHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    menuListener = menuListener,
                    linksListener = linksListener,
                    focusListener = focusListener
                )
            }
            is ViewHolder.HeaderOneHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    menuListener = menuListener,
                    linksListener = linksListener,
                    focusListener = focusListener
                )
            }
            is ViewHolder.HeaderTwoHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    menuListener = menuListener,
                    linksListener = linksListener,
                    focusListener = focusListener
                )
            }
            is ViewHolder.HeaderThreeHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    menuListener = menuListener,
                    linksListener = linksListener,
                    focusListener = focusListener
                )
            }
            is ViewHolder.HeaderFourHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    menuListener = menuListener,
                    linksListener = linksListener,
                    focusListener = focusListener
                )
            }
            is ViewHolder.QuoteHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    menuListener = menuListener,
                    linksListener = linksListener,
                    focusListener = focusListener
                )
            }
            is ViewHolder.CheckBoxHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    menuListener = menuListener,
                    linksListener = linksListener,
                    focusListener = focusListener
                )
            }
            is ViewHolder.CodeSnippetHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    menuListener = menuListener,
                    linksListener = linksListener,
                    focusListener = focusListener
                )
            }
            is ViewHolder.BulletHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    menuListener = menuListener,
                    linksListener = linksListener,
                    focusListener = focusListener
                )
            }
            is ViewHolder.NumberedHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    menuListener = menuListener,
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
                menuListener: (BlockMenuAction) -> Unit,
                linksListener: (String) -> Unit,
                focusListener: (Int) -> Unit
            ) = with(block) {
                itemView.textEditable.customSelectionActionModeCallback =
                    TextStyleCallback(itemView.textEditable)

                if (block is BlockView.Editable)
                    itemView.textEditable.setText(block.text)

                setFocusListener(
                    editText = itemView.textEditable,
                    focusListener = focusListener
                )
                itemView.btnEditable.setOnClickListener {
                    showBlockMenu(
                        context = itemView.context,
                        block = block,
                        menuListener = menuListener,
                        parent = it
                    )
                }
            }
        }

        class HeaderOneHolder(itemView: View, val editTextWatcher: MyEditorTextWatcher) :
            ViewHolder(itemView) {

            init {
                itemView.textHeaderOne.addTextChangedListener(editTextWatcher)
            }

            fun bind(
                block: BlockView,
                menuListener: (BlockMenuAction) -> Unit,
                linksListener: (String) -> Unit,
                focusListener: (Int) -> Unit
            ) = with(block) {

                if (block is BlockView.Editable)
                    itemView.textHeaderOne.setText(block.text)

                setFocusListener(
                    editText = itemView.textHeaderOne,
                    focusListener = focusListener
                )
                itemView.btnHeaderOne.setOnClickListener {
                    showBlockMenu(
                        context = itemView.context,
                        block = block,
                        menuListener = menuListener,
                        parent = it
                    )
                }
            }
        }

        class HeaderTwoHolder(itemView: View, val editTextWatcher: MyEditorTextWatcher) :
            ViewHolder(itemView) {

            init {
                itemView.textHeaderTwo.addTextChangedListener(editTextWatcher)
            }

            fun bind(
                block: BlockView,
                menuListener: (BlockMenuAction) -> Unit,
                linksListener: (String) -> Unit,
                focusListener: (Int) -> Unit
            ) = with(block) {

                if (block is BlockView.Editable)
                    itemView.textHeaderTwo.setText(block.text)

                setFocusListener(
                    editText = itemView.textHeaderTwo,
                    focusListener = focusListener
                )
                itemView.btnHeaderTwo.setOnClickListener {
                    showBlockMenu(
                        context = itemView.context,
                        block = block,
                        menuListener = menuListener,
                        parent = it
                    )
                }
            }
        }

        class HeaderThreeHolder(itemView: View, val editTextWatcher: MyEditorTextWatcher) :
            ViewHolder(itemView) {

            init {
                itemView.textHeaderThree.addTextChangedListener(editTextWatcher)
            }

            fun bind(
                block: BlockView,
                menuListener: (BlockMenuAction) -> Unit,
                linksListener: (String) -> Unit,
                focusListener: (Int) -> Unit
            ) = with(block) {

                if (block is BlockView.Editable)
                    itemView.textHeaderThree.setText(block.text)

                setFocusListener(
                    editText = itemView.textHeaderThree,
                    focusListener = focusListener
                )
                itemView.btnHeaderThree.setOnClickListener {
                    showBlockMenu(
                        context = itemView.context,
                        block = block,
                        menuListener = menuListener,
                        parent = it
                    )
                }
            }
        }

        class HeaderFourHolder(itemView: View, val editTextWatcher: MyEditorTextWatcher) :
            ViewHolder(itemView) {

            init {
                itemView.textHeaderFour.addTextChangedListener(editTextWatcher)
            }

            fun bind(
                block: BlockView,
                menuListener: (BlockMenuAction) -> Unit,
                linksListener: (String) -> Unit,
                focusListener: (Int) -> Unit
            ) = with(block) {

                if (block is BlockView.Editable)
                    itemView.textHeaderFour.setText(block.text)

                setFocusListener(
                    editText = itemView.textHeaderFour,
                    focusListener = focusListener
                )

                itemView.btnHeaderFour.setOnClickListener {
                    showBlockMenu(
                        context = itemView.context,
                        block = block,
                        menuListener = menuListener,
                        parent = it
                    )
                }
            }
        }

        class QuoteHolder(itemView: View, val editTextWatcher: MyEditorTextWatcher) :
            ViewHolder(itemView) {

            init {
                itemView.textQuote.addTextChangedListener(editTextWatcher)
            }

            fun bind(
                block: BlockView,
                menuListener: (BlockMenuAction) -> Unit,
                linksListener: (String) -> Unit,
                focusListener: (Int) -> Unit
            ) = with(block) {

                itemView.textQuote.customSelectionActionModeCallback =
                    TextStyleCallback(itemView.textQuote)

                if (block is BlockView.Editable) {
                    itemView.textQuote.setText(block.text)
                }

                setFocusListener(
                    editText = itemView.textQuote,
                    focusListener = focusListener
                )
                itemView.btnQuote.setOnClickListener {
                    showBlockMenu(
                        context = itemView.context,
                        block = block,
                        menuListener = menuListener,
                        parent = it
                    )
                }
            }
        }

        class CheckBoxHolder(itemView: View, val editTextWatcher: MyEditorTextWatcher) :
            ViewHolder(itemView) {

            init {
                itemView.textCheckBox.addTextChangedListener(editTextWatcher)
            }

            fun bind(
                block: BlockView,
                menuListener: (BlockMenuAction) -> Unit,
                linksListener: (String) -> Unit,
                focusListener: (Int) -> Unit
            ) = with(block) {
                itemView.textCheckBox.customSelectionActionModeCallback =
                    TextStyleCallback(itemView.textCheckBox)

                if (block is BlockView.Editable)
                    itemView.textCheckBox.setText(block.text)

                setFocusListener(
                    editText = itemView.textCheckBox,
                    focusListener = focusListener
                )
                itemView.btnCheckboxBlock.setOnClickListener {
                    showBlockMenu(
                        context = itemView.context,
                        block = block,
                        menuListener = menuListener,
                        parent = it
                    )
                }
            }
        }

        class CodeSnippetHolder(itemView: View, val editTextWatcher: MyEditorTextWatcher) :
            ViewHolder(itemView) {

            init {
                itemView.textCode.addTextChangedListener(editTextWatcher)
            }

            fun bind(
                block: BlockView,
                menuListener: (BlockMenuAction) -> Unit,
                linksListener: (String) -> Unit,
                focusListener: (Int) -> Unit
            ) = with(block) {

                if (block is BlockView.Editable)
                    itemView.textCode.setText(block.text)

                setFocusListener(
                    editText = itemView.textCode,
                    focusListener = focusListener
                )
                itemView.btnCode.setOnClickListener {
                    showBlockMenu(
                        context = itemView.context,
                        block = block,
                        menuListener = menuListener,
                        parent = it
                    )
                }
            }
        }

        class BulletHolder(itemView: View, val editTextWatcher: MyEditorTextWatcher) :
            ViewHolder(itemView) {

            init {
                itemView.textBullet.addTextChangedListener(editTextWatcher)
            }

            fun bind(
                block: BlockView,
                menuListener: (BlockMenuAction) -> Unit,
                linksListener: (String) -> Unit,
                focusListener: (Int) -> Unit
            ) = with(block) {
                itemView.textBullet.customSelectionActionModeCallback =
                    TextStyleCallback(itemView.textBullet)

                if (block is BlockView.Editable) itemView.textBullet.setText(
                    SpannableString(block.text)
                        .withBulletSpan(gapWidth = 40, start = 0), TextView.BufferType.SPANNABLE
                )

                setFocusListener(
                    editText = itemView.textBullet,
                    focusListener = focusListener
                )
                itemView.btnBullet.setOnClickListener {
                    showBlockMenu(
                        context = itemView.context,
                        block = block,
                        menuListener = menuListener,
                        parent = it
                    )
                }
            }
        }

        class NumberedHolder(itemView: View, val editTextWatcher: MyEditorTextWatcher) :
            ViewHolder(itemView) {

            init {
                itemView.contentText.addTextChangedListener(editTextWatcher)
            }

            fun bind(
                block: BlockView,
                menuListener: (BlockMenuAction) -> Unit,
                linksListener: (String) -> Unit,
                focusListener: (Int) -> Unit
            ) {
                with(itemView) {

                    contentText.customSelectionActionModeCallback = TextStyleCallback(contentText)

                    if (block is BlockView.NumberListItemView)
                        positionText.text = "${block.number}."

                    if (block is BlockView.Editable)
                        contentText.setText(block.text)
                }

                setFocusListener(
                    editText = itemView.contentText,
                    focusListener = focusListener
                )

                itemView.btnNumbered.setOnClickListener {
                    showBlockMenu(
                        context = itemView.context,
                        block = block,
                        menuListener = menuListener,
                        parent = it
                    )
                }
            }
        }

        fun showBlockMenu(
            context: Context,
            parent: View,
            block: BlockView,
            menuListener: (BlockMenuAction) -> Unit
        ) {
            val menu = BlockMenu(context, block) { menuListener.invoke(it) }
            menu.showAtLocation(parent, Gravity.BOTTOM, 0, 0)
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
            s?.let { chars ->
                val block = blocks[position]
                if (block is BlockView.Editable && chars is SpannableStringBuilder) {
                    block.text = SpannableString.valueOf(chars)
                    contentListener.invoke(block)
                }
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
        const val HOLDER_PAGE = 10
        const val HOLDER_BOOKMARK = 11
        const val HOLDER_DIVIDER = 12
        const val HOLDER_PICTURE = 13

    }
}

