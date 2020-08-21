package com.agileburo.anytype.core_ui.features.editor.holders.text

import android.text.Editable
import android.view.View
import androidx.core.view.updateLayoutParams
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.BlockViewHolder
import com.agileburo.anytype.core_ui.features.page.ListenerType
import com.agileburo.anytype.core_ui.menu.ContextMenuType
import com.agileburo.anytype.core_ui.tools.DefaultSpannableFactory
import com.agileburo.anytype.core_ui.widgets.text.TextInputWidget
import com.agileburo.anytype.core_utils.ext.dimen
import kotlinx.android.synthetic.main.item_block_highlight.view.*

class Highlight(
    view: View,
    onMarkupActionClicked: (Markup.Type, IntRange) -> Unit
) : Text(view), BlockViewHolder.IndentableHolder {

    override val content: TextInputWidget = itemView.highlightContent
    override val root: View = itemView
    private val indent = itemView.highlightIndent
    private val container = itemView.highlightBlockContentContainer

    init {
        content.setSpannableFactory(DefaultSpannableFactory())
        setup(onMarkupActionClicked, ContextMenuType.HIGHLIGHT)
    }

    fun bind(
        item: BlockView.Text.Highlight,
        onTextChanged: (String, Editable) -> Unit,
        onFocusChanged: (String, Boolean) -> Unit,
        onSelectionChanged: (String, IntRange) -> Unit,
        clicked: (ListenerType) -> Unit,
        onEndLineEnterClicked: (String, Editable) -> Unit,
        onSplitLineEnterClicked: (String, Int, Editable) -> Unit,
        onEmptyBlockBackspaceClicked: (String) -> Unit,
        onNonEmptyBlockBackspaceClicked: (String, Editable) -> Unit,
        onTextInputClicked: (String) -> Unit
    ) = super.bind(
        item = item,
        onTextChanged = onTextChanged,
        onFocusChanged = onFocusChanged,
        onSelectionChanged = onSelectionChanged,
        clicked = clicked,
        onEndLineEnterClicked = onEndLineEnterClicked,
        onEmptyBlockBackspaceClicked = onEmptyBlockBackspaceClicked,
        onSplitLineEnterClicked = onSplitLineEnterClicked,
        onNonEmptyBlockBackspaceClicked = onNonEmptyBlockBackspaceClicked,
        onTextInputClicked = onTextInputClicked
    )

    override fun select(item: BlockView.Selectable) {
        container.isSelected = item.isSelected
    }

    override fun indentize(item: BlockView.Indentable) {
        indent.updateLayoutParams { width = item.indent * dimen(R.dimen.indent) }
    }

    override fun getMentionImageSizeAndPadding(): Pair<Int, Int> = with(itemView) {
        Pair(
            first = resources.getDimensionPixelSize(R.dimen.mention_span_image_size_default),
            second = resources.getDimensionPixelSize(R.dimen.mention_span_image_padding_default)
        )
    }
}