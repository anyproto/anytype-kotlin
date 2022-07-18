package com.anytypeio.anytype.core_ui.features.editor.holders.text

import android.graphics.drawable.Drawable
import android.text.Editable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.BuildConfig
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockHighlightBinding
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.SupportNesting
import com.anytypeio.anytype.core_ui.features.editor.decoration.DecoratableViewHolder
import com.anytypeio.anytype.core_ui.features.editor.decoration.EditorDecorationContainer
import com.anytypeio.anytype.core_ui.features.editor.marks
import com.anytypeio.anytype.core_ui.tools.DefaultSpannableFactory
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.mention.MentionEvent
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashEvent

class Highlight(
    val binding: ItemBlockHighlightBinding,
    clicked: (ListenerType) -> Unit,
) : Text(binding.root, clicked), BlockViewHolder.IndentableHolder, SupportNesting, DecoratableViewHolder {

    override val content: TextInputWidget = binding.highlightContent
    override val root: View = itemView
    private val indent = binding.highlightIndent
    private val container = binding.highlightBlockContentContainer

    override val decoratableContainer: EditorDecorationContainer = binding.decorationContainer

    private val mentionIconSize: Int
    private val mentionIconPadding: Int
    private val mentionCheckedIcon: Drawable?
    private val mentionUncheckedIcon: Drawable?
    private val mentionInitialsSize: Float

    init {
        content.setSpannableFactory(DefaultSpannableFactory())
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
                topMargin = dimen(R.dimen.default_highlight_content_margin_top)
                bottomMargin = dimen(R.dimen.default_highlight_content_margin_bottom)
            }
            binding.highlightBlockContentContainer.updatePadding(
                left = dimen(R.dimen.default_document_content_padding_start),
                right = dimen(R.dimen.default_document_content_padding_end)
            )
        }
    }

    fun bind(
        item: BlockView.Text.Highlight,
        onTextBlockTextChanged: (BlockView.Text) -> Unit,
        clicked: (ListenerType) -> Unit,
        onMentionEvent: (MentionEvent) -> Unit,
        onSlashEvent: (SlashEvent) -> Unit,
        onSplitLineEnterClicked: (String, Editable, IntRange) -> Unit,
        onEmptyBlockBackspaceClicked: (String) -> Unit,
        onNonEmptyBlockBackspaceClicked: (String, Editable) -> Unit,
        onBackPressedCallback: () -> Boolean
    ) = super.bind(
        item = item,
        onTextChanged = { _, editable ->
            item.apply {
                text = editable.toString()
                marks = editable.marks()
            }
            onTextBlockTextChanged(item)
        },
        onEmptyBlockBackspaceClicked = onEmptyBlockBackspaceClicked,
        onSplitLineEnterClicked = onSplitLineEnterClicked,
        onNonEmptyBlockBackspaceClicked = onNonEmptyBlockBackspaceClicked,
        onBackPressedCallback = onBackPressedCallback
    ).also {
        setupMentionWatcher(onMentionEvent)
        setupSlashWatcher(onSlashEvent, item.getViewType())
    }

    override fun select(item: BlockView.Selectable) {
        container.isSelected = item.isSelected
    }

    override fun indentize(item: BlockView.Indentable) {
        if (!BuildConfig.NESTED_DECORATION_ENABLED) {
            indent.updateLayoutParams { width = item.indent * dimen(R.dimen.indent) }
        }
    }

    override fun getMentionIconSize(): Int = mentionIconSize
    override fun getMentionIconPadding(): Int = mentionIconPadding
    override fun getMentionCheckedIcon(): Drawable? = mentionCheckedIcon
    override fun getMentionUncheckedIcon(): Drawable? = mentionUncheckedIcon
    override fun getMentionInitialsSize(): Float = mentionInitialsSize

    override fun applyDecorations(decorations: List<BlockView.Decoration>) {
        if (BuildConfig.NESTED_DECORATION_ENABLED) {
            decoratableContainer.decorate(decorations) { rect ->
                indent.updateLayoutParams {
                    width = dimen(R.dimen.default_indent) + rect.left
                }
            }
        }
    }
}