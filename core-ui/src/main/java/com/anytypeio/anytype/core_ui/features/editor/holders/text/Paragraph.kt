package com.anytypeio.anytype.core_ui.features.editor.holders.text

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockTextBinding
import com.anytypeio.anytype.core_ui.features.editor.SupportNesting
import com.anytypeio.anytype.core_ui.features.editor.decoration.DecoratableViewHolder
import com.anytypeio.anytype.core_ui.features.editor.decoration.EditorDecorationContainer
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class Paragraph(
    val binding: ItemBlockTextBinding,
    clicked: (ListenerType) -> Unit,
) : Text<BlockView.Text.Paragraph>(binding.root, clicked), SupportNesting, DecoratableViewHolder {

    override val root: View = binding.root
    override val content: TextInputWidget = binding.textContent
    override val decoratableContainer : EditorDecorationContainer = binding.decorationContainer
    override val selectionView: View = binding.selectionView
    private val mentionIconSize: Int
    private val mentionIconPadding: Int
    private val mentionCheckedIcon: Drawable?
    private val mentionUncheckedIcon: Drawable?
    private val mentionInitialsSize: Float

    init {
        setup()
        with(itemView.context) {
            mentionIconSize =
                resources.getDimensionPixelSize(R.dimen.mention_span_image_size_default)
            mentionIconPadding =
                resources.getDimensionPixelSize(R.dimen.mention_span_image_padding_default)
            mentionUncheckedIcon = ContextCompat.getDrawable(this, R.drawable.ic_task_0_text_16)
            mentionCheckedIcon = ContextCompat.getDrawable(this, R.drawable.ic_task_1_text_16)
            mentionInitialsSize = resources.getDimension(R.dimen.mention_span_initials_size_default)
        }
    }

    override fun getMentionIconSize(): Int = mentionIconSize
    override fun getMentionIconPadding(): Int = mentionIconPadding
    override fun getMentionCheckedIcon(): Drawable? = mentionCheckedIcon
    override fun getMentionUncheckedIcon(): Drawable? = mentionUncheckedIcon
    override fun getMentionInitialsSize(): Float = mentionInitialsSize

    @Deprecated("Pre-nested-styling legacy.")
    override fun indentize(item: BlockView.Indentable) {
        // Do nothing.
    }

    override fun applyDecorations(decorations: List<BlockView.Decoration>) {
        decoratableContainer.decorate(
            decorations = decorations
        ) { rect ->
            content.updateLayoutParams<FrameLayout.LayoutParams> {
                marginStart = dimen(R.dimen.dp_8) + rect.left
                marginEnd = dimen(R.dimen.dp_8) + rect.right
                bottomMargin = rect.bottom + dimen(R.dimen.dp_2)
            }
        }
    }
}