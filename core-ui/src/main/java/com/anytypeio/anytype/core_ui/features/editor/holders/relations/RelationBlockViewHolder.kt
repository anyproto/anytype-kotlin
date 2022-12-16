package com.anytypeio.anytype.core_ui.features.editor.holders.relations

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_ui.BuildConfig
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockRelationCheckboxBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockRelationDefaultBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockRelationFileBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockRelationObjectBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockRelationPlaceholderBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockRelationStatusBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockRelationTagBinding
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.extensions.setBlockBackgroundColor
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.features.editor.decoration.DecoratableViewHolder
import com.anytypeio.anytype.core_ui.features.editor.decoration.EditorDecorationContainer
import com.anytypeio.anytype.core_ui.widgets.GridCellFileItem
import com.anytypeio.anytype.core_ui.widgets.RelationObjectItem
import com.anytypeio.anytype.core_ui.widgets.text.TagWidget
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.features.editor.BlockViewDiffUtil
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.relations.DocumentRelationView
import com.anytypeio.anytype.presentation.sets.model.ObjectView

sealed class RelationBlockViewHolder(
    view: View
) : BlockViewHolder(view),
    BlockViewHolder.DragAndDropHolder,
    BlockViewHolder.IndentableHolder,
    DecoratableViewHolder {

    abstract val selected: View
    abstract val content: ViewGroup
    abstract val relationName: TextView

    fun bindHolder(item: BlockView.Relation.Related) {
        indentize(item = item)
        applyBackground(item)
        applySelection(item)
    }

    fun indent(item: BlockView.Indentable, view: View) {
        if (!BuildConfig.NESTED_DECORATION_ENABLED) {
            val indent = dimen(R.dimen.indent) * item.indent
            view.updatePadding(left = indent)
        }
    }

    private fun applyDefaultOffsets(root: View) {
        if (!BuildConfig.NESTED_DECORATION_ENABLED) {
            root.updatePadding(
                left = dimen(R.dimen.default_document_item_padding_start),
                right = dimen(R.dimen.default_document_item_padding_end)
            )
            root.updateLayoutParams<RecyclerView.LayoutParams> {
                bottomMargin = dimen(R.dimen.dp_2)
            }
        }
    }

    fun applyContentDecorations(
        root: View,
        decorations: List<BlockView.Decoration>
    ) {
        if (BuildConfig.NESTED_DECORATION_ENABLED) {
            if (decorations.isEmpty() || (decorations.size == 1 && decorations[0].style == BlockView.Decoration.Style.None)) {
                root.updateLayoutParams<RecyclerView.LayoutParams> {
                    bottomMargin = dimen(R.dimen.dp_2)
                }
            }
            decoratableContainer.decorate(decorations = decorations) { rect ->
                content.updateLayoutParams<FrameLayout.LayoutParams> {
                    marginStart = dimen(R.dimen.dp_8) + rect.left
                    marginEnd = dimen(R.dimen.dp_8) + rect.right
                    bottomMargin = rect.bottom
                }
                selected.updateLayoutParams<FrameLayout.LayoutParams> {
                    marginStart = dimen(R.dimen.dp_8) + rect.left
                    marginEnd = dimen(R.dimen.dp_8) + rect.right
                    bottomMargin = rect.bottom
                }
            }
        }
    }

    fun applyBackground(item: BlockView.Relation) {
        content.setBlockBackgroundColor(item.background)
    }

    fun applySelection(item: BlockView.Selectable) {
        selected.isSelected = item.isSelected
    }

    fun processChangePayload(
        payloads: List<BlockViewDiffUtil.Payload>,
        block: BlockView.Relation.Related
    ) {
        payloads.forEach { payload ->
            if (payload.selectionChanged()) {
                applySelection(block)
            }
            if (payload.backgroundColorChanged()) {
                applyBackground(block)
            }
            if (payload.relationValueChanged()) {
                applyRelationValue(block.view)
            }
            if (payload.relationNameChanged()) {
                applyRelationName(block.view.name)
            }
        }
    }

    fun applyRelationName(name: String) {
        relationName.text = name
    }

    abstract fun applyRelationValue(item: DocumentRelationView)

    init {
        applyDefaultOffsets(itemView)
    }

    class Placeholder(binding: ItemBlockRelationPlaceholderBinding) :
        RelationBlockViewHolder(binding.root) {

        val icon = binding.relationIcon
        override val content = binding.content
        override val selected = binding.selected
        override val relationName: TextView = binding.tvPlaceholder

        fun bind(item: BlockView.Relation.Placeholder) = with(itemView) {
            indentize(item)
            applySelection(item)
            applyBackground(item)
        }

        override fun indentize(item: BlockView.Indentable) {
            indent(item, itemView)
        }

        override val decoratableContainer: EditorDecorationContainer = binding.decorationContainer

        override fun applyDecorations(decorations: List<BlockView.Decoration>) {
            super.applyContentDecorations(itemView, decorations)
        }

        override fun applyRelationValue(item: DocumentRelationView) {}
    }

    class Default(binding: ItemBlockRelationDefaultBinding) :
        RelationBlockViewHolder(binding.root) {

        private val tvTitle = binding.tvRelationTitle
        private val tvValue = binding.tvRelationValue
        override val content = binding.content
        override val selected = binding.selected
        override val relationName: TextView = tvTitle
        override val decoratableContainer: EditorDecorationContainer = binding.decorationContainer

        fun bind(item: DocumentRelationView) {
            applyRelationName(item.name)
            applyRelationValue(item)
        }

        override fun indentize(item: BlockView.Indentable) {
            indent(item, itemView)
        }

        override fun applyDecorations(decorations: List<BlockView.Decoration>) {
            super.applyContentDecorations(itemView, decorations)
        }

        override fun applyRelationValue(item: DocumentRelationView) {
            tvValue.apply {
                text = item.value
                if (item is DocumentRelationView.Default) {
                    when (item.format) {
                        Relation.Format.SHORT_TEXT -> setHint(R.string.enter_text)
                        Relation.Format.LONG_TEXT -> setHint(R.string.enter_text)
                        Relation.Format.NUMBER -> setHint(R.string.enter_number)
                        Relation.Format.DATE -> setHint(R.string.enter_date)
                        Relation.Format.URL -> setHint(R.string.enter_url)
                        Relation.Format.EMAIL -> setHint(R.string.enter_email)
                        Relation.Format.PHONE -> setHint(R.string.enter_phone)
                        else -> setHint(R.string.enter_value)
                    }
                }
            }
        }
    }

    class Checkbox(binding: ItemBlockRelationCheckboxBinding) :
        RelationBlockViewHolder(binding.root) {

        private val tvTitle = binding.tvRelationTitle
        private val tvCheckbox = binding.ivRelationCheckbox
        override val content = binding.content
        override val selected = binding.selected
        override val relationName: TextView = tvTitle

        fun bind(item: DocumentRelationView) {
            applyRelationName(item.name)
            applyRelationValue(item)
        }

        override fun indentize(item: BlockView.Indentable) {
            indent(item, itemView)
        }

        override val decoratableContainer: EditorDecorationContainer = binding.decorationContainer

        override fun applyDecorations(decorations: List<BlockView.Decoration>) {
            super.applyContentDecorations(itemView, decorations)
        }

        override fun applyRelationValue(item: DocumentRelationView) {
            if (item is DocumentRelationView.Checkbox) {
                tvCheckbox.isSelected = item.isChecked
            }
        }
    }

    class Status(binding: ItemBlockRelationStatusBinding) : RelationBlockViewHolder(binding.root) {

        private val tvTitle = binding.tvRelationTitle
        private val tvValue = binding.tvRelationValue
        override val content = binding.content
        override val selected = binding.selected
        override val relationName: TextView = tvTitle

        val c = binding.content

        fun bind(item: DocumentRelationView) {
            applyRelationName(item.name)
            applyRelationValue(item)
        }

        override fun indentize(item: BlockView.Indentable) {
            indent(item, itemView)
        }

        override val decoratableContainer: EditorDecorationContainer = binding.decorationContainer

        override fun applyDecorations(decorations: List<BlockView.Decoration>) {
            super.applyContentDecorations(itemView, decorations)
        }

        override fun applyRelationValue(item: DocumentRelationView) {
            if (item is DocumentRelationView.Status) {
                tvValue.apply {
                    if (item.status.isNotEmpty()) {
                        val status = item.status.first()
                        text = status.status
                        val color = ThemeColor.values().find { v -> v.code == status.color }
                        val defaultTextColor = resources.getColor(R.color.text_primary, null)
                        if (color != null && color != ThemeColor.DEFAULT) {
                            setTextColor(resources.dark(color, defaultTextColor))
                        } else {
                            setTextColor(defaultTextColor)
                        }
                    } else {
                        text = null
                    }
                }
            }
        }
    }

    class Tags(private val binding: ItemBlockRelationTagBinding) :
        RelationBlockViewHolder(binding.root) {

        private val tvTitle = binding.tvRelationTitle
        override val content = binding.content
        private val placeholder = binding.tvPlaceholder
        override val selected = binding.selected
        override val relationName: TextView = tvTitle

        fun bind(item: DocumentRelationView) {
            applyRelationName(item.name)
            applyRelationValue(item)
        }

        override fun applyRelationValue(item: DocumentRelationView) {
            if (item is DocumentRelationView.Tags) {
                if (item.tags.isEmpty()) {
                    placeholder.visible()
                } else {
                    placeholder.gone()
                }
                for (i in 0..MAX_VISIBLE_TAGS_INDEX) getViewByIndex(i)?.gone()
                item.tags.forEachIndexed { index, tagView ->
                    when (index) {
                        in 0..MAX_VISIBLE_TAGS_INDEX -> {
                            getViewByIndex(index)?.setup(tagView.tag, tagView.color)
                        }
                    }
                }
            }
        }

        private fun getViewByIndex(index: Int): TagWidget? = when (index) {
            0 -> binding.tag0
            1 -> binding.tag1
            2 -> binding.tag2
            3 -> binding.tag3
            4 -> binding.tag4
            5 -> binding.tag5
            else -> null
        }

        override fun indentize(item: BlockView.Indentable) {
            indent(item, itemView)
        }

        override val decoratableContainer: EditorDecorationContainer = binding.decorationContainer

        override fun applyDecorations(decorations: List<BlockView.Decoration>) {
            super.applyContentDecorations(itemView, decorations)
        }

        companion object {
            const val MAX_VISIBLE_TAGS_INDEX = 5
        }
    }

    class Object(private val binding: ItemBlockRelationObjectBinding) :
        RelationBlockViewHolder(binding.root) {

        private val tvTitle = binding.tvRelationTitle
        private val placeholder = binding.tvPlaceholder
        override val content = binding.content
        override val selected = binding.selected
        override val relationName: TextView = tvTitle

        fun bind(item: DocumentRelationView) {
            applyRelationName(item.name)
            applyRelationValue(item)
        }

        override fun applyRelationValue(item: DocumentRelationView) {
            if (item is DocumentRelationView.Object) {
                if (item.objects.isEmpty()) {
                    placeholder.visible()
                } else {
                    placeholder.gone()
                }
                for (i in 0..MAX_VISIBLE_OBJECTS_INDEX) getViewByIndex(i)?.gone()
                item.objects.forEachIndexed { index, objectView ->
                    when (index) {
                        in 0..MAX_VISIBLE_OBJECTS_INDEX -> {
                            getViewByIndex(index)?.let { view ->
                                if (objectView is ObjectView.Default) {
                                    view.visible()
                                    view.setup(name = objectView.name, icon = objectView.icon)
                                } else {
                                    view.visible()
                                    view.setupAsNonExistent()
                                }
                            }
                        }
                    }
                }
            }
        }

        override fun indentize(item: BlockView.Indentable) {
            indent(item, itemView)
        }

        private fun getViewByIndex(index: Int): RelationObjectItem? = when (index) {
            0 -> binding.obj0
            1 -> binding.obj1
            2 -> binding.obj2
            3 -> binding.obj3
            else -> null
        }

        override val decoratableContainer: EditorDecorationContainer = binding.decorationContainer

        override fun applyDecorations(decorations: List<BlockView.Decoration>) {
            super.applyContentDecorations(itemView, decorations)
        }

        companion object {
            const val MAX_VISIBLE_OBJECTS_INDEX = 3
        }
    }

    class File(private val binding: ItemBlockRelationFileBinding) :
        RelationBlockViewHolder(binding.root) {

        private val tvTitle = binding.tvRelationTitle
        private val placeholder = binding.tvPlaceholder
        override val content = binding.content
        override val selected = binding.selected
        override val relationName: TextView = tvTitle

        fun bind(item: DocumentRelationView) {
            applyRelationName(item.name)
            applyRelationValue(item)
        }

        override fun applyRelationValue(item: DocumentRelationView) {
            if (item is DocumentRelationView.File) {
                if (item.files.isEmpty()) {
                    placeholder.visible()
                } else {
                    placeholder.gone()
                }
                item.files.forEachIndexed { index, fileView ->
                    when (index) {
                        in 0..MAX_VISIBLE_FILES_INDEX -> {
                            getViewByIndex(index)?.let { view ->
                                view.visible()
                                view.setup(name = fileView.name, mime = fileView.mime)
                            }
                        }
                    }
                }
            }
        }

        private fun getViewByIndex(index: Int): GridCellFileItem? = when (index) {
            0 -> binding.file0
            1 -> binding.file1
            2 -> binding.file2
            else -> null
        }

        override fun indentize(item: BlockView.Indentable) {
            indent(item, itemView)
        }

        override val decoratableContainer: EditorDecorationContainer = binding.decorationContainer

        override fun applyDecorations(decorations: List<BlockView.Decoration>) {
            super.applyContentDecorations(itemView, decorations)
        }

        companion object {
            const val MAX_VISIBLE_FILES_INDEX = 2
        }
    }
}