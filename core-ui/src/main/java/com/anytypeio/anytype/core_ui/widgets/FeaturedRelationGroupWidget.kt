package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.helper.widget.Flow
import androidx.constraintlayout.widget.ConstraintLayout
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.extensions.light
import com.anytypeio.anytype.core_ui.menu.ObjectTypePopupMenu
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.setDrawableColor
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.relations.DocumentRelationView
import com.anytypeio.anytype.presentation.sets.model.ObjectView

class FeaturedRelationGroupWidget : ConstraintLayout {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    private val defaultTextSize: Float = context.dimen(R.dimen.sp_13)
    private val dividerSize: Int = context.dimen(R.dimen.dp_4).toInt()
    private val defaultTextColor = resources.getColor(R.color.text_secondary, null)

    fun set(
        item: BlockView.FeaturedRelation,
        click: (ListenerType.Relation) -> Unit,
        isObjectSet: Boolean = false
    ) {
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
            when (relation) {
                is DocumentRelationView.Default -> {
                    val view = TextView(context).apply {
                        id = generateViewId()
                        text = relation.value ?: getPlaceholderHint(relation)
                        isSingleLine = true
                        maxLines = 1
                        ellipsize = TextUtils.TruncateAt.END
                        setTextColor(defaultTextColor)
                        setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize)
                        setOnClickListener { click(ListenerType.Relation.Featured(relation)) }
                    }
                    if (relation.value == null) {
                        view.alpha = 0.5f
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
                        setOnClickListener { click(ListenerType.Relation.Featured(relation)) }
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
                            setTextColor(defaultTextColor)
                            setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize)
                            setOnClickListener { click(ListenerType.Relation.Featured(relation)) }
                        }
                        addView(view)
                        ids.add(view.id)
                    }
                    if (relation.files.isEmpty()) {
                        val placeholder =
                            buildPlaceholderView(resources.getString(R.string.select_files)).apply {
                                setOnClickListener { click(ListenerType.Relation.Featured(relation)) }
                            }
                        addView(placeholder)
                        ids.add(placeholder.id)
                    }
                }
                is DocumentRelationView.Object -> {
                    relation.objects.forEach { obj ->
                        if (obj is ObjectView.Default) {
                            val view = TextView(context).apply {
                                id = generateViewId()
                                text = obj.name
                                isSingleLine = true
                                maxLines = 1
                                ellipsize = TextUtils.TruncateAt.END
                                setTextColor(defaultTextColor)
                                setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize)
                                setOnClickListener { click(ListenerType.Relation.Featured(relation)) }
                            }
                            addView(view)
                            ids.add(view.id)
                        } else {
                            val view = TextView(context).apply {
                                id = generateViewId()
                                setText(R.string.deleted)
                                isSingleLine = true
                                maxLines = 1
                                ellipsize = TextUtils.TruncateAt.END
                                setTextColor(defaultTextColor)
                                setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize)
                                setOnClickListener { click(ListenerType.Relation.Featured(relation)) }
                            }
                            addView(view)
                            ids.add(view.id)
                        }
                    }
                    if (relation.objects.isEmpty()) {
                        val placeholder =
                            buildPlaceholderView(resources.getString(R.string.select_objects)).apply {
                                setOnClickListener { click(ListenerType.Relation.Featured(relation)) }
                            }
                        addView(placeholder)
                        ids.add(placeholder.id)
                    }
                }
                is DocumentRelationView.Status -> {
                    relation.status.forEach { status ->
                        val color = ThemeColor.values().find { v -> v.code == status.color }
                        val view = TextView(context).apply {
                            id = generateViewId()
                            text = status.status
                            isSingleLine = true
                            maxLines = 1
                            ellipsize = TextUtils.TruncateAt.END
                            setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize)
                            if (color != null) {
                                setTextColor(resources.dark(color, defaultTextColor))
                            } else {
                                setTextColor(defaultTextColor)
                            }
                            setOnClickListener { click(ListenerType.Relation.Featured(relation)) }
                        }
                        addView(view)
                        ids.add(view.id)
                    }
                    if (relation.status.isEmpty()) {
                        val placeholder =
                            buildPlaceholderView(resources.getString(R.string.select_status)).apply {
                                setOnClickListener { click(ListenerType.Relation.Featured(relation)) }
                            }
                        addView(placeholder)
                        ids.add(placeholder.id)
                    }
                }
                is DocumentRelationView.Tags -> {
                    relation.tags.forEach { tag ->
                        val color = ThemeColor.values().find { v -> v.code == tag.color }
                        val defaultBackground = resources.getColor(R.color.shape_primary, null)
                        val view = TextView(context).apply {
                            id = generateViewId()
                            text = tag.tag
                            isSingleLine = true
                            maxLines = 1
                            ellipsize = TextUtils.TruncateAt.END
                            if (color != null) {
                                setTextColor(resources.dark(color, defaultTextColor))
                                setBackgroundResource(R.drawable.rect_dv_cell_tag_item)
                                background.setDrawableColor(
                                    resources.light(
                                        color,
                                        defaultBackground
                                    )
                                )
                            } else {
                                setTextColor(defaultTextColor)
                                setBackgroundResource(R.drawable.rect_dv_cell_tag_item)
                                background.setDrawableColor(defaultBackground)
                            }
                            setOnClickListener { click(ListenerType.Relation.Featured(relation)) }
                        }
                        addView(view)
                        ids.add(view.id)
                    }
                    if (relation.tags.isEmpty()) {
                        val placeholder =
                            buildPlaceholderView(resources.getString(R.string.select_tags)).apply {
                                setOnClickListener { click(ListenerType.Relation.Featured(relation)) }
                            }
                        addView(placeholder)
                        ids.add(placeholder.id)
                    }
                }
                is DocumentRelationView.ObjectType -> {
                    val view = TextView(context).apply {
                        id = generateViewId()
                        hint = context.resources.getString(R.string.untitled)
                        text = relation.name
                        isSingleLine = true
                        maxLines = 1
                        ellipsize = TextUtils.TruncateAt.END
                        setTextColor(defaultTextColor)
                        setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize)
                    }
                    view.setOnClickListener {
                        if (!isObjectSet) {
                            val popup = ObjectTypePopupMenu(
                                context = context,
                                view = it,
                                onChangeTypeClicked = {
                                    click(ListenerType.Relation.ChangeObjectType(type = relation.relationId))
                                },
                                onOpenSetClicked = {
                                    click(ListenerType.Relation.ObjectTypeOpenSet(type = relation.type))
                                },
                                allowChangingObjectType = item.allowChangingObjectType
                            )
                            popup.show()
                        }
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

    private fun getPlaceholderHint(relation: DocumentRelationView.Default): String {
        return when (relation.format) {
            Relation.Format.SHORT_TEXT -> resources.getString(R.string.enter_text)
            Relation.Format.LONG_TEXT -> resources.getString(R.string.enter_text)
            Relation.Format.NUMBER -> resources.getString(R.string.enter_number)
            Relation.Format.DATE -> resources.getString(R.string.enter_date)
            Relation.Format.URL -> resources.getString(R.string.enter_url)
            Relation.Format.EMAIL -> resources.getString(R.string.enter_email)
            Relation.Format.PHONE -> resources.getString(R.string.enter_phone)
            else -> resources.getString(R.string.enter_value)
        }
    }

    private fun buildPlaceholderView(txt: String): TextView = TextView(context).apply {
        id = generateViewId()
        text = txt
        isSingleLine = true
        alpha = 0.5f
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
        setTextColor(defaultTextColor)
        setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize)
    }

    fun clear() {
        removeAllViews()
    }
}