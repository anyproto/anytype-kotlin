package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.ObjectWrapper.Type
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.core_models.SupportedLayouts
import com.anytypeio.anytype.core_models.SupportedLayouts.createObjectLayouts
import com.anytypeio.anytype.core_models.SupportedLayouts.getSystemLayouts
import com.anytypeio.anytype.core_models.ext.asMap
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.widgets.BundledWidgetSourceIds
import com.anytypeio.anytype.core_utils.R
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.getTypeOfObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.presentation.home.ObjectViewState
import com.anytypeio.anytype.core_models.ui.objectIcon
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.presentation.objects.canCreateObjectOfType
import com.anytypeio.anytype.presentation.objects.sortByTypePriority
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.widgets.Widget.Source.Companion.SECTION_OBJECT_TYPE
import com.anytypeio.anytype.presentation.widgets.Widget.Source.Companion.SECTION_PINNED
import com.anytypeio.anytype.presentation.widgets.Widget.Source.Companion.WIDGET_BIN_ID
import com.anytypeio.anytype.presentation.widgets.WidgetView.Name
import com.anytypeio.anytype.presentation.widgets.WidgetView.Name.Bundled
import com.anytypeio.anytype.presentation.widgets.WidgetView.Name.Empty
import timber.log.Timber

enum class SectionType {
    PINNED,
    UNREAD,
    TYPES,
    RECENTLY_EDITED,
    NONE
}

sealed class Widget {

    abstract val id: Id

    abstract val source: Source
    abstract val config: Config
    abstract val icon: ObjectIcon

    abstract val isAutoCreated: Boolean
    abstract val sectionType: SectionType

    /**
     * @property [id] id of the widget
     * @property [source] source for this widget - root object for a tree of objects.
     */
    data class Tree(
        override val id: Id,
        override val source: Source,
        override val config: Config,
        override val isAutoCreated: Boolean = false,
        val limit: Int = 0,
        override val icon: ObjectIcon,
        override val sectionType: SectionType
    ) : Widget()

    /**
     * @property [id] id of the widget
     * @property [source] source for this widget - one specific object associate with this widget.
     */
    data class Link(
        override val id: Id,
        override val source: Source,
        override val config: Config,
        override val isAutoCreated: Boolean = false,
        override val icon: ObjectIcon,
        override val sectionType: SectionType
    ) : Widget()

    /**
     * @property [id] id of the widget
     * @property [source] source for this widget - one specific object associate with this widget.
     */
    data class List(
        override val id: Id,
        override val source: Source,
        override val config: Config,
        override val isAutoCreated: Boolean = false,
        override val icon: ObjectIcon,
        val isCompact: Boolean = false,
        val limit: Int = 0,
        override val sectionType: SectionType
    ) : Widget()

    data class View(
        override val id: Id,
        override val source: Source,
        override val config: Config,
        override val isAutoCreated: Boolean = false,
        override val icon: ObjectIcon,
        val limit: Int,
        override val sectionType: SectionType
    ) : Widget()

    data class AllObjects(
        override val id: Id,
        override val source: Source.Bundled.AllObjects,
        override val config: Config,
        override val icon: ObjectIcon = ObjectIcon.None,
        override val isAutoCreated: Boolean = false,
        override val sectionType: SectionType
    ) : Widget()

    data class Chat(
        override val id: Id = BundledWidgetSourceIds.CHAT,
        override val source: Source.Bundled.Chat = Source.Bundled.Chat,
        override val config: Config,
        override val isAutoCreated: Boolean = false,
        override val icon: ObjectIcon = ObjectIcon.SimpleIcon("chatbubble", R.color.control_primary),
        override val sectionType: SectionType = SectionType.NONE
    ) : Widget()

    data class UnreadChatList(
        override val id: Id,
        override val source: Source.Bundled.Chat,
        override val config: Config,
        override val isAutoCreated: Boolean = false,
        override val icon: ObjectIcon = ObjectIcon.SimpleIcon("chatbubble", R.color.control_primary),
        override val sectionType: SectionType = SectionType.UNREAD
    ) : Widget()

