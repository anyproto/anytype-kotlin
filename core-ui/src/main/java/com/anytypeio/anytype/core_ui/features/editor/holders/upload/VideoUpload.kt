package com.anytypeio.anytype.core_ui.features.editor.holders.upload

import android.view.View
import com.anytypeio.anytype.core_ui.BuildConfig
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockVideoUploadingBinding
import com.anytypeio.anytype.core_ui.features.editor.decoration.DecoratableCardViewHolder
import com.anytypeio.anytype.core_ui.features.editor.decoration.EditorDecorationContainer
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.indentize
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class VideoUpload(
    val binding: ItemBlockVideoUploadingBinding
) : MediaUpload(binding.root), DecoratableCardViewHolder {

    override val root: View = itemView

    override val decoratableContainer: EditorDecorationContainer
        get() = binding.decorationContainer

    override val decoratableCard: View = binding.root

    override fun uploadClick(target: String, clicked: (ListenerType) -> Unit) {
        clicked(ListenerType.Video.Upload(target))
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
        root.isSelected = isSelected
    }
}