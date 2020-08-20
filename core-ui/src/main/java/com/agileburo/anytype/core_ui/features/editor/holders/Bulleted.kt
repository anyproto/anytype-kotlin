package com.agileburo.anytype.core_ui.features.editor.holders

import android.text.Editable
import android.view.View
import androidx.core.view.updateLayoutParams
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.common.ThemeColor
import com.agileburo.anytype.core_ui.extensions.color
import com.agileburo.anytype.core_ui.extensions.tint
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.ListenerType
import com.agileburo.anytype.core_ui.features.page.SupportNesting
import com.agileburo.anytype.core_ui.menu.ContextMenuType
import com.agileburo.anytype.core_ui.widgets.text.TextInputWidget
import com.agileburo.anytype.core_utils.ext.dimen
import kotlinx.android.synthetic.main.item_block_bulleted.view.*

class Bulleted(
    view: View,
    onMarkupActionClicked: (Markup.Type, IntRange) -> Unit
) : Text(view), SupportNesting {

    val indent: View = itemView.bulletIndent
    private val bullet = itemView.bullet
    private val container = itemView.bulletBlockContainer
    override val content: TextInputWidget = itemView.bulletedListContent
    override val root: View = itemView

    init {
        setup(onMarkupActionClicked, ContextMenuType.TEXT)
    }

    fun bind(
        item: BlockView.Bulleted,
        onTextChanged: (String, Editable) -> Unit,
        onSelectionChanged: (String, IntRange) -> Unit,
        onFocusChanged: (String, Boolean) -> Unit,
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

    override fun getMentionImageSizeAndPadding(): Pair<Int, Int> = with(itemView) {
        Pair(
            first = resources.getDimensionPixelSize(R.dimen.mention_span_image_size_default),
            second = resources.getDimensionPixelSize(R.dimen.mention_span_image_padding_default)
        )
    }

    override fun setTextColor(color: String) {
        super.setTextColor(color)
        bullet.setColorFilter(
            ThemeColor.values().first { value ->
                value.title == color
            }.text
        )
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