    data class Bin(
        override val id: Id ,
        override val source: Source.Bundled.Bin,
        override val config: Config,
        override val isAutoCreated: Boolean = false,
        override val icon: ObjectIcon = ObjectIcon.None,
        override val sectionType: SectionType = SectionType.NONE
    ) : Widget()

    /**
     * Recently edited widget displaying objects sorted by last modified date.
     * Visibility controlled by WidgetSectionType.RECENTLY_EDITED.
     */
    data class RecentlyEdited(
        override val id: Id,
        override val source: Source.Bundled.Recent,
        override val config: Config,
        override val isAutoCreated: Boolean = false,
        override val icon: ObjectIcon = ObjectIcon.None,
        override val sectionType: SectionType = SectionType.RECENTLY_EDITED
    ) : Widget()

    /**
     * Grouped widget containing all object types.
     * Replaces individual type widgets for better performance and visual consistency.
     * The actual types data will be fetched by ObjectTypesGroupWidgetContainer.
     * 
     * @property id Unique identifier (typically "object_types_group")
     * @property source Source for this grouped widget
     * @property config Widget configuration
     * @property isAutoCreated Whether this widget was auto-created
     * @property icon Icon for the widget (typically None)
     * @property sectionType Always SectionType.TYPES
     */
    data class ObjectTypesGroup(
        override val id: Id,
        override val source: Source,
        override val config: Config,
        override val isAutoCreated: Boolean = false,
        override val icon: ObjectIcon = ObjectIcon.None,
        override val sectionType: SectionType = SectionType.TYPES
    ) : Widget()

    sealed class Source {

        abstract val id: Id
        abstract val type: Id?

        data class Default(val obj: ObjectWrapper.Basic) : Source() {
            override val id: Id = obj.id
            // For ObjectType objects, use uniqueKey as type; for regular objects, use type field
            override val type: Id? = obj.uniqueKey ?: obj.type.firstOrNull()
        }

        sealed class Bundled : Source() {
            data object Favorites : Bundled() {
                override val id: Id = BundledWidgetSourceIds.FAVORITE
                override val type: Id? = null
            }

            data object Recent : Bundled() {
                override val id: Id = BundledWidgetSourceIds.RECENT
                override val type: Id? = null
            }

            data object RecentLocal : Bundled() {
                override val id: Id = BundledWidgetSourceIds.RECENT_LOCAL
                override val type: Id? = null
            }

            data object Bin : Bundled() {
                override val id: Id = BundledWidgetSourceIds.BIN
                override val type: Id? = null
            }

            data object AllObjects : Bundled() {
                override val id: Id = BundledWidgetSourceIds.ALL_OBJECTS
                override val type: Id? = null
            }

            data object Chat : Bundled() {
                override val id: Id = BundledWidgetSourceIds.CHAT
                override val type: Id? = null
            }
        }

        data object Other : Source() {
            override val id: Id = SOURCE_OTHER
            override val type: Id? = null
        }

        companion object {
            const val WIDGET_BIN_ID = "widget_bin_id"
            const val WIDGET_RECENTLY_EDITED_ID = "widget_recently_edited_id"
            const val SECTION_PINNED = "pinned_section"
            const val SECTION_UNREAD = "unread_section"
            const val SECTION_OBJECT_TYPE = "object_type_section"
            const val SECTION_RECENTLY_EDITED = "recently_edited_section"
            const val OBJECT_TYPES_GROUP_ID = "object_types_group"
            const val SOURCE_OTHER = "source_other"

            val SOURCE_KEYS = ObjectSearchConstants.defaultKeys
        }
    }
}


fun Widget.Source.getPrettyName(fieldParser: FieldParser): Name {
    return when (this) {
        is Widget.Source.Bundled -> Bundled(source = this)
        is Widget.Source.Default -> buildWidgetName(obj, fieldParser)
        Widget.Source.Other -> Empty
    }
}

