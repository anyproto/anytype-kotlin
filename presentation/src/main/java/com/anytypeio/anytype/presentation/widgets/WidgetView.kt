package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.presentation.editor.model.Indent
import com.anytypeio.anytype.presentation.objects.ObjectIcon

sealed class WidgetView {
    data class Tree(
        val id: Id,
        val obj: ObjectWrapper.Basic,
        val elements: List<Element> = emptyList(),
        val isExpanded: Boolean = false,
        val isEditable: Boolean = true
    ) : WidgetView() {
        data class Element(
            val elementIcon: ElementIcon,
            val objectIcon: ObjectIcon = ObjectIcon.None,
            val indent: Indent,
            val obj: ObjectWrapper.Basic,
            val path: String
        )

        sealed class ElementIcon {
            data class Branch(val isExpanded: Boolean) : ElementIcon()
            object Leaf : ElementIcon()
            object Set : ElementIcon()
        }
    }

    data class Link(
        val id: Id,
        val obj: ObjectWrapper.Basic,
    ) : WidgetView()

    data class SetOfObjects(
        val id: Id,
        val obj: ObjectWrapper.Basic,
        val tabs: List<Tab>,
        val elements: List<Element>,
        val isExpanded: Boolean
    ) : WidgetView() {
        data class Tab(
            val id: Id,
            val name: String,
            val isSelected: Boolean
        )
        data class Element(
            val icon: ObjectIcon,
            val obj: ObjectWrapper.Basic
        )
    }

    data class ListOfObjects(
        val id: Id,
        val type: Type,
        val elements: List<Element>,
        val isExpanded: Boolean
    ) : WidgetView() {
        data class Element(
            val icon: ObjectIcon,
            val obj: ObjectWrapper.Basic
        )
        sealed class Type {
            object Recent : Type()
            object Favorites : Type()
            object Sets: Type()
        }
    }

    data class Bin(val id: Id) : WidgetView()

    sealed class Action : WidgetView() {
        object EditWidgets : Action()
        object CreateWidget: Action()
        // Will be deleted. For testing only.
        object Refresh: Action()
    }
}