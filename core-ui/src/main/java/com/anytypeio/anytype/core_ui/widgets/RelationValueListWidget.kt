package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.marginStart
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.databinding.RelationValueListBinding
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.extensions.light
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.setDrawableColor
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.relations.ObjectRelationView
import com.anytypeio.anytype.presentation.sets.model.ObjectView

class RelationValueListWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val textColorPrimary = context.getColor(R.color.text_secondary)
    private val textColorSecondary = context.getColor(R.color.text_tertiary)
    private val defaultItemsMargin = resources.getDimensionPixelSize(R.dimen.dp_6)
    private val defaultObjectMargin = resources.getDimensionPixelSize(R.dimen.dp_4)
    private val defaultBackground = resources.getColor(R.color.shape_primary, null)

    private val MAX_ITEMS = 2
    private val DEFAULT_MARGIN_START = 0

    private val binding: RelationValueListBinding =
        RelationValueListBinding.inflate(LayoutInflater.from(context), this, true)
    private val text1: TextView = binding.text1
    private val text2: TextView = binding.text2
    private val icon1: ObjectIconWidget = binding.icon1
    private val icon2: ObjectIconWidget = binding.icon2
    private val number: TextView = binding.number
    private val dot: View = binding.dot
    private var maxTextWidth: Int = 0

    init {
        maxTextWidth =
            resources.displayMetrics.widthPixels / 2 - resources.getDimensionPixelSize(R.dimen.dp_72)
    }

    fun setRelation(
        relation: ObjectRelationView,
        isLast: Boolean = false,
        clickListener: ((ListenerType.Relation) -> Unit)? = null
    ) {
        when (relation) {
            is ObjectRelationView.Default -> setDefaultRelation(relation)
            is ObjectRelationView.Object -> setObjectRelation(relation)
            is ObjectRelationView.Tags -> setTagRelation(relation)
            is ObjectRelationView.File -> setRelationFile(relation)
            is ObjectRelationView.Status -> setStatusRelation(relation)
            is ObjectRelationView.Checkbox -> setCheckboxRelation(relation)
            is ObjectRelationView.ObjectType -> setObjectTypeRelation(relation)
            is ObjectRelationView.Links -> setLinksRelation(relation)
            is ObjectRelationView.Source -> setSourceRelation(relation, clickListener)
        }
        if (!isLast) dot.visible()
    }

    //region DEFAULT
    private fun setDefaultRelation(relation: ObjectRelationView.Default) {
        setupSingleTextItem(
            name = relation.value ?: relation.name,
            textColor = if (relation.value != null) textColorPrimary else textColorSecondary
        )
    }
    //endregion

    //region OBJECTS
    private fun setObjectRelation(relation: ObjectRelationView.Object) {
        when {
            relation.objects.isEmpty() -> setupSingleTextItem(name = relation.name)
            relation.objects.size > MAX_ITEMS -> setupMultipleObjects(relation)
            else -> setupObjects(relation)
        }
    }

    private fun setupMultipleObjects(relation: ObjectRelationView.Object) {
        setupObjects(relation)
        with(number) {
            text = "+${relation.objects.size - MAX_ITEMS}"
            visible()
        }
    }

    private fun setupObjects(relation: ObjectRelationView.Object) {
        relation.objects.take(MAX_ITEMS).forEachIndexed { index, obj ->
            val (textView, iconView, marginStartWithoutIcon) = when (index) {
                0 -> Triple(text1, icon1, 0)
                else -> Triple(text2, icon2, defaultItemsMargin)
            }
            when (obj) {
                is ObjectView.Default -> setupObject(
                    textView = textView.apply { maxWidth = maxTextWidth },
                    iconView = iconView,
                    txt = obj.name,
                    objIcon = obj.icon,
                    marginStartWithIcon = defaultObjectMargin,
                    marginStartWithoutIcon = marginStartWithoutIcon
                )

                is ObjectView.Deleted -> setupObject(
                    textView = textView.apply {
                        setTextColor(textColorSecondary)
                        maxWidth = maxTextWidth
                    },
                    iconView = iconView,
                    txt = resources.getString(R.string.deleted),
                    objIcon = ObjectIcon.Deleted,
                    marginStartWithIcon = defaultObjectMargin,
                    marginStartWithoutIcon = marginStartWithoutIcon
                )
            }
        }
    }

    private fun setupObject(
        textView: TextView,
        iconView: ObjectIconWidget,
        txt: String,
        objIcon: ObjectIcon,
        marginStartWithIcon: Int,
        marginStartWithoutIcon: Int
    ) {
        val marginStart = when (objIcon) {
            is ObjectIcon.None -> {
                iconView.gone()
                marginStartWithoutIcon
            }

            else -> {
                iconView.visible()
                iconView.setIcon(objIcon)
                marginStartWithIcon
            }
        }
        textView.apply {
            visible()
            text = txt
            updateLayoutParams<LayoutParams> {
                setMarginStart(marginStart)
            }
        }
    }
    //endregion

    //region TAGS
    private fun setTagRelation(relation: ObjectRelationView.Tags) {
        when {
            relation.tags.isEmpty() -> setupSingleTextItem(name = relation.name)
            relation.tags.size > MAX_ITEMS -> setupMultipleTags(relation)
            else -> setupTags(relation)
        }
    }

    private fun setupSingleTextItem(
        name: String,
        marginStart: Int = DEFAULT_MARGIN_START,
        textColor: Int = textColorSecondary,
        click: (() -> Unit)? = null
    ) {
        text1.apply {
            updateLayoutParams<LayoutParams> {
                setMarginStart(marginStart)
            }
            setTextColor(textColor)
            text = name
            visible()
            if (click != null) {
                setOnClickListener { click.invoke() }
            }
        }
    }

    private fun setupTags(relation: ObjectRelationView.Tags) {
        relation.tags.take(MAX_ITEMS).forEachIndexed { index, tag ->
            val color = ThemeColor.values().find { it.code == tag.color }
            if (index == 0) setupTag(text1, color, tag.tag)
            else setupTag(
                textView = text2.apply { maxWidth = maxTextWidth },
                color = color,
                txt = tag.tag,
                marginStart = resources.getDimensionPixelSize(R.dimen.dp_6)
            )
        }
    }

    private fun setupMultipleTags(relation: ObjectRelationView.Tags) {
        setupTags(relation)
        with(number) {
            text = "+${relation.tags.size - MAX_ITEMS}"
            visible()
        }
    }

    private fun setupTag(
        textView: TextView,
        color: ThemeColor?,
        txt: String,
        marginStart: Int = DEFAULT_MARGIN_START
    ) {
        textView.apply {
            visible()
            setTextColor(color?.let { resources.dark(it, textColorPrimary) } ?: textColorPrimary)
            setBackgroundResource(R.drawable.rect_dv_cell_tag_item)
            background.setDrawableColor(color?.let { resources.light(it, defaultBackground) }
                ?: defaultBackground)
            text = txt
            updateLayoutParams<LayoutParams> {
                setMarginStart(marginStart)
            }
        }
    }
    //endregion

    //region STATUS
    private fun setStatusRelation(relation: ObjectRelationView.Status) {
        when {
            relation.status.isEmpty() -> setupSingleTextItem(name = relation.name)
            else -> {
                val color = ThemeColor.values().find { it.code == relation.status[0].color }
                val txt = relation.status[0].status
                setupStatus(text1, color, txt)
            }
        }
    }

    private fun setupStatus(
        textView: TextView,
        color: ThemeColor?,
        txt: String,
        marginStart: Int = DEFAULT_MARGIN_START
    ) {
        textView.apply {
            visible()
            setTextColor(color?.let { resources.dark(it, textColorPrimary) } ?: textColorPrimary)
            text = txt
            updateLayoutParams<LayoutParams> {
                setMarginStart(marginStart)
            }
        }
    }
    //endregion

    //region FILES
    private fun setRelationFile(relation: ObjectRelationView.File) {
        when {
            relation.files.isEmpty() -> setupSingleTextItem(name = relation.name)
            relation.files.size > MAX_ITEMS -> setupMultipleFiles(relation)
            else -> setupFiles(relation)
        }
    }

    private fun setupMultipleFiles(relation: ObjectRelationView.File) {
        setupFiles(relation)
        with(number) {
            text = "+${relation.files.size - MAX_ITEMS}"
            visible()
        }
    }

    private fun setupFiles(relation: ObjectRelationView.File) {
        relation.files.take(MAX_ITEMS).forEachIndexed { index, file ->
            val (textView, iconView) = when (index) {
                0 -> Pair(text1, icon1)
                else -> Pair(text2, icon2)
            }
            setupFile(
                textView = textView.apply { maxWidth = maxTextWidth },
                iconView = iconView,
                txt = "${file.name}.${file.ext}",
                objIcon = file.icon,
                marginStartWithIcon = defaultObjectMargin,
            )
        }
    }

    private fun setupFile(
        textView: TextView,
        iconView: ObjectIconWidget,
        txt: String,
        objIcon: ObjectIcon,
        marginStartWithIcon: Int
    ) {
        iconView.visible()
        iconView.setIcon(objIcon)
        textView.apply {
            visible()
            text = txt
            updateLayoutParams<LayoutParams> {
                marginStart = marginStartWithIcon
            }
        }
    }
    //endregion

    //region OBJECT TYPE
    private fun setObjectTypeRelation(objType: ObjectRelationView.ObjectType) {
        when (objType) {
            is ObjectRelationView.ObjectType.Base -> {
                text1.apply {
                    visible()
                    setTextColor(textColorPrimary)
                    text = objType.name.ifEmpty {
                        context.resources.getString(R.string.untitled)
                    }
                    updateLayoutParams<LayoutParams> {
                        marginStart = 0
                    }
                }
            }

            is ObjectRelationView.ObjectType.Deleted -> {
                text1.apply {
                    visible()
                    setTextColor(context.getColor(R.color.palette_dark_red))
                    text = context.resources.getString(R.string.deleted_type)
                    updateLayoutParams<LayoutParams> {
                        marginStart = 0
                    }
                }
            }
        }
    }
    //endregion

    //region SOURCE
    private fun setSourceRelation(
        objectSource: ObjectRelationView.Source,
        clickListener: ((ListenerType.Relation) -> Unit)? = null
    ) {
        when {
            objectSource.sources.isEmpty() -> {
                setupSingleTextItem(
                    name = resources.getString(R.string.query)
                ) {
                    clickListener?.invoke(ListenerType.Relation.SetQuery())
                }
            }

            else -> {
                val sourceName = objectSource.sources.first().name
                if (objectSource.isSourceByRelation) {
                    setupSingleTextItem(
                        name = resources.getString(R.string.set_by_relation, sourceName),
                        textColor = textColorPrimary
                    ) {
                        val msg = resources.getString(R.string.msg_query_cannot_be_changed)
                        clickListener?.invoke(ListenerType.Relation.ChangeQueryByRelation(msg))
                    }
                } else {
                    setupSingleTextItem(
                        name = resources.getString(R.string.set_by_type, sourceName),
                        textColor = textColorPrimary
                    ) {
                        clickListener?.invoke(ListenerType.Relation.SetQuery(objectSource.sources))
                    }
                }
            }
        }

    }
    //endregion

    //region CHECKBOX
    private fun setCheckboxRelation(relation: ObjectRelationView.Checkbox) {
        icon1.visible()
        val icon = if (relation.isChecked) ObjectIcon.Checkbox(true) else ObjectIcon.Checkbox(false)
        icon1.setIcon(icon = icon)
        setupSingleTextItem(name = relation.name, marginStart = defaultObjectMargin)
    }
    //endregion

    //region LINKS
    private fun setLinksRelation(relation: ObjectRelationView.Links) {
        when (relation) {
            is ObjectRelationView.Links.From -> {
                val count = context.resources.getQuantityString(
                    R.plurals.links_from_count,
                    relation.count,
                    relation.count
                )
                setupSingleTextItem(
                    name = count,
                    textColor = textColorPrimary
                )
            }

            is ObjectRelationView.Links.To -> {
                val count = context.resources.getQuantityString(
                    R.plurals.links_to_count,
                    relation.count,
                    relation.count
                )
                setupSingleTextItem(
                    name = count,
                    textColor = textColorPrimary
                )
            }
        }
    }
    //endregion
}