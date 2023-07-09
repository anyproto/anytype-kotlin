package com.anytypeio.anytype.core_ui.features.editor.holders.text

import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockHeaderThreeBinding
import com.anytypeio.anytype.core_ui.features.editor.decoration.EditorDecorationContainer
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class HeaderThree(
    val binding: ItemBlockHeaderThreeBinding,
    clicked: (ListenerType) -> Unit,
) : Header<BlockView.Text.Header.Three>(binding.root, clicked) {

    override val header: TextInputWidget = binding.headerThree
    override val content: TextInputWidget get() = header
    override val root: View = itemView
    override val selectionView: View = binding.selectionView

    private val mentionIconSize: Int
    private val mentionIconPadding: Int
    private val mentionCheckedIcon: Drawable?
    private val mentionUncheckedIcon: Drawable?
    private val mentionInitialsSize: Float

    override val decoratableContainer: EditorDecorationContainer
        get() = binding.decorationContainer

    override val contentTopMargin: Int = dimen(R.dimen.dp_16)

    init {
        setup()
        with(itemView.context) {
            mentionIconSize =
                resources.getDimensionPixelSize(R.dimen.mention_span_image_size_header_three)
            mentionIconPadding =
                resources.getDimensionPixelSize(R.dimen.mention_span_image_padding_header_three)
            mentionUncheckedIcon = ContextCompat.getDrawable(this, R.drawable.ic_task_0_text_17)
            mentionCheckedIcon = ContextCompat.getDrawable(this, R.drawable.ic_task_1_text_17)
            mentionInitialsSize =
                resources.getDimension(R.dimen.mention_span_initials_size_header_three)
        }
    }

    override fun getMentionIconSize(): Int = mentionIconSize
    override fun getMentionIconPadding(): Int = mentionIconPadding
    override fun getMentionCheckedIcon(): Drawable? = mentionCheckedIcon
    override fun getMentionUncheckedIcon(): Drawable? = mentionUncheckedIcon
    override fun getMentionInitialsSize(): Float = mentionInitialsSize
}