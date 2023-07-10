package com.anytypeio.anytype.core_ui.features.editor.holders.text

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockHighlightBinding
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.SupportNesting
import com.anytypeio.anytype.core_ui.features.editor.decoration.DecoratableViewHolder
import com.anytypeio.anytype.core_ui.features.editor.decoration.EditorDecorationContainer
import com.anytypeio.anytype.core_ui.features.editor.decoration.applySelectorOffset
import com.anytypeio.anytype.core_ui.tools.DefaultSpannableFactory
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class Highlight(
    val binding: ItemBlockHighlightBinding,
    clicked: (ListenerType) -> Unit,
) : Text<BlockView.Text.Highlight>(binding.root, clicked), BlockViewHolder.IndentableHolder, SupportNesting, DecoratableViewHolder {

    override val content: TextInputWidget = binding.highlightContent
    override val root: View = itemView
    override val selectionView: View = binding.selectionView

    override val decoratableContainer: EditorDecorationContainer = binding.decorationContainer

    private val mentionIconSize: Int
    private val mentionIconPadding: Int
    private val mentionCheckedIcon: Drawable?
    private val mentionUncheckedIcon: Drawable?
    private val mentionInitialsSize: Float

    init {
        content.setSpannableFactory(DefaultSpannableFactory())
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

    override fun select(item: BlockView.Selectable) {
        if (item.isSelected) {
            selectionView.isSelected = true
            selectionView.visible()
        } else {
            selectionView.isSelected = false
            selectionView.gone()
        }
    }

    @Deprecated("Pre-nested-styling legacy.")
    override fun indentize(item: BlockView.Indentable) {
        // Do nothing.
    }

    override fun getMentionIconSize(): Int = mentionIconSize
    override fun getMentionIconPadding(): Int = mentionIconPadding
    override fun getMentionCheckedIcon(): Drawable? = mentionCheckedIcon
    override fun getMentionUncheckedIcon(): Drawable? = mentionUncheckedIcon
    override fun getMentionInitialsSize(): Float = mentionInitialsSize

    override fun applyDecorations(decorations: List<BlockView.Decoration>) {
        decoratableContainer.decorate(decorations) { rect ->
            binding.highlightBlockContentContainer.updateLayoutParams<FrameLayout.LayoutParams> {
                marginStart = dimen(R.dimen.default_indent) + rect.left
                marginEnd = dimen(R.dimen.dp_8) + rect.right
            }
            content.updateLayoutParams<LinearLayout.LayoutParams> {
                bottomMargin = rect.bottom + dimen(R.dimen.dp_2)
            }
        }
        if (decorations.isNotEmpty()) {
            when (val style = decorations.last().style) {
                is BlockView.Decoration.Style.Highlight.End -> {
                    binding.highlightGlyphContainer.updateLayoutParams<LinearLayout.LayoutParams> {
                        bottomMargin = dimen(R.dimen.dp_6)
                    }
                }
                is BlockView.Decoration.Style.Highlight.Itself -> {
                    binding.highlightGlyphContainer.updateLayoutParams<LinearLayout.LayoutParams> {
                        bottomMargin = if (style.hasChildren) 0 else dimen(R.dimen.dp_6)
                    }
                }
                else -> {
                    binding.highlightGlyphContainer.updateLayoutParams<LinearLayout.LayoutParams> {
                        bottomMargin = 0
                    }
                }
            }
        }
        selectionView.applySelectorOffset<FrameLayout.LayoutParams>(
            content = binding.highlightBlockContentContainer,
            res = itemView.resources
        )
        selectionView.updateLayoutParams<FrameLayout.LayoutParams> {
            marginEnd = dimen(R.dimen.dp_8)
        }
    }
}