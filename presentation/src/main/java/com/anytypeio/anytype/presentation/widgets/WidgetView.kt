package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.presentation.editor.cover.CoverView
import com.anytypeio.anytype.presentation.editor.model.Indent
import com.anytypeio.anytype.presentation.objects.ObjectIcon

sealed class WidgetView {

    sealed interface Name {
        data class Bundled(val source: Widget.Source.Bundled) : Name
        data class Default(val prettyPrintName: String) : Name
        data object Empty : Name
    }

    interface Element {
        val objectIcon: ObjectIcon
        val obj: ObjectWrapper.Basic
        val name: Name
    }

    abstract val id: Id
    abstract val canCreateObjectOfType: Boolean
    abstract val sectionType: SectionType?

    data class Tree(
        override val id: Id,
        val name: Name,
        val icon: ObjectIcon = ObjectIcon.None,
        val source: Widget.Source,
        val elements: List<Element> = emptyList(),
        val isExpanded: Boolean = false,
        val isEditable: Boolean = true,
        val hasMore: Boolean = false,
        override val sectionType: SectionType? = null
    ) : WidgetView(), Draggable {

        override val canCreateObjectOfType: Boolean
            get() = false

        /**
         * @property [obj] is deprecated
         */
        data class Element(
            val id: Id,
            val obj: ObjectWrapper.Basic,
            val elementIcon: ElementIcon,
            val objectIcon: ObjectIcon = ObjectIcon.None,
            val indent: Indent,
            val path: String,
            val name: Name
        )

        sealed class ElementIcon {
            data class Branch(val isExpanded: Boolean) : ElementIcon()
            data object Leaf : ElementIcon()
            data object Set : ElementIcon()
            data object Collection : ElementIcon()
        }
    }

    data class Link(
        override val id: Id,
        val icon: ObjectIcon = ObjectIcon.None,
        val name: Name,
        val source: Widget.Source,
        override val sectionType: SectionType? = null
    ) : WidgetView(), Draggable {
        override val canCreateObjectOfType: Boolean
            get() = source.canCreateObjectOfType()
    }

    data class SetOfObjects(
        override val id: Id,
        val icon: ObjectIcon = ObjectIcon.None,
        val source: Widget.Source,
        val tabs: List<Tab>,
        val elements: List<Element>,
        val isExpanded: Boolean,
        val isCompact: Boolean = false,
        val name: Name,
        val hasMore: Boolean = false,
        override val sectionType: SectionType? = null
    ) : WidgetView(), Draggable {

        override val canCreateObjectOfType: Boolean
            get() = source.canCreateObjectOfType()

        data class Tab(
            val id: Id,
            val name: String,
            val isSelected: Boolean
        )

        data class Element(
            override val objectIcon: ObjectIcon,
            override val obj: ObjectWrapper.Basic,
            override val name: Name,
            val cover: CoverView? = null
        ) : WidgetView.Element
    }

    data class Gallery(
        override val id: Id,
        val icon: ObjectIcon,
        val view: Id? = null,
        val name: Name,
        val source: Widget.Source,
        val tabs: List<SetOfObjects.Tab>,
        val elements: List<SetOfObjects.Element>,
        val isExpanded: Boolean,
        val showIcon: Boolean = false,
        val showCover: Boolean = false,
        val hasMore: Boolean = false,
        override val sectionType: SectionType? = null
    ) : WidgetView(), Draggable {

        override val canCreateObjectOfType: Boolean
            get() = source.canCreateObjectOfType()
    }

    data class ListOfObjects(
        override val id: Id,
        val icon: ObjectIcon,
        val source: Widget.Source,
        val type: Type,
        val elements: List<Element>,
        val isExpanded: Boolean,
        val isCompact: Boolean = false,
        val hasMore: Boolean = false,
        override val sectionType: SectionType? = null
    ) : WidgetView(), Draggable {

        override val canCreateObjectOfType: Boolean
            get() = source.canCreateObjectOfType()

        data class Element(
            override val objectIcon: ObjectIcon,
            override val obj: ObjectWrapper.Basic,
            override val name: Name
        ) : WidgetView.Element

        sealed class Type {
            data object Recent : Type()
            data object RecentLocal : Type()
            data object Favorites : Type()
            data object Bin : Type()
        }
    }

    data class Bin(
        override val id: Id,
        override val canCreateObjectOfType: Boolean = false,
        val source: Widget.Source,
        val isEmpty: Boolean = false,
        override val sectionType: SectionType? = null
    ) : WidgetView()

    data class AllContent(
        override val id: Id,
        override val canCreateObjectOfType: Boolean = false,
        override val sectionType: SectionType? = null
    ) : WidgetView()

    data class SpaceChat(
        override val id: Id,
        val source: Widget.Source,
        val unreadMessageCount: Int = 0,
        val unreadMentionCount: Int = 0,
        override val canCreateObjectOfType: Boolean = false,
        val isMuted: Boolean = false,
        override val sectionType: SectionType? = null
    ) : WidgetView()

    data object EmptyState : WidgetView() {
        override val id: Id get() = "id.widgets.empty.state"
        override val canCreateObjectOfType: Boolean = false
        override val sectionType: SectionType? = null
    }

    interface Draggable
}

sealed class DropDownMenuAction {
    data object ChangeWidgetType : DropDownMenuAction()
    data object RemoveWidget : DropDownMenuAction()
    data object EmptyBin : DropDownMenuAction()
    data class CreateObjectOfType(val widgetId: WidgetId) : DropDownMenuAction()
}