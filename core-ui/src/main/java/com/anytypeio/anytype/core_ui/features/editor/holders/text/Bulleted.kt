package com.anytypeio.anytype.core_ui.features.editor.holders.text

import android.text.Editable
import android.view.View
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_ui.extensions.tint
import com.anytypeio.anytype.core_ui.features.page.SupportNesting
import com.anytypeio.anytype.core_ui.features.page.marks
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.presentation.page.editor.ThemeColor
import com.anytypeio.anytype.presentation.page.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.page.editor.mention.MentionEvent
import com.anytypeio.anytype.presentation.page.editor.model.BlockView
import kotlinx.android.synthetic.main.item_block_bulleted.view.*

class Bulleted(
    view: View,
    onContextMenuStyleClick: (IntRange) -> Unit
) : Text(view), SupportNesting {

    val indent: View = itemView.bulletIndent
    val bullet = itemView.bullet
    private val container = itemView.bulletBlockContainer
    override val content: TextInputWidget = itemView.bulletedListContent
    override val root: View = itemView

    init {
        setup(onContextMenuStyleClick)
    }

    fun bind(
        item: BlockView.Text.Bulleted,
        onTextBlockTextChanged: (BlockView.Text) -> Unit,
        onSelectionChanged: (String, IntRange) -> Unit,
        clicked: (ListenerType) -> Unit,
        onMentionEvent: (MentionEvent) -> Unit,
        onSplitLineEnterClicked: (String, Editable, IntRange) -> Unit,
        onEmptyBlockBackspaceClicked: (String) -> Unit,
        onNonEmptyBlockBackspaceClicked: (String, Editable) -> Unit,
        onTextInputClicked: (String) -> Unit,
        onBackPressedCallback: () -> Boolean
    ) = super.bind(
        item = item,
        onTextChanged = { _, editable ->
            item.apply {
                text = editable.toString()
                marks = editable.marks()
            }
            onTextBlockTextChanged(item)
        },
        onSelectionChanged = onSelectionChanged,
        clicked = clicked,
        onEmptyBlockBackspaceClicked = onEmptyBlockBackspaceClicked,
        onSplitLineEnterClicked = onSplitLineEnterClicked,
        onNonEmptyBlockBackspaceClicked = onNonEmptyBlockBackspaceClicked,
        onTextInputClicked = onTextInputClicked,
        onBackPressedCallback = onBackPressedCallback
    ).also {
        setupMentionWatcher(onMentionEvent)
    }

    override fun getMentionImageSizeAndPadding(): Pair<Int, Int> = with(itemView) {
        Pair(
            first = resources.getDimensionPixelSize(R.dimen.mention_span_image_size_default),
            second = resources.getDimensionPixelSize(R.dimen.mention_span_image_padding_default)
        )
    }

    override fun setTextColor(color: String) {
        super.setTextColor(color)
        val code = ThemeColor.values().find { value -> value.title == color }
        if (code != null) bullet.setColorFilter(code.text)
    }

    override fun setTextColor(color: Int) {
        super.setTextColor(color)
        bullet.tint(content.context.color(R.color.black))
    }

    override fun indentize(item: BlockView.Indentable) {
        indent.updateLayoutParams { width = item.indent * dimen(R.dimen.indent) }
    }

    override fun select(item: BlockView.Selectable) {
        container.isSelected = item.isSelected
    }
}