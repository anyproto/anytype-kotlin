package com.anytypeio.anytype.core_ui.features.editor.holders.other

import android.widget.LinearLayout
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockUnsupportedBinding
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class Unsupported(
    val binding: ItemBlockUnsupportedBinding
) : BlockViewHolder(binding.root), BlockViewHolder.IndentableHolder {
    fun bind(item: BlockView.Unsupported) {
        indentize(item)
        itemView.isSelected = item.isSelected
    }

    override fun indentize(item: BlockView.Indentable) {
        binding.indentator.updateLayoutParams<LinearLayout.LayoutParams> {
            width = item.indent * dimen(R.dimen.indent)
        }
    }
}