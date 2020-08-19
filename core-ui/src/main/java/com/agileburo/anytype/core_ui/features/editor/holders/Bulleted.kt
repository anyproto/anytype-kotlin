package com.agileburo.anytype.core_ui.features.editor.holders

import android.text.Editable
import android.view.View
import androidx.core.view.updateLayoutParams
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.common.ThemeColor
import com.agileburo.anytype.core_ui.common.isLinksPresent
import com.agileburo.anytype.core_ui.extensions.color
import com.agileburo.anytype.core_ui.extensions.tint
import com.agileburo.anytype.core_ui.features.page.*
import com.agileburo.anytype.core_ui.menu.ContextMenuType
import com.agileburo.anytype.core_ui.tools.DefaultTextWatcher
import com.agileburo.anytype.core_ui.widgets.text.EditorLongClickListener
import com.agileburo.anytype.core_ui.widgets.text.TextInputWidget
import com.agileburo.anytype.core_utils.ext.dimen
import kotlinx.android.synthetic.main.item_block_bulleted.view.*

class Bulleted(
    view: View,
    onMarkupActionClicked: (Markup.Type, IntRange) -> Unit
) : BlockViewHolder(view), TextHolder, BlockViewHolder.IndentableHolder, SupportNesting {

    val indent = itemView.bulletIndent
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
        clicked: (ListenerType) -> Unit
    ) {
        indentize(item)

        if (item.mode == BlockView.Mode.READ) {

            enableReadOnlyMode()

            select(item)

            setBlockText(text = item.text, markup = item, clicked = clicked)

            if (item.color != null)
                setTextColor(item.color)
            else
                setTextColor(content.context.color(R.color.black))

        } else {

            enableEditMode()

            select(item)

            content.setOnLongClickListener(
                EditorLongClickListener(
                    t = item.id,
                    click = { onBlockLongClick(root, it, clicked) }
                )
            )

            content.clearTextWatchers()

            if (item.marks.isLinksPresent()) {
                content.setLinksClickable()
            }

            setBlockText(text = item.text, markup = item, clicked = clicked)

            if (item.color != null) {
                setTextColor(item.color)
            } else {
                setTextColor(content.context.color(R.color.black))
            }

            if (item.isFocused) setCursor(item)

            setFocus(item)

            content.addTextChangedListener(
                DefaultTextWatcher { text ->
                    onTextChanged(item.id, text)
                }
            )

            content.setOnFocusChangeListener { _, hasFocus ->
                onFocusChanged(item.id, hasFocus)
            }

            content.selectionWatcher = {
                onSelectionChanged(item.id, it)
            }
        }
    }

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