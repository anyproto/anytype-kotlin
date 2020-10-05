package com.anytypeio.anytype.core_ui.features.editor.holders.text

import android.text.Editable
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.ThemeColor
import com.anytypeio.anytype.core_ui.features.page.*
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.ext.addDot
import com.anytypeio.anytype.core_utils.ext.dimen
import kotlinx.android.synthetic.main.item_block_numbered.view.*

class Numbered(
    view: View,
    onContextMenuStyleClick: (IntRange) -> Unit
) : Text(view), SupportNesting {

    private val container = itemView.numberedBlockContentContainer
    val number = itemView.number
    override val content: TextInputWidget = itemView.numberedListContent
    override val root: View = itemView

    init {
        setup(onContextMenuStyleClick)
    }

    fun bind(
        item: BlockView.Text.Numbered,
        onTextBlockTextChanged: (BlockView.Text) -> Unit,
        onSelectionChanged: (String, IntRange) -> Unit,
        onFocusChanged: (String, Boolean) -> Unit,
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
        onFocusChanged = onFocusChanged,
        clicked = clicked,
        onEmptyBlockBackspaceClicked = onEmptyBlockBackspaceClicked,
        onSplitLineEnterClicked = onSplitLineEnterClicked,
        onNonEmptyBlockBackspaceClicked = onNonEmptyBlockBackspaceClicked,
        onTextInputClicked = onTextInputClicked,
        onBackPressedCallback = onBackPressedCallback
    ).also {
        setNumber(item)
        setupMentionWatcher(onMentionEvent)
    }

    private fun setNumber(item: BlockView.Text.Numbered) {
        number.gravity = when (item.number) {
            in 1..19 -> Gravity.CENTER_HORIZONTAL
            else -> Gravity.START
        }
        number.text = item.number.addDot()
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
        onTextChanged: (BlockView.Text) -> Unit,
        onSelectionChanged: (String, IntRange) -> Unit,
        clicked: (ListenerType) -> Unit
    ) {
        super.processChangePayload(payloads, item, onTextChanged, onSelectionChanged, clicked)
        payloads.forEach { payload ->
            if (payload.changes.contains(BlockViewDiffUtil.NUMBER_CHANGED))
                number.text = (item as BlockView.Text.Numbered).number.addDot()
        }
    }

    override fun setTextColor(color: String) {
        super.setTextColor(color)
        number.setTextColor(ThemeColor.values().first { value -> value.title == color }.text)
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