package com.anytypeio.anytype.core_ui.features.editor.holders.upload

import android.view.View
import android.widget.FrameLayout
import com.anytypeio.anytype.core_models.Url
import com.anytypeio.anytype.core_ui.BuildConfig
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockBookmarkUploadingBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockFileUploadingBinding
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.decoration.DecoratableCardViewHolder
import com.anytypeio.anytype.core_ui.features.editor.decoration.EditorDecorationContainer
import com.anytypeio.anytype.core_ui.features.editor.decoration.applySelectorOffset
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.indentize
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class BookmarkUpload(
    private val binding: ItemBlockBookmarkUploadingBinding
) : MediaUpload(binding.root), DecoratableCardViewHolder {

    override val root: View = itemView

    override val decoratableContainer: EditorDecorationContainer
        get() = binding.decorationContainer

    override val decoratableCard: View = binding.card

    override fun uploadClick(target: String, clicked: (ListenerType) -> Unit) {
        clicked(ListenerType.File.Upload(target))
    }

    fun setUrl(url: Url?) {
        binding.editUrl.text = url
    }

    override fun indentize(item: BlockView.Indentable) {
        if (!BuildConfig.NESTED_DECORATION_ENABLED) {
            root.indentize(
                indent = item.indent,
                defIndent = dimen(R.dimen.indent),
                margin = dimen(R.dimen.bookmark_default_margin_start)
            )
        }
    }

    override fun select(isSelected: Boolean) {
        if (!BuildConfig.NESTED_DECORATION_ENABLED) {
            root.isSelected = isSelected
        } else {
            binding.selected.isSelected = isSelected
        }
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