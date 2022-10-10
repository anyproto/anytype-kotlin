package com.anytypeio.anytype.core_ui.features.editor.holders.other

import android.text.Spannable
import android.text.SpannableString
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_ui.BuildConfig
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.SearchHighlightSpan
import com.anytypeio.anytype.core_ui.common.SearchTargetHighlightSpan
import com.anytypeio.anytype.core_ui.databinding.ItemBlockObjectLinkCardBinding
import com.anytypeio.anytype.core_ui.extensions.setBlockBackgroundColor
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.EditorTouchProcessor
import com.anytypeio.anytype.core_ui.features.editor.SupportCustomTouchProcessor
import com.anytypeio.anytype.core_ui.features.editor.SupportNesting
import com.anytypeio.anytype.core_ui.features.editor.decoration.DecoratableCardViewHolder
import com.anytypeio.anytype.core_ui.features.editor.decoration.applySelectorOffset
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.removeSpans
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.cover.CoverColor
import com.anytypeio.anytype.presentation.editor.cover.CoverGradient
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.bumptech.glide.Glide

class LinkToObjectCard(
    private val binding: ItemBlockObjectLinkCardBinding
) : BlockViewHolder(binding.root),
    BlockViewHolder.IndentableHolder,
    BlockViewHolder.DragAndDropHolder,
    DecoratableCardViewHolder,
    SupportCustomTouchProcessor,
    SupportNesting {

    private val root = binding.root
    private val container = binding.containerWithBackground
    private val cover = binding.cover
    private val untitled = itemView.resources.getString(R.string.untitled)
    private val objectIcon = binding.cardIcon
    private val title = binding.cardName
    private val description = binding.cardDescription
    private val selected = binding.selected
    private val type = binding.cardType

    override val decoratableContainer = binding.decorationContainer
    override val decoratableCard = binding.card

    override val editorTouchProcessor = EditorTouchProcessor(
        fallback = { e -> itemView.onTouchEvent(e) }
    )

    init {
        itemView.setOnTouchListener { v, e -> editorTouchProcessor.process(v, e) }
        applyDefaultOffsets()
    }

    fun bind(
        item: BlockView.LinkToObject.Default.Card,
        clicked: (ListenerType) -> Unit
    ) {
        indentize(item)

        selected.isSelected = item.isSelected

        applyRootMargins(item)

        applyName(item)

        applyDescription(item)

        applyCover(item)

        applyBackground(item.background)

        applySearchHighlight(item)

        applyImageOrEmoji(item)

        applyObjectType(item)

        itemView.setOnClickListener { clicked(ListenerType.LinkToObject(item.id)) }
    }

    private fun applyName(item: BlockView.LinkToObject.Default.Card) {
        val name = item.text
        when {
            name == null -> title.gone()
            name.isBlank() -> {
                title.visible()
                val sb = SpannableString(untitled)
                title.setText(sb, TextView.BufferType.SPANNABLE)
            }
            else -> {
                title.visible()
                val sb = SpannableString(name)
                title.setText(sb, TextView.BufferType.SPANNABLE)
            }
        }
    }

    private fun applyDescription(item: BlockView.LinkToObject.Default.Card) {
        if (item.description.isNullOrBlank()) {
            description.gone()
        } else {
            description.visible()
            description.text = item.description
        }
    }

    private fun applyImageOrEmoji(item: BlockView.LinkToObject.Default.Card) {
        when (item.icon) {
            ObjectIcon.None -> {
                objectIcon.gone()
            }
            else -> {
                objectIcon.visible()
                objectIcon.setIcon(item.icon)
            }
        }
    }

    private fun applyCover(item: BlockView.LinkToObject.Default.Card) {
        setCover(
            coverColor = item.coverColor,
            coverGradient = item.coverGradient,
            coverImage = item.coverImage
        )
    }

    private fun applySearchHighlight(item: BlockView.Searchable) {
        item.searchFields.find { it.key == BlockView.Searchable.Field.DEFAULT_SEARCH_FIELD_KEY }
            ?.let { field ->
                applySearchHighlight(field, title)
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
        title.editableText?.removeSpans<SearchHighlightSpan>()
        title.editableText?.removeSpans<SearchTargetHighlightSpan>()
    }

    override fun indentize(item: BlockView.Indentable) {
        if (!BuildConfig.NESTED_DECORATION_ENABLED) {
            root.updateLayoutParams<RecyclerView.LayoutParams> {
                marginStart = item.indent * dimen(R.dimen.indent)
            }
        }
    }

    fun processChangePayload(payloads: List<BlockViewDiffUtil.Payload>, item: BlockView) {
        check(item is BlockView.LinkToObject.Default.Card) { "Expected a link to object block card, but was: $item" }
        payloads.forEach { payload ->
            if (payload.changes.contains(BlockViewDiffUtil.SELECTION_CHANGED)) {
                selected.isSelected = item.isSelected
                applyImageOrEmoji(item)
            }
            if (payload.isSearchHighlightChanged) {
                applySearchHighlight(item)
            }
            if (payload.isObjectTitleChanged)
                applyName(item)
            if (payload.isObjectIconChanged)
                applyImageOrEmoji(item)
            if (payload.isObjectDescriptionChanged)
                applyDescription(item)
            if (payload.isObjectCoverChanged)
                applyCover(item)
            if (payload.isBackgroundColorChanged)
                applyBackground(item.background)
            if (payload.isObjectTypeChanged)
                applyObjectType(item)
        }
    }

    private fun setCover(
        coverColor: CoverColor?,
        coverImage: String?,
        coverGradient: String?
    ) {
        when {
            coverColor != null -> {
                cover.apply {
                    visible()
                    setImageDrawable(null)
                    setBackgroundColor(coverColor.color)
                }
            }
            coverImage != null -> {
                cover.apply {
                    visible()
                    setBackgroundColor(0)
                    Glide
                        .with(itemView)
                        .load(coverImage)
                        .centerCrop()
                        .into(this)
                }
            }
            coverGradient != null -> {
                cover.apply {
                    setImageDrawable(null)
                    setBackgroundColor(0)
                    when (coverGradient) {
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
                cover.apply {
                    setImageDrawable(null)
                    setBackgroundColor(0)
                    gone()
                }
            }
        }
    }

    private fun applyBackground(background: ThemeColor) {
        container.setBlockBackgroundColor(background)
    }

    private fun applyRootMargins(item: BlockView.LinkToObject.Default.Card) {
        if (!BuildConfig.NESTED_DECORATION_ENABLED) {
            if (item.isPreviousBlockMedia) {
                root.updateLayoutParams<RecyclerView.LayoutParams> {
                    apply { topMargin = 0 }
                }
            } else {
                root.updateLayoutParams<RecyclerView.LayoutParams> {
                    apply {
                        topMargin =
                            root.resources.getDimension(R.dimen.default_link_card_root_margin_top)
                                .toInt()
                    }
                }
            }
        }
    }

    private fun applyObjectType(item: BlockView.LinkToObject.Default.Card) {
        if (!item.objectTypeName.isNullOrBlank()) {
            type.text = item.objectTypeName
            type.visible()
        } else {
            type.gone()
        }
    }

    private fun applyDefaultOffsets() {
        if (!BuildConfig.NESTED_DECORATION_ENABLED) {
            root.updatePadding(
                left = dimen(R.dimen.default_document_item_padding_start),
                right = dimen(R.dimen.default_document_item_padding_end)
            )
            root.updateLayoutParams<RecyclerView.LayoutParams> {
                bottomMargin = dimen(R.dimen.dp_10)
            }
            binding.card.updateLayoutParams<FrameLayout.LayoutParams> {
                marginStart = dimen(R.dimen.dp_12)
                marginEnd = dimen(R.dimen.dp_12)
            }
        }
    }

    override fun applyDecorations(decorations: List<BlockView.Decoration>) {
        super.applyDecorations(decorations)
        if (BuildConfig.NESTED_DECORATION_ENABLED) {
            binding.selected.applySelectorOffset<FrameLayout.LayoutParams>(
                content = binding.card,
                res = itemView.resources
            )
        }
    }
}