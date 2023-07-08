package com.anytypeio.anytype.core_ui.features.editor.holders.text

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockHeaderOneBinding
import com.anytypeio.anytype.core_ui.features.editor.decoration.EditorDecorationContainer
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class HeaderOne(
    val binding: ItemBlockHeaderOneBinding,
    clicked: (ListenerType) -> Unit,
) : Header<BlockView.Text.Header.One>(binding.root, clicked) {

    override val header: TextInputWidget = binding.headerOne
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

    init {
        setup()
        with(itemView.context) {
            mentionIconSize =
                resources.getDimensionPixelSize(R.dimen.mention_span_image_size_header_one)
            mentionIconPadding =
                resources.getDimensionPixelSize(R.dimen.mention_span_image_padding_header_one)
            mentionUncheckedIcon = ContextCompat.getDrawable(this, R.drawable.ic_task_0_h1)
            mentionCheckedIcon = ContextCompat.getDrawable(this, R.drawable.ic_task_1_h1)
            mentionInitialsSize =
                resources.getDimension(R.dimen.mention_span_initials_size_header_one)
        }
    }

    override fun getMentionIconSize(): Int = mentionIconSize
    override fun getMentionIconPadding(): Int = mentionIconPadding
    override fun getMentionCheckedIcon(): Drawable? = mentionCheckedIcon
    override fun getMentionUncheckedIcon(): Drawable? = mentionUncheckedIcon
    override fun getMentionInitialsSize(): Float = mentionInitialsSize

    override fun applyDecorations(decorations: List<BlockView.Decoration>) {
        decoratableContainer.decorate(decorations) { rect ->
            binding.box.updateLayoutParams<FrameLayout.LayoutParams> {
                marginStart = rect.left
                marginEnd = rect.right
                bottomMargin = rect.bottom
                // TODO handle top and bottom offsets
            }
        }
    }
}