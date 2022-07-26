package com.anytypeio.anytype.core_ui.features.table.holders

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.toSpannable
import com.anytypeio.anytype.core_ui.databinding.ItemBlockTableRowItemBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockTableSpaceBinding
import com.anytypeio.anytype.core_ui.extensions.resolveThemedTextColor
import com.anytypeio.anytype.core_ui.extensions.veryLight
import com.anytypeio.anytype.core_ui.features.editor.EditorTouchProcessor
import com.anytypeio.anytype.core_ui.features.editor.SupportCustomTouchProcessor
import com.anytypeio.anytype.core_ui.features.table.TableCellsDiffUtil
import com.anytypeio.anytype.core_utils.ext.invisible
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.editor.Markup
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.editor.editor.model.Alignment
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView


sealed class TableCellHolder(view: View) : RecyclerView.ViewHolder(view) {

    class TableTextCellHolder(context: Context, binding: ItemBlockTableRowItemBinding) :
        TableCellHolder(binding.root), SupportCustomTouchProcessor {

        val root: View = binding.root
        val textContent: AppCompatTextView = binding.textContent
        val selection: View = binding.selection

        private val defTextColor: Int = itemView.resources.getColor(R.color.text_primary, null)
        private val mentionIconSize =
            itemView.resources.getDimensionPixelSize(R.dimen.mention_span_image_size_default)
        private val mentionIconPadding =
            itemView.resources.getDimensionPixelSize(R.dimen.mention_span_image_padding_default)
        private val mentionCheckedIcon =
            ContextCompat.getDrawable(context, R.drawable.ic_task_0_text_16)
        private val mentionUncheckedIcon =
            ContextCompat.getDrawable(context, R.drawable.ic_task_0_text_16)
        private val mentionInitialsSize =
            itemView.resources.getDimension(R.dimen.mention_span_initials_size_default)

        override val editorTouchProcessor = EditorTouchProcessor(
            fallback = { e -> itemView.onTouchEvent(e) })

        init {
            textContent.setOnTouchListener { v, e -> editorTouchProcessor.process(v, e) }
        }

        fun bind(
            cell: BlockView.Table.Cell
        ) {
            when (cell) {
                is BlockView.Table.Cell.Empty -> {
                    setBorders(cell.settings)
                    textContent.text = null
                    setBackground(null, cell.settings)
                }
                is BlockView.Table.Cell.Text -> {
                    setBorders(cell.settings)
                    setBlockText(
                        text = cell.block.text,
                        markup = cell.block,
                        color = cell.block.color
                    )
                    setTextColor(cell.block.color)
                    setBackground(cell.block.backgroundColor, cell.settings)
                    setAlignment(cell.block.alignment)
                }
            }
        }

        fun processChangePayload(
            payloads: TableCellsDiffUtil.Payload,
            cell: BlockView.Table.Cell
        ) {
            if (payloads.isBordersChanged) {
                when (cell) {
                    is BlockView.Table.Cell.Empty -> setBorders(cell.settings)
                    is BlockView.Table.Cell.Text -> setBorders(cell.settings)
                    BlockView.Table.Cell.Space -> {}
                }
            }
            if (cell is BlockView.Table.Cell.Text) {
                processChangePayload(payloads, cell.block, cell.settings)
            }
        }

        fun processChangePayload(
            payloads: TableCellsDiffUtil.Payload,
            block: BlockView.Text.Paragraph,
            settings: BlockView.Table.CellSettings
        ) {
            if (payloads.isTextChanged) {
                setBlockText(
                    text = block.text,
                    markup = block,
                    color = block.color
                )
            }
            if (payloads.isTextColorChanged) {
                setTextColor(block.color)
            }
            if (payloads.isBackgroundChanged) {
                setBackground(block.backgroundColor, settings)
            }
            if (payloads.isMarkupChanged) {
                setBlockSpannableText(block, resolveTextBlockThemedColor(block.color))
            }
            if (payloads.isAlignChanged) {
                setAlignment(block.alignment)
            }
        }

        private fun setBlockText(
            text: String,
            markup: Markup,
            color: String?
        ) {
            when (markup.marks.isEmpty()) {
                true -> textContent.text = text
                false -> setBlockSpannableText(markup, resolveTextBlockThemedColor(color))
            }
        }

        private fun setBlockSpannableText(
            markup: Markup,
            color: Int
        ) {
            when (markup.marks.any { it is Markup.Mark.Mention || it is Markup.Mark.Object }) {
                true -> setSpannableWithMention(markup, color)
                false -> setSpannable(markup, color)
            }
        }

        private fun setSpannable(markup: Markup, textColor: Int) {
            textContent.setText(
                markup.toSpannable(
                    textColor = textColor,
                    context = itemView.context
                ),
                TextView.BufferType.SPANNABLE
            )
        }

        private fun setSpannableWithMention(
            markup: Markup,
            textColor: Int
        ) {
            textContent.setText(
                markup.toSpannable(
                    textColor = textColor,
                    context = itemView.context,
                    mentionImageSize = mentionIconSize,
                    mentionImagePadding = mentionIconPadding,
                    mentionCheckedIcon = mentionCheckedIcon,
                    mentionUncheckedIcon = mentionUncheckedIcon,
                    mentionInitialsSize = mentionInitialsSize
                ),
                TextView.BufferType.SPANNABLE
            )
        }

        private fun setAlignment(alignment: Alignment?) {
            if (alignment != null) {
                textContent.gravity = when (alignment) {
                    Alignment.START -> Gravity.START
                    Alignment.CENTER -> Gravity.CENTER
                    Alignment.END -> Gravity.END
                }
            } else {
                textContent.gravity = Gravity.START
            }
        }

        private fun setTextColor(color: String?) {
            textContent.setTextColor(resolveTextBlockThemedColor(color))
        }


        private fun setBackground(background: String?, settings: BlockView.Table.CellSettings) {
            setTableCellBackgroundColor(background, root, settings.isHeader)
        }

        /**
         * @param [color] color code, @see [ThemeColor]
         */
        private fun setTableCellBackgroundColor(color: String?, view: View, isHeader: Boolean) {
            if (!color.isNullOrEmpty()) {
                val value = ThemeColor.values().find { value -> value.code == color }
                if (value != null && value != ThemeColor.DEFAULT) {
                    view.setBackgroundColor(view.resources.veryLight(value, 0))
                } else {
                    setTableCellHeaderOrEmptyBackground(view, isHeader)
                }
            } else {
                setTableCellHeaderOrEmptyBackground(view, isHeader)
            }
        }

        private fun setTableCellHeaderOrEmptyBackground(view: View, isHeader: Boolean) {
            if (isHeader) {
                root.setBackgroundColor(
                    itemView.resources.getColor(
                        R.color.table_row_header_background,
                        null
                    )
                )
            } else {
                view.background = null
            }
        }

        private fun resolveTextBlockThemedColor(color: String?): Int {
            return itemView.context.resolveThemedTextColor(color, defTextColor)
        }

        private fun setBorders(settings: BlockView.Table.CellSettings) {
            if (settings.isAllBordersApply()) {
                selection.visible()
            } else {
                selection.invisible()
            }
        }
    }

    class TableSpaceHolder(binding: ItemBlockTableSpaceBinding) : TableCellHolder(binding.root)
}