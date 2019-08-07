package com.agileburo.anytype.feature_editor.ui

import android.content.Context
import android.graphics.Color
import android.text.*
import android.text.Editable
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.agileburo.anytype.core_utils.showKeyboard
import com.agileburo.anytype.core_utils.swap
import com.agileburo.anytype.feature_editor.R
import com.agileburo.anytype.feature_editor.presentation.model.BlockView
import com.agileburo.anytype.feature_editor.presentation.model.BlockView.*
import com.agileburo.anytype.feature_editor.presentation.model.BlockView.HeaderView.HeaderType
import com.agileburo.anytype.feature_editor.presentation.util.BlockViewDiffUtil
import com.agileburo.anytype.feature_editor.presentation.util.SwapRequest
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_block_bookmark.view.*
import kotlinx.android.synthetic.main.item_block_bullet.view.*
import kotlinx.android.synthetic.main.item_block_checkbox.view.*
import kotlinx.android.synthetic.main.item_block_code_snippet.view.*
import kotlinx.android.synthetic.main.item_block_editable.view.*
import kotlinx.android.synthetic.main.item_block_editable.view.textEditable
import kotlinx.android.synthetic.main.item_block_header_four.view.*
import kotlinx.android.synthetic.main.item_block_header_one.view.*
import kotlinx.android.synthetic.main.item_block_header_three.view.*
import kotlinx.android.synthetic.main.item_block_header_two.view.*
import kotlinx.android.synthetic.main.item_block_image.view.*
import kotlinx.android.synthetic.main.item_block_quote.view.*
import kotlinx.android.synthetic.main.item_block_toggle.view.*
import kotlinx.android.synthetic.main.item_number_list_item.view.*
import timber.log.Timber

