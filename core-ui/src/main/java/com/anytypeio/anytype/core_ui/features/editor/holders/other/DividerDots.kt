package com.anytypeio.anytype.core_ui.features.editor.holders.other

import android.view.View
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.core_ui.BuildConfig
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockDividerDotsBinding
import com.anytypeio.anytype.core_ui.features.editor.decoration.DecoratableViewHolder
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class DividerDots(
    private val binding: ItemBlockDividerDotsBinding
) : Divider(binding.root), DecoratableViewHolder {

    override val container: View = binding.container
    override val decoratableContainer = binding.decorationContainer

    init {
        applyDefaultOffsets()
    }

    fun bind(item: BlockView.DividerDots, clicked: (ListenerType) -> Unit) {
        super.bind(
            id = item.id,
            item = item,
            isItemSelected = item.isSelected,
            clicked = clicked,
            background = item.backgroundColor
        )
    }

    override fun applyDecorations(decorations: List<BlockView.Decoration>) {
        if (BuildConfig.NESTED_DECORATION_ENABLED) {
            decoratableContainer.decorate(decorations) { rect ->
                val defaultIndentOffset = dimen(R.dimen.default_indent)
                container.updateLayoutParams<FrameLayout.LayoutParams> {
                    marginStart = rect.left + defaultIndentOffset
                    marginEnd = rect.right + defaultIndentOffset
                    bottomMargin = rect.bottom + dimen(R.dimen.divider_extra_space_bottom)
                }
            }
        }
    }
}