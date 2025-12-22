package com.anytypeio.anytype.core_ui.features.editor.holders.placeholders

import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockMediaPlaceholderBinding
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class EmbedPlaceholder(
    binding: ItemBlockMediaPlaceholderBinding
) : MediaPlaceholder(binding) {

    fun bind(item: BlockView.Embed, clicked: (ListenerType) -> Unit) {
        super.bind(item, clicked)
        setup()
        binding.fileName.text = binding.root.context.getString(
            R.string.embed_content_not_available,
            item.processor
        )
        // Override click to pass the full item
        binding.root.setOnClickListener {
            clicked(ListenerType.Embed.Click(item))
        }
    }

    override fun setup() {
        binding.fileIcon.setImageResource(R.drawable.ic_bookmark_placeholder)
    }

    override fun placeholderClick(target: String, clicked: (ListenerType) -> Unit) {
        // Click will be handled by the holder, passing the full item
    }

    override fun processChangePayload(payloads: List<BlockViewDiffUtil.Payload>, item: BlockView, clicked: (ListenerType) -> Unit) {
        check(item is BlockView.Embed) { "Expected an embed block, but was: $item" }
        super.processChangePayload(payloads, item)
        payloads.forEach { payload ->
            if (payload.isSelectionChanged) {
                select(item.isSelected)
            }
        }
    }

    private fun select(isSelected: Boolean) {
        binding.selected.isSelected = isSelected
    }
}
