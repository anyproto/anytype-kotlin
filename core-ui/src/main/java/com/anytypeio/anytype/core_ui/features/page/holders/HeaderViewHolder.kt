package com.anytypeio.anytype.core_ui.features.page.holders

import android.text.Editable
import android.view.View
import androidx.core.view.updatePadding
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.page.BlockTextEvent
import com.anytypeio.anytype.core_ui.features.page.marks
import com.anytypeio.anytype.core_ui.features.page.models.BlockTextViewHolder
import com.anytypeio.anytype.core_ui.features.page.models.Item
import com.anytypeio.anytype.core_ui.tools.*
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.ext.dimen
import kotlinx.android.synthetic.main.item_block_header_one.view.*
import kotlinx.android.synthetic.main.item_block_header_three.view.*
import kotlinx.android.synthetic.main.item_block_header_two.view.*

abstract class HeaderViewHolder constructor(
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
    abstract override val content: TextInputWidget

    override val spannableFactory: DefaultSpannableFactory = DefaultSpannableFactory()

    override fun indentize(indent: Int) {
        content.updatePadding(left = defaultPadding + indent * dimen(R.dimen.indent))
    }

    override fun getMentionSizes(): Pair<Int, Int> {
        TODO("Not yet implemented")
    }

    override fun select(isSelected: Boolean) {
        TODO("Not yet implemented")
    }

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
        event(BlockTextEvent.TextEvent.Default(id, item))
    }
}

class HeaderOneViewHolder constructor(
    view: View,
    textWatcher: BlockTextWatcher,
    mentionWatcher: BlockTextMentionWatcher,
    backspaceWatcher: BlockTextBackspaceWatcher,
    enterWatcher: BlockTextEnterWatcher,
    actionMenu: BlockTextMenu
) : HeaderViewHolder(
    view, textWatcher, mentionWatcher, backspaceWatcher, enterWatcher, actionMenu
) {
    override val content: TextInputWidget get() = itemView.headerOne
    override fun getMentionSizes(): Pair<Int, Int> = with(itemView) {
        Pair(
            first = resources.getDimensionPixelSize(R.dimen.mention_span_image_size_header_one),
            second = resources.getDimensionPixelSize(R.dimen.mention_span_image_padding_header_one)
        )
    }

    override fun select(isSelected: Boolean) {
        TODO("Not yet implemented")
    }
}

class HeaderTwoViewHolder constructor(
    view: View,
    textWatcher: BlockTextWatcher,
    mentionWatcher: BlockTextMentionWatcher,
    backspaceWatcher: BlockTextBackspaceWatcher,
    enterWatcher: BlockTextEnterWatcher,
    actionMenu: BlockTextMenu
) : HeaderViewHolder(
    view, textWatcher, mentionWatcher, backspaceWatcher, enterWatcher, actionMenu
) {
    override val content: TextInputWidget get() = itemView.headerTwo
    override fun getMentionSizes(): Pair<Int, Int> = with(itemView) {
        Pair(
            first = resources.getDimensionPixelSize(R.dimen.mention_span_image_size_header_two),
            second = resources.getDimensionPixelSize(R.dimen.mention_span_image_padding_header_two)
        )
    }
}

class HeaderThreeViewHolder constructor(
    view: View,
    textWatcher: BlockTextWatcher,
    mentionWatcher: BlockTextMentionWatcher,
    backspaceWatcher: BlockTextBackspaceWatcher,
    enterWatcher: BlockTextEnterWatcher,
    actionMenu: BlockTextMenu
) : HeaderViewHolder(
    view, textWatcher, mentionWatcher, backspaceWatcher, enterWatcher, actionMenu
) {
    override val content: TextInputWidget get() = itemView.headerThree
    override fun getMentionSizes(): Pair<Int, Int> = with(itemView) {
        Pair(
            first = resources.getDimensionPixelSize(R.dimen.mention_span_image_size_header_three),
            second = resources.getDimensionPixelSize(R.dimen.mention_span_image_padding_default)
        )
    }
}