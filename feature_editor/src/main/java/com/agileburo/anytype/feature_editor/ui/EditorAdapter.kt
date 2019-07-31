package com.agileburo.anytype.feature_editor.ui

import android.content.Context
import android.text.Editable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
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
import kotlinx.android.synthetic.main.item_block_bookmark.view.*
import kotlinx.android.synthetic.main.item_block_bullet.view.*
import kotlinx.android.synthetic.main.item_block_checkbox.view.*
import kotlinx.android.synthetic.main.item_block_code_snippet.view.*
import kotlinx.android.synthetic.main.item_block_header_four.view.*
import kotlinx.android.synthetic.main.item_block_header_one.view.*
import kotlinx.android.synthetic.main.item_block_header_three.view.*
import kotlinx.android.synthetic.main.item_block_header_two.view.*
import kotlinx.android.synthetic.main.item_block_image.view.*
import kotlinx.android.synthetic.main.item_block_quote.view.*
import kotlinx.android.synthetic.main.item_number_list_item.view.*
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_block_editable.view.btnEditable
import kotlinx.android.synthetic.main.item_block_editable.view.textEditable
import kotlinx.android.synthetic.main.item_block_toggle.view.*
import kotlin.contracts.contract


class EditorAdapter(
    val blocks: MutableList<BlockView>,
    private val blockContentListener: (BlockView) -> Unit,
    private val menuListener: (BlockMenuAction) -> Unit,
    private val focusListener: (Int) -> Unit,
    private val onExpandClick : (BlockView) -> Unit
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
            HOLDER_PAGE -> {
                val view = inflater.inflate(R.layout.item_block_link_to_page, parent, false)
                ViewHolder.LinkToPageHolder(view)
            }
            HOLDER_BOOKMARK -> {
                val view = inflater.inflate(R.layout.item_block_bookmark, parent, false)
                ViewHolder.BookmarkHolder(view)
            }
            HOLDER_DIVIDER -> {
                val view = inflater.inflate(R.layout.item_block_divider, parent, false)
                ViewHolder.DividerHolder(view)
            }
            HOLDER_PICTURE -> {
                val view = inflater.inflate(R.layout.item_block_image, parent, false)
                ViewHolder.PictureHolder(view)
            }
            HOLDER_TOGGLE -> {
                val view = inflater.inflate(R.layout.item_block_toggle, parent, false)
                ViewHolder.ToggleHolder(
                    itemView = view,
                    editTextWatcher = MyEditorTextWatcher(blockContentListener)
                )
            }
            else -> throw IllegalStateException("Unknown toView type: $viewType")
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
            is ToggleView -> HOLDER_TOGGLE
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
                    focusListener = focusListener
                )
            }
            is ViewHolder.HeaderOneHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    menuListener = menuListener,
                    focusListener = focusListener
                )
            }
            is ViewHolder.HeaderTwoHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    menuListener = menuListener,
                    focusListener = focusListener
                )
            }
            is ViewHolder.HeaderThreeHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    menuListener = menuListener,
                    focusListener = focusListener
                )
            }
            is ViewHolder.HeaderFourHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    menuListener = menuListener,
                    focusListener = focusListener
                )
            }
            is ViewHolder.QuoteHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    menuListener = menuListener,
                    focusListener = focusListener
                )
            }
            is ViewHolder.CheckBoxHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    menuListener = menuListener,
                    focusListener = focusListener
                )
            }
            is ViewHolder.CodeSnippetHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    menuListener = menuListener,
                    focusListener = focusListener
                )
            }
            is ViewHolder.BulletHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    menuListener = menuListener,
                    focusListener = focusListener
                )
            }
            is ViewHolder.NumberedHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    menuListener = menuListener,
                    focusListener = focusListener
                )
            }
            is ViewHolder.BookmarkHolder -> {
                holder.bind(blocks[position] as BookmarkView)
            }
            is ViewHolder.PictureHolder -> {
                holder.bind(blocks[position] as PictureView)
            }
            is ViewHolder.ToggleHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    view = blocks[position],
                    onExpandClick = onExpandClick,
                    menuListener = menuListener
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

        open class IndentableViewHolder(itemView: View) : ViewHolder(itemView) {

            fun applyIndent(indent : Int = 0) {
                (itemView.layoutParams as RecyclerView.LayoutParams).apply {
                    setMargins(
                        (indent * itemView.context.resources.getDimension(R.dimen.indent).toInt()),
                        this.topMargin,
                        this.rightMargin,
                        this.bottomMargin
                    )
                }
            }

        }

        class ParagraphHolder(
            itemView: View,
            val editTextWatcher: MyEditorTextWatcher
        ) : IndentableViewHolder(itemView) {

            init {
                itemView.textEditable.apply {
                    addTextChangedListener(editTextWatcher)
                    movementMethod = LinkMovementMethod.getInstance()
                }
            }

            fun bind(
                block: BlockView,
                menuListener: (BlockMenuAction) -> Unit,
                focusListener: (Int) -> Unit
            ) = with(block) {

                check(block is ParagraphView)

                itemView.textEditable.customSelectionActionModeCallback =
                    TextStyleCallback(itemView.textEditable) { editText, start, end ->
                        showHyperlinkMenu(
                            context = itemView.context,
                            parent = itemView.textEditable,
                            editText = editText,
                            start = start,
                            end = end
                        )
                    }

                itemView.textEditable.setText(block.text)

                applyIndent(block.indent)

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

        class HeaderOneHolder(
            itemView: View,
            val editTextWatcher: MyEditorTextWatcher
        ) : IndentableViewHolder(itemView) {

            init {
                itemView.textHeaderOne.apply {
                    addTextChangedListener(editTextWatcher)
                }
            }

            fun bind(
                block: BlockView,
                menuListener: (BlockMenuAction) -> Unit,
                focusListener: (Int) -> Unit
            ) = with(block) {

                check(block is HeaderView)

                applyIndent(block.indent)

                itemView.textHeaderOne.setText(block.text)

                setFocusListener(
                    editText = itemView.textHeaderOne,
                    focusListener = focusListener
                )
            }
        }

        class HeaderTwoHolder(
            itemView: View,
            val editTextWatcher: MyEditorTextWatcher
        ) : IndentableViewHolder(itemView) {

            init {
                itemView.textHeaderTwo.apply {
                    addTextChangedListener(editTextWatcher)
                }
            }

            fun bind(
                block: BlockView,
                menuListener: (BlockMenuAction) -> Unit,
                focusListener: (Int) -> Unit
            ) = with(block) {

                check(block is HeaderView)

                applyIndent(block.indent)

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

        class HeaderThreeHolder(
            itemView: View,
            val editTextWatcher: MyEditorTextWatcher
        ) : IndentableViewHolder(itemView) {

            init {
                itemView.textHeaderThree.apply {
                    addTextChangedListener(editTextWatcher)
                }
            }

            fun bind(
                block: BlockView,
                menuListener: (BlockMenuAction) -> Unit,
                focusListener: (Int) -> Unit
            ) = with(block) {

                check(block is HeaderView)

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

        class HeaderFourHolder(
            itemView: View,
            val editTextWatcher: MyEditorTextWatcher
        ) : IndentableViewHolder(itemView) {

            init {
                itemView.textHeaderFour.apply {
                    addTextChangedListener(editTextWatcher)
                }
            }

            fun bind(
                block: BlockView,
                menuListener: (BlockMenuAction) -> Unit,
                focusListener: (Int) -> Unit
            ) = with(block) {

                check(block is HeaderView)

                itemView.textHeaderFour.setText(block.text)

                applyIndent(block.indent)

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

        class QuoteHolder(
            itemView: View,
            val editTextWatcher: MyEditorTextWatcher
        ) : IndentableViewHolder(itemView) {

            init {
                itemView.textQuote.apply {
                    addTextChangedListener(editTextWatcher)
                    movementMethod = LinkMovementMethod.getInstance()
                }
            }

            fun bind(
                block: BlockView,
                menuListener: (BlockMenuAction) -> Unit,
                focusListener: (Int) -> Unit
            ) = with(block) {

                check(block is QuoteView)

                applyIndent(indent = block.indent)

                itemView.textQuote.customSelectionActionModeCallback =
                    TextStyleCallback(itemView.textQuote)
                    { editText, start, end ->
                        showHyperlinkMenu(
                            context = itemView.context,
                            editText = editText,
                            start = start,
                            end = end,
                            parent = itemView.textEditable
                        )
                    }

                itemView.textQuote.setText(block.text)

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

        class CheckBoxHolder(
            itemView: View,
            val editTextWatcher: MyEditorTextWatcher
        ) : IndentableViewHolder(itemView) {

            init {
                itemView.textCheckBox.apply {
                    addTextChangedListener(editTextWatcher)
                    movementMethod = LinkMovementMethod.getInstance()
                }
            }

            fun bind(
                block: BlockView,
                menuListener: (BlockMenuAction) -> Unit,
                focusListener: (Int) -> Unit
            ) = with(block) {

                check(block is CheckboxView)

                applyIndent(block.indent)

                itemView.textCheckBox.customSelectionActionModeCallback =
                    TextStyleCallback(itemView.textCheckBox)
                    { editText, start, end ->
                        showHyperlinkMenu(
                            context = itemView.context,
                            editText = editText,
                            start = start,
                            end = end,
                            parent = itemView.textEditable
                        )
                    }

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

        class CodeSnippetHolder(
            itemView: View,
            val editTextWatcher: MyEditorTextWatcher
        ) : IndentableViewHolder(itemView) {

            init {
                itemView.textCode.apply {
                    addTextChangedListener(editTextWatcher)
                }
            }

            fun bind(
                block: BlockView,
                menuListener: (BlockMenuAction) -> Unit,
                focusListener: (Int) -> Unit
            ) = with(block) {

                check(block is CodeSnippetView)

                applyIndent(block.indent)

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

        class BulletHolder(
            itemView: View,
            val editTextWatcher: MyEditorTextWatcher
        ) : IndentableViewHolder(itemView) {

            init {
                itemView.textBullet.apply {
                    addTextChangedListener(editTextWatcher)
                    movementMethod = LinkMovementMethod.getInstance()
                }
            }

            fun bind(
                block: BlockView,
                menuListener: (BlockMenuAction) -> Unit,
                focusListener: (Int) -> Unit
            ) = with(block) {

                check(block is BulletView)

                itemView.textBullet.customSelectionActionModeCallback =
                    TextStyleCallback(itemView.textBullet)
                    { editText, start, end ->
                        showHyperlinkMenu(
                            context = itemView.context,
                            editText = editText,
                            start = start,
                            end = end,
                            parent = itemView.textEditable
                        )
                    }

                applyIndent(block.indent)

                itemView.textBullet.setText(
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

        class NumberedHolder(
            itemView: View,
            val editTextWatcher: MyEditorTextWatcher
        ) : IndentableViewHolder(itemView) {

            init {
                itemView.contentText.apply {
                    addTextChangedListener(editTextWatcher)
                    movementMethod = LinkMovementMethod.getInstance()
                }
            }

            fun bind(
                block: BlockView,
                menuListener: (BlockMenuAction) -> Unit,
                focusListener: (Int) -> Unit
            ) {

                check(block is NumberListItemView)

                applyIndent(block.indent)

                with(itemView) {

                    contentText.customSelectionActionModeCallback = TextStyleCallback(contentText)
                    { editText, start, end ->
                        showHyperlinkMenu(
                            context = itemView.context,
                            editText = editText,
                            start = start,
                            end = end,
                            parent = itemView.textEditable
                        )
                    }

                    positionText.text = "${block.number}."
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

        class ToggleHolder(
            val editTextWatcher: MyEditorTextWatcher,
            itemView: View
        ) : IndentableViewHolder(itemView) {

            init {
                itemView.textEditable.apply {
                    addTextChangedListener(editTextWatcher)
                }
            }

            fun bind(
                view : BlockView,
                onExpandClick: (BlockView) -> Unit,
                menuListener: (BlockMenuAction) -> Unit
            ) {

                check(view is ToggleView)

                applyIndent(view.indent)

                itemView.blockMenuButton.setOnClickListener {
                    showBlockMenu(
                        context = itemView.context,
                        block = view,
                        menuListener = menuListener,
                        parent = it
                    )
                }

                itemView.apply {

                    textEditable.setText(view.text)

                    toggleArrow.rotation = if (view.expanded) 90f else 0f

                    arrowContainer.setOnClickListener { onExpandClick(view) }
                }
            }
        }

        class LinkToPageHolder(itemView: View) : ViewHolder(itemView)

        class DividerHolder(itemView: View) : ViewHolder(itemView)

        class PictureHolder(itemView: View) : ViewHolder(itemView) {

            fun bind(view : PictureView) {
                Glide.with(itemView)
                    .load(view.url)
                    .centerCrop()
                    .into(itemView.picture)
            }

        }

        class BookmarkHolder(itemView: View) : ViewHolder(itemView) {

            fun bind(
                view : BookmarkView
            ) {
                with(itemView) {
                    title.text = view.title
                    description.text = view.description
                    url.text = view.url

                    Glide.with(itemView)
                        .load(view.image)
                        .centerCrop()
                        .into(image)
                }
            }

        }

        fun showHyperlinkMenu(
            context: Context,
            parent: View,
            editText: EditText,
            start: Int,
            end: Int
        ) {
            val menu = HyperLinkMenu(
                context = context, editText = editText,
                start = start, end = end
            )
            menu.showAtLocation(parent, Gravity.BOTTOM, 0, 0)
        }

        fun showBlockMenu(
            context: Context,
            parent: View,
            block: BlockView,
            menuListener: (BlockMenuAction) -> Unit
        ) {
            BlockMenu(context, block) { menuListener.invoke(it) }.apply {
                showAtLocation(parent, Gravity.BOTTOM, 0, 0)
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
        const val HOLDER_TOGGLE = 14
    }
}

