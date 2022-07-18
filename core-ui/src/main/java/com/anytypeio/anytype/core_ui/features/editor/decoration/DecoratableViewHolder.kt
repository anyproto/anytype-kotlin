package com.anytypeio.anytype.core_ui.features.editor.decoration

import android.content.res.Resources
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.CallSuper
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.core_ui.BuildConfig
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.model.Indent

interface DecoratableViewHolder {
    val decoratableContainer: EditorDecorationContainer
    fun applyDecorations(decorations: List<BlockView.Decoration>)
    fun onDecorationsChanged(decorations: List<BlockView.Decoration>) {
        applyDecorations(decorations = decorations)
    }
}

interface DecoratableCardViewHolder: DecoratableViewHolder {
    val decoratableCard: View
    @CallSuper
    override fun applyDecorations(decorations: List<BlockView.Decoration>) {
        if (BuildConfig.NESTED_DECORATION_ENABLED) {
            decoratableContainer.decorate(decorations) { rect ->
                decoratableCard.applyCardDecorations<FrameLayout.LayoutParams>(
                    rect = rect,
                    res = decoratableCard.resources,
                    indent = decorations.lastIndex
                )
            }
        }
    }
}

/**
 * Applying decorations for card blocks (media blocks, placeholders, etc.)
 */
inline fun <reified LP : ViewGroup.MarginLayoutParams> View.applyCardDecorations(
    rect: Rect,
    res: Resources,
    indent: Indent
) = updateLayoutParams<LP> {
    val defaultIndentOffset = res.getDimension(R.dimen.default_indent).toInt()
    marginStart = if (indent == 0) defaultIndentOffset + rect.left else rect.left
    marginEnd = defaultIndentOffset + rect.right
    topMargin = res.getDimension(R.dimen.card_block_extra_space_top).toInt()
    bottomMargin = res.getDimension(R.dimen.card_block_extra_space_bottom).toInt() + rect.bottom
}

/**
 * Setting offsets for selection view.
 */
inline fun <reified LP : ViewGroup.MarginLayoutParams> View.applySelectorOffset(
    content: View,
    res: Resources
) = updateLayoutParams<LP> {
    val selectorLeftRightOffset = res.getDimension(R.dimen.selection_left_right_offset).toInt()
    marginStart = content.marginStart - selectorLeftRightOffset
    marginEnd = content.marginEnd - selectorLeftRightOffset
    topMargin = content.marginTop
    bottomMargin = content.marginBottom
}