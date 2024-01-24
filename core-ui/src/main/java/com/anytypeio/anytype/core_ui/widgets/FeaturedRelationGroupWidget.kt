package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.helper.widget.Flow
import androidx.constraintlayout.widget.ConstraintLayout
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
        val ids = mutableListOf<Int>()
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
        item.relations.forEachIndexed { index, relation ->
            val view = createRelationValueListWidget(
                context = context,
                relation = relation,
                isLast = index == item.relations.lastIndex,
                click = click
            )
            addView(view)
            ids.add(view.id)
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

    private fun createRelationValueListWidget(context: Context, relation: ObjectRelationView, isLast: Boolean, click: (ListenerType.Relation) -> Unit): RelationValueListWidget {
        return RelationValueListWidget(context).apply {
            id = generateViewId()
            setRelation(relation, isLast, click)
            val action = determineClickAction(relation, this.id)
            setOnClickListener { if (action != null) click(action) }
        }
    }

    private fun determineClickAction(relation: ObjectRelationView, viewId: Int): ListenerType.Relation? {
        return when (relation) {
            is ObjectRelationView.ObjectType -> {
                objectTypeIds.add(viewId)
                ListenerType.Relation.ObjectType(relation, viewId)
            }
            is ObjectRelationView.Source -> {
                objectTypeIds.add(viewId)
                null
            }
            else -> ListenerType.Relation.Featured(relation)
        }
    }
}