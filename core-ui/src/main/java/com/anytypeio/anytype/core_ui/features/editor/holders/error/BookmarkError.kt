package com.anytypeio.anytype.core_ui.features.editor.holders.error

import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.core_ui.BuildConfig
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockBookmarkErrorBinding
import com.anytypeio.anytype.core_ui.features.editor.decoration.DecoratableCardViewHolder
import com.anytypeio.anytype.core_ui.features.editor.decoration.EditorDecorationContainer
import com.anytypeio.anytype.core_ui.features.editor.decoration.applySelectorOffset
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.indentize
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class BookmarkError(
    val binding: ItemBlockBookmarkErrorBinding
) : MediaError(binding.root), DecoratableCardViewHolder {

    override val root: View = binding.bookmarkErrorRoot
    private val urlView: TextView = binding.errorBookmarkUrl

    override val decoratableContainer: EditorDecorationContainer
        get() = binding.decorationContainer
    override val decoratableCard: View
        get() = binding.card

    fun setUrl(url: String) {
        urlView.text = url.ifEmpty { null }
    }

    override fun errorClick(item: BlockView.Error, clicked: (ListenerType) -> Unit) {
        if (item is BlockView.Error.Bookmark) {
            clicked(ListenerType.Bookmark.Error(item))
        }
    }

    override fun indentize(item: BlockView.Indentable) {
        if (!BuildConfig.NESTED_DECORATION_ENABLED) {
            root.indentize(
                indent = item.indent,
                defIndent = dimen(R.dimen.indent),
                margin = 0
            )
            binding.selected.updateLayoutParams<FrameLayout.LayoutParams> {
                marginStart = dimen(R.dimen.default_indent) * item.indent
            }
        }
    }

    override fun select(isSelected: Boolean) {
        binding.selected.isSelected = isSelected
    }

    override fun applyDecorations(decorations: List<BlockView.Decoration>) {
        super.applyDecorations(decorations)
        if (BuildConfig.NESTED_DECORATION_ENABLED) {
            binding.selected.applySelectorOffset<FrameLayout.LayoutParams>(
                content = binding.card,
                res = itemView.resources
            )
        }
    }
}