fun List<Widget>.forceChatPosition(): List<Widget> {
    // Partition the list into chat widgets and the rest
    val (chatWidgets, otherWidgets) = partition { widget ->
        widget.source is Widget.Source.Bundled.Chat
    }
    // Place chat widgets first, followed by the others
    return chatWidgets + otherWidgets
}

fun Widget.Source.hasValidSource(): Boolean = when (this) {
    is Widget.Source.Bundled -> true
    is Widget.Source.Default -> obj.isValid && obj.notDeletedNorArchived
    Widget.Source.Other -> false
}

fun Widget.Source.canCreateObjectOfType(): Boolean {
    return when (this) {
        Widget.Source.Bundled.Favorites -> true
        is Widget.Source.Default -> {
            if (obj.layout == ObjectType.Layout.OBJECT_TYPE) {
                val wrapper = Type(obj.map)
                canCreateObjectOfType(wrapper)
            } else {
                // For Sets: only allow object creation if setOf is not empty
                if (obj.layout == ObjectType.Layout.SET) {
                    obj.setOf.isNotEmpty() && createObjectLayouts.contains(obj.layout)
                } else {
                    createObjectLayouts.contains(obj.layout)
                }
            }
        }
        Widget.Source.Bundled.AllObjects -> false
        Widget.Source.Bundled.Bin -> false
        Widget.Source.Bundled.Chat -> false
        Widget.Source.Bundled.Recent -> false
        Widget.Source.Bundled.RecentLocal -> false
        Widget.Source.Other -> false
    }
}

fun List<Block>.parseActiveViews() : WidgetToActiveView {
    val result = mutableMapOf<WidgetId, WidgetActiveViewId>()
    forEach { block ->
        val content = block.content
        if (content is Block.Content.Widget) {
            val view = content.activeView
            if (!view.isNullOrEmpty()) {
                result[block.id] = view
            }
        }
    }
    return result
}

