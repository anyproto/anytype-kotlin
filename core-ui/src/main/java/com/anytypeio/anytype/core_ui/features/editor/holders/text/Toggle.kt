package com.anytypeio.anytype.core_ui.features.editor.holders.text

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.BuildConfig
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockToggleBinding
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.SupportNesting
import com.anytypeio.anytype.core_ui.features.editor.decoration.DecoratableViewHolder
import com.anytypeio.anytype.core_ui.features.editor.decoration.EditorDecorationContainer
import com.anytypeio.anytype.core_ui.features.editor.provide
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.mention.MentionEvent
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashEvent

class Toggle(
    val binding: ItemBlockToggleBinding,
    clicked: (ListenerType) -> Unit,
    headerLevel: Int? = null
) : Text<BlockView.Text.Toggle>(binding.root, clicked), SupportNesting, DecoratableViewHolder {

    private var mode = BlockView.Mode.EDIT

    val toggle = binding.toggle
    private val line = binding.guideline
    private val placeholder = binding.togglePlaceholder
    private val container = binding.graphicPlusTextContainer
    override val content: TextInputWidget = binding.toggleContent
    override val root: View = itemView
    override val selectionView: View = binding.selectionView

    private val mentionIconSize: Int
    private val mentionIconPadding: Int
    private val mentionCheckedIcon: Drawable?
    private val mentionUncheckedIcon: Drawable?
    private val mentionInitialsSize: Float

    override val decoratableContainer: EditorDecorationContainer = binding.decorationContainer

    private val contentTopMargin: Int = when (headerLevel) {
        null -> 0
        1 -> dimen(R.dimen.dp_24)
        else -> dimen(R.dimen.dp_16)
    }

    init {
        setup()
        with(itemView.context) {
            when (headerLevel) {
                null -> {
                    mentionIconSize =
                        resources.getDimensionPixelSize(R.dimen.mention_span_image_size_default)
                    mentionIconPadding =
                        resources.getDimensionPixelSize(R.dimen.mention_span_image_padding_default)
                    mentionUncheckedIcon =
                        ContextCompat.getDrawable(this, R.drawable.ic_task_0_text_16)
                    mentionCheckedIcon =
                        ContextCompat.getDrawable(this, R.drawable.ic_task_1_text_16)
                    mentionInitialsSize =
                        resources.getDimension(R.dimen.mention_span_initials_size_default)
                }
                1 -> {
                    mentionIconSize =
                        resources.getDimensionPixelSize(R.dimen.mention_span_image_size_header_one)
                    mentionIconPadding =
                        resources.getDimensionPixelSize(R.dimen.mention_span_image_padding_header_one)
                    mentionUncheckedIcon = ContextCompat.getDrawable(this, R.drawable.ic_task_0_h1)
                    mentionCheckedIcon = ContextCompat.getDrawable(this, R.drawable.ic_task_1_h1)
                    mentionInitialsSize =
                        resources.getDimension(R.dimen.mention_span_initials_size_header_one)
                }
                2 -> {
                    mentionIconSize =
                        resources.getDimensionPixelSize(R.dimen.mention_span_image_size_header_two)
                    mentionIconPadding =
                        resources.getDimensionPixelSize(R.dimen.mention_span_image_padding_header_two)
                    mentionUncheckedIcon = ContextCompat.getDrawable(this, R.drawable.ic_task_0_h2)
                    mentionCheckedIcon = ContextCompat.getDrawable(this, R.drawable.ic_task_1_h2)
                    mentionInitialsSize =
                        resources.getDimension(R.dimen.mention_span_initials_size_header_two)
                }
                else -> {
                    mentionIconSize =
                        resources.getDimensionPixelSize(R.dimen.mention_span_image_size_header_three)
                    mentionIconPadding =
                        resources.getDimensionPixelSize(R.dimen.mention_span_image_padding_header_three)
                    mentionUncheckedIcon =
                        ContextCompat.getDrawable(this, R.drawable.ic_task_0_text_17)
                    mentionCheckedIcon =
                        ContextCompat.getDrawable(this, R.drawable.ic_task_1_text_17)
                    mentionInitialsSize =
                        resources.getDimension(R.dimen.mention_span_initials_size_header_three)
                }
            }
        }
    }

    override fun bind(
        item: BlockView.Text.Toggle
    ) = super.bind(item = item).also {
        toggle.rotation = if (item.toggled) EXPANDED_ROTATION else COLLAPSED_ROTATION
        if (item.mode == BlockView.Mode.READ) {
            placeholder.isVisible = false
        } else {
            placeholder.isVisible = item.isEmpty && item.toggled
        }
    }

    fun setupToggle(
        onToggleClicked: (String) -> Unit,
        onTogglePlaceholderClicked: (String) -> Unit
    ) {
        placeholder.setOnClickListener {
            val id = provide<BlockView.Text.Toggle>()?.id ?: return@setOnClickListener
            onTogglePlaceholderClicked(id)
        }
        toggle.setOnClickListener {
            val id = provide<BlockView.Text.Toggle>()?.id ?: return@setOnClickListener
            onToggleClicked(id)
        }
    }

    override fun getMentionIconSize(): Int = mentionIconSize
    override fun getMentionIconPadding(): Int = mentionIconPadding
    override fun getMentionCheckedIcon(): Drawable? = mentionCheckedIcon
    override fun getMentionUncheckedIcon(): Drawable? = mentionUncheckedIcon
    override fun getMentionInitialsSize(): Float = mentionInitialsSize

    @Deprecated("Pre-nested-styling legacy.")
    override fun indentize(item: BlockView.Indentable) {
        // Do nothing.
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
        clicked: (ListenerType) -> Unit
    ) {
        check(item is BlockView.Text.Toggle) { "Expected a toggle block, but was: $item" }
        super.processChangePayload(
            payloads,
            item,
            clicked,
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

    override fun applyDecorations(decorations: List<BlockView.Decoration>) {
        decoratableContainer.decorate(
            decorations = decorations
        ) { rect ->
            binding.graphicPlusTextContainer.updateLayoutParams<FrameLayout.LayoutParams> {
                marginStart = dimen(R.dimen.default_indent) + rect.left
                marginEnd = dimen(R.dimen.dp_8) + rect.right
                topMargin = contentTopMargin
                bottomMargin = rect.bottom + dimen(R.dimen.dp_2)
            }
            selectionView.updateLayoutParams<FrameLayout.LayoutParams> {
                marginStart = dimen(R.dimen.dp_8) + rect.left
                marginEnd = dimen(R.dimen.dp_8) + rect.right
                bottomMargin = rect.bottom + dimen(R.dimen.dp_2)
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