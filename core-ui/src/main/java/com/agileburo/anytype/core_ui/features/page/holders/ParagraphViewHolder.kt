package com.agileburo.anytype.core_ui.features.page.holders

import android.text.Editable
import android.view.View
import androidx.core.view.updatePadding
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.features.page.BlockTextEvent
import com.agileburo.anytype.core_ui.features.page.marks
import com.agileburo.anytype.core_ui.features.page.models.BlockTextViewHolder
import com.agileburo.anytype.core_ui.features.page.models.Item
import com.agileburo.anytype.core_ui.tools.*
import com.agileburo.anytype.core_ui.widgets.text.TextInputWidget
import com.agileburo.anytype.core_utils.ext.dimen
import kotlinx.android.synthetic.main.item_block_text.view.*

class ParagraphViewHolder constructor(
    view: View,
    textWatcher: BlockTextWatcher,
    mentionWatcher: BlockTextMentionWatcher,
    backspaceWatcher: BlockTextBackspaceWatcher,
    enterWatcher: BlockTextEnterWatcher,
    actionMenu: BlockTextMenu
) : BlockTextViewHolder(
    view, textWatcher, mentionWatcher, backspaceWatcher, enterWatcher, actionMenu
) {

    private val defaultPadding = dimen(R.dimen.default_document_content_padding_start)

    override val content: TextInputWidget get() = itemView.textContent

    override fun indentize(indent: Int) {
        content.updatePadding(left = defaultPadding + indent * dimen(R.dimen.indent))
    }

    override fun getMentionSizes(): Pair<Int, Int> = with(itemView) {
        Pair(
            first = resources.getDimensionPixelSize(R.dimen.mention_span_image_size_default),
            second = resources.getDimensionPixelSize(R.dimen.mention_span_image_padding_default)
        )
    }

    override fun select(isSelected: Boolean) {}

    override fun onTextEvent(
        event: (BlockTextEvent) -> Unit,
        id: String,
        item: Item,
        editable: Editable
    ) {
        item.apply {
            text = editable.toString()
            marks = editable.marks()
        }
        event(BlockTextEvent.TextEvent.Pattern(id, item))
    }
}