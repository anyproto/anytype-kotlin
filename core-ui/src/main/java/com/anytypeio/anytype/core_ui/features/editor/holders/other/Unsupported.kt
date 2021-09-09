package com.anytypeio.anytype.core_ui.features.editor.holders.other

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import kotlinx.android.synthetic.main.item_block_unsupported.view.*
import timber.log.Timber

class Unsupported(
    parent: ViewGroup
) : BlockViewHolder(
    view = LayoutInflater.from(parent.context).inflate(
        R.layout.item_block_unsupported,
        parent,
        false
    )
), BlockViewHolder.IndentableHolder {
    fun bind(item: BlockView.Unsupported) {
        Timber.d("Binding unsupported block")
        indentize(item)
        itemView.isSelected = item.isSelected
    }

    override fun indentize(item: BlockView.Indentable) {
        itemView.indentator.updateLayoutParams<LinearLayout.LayoutParams> {
            width = item.indent * dimen(R.dimen.indent)
        }
    }
}