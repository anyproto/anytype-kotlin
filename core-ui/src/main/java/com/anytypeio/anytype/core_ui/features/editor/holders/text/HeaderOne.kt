package com.anytypeio.anytype.core_ui.features.editor.holders.text

import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import kotlinx.android.synthetic.main.item_block_header_one.view.*

class HeaderOne(
    view: View,
    onContextMenuStyleClick: (IntRange) -> Unit
) : Header(view) {

    override val header: TextInputWidget = itemView.headerOne
    override val content: TextInputWidget get() = header
    override val root: View = itemView

    private val mentionIconSize: Int
    private val mentionIconPadding: Int
    private val mentionCheckedIcon: Drawable?
    private val mentionUncheckedIcon: Drawable?
    private val mentionInitialsSize: Float

    init {
        setup(onContextMenuStyleClick)
        with(itemView.context) {
            mentionIconSize =
                resources.getDimensionPixelSize(R.dimen.mention_span_image_size_header_one)
            mentionIconPadding =
                resources.getDimensionPixelSize(R.dimen.mention_span_image_padding_header_one)
            mentionUncheckedIcon = ContextCompat.getDrawable(this, R.drawable.ic_task_0_h1)
            mentionCheckedIcon = ContextCompat.getDrawable(this, R.drawable.ic_task_1_h1)
            mentionInitialsSize = resources.getDimension(R.dimen.mention_span_initials_size_header_one)
        }
    }

    override fun getMentionIconSize(): Int = mentionIconSize
    override fun getMentionIconPadding(): Int = mentionIconPadding
    override fun getMentionCheckedIcon(): Drawable? = mentionCheckedIcon
    override fun getMentionUncheckedIcon(): Drawable? = mentionUncheckedIcon
    override fun getMentionInitialsSize(): Float = mentionInitialsSize
}