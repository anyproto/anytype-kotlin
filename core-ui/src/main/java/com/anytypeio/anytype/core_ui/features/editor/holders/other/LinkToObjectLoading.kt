package com.anytypeio.anytype.core_ui.features.editor.holders.other

import androidx.core.view.updatePadding
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockObjectLinkLoadingBinding
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class LinkToObjectLoading(binding: ItemBlockObjectLinkLoadingBinding) :
    BlockViewHolder(binding.root),
    BlockViewHolder.IndentableHolder {

    override fun indentize(item: BlockView.Indentable) {
        itemView.updatePadding(
            left = dimen(R.dimen.default_document_content_padding_start) + item.indent * dimen(R.dimen.indent)
        )
    }
}