package com.anytypeio.anytype.core_ui.features.editor.holders.text

import android.text.Editable
import android.view.View
import androidx.core.view.updatePadding
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.TextBlockHolder
import com.anytypeio.anytype.core_ui.features.editor.marks
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.mention.MentionEvent
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashEvent

abstract class Header(
    view: View
) : Text(view), TextBlockHolder, BlockViewHolder.IndentableHolder {

    abstract val header: TextInputWidget

    fun bind(
        block: BlockView.Text.Header,
        onTextBlockTextChanged: (BlockView.Text) -> Unit,
        clicked: (ListenerType) -> Unit,
        onMentionEvent: (MentionEvent) -> Unit,
        onSlashEvent: (SlashEvent) -> Unit,
        onSplitLineEnterClicked: (String, Editable, IntRange) -> Unit,
        onEmptyBlockBackspaceClicked: (String) -> Unit,
        onNonEmptyBlockBackspaceClicked: (String, Editable) -> Unit,
        onBackPressedCallback: () -> Boolean
    ) = super.bind(
        item = block,
        onTextChanged = { _, editable ->
            block.apply {
                text = editable.toString()
                marks = editable.marks()
            }
            onTextBlockTextChanged(block)
        },
        clicked = clicked,
        onEmptyBlockBackspaceClicked = onEmptyBlockBackspaceClicked,
        onSplitLineEnterClicked = onSplitLineEnterClicked,
        onNonEmptyBlockBackspaceClicked = onNonEmptyBlockBackspaceClicked,
        onBackPressedCallback = onBackPressedCallback
    ).also {
        setupMentionWatcher(onMentionEvent)
        setupSlashWatcher(onSlashEvent, block.getViewType())
    }

    override fun indentize(item: BlockView.Indentable) {
        header.updatePadding(
            left = dimen(R.dimen.default_document_content_padding_start) + item.indent * dimen(R.dimen.indent)
        )
    }
}