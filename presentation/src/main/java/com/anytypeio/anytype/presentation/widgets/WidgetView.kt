package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.presentation.editor.model.Indent

sealed class WidgetView {
    data class Tree(
        val id: Id,
        val obj: ObjectWrapper.Basic,
        val elements: List<Element>,
        val isExpanded: Boolean
    ) : WidgetView() {
        data class Element(
            val icon: Icon,
            val indent: Indent,
            val obj: ObjectWrapper.Basic,
            val path: String
        )

        sealed class Icon {
            data class Branch(val isExpanded: Boolean) : Icon()
            object Leaf : Icon()
            object Set : Icon()
        }
    }

    data class Link(
        val id: Id,
        val obj: ObjectWrapper.Basic,
    ) : WidgetView()

    data class Set(
        val id: Id,
        val obj: ObjectWrapper.Basic,
        val tabs: List<Tab>,
        val elements: List<ObjectWrapper.Basic>,
        val isExpanded: Boolean
    ) : WidgetView() {
        data class Tab(
            val id: Id,
            val name: String,
            val isSelected: Boolean
        )
    }

    sealed class Action : WidgetView() {
        object EditWidgets : Action()
        object CreateWidget: Action()
        // Will be deleted. For testing only.
        object Refresh: Action()
    }
}