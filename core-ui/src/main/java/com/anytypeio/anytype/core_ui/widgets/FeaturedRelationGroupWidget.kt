package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.helper.widget.Flow
import androidx.constraintlayout.widget.ConstraintLayout
import com.anytypeio.anytype.core_ui.menu.ObjectSetTypePopupMenu
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.relations.ObjectRelationView

class FeaturedRelationGroupWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var objectTypeIds = mutableListOf<Int>()

    fun set(
        item: BlockView.FeaturedRelation,
        click: (ListenerType.Relation) -> Unit
    ) {
        clear()

        val flow = Flow(context).apply {
            id = View.generateViewId()
            setOrientation(Flow.HORIZONTAL)
            setWrapMode(Flow.WRAP_CHAIN)
            setHorizontalStyle(Flow.CHAIN_PACKED)
            setHorizontalBias(0f)
            setHorizontalAlign(Flow.HORIZONTAL_ALIGN_START)
        }

        addView(flow)

        val ids = mutableListOf<Int>()

        item.relations.forEachIndexed { index, relation ->
            when (relation) {
                is ObjectRelationView.Default -> {
                    val view = RelationValueListWidget(context).apply {
                        id = generateViewId()
                        setRelation(relation, index == item.relations.lastIndex)
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
                is ObjectRelationView.Source -> {
                    val view = RelationValueListWidget(context).apply {
                        id = generateViewId()
                        setRelation(
                            relation = relation,
                            isLast = index == item.relations.lastIndex,
                            clickListener = click
                        )
                    }
                    addView(view)
                    ids.add(view.id)
                }
                is ObjectRelationView.ObjectType.Base -> {
                    val view = RelationValueListWidget(context).apply {
                        id = generateViewId()
                        setRelation(
                            relation = relation,
                            isLast = index == item.relations.lastIndex
                        )
                        setOnClickListener {
                            click(
                                ListenerType.Relation.ObjectType(
                                    typeId = relation.type,
                                    typeName = relation.name
                                )
                            )
                        }
                    }
                    addView(view)
                    ids.add(view.id)
                    objectTypeIds.add(view.id)
                }
                is ObjectRelationView.ObjectType.Deleted -> {
                    val view = RelationValueListWidget(context).apply {
                        id = generateViewId()
                        setRelation(
                            relation = relation,
                            isLast = index == item.relations.lastIndex
                        )
                        setOnClickListener {
                            click(ListenerType.Relation.ObjectTypeDeleted)
                        }
                    }
                    addView(view)
                    ids.add(view.id)
                    objectTypeIds.add(view.id)
                }
                is ObjectRelationView.ObjectType.Collection -> {
                    val view = RelationValueListWidget(context).apply {
                        id = generateViewId()
                        setRelation(relation, index == item.relations.lastIndex)
                    }
                    addView(view)
                    ids.add(view.id)
                }
                is ObjectRelationView.ObjectType.Set -> {
                    val view = RelationValueListWidget(context).apply {
                        id = generateViewId()
                        setRelation(relation, index == item.relations.lastIndex)
                        setOnClickListener {
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
                            popup.setOnDismissListener { background = null }
                            popup.show()
                        }
                    }
                    addView(view)
                    ids.add(view.id)
                }
                is ObjectRelationView.Links -> {
                    val view = RelationValueListWidget(context).apply {
                        id = generateViewId()
                        setRelation(relation, index == item.relations.lastIndex)
                        setOnClickListener { click(ListenerType.Relation.Featured(relation)) }
                    }
                    addView(view)
                    ids.add(view.id)
                }
            }
        }

        flow.referencedIds = ids.toIntArray()
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
}