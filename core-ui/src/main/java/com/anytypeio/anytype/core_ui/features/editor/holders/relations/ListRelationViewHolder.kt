package com.anytypeio.anytype.core_ui.features.editor.holders.relations

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.ItemRelationListRelationCheckboxBinding
import com.anytypeio.anytype.core_ui.databinding.ItemRelationListRelationDefaultBinding
import com.anytypeio.anytype.core_ui.databinding.ItemRelationListRelationFileBinding
import com.anytypeio.anytype.core_ui.databinding.ItemRelationListRelationObjectBinding
import com.anytypeio.anytype.core_ui.databinding.ItemRelationListRelationStatusBinding
import com.anytypeio.anytype.core_ui.databinding.ItemRelationListRelationTagBinding
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.extensions.setBlockBackgroundColor
import com.anytypeio.anytype.core_ui.widgets.GridCellFileItem
import com.anytypeio.anytype.core_ui.widgets.RelationObjectItem
import com.anytypeio.anytype.core_ui.widgets.text.TagWidget
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.relations.ObjectRelationView
import com.anytypeio.anytype.presentation.sets.model.ObjectView

sealed class ListRelationViewHolder(
    view: View
) : RecyclerView.ViewHolder(view) {

    private val systemIconDrawable = itemView.context.getDrawable(R.drawable.ic_system_relation)

    class Default(binding: ItemRelationListRelationDefaultBinding) :
        ListRelationViewHolder(binding.root) {

        private val tvTitle = binding.content.tvRelationTitle
        private val tvValue = binding.content.tvRelationValue

        fun bind(item: ObjectRelationView) {
            tvTitle.text = item.name
            tvValue.apply {
                if (item.key == Relations.ORIGIN) {
                    val code = item.value?.toInt() ?: -1
                    setText(code.resRelationOrigin())
                } else {
                    text = item.value
                }
                if (item is ObjectRelationView.Default) {
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
            setLockIcon(tvTitle, item.readOnly)
        }
    }

    class Checkbox(binding: ItemRelationListRelationCheckboxBinding) :
        ListRelationViewHolder(binding.root) {

        private val tvTitle = binding.content.tvRelationTitle
        private val ivCheckbox = binding.content.ivRelationCheckbox

        fun bind(item: ObjectRelationView.Checkbox) = with(itemView) {
            tvTitle.text = item.name
            ivCheckbox.isSelected = item.isChecked
            setLockIcon(tvTitle, item.system)
        }
    }

    class Status(binding: ItemRelationListRelationStatusBinding) :
        ListRelationViewHolder(binding.root) {

        private val tvTitle = binding.content.tvRelationTitle
        private val tvValue = binding.content.tvRelationValue

        fun bind(item: ObjectRelationView.Status) {
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
            setLockIcon(tvTitle, item.system)
        }
    }

    class Tags(binding: ItemRelationListRelationTagBinding) : ListRelationViewHolder(binding.root) {

        private val placeholder = binding.content.tvPlaceholder
        private val content = binding.content
        private val tvTitle = binding.content.tvRelationTitle

        fun bind(item: ObjectRelationView.Tags) = with(itemView) {
            tvTitle.text = item.name
            if (item.tags.isEmpty()) {
                placeholder.visible()
            } else {
                placeholder.gone()
            }
            setLockIcon(tvTitle, item.system)
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

        private fun getViewByIndex(index: Int): TagWidget? = when (index) {
            0 -> content.tag0
            1 -> content.tag1
            2 -> content.tag2
            3 -> content.tag3
            4 -> content.tag4
            5 -> content.tag5
            else -> null
        }

        companion object {
            const val MAX_VISIBLE_TAGS_INDEX = 5
        }
    }

    class Object(binding: ItemRelationListRelationObjectBinding) :
        ListRelationViewHolder(binding.root) {

        private val placeholder = binding.content.tvPlaceholder
        private val content = binding.content
        private val tvTitle = binding.content.tvRelationTitle

        fun bind(item: ObjectRelationView.Object) {
            content.tvRelationTitle.text = item.name
            if (item.objects.isEmpty()) {
                placeholder.visible()
            } else {
                placeholder.gone()
            }
            setLockIcon(tvTitle, item.system)
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

        private fun getViewByIndex(index: Int): RelationObjectItem? = when (index) {
            0 -> content.obj0
            1 -> content.obj1
            2 -> content.obj2
            3 -> content.obj3
            else -> null
        }

        companion object {
            const val MAX_VISIBLE_OBJECTS_INDEX = 3
        }
    }

    class File(binding: ItemRelationListRelationFileBinding) :
        ListRelationViewHolder(binding.root) {

        private val placeholder = binding.content.tvPlaceholder
        private val content = binding.content
        private val tvTitle = binding.content.tvRelationTitle

        fun bind(item: ObjectRelationView.File) = with(itemView) {
            tvTitle.text = item.name
            if (item.files.isEmpty()) {
                placeholder.visible()
            } else {
                placeholder.gone()
            }
            setLockIcon(tvTitle, item.system)
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

        companion object {
            const val MAX_VISIBLE_FILES_INDEX = 2
        }
    }

    fun setIsFeatured(isFeatured: Boolean) {
        itemView.findViewById<View>(R.id.featuredRelationCheckbox).apply {
            isSelected = isFeatured
        }
    }

    fun setIsRemovable(isRemovable: Boolean) {
        itemView.findViewById<View>(R.id.ivActionDelete).apply {
            if (isRemovable) visible() else gone()
        }
    }

    fun setBackgroundColor(color: String? = null) {
        itemView.setBlockBackgroundColor(color)
    }

    fun setLockIcon(view: TextView, isReadOnly: Boolean) {
        if (isReadOnly) {
            view.setCompoundDrawablesWithIntrinsicBounds(systemIconDrawable, null, null, null)
            view.visible()
        } else {
            view.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        }
    }
}

fun Int.resRelationOrigin() : Int {
    return when(this) {
        0 -> R.string.relation_origin_none
        1 -> R.string.relation_origin_clipboard
        2 -> R.string.relation_origin_dnd
        3 -> R.string.relation_origin_imported_object
        4 -> R.string.relation_origin_web_clipper
        5 -> R.string.relation_origin_mobile_sharing_extension
        6 -> R.string.relation_origin_use_case
        7 -> R.string.relation_origin_library_installed
        else -> R.string.unknown
    }
}