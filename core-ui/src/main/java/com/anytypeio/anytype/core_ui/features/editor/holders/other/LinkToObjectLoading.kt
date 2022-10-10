package com.anytypeio.anytype.core_ui.features.editor.holders.other

import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.BuildConfig
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockObjectLinkLoadingBinding
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class LinkToObjectLoading(binding: ItemBlockObjectLinkLoadingBinding) :
    BlockViewHolder(binding.root),
    BlockViewHolder.IndentableHolder {

    private val root = binding.root
    private val container = binding.container

    fun bind(
        item: BlockView.LinkToObject.Loading,
        clicked: (ListenerType) -> Unit
    ) {

        applySelectedState(item)

        itemView.setOnClickListener { clicked(ListenerType.LinkToObjectLoading(item.id)) }
    }

    private fun applySelectedState(item: BlockView.LinkToObject.Loading) {
        container.isSelected = item.isSelected
    }

    fun processChangePayload(payloads: List<BlockViewDiffUtil.Payload>, item: BlockView) {
        check(item is BlockView.LinkToObject.Loading) { "Expected a link to object block, but was: $item" }
        payloads.forEach { payload ->
            if (payload.isSelectionChanged) {
                applySelectedState(item)
            }
        }
    }

    override fun indentize(item: BlockView.Indentable) {
        if (!BuildConfig.NESTED_DECORATION_ENABLED) {
            root.updateLayoutParams<RecyclerView.LayoutParams> {
                marginStart = item.indent * dimen(R.dimen.indent)
            }
        }
    }
}