package com.anytypeio.anytype.core_ui.features.editor.holders.relations

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.viewbinding.ViewBinding
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemBlockRelationFileBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockRelationObjectBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockRelationPlaceholderBinding
import com.anytypeio.anytype.core_ui.databinding.ItemBlockRelationTagBinding
import com.anytypeio.anytype.core_ui.databinding.ItemRelationListRelationFileBinding
import com.anytypeio.anytype.core_ui.databinding.ItemRelationListRelationObjectBinding
import com.anytypeio.anytype.core_ui.databinding.ItemRelationListRelationTagBinding
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.extensions.setBlockBackgroundColor
import com.anytypeio.anytype.core_ui.features.editor.BlockViewHolder
import com.anytypeio.anytype.core_ui.widgets.GridCellFileItem
import com.anytypeio.anytype.core_ui.widgets.RelationObjectItem
import com.anytypeio.anytype.core_ui.widgets.text.TagWidget
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.editor.ThemeColor
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.relations.DocumentRelationView
import com.anytypeio.anytype.presentation.sets.model.ObjectView

sealed class RelationViewHolder(
    view: View
) : BlockViewHolder(view),
    BlockViewHolder.DragAndDropHolder,
    BlockViewHolder.IndentableHolder {

    fun setIsFeatured(isFeatured: Boolean) {
        itemView.findViewById<View>(R.id.featuredRelationCheckbox).apply {
            isSelected = isFeatured
        }
    }

    fun setIsRemovable(isRemoveable: Boolean) {
        itemView.findViewById<View>(R.id.actionsLeftContainer).apply {
            if (isRemoveable) visible() else gone()
        }
    }

    fun setBackgroundColor(color: String? = null) {
        itemView.setBlockBackgroundColor(color)
    }

    class Placeholder(val binding: ItemBlockRelationPlaceholderBinding) : RelationViewHolder(binding.root) {

        val icon: View get() = itemView.findViewById(R.id.relationIcon)

        fun bind(item: BlockView.Relation.Placeholder) = with(itemView) {
            findViewById<LinearLayout>(R.id.placeholderContainer).isSelected = item.isSelected
            indentize(item)
        }
        override fun indentize(item: BlockView.Indentable) {
            val indent = dimen(R.dimen.indent)
            icon.updateLayoutParams<LinearLayout.LayoutParams> {
                this.marginStart = item.indent * indent
            }
        }
    }

    class Default(view: View) : RelationViewHolder(view) {
        fun bind(item: DocumentRelationView) : Unit = with(itemView) {
            findViewById<TextView>(R.id.tvRelationTitle).text = item.name
            findViewById<TextView>(R.id.tvRelationValue).apply {
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
            val title = itemView.findViewById<TextView>(R.id.tvRelationTitle)
            val indent = dimen(R.dimen.indent) * item.indent
            title.updatePadding(left = indent)
        }
    }

    class Checkbox(view: View) : RelationViewHolder(view) {
        fun bind(item: DocumentRelationView.Checkbox) = with(itemView) {
            findViewById<TextView>(R.id.tvRelationTitle).text = item.name
            findViewById<ImageView>(R.id.ivRelationCheckbox).isSelected = item.isChecked
        }

        override fun indentize(item: BlockView.Indentable) {
            val title = itemView.findViewById<TextView>(R.id.tvRelationTitle)
            val indent = dimen(R.dimen.indent) * item.indent
            title.updatePadding(left = indent)
        }
    }

    class Status(view: View) : RelationViewHolder(view) {

        fun bind(item: DocumentRelationView.Status) {
            itemView.findViewById<TextView>(R.id.tvRelationTitle).text = item.name
            itemView.findViewById<TextView>(R.id.tvRelationValue).apply {
                if (item.status.isNotEmpty()) {
                    val status = item.status.first()
                    text = status.status
                    val color = ThemeColor.values().find { v -> v.title == status.color }
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
            val title = itemView.findViewById<TextView>(R.id.tvRelationTitle)
            val indent = dimen(R.dimen.indent) * item.indent
            title.updatePadding(left = indent)
        }
    }

    class Tags(val binding: ViewBinding) : RelationViewHolder(binding.root) {

        private val placeholder : TextView get() = itemView.findViewById(R.id.tvPlaceholder)

        fun bind(item: DocumentRelationView.Tags) = with(itemView) {
            findViewById<TextView>(R.id.tvRelationTitle).text = item.name
            if (item.tags.isEmpty()) {
                placeholder.visible()
            } else {
                placeholder.gone()
            }
            for (i in 0..MAX_VISIBLE_TAGS_INDEX) getViewByIndex(i)?.gone()
            item.tags.forEachIndexed { index, tagView ->
                when (index) {
                    in 0..MAX_VISIBLE_TAGS_INDEX -> {
                        getViewByIndex(index)?.let { view ->
                            view.setup(tagView.tag, tagView.color)
                        }
                    }
                }
            }
        }

        private fun getViewByIndex(index: Int): TagWidget? = when(binding) {
            is ItemBlockRelationTagBinding -> {
                when (index) {
                    0 -> binding.content.tag0
                    1 -> binding.content.tag1
                    2 -> binding.content.tag2
                    3 -> binding.content.tag3
                    4 -> binding.content.tag4
                    5 -> binding.content.tag5
                    else -> null
                }
            }
            is ItemRelationListRelationTagBinding -> {
                when (index) {
                    0 -> binding.content.tag0
                    1 -> binding.content.tag1
                    2 -> binding.content.tag2
                    3 -> binding.content.tag3
                    4 -> binding.content.tag4
                    5 -> binding.content.tag5
                    else -> null
                }
            }
            else -> null
        }

        companion object {
            const val MAX_VISIBLE_TAGS_INDEX = 5
        }

        override fun indentize(item: BlockView.Indentable) {
            val title = itemView.findViewById<TextView>(R.id.tvRelationTitle)
            val indent = dimen(R.dimen.indent) * item.indent
            title.updatePadding(left = indent)
        }
    }

    class Object(val binding: ViewBinding) : RelationViewHolder(binding.root) {

        private val placeholder : TextView get() = itemView.findViewById(R.id.tvPlaceholder)

        fun bind(item: DocumentRelationView.Object) {
            itemView.findViewById<TextView>(R.id.tvRelationTitle).text = item.name
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
            val title = itemView.findViewById<TextView>(R.id.tvRelationTitle)
            val indent = dimen(R.dimen.indent) * item.indent
            title.updatePadding(left = indent)
        }

        private fun getViewByIndex(index: Int): RelationObjectItem? = when(binding) {
            is ItemBlockRelationObjectBinding -> {
                when (index) {
                    0 -> binding.content.obj0
                    1 -> binding.content.obj1
                    2 -> binding.content.obj2
                    3 -> binding.content.obj3
                    else -> null
                }
            }
            is ItemRelationListRelationObjectBinding -> {
                when (index) {
                    0 -> binding.content.obj0
                    1 -> binding.content.obj1
                    2 -> binding.content.obj2
                    3 -> binding.content.obj3
                    else -> null
                }
            }
            else -> null
        }

        companion object {
            const val MAX_VISIBLE_OBJECTS_INDEX = 3
        }
    }

    class File(val binding: ViewBinding) : RelationViewHolder(binding.root) {

        private val placeholder : TextView get() = itemView.findViewById(R.id.tvPlaceholder)

        fun bind(item: DocumentRelationView.File) = with(itemView) {
            findViewById<TextView>(R.id.tvRelationTitle).text = item.name
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

        private fun getViewByIndex(index: Int): GridCellFileItem? = when(binding) {
            is ItemBlockRelationFileBinding -> {
                when (index) {
                    0 -> binding.content.file0
                    1 -> binding.content.file1
                    2 -> binding.content.file2
                    else -> null
                }
            }
            is ItemRelationListRelationFileBinding -> {
                when (index) {
                    0 -> binding.content.file0
                    1 -> binding.content.file1
                    2 -> binding.content.file2
                    else -> null
                }
            }
            else -> null
        }

        companion object {
            const val MAX_VISIBLE_FILES_INDEX = 2
        }

        override fun indentize(item: BlockView.Indentable) {
            val title = itemView.findViewById<TextView>(R.id.tvRelationTitle)
            val indent = dimen(R.dimen.indent) * item.indent
            title.updatePadding(left = indent)
        }
    }
}