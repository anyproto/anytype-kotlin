package com.anytypeio.anytype.core_ui.features.editor.holders.text

import android.text.Editable
import android.view.View
import androidx.core.view.isVisible
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.SupportNesting
import com.anytypeio.anytype.core_ui.features.editor.marks
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.mention.MentionEvent
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashEvent
import kotlinx.android.synthetic.main.item_block_toggle.view.*

class Toggle(
    view: View,
    onContextMenuStyleClick: (IntRange) -> Unit
) : Text(view), SupportNesting {

    private var mode = BlockView.Mode.EDIT

    val toggle = itemView.toggle
    private val line = itemView.guideline
    private val placeholder = itemView.togglePlaceholder
    private val container = itemView.toolbarBlockContentContainer
    override val content: TextInputWidget = itemView.toggleContent
    override val root: View = itemView

    init {
        setup(onContextMenuStyleClick)
    }

    fun bind(
        item: BlockView.Text.Toggle,
        onTextBlockTextChanged: (BlockView.Text) -> Unit,
        onToggleClicked: (String) -> Unit,
        onTogglePlaceholderClicked: (String) -> Unit,
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
        clicked = clicked,
        onEmptyBlockBackspaceClicked = onEmptyBlockBackspaceClicked,
        onSplitLineEnterClicked = onSplitLineEnterClicked,
        onNonEmptyBlockBackspaceClicked = onNonEmptyBlockBackspaceClicked,
        onBackPressedCallback = onBackPressedCallback
    ).also {
        toggle.rotation = if (item.toggled) EXPANDED_ROTATION else COLLAPSED_ROTATION
        if (item.mode == BlockView.Mode.READ) {
            placeholder.isVisible = false
        } else {
            placeholder.apply {
                isVisible = item.isEmpty && item.toggled
                setOnClickListener { onTogglePlaceholderClicked(item.id) }
            }
        }
        toggle.setOnClickListener {
            if (mode == BlockView.Mode.EDIT) onToggleClicked(item.id)
        }
        setupMentionWatcher(onMentionEvent)
        setupSlashWatcher(onSlashEvent, item.getViewType())
    }

    override fun getMentionImageSizeAndPadding(): Pair<Int, Int> = with(itemView) {
        Pair(
            first = resources.getDimensionPixelSize(R.dimen.mention_span_image_size_default),
            second = resources.getDimensionPixelSize(R.dimen.mention_span_image_padding_default)
        )
    }

    override fun indentize(item: BlockView.Indentable) {
        line.setGuidelineBegin(item.indent * dimen(R.dimen.indent))
    }

    override fun select(item: BlockView.Selectable) {
        container.isSelected = item.isSelected
    }

    override fun enableReadMode() {
        super.enableReadMode()
        mode = BlockView.Mode.READ
    }

    override fun enableEditMode() {
        super.enableEditMode()
        mode = BlockView.Mode.EDIT
    }

    override fun processChangePayload(
        payloads: List<BlockViewDiffUtil.Payload>,
        item: BlockView,
        onTextChanged: (BlockView.Text) -> Unit,
        onSelectionChanged: (String, IntRange) -> Unit,
        clicked: (ListenerType) -> Unit,
        onMentionEvent: (MentionEvent) -> Unit,
        onSlashEvent: (SlashEvent) -> Unit
    ) {
        check(item is BlockView.Text.Toggle) { "Expected a toggle block, but was: $item" }
        super.processChangePayload(payloads, item, onTextChanged, onSelectionChanged, clicked, onMentionEvent, onSlashEvent)
        payloads.forEach { payload ->
            if (payload.changes.contains(BlockViewDiffUtil.TOGGLE_EMPTY_STATE_CHANGED))
                placeholder.isVisible = item.isEmpty
        }
    }

    companion object {
        /**
         * Rotation value for a toggle icon for expanded state.
         */
        const val EXPANDED_ROTATION = 90f

        /**
         * Rotation value for a toggle icon for collapsed state.
         */
        const val COLLAPSED_ROTATION = 0f
    }
}