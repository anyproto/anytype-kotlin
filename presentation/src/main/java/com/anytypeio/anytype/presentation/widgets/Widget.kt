package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.core_models.ext.asMap

sealed class Widget {

    abstract val id: Id

    abstract val source: ObjectWrapper.Basic

    /**
     * @property [id] id of the widget
     * @property [source] source for this widget - root object for a tree of objects.
     */
    data class Tree(
        override val id: Id,
        override val source: ObjectWrapper.Basic
    ) : Widget()

    /**
     * @property [id] id of the widget
     * @property [source] source for this widget - one specific object associate with this widget.
     */
    data class Link(
        override val id: Id,
        override val source: ObjectWrapper.Basic
    ) : Widget()

    /**
     * @property [id] id of the widget
     * @property [source] source for this widget - one specific object associate with this widget.
     */
    data class List(
        override val id: Id,
        override val source: ObjectWrapper.Basic
    ) : Widget()
}

fun List<Block>.parseWidgets(
    root: Id,
    details: Map<Id, Struct>
): List<Widget> = buildList {
    val map = asMap()
    val widgets = map[root] ?: emptyList()
    widgets.forEach { w ->
        val content = w.content
        if (content is Block.Content.Widget) {
            val child = (map[w.id] ?: emptyList()).firstOrNull()
            if (child != null) {
                val source = child.content
                if (source is Block.Content.Link) {
                    val raw = details[source.target] ?: emptyMap()
                    val data = ObjectWrapper.Basic(raw)
                    when (content.layout) {
                        Block.Content.Widget.Layout.TREE -> {
                            add(
                                Widget.Tree(
                                    id = w.id,
                                    source = data
                                )
                            )
                        }
                        Block.Content.Widget.Layout.LINK -> {
                            add(
                                Widget.Link(
                                    id = w.id,
                                    source = data
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

typealias WidgetId = Id
typealias ViewId = Id
