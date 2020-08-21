package com.agileburo.anytype.core_ui.features.editor.holders.text

import android.text.Editable
import android.view.View
import androidx.core.view.updatePadding
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.features.page.*
import com.agileburo.anytype.core_ui.menu.ContextMenuType
import com.agileburo.anytype.core_ui.widgets.text.TextInputWidget
import com.agileburo.anytype.core_utils.ext.dimen
import kotlinx.android.synthetic.main.item_block_text.view.*

class Paragraph(
    view: View,
    onMarkupActionClicked: (Markup.Type, IntRange) -> Unit
) : Text(view), SupportNesting {

    override val root: View = itemView
    override val content: TextInputWidget = itemView.textContent

    init {
        setup(onMarkupActionClicked, ContextMenuType.TEXT)
    }

    fun bind(
        item: BlockView.Text.Paragraph,
        onTextChanged: (BlockView.Text.Paragraph) -> Unit,
        onSelectionChanged: (String, IntRange) -> Unit,
        onFocusChanged: (String, Boolean) -> Unit,
        clicked: (ListenerType) -> Unit,
        onMentionEvent: (MentionEvent) -> Unit,
        onEndLineEnterClicked: (String, Editable) -> Unit,
        onSplitLineEnterClicked: (String, Int, Editable) -> Unit,
        onEmptyBlockBackspaceClicked: (String) -> Unit,
        onNonEmptyBlockBackspaceClicked: (String, Editable) -> Unit,
        onTextInputClicked: (String) -> Unit
    ) = super.bind(
        item = item,
        onTextChanged = { _, editable ->
            item.apply {
                text = editable.toString()
                marks = editable.marks()
            }
            onTextChanged(item)
        },
        onSelectionChanged = onSelectionChanged,
        onFocusChanged = onFocusChanged,
        clicked = clicked,
        onEndLineEnterClicked = onEndLineEnterClicked,
        onEmptyBlockBackspaceClicked = onEmptyBlockBackspaceClicked,
        onSplitLineEnterClicked = onSplitLineEnterClicked,
        onNonEmptyBlockBackspaceClicked = onNonEmptyBlockBackspaceClicked,
        onTextInputClicked = onTextInputClicked
    ).also {
        setupMentionWatcher(onMentionEvent)
    }

    override fun getMentionImageSizeAndPadding(): Pair<Int, Int> = with(itemView) {
        Pair(
            first = resources.getDimensionPixelSize(R.dimen.mention_span_image_size_default),
            second = resources.getDimensionPixelSize(R.dimen.mention_span_image_padding_default)
        )
    }

    override fun indentize(item: BlockView.Indentable) {
        content.updatePadding(
            left = dimen(R.dimen.default_document_content_padding_start) + item.indent * dimen(R.dimen.indent)
        )
    }
}