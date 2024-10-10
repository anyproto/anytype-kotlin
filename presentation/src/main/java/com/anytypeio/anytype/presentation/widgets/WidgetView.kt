package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.SHARED_SPACE_TYPE
import com.anytypeio.anytype.core_models.SpaceType
import com.anytypeio.anytype.presentation.editor.cover.CoverView
import com.anytypeio.anytype.presentation.editor.model.Indent
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.spaces.SpaceIconView

sealed class WidgetView {

    interface Element {
        val objectIcon: ObjectIcon
        val obj: ObjectWrapper.Basic
    }

    abstract val id: Id
    abstract val isLoading: Boolean

    data class Tree(
        override val id: Id,
        override val isLoading: Boolean = false,
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
            data object Leaf : ElementIcon()
            data object Set : ElementIcon()
            data object Collection: ElementIcon()
        }
    }

    data class Link(
        override val id: Id,
        override val isLoading: Boolean = false,
        val source: Widget.Source,
    ) : WidgetView(), Draggable

    data class SetOfObjects(
        override val id: Id,
        override val isLoading: Boolean = false,
        val source: Widget.Source,
        val tabs: List<Tab>,
        val elements: List<Element>,
        val isExpanded: Boolean,
        val isCompact: Boolean = false
    ) : WidgetView(), Draggable {
        data class Tab(
            val id: Id,
            val name: String,
            val isSelected: Boolean
        )
        data class Element(
            override val objectIcon: ObjectIcon,
            override val obj: ObjectWrapper.Basic,
            val cover: CoverView? = null
        ) : WidgetView.Element
    }

    data class Gallery(
        override val id: Id,
        override val isLoading: Boolean = false,
        val view: Id? = null,
        val source: Widget.Source,
        val tabs: List<SetOfObjects.Tab>,
        val elements: List<SetOfObjects.Element>,
        val isExpanded: Boolean,
        val showIcon: Boolean = false,
        val showCover: Boolean = false
    ) : WidgetView(), Draggable

    data class ListOfObjects(
        override val id: Id,
        override val isLoading: Boolean = false,
        val source: Widget.Source,
        val type: Type,
        val elements: List<Element>,
        val isExpanded: Boolean,
        val isCompact: Boolean = false
    ) : WidgetView(), Draggable {
        data class Element(
            override val objectIcon: ObjectIcon,
            override val obj: ObjectWrapper.Basic
        ) : WidgetView.Element
        sealed class Type {
            data object Recent : Type()
            data object RecentLocal : Type()
            data object Favorites : Type()
            data object Sets: Type()
            data object Collections: Type()
        }
    }

    data class Bin(override val id: Id) : WidgetView() {
        override val isLoading: Boolean = false
    }

    data object AllContent: WidgetView() {
        const val ALL_CONTENT_WIDGET_ID = "bundled-widget.all-content"
        override val isLoading: Boolean = false
        override val id: Id = ALL_CONTENT_WIDGET_ID
    }

    sealed class SpaceWidget: WidgetView() {
        override val id: Id get() = SpaceWidgetContainer.SPACE_WIDGET_SUBSCRIPTION
        data class View(
            val space: ObjectWrapper.SpaceView,
            val icon: SpaceIconView,
            val type: SpaceType,
            val membersCount: Int
        ) : SpaceWidget() {
            val isShared: Boolean get() = type == SHARED_SPACE_TYPE
            override val isLoading: Boolean = false
        }
    }

    data object Library : WidgetView() {
        override val id: Id get() = "id.button.library"
        override val isLoading: Boolean = false
    }

    sealed class Action : WidgetView() {
        data object EditWidgets : Action() {
            override val id: Id get() = "id.action.edit-widgets"
            override val isLoading: Boolean = false
        }
    }

    interface Draggable
}

sealed class DropDownMenuAction {
    data object ChangeWidgetType : DropDownMenuAction()
    data object ChangeWidgetSource : DropDownMenuAction()
    data object RemoveWidget : DropDownMenuAction()
    data object AddBelow: DropDownMenuAction()
    data object EditWidgets : DropDownMenuAction()
    data object EmptyBin: DropDownMenuAction()
}

fun ObjectWrapper.Basic.getWidgetObjectName(): String? {
    return if (layout == ObjectType.Layout.NOTE) {
        snippet?.trim()?.ifEmpty { null }
    } else {
        name?.trim()?.ifEmpty { null }
    }
}