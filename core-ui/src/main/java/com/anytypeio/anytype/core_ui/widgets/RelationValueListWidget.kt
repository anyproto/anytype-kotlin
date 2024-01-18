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
import com.anytypeio.anytype.core_utils.ext.setDrawableColor
import com.anytypeio.anytype.core_utils.ext.visible
import com.anytypeio.anytype.presentation.relations.ObjectRelationView

class RelationValueListWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {
    
    private val textColorPrimary = context.getColor(R.color.text_secondary)
    private val textColorSecondary = context.getColor(R.color.text_tertiary)
    private val defaultBackground = resources.getColor(R.color.shape_primary, null)

    private val text1: TextView
    private val text2: TextView
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
        number = findViewById(R.id.number)
        dot = findViewById(R.id.dot)
    }

    //region TAGS
    fun setTagRelation(relation: ObjectRelationView.Tags, isLast: Boolean = false) {
        when {
            relation.tags.isEmpty() -> setupSingleTag(name = relation.name)
            relation.tags.size > MAX_TAGS -> setupMultipleTags(relation)
            else -> setupTags(relation)
        }
        if (!isLast) dot.visible()
    }

    private fun setupSingleTag(name: String, marginStart: Int = DEFAULT_MARGIN_START) {
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
        relation.tags.take(MAX_TAGS).forEachIndexed { index, tag ->
            val color = ThemeColor.values().find { it.code == tag.color }
            if (index == 0) setupTag(text1, color, tag.tag)
            else setupTag(text2, color, tag.tag, resources.getDimensionPixelSize(R.dimen.dp_6))
        }
    }

    private fun setupMultipleTags(relation: ObjectRelationView.Tags) {
        setupTags(relation)
        with(number) {
            text = "+${relation.tags.size - MAX_TAGS}"
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
        private const val MAX_TAGS = 2
        private const val DEFAULT_MARGIN_START = 0
    }
}