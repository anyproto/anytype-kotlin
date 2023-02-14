package com.anytypeio.anytype.core_ui.features.editor.holders.dataview

import android.text.Spannable
import android.text.SpannableString
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.common.SearchHighlightSpan
import com.anytypeio.anytype.core_ui.common.SearchTargetHighlightSpan
import com.anytypeio.anytype.core_ui.databinding.ItemBlockDataViewDefaultBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockDataViewEmptyDataBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockDataViewEmptySourceBinding
import com.anytypeio.anytype.core_ui.extensions.setBlockBackgroundColor
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.EditorTouchProcessor
import com.anytypeio.anytype.core_ui.features.editor.SupportCustomTouchProcessor
import com.anytypeio.anytype.core_ui.features.editor.SupportNesting
import com.anytypeio.anytype.core_ui.features.editor.decoration.DecoratableCardViewHolder
import com.anytypeio.anytype.core_ui.features.editor.decoration.EditorDecorationContainer
import com.anytypeio.anytype.core_ui.widgets.ObjectIconWidget
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.removeSpans
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.objects.ObjectIcon

class DataViewBlockEmptySourceHolder(binding: ItemBlockDataViewEmptySourceBinding) :
    DataViewBlockViewHolder(binding.root) {

    override val decoratableContainer: EditorDecorationContainer = binding.decorationContainer
    override val rootView: View = binding.root
    override val containerView: ConstraintLayout = binding.containerWithBackground
    override val objectIconView: ObjectIconWidget = binding.cardIcon
    override val titleView: TextView = binding.cardName
    override val descriptionView: TextView = binding.cardDescription
    override val selectedView: View = binding.selected
    override val decoratableCard: CardView = binding.card

    init {
        itemView.setOnTouchListener { v, e -> editorTouchProcessor.process(v, e) }
    }

    fun bind(
        item: BlockView.DataView.EmptySource,
        clicked: (ListenerType) -> Unit
    ) {
        super.bind(item = item, clicked = clicked)
    }

    fun processChangePayloads(
        payloads: List<BlockViewDiffUtil.Payload>,
        item: BlockView.DataView
    ) {
        payloads.forEach { payload ->
            processChangeBasePayloads(payload, item)
        }
    }
}

class DataViewBlockEmptyDataHolder(binding: ItemBlockDataViewEmptyDataBinding) :
    DataViewBlockViewHolder(binding.root) {

    override val decoratableContainer: EditorDecorationContainer = binding.decorationContainer
    override val rootView: View = binding.root
    override val containerView: ConstraintLayout = binding.containerWithBackground
    override val objectIconView: ObjectIconWidget = binding.cardIcon
    override val titleView: TextView = binding.cardName
    override val descriptionView: TextView = binding.cardDescription
    override val selectedView: View = binding.selected
    override val decoratableCard: CardView = binding.card

    init {
        itemView.setOnTouchListener { v, e -> editorTouchProcessor.process(v, e) }
    }

    fun bind(
        item: BlockView.DataView.EmptyData,
        clicked: (ListenerType) -> Unit
    ) {
        super.bind(item = item, clicked = clicked)
    }

    fun processChangePayloads(
        payloads: List<BlockViewDiffUtil.Payload>,
        item: BlockView.DataView
    ) {
        payloads.forEach { payload ->
            processChangeBasePayloads(payload, item)
        }
    }
}

data class DataViewBlockDefaultHolder(
    val binding: ItemBlockDataViewDefaultBinding
) : DataViewBlockViewHolder(binding.root) {

    override val decoratableContainer: EditorDecorationContainer = binding.decorationContainer
    override val rootView: View = binding.root
    override val containerView: ConstraintLayout = binding.containerWithBackground
    override val objectIconView: ObjectIconWidget = binding.cardIcon
    override val titleView: TextView = binding.cardName
    override val descriptionView: TextView = binding.cardDescription
    override val selectedView: View = binding.selected
    override val decoratableCard: CardView = binding.card

    init {
        itemView.setOnTouchListener { v, e -> editorTouchProcessor.process(v, e) }
    }

    fun bind(
        item: BlockView.DataView.Default,
        clicked: (ListenerType) -> Unit
    ) {
        super.bind(item = item, clicked = clicked)
    }

    fun processChangePayloads(
        payloads: List<BlockViewDiffUtil.Payload>,
        item: BlockView.DataView
    ) {
        payloads.forEach { payload ->
            processChangeBasePayloads(payload, item)
        }
    }
}

