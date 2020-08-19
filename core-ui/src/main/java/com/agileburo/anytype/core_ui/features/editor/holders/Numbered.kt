package com.agileburo.anytype.core_ui.features.editor.holders

import android.text.Editable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.updateLayoutParams
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.common.isLinksPresent
import com.agileburo.anytype.core_ui.extensions.color
import com.agileburo.anytype.core_ui.features.page.*
import com.agileburo.anytype.core_ui.menu.ContextMenuType
import com.agileburo.anytype.core_ui.tools.DefaultTextWatcher
import com.agileburo.anytype.core_ui.widgets.text.EditorLongClickListener
import com.agileburo.anytype.core_ui.widgets.text.TextInputWidget
import com.agileburo.anytype.core_utils.ext.addDot
import com.agileburo.anytype.core_utils.ext.dimen
import kotlinx.android.synthetic.main.item_block_numbered.view.*

class Numbered(
    view: View,
    onMarkupActionClicked: (Markup.Type, IntRange) -> Unit
) : BlockViewHolder(view), TextHolder, BlockViewHolder.IndentableHolder, SupportNesting {

    private val container = itemView.numberedBlockContentContainer
    val number = itemView.number
    override val content: TextInputWidget = itemView.numberedListContent
    override val root: View = itemView

    init {
        setup(onMarkupActionClicked, ContextMenuType.TEXT)
    }

    fun bind(
        item: BlockView.Numbered,
        onTextChanged: (String, Editable) -> Unit,
        onSelectionChanged: (String, IntRange) -> Unit,
        onFocusChanged: (String, Boolean) -> Unit,
        clicked: (ListenerType) -> Unit
    ) {
        indentize(item)

        if (item.mode == BlockView.Mode.READ) {
            enableReadOnlyMode()

            select(item)

            number.gravity = when (item.number) {
                in 1..19 -> Gravity.CENTER_HORIZONTAL
                else -> Gravity.START
            }

            number.text = item.number.addDot()

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
            indentize(item)
            number.gravity = when (item.number) {
                in 1..19 -> Gravity.CENTER_HORIZONTAL
                else -> Gravity.START
            }
            number.text = item.number.addDot()

            if (item.marks.isLinksPresent()) {
                content.setLinksClickable()
            }

            setBlockText(text = item.text, markup = item, clicked = clicked)

            if (item.color != null)
                setTextColor(item.color)
            else
                setTextColor(content.context.color(R.color.black))

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

    override fun processChangePayload(
        payloads: List<BlockViewDiffUtil.Payload>,
        item: BlockView,
        onTextChanged: (String, Editable) -> Unit,
        onSelectionChanged: (String, IntRange) -> Unit,
        clicked: (ListenerType) -> Unit
    ) {
        super.processChangePayload(payloads, item, onTextChanged, onSelectionChanged, clicked)
        payloads.forEach { payload ->
            if (payload.changes.contains(BlockViewDiffUtil.NUMBER_CHANGED))
                number.text = "${(item as BlockView.Numbered).number}"
        }
    }

    override fun indentize(item: BlockView.Indentable) {
        number.updateLayoutParams<LinearLayout.LayoutParams> {
            setMargins(
                item.indent * dimen(R.dimen.indent),
                0,
                0,
                0
            )
        }
    }

    override fun select(item: BlockView.Selectable) {
        container.isSelected = item.isSelected
    }
}