class EditorAdapter(
    val blocks: MutableList<BlockView>,
    private val blockContentListener: (BlockView) -> Unit,
    private val menuListener: (BlockMenuAction) -> Unit,
    private val focusListener: (Int) -> Unit,
    private val onExpandClick: (BlockView) -> Unit,
    private val onEnterPressed: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun setBlocks(items: List<BlockView>) {
        blocks.addAll(items)
        notifyDataSetChanged()
    }

    fun clearSelected() {
        blocks.forEach { it.isSelected = false }
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
                    focusListener = focusListener,
                    onEnterPressed = onEnterPressed
                )
            }
            is ViewHolder.HeaderOneHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    menuListener = menuListener,
                    focusListener = focusListener,
                    onEnterPressed = onEnterPressed
                )
            }
            is ViewHolder.HeaderTwoHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    menuListener = menuListener,
                    focusListener = focusListener,
                    onEnterPressed = onEnterPressed
                )
            }
            is ViewHolder.HeaderThreeHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    menuListener = menuListener,
                    focusListener = focusListener,
                    onEnterPressed = onEnterPressed
                )
            }
            is ViewHolder.HeaderFourHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    menuListener = menuListener,
                    focusListener = focusListener,
                    onEnterPressed = onEnterPressed
                )
            }
            is ViewHolder.QuoteHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    menuListener = menuListener,
                    focusListener = focusListener,
                    onEnterPressed = onEnterPressed
                )
            }
            is ViewHolder.CheckBoxHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    menuListener = menuListener,
                    focusListener = focusListener,
                    onEnterPressed = onEnterPressed
                )
            }
            is ViewHolder.CodeSnippetHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    menuListener = menuListener,
                    focusListener = focusListener,
                    onEnterPressed = onEnterPressed
                )
            }
            is ViewHolder.BulletHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    menuListener = menuListener,
                    focusListener = focusListener,
                    onEnterPressed = onEnterPressed
                )
            }
            is ViewHolder.NumberedHolder -> {
                holder.editTextWatcher.position = holder.adapterPosition
                holder.bind(
                    block = blocks[position],
                    menuListener = menuListener,
                    focusListener = focusListener,
                    onEnterPressed = onEnterPressed
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
                    menuListener = menuListener,
                    onEnterPressed = onEnterPressed
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

    fun remove(position: Int) {
        blocks.removeAt(position)
        notifyItemRemoved(position)
    }

    private fun swapPosition(fromPosition: Int, toPosition: Int) =
        blocks.swap(fromPosition, toPosition)

    private fun setIsEnabled(editText: EditText) {
        editText.isEnabled = false
        editText.isEnabled = true
    }

    sealed class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun select(selected: Boolean) {
            if (selected) {
                itemView.setBackgroundColor(Color.LTGRAY)
            } else {
                itemView.setBackgroundColor(0)
            }
        }

        fun focus(editText: EditText) {
            editText.apply {
                postDelayed(
                    {
                        requestFocus()
                        showKeyboard()
                    }, 300
                )
            }
        }

        open class IndentableViewHolder(itemView: View) : ViewHolder(itemView) {

            fun applyIndent(indent: Int = 0) {
                (itemView.layoutParams as RecyclerView.LayoutParams).apply {
                    setMargins(
                        (indent * itemView.context.resources.getDimension(R.dimen.indent).toInt()),
                        this.topMargin,
                        this.rightMargin,
                        this.bottomMargin
                    )
                }
            }

            fun watchEnter(
                editText: EditText,
                onEnterPressed: (String) -> Unit,
                block: BlockView
            ) {

                editText.setOnEditorActionListener(null)

                editText.setOnEditorActionListener { _, id, event ->
                    if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                        onEnterPressed(block.id)
                        true
                    } else if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_ACTION_NEXT) {
                        onEnterPressed(block.id)
                        false
                    } else
                        false
                }
            }

        }

        class ParagraphHolder(
            itemView: View,
            val editTextWatcher: MyEditorTextWatcher
        ) : IndentableViewHolder(itemView), ItemTouchHelperViewHolder {

            private val inputField = itemView.textEditable
            private val editButton = itemView.btnEditable

            init {
                inputField.textEditable.apply {
                    addTextChangedListener(editTextWatcher)
                    //movementMethod = LinkMovementMethod.getInstance()
                    imeOptions = EditorInfo.IME_ACTION_NEXT
                    setRawInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
                }
            }

            fun bind(
                block: BlockView,
                menuListener: (BlockMenuAction) -> Unit,
                focusListener: (Int) -> Unit,
                onEnterPressed: (String) -> Unit
            ) = with(block) {

                check(block is ParagraphView)

                applyIndent(block.indent)

                inputField.apply {

                    isLongClickable = false

                    customSelectionActionModeCallback = TextStyleCallback(this) { editText, start, end ->
                        showHyperlinkMenu(
                            context = itemView.context,
                            parent = this,
                            editText = editText,
                            start = start,
                            end = end
                        )
                    }

                    setText(block.text)

                    if (block.focused) focus(this)

                    watchEnter(this, onEnterPressed, block)

                    setFocusListener(
                        editText = this,
                        focusListener = focusListener
                    )
                }

                editButton.setOnClickListener {
                    showBlockMenu(
                        context = itemView.context,
                        block = block,
                        menuListener = menuListener,
                        parent = it
                    )
                }

                select(isSelected)
            }

            override fun targetView() {
                itemView.border_top.visibility = View.VISIBLE
            }

            override fun targetViewBottom() {
                itemView.border_bottom.visibility = View.VISIBLE
            }

            override fun clearTargetView() {
                itemView.border_top.visibility = View.INVISIBLE
                itemView.border_bottom.visibility = View.INVISIBLE
            }
        }

        class HeaderOneHolder(
            itemView: View,
            val editTextWatcher: MyEditorTextWatcher
        ) : IndentableViewHolder(itemView) {

            private val inputField = itemView.textHeaderOne

            init {
                inputField.apply {
                    addTextChangedListener(editTextWatcher)
                    imeOptions = EditorInfo.IME_ACTION_NEXT
                    setRawInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
                }
            }

            fun bind(
                block: BlockView,
                menuListener: (BlockMenuAction) -> Unit,
                focusListener: (Int) -> Unit,
                onEnterPressed: (String) -> Unit
            ) = with(block) {

                check(block is HeaderView)

                applyIndent(block.indent)

                inputField.apply {

                    isLongClickable = false

                    setText(block.text)

                    watchEnter(this, onEnterPressed, block)
                    if (block.focused) focus(this)

                    setFocusListener(
                        editText = this,
                        focusListener = focusListener
                    )
                }
            }
        }

        class HeaderTwoHolder(
            itemView: View,
            val editTextWatcher: MyEditorTextWatcher
        ) : IndentableViewHolder(itemView), ItemTouchHelperViewHolder {

            private val inputField = itemView.textHeaderTwo
            private val editButton = itemView.btnHeaderTwo

            init {
                inputField.apply {
                    addTextChangedListener(editTextWatcher)
                    imeOptions = EditorInfo.IME_ACTION_NEXT
                    setRawInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
                }
            }

            fun bind(
                block: BlockView,
                menuListener: (BlockMenuAction) -> Unit,
                focusListener: (Int) -> Unit,
                onEnterPressed: (String) -> Unit
            ) = with(block) {

                check(block is HeaderView)

                applyIndent(block.indent)

                inputField.apply {

                    setText(block.text)
                    isLongClickable = false

                    watchEnter(this, onEnterPressed, block)
                    if (block.focused) focus(this)

                    setFocusListener(
                        editText = this,
                        focusListener = focusListener
                    )
                }

                editButton.setOnClickListener {
                    showBlockMenu(
                        context = itemView.context,
                        block = block,
                        menuListener = menuListener,
                        parent = it
                    )
                }
            }

            override fun targetView() {
                itemView.borderTopHeaderTwo.visibility = View.VISIBLE
            }

            override fun targetViewBottom() {
                itemView.borderBottomHeaderTwo.visibility = View.VISIBLE
            }

            override fun clearTargetView() {
                itemView.borderTopHeaderTwo.visibility = View.INVISIBLE
                itemView.borderBottomHeaderTwo.visibility = View.INVISIBLE
            }
        }

        class HeaderThreeHolder(
            itemView: View,
            val editTextWatcher: MyEditorTextWatcher
        ) : IndentableViewHolder(itemView), ItemTouchHelperViewHolder {

            private val inputField = itemView.textHeaderThree
            private val editButton = itemView.btnHeaderThree

            init {
                inputField.apply {
                    addTextChangedListener(editTextWatcher)
                    imeOptions = EditorInfo.IME_ACTION_NEXT
                    setRawInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
                }
            }

            fun bind(
                block: BlockView,
                menuListener: (BlockMenuAction) -> Unit,
                focusListener: (Int) -> Unit,
                onEnterPressed: (String) -> Unit
            ) = with(block) {

                check(block is HeaderView)

                applyIndent(block.indent)

                inputField.apply {

                    isLongClickable = false
                    setText(block.text)

                    watchEnter(this, onEnterPressed, block)
                    if (block.focused) focus(this)

                    setFocusListener(
                        editText = this,
                        focusListener = focusListener
                    )

                }

                editButton.setOnClickListener {
                    showBlockMenu(
                        context = itemView.context,
                        block = block,
                        menuListener = menuListener,
                        parent = it
                    )
                }
            }

            override fun targetView() {
                itemView.borderTopHeaderThree.visibility = View.VISIBLE
            }

            override fun targetViewBottom() {
                itemView.borderBottomHeaderThree.visibility = View.VISIBLE
            }

            override fun clearTargetView() {
                itemView.borderTopHeaderThree.visibility = View.INVISIBLE
                itemView.borderBottomHeaderThree.visibility = View.INVISIBLE
            }
        }

        class HeaderFourHolder(
            itemView: View,
            val editTextWatcher: MyEditorTextWatcher
        ) : IndentableViewHolder(itemView), ItemTouchHelperViewHolder {

            private val inputField = itemView.textHeaderFour
            private val editButton = itemView.btnHeaderFour

            init {
                inputField.apply {
                    addTextChangedListener(editTextWatcher)
                    imeOptions = EditorInfo.IME_ACTION_NEXT
                    setRawInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
                }
            }

            fun bind(
                block: BlockView,
                menuListener: (BlockMenuAction) -> Unit,
                focusListener: (Int) -> Unit,
                onEnterPressed: (String) -> Unit
            ) = with(block) {

                check(block is HeaderView)

                applyIndent(block.indent)

                inputField.apply {

                    isLongClickable = false

                    setText(block.text)

                    watchEnter(this, onEnterPressed, block)
                    if (block.focused) focus(this)

                    setFocusListener(
                        editText = this,
                        focusListener = focusListener
                    )
                }

                editButton.setOnClickListener {
                    showBlockMenu(
                        context = itemView.context,
                        block = block,
                        menuListener = menuListener,
                        parent = it
                    )
                }
            }

            override fun targetView() {
                itemView.borderTopHeaderFour.visibility = View.VISIBLE
            }

            override fun targetViewBottom() {
                itemView.borderBottomHeaderFour.visibility = View.VISIBLE
            }

            override fun clearTargetView() {
                itemView.borderTopHeaderFour.visibility = View.INVISIBLE
                itemView.borderBottomHeaderFour.visibility = View.INVISIBLE
            }
        }

        class QuoteHolder(
            itemView: View,
            val editTextWatcher: MyEditorTextWatcher
        ) : IndentableViewHolder(itemView), ItemTouchHelperViewHolder {

            private val inputField = itemView.textQuote
            private val editButton = itemView.btnQuote

            init {
                inputField.apply {
                    addTextChangedListener(editTextWatcher)
                    //movementMethod = LinkMovementMethod.getInstance()
                    imeOptions = EditorInfo.IME_ACTION_NEXT
                    setRawInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
                }
            }

            fun bind(
                block: BlockView,
                menuListener: (BlockMenuAction) -> Unit,
                focusListener: (Int) -> Unit,
                onEnterPressed: (String) -> Unit
            ) = with(block) {

                check(block is QuoteView)

                applyIndent(indent = block.indent)

                inputField.apply {

                    customSelectionActionModeCallback =
                        TextStyleCallback(this)
                        { editText, start, end ->
                            showHyperlinkMenu(
                                context = itemView.context,
                                editText = editText,
                                start = start,
                                end = end,
                                parent = this
                            )
                        }

                    isLongClickable = false

                    setText(block.text)

                    watchEnter(this, onEnterPressed, block)

                    if (block.focused) focus(this)

                    setFocusListener(
                        editText = this,
                        focusListener = focusListener
                    )

                }

                editButton.setOnClickListener {
                    showBlockMenu(
                        context = itemView.context,
                        block = block,
                        menuListener = menuListener,
                        parent = it
                    )
                }
            }

            override fun targetView() {
                itemView.borderTopQuote.visibility = View.VISIBLE
            }

            override fun targetViewBottom() {
                itemView.borderBottomQuote.visibility = View.VISIBLE
            }

            override fun clearTargetView() {
                itemView.borderTopQuote.visibility = View.INVISIBLE
                itemView.borderBottomQuote.visibility = View.INVISIBLE
            }
        }

        class CheckBoxHolder(
            itemView: View,
            val editTextWatcher: MyEditorTextWatcher
        ) : IndentableViewHolder(itemView), ItemTouchHelperViewHolder, Consumer {

            private val inputField = itemView.textCheckBox
            private val editButton = itemView.btnCheckboxBlock

            init {
                inputField.apply {
                    addTextChangedListener(editTextWatcher)
                    //movementMethod = LinkMovementMethod.getInstance()
                    imeOptions = EditorInfo.IME_ACTION_NEXT
                    setRawInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
                }
            }

            fun bind(
                block: BlockView,
                menuListener: (BlockMenuAction) -> Unit,
                focusListener: (Int) -> Unit,
                onEnterPressed: (String) -> Unit
            ) = with(block) {

                check(block is CheckboxView)

                applyIndent(block.indent)

                inputField.apply {
                    setText(block.text)

                    isLongClickable = false

                    customSelectionActionModeCallback = TextStyleCallback(this) { editText, start, end ->
                        showHyperlinkMenu(
                            context = itemView.context,
                            editText = editText,
                            start = start,
                            end = end,
                            parent = this
                        )
                    }

                    watchEnter(this, onEnterPressed, block)
                    if (block.focused) focus(this)

                    setFocusListener(
                        editText = this,
                        focusListener = focusListener
                    )
                }

                editButton.setOnClickListener {
                    showBlockMenu(
                        context = itemView.context,
                        block = block,
                        menuListener = menuListener,
                        parent = it
                    )
                }
            }

            override fun targetView() {
                itemView.borderTopCheckBox.visibility = View.VISIBLE
            }

            override fun targetViewBottom() {
                itemView.borderBottomCheckBox.visibility = View.VISIBLE
            }

            override fun clearTargetView() {
                itemView.borderTopCheckBox.visibility = View.INVISIBLE
                itemView.borderBottomCheckBox.visibility = View.INVISIBLE
            }
        }

        class CodeSnippetHolder(
            itemView: View,
            val editTextWatcher: MyEditorTextWatcher
        ) : IndentableViewHolder(itemView), ItemTouchHelperViewHolder {

            private val inputField = itemView.textCode
            private val editButton = itemView.btnCode

            init {
                inputField.apply {
                    addTextChangedListener(editTextWatcher)
                    imeOptions = EditorInfo.IME_ACTION_NEXT
                    setRawInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
                }
            }

            fun bind(
                block: BlockView,
                menuListener: (BlockMenuAction) -> Unit,
                focusListener: (Int) -> Unit,
                onEnterPressed: (String) -> Unit
            ) = with(block) {

                check(block is CodeSnippetView)

                applyIndent(block.indent)

                inputField.apply {
                    setText(block.text)
                    isLongClickable = false

                    watchEnter(this, onEnterPressed, block)
                    if (block.focused) focus(this)

                    setFocusListener(
                        editText = this,
                        focusListener = focusListener
                    )
                }

                editButton.setOnClickListener {
                    showBlockMenu(
                        context = itemView.context,
                        block = block,
                        menuListener = menuListener,
                        parent = it
                    )
                }
            }

            override fun targetView() {
                itemView.borderTopCode.visibility = View.VISIBLE
            }

            override fun targetViewBottom() {
                itemView.borderBottomCode.visibility = View.VISIBLE
            }

            override fun clearTargetView() {
                itemView.borderTopCode.visibility = View.INVISIBLE
                itemView.borderBottomCode.visibility = View.INVISIBLE
            }
        }

        class BulletHolder(
            itemView: View,
            val editTextWatcher: MyEditorTextWatcher
        ) : IndentableViewHolder(itemView), ItemTouchHelperViewHolder, Consumer {

            private val inputField = itemView.textBullet
            private val editButton = itemView.btnBullet

            init {
                inputField.apply {
                    addTextChangedListener(editTextWatcher)
                    //movementMethod = LinkMovementMethod.getInstance()
                    imeOptions = EditorInfo.IME_ACTION_NEXT
                    setRawInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
                }
            }

            fun bind(
                block: BlockView,
                menuListener: (BlockMenuAction) -> Unit,
                focusListener: (Int) -> Unit,
                onEnterPressed: (String) -> Unit
            ) = with(block) {

                check(block is BulletView)

                applyIndent(block.indent)

                inputField.apply {
                    isLongClickable = false

                    setText(
                        SpannableString(block.text)
                            .withBulletSpan(gapWidth = 40, start = 0), TextView.BufferType.SPANNABLE
                    )

                    watchEnter(this, onEnterPressed, block)
                    if (block.focused) focus(this)

                    setFocusListener(
                        editText = this,
                        focusListener = focusListener
                    )

                    customSelectionActionModeCallback = TextStyleCallback(this) { editText, start, end ->
                        showHyperlinkMenu(
                            context = itemView.context,
                            editText = editText,
                            start = start,
                            end = end,
                            parent = this
                        )
                    }
                }

                editButton.setOnClickListener {
                    showBlockMenu(
                        context = itemView.context,
                        block = block,
                        menuListener = menuListener,
                        parent = it
                    )
                }
            }

            override fun targetView() {
                itemView.borderTopBullet.visibility = View.VISIBLE
            }

            override fun targetViewBottom() {
                itemView.borderBottomBullet.visibility = View.VISIBLE
            }

            override fun clearTargetView() {
                itemView.borderTopBullet.visibility = View.INVISIBLE
                itemView.borderBottomBullet.visibility = View.INVISIBLE
            }
        }

        class NumberedHolder(
            itemView: View,
            val editTextWatcher: MyEditorTextWatcher
        ) : IndentableViewHolder(itemView), ItemTouchHelperViewHolder, Consumer {

            private val inputField = itemView.contentText
            private val editButton = itemView.editContentButton

            init {
                itemView.contentText.apply {
                    addTextChangedListener(editTextWatcher)
                    //movementMethod = LinkMovementMethod.getInstance()
                    imeOptions = EditorInfo.IME_ACTION_NEXT
                    setRawInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
                }
            }

            fun bind(
                block: BlockView,
                menuListener: (BlockMenuAction) -> Unit,
                focusListener: (Int) -> Unit,
                onEnterPressed: (String) -> Unit
            ) {

                check(block is NumberListItemView)

                applyIndent(block.indent)

                inputField.apply {

                    isLongClickable = false

                    setText(block.text)

                    customSelectionActionModeCallback = TextStyleCallback(this) { editText, start, end ->
                        showHyperlinkMenu(
                            context = itemView.context,
                            editText = editText,
                            start = start,
                            end = end,
                            parent = this
                        )
                    }

                    watchEnter(inputField, onEnterPressed, block)
                    if (block.focused) focus(inputField)

                    setFocusListener(
                        editText = this,
                        focusListener = focusListener
                    )

                }

                itemView.positionText.text = "${block.number}."

                editButton.setOnClickListener {
                    showBlockMenu(
                        context = itemView.context,
                        block = block,
                        menuListener = menuListener,
                        parent = it
                    )
                }
            }

            override fun targetView() {
                itemView.borderTopNumbered.visibility = View.VISIBLE
            }

            override fun targetViewBottom() {
                itemView.borderBottomNumbered.visibility = View.VISIBLE
            }

            override fun clearTargetView() {
                itemView.borderTopNumbered.visibility = View.INVISIBLE
                itemView.borderBottomNumbered.visibility = View.INVISIBLE
            }
        }

        class LinkToPageHolder(itemView: View) : ViewHolder(itemView)

        class ToggleHolder(
            val editTextWatcher: MyEditorTextWatcher,
            itemView: View
        ) : IndentableViewHolder(itemView), ItemTouchHelperViewHolder, Consumer {

            private val inputField = itemView.textEditable
            private val editButton = itemView.blockMenuButton

            init {
                itemView.textEditable.apply {
                    addTextChangedListener(editTextWatcher)
                    imeOptions = EditorInfo.IME_ACTION_NEXT
                    setRawInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
                }
            }

            fun bind(
                view: BlockView,
                onExpandClick: (BlockView) -> Unit,
                menuListener: (BlockMenuAction) -> Unit,
                onEnterPressed: (String) -> Unit
            ) {

                check(view is ToggleView)

                applyIndent(view.indent)

                inputField.apply {

                    isLongClickable = false

                    setText(view.text)

                    watchEnter(this, onEnterPressed, view)
                    if (view.focused) focus(this)

                    // TODO add focus listener?

                }

                editButton.setOnClickListener {
                    showBlockMenu(
                        context = itemView.context,
                        block = view,
                        menuListener = menuListener,
                        parent = it
                    )
                }

                itemView.apply {
                    toggleArrow.rotation = if (view.expanded) 90f else 0f
                    arrowContainer.setOnClickListener { onExpandClick(view) }
                }

                select(itemView.isSelected)
            }

            override fun targetView() {
                itemView.topBorder.visibility = View.VISIBLE
            }

            override fun targetViewBottom() {
                itemView.bottomBorder.visibility = View.VISIBLE
            }

            override fun clearTargetView() {
                itemView.bottomBorder.visibility = View.INVISIBLE
                itemView.topBorder.visibility = View.INVISIBLE
            }
        }

        class DividerHolder(itemView: View) : ViewHolder(itemView)

        class PictureHolder(itemView: View) : ViewHolder(itemView) {

            fun bind(view: PictureView) {
                Glide.with(itemView)
                    .load(view.url)
                    .fitCenter()
                    .into(itemView.picture)
            }
        }

        class BookmarkHolder(itemView: View) : ViewHolder(itemView) {

            fun bind(
                view: BookmarkView
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
            editText.setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    view.isLongClickable = true
                    focusListener.invoke(adapterPosition)
                } else {
                    view.isLongClickable = false
                    focusListener.invoke(-1)
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