data class DataViewBlockDeleteHolder(
    val binding: ItemBlockDataViewEmptyDataBinding
) : DataViewBlockViewHolder(binding.root) {

    override val decoratableContainer: EditorDecorationContainer = binding.decorationContainer
    override val rootView: View = binding.root
    override val containerView: ConstraintLayout = binding.containerWithBackground
    override val objectIconView: ObjectIconWidget = binding.cardIcon
    override val titleView: TextView = binding.cardName
    override val descriptionView: TextView = binding.cardDescription
    override val selectedView: View = binding.selected
    override val decoratableCard: CardView = binding.card

    init {
        itemView.setOnTouchListener { v, e -> editorTouchProcessor.process(v, e) }
    }

    fun bind(
        item: BlockView.DataView.Deleted,
        clicked: (ListenerType) -> Unit
    ) {
        super.bind(item = updateTitle(item), clicked = clicked)
    }

    fun processChangePayloads(
        payloads: List<BlockViewDiffUtil.Payload>,
        item: BlockView.DataView.Deleted
    ) {
        payloads.forEach { payload ->
            if (payload.isDataViewTitleChanged) {
                processChangeBasePayloads(payload, updateTitle(item))
            } else {
                processChangeBasePayloads(payload, item)
            }
        }
    }

    private fun updateTitle(item: BlockView.DataView.Deleted): BlockView.DataView {
        return item.copy(
            title = itemView.resources.getString(R.string.non_existent_object),
        )
    }
}

sealed class DataViewBlockViewHolder(
    view: View
) : BlockViewHolder(view),
    BlockViewHolder.DragAndDropHolder,
    DecoratableCardViewHolder,
    SupportCustomTouchProcessor,
    SupportNesting {

    protected abstract val rootView: View
    abstract val containerView: ConstraintLayout

    private val untitled = itemView.resources.getString(R.string.untitled_set)
    abstract val objectIconView: ObjectIconWidget
    abstract val titleView: TextView
    abstract val descriptionView: TextView
    abstract val selectedView: View
    abstract override val decoratableCard: CardView

    override val editorTouchProcessor = EditorTouchProcessor(
        fallback = { e -> itemView.onTouchEvent(e) }
    )

    protected fun bind(
        item: BlockView.DataView,
        clicked: (ListenerType) -> Unit
    ) {
        selected(item)

        applyName(item)

        applyBackground(item.background)

        applySearchHighlight(item)

        applyImageOrEmoji(item)

        itemView.setOnClickListener { clicked(ListenerType.DataViewClick(item.id)) }
    }

    private fun selected(item: BlockView.DataView) {
        selectedView.isSelected = item.isSelected
    }

    private fun applyName(item: BlockView.DataView) {
        val name = item.title
        val sb = if (name.isNullOrBlank()) "" else SpannableString(name)
        titleView.text = sb
    }

    private fun applyImageOrEmoji(item: BlockView.DataView) {
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

    protected fun processChangeBasePayloads(
        payload: BlockViewDiffUtil.Payload,
        item: BlockView.DataView
    ) {
        if (payload.isSelectionChanged) {
            selected(item)
        }
        if (payload.isDataViewTitleChanged) {
            applyName(item)
        }
        if (payload.isDataViewIconChanged) {
            applyImageOrEmoji(item)
        }
        if (payload.isDataViewBackgroundChanged) {
            applyBackground(item.background)
        }
        if (payload.isSearchHighlightChanged) {
            applySearchHighlight(item)
        }
    }

    private fun applyBackground(background: ThemeColor) {
        containerView.setBlockBackgroundColor(background)
    }

    override fun applyDecorations(decorations: List<BlockView.Decoration>) {
        super.applyDecorations(decorations)
        decoratableContainer.decorate(decorations) { rect ->
            rootView.updateLayoutParams<RecyclerView.LayoutParams> {
                topMargin = if (rect.left == 0) {
                    itemView.resources.getDimension(R.dimen.dp_10).toInt()
                } else {
                    0
                }
            }
            selectedView.updateLayoutParams<FrameLayout.LayoutParams> {
                val defaultIndentOffset =
                    itemView.resources.getDimension(R.dimen.default_indent).toInt()
                leftMargin = defaultIndentOffset + rect.left
                rightMargin = defaultIndentOffset + rect.right
            }
        }
    }
}
