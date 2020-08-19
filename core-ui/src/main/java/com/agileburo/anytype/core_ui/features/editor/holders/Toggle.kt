package com.agileburo.anytype.core_ui.features.editor.holders

import android.text.Editable
import android.view.View
import androidx.core.view.isVisible
import com.agileburo.anytype.core_ui.R
import com.agileburo.anytype.core_ui.common.Markup
import com.agileburo.anytype.core_ui.features.page.BlockView
import com.agileburo.anytype.core_ui.features.page.BlockViewDiffUtil
import com.agileburo.anytype.core_ui.features.page.ListenerType
import com.agileburo.anytype.core_ui.features.page.SupportNesting
import com.agileburo.anytype.core_ui.menu.ContextMenuType
import com.agileburo.anytype.core_ui.widgets.text.TextInputWidget
import com.agileburo.anytype.core_utils.ext.dimen
import kotlinx.android.synthetic.main.item_block_toggle.view.*

class Toggle(
    view: View,
    onMarkupActionClicked: (Markup.Type, IntRange) -> Unit
) : Text(view), SupportNesting {

    private var mode = BlockView.Mode.EDIT

    val toggle = itemView.toggle
    private val line = itemView.guideline
    private val placeholder = itemView.togglePlaceholder
    private val container = itemView.toolbarBlockContentContainer
    override val content: TextInputWidget = itemView.toggleContent
    override val root: View = itemView

    init {
        setup(onMarkupActionClicked, ContextMenuType.TEXT)
    }

    fun bind(
        item: BlockView.Toggle,
        onTextChanged: (String, Editable) -> Unit,
        onSelectionChanged: (String, IntRange) -> Unit,
        onFocusChanged: (String, Boolean) -> Unit,
        onToggleClicked: (String) -> Unit,
        onTogglePlaceholderClicked: (String) -> Unit,
        clicked: (ListenerType) -> Unit
    ) {

        super.bind(
            item = item,
            onTextChanged = onTextChanged,
            onSelectionChanged = onSelectionChanged,
            onFocusChanged = onFocusChanged,
            clicked = clicked
        )

        indentize(item)

        if (item.mode == BlockView.Mode.READ) {
            placeholder.isVisible = false
            toggle.apply {
                rotation = if (item.toggled) EXPANDED_ROTATION else COLLAPSED_ROTATION
            }
        } else {
            placeholder.apply {
                isVisible = item.isEmpty && item.toggled
                setOnClickListener { onTogglePlaceholderClicked(item.id) }
            }
        }

        toggle.apply {
            rotation = if (item.toggled) EXPANDED_ROTATION else COLLAPSED_ROTATION
            setOnClickListener {
                if (mode == BlockView.Mode.EDIT) onToggleClicked(item.id)
            }
        }
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

    override fun enableReadOnlyMode() {
        super.enableReadOnlyMode()
        mode = BlockView.Mode.READ
    }

    override fun enableEditMode() {
        super.enableEditMode()
        mode = BlockView.Mode.EDIT
    }

    override fun processChangePayload(
        payloads: List<BlockViewDiffUtil.Payload>,
        item: BlockView,
        onTextChanged: (String, Editable) -> Unit,
        onSelectionChanged: (String, IntRange) -> Unit,
        clicked: (ListenerType) -> Unit
    ) {
        check(item is BlockView.Toggle) { "Expected a toggle block, but was: $item" }
        super.processChangePayload(payloads, item, onTextChanged, onSelectionChanged, clicked)
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