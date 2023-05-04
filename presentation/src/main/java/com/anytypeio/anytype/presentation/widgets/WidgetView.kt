package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.misc.UrlBuilder
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
            object Collection: ElementIcon()
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
    ) : WidgetView(), Draggable {
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

    object Library : WidgetView() {
        override val id: Id get() = "id.button.library"
    }

    sealed class Action : WidgetView() {
        object EditWidgets : Action() {
            override val id: Id get() = "id.action.edit-widgets"
        }
    }

    interface Draggable
}

sealed class DropDownMenuAction {
    object ChangeWidgetType : DropDownMenuAction()
    object ChangeWidgetSource : DropDownMenuAction()
    object RemoveWidget : DropDownMenuAction()
    object AddBelow: DropDownMenuAction()
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

fun ObjectWrapper.Basic.widgetElementIcon(builder: UrlBuilder) : ObjectIcon {
    val img = iconImage
    val emoji = iconEmoji
    return when (layout) {
        ObjectType.Layout.BASIC -> when {
            !img.isNullOrBlank() -> ObjectIcon.Basic.Image(hash = builder.thumbnail(img))
            !emoji.isNullOrBlank() -> ObjectIcon.Basic.Emoji(unicode = emoji)
            else -> ObjectIcon.Basic.Avatar(name.orEmpty())
        }
        ObjectType.Layout.OBJECT_TYPE -> when {
            !img.isNullOrBlank() -> ObjectIcon.Basic.Image(hash = builder.thumbnail(img))
            !emoji.isNullOrBlank() -> ObjectIcon.Basic.Emoji(unicode = emoji)
            else -> ObjectIcon.None
        }
        ObjectType.Layout.PROFILE -> if (!img.isNullOrBlank()) {
            ObjectIcon.Profile.Image(hash = builder.thumbnail(img))
        } else {
            ObjectIcon.Profile.Avatar(name = name.orEmpty())
        }
        ObjectType.Layout.SET, ObjectType.Layout.COLLECTION -> if (!img.isNullOrBlank()) {
            ObjectIcon.Basic.Image(hash = builder.thumbnail(img))
        } else if (!emoji.isNullOrBlank()) {
            ObjectIcon.Basic.Emoji(unicode = emoji)
        } else {
            ObjectIcon.None
        }
        ObjectType.Layout.IMAGE -> if (!img.isNullOrBlank()) {
            ObjectIcon.Basic.Image(hash = builder.thumbnail(img))
        } else {
            ObjectIcon.None
        }
        ObjectType.Layout.TODO -> ObjectIcon.Task(isChecked = done ?: false)
        ObjectType.Layout.NOTE -> ObjectIcon.Basic.Avatar(snippet.orEmpty())
        ObjectType.Layout.FILE -> ObjectIcon.Basic.Avatar(name.orEmpty())
        ObjectType.Layout.BOOKMARK -> when {
            !img.isNullOrBlank() -> ObjectIcon.Bookmark(image = builder.thumbnail(img))
            !emoji.isNullOrBlank() -> ObjectIcon.Basic.Emoji(unicode = emoji)
            else -> ObjectIcon.None
        }
        else -> ObjectIcon.None
    }
}

fun List<WidgetView>.getActiveTabViews() : Map<Id, Id> {
    return filterIsInstance<WidgetView.SetOfObjects>().mapNotNull { widget ->
        if (widget.tabs.isNotEmpty()) {
            val selected =  widget.tabs.firstOrNull { it.isSelected }
            if (selected != null)
                widget.id to selected.id
            else
                null
        } else {
            null
        }
    }.toMap()
}