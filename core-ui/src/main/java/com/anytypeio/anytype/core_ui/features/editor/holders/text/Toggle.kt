package com.anytypeio.anytype.core_ui.features.editor.holders.text

import android.graphics.drawable.Drawable
import android.text.Editable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockToggleBinding
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.SupportNesting
import com.anytypeio.anytype.core_ui.features.editor.marks
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.mention.MentionEvent
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashEvent

class Toggle(
    val binding: ItemBlockToggleBinding,
    clicked: (ListenerType) -> Unit,
) : Text(binding.root, clicked), SupportNesting {

    private var mode = BlockView.Mode.EDIT

    val toggle = binding.toggle
    private val line = binding.guideline
    private val placeholder = binding.togglePlaceholder
    private val container = binding.toolbarBlockContentContainer
    override val content: TextInputWidget = binding.toggleContent
    override val root: View = itemView

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

    fun bind(
        item: BlockView.Text.Toggle,
        onTextBlockTextChanged: (BlockView.Text) -> Unit,
        onToggleClicked: (String) -> Unit,
        onTogglePlaceholderClicked: (String) -> Unit,
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
        toggle.rotation = if (item.toggled) EXPANDED_ROTATION else COLLAPSED_ROTATION
        if (item.mode == BlockView.Mode.READ) {
            placeholder.isVisible = false
        } else {
            placeholder.isVisible = item.isEmpty && item.toggled
        }
        placeholder.setOnClickListener { onTogglePlaceholderClicked(item.id) }
        toggle.setOnClickListener { onToggleClicked(item.id) }
        setupMentionWatcher(onMentionEvent)
        setupSlashWatcher(onSlashEvent, item.getViewType())
    }

    override fun getMentionIconSize(): Int = mentionIconSize
    override fun getMentionIconPadding(): Int = mentionIconPadding
    override fun getMentionCheckedIcon(): Drawable? = mentionCheckedIcon
    override fun getMentionUncheckedIcon(): Drawable? = mentionUncheckedIcon
    override fun getMentionInitialsSize(): Float = mentionInitialsSize

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
        super.processChangePayload(
            payloads,
            item,
            onTextChanged,
            onSelectionChanged,
            clicked,
            onMentionEvent,
            onSlashEvent
        )
        payloads.forEach { payload ->
            if (payload.isToggleStateChanged) {
                if (item.toggled) {
                    toggle.rotation = EXPANDED_ROTATION
                } else {
                    toggle.rotation = COLLAPSED_ROTATION
                }
                placeholder.isVisible = item.isCreateBlockButtonVisible
            }
            if (payload.isToggleEmptyStateChanged) {
                placeholder.isVisible = item.isCreateBlockButtonVisible
            }
            if (payload.readWriteModeChanged()) {
                placeholder.isVisible = item.isCreateBlockButtonVisible
            }
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