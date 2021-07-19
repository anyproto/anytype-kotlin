package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.helper.widget.Flow
import androidx.constraintlayout.widget.ConstraintLayout
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.setDrawableColor
import com.anytypeio.anytype.presentation.page.editor.ThemeColor
import com.anytypeio.anytype.presentation.page.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.page.editor.model.BlockView
import com.anytypeio.anytype.presentation.relations.DocumentRelationView

class FeaturedRelationGroupWidget : ConstraintLayout {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    private val defaultTextSize : Float = context.dimen(R.dimen.sp_13)
    private val dividerSize : Int = context.dimen(R.dimen.dp_4).toInt()

    fun set(item: BlockView.FeaturedRelation, click: (ListenerType.Relation) -> Unit) {
        clear()

        val flow = Flow(context).apply {
            id = View.generateViewId()
            setWrapMode(Flow.WRAP_CHAIN)
            setHorizontalStyle(Flow.CHAIN_PACKED)
            setHorizontalBias(0f)
            setHorizontalAlign(Flow.HORIZONTAL_ALIGN_START)
            setHorizontalGap(15)
            setVerticalGap(15)
        }

        addView(flow)

        val ids = mutableListOf<Int>()

        item.relations.forEachIndexed { index, relation ->
            when(relation) {
                is DocumentRelationView.Default -> {
                    val view = TextView(context).apply {
                        id = generateViewId()
                        text = relation.value
                        isSingleLine = true
                        maxLines = 1
                        ellipsize = TextUtils.TruncateAt.END
                        setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize)
                    }
                    addView(view)
                    ids.add(view.id)
                }
                is DocumentRelationView.Checkbox -> {
                    val view = View(context).apply {
                        id = generateViewId()
                        val size = context.dimen(R.dimen.dp_16).toInt()
                        layoutParams = LayoutParams(size, size)
                        setBackgroundResource(R.drawable.ic_relation_checkbox_selector)
                        isSelected = relation.isChecked
                    }
                    addView(view)
                    ids.add(view.id)
                }
                is DocumentRelationView.File -> {
                    relation.files.forEach { file ->
                        val view = TextView(context).apply {
                            id = generateViewId()
                            text = file.name
                            isSingleLine = true
                            maxLines = 1
                            ellipsize = TextUtils.TruncateAt.END
                            setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize)
                        }
                        addView(view)
                        ids.add(view.id)
                    }
                }
                is DocumentRelationView.Object -> {
                    relation.objects.forEach { obj ->
                        val view = TextView(context).apply {
                            id = generateViewId()
                            text = obj.name
                            isSingleLine = true
                            maxLines = 1
                            ellipsize = TextUtils.TruncateAt.END
                            setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize)
                        }
                        addView(view)
                        ids.add(view.id)
                    }
                }
                is DocumentRelationView.Status -> {
                    relation.status.forEach { status ->
                        val color = ThemeColor.values().find { v -> v.title == status.color }
                        val view = TextView(context).apply {
                            id = generateViewId()
                            text = status.status
                            isSingleLine = true
                            maxLines = 1
                            ellipsize = TextUtils.TruncateAt.END
                            setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize)
                            if (color != null) {
                                setTextColor(color.text)
                            }
                        }
                        addView(view)
                        ids.add(view.id)
                    }
                }
                is DocumentRelationView.Tags -> {
                    relation.tags.forEach { tag ->
                        val color = ThemeColor.values().find { v -> v.title == tag.color }
                        val view = TextView(context).apply {
                            id = generateViewId()
                            text = tag.tag
                            isSingleLine = true
                            maxLines = 1
                            ellipsize = TextUtils.TruncateAt.END
                            if (color != null) {
                                setTextColor(color.text)
                                setBackgroundResource(R.drawable.rect_dv_cell_tag_item)
                                background.setDrawableColor(color.background)
                            }
                        }
                        addView(view)
                        ids.add(view.id)
                    }
                }
                is DocumentRelationView.ObjectType -> {
                    val view = TextView(context).apply {
                        id = generateViewId()
                        text = relation.name
                        isSingleLine = true
                        maxLines = 1
                        ellipsize = TextUtils.TruncateAt.END
                        setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize)
                    }
                    view.setOnClickListener {
                        click.invoke(ListenerType.Relation.ObjectType(type = relation.relationId))
                    }
                    addView(view)
                    ids.add(view.id)
                }
            }

            if (index != item.relations.lastIndex) {
                val div = View(context).apply {
                    id = View.generateViewId()
                    layoutParams = LayoutParams(dividerSize, dividerSize)
                    setBackgroundResource(R.drawable.divider_featured_relations)
                }
                addView(div)
                ids.add(div.id)
            }
        }

        flow.referencedIds = ids.toIntArray()
    }

    fun clear() {
        removeAllViews()
    }
}