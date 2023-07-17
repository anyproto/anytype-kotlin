package com.anytypeio.anytype.core_ui.features.editor.holders.other

import android.text.Spannable
import android.text.SpannableString
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.BuildConfig
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.SearchHighlightSpan
import com.anytypeio.anytype.core_ui.common.SearchTargetHighlightSpan
import com.anytypeio.anytype.core_ui.extensions.setBlockBackgroundColor
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.EditorTouchProcessor
import com.anytypeio.anytype.core_ui.features.editor.SupportCustomTouchProcessor
import com.anytypeio.anytype.core_ui.features.editor.SupportNesting
import com.anytypeio.anytype.core_ui.features.editor.decoration.DecoratableCardViewHolder
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.removeSpans
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.cover.CoverGradient
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.databinding.ItemBlockObjectLinkCardMediumIconBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockObjectLinkCardMediumIconCoverBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockObjectLinkCardSmallIconBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockObjectLinkCardSmallIconCoverBinding
import com.anytypeio.anytype.core_ui.features.editor.decoration.EditorDecorationContainer
import com.anytypeio.anytype.core_ui.features.editor.decoration.applySelectorOffset
import com.anytypeio.anytype.core_ui.widgets.ObjectIconWidget
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.bumptech.glide.Glide

abstract class LinkToObjectCard(
    view: View
) : BlockViewHolder(view),
    BlockViewHolder.IndentableHolder,
    BlockViewHolder.DragAndDropHolder,
    DecoratableCardViewHolder,
    SupportCustomTouchProcessor,
    SupportNesting {

    protected abstract val rootView: View
    abstract val containerView: ConstraintLayout

    private val untitled = itemView.resources.getString(R.string.untitled)
    abstract val objectIconView: ObjectIconWidget
    abstract val titleView: TextView
    abstract val descriptionView: TextView
    abstract val selectedView: View
    abstract val objectTypeView: TextView
    abstract override val decoratableContainer: EditorDecorationContainer
    abstract override val decoratableCard: CardView

    override val editorTouchProcessor = EditorTouchProcessor(
        fallback = { e -> itemView.onTouchEvent(e) }
    )

    protected fun bind(
        item: BlockView.LinkToObject.Default.Card,
        clicked: (ListenerType) -> Unit
    ) {
        selected(item)

        applyName(item)

        applyDescription(item)

        applyBackground(item.background)

        applyImageOrEmoji(item)

        applyObjectType(item)

        applySearchHighlight(item)

        itemView.setOnClickListener { clicked(ListenerType.LinkToObject(item.id)) }
    }

    private fun selected(item: BlockView.LinkToObject.Default.Card) {
        selectedView.isSelected = item.isSelected
    }

    private fun applyName(item: BlockView.LinkToObject.Default.Card) {
        val name = item.text
        when {
            name == null -> titleView.gone()
            name.isBlank() -> {
                titleView.visible()
                val sb = SpannableString(untitled)
                titleView.setText(sb, TextView.BufferType.EDITABLE)
            }
            else -> {
                titleView.visible()
                val sb = SpannableString(name)
                titleView.setText(sb, TextView.BufferType.EDITABLE)
            }
        }
    }

    private fun applyDescription(item: BlockView.LinkToObject.Default.Card) {
        if (item.description.isNullOrBlank()) {
            descriptionView.gone()
        } else {
            descriptionView.visible()
            descriptionView.text = item.description
        }
    }

    private fun applyImageOrEmoji(item: BlockView.LinkToObject.Default.Card) {
        when (item.icon) {
            ObjectIcon.None -> {
                objectIconView.gone()
            }
            else -> {
                objectIconView.visible()
                objectIconView.setIcon(item.icon)
            }
        }
    }

    private fun applySearchHighlight(item: BlockView.Searchable) {
        item.searchFields.find { it.key == BlockView.Searchable.Field.DEFAULT_SEARCH_FIELD_KEY }
            ?.let { field ->
                applySearchHighlight(field, titleView)
            } ?: clearSearchHighlights()
    }

    private fun applySearchHighlight(field: BlockView.Searchable.Field, input: TextView) {
        val content = input.text as Spannable
        content.removeSpans<SearchHighlightSpan>()
        content.removeSpans<SearchTargetHighlightSpan>()
        field.highlights.forEach { highlight ->
            content.setSpan(
                SearchHighlightSpan(),
                highlight.first,
                highlight.last,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        if (field.isTargeted) {
            content.setSpan(
                SearchTargetHighlightSpan(),
                field.target.first,
                field.target.last,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }

    private fun clearSearchHighlights() {
        titleView.editableText?.removeSpans<SearchHighlightSpan>()
        titleView.editableText?.removeSpans<SearchTargetHighlightSpan>()
    }

    @Deprecated("Pre-nested-styling legacy.")
    override fun indentize(item: BlockView.Indentable) {
        // Do nothing.
    }

    protected fun processChangeBasePayloads(
        payload: BlockViewDiffUtil.Payload,
        item: BlockView.LinkToObject.Default.Card
    ) {
        if (payload.isSelectionChanged) {
            selected(item)
        }
        if (payload.isSearchHighlightChanged) {
            applySearchHighlight(item)
        }
        if (payload.isObjectTitleChanged) {
            applyName(item)
        }
        if (payload.isObjectIconChanged) {
            applyImageOrEmoji(item)
        }
        if (payload.isObjectDescriptionChanged) {
            applyDescription(item)
        }
        if (payload.isBackgroundColorChanged) {
            applyBackground(item.background)
        }
        if (payload.isObjectTypeChanged) {
            applyObjectType(item)
        }
    }

    protected fun applyCover(
        coverView: ImageView,
        cover: BlockView.LinkToObject.Default.Card.Cover?
    ) {
        when (cover) {
            is BlockView.LinkToObject.Default.Card.Cover.Color -> {
                coverView.apply {
                    visible()
                    setImageDrawable(null)
                    setBackgroundColor(cover.color.color)
                }
            }
            is BlockView.LinkToObject.Default.Card.Cover.Image -> {
                coverView.apply {
                    visible()
                    setBackgroundColor(0)
                    Glide
                        .with(itemView)
                        .load(cover.url)
                        .centerCrop()
                        .into(this)
                }
            }
            is BlockView.LinkToObject.Default.Card.Cover.Gradient -> {
                coverView.apply {
                    setImageDrawable(null)
                    setBackgroundColor(0)
                    when (cover.gradient) {
                        CoverGradient.YELLOW -> setBackgroundResource(R.drawable.cover_gradient_yellow)
                        CoverGradient.RED -> setBackgroundResource(R.drawable.cover_gradient_red)
                        CoverGradient.BLUE -> setBackgroundResource(R.drawable.cover_gradient_blue)
                        CoverGradient.TEAL -> setBackgroundResource(R.drawable.cover_gradient_teal)
                        CoverGradient.PINK_ORANGE -> setBackgroundResource(R.drawable.wallpaper_gradient_1)
                        CoverGradient.BLUE_PINK -> setBackgroundResource(R.drawable.wallpaper_gradient_2)
                        CoverGradient.GREEN_ORANGE -> setBackgroundResource(R.drawable.wallpaper_gradient_3)
                        CoverGradient.SKY -> setBackgroundResource(R.drawable.wallpaper_gradient_4)
                    }
                    visible()
                }
            }
            else -> {
                coverView.apply {
                    setImageDrawable(null)
                    setBackgroundColor(0)
                }
            }
        }
    }

    private fun applyBackground(background: ThemeColor) {
        containerView.setBlockBackgroundColor(background)
    }

    private fun applyObjectType(item: BlockView.LinkToObject.Default.Card) {
        if (!item.objectTypeName.isNullOrBlank()) {
            objectTypeView.text = item.objectTypeName
            objectTypeView.visible()
        } else {
            objectTypeView.gone()
        }
    }

    override fun applyDecorations(decorations: List<BlockView.Decoration>) {
        super.applyDecorations(decorations)
        selectedView.applySelectorOffset<FrameLayout.LayoutParams>(
            content = decoratableCard,
            res = itemView.resources
        )
    }
}

class LinkToObjectCardSmallIcon(binding: ItemBlockObjectLinkCardSmallIconBinding) :
    LinkToObjectCard(binding.root) {

    override val rootView = binding.root
    override val containerView = binding.containerWithBackground
    override val objectIconView = binding.cardIcon
    override val titleView = binding.cardName
    override val descriptionView = binding.cardDescription
    override val objectTypeView = binding.cardType
    override val decoratableContainer = binding.decorationContainer
    override val selectedView = binding.selected
    override val decoratableCard = binding.card

    init {
        itemView.setOnTouchListener { v, e -> editorTouchProcessor.process(v, e) }
    }

    fun bind(
        item: BlockView.LinkToObject.Default.Card.SmallIcon,
        clicked: (ListenerType) -> Unit
    ) {
        super.bind(item = item, clicked = clicked)
    }

    fun processChangePayloads(
        payloads: List<BlockViewDiffUtil.Payload>,
        item: BlockView.LinkToObject.Default.Card.SmallIcon
    ) {
        payloads.forEach { payload ->
            processChangeBasePayloads(payload, item)
        }
    }
}

class LinkToObjectCardMediumIcon(binding: ItemBlockObjectLinkCardMediumIconBinding) :
    LinkToObjectCard(binding.root) {

    override val rootView = binding.root
    override val containerView = binding.containerWithBackground
    override val objectIconView = binding.cardIcon
    override val titleView = binding.cardName
    override val descriptionView = binding.cardDescription
    override val objectTypeView = binding.cardType
    override val decoratableContainer = binding.decorationContainer
    override val selectedView = binding.selected
    override val decoratableCard = binding.card

    init {
        itemView.setOnTouchListener { v, e -> editorTouchProcessor.process(v, e) }
    }

    fun bind(
        item: BlockView.LinkToObject.Default.Card.MediumIcon,
        clicked: (ListenerType) -> Unit
    ) {
        super.bind(item = item, clicked = clicked)
    }

    fun processChangePayloads(
        payloads: List<BlockViewDiffUtil.Payload>,
        item: BlockView.LinkToObject.Default.Card.MediumIcon
    ) {
        payloads.forEach { payload ->
            processChangeBasePayloads(payload, item)
        }
    }
}

class LinkToObjectCardSmallIconCover(binding: ItemBlockObjectLinkCardSmallIconCoverBinding) :
    LinkToObjectCard(binding.root) {

    override val rootView = binding.root
    override val containerView = binding.containerWithBackground
    override val objectIconView = binding.cardIcon
    override val titleView = binding.cardName
    override val descriptionView = binding.cardDescription
    override val objectTypeView = binding.cardType
    override val decoratableContainer = binding.decorationContainer
    override val decoratableCard = binding.card
    override val selectedView = binding.selected
    private val coverView = binding.coverImage

    init {
        itemView.setOnTouchListener { v, e -> editorTouchProcessor.process(v, e) }
    }

    fun bind(
        item: BlockView.LinkToObject.Default.Card.SmallIconCover,
        clicked: (ListenerType) -> Unit
    ) {
        super.bind(item = item, clicked = clicked)
        applyCover(coverView = coverView, cover = item.cover)
    }

    fun processChangePayloads(
        payloads: List<BlockViewDiffUtil.Payload>,
        item: BlockView.LinkToObject.Default.Card.SmallIconCover
    ) {
        payloads.forEach { payload ->
            if (payload.isObjectCoverChanged) {
                applyCover(coverView = coverView, cover = item.cover)
            }
            processChangeBasePayloads(payload, item)
        }
    }
}

class LinkToObjectCardMediumIconCover(binding: ItemBlockObjectLinkCardMediumIconCoverBinding) :
    LinkToObjectCard(binding.root) {

    override val rootView = binding.root
    override val containerView = binding.containerWithBackground
    override val objectIconView = binding.cardIcon
    override val titleView = binding.cardName
    override val descriptionView = binding.cardDescription
    override val objectTypeView = binding.cardType
    override val decoratableContainer = binding.decorationContainer
    override val decoratableCard = binding.card
    override val selectedView = binding.selected
    private val coverView = binding.coverImage

    init {
        itemView.setOnTouchListener { v, e -> editorTouchProcessor.process(v, e) }
    }

    fun bind(
        item: BlockView.LinkToObject.Default.Card.MediumIconCover,
        clicked: (ListenerType) -> Unit
    ) {
        super.bind(item = item, clicked = clicked)
        applyCover(coverView = coverView, cover = item.cover)
    }

    fun processChangePayloads(
        payloads: List<BlockViewDiffUtil.Payload>,
        item: BlockView.LinkToObject.Default.Card.MediumIconCover
    ) {
        payloads.forEach { payload ->
            if (payload.isObjectCoverChanged) {
                applyCover(coverView = coverView, cover = item.cover)
            }
            processChangeBasePayloads(payload, item)
        }
    }
}