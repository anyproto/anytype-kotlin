package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.extensions.light
import com.anytypeio.anytype.core_utils.ext.gone
import com.anytypeio.anytype.core_utils.ext.setDrawableColor
import com.anytypeio.anytype.core_utils.ext.visible
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

    private val text1: TextView
    private val text2: TextView
    private val icon1: ObjectIconWidget
    private val icon2: ObjectIconWidget
    private val number: TextView
    private val dot: View

    init {
        LayoutInflater.from(context).inflate(
            R.layout.relation_value_list,
            this,
            true
        ) as LinearLayout
        text1 = findViewById(R.id.text1)
        text2 = findViewById(R.id.text2)
        icon1 = findViewById(R.id.icon1)
        icon2 = findViewById(R.id.icon2)
        number = findViewById(R.id.number)
        dot = findViewById(R.id.dot)
    }

    fun setRelation(
        relation: ObjectRelationView,
        isLast: Boolean = false,
        isFirst: Boolean = false
    ) {
        when (relation) {
            is ObjectRelationView.Object -> setObjectRelation(relation, isLast)
            is ObjectRelationView.Tags -> setTagRelation(relation, isLast)
            is ObjectRelationView.File -> TODO()
            is ObjectRelationView.Status -> TODO()
            else -> TODO()
        }
        if (!isLast) dot.visible()
    }

    //region OBJECTS
    private fun setObjectRelation(relation: ObjectRelationView.Object, isFirst: Boolean = false) {
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
                    textView = textView,
                    iconView = iconView,
                    txt = obj.name,
                    objIcon = obj.icon,
                    marginStartWithIcon = defaultObjectMargin,
                    marginStartWithoutIcon = marginStartWithoutIcon
                )
                is ObjectView.Deleted -> setupObject(
                    textView = textView.apply { setTextColor(textColorSecondary) },
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
    private fun setTagRelation(relation: ObjectRelationView.Tags, isLast: Boolean = false) {
        when {
            relation.tags.isEmpty() -> setupSingleTextItem(name = relation.name)
            relation.tags.size > MAX_ITEMS -> setupMultipleTags(relation)
            else -> setupTags(relation)
        }
    }

    private fun setupSingleTextItem(name: String, marginStart: Int = DEFAULT_MARGIN_START) {
        text1.apply {
            updateLayoutParams<LayoutParams> {
                setMarginStart(marginStart)
            }
            setTextColor(textColorSecondary)
            text = name
            visible()
        }
    }

    private fun setupTags(relation: ObjectRelationView.Tags) {
        relation.tags.take(MAX_ITEMS).forEachIndexed { index, tag ->
            val color = ThemeColor.values().find { it.code == tag.color }
            if (index == 0) setupTag(text1, color, tag.tag)
            else setupTag(text2, color, tag.tag, resources.getDimensionPixelSize(R.dimen.dp_6))
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

    companion object {
        private const val MAX_ITEMS = 2
        private const val DEFAULT_MARGIN_START = 0
    }
}