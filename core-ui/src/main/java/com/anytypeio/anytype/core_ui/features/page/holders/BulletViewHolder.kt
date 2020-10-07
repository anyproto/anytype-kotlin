package com.anytypeio.anytype.core_ui.features.page.holders

import android.text.Editable
import android.view.View
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.ThemeColor
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_ui.extensions.tint
import com.anytypeio.anytype.core_ui.features.page.BlockTextEvent
import com.anytypeio.anytype.core_ui.features.page.marks
import com.anytypeio.anytype.core_ui.features.page.models.BlockTextViewHolder
import com.anytypeio.anytype.core_ui.features.page.models.Item
import com.anytypeio.anytype.core_ui.tools.*
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.ext.dimen
import kotlinx.android.synthetic.main.item_block_bulleted.view.*

class BulletViewHolder(
    view: View,
    textWatcher: BlockTextWatcher,
    mentionWatcher: BlockTextMentionWatcher,
    backspaceWatcher: BlockTextBackspaceWatcher,
    enterWatcher: BlockTextEnterWatcher,
    actionMenu: BlockTextMenu
) : BlockTextViewHolder(
    view, textWatcher, mentionWatcher, backspaceWatcher, enterWatcher, actionMenu
) {

    private val bulletIndent = itemView.bulletIndent
    private val bullet = itemView.bullet
    private val container = itemView.bulletBlockContainer

    override val content: TextInputWidget
        get() = itemView.bulletedListContent

    override val spannableFactory: DefaultSpannableFactory
        get() = DefaultSpannableFactory()

    override fun indentize(indent: Int) {
        bulletIndent.updateLayoutParams { width = indent * dimen(R.dimen.indent) }
    }

    override fun getMentionSizes(): Pair<Int, Int> =
        with(itemView) {
            Pair(
                first = resources.getDimensionPixelSize(R.dimen.mention_span_image_size_default),
                second = resources.getDimensionPixelSize(R.dimen.mention_span_image_padding_default)
            )
        }

    override fun setTextColor(textColor: Int) {
        super.setTextColor(textColor)
        bullet.setColorFilter(textColor)
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
        event(BlockTextEvent.TextEvent.Default(id, item))
    }
}
