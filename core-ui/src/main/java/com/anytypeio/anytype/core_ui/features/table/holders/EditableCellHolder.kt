package com.anytypeio.anytype.core_ui.features.table.holders

import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockTableCellBinding
import com.anytypeio.anytype.core_ui.extensions.setBlockBackgroundColor
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.holders.text.Text
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class EditableCellHolder(
    private val binding: ItemBlockTableCellBinding,
    clicked: (ListenerType) -> Unit,
) : Text<BlockView.Text.Paragraph>(binding.root, clicked) {

    override val root: View = binding.root
    override val content: TextInputWidget = binding.textContent

    private val mentionIconSize: Int
    private val mentionIconPadding: Int
    private val mentionCheckedIcon: Drawable?
    private val mentionUncheckedIcon: Drawable?
    private val mentionInitialsSize: Float

    init {
        setup()
        with(itemView.context) {
            mentionIconSize =
                resources.getDimensionPixelSize(R.dimen.mention_span_image_size_default)
            mentionIconPadding =
                resources.getDimensionPixelSize(R.dimen.mention_span_image_padding_default)
            mentionUncheckedIcon = ContextCompat.getDrawable(this, R.drawable.ic_task_0_text_16)
            mentionCheckedIcon = ContextCompat.getDrawable(this, R.drawable.ic_task_1_text_16)
            mentionInitialsSize = resources.getDimension(R.dimen.mention_span_initials_size_default)
        }
    }

    override fun bind(item: BlockView.Text.Paragraph) {
        super.bind(item)
        applyBackground(item)
    }

    fun bindEmptyCell() {
        if (root.background != null) root.background = null
        if (content.text != null) content.text = null
        if (content.isTextSelectable) content.enableReadMode()
    }

    fun cellSelection(isSelected: Boolean) {
        binding.selection.isSelected = isSelected
    }

    override fun processChangePayload(
        payloads: List<BlockViewDiffUtil.Payload>,
        item: BlockView,
        clicked: (ListenerType) -> Unit,
    ) {
        if (item is BlockView.Text.Paragraph) {
            if (item.mode == BlockView.Mode.EDIT) {
                content.enableEditMode()
            }
            if (payloads.any { p -> p.isBackgroundColorChanged }) {
                applyBackground(item)
            }
        }
        super.processChangePayload(payloads, item, clicked)
    }

    private fun applyBackground(item: BlockView.Text.Paragraph) {
        root.setBlockBackgroundColor(item.background)
    }

    override fun indentize(item: BlockView.Indentable) {}
    override fun getMentionIconSize(): Int = mentionIconSize
    override fun getMentionIconPadding(): Int = mentionIconPadding
    override fun getMentionCheckedIcon(): Drawable? = mentionCheckedIcon
    override fun getMentionUncheckedIcon(): Drawable? = mentionUncheckedIcon
    override fun getMentionInitialsSize(): Float = mentionInitialsSize
}