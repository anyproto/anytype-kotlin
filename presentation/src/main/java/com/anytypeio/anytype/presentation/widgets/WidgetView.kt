package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelativeDate
import com.anytypeio.anytype.core_models.SHARED_SPACE_TYPE
import com.anytypeio.anytype.core_models.SpaceType
import com.anytypeio.anytype.core_models.SupportedLayouts
import com.anytypeio.anytype.presentation.editor.cover.CoverView
import com.anytypeio.anytype.presentation.editor.model.Indent
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.spaces.SpaceIconView

sealed class WidgetView {

    sealed interface Name {
        data class Bundled(val source: Widget.Source.Bundled): Name
        data class Default(val prettyPrintName: String): Name
    }

    interface Element {
        val objectIcon: ObjectIcon
        val obj: ObjectWrapper.Basic
        val name: Name
    }

    abstract val id: Id
    abstract val isLoading: Boolean

    data class Tree(
        override val id: Id,
        override val isLoading: Boolean = false,
        val name: Name,
        val source: Widget.Source,
        val elements: List<Element> = emptyList(),
        val isExpanded: Boolean = false,
        val isEditable: Boolean = true
    ) : WidgetView(), Draggable {
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
            data object Collection: ElementIcon()
        }
    }

    data class Link(
        override val id: Id,
        override val isLoading: Boolean = false,
        val name: Name,
        val source: Widget.Source,
    ) : WidgetView(), Draggable

    data class SetOfObjects(
        override val id: Id,
        override val isLoading: Boolean = false,
        val source: Widget.Source,
        val tabs: List<Tab>,
        val elements: List<Element>,
        val isExpanded: Boolean,
        val isCompact: Boolean = false,
        val name: Name
    ) : WidgetView(), Draggable {
        val canCreateObjectOfType : Boolean get() {
            return when(source) {
                Widget.Source.Bundled.AllObjects -> false
                Widget.Source.Bundled.Chat -> false
                Widget.Source.Bundled.Bin -> false
                Widget.Source.Bundled.Favorites -> true
                Widget.Source.Bundled.Recent -> false
                Widget.Source.Bundled.RecentLocal -> false
                is Widget.Source.Default -> {
                    if (source.obj.layout == ObjectType.Layout.OBJECT_TYPE) {
                        val wrapper = ObjectWrapper.Type(source.obj.map)
                        SupportedLayouts.createObjectLayouts.contains(wrapper.recommendedLayout)
                    } else {
                        true
                    }
                }
            }
        }

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
        override val isLoading: Boolean = false,
        val view: Id? = null,
        val name: Name,
        val source: Widget.Source,
        val tabs: List<SetOfObjects.Tab>,
        val elements: List<SetOfObjects.Element>,
        val isExpanded: Boolean,
        val showIcon: Boolean = false,
        val showCover: Boolean = false
    ) : WidgetView(), Draggable {
        val canCreateObjectOfType : Boolean get() {
            return when(source) {
                Widget.Source.Bundled.AllObjects -> false
                Widget.Source.Bundled.Chat -> false
                Widget.Source.Bundled.Bin -> false
                Widget.Source.Bundled.Favorites -> true
                Widget.Source.Bundled.Recent -> false
                Widget.Source.Bundled.RecentLocal -> false
                is Widget.Source.Default -> {
                    if (source.obj.layout == ObjectType.Layout.OBJECT_TYPE) {
                        val wrapper = ObjectWrapper.Type(source.obj.map)
                        SupportedLayouts.createObjectLayouts.contains(wrapper.recommendedLayout)
                    } else {
                        true
                    }
                }
            }
        }
    }

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
            override val obj: ObjectWrapper.Basic,
            override val name: Name
        ) : WidgetView.Element
        sealed class Type {
            data object Recent : Type()
            data object RecentLocal : Type()
            data object Favorites : Type()
            data object Bin: Type()
        }
    }

    data class Bin(
        override val id: Id,
        override val isLoading: Boolean = false,
        val isEmpty: Boolean = false
    ) : WidgetView()

    data class AllContent(
        override val id: Id
        ): WidgetView() {
        override val isLoading: Boolean = false
    }

    data class SpaceChat(
        override val id: Id,
        val source: Widget.Source,
        val unreadMessageCount: Int = 0,
        val unreadMentionCount: Int = 0,
        val isMuted: Boolean = false
    ) : WidgetView() {
        override val isLoading: Boolean = false
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

    sealed class Action : WidgetView() {
        data object EditWidgets : Action() {
            override val id: Id get() = "id.action.edit-widgets"
            override val isLoading: Boolean = false
        }
    }

    data object EmptyState : WidgetView() {
        override val id: Id get() = "id.widgets.empty.state"
        override val isLoading: Boolean = false
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