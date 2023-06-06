package com.anytypeio.anytype.core_ui.features.editor.holders.text

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.BuildConfig
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockCalloutBinding
import com.anytypeio.anytype.core_ui.extensions.setBlockBackgroundTintColor
import com.anytypeio.anytype.core_ui.extensions.veryLight
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.SupportNesting
import com.anytypeio.anytype.core_ui.features.editor.decoration.DecoratableViewHolder
import com.anytypeio.anytype.core_ui.features.editor.decoration.EditorDecorationContainer
import com.anytypeio.anytype.core_ui.features.editor.decoration.applySelectorOffset
import com.anytypeio.anytype.core_ui.tools.DefaultSpannableFactory
import com.anytypeio.anytype.core_ui.widgets.ObjectIconWidget
import com.anytypeio.anytype.core_ui.widgets.text.TextInputWidget
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView

class Callout(
    val binding: ItemBlockCalloutBinding,
    clicked: (ListenerType) -> Unit,
) : Text <BlockView.Text.Callout>(
    view = binding.root,
    clicked = clicked
), BlockViewHolder.IndentableHolder, SupportNesting, DecoratableViewHolder {

    override val root: View = itemView
    override val content: TextInputWidget = binding.calloutText
    private val icon: ObjectIconWidget = binding.calloutIcon

    private val mentionIconSize: Int
    private val mentionIconPadding: Int
    private val mentionCheckedIcon: Drawable?
    private val mentionUncheckedIcon: Drawable?
    private val mentionInitialsSize: Float

    private val indentOffset = dimen(R.dimen.default_indent)

    override val decoratableContainer: EditorDecorationContainer = binding.decorationContainer

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
    }

    override fun bind(
        item: BlockView.Text.Callout
    ) = super.bind(
        item = item
    ).also {
        icon.setIcon(item.icon)
        icon.setOnClickListener {
            clicked(ListenerType.Callout.Icon(item.id))
        }
    }

    override fun processChangePayload(
        payloads: List<BlockViewDiffUtil.Payload>,
        item: BlockView,
        clicked: (ListenerType) -> Unit,
    ) {
        val callout = requireNotNull(item as? BlockView.Text.Callout) {
            "Failed to processChangePayload. $item must be Callout"
        }
        payloads.forEach { payload ->
            if (payload.isCalloutIconChanged) {
                icon.setIcon(callout.icon)
            }
        }
        super.processChangePayload(
            payloads,
            item,
            clicked,
        )
    }

    override fun select(item: BlockView.Selectable) {
        binding.selectionView.isSelected = item.isSelected
    }

    @Deprecated("Pre-nested-styling legacy.")
    override fun indentize(item: BlockView.Indentable) {
        // Do nothing.
    }

    @Deprecated("Pre-nested-styling legacy.")
    override fun setBackgroundColor(background: ThemeColor) {
        // Do nothing.
    }

    override fun getMentionIconSize(): Int = mentionIconSize
    override fun getMentionIconPadding(): Int = mentionIconPadding
    override fun getMentionCheckedIcon(): Drawable? = mentionCheckedIcon
    override fun getMentionUncheckedIcon(): Drawable? = mentionUncheckedIcon
    override fun getMentionInitialsSize(): Float = mentionInitialsSize

    override fun applyDecorations(decorations: List<BlockView.Decoration>) {
        val indent = decorations.lastIndex
        val last = decorations.last()
        decoratableContainer.decorate(decorations) { rect ->
            binding.calloutCardContainer.updateLayoutParams<FrameLayout.LayoutParams> {
                marginStart = if (indent == 0) rect.left + indentOffset else rect.left
                marginEnd = rect.right
                bottomMargin = rect.bottom
            }
        }
        when (last.style) {
            is BlockView.Decoration.Style.Callout.Start -> {
                binding.calloutCardContainer.setBackgroundResource(R.drawable.rect_callout_start)
            }
            is BlockView.Decoration.Style.Callout.Full -> {
                binding.calloutCardContainer.setBackgroundResource(R.drawable.rect_callout_full)
            }
            else -> {}
        }
        binding.calloutCardContainer.setBlockBackgroundTintColor(
            color = last.background,
            default = itemView.resources.getColor(R.color.palette_very_light_grey, null)
        )
        binding.selectionView.applySelectorOffset<FrameLayout.LayoutParams>(
            content = binding.calloutCardContainer,
            res = itemView.resources
        )
    }
}