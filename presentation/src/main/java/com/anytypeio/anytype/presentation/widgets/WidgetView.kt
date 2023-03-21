package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.presentation.editor.model.Indent
import com.anytypeio.anytype.presentation.objects.ObjectIcon

sealed class WidgetView {

    abstract val id: Id

    data class Tree(
        override val id: Id,
        val source: Widget.Source,
        val elements: List<Element> = emptyList(),
        val isExpanded: Boolean = false,
        val isEditable: Boolean = true
    ) : WidgetView(), Draggable {
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
        override val id: Id,
        val source: Widget.Source,
    ) : WidgetView(), Draggable

    data class SetOfObjects(
        override val id: Id,
        val source: Widget.Source,
        val tabs: List<Tab>,
        val elements: List<Element>,
        val isExpanded: Boolean
    ) : WidgetView(), Draggable {
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
        override val id: Id,
        val source: Widget.Source,
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
            object Collections: Type()
        }
    }

    data class Bin(override val id: Id) : WidgetView()

    sealed class Action : WidgetView() {
        object EditWidgets : Action() {
            override val id: Id get() = "id.action.edit-widgets"
        }
        // Will be deleted. For testing only.
        object Library : Action() {
            override val id: Id get() = "id.action.library"
        }
    }

    interface Draggable
}

sealed class DropDownMenuAction {
    object ChangeWidgetType : DropDownMenuAction()
    object ChangeWidgetSource : DropDownMenuAction()
    object RemoveWidget : DropDownMenuAction()
    object EditWidgets : DropDownMenuAction()
    object EmptyBin: DropDownMenuAction()
}

fun ObjectWrapper.Basic.getWidgetObjectName(): String? {
    return if (layout == ObjectType.Layout.NOTE) {
        snippet?.trim()?.ifEmpty { null }
    } else {
        name?.trim()?.ifEmpty { null }
    }
}