suspend fun List<Block>.parseWidgets(
    root: Id,
    details: Map<Id, Struct>,
    config: Config,
    urlBuilder: UrlBuilder,
    storeOfObjectTypes: StoreOfObjectTypes,
): List<Widget> = buildList {
    val map = asMap()
    val widgets = map[root] ?: emptyList()
    widgets.forEach { w ->
        val widgetContent = w.content
        if (widgetContent is Block.Content.Widget) {
            val child = (map[w.id] ?: emptyList()).firstOrNull()
            if (child != null) {
                val sourceContent = child.content
                if (sourceContent is Block.Content.Link) {
                    val target = sourceContent.target
                    val raw = details[target].orEmpty()
                    val targetObj = ObjectWrapper.Basic(raw)
                    val source = if (BundledWidgetSourceIds.ids.contains(target)) {
                        target.bundled()
                    } else {
                        Widget.Source.Default(obj = targetObj)
                    }
                    if (source.hasValidSource() && !WidgetConfig.excludedTypes.contains(source.type)) {
                        val icon = targetObj.objectIcon(
                            builder = urlBuilder,
                            objType = storeOfObjectTypes.getTypeOfObject(targetObj)
                        )
                        when (source) {
                            is Widget.Source.Bundled.AllObjects -> {
                                add(
                                    Widget.AllObjects(
                                        id = w.id,
                                        source = source,
                                        config = config,
                                        isAutoCreated = widgetContent.isAutoAdded,
                                        sectionType = SectionType.PINNED
                                    )
                                )
                            }

                            is Widget.Source.Bundled.Chat -> {
                                Timber.d("DROID-4016, Skipping chat widget in pinned section")
                                //DROID-4016 skip chat widget
                            }

                            else -> {
                                when (widgetContent.layout) {
                                    Block.Content.Widget.Layout.TREE -> {
                                        add(
                                            Widget.Tree(
                                                id = w.id,
                                                source = source,
                                                limit = widgetContent.limit,
                                                config = config,
                                                isAutoCreated = widgetContent.isAutoAdded,
                                                icon = icon,
                                                sectionType = SectionType.PINNED
                                            )
                                        )
                                    }

                                    Block.Content.Widget.Layout.LINK -> {
                                        add(
                                            Widget.Link(
                                                id = w.id,
                                                source = source,
                                                config = config,
                                                icon = icon,
                                                isAutoCreated = widgetContent.isAutoAdded,
                                                sectionType = SectionType.PINNED
                                            )
                                        )
                                    }

                                    Block.Content.Widget.Layout.LIST -> {
                                        add(
                                            Widget.List(
                                                id = w.id,
                                                source = source,
                                                isCompact = false,
                                                limit = widgetContent.limit,
                                                config = config,
                                                icon = icon,
                                                isAutoCreated = widgetContent.isAutoAdded,
                                                sectionType = SectionType.PINNED
                                            )
                                        )
                                    }

                                    Block.Content.Widget.Layout.COMPACT_LIST -> {
                                        add(
                                            Widget.List(
                                                id = w.id,
                                                source = source,
                                                isCompact = true,
                                                limit = widgetContent.limit,
                                                config = config,
                                                icon = icon,
                                                isAutoCreated = widgetContent.isAutoAdded,
                                                sectionType = SectionType.PINNED
                                            )
                                        )
                                    }

                                    Block.Content.Widget.Layout.VIEW -> {
                                        if (source is Widget.Source.Default) {
                                            add(
                                                Widget.View(
                                                    id = w.id,
                                                    source = source,
                                                    limit = widgetContent.limit,
                                                    config = config,
                                                    icon = icon,
                                                    isAutoCreated = widgetContent.isAutoAdded,
                                                    sectionType = SectionType.PINNED
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class WidgetUiParams(
    val isOwnerOrEditor: Boolean,
    val expandedIds: Set<Id>,
    val collapsedSections: Set<String>
)

/**
 * Result of building widgets, separated into sections.
 *
 * @property pinnedWidgets Widgets from the pinned section (user-arranged widgets)
 * @property typeWidgets Widgets from the object type section
 * @property unreadWidget The unread chat list widget, displayed separately at the top
 * @property recentlyEditedWidget The recently edited widget, displayed after objects section
 * @property binWidget The bin widget, displayed separately at the bottom
 */
data class WidgetSections(
    val pinnedWidgets: List<Widget>,
    val typeWidgets: List<Widget>,
    val unreadWidget: Widget.UnreadChatList? = null,
    val recentlyEditedWidget: Widget.RecentlyEdited? = null,
    val binWidget: Widget.Bin? = null
)

suspend fun buildWidgetSections(
    spaceView: ObjectWrapper.SpaceView,
    state: ObjectViewState.Success,
    params: WidgetUiParams,
    urlBuilder: UrlBuilder,
    storeOfObjectTypes: StoreOfObjectTypes,
    sectionConfig: com.anytypeio.anytype.core_models.WidgetSections
): WidgetSections {
    val currentCollapsedSections = params.collapsedSections

    // Build pinned section only if visible
    val pinnedWidgets = if (sectionConfig.isSectionVisible(com.anytypeio.anytype.core_models.WidgetSectionType.PINNED)) {
        buildPinnedSection(
            state = state,
            isPinnedSectionCollapsed = currentCollapsedSections.contains(SECTION_PINNED),
            urlBuilder = urlBuilder,
            storeOfObjectTypes = storeOfObjectTypes
        )
    } else {
        emptyList()
    }

    // Build type section only if visible
    val typeWidgets = if (sectionConfig.isSectionVisible(com.anytypeio.anytype.core_models.WidgetSectionType.OBJECTS)) {
        buildTypeSection(
            state = state,
            params = params,
            isObjectTypeSectionCollapsed = currentCollapsedSections.contains(SECTION_OBJECT_TYPE),
            storeOfObjectTypes = storeOfObjectTypes,
            spaceUxType = spaceView.spaceUxType
        )
    } else {
        emptyList()
    }

    // Build unread widget only if visible - only for data spaces
    val unreadWidget = if (sectionConfig.isSectionVisible(com.anytypeio.anytype.core_models.WidgetSectionType.UNREAD)) {
        buildUnreadWidget(
            state = state,
            spaceUxType = spaceView.spaceUxType
        )
    } else {
        null
    }

    // Build bin widget only if visible
    val binWidget = if (sectionConfig.isSectionVisible(com.anytypeio.anytype.core_models.WidgetSectionType.BIN)) {
        buildBinWidget(
            state = state,
            params = params
        )
    } else {
        null
    }

    // Build recently edited widget only if visible
    val recentlyEditedWidget = if (sectionConfig.isSectionVisible(com.anytypeio.anytype.core_models.WidgetSectionType.RECENTLY_EDITED)) {
        buildRecentlyEditedWidget(state = state)
    } else {
        null
    }

    return WidgetSections(
        pinnedWidgets = pinnedWidgets,
        typeWidgets = typeWidgets,
        unreadWidget = unreadWidget,
        recentlyEditedWidget = recentlyEditedWidget,
        binWidget = binWidget
    )
}

/**
 * Builds the space chat widget for data spaces with chat.
 * Displayed separately at the top, above all sections.
 */
private fun buildChatWidget(
    spaceView: ObjectWrapper.SpaceView,
    state: ObjectViewState.Success
): Widget.Chat? {
    val spaceChatId = spaceView.chatId
    return if (!spaceChatId.isNullOrEmpty()
        && spaceView.spaceUxType != SpaceUxType.CHAT
    ) {
        Widget.Chat(config = state.config)
    } else {
        null
    }
}

/**
 * Builds the pinned widgets section with user-arranged widgets.
 */
private suspend fun buildPinnedSection(
    state: ObjectViewState.Success,
    isPinnedSectionCollapsed: Boolean,
    urlBuilder: UrlBuilder,
    storeOfObjectTypes: StoreOfObjectTypes
): List<Widget> = buildList {
    // Pinned widgets (from blocks)
    val userPinnedWidgets = state.obj.blocks.parseWidgets(
        root = state.obj.root,
        details = state.obj.details,
        config = state.config,
        urlBuilder = urlBuilder,
        storeOfObjectTypes = storeOfObjectTypes
    ).filterNot { widget ->
        widget.source is Widget.Source.Bundled.Bin
    }

    if (userPinnedWidgets.isNotEmpty()) {
        // Add widgets only if section is expanded
        if (!isPinnedSectionCollapsed) {
            addAll(userPinnedWidgets)
        }
    }
}

/**
 * Builds the object type widgets section.
 */
private suspend fun buildTypeSection(
    state: ObjectViewState.Success,
    params: WidgetUiParams,
    isObjectTypeSectionCollapsed: Boolean,
    storeOfObjectTypes: StoreOfObjectTypes,
    spaceUxType: SpaceUxType?
): List<Widget> = buildList {

    val sectionStateDesc = if (isObjectTypeSectionCollapsed) "collapsed" else "expanded"

    if (!isObjectTypeSectionCollapsed) {
        val types = mapSpaceTypesToWidgets(
            isOwnerOrEditor = params.isOwnerOrEditor,
            config = state.config,
            storeOfObjectTypes = storeOfObjectTypes,
            spaceUxType = spaceUxType
        )
        addAll(types)
        Timber.d("ObjectType section: $sectionStateDesc, widgets added: ${types.size}")
    } else {
        Timber.d("ObjectType section: $sectionStateDesc, widgets: 0 (section collapsed)")
    }
}

private fun buildUnreadWidget(
    state: ObjectViewState.Success,
    spaceUxType: SpaceUxType?
): Widget.UnreadChatList? {
    return if (spaceUxType == SpaceUxType.DATA) {
        Timber.d("buildUnreadWidget: Creating unread widget for data space")
        Widget.UnreadChatList(
            id = "widget_unread_chat_list",
            source = Widget.Source.Bundled.Chat,
            config = state.config,
            icon = ObjectIcon.SimpleIcon("chatbubble", R.color.control_primary)
        )
    } else {
        Timber.d("buildUnreadWidget: Skipping unread widget for non-data space")
        null
    }
}

private fun buildBinWidget(
    state: ObjectViewState.Success,
    params: WidgetUiParams
): Widget.Bin? {
    return if (params.isOwnerOrEditor) {
        Widget.Bin(
            id = WIDGET_BIN_ID,
            source = Widget.Source.Bundled.Bin,
            config = state.config,
            icon = ObjectIcon.None
        )
    } else {
        null
    }
}

/**
 * Builds the recently edited widget displaying objects sorted by last modified date.
 */
private fun buildRecentlyEditedWidget(
    state: ObjectViewState.Success
): Widget.RecentlyEdited {
    return Widget.RecentlyEdited(
        id = Widget.Source.WIDGET_RECENTLY_EDITED_ID,
        source = Widget.Source.Bundled.Recent,
        config = state.config,
        icon = ObjectIcon.None
    )
}

internal suspend fun mapSpaceTypesToWidgets(
    isOwnerOrEditor: Boolean,
    config: Config,
    storeOfObjectTypes: StoreOfObjectTypes,
    spaceUxType: SpaceUxType?
): List<Widget> {
    val allTypes = storeOfObjectTypes.getAll()
    
    // Get system layouts based on space context
    val systemLayoutsForSpace = getSystemLayouts(spaceUxType)
    val excludedLayouts = systemLayoutsForSpace + SupportedLayouts.dateLayouts + listOf(
        ObjectType.Layout.OBJECT_TYPE,
        ObjectType.Layout.PARTICIPANT
    )
    
    val filteredObjectTypes = allTypes
        .mapNotNull { objectType ->
            if (!objectType.isValid ||
                excludedLayouts.contains(objectType.recommendedLayout) ||
                objectType.isArchived == true ||
                objectType.isDeleted == true ||
                objectType.uniqueKey == ObjectTypeIds.TEMPLATE
            ) {
                return@mapNotNull null
            } else {
                objectType
            }
        }

    Timber.d("Refreshing system types, isOwnerOrEditor = $isOwnerOrEditor, allTypes = ${allTypes.size}, types = ${filteredObjectTypes.size}")

    // Sort types by priority using shared extension
    val isChatSpace = spaceUxType == SpaceUxType.CHAT || spaceUxType == SpaceUxType.ONE_TO_ONE
    val sortedTypes = filteredObjectTypes.sortByTypePriority(isChatSpace)

    // Create single grouped widget for object types
    // The container (ObjectTypesGroupWidgetContainer) fetches types from StoreOfObjectTypes
    // and filters types with at least one instance using HasInstanceOfObjectTypeSubscriptionContainer
    return listOf(
        Widget.ObjectTypesGroup(
            id = "object_types_group",
            source = Widget.Source.Other,
            config = config,
            isAutoCreated = true
        )
    )
}

/**
 * Creates a WidgetView from ObjectWrapper.Type based on the widget layout configuration.
 */
private fun createWidgetViewFromType(objectType: Type, config: Config): Widget {
    val widgetSource = Widget.Source.Default(obj = objectType.toBasic())
    val icon = objectType.objectIcon()
    val widgetLimit = objectType.widgetLimit ?: 0

    return when (objectType.widgetLayout) {
        Block.Content.Widget.Layout.TREE -> {
            Widget.Tree(
                id = objectType.id,
                source = widgetSource,
                config = config,
                icon = icon,
                limit = widgetLimit,
                sectionType = SectionType.TYPES
            )
        }
        Block.Content.Widget.Layout.LIST -> {
            Widget.List(
                id = objectType.id,
                source = widgetSource,
                config = config,
                icon = icon,
                limit = widgetLimit,
                sectionType = SectionType.TYPES
            )
        }
        Block.Content.Widget.Layout.COMPACT_LIST -> {
            Widget.List(
                id = objectType.id,
                source = widgetSource,
                config = config,
                icon = icon,
                limit = widgetLimit,
                isCompact = true,
                sectionType = SectionType.TYPES
            )
        }
        Block.Content.Widget.Layout.VIEW -> {
            Widget.View(
                id = objectType.id,
                source = widgetSource,
                config = config,
                icon = icon,
                limit = widgetLimit,
                sectionType = SectionType.TYPES
            )
        }
        Block.Content.Widget.Layout.LINK -> {
            Widget.Link(
                id = objectType.id,
                source = widgetSource,
                config = config,
                icon = icon,
                sectionType = SectionType.TYPES
            )
        }
        null -> {
            if (objectType.uniqueKey == ObjectTypeIds.IMAGE) {
                // Image type widgets default to gallery view
                Widget.View(
                    id = objectType.id,
                    source = widgetSource,
                    config = config,
                    icon = icon,
                    limit = widgetLimit,
                    sectionType = SectionType.TYPES
                )
            } else {
                // Default to compact list for other types
                Widget.List(
                    id = objectType.id,
                    source = widgetSource,
                    config = config,
                    icon = icon,
                    limit = widgetLimit,
                    isCompact = true,
                    sectionType = SectionType.TYPES
                )
            }
        }
    }
}

fun Id.bundled(): Widget.Source.Bundled = when (this) {
    BundledWidgetSourceIds.RECENT -> Widget.Source.Bundled.Recent
    BundledWidgetSourceIds.RECENT_LOCAL -> Widget.Source.Bundled.RecentLocal
    BundledWidgetSourceIds.FAVORITE -> Widget.Source.Bundled.Favorites
    BundledWidgetSourceIds.BIN -> Widget.Source.Bundled.Bin
    BundledWidgetSourceIds.ALL_OBJECTS -> Widget.Source.Bundled.AllObjects
    BundledWidgetSourceIds.CHAT -> Widget.Source.Bundled.Chat
    else -> throw IllegalStateException("Widget bundled id can't be $this")
}

fun buildWidgetName(
    obj: ObjectWrapper.Basic,
    fieldParser: FieldParser
): Name {
    val prettyPrintName = fieldParser.getObjectPluralName(obj, false)
    return Name.Default(prettyPrintName = prettyPrintName)
}

/**
 * Extension to convert ObjectWrapper.Type to ObjectWrapper.Basic
 * This allows us to use a unified Widget.Source.Default for both regular objects and type objects
 */
fun ObjectWrapper.Type.toBasic(): ObjectWrapper.Basic = ObjectWrapper.Basic(this.map)

/**
 * Finds the widget block that links to the specified object context.
 * Returns the block ID if found, null otherwise.
 */
fun findWidgetBlockForObject(ctx: Id, blocks: List<Block>): Id? {
    return blocks.find { block ->
        isWidgetPointingToObject(block, ctx, blocks)
    }?.id
}

/**
 * Checks if a widget block is pointing to the target object.
 * A widget points to an object when its first child is a Link block targeting that object.
 */
private fun isWidgetPointingToObject(
    block: Block,
    targetCtx: Id,
    allBlocks: List<Block>
): Boolean {
    if (block.content !is Block.Content.Widget) return false

    val childLinkId = block.children.firstOrNull() ?: return false
    val linkBlock = allBlocks.find { it.id == childLinkId } ?: return false
    val linkContent = linkBlock.content as? Block.Content.Link ?: return false

    return linkContent.target == targetCtx
}

typealias WidgetId = Id
typealias ViewId = Id
typealias FromIndex = Int
typealias ToIndex = Int
