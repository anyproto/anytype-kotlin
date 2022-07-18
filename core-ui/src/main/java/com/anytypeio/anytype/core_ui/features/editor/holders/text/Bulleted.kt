package com.anytypeio.anytype.core_ui.features.editor.holders.text

import android.graphics.drawable.Drawable
import android.text.Editable
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.BuildConfig
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockBulletedBinding
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.features.editor.SupportNesting
import com.anytypeio.anytype.core_ui.features.editor.decoration.DecoratableViewHolder
import com.anytypeio.anytype.core_ui.features.editor.decoration.EditorDecorationContainer
import com.anytypeio.anytype.core_ui.features.editor.marks
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.mention.MentionEvent
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.editor.slash.SlashEvent

class Bulleted(
    val binding: ItemBlockBulletedBinding,
    clicked: (ListenerType) -> Unit,
) : Text(binding.root, clicked), SupportNesting, DecoratableViewHolder {

    val indent: View = binding.bulletIndent
    val bullet = binding.bullet
    private val container = binding.graphicPlusTextContainer
    override val content: TextInputWidget = binding.bulletedListContent
    override val root: View = itemView

    private val mentionIconSize: Int
    private val mentionIconPadding: Int
    private val mentionCheckedIcon: Drawable?
    private val mentionUncheckedIcon: Drawable?
    private val mentionInitialsSize: Float

    override val decoratableContainer: EditorDecorationContainer = binding.decorationContainer

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
            binding.graphicPlusTextContainer.updatePadding(
                left = dimen(R.dimen.default_document_content_padding_start),
                right = dimen(R.dimen.default_document_content_padding_end),
            )
        }
    }

    fun bind(
        item: BlockView.Text.Bulleted,
        onTextBlockTextChanged: (BlockView.Text) -> Unit,
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

    override fun getMentionIconSize(): Int = mentionIconSize
    override fun getMentionIconPadding(): Int = mentionIconPadding
    override fun getMentionCheckedIcon(): Drawable? = mentionCheckedIcon
    override fun getMentionUncheckedIcon(): Drawable? = mentionUncheckedIcon
    override fun getMentionInitialsSize(): Float = mentionInitialsSize

    override fun setTextColor(color: String) {
        super.setTextColor(color)
        val value = ThemeColor.values().find { value -> value.code == color }
        if (value != null && value != ThemeColor.DEFAULT) {
            DrawableCompat.setTint(
                bullet.drawable,
                content.context.resources.dark(value, getDefaultTextColor())
            )
        } else {
            DrawableCompat.setTint(
                bullet.drawable,
                getDefaultTextColor()
            )
        }
    }

    override fun setTextColor(color: Int) {
        super.setTextColor(color)
        DrawableCompat.setTint(bullet.drawable, color)
    }

    override fun indentize(item: BlockView.Indentable) {
        if (!BuildConfig.NESTED_DECORATION_ENABLED) {
            indent.updateLayoutParams { width = item.indent * dimen(R.dimen.indent) }
        }
    }

    override fun select(item: BlockView.Selectable) {
        container.isSelected = item.isSelected
    }

    override fun applyDecorations(decorations: List<BlockView.Decoration>) {
        if (BuildConfig.NESTED_DECORATION_ENABLED) {
            decoratableContainer.decorate(decorations = decorations) { rect ->
                binding.graphicPlusTextContainer.updateLayoutParams<FrameLayout.LayoutParams> {
                    marginStart = dimen(R.dimen.default_indent) + rect.left
                    marginEnd = dimen(R.dimen.dp_8)
                    bottomMargin = rect.bottom
                    // TODO handle top and bottom offsets
                }
            }
        }
    }
}