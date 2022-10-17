package com.anytypeio.anytype.core_ui.features.table.holders

import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.BuildConfig
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockTableRowItemEditableBinding
import com.anytypeio.anytype.core_ui.features.editor.holders.text.Text
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class EditableCellHolder(
    private val binding: ItemBlockTableRowItemEditableBinding,
    clicked: (ListenerType) -> Unit,
) : Text<BlockView.Text.Paragraph>(binding.root, clicked) {

    override val root: View = binding.root
    override val content: TextInputWidget = binding.textContent
    private val selectionView: View = binding.selection

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
        applyDefaultOffsets()
    }

    private fun applyDefaultOffsets() {
        if (!BuildConfig.NESTED_DECORATION_ENABLED) {
            binding.root.updatePadding(
                left = dimen(R.dimen.default_document_item_padding_start),
                right = dimen(R.dimen.default_document_item_padding_end)
            )
            binding.root.updateLayoutParams<RecyclerView.LayoutParams> {
                topMargin = dimen(R.dimen.default_document_item_margin_top)
                bottomMargin = dimen(R.dimen.default_document_item_margin_bottom)
            }
        }
    }

    fun cellSelection(isSelected: Boolean) {
        selectionView.isSelected = isSelected
    }

    override fun indentize(item: BlockView.Indentable) {}
    override fun getMentionIconSize(): Int = mentionIconSize
    override fun getMentionIconPadding(): Int = mentionIconPadding
    override fun getMentionCheckedIcon(): Drawable? = mentionCheckedIcon
    override fun getMentionUncheckedIcon(): Drawable? = mentionUncheckedIcon
    override fun getMentionInitialsSize(): Float = mentionInitialsSize
}