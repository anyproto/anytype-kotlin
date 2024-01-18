package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.helper.widget.Flow
import androidx.constraintlayout.widget.ConstraintLayout
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_ui.R
import com.anytypeio.anytype.core_ui.extensions.color
import com.anytypeio.anytype.core_ui.extensions.dark
import com.anytypeio.anytype.core_ui.extensions.drawable
import com.anytypeio.anytype.core_ui.extensions.light
import com.anytypeio.anytype.core_ui.menu.ObjectSetTypePopupMenu
import com.anytypeio.anytype.core_utils.ext.dimen
import com.anytypeio.anytype.core_utils.ext.dp
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

    private val style = R.style.FeaturedRelationTextStyle
    private val themeWrapper = ContextThemeWrapper(context, style)
    private val defaultTextColor = resources.getColor(R.color.text_secondary, null)
    private val itemRightPadding = resources.getDimensionPixelOffset(R.dimen.dp_8)
    private val itemBottomPadding = resources.getDimensionPixelOffset(R.dimen.dp_4)
    private val textColorPrimary = context.getColor(R.color.text_secondary)
    private val textColorSecondary = context.getColor(R.color.text_tertiary)

    private var objectTypeIds = mutableListOf<Int>()

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
//            setHorizontalGap(15)
//            setVerticalGap(15)
        }

        addView(flow)

        val ids = mutableListOf<Int>()

        item.relations.forEachIndexed { index, relation ->
            when (relation) {
                is ObjectRelationView.Default -> {
                    val view = buildTextItem(
                        txt = relation.value ?: relation.name,
                        textColor = getTextColorByValue(relation.value)
                    ).apply {
                        setOnClickListener { click(ListenerType.Relation.Featured(relation)) }
                    }
                    addView(view)
                    ids.add(view.id)
                }
                is ObjectRelationView.Checkbox -> {
                    val view = RelationValueListWidget(context).apply {
                        id = generateViewId()
                        setRelation(relation, index == item.relations.lastIndex)
                        setOnClickListener { click(ListenerType.Relation.Featured(relation)) }
                    }
                    addView(view)
                    ids.add(view.id)
                }
                is ObjectRelationView.File -> {
                    val view = RelationValueListWidget(context).apply {
                        id = generateViewId()
                        setRelation(relation, index == item.relations.lastIndex)
                        setOnClickListener { click(ListenerType.Relation.Featured(relation)) }
                    }
                    addView(view)
                    ids.add(view.id)
                }
                is ObjectRelationView.Object -> {
                    val view = RelationValueListWidget(context).apply {
                        id = generateViewId()
                        setRelation(relation, index == item.relations.lastIndex)
                        setOnClickListener { click(ListenerType.Relation.Featured(relation)) }
                    }
                    addView(view)
                    ids.add(view.id)
                }
                is ObjectRelationView.Status -> {
                    val view = RelationValueListWidget(context).apply {
                        id = generateViewId()
                        setRelation(relation, index == item.relations.lastIndex)
                        setOnClickListener { click(ListenerType.Relation.Featured(relation)) }
                    }
                    addView(view)
                    ids.add(view.id)
                }
                is ObjectRelationView.Tags -> {
                    val view = RelationValueListWidget(context).apply {
                        id = generateViewId()
                        setRelation(relation, index == item.relations.lastIndex)
                        setOnClickListener { click(ListenerType.Relation.Featured(relation)) }
                    }
                    addView(view)
                    ids.add(view.id)
                }
                is ObjectRelationView.ObjectType.Base -> {
                    val view = inflateObjectTypeTextView(
                        name = relation.name,
                        isFirst = index == 0
                    )
                    view.setOnClickListener {
                        click(
                            ListenerType.Relation.ObjectType(
                                typeId = relation.type,
                                typeName = relation.name
                            )
                        )
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
                    val view = TextView(themeWrapper).apply {
                        id = generateViewId()
                        text = context.resources.getString(R.string.deleted_type)
                        isSingleLine = true
                        maxLines = 1
                        ellipsize = TextUtils.TruncateAt.END
                        setPadding(itemRightPadding, 0.dp, itemRightPadding, itemBottomPadding)
                        setTextColor(context.getColor(R.color.palette_dark_red))
                        objectTypeIds.add(id)
                    }
                    view.setOnClickListener {
                        click(ListenerType.Relation.ObjectTypeDeleted)
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

                is ObjectRelationView.Links.From -> {
                    val count = context.resources.getQuantityString(
                        R.plurals.links_from_count,
                        relation.count,
                        relation.count
                    )
                    val view = TextView(themeWrapper).apply {
                        id = generateViewId()
                        text = count
                        isSingleLine = true
                        maxLines = 1
                        ellipsize = TextUtils.TruncateAt.END
                        setPadding(itemRightPadding, 0.dp, itemRightPadding, itemBottomPadding)
                    }
                    view.setOnClickListener {
                        click(ListenerType.Relation.Featured(relation))
                    }
                    addView(view)
                    ids.add(view.id)
                }
                is ObjectRelationView.Links.To -> {
                    val count = context.resources.getQuantityString(
                        R.plurals.links_to_count,
                        relation.count,
                        relation.count
                    )
                    val view = TextView(themeWrapper).apply {
                        id = generateViewId()
                        text = count
                        isSingleLine = true
                        maxLines = 1
                        ellipsize = TextUtils.TruncateAt.END
                        setPadding(itemRightPadding, 0.dp, itemRightPadding, itemBottomPadding)
                    }
                    view.setOnClickListener {
                        click(ListenerType.Relation.Featured(relation))
                    }
                    addView(view)
                    ids.add(view.id)
                }
            }

//            if (index != item.relations.lastIndex) {
//                val div = View(context).apply {
//                    id = View.generateViewId()
//                    layoutParams = LayoutParams(dividerSize, dividerSize)
//                    setBackgroundResource(R.drawable.divider_featured_relations)
//                }
//                addView(div)
//                ids.add(div.id)
//            }
        }

        flow.referencedIds = ids.toIntArray()
    }

    private fun inflateObjectTypeTextView(
        name: String,
        isFirst: Boolean
    ): TextView {
        val textView = TextView(themeWrapper).apply {
            id = View.generateViewId()
            isSingleLine = true
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            setPadding(itemRightPadding, 0.dp, itemRightPadding, itemBottomPadding)
            objectTypeIds.add(id)
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
        val placeholder = buildTextItem(resources.getString(R.string.query)).apply {
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
                        setup(
                            name = resources.getString(
                                R.string.set_by_type,
                                obj.name
                            )
                        )
                    }
                    is ObjectView.Deleted -> {
                        setTextColor(context.color(R.color.glyph_active))
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

    private fun buildTextItem(
        txt: String,
        textColor: Int = context.getColor(R.color.text_secondary)
    ): TextView = TextView(themeWrapper).apply {
        id = generateViewId()
        text = txt
        isSingleLine = true
        setTextColor(textColor)
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
        setPadding(itemRightPadding, 0.dp, itemRightPadding, itemBottomPadding)
    }

    fun clear() {
        removeAllViews()
        objectTypeIds.clear()
    }

    fun getObjectTypeView(): View? {
        return if (objectTypeIds.isNotEmpty()) {
            findViewById(objectTypeIds.first())
        } else {
            null
        }
    }

    private fun getTextColorByValue(value: String?): Int {
        return if (value.isNullOrEmpty()) {
            textColorSecondary
        } else {
            textColorPrimary
        }
    }
}