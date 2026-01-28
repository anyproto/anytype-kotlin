package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.chats.NotificationState
import com.anytypeio.anytype.core_models.ui.AttachmentPreview
import com.anytypeio.anytype.presentation.editor.cover.CoverView
import com.anytypeio.anytype.presentation.editor.model.Indent
import com.anytypeio.anytype.core_models.ui.ObjectIcon

sealed class WidgetView {

    sealed interface Name {
        data class Bundled(val source: Widget.Source.Bundled) : Name
        data class Default(val prettyPrintName: String) : Name
        data object Empty : Name
    }

    sealed interface Element {
        val objectIcon: ObjectIcon
        val obj: ObjectWrapper.Basic
        val name: Name
        
        interface Regular : Element
        
        interface Chat : Element {
            val counter: ChatCounter?
        }
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
        override val sectionType: SectionType? = null,
        val counter: ChatCounter? = null,
        val notificationState: NotificationState? = null
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

        sealed class Element : WidgetView.Element {
            abstract val cover: CoverView?
            
            data class Regular(
                override val objectIcon: ObjectIcon,
                override val obj: ObjectWrapper.Basic,
                override val name: Name,
                override val cover: CoverView? = null
            ) : Element(), WidgetView.Element.Regular
            
            data class Chat(
                override val objectIcon: ObjectIcon,
                override val obj: ObjectWrapper.Basic,
                override val name: Name,
                override val cover: CoverView? = null,
                override val counter: ChatCounter? = null,
                val creatorName: String? = null,
                val messageText: String? = null,
                val messageTime: String? = null,
                val attachmentPreviews: List<AttachmentPreview> = emptyList(),
                val chatNotificationState: NotificationState = NotificationState.ALL
            ) : Element(), WidgetView.Element.Chat
        }
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

    data class ChatList(
        override val id: Id,
        val icon: ObjectIcon = ObjectIcon.None,
        val source: Widget.Source,
        val tabs: List<SetOfObjects.Tab>,
        val elements: List<SetOfObjects.Element>,
        val isExpanded: Boolean,
        val isCompact: Boolean = false,
        val name: Name,
        val hasMore: Boolean = false,
        override val sectionType: SectionType? = null,
        val displayMode: DisplayMode = DisplayMode.Preview
    ) : WidgetView(), Draggable {

        override val canCreateObjectOfType: Boolean
            get() = source.canCreateObjectOfType()

        sealed class DisplayMode {
            /**
             * Compact display: shows chat objects with counters as list items.
             */
            data object Compact : DisplayMode()

            /**
             * Preview display: shows chat list with message previews.
             * Currently displays identically to Compact as a placeholder.
             * Will be replaced with rich preview UI in the future.
             */
            data object Preview : DisplayMode()
        }

        fun toGallery(): WidgetView.Gallery {
            return WidgetView.Gallery(
                id = id,
                icon = icon,
                name = name,
                tabs = tabs,
                elements = elements,
                isExpanded = isExpanded,
                source = source,
                hasMore = hasMore,
                sectionType = sectionType
            )
        }
    }

    data class UnreadChatList(
        override val id: Id,
        val icon: ObjectIcon = ObjectIcon.None,
        val source: Widget.Source,
        val elements: List<SetOfObjects.Element>,
        val isExpanded: Boolean,
        val name: Name,
        override val sectionType: SectionType? = null
    ) : WidgetView() {
        override val canCreateObjectOfType: Boolean = false
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

        sealed class Element : WidgetView.Element {
            data class Regular(
                override val objectIcon: ObjectIcon,
                override val obj: ObjectWrapper.Basic,
                override val name: Name
            ) : Element(), WidgetView.Element.Regular
            
            data class Chat(
                override val objectIcon: ObjectIcon,
                override val obj: ObjectWrapper.Basic,
                override val name: Name,
                override val counter: ChatCounter? = null,
                val creatorName: String? = null,
                val messageText: String? = null,
                val messageTime: String? = null,
                val attachmentPreviews: List<AttachmentPreview> = emptyList(),
                val chatNotificationState: NotificationState = NotificationState.ALL
            ) : Element(), WidgetView.Element.Chat
        }

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

    data class ChatCounter(
        val unreadMentionCount: Int,
        val unreadMessageCount: Int
    )
}

/**
 * Generates a composite key for use in Compose lazy lists to ensure uniqueness
 * across different sections (PINNED, TYPES, etc.).
 * Format: "SECTION_widgetId"
 */
fun WidgetView.compositeKey(): String = "${sectionType}_${id}"

/**
 * Extracts the widget ID from a composite key generated by [compositeKey].
 *
 * Expected format: "SECTION_widgetId" (e.g., "PINNED_abc123" or "TYPES_xyz789")
 *
 * @return The widget ID portion of the composite key, or null if the format is invalid
 *         (e.g., no underscore present or empty result after extraction)
 *
 * Examples:
 * - "PINNED_abc123".extractWidgetId() → "abc123"
 * - "TYPES_xyz".extractWidgetId() → "xyz"
 * - "PINNED_id_with_underscores".extractWidgetId() → "id_with_underscores"
 * - "INVALIDKEY".extractWidgetId() → null
 * - "".extractWidgetId() → null
 */
fun String.extractWidgetId(): String? =
    substringAfter("_", "").takeIf { it.isNotEmpty() }

sealed class DropDownMenuAction {
    data object ChangeWidgetType : DropDownMenuAction()
    data object RemoveWidget : DropDownMenuAction()
    data object EmptyBin : DropDownMenuAction()
    data class CreateObjectOfType(val widgetId: WidgetId) : DropDownMenuAction()
}