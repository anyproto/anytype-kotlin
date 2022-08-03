package com.anytypeio.anytype.core_ui.features.editor.holders.relations

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
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
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.relations.DocumentRelationView
import com.anytypeio.anytype.presentation.sets.model.ObjectView

sealed class RelationBlockViewHolder(
    view: View
) : BlockViewHolder(view),
    BlockViewHolder.DragAndDropHolder,
    BlockViewHolder.IndentableHolder,
    DecoratableViewHolder {

    fun setBackgroundColor(background: ThemeColor) {
        itemView.setBlockBackgroundColor(background)
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
        viewGroup: ViewGroup,
        decorations: List<BlockView.Decoration>,
    ) {
        if (BuildConfig.NESTED_DECORATION_ENABLED) {
            decoratableContainer.decorate(decorations = decorations) { rect ->
                viewGroup.updateLayoutParams<FrameLayout.LayoutParams> {
                    marginStart = dimen(R.dimen.dp_8) + rect.left
                    marginEnd = dimen(R.dimen.dp_8) + rect.right
                    bottomMargin = rect.bottom
                }
            }
        }
    }

    init {
        applyDefaultOffsets(itemView)
    }

    class Placeholder(binding: ItemBlockRelationPlaceholderBinding) :
        RelationBlockViewHolder(binding.root) {

        val icon = binding.relationIcon
        val content = binding.content

        fun bind(item: BlockView.Relation.Placeholder) = with(itemView) {
            content.isSelected = item.isSelected
            indentize(item)
        }

        override fun indentize(item: BlockView.Indentable) {
            indent(item, icon)
        }

        override val decoratableContainer: EditorDecorationContainer = binding.decorationContainer

        override fun applyDecorations(decorations: List<BlockView.Decoration>) {
            applyContentDecorations(content, decorations)
        }
    }

    class Default(binding: ItemBlockRelationDefaultBinding) :
        RelationBlockViewHolder(binding.root) {

        private val tvTitle = binding.content.tvRelationTitle
        private val tvValue = binding.content.tvRelationValue
        private val content = binding.content.root

        fun bind(item: DocumentRelationView): Unit = with(itemView) {
            tvTitle.text = item.name
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

        override fun indentize(item: BlockView.Indentable) {
            indent(item, tvTitle)
        }

        override val decoratableContainer: EditorDecorationContainer = binding.decorationContainer

        override fun applyDecorations(decorations: List<BlockView.Decoration>) {
            applyContentDecorations(content, decorations)
        }
    }

    class Checkbox(binding: ItemBlockRelationCheckboxBinding) :
        RelationBlockViewHolder(binding.root) {

        private val tvTitle = binding.content.tvRelationTitle
        private val tvCheckbox = binding.content.ivRelationCheckbox
        private val content = binding.content.root

        fun bind(item: DocumentRelationView.Checkbox) = with(itemView) {
            tvTitle.text = item.name
            tvCheckbox.isSelected = item.isChecked
        }

        override fun indentize(item: BlockView.Indentable) {
            indent(item, tvTitle)
        }

        override val decoratableContainer: EditorDecorationContainer = binding.decorationContainer

        override fun applyDecorations(decorations: List<BlockView.Decoration>) {
            applyContentDecorations(content, decorations)
        }
    }

    class Status(binding: ItemBlockRelationStatusBinding) : RelationBlockViewHolder(binding.root) {

        private val tvTitle = binding.content.tvRelationTitle
        private val tvValue = binding.content.tvRelationValue
        private val content = binding.content.root

        fun bind(item: DocumentRelationView.Status) {
            tvTitle.text = item.name
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

        override fun indentize(item: BlockView.Indentable) {
            indent(item, tvTitle)
        }

        override val decoratableContainer: EditorDecorationContainer = binding.decorationContainer

        override fun applyDecorations(decorations: List<BlockView.Decoration>) {
            applyContentDecorations(content, decorations)
        }
    }

    class Tags(binding: ItemBlockRelationTagBinding) : RelationBlockViewHolder(binding.root) {

        private val tvTitle = binding.content.tvRelationTitle
        private val content = binding.content
        private val placeholder = binding.content.tvPlaceholder

        fun bind(item: DocumentRelationView.Tags) = with(itemView) {
            tvTitle.text = item.name
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

        private fun getViewByIndex(index: Int): TagWidget? = when (index) {
            0 -> content.tag0
            1 -> content.tag1
            2 -> content.tag2
            3 -> content.tag3
            4 -> content.tag4
            5 -> content.tag5
            else -> null
        }

        override fun indentize(item: BlockView.Indentable) {
            indent(item, tvTitle)
        }

        override val decoratableContainer: EditorDecorationContainer = binding.decorationContainer

        override fun applyDecorations(decorations: List<BlockView.Decoration>) {
            applyContentDecorations(content.root, decorations)
        }

        companion object {
            const val MAX_VISIBLE_TAGS_INDEX = 5
        }
    }

    class Object(binding: ItemBlockRelationObjectBinding) : RelationBlockViewHolder(binding.root) {

        private val tvTitle = binding.content.tvRelationTitle
        private val placeholder = binding.content.tvPlaceholder
        private val content = binding.content

        fun bind(item: DocumentRelationView.Object) {
            tvTitle.text = item.name
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

        override fun indentize(item: BlockView.Indentable) {
            indent(item, tvTitle)
        }

        private fun getViewByIndex(index: Int): RelationObjectItem? = when (index) {
            0 -> content.obj0
            1 -> content.obj1
            2 -> content.obj2
            3 -> content.obj3
            else -> null
        }

        override val decoratableContainer: EditorDecorationContainer = binding.decorationContainer

        override fun applyDecorations(decorations: List<BlockView.Decoration>) {
            applyContentDecorations(content.root, decorations)
        }

        companion object {
            const val MAX_VISIBLE_OBJECTS_INDEX = 3
        }
    }

    class File(binding: ItemBlockRelationFileBinding) : RelationBlockViewHolder(binding.root) {

        private val tvTitle = binding.content.tvRelationTitle
        private val placeholder = binding.content.tvPlaceholder
        private val content = binding.content

        fun bind(item: DocumentRelationView.File) = with(itemView) {
            tvTitle.text = item.name
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

        private fun getViewByIndex(index: Int): GridCellFileItem? = when (index) {
            0 -> content.file0
            1 -> content.file1
            2 -> content.file2
            else -> null
        }

        override fun indentize(item: BlockView.Indentable) {
            indent(item, tvTitle)
        }

        override val decoratableContainer: EditorDecorationContainer = binding.decorationContainer

        override fun applyDecorations(decorations: List<BlockView.Decoration>) {
            applyContentDecorations(content.root, decorations)
        }

        companion object {
            const val MAX_VISIBLE_FILES_INDEX = 2
        }
    }
}