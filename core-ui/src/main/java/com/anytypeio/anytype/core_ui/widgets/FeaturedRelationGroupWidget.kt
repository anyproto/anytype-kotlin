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
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.extensions.drawable
import com.anytypeio.anytype.core_ui.extensions.light
import com.anytypeio.anytype.core_ui.menu.ObjectSetTypePopupMenu
import com.anytypeio.anytype.core_ui.menu.ObjectTypePopupMenu
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.px
import com.anytypeio.anytype.core_utils.ext.setDrawableColor
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.relations.ObjectRelationView
import com.anytypeio.anytype.presentation.sets.model.ObjectView

class FeaturedRelationGroupWidget : ConstraintLayout {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    private val defaultTextSize: Float = context.dimen(R.dimen.sp_15)
    private val dividerSize: Int = context.dimen(R.dimen.dp_4).toInt()
    private val defaultTextColor = resources.getColor(R.color.text_secondary, null)

    fun set(
        item: BlockView.FeaturedRelation,
        click: (ListenerType.Relation) -> Unit
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
                is ObjectRelationView.Default -> {
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
                is ObjectRelationView.Checkbox -> {
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
                is ObjectRelationView.File -> {
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
                is ObjectRelationView.Object -> {
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
                is ObjectRelationView.Status -> {
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
                is ObjectRelationView.Tags -> {
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
                is ObjectRelationView.ObjectType.Base -> {
                    val view = inflateObjectTypeTextView(
                        name = relation.name,
                        isFirst = index == 0
                    )
                    view.setOnClickListener {
                        val popup = ObjectTypePopupMenu(
                            context = context,
                            view = it,
                            onChangeTypeClicked = {
                                click(ListenerType.Relation.ChangeObjectType(type = relation.id))
                            },
                            onOpenSetClicked = {
                                click(ListenerType.Relation.ObjectTypeOpenSet(type = relation.type))
                            },
                            allowChangingObjectType = item.allowChangingObjectType
                        )
                        popup.show()
                    }
                    addView(view)
                    ids.add(view.id)
                }
                is ObjectRelationView.Source.Base -> {
                    if (relation.sources.isEmpty()) {
                        inflateEmptySourcePlaceholderTextView(
                            click = click,
                            ids = ids
                        )
                    } else {
                        if (relation.isSourceByRelation) {
                            inflateSourceByRelationTextView(
                                relation = relation,
                                click = click,
                                ids = ids
                            )
                        } else {
                            inflateDefaultSourceTextView(
                                relation = relation,
                                click = click,
                                ids = ids
                            )
                        }
                    }
                }
                is ObjectRelationView.Source.Deleted -> {
                    inflateDeletedSourceTextView(
                        click = click,
                        ids = ids
                    )
                }
                is ObjectRelationView.ObjectType.Deleted -> {
                    val view = TextView(context).apply {
                        id = generateViewId()
                        text = context.resources.getString(R.string.deleted_type)
                        isSingleLine = true
                        maxLines = 1
                        ellipsize = TextUtils.TruncateAt.END
                        setTextColor(context.getColor(R.color.palette_dark_red))
                        setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize)
                    }
                    view.setOnClickListener {
                        val popup = ObjectTypePopupMenu(
                            context = context,
                            view = it,
                            onChangeTypeClicked = {
                                click(ListenerType.Relation.ChangeObjectType(type = relation.id))
                            },
                            onOpenSetClicked = {},
                            allowOnlyChangingType = true
                        )
                        popup.show()
                    }
                    addView(view)
                    ids.add(view.id)
                }
                is ObjectRelationView.ObjectType.Collection -> {
                    val view = inflateObjectTypeTextView(
                        name = relation.name,
                        isFirst = index == 0
                    )
                    addView(view)
                    ids.add(view.id)
                }
                is ObjectRelationView.ObjectType.Set -> {
                    val view = inflateObjectTypeTextView(
                        name = relation.name,
                        isFirst = index == 0
                    )
                    view.setOnClickListener {
                        val popup = ObjectSetTypePopupMenu(
                            context = context,
                            view = it,
                            onChangeTypeClicked = {
                                click(ListenerType.Relation.SetQuery(queries = emptyList()))
                            },
                            onConvertToCollection = {
                                click(ListenerType.Relation.TurnIntoCollection)
                            }
                        )
                        popup.setOnDismissListener { view.background = null }
                        view.background = context.drawable(R.drawable.bg_featured_relation)
                        popup.show()
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

    private fun inflateObjectTypeTextView(
        name: String,
        isFirst: Boolean
    ): TextView {
        val textView = TextView(context).apply {
            id = View.generateViewId()
            isSingleLine = true
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            setPadding(if (isFirst) 0.px else 4.px, 2.px, 4.px, 2.px)
            setTextColor(defaultTextColor)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, defaultTextSize)
        }
        if (name.isEmpty()) {
            textView.hint = context.resources.getString(R.string.untitled)
        } else {
            textView.text = name
        }
        return textView
    }

    private fun inflateEmptySourcePlaceholderTextView(
        click: (ListenerType.Relation) -> Unit,
        ids: MutableList<Int>
    ) {
        val placeholder = buildPlaceholderView(resources.getString(R.string.query)).apply {
                setOnClickListener {
                    click(
                        ListenerType.Relation.SetQuery(queries = emptyList())
                    )
                }
        }
        addView(placeholder)
        ids.add(placeholder.id)
    }

    private fun inflateDefaultSourceTextView(
        relation: ObjectRelationView.Source.Base,
        click: (ListenerType.Relation) -> Unit,
        ids: MutableList<Int>
    ) {
        relation.sources.forEach { obj ->
            val view = ObjectIconTextWidget(context).apply {
                id = generateViewId()
                when (obj) {
                    is ObjectView.Default -> {
                        setTextColor(context.color(R.color.text_secondary))
                        setTextSize(context.dimen(R.dimen.featured_relations_text_size))
                        setup(
                            name = resources.getString(
                                R.string.set_by_type,
                                obj.name
                            )
                        )
                    }
                    is ObjectView.Deleted -> {
                        setTextColor(context.color(R.color.glyph_active))
                        setTextSize(context.dimen(R.dimen.featured_relations_text_size))
                        setup(
                            name = context.getString(R.string.deleted),
                            icon = ObjectIcon.None
                        )
                    }
                }
            }
            view.setOnClickListener {
                click(
                    ListenerType.Relation.SetQuery(queries = relation.sources)
                )
            }
            addView(view)
            ids.add(view.id)
        }
    }

    private fun inflateSourceByRelationTextView(
        relation: ObjectRelationView.Source.Base,
        click: (ListenerType.Relation) -> Unit,
        ids: MutableList<Int>
    ) {
        val names = relation.sources.mapNotNull { s ->
            if (s is ObjectView.Default) s.name else null
        }
        val view = ObjectIconTextWidget(context).apply {
            id = generateViewId()
            setTextColor(context.color(R.color.text_secondary))
            setTextSize(context.dimen(R.dimen.featured_relations_text_size))
            setup(
                name = if (names.size == 1) {
                    resources.getString(
                        R.string.set_by_relation,
                        names.first()
                    )
                } else {
                    resources.getString(
                        R.string.set_by_relations,
                        names
                    )
                },
                icon = ObjectIcon.None
            )
        }
        view.setOnClickListener {
            click(ListenerType.Relation.ChangeQueryByRelation)
        }
        addView(view)
        ids.add(view.id)
    }

    private fun inflateDeletedSourceTextView(
        click: (ListenerType.Relation) -> Unit,
        ids: MutableList<Int>
    ) {
            val view = ObjectIconTextWidget(context).apply {
                id = generateViewId()
                setTextColor(context.color(R.color.palette_dark_red))
                setTextSize(context.dimen(R.dimen.featured_relations_text_size))
                setup(
                    name = context.getString(R.string.deleted_type_in_set),
                    icon = ObjectIcon.None
                )
            }
            view.setOnClickListener {
                click(
                    ListenerType.Relation.SetQuery(queries = listOf())
                )
            }
            addView(view)
            ids.add(view.id)
    }

    private fun getPlaceholderHint(relation: ObjectRelationView.Default): String {
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