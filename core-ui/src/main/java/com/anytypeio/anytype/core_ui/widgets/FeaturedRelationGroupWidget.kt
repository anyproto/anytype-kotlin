package com.anytypeio.anytype.core_ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.relations.ObjectRelationView
import kotlin.math.max

/**
 * Lays featured relations out in horizontal rows, wrapping to a new row when the
 * available width is exhausted (start-aligned, packed — same visual behavior as the
 * previous ConstraintLayout + Flow implementation).
 *
 * Deliberately a plain [ViewGroup] with hand-rolled measure/layout: the previous
 * ConstraintLayout + Flow-helper version non-deterministically produced a solve in
 * which children stayed unmeasured (0x0) while the widget itself expanded to the
 * full AT_MOST height — freezing the data-view header as a giant blank area after
 * the fragment view was recreated (DROID-4529).
 */
class FeaturedRelationGroupWidget @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    private var objectTypeIds = mutableListOf<Int>()

    fun set(
        item: BlockView.FeaturedRelation,
        click: (ListenerType.Relation) -> Unit
    ) {
        clear()
        item.relations.forEachIndexed { index, relation ->
            val view = createRelationValueListWidget(
                context = context,
                relation = relation,
                isLast = index == item.relations.lastIndex,
                click = click
            )
            addView(view)
        }
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

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val availableWidth =
            (MeasureSpec.getSize(widthMeasureSpec) - paddingStart - paddingEnd).coerceAtLeast(0)
        var rowWidth = 0
        var rowHeight = 0
        var totalHeight = 0
        var maxRowWidth = 0
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility == GONE) continue
            child.measure(
                MeasureSpec.makeMeasureSpec(availableWidth, MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            )
            if (rowWidth > 0 && rowWidth + child.measuredWidth > availableWidth) {
                totalHeight += rowHeight
                maxRowWidth = max(maxRowWidth, rowWidth)
                rowWidth = 0
                rowHeight = 0
            }
            rowWidth += child.measuredWidth
            rowHeight = max(rowHeight, child.measuredHeight)
        }
        totalHeight += rowHeight
        maxRowWidth = max(maxRowWidth, rowWidth)
        setMeasuredDimension(
            resolveSize(maxRowWidth + paddingStart + paddingEnd, widthMeasureSpec),
            resolveSize(totalHeight + paddingTop + paddingBottom, heightMeasureSpec)
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val availableWidth = (r - l - paddingStart - paddingEnd).coerceAtLeast(0)
        var x = paddingStart
        var y = paddingTop
        var rowHeight = 0
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child.visibility == GONE) continue
            if (x > paddingStart && x - paddingStart + child.measuredWidth > availableWidth) {
                x = paddingStart
                y += rowHeight
                rowHeight = 0
            }
            child.layout(x, y, x + child.measuredWidth, y + child.measuredHeight)
            x += child.measuredWidth
            rowHeight = max(rowHeight, child.measuredHeight)
        }
    }

    private fun createRelationValueListWidget(
        context: Context,
        relation: ObjectRelationView,
        isLast: Boolean,
        click: (ListenerType.Relation) -> Unit
    ): RelationValueListWidget {
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
