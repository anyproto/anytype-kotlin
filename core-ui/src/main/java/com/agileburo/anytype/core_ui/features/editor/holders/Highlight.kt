package com.agileburo.anytype.core_ui.features.editor.holders

import android.text.Editable
import android.view.View
import androidx.core.view.updateLayoutParams
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.common.isLinksPresent
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder
import com.agileburo.anytype.core_ui.features.page.ListenerType
import com.agileburo.anytype.core_ui.features.page.TextHolder
import com.agileburo.anytype.core_ui.menu.ContextMenuType
import com.agileburo.anytype.core_ui.tools.DefaultSpannableFactory
import com.agileburo.anytype.core_ui.tools.DefaultTextWatcher
import com.agileburo.anytype.core_ui.widgets.text.EditorLongClickListener
import com.agileburo.anytype.core_ui.widgets.text.TextInputWidget
import com.agileburo.anytype.core_utils.ext.dimen
import kotlinx.android.synthetic.main.item_block_highlight.view.*

class Highlight(
    view: View,
    onMarkupActionClicked: (Markup.Type, IntRange) -> Unit
) : BlockViewHolder(view), TextHolder, BlockViewHolder.IndentableHolder {

    override val content: TextInputWidget = itemView.highlightContent
    override val root: View = itemView
    private val indent = itemView.highlightIndent
    private val container = itemView.highlightBlockContentContainer

    init {
        content.setSpannableFactory(DefaultSpannableFactory())
        setup(onMarkupActionClicked, ContextMenuType.HIGHLIGHT)
    }

    fun bind(
        item: BlockView.Highlight,
        onTextChanged: (String, Editable) -> Unit,
        onFocusChanged: (String, Boolean) -> Unit,
        onSelectionChanged: (String, IntRange) -> Unit,
        clicked: (ListenerType) -> Unit
    ) {
        //indentize(item)

        if (item.mode == BlockView.Mode.READ) {
            enableReadOnlyMode()
            setBlockText(text = item.text, markup = item, clicked = clicked)
        } else {
            enableEditMode()
            setLinksClickable(item)
            setBlockText(text = item.text, markup = item, clicked = clicked)
            if (item.isFocused) setCursor(item)
            setFocus(item)
            with(content) {
                clearTextWatchers()
                setOnFocusChangeListener { _, hasFocus ->
                    onFocusChanged(item.id, hasFocus)
                }
                addTextChangedListener(
                    DefaultTextWatcher { text ->
                        onTextChanged(item.id, text)
                    }
                )
                setOnLongClickListener(
                    EditorLongClickListener(
                        t = item.id,
                        click = { onBlockLongClick(itemView, it, clicked) }
                    )
                )
                selectionWatcher = {
                    onSelectionChanged(item.id, it)
                }
            }
        }
    }

    override fun select(item: BlockView.Selectable) {
        container.isSelected = item.isSelected
    }

    override fun indentize(item: BlockView.Indentable) {
        indent.updateLayoutParams {
            width = item.indent * dimen(R.dimen.indent)
        }
    }

    override fun getMentionImageSizeAndPadding(): Pair<Int, Int> = with(itemView) {
        Pair(
            first = resources.getDimensionPixelSize(R.dimen.mention_span_image_size_default),
            second = resources.getDimensionPixelSize(R.dimen.mention_span_image_padding_default)
        )
    }

    /**
     *  Should be set before @[setBlockText]!
     */
    private fun setLinksClickable(block: BlockView.Highlight) {
        if (block.marks.isLinksPresent()) {
            content.setLinksClickable()
        }
    }
}