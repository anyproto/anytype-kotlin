package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.ObjectWrapper.Type
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.core_models.SupportedLayouts
import com.anytypeio.anytype.core_models.SupportedLayouts.createObjectLayouts
import com.anytypeio.anytype.core_models.ext.asMap
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.presentation.objects.canCreateObjectOfType
import com.anytypeio.anytype.core_models.widgets.BundledWidgetSourceIds
import com.anytypeio.anytype.core_utils.R
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.getTypeOfObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.presentation.home.ObjectViewState
import com.anytypeio.anytype.presentation.mapper.objectIcon
import com.anytypeio.anytype.presentation.objects.ObjectIcon
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
    TYPES,
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

    sealed class Section : Widget() {
        data class Pinned(
            override val id: Id = SECTION_PINNED,
            override val source: Source = Source.Other,
            override val config: Config,
            override val isAutoCreated: Boolean = false,
            override val icon: ObjectIcon = ObjectIcon.None,
            override val sectionType: SectionType = SectionType.PINNED
        ) : Section()

        data class ObjectType(
            override val id: Id = SECTION_OBJECT_TYPE,
            override val source: Source = Source.Other,
            override val config: Config,
            override val isAutoCreated: Boolean = false,
            override val icon: ObjectIcon = ObjectIcon.None,
            override val sectionType: SectionType = SectionType.TYPES
        ) : Section()
    }

    data class Bin(
        override val id: Id ,
        override val source: Source.Bundled.Bin,
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
            const val SECTION_PINNED = "pinned_section"
            const val SECTION_OBJECT_TYPE = "object_type_section"
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
                createObjectLayouts.contains(obj.layout)
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
                    val raw = details[target] ?: mapOf(Relations.ID to sourceContent.target)
                    val targetObj = ObjectWrapper.Basic(raw)
                    val icon = targetObj.objectIcon(
                        builder = urlBuilder,
                        objType = storeOfObjectTypes.getTypeOfObject(targetObj)
                    )
                    val source = if (BundledWidgetSourceIds.ids.contains(target)) {
                        target.bundled()
                    } else {
                        Widget.Source.Default(obj = targetObj)
                    }
                    if (source.hasValidSource() && !WidgetConfig.excludedTypes.contains(source.type)) {
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
 * Result of building widgets, separated into pinned and type sections.
 *
 * @property pinnedWidgets Widgets from the pinned section (user-arranged, including chat)
 * @property typeWidgets Widgets from the object type section (system-generated, including bin)
 */
data class WidgetSections(
    val pinnedWidgets: List<Widget>,
    val typeWidgets: List<Widget>
)

suspend fun buildWidgetSections(
    spaceView: ObjectWrapper.SpaceView,
    state: ObjectViewState.Success,
    params: WidgetUiParams,
    urlBuilder: UrlBuilder,
    storeOfObjectTypes: StoreOfObjectTypes
): WidgetSections {
    val currentCollapsedSections = params.collapsedSections

    // Build pinned section
    val pinnedWidgets = buildPinnedSection(
        spaceView = spaceView,
        state = state,
        isPinnedSectionCollapsed = currentCollapsedSections.contains(SECTION_PINNED),
        urlBuilder = urlBuilder,
        storeOfObjectTypes = storeOfObjectTypes
    )

    // Build type section
    val typeWidgets = buildTypeSection(
        state = state,
        params = params,
        isObjectTypeSectionCollapsed = currentCollapsedSections.contains(SECTION_OBJECT_TYPE),
        storeOfObjectTypes = storeOfObjectTypes,
        isChatSpace = spaceView.spaceUxType == SpaceUxType.CHAT
    )

    return WidgetSections(
        pinnedWidgets = pinnedWidgets,
        typeWidgets = typeWidgets
    )
}

/**
 * Builds the pinned widgets section including chat widget and user-arranged widgets.
 */
private suspend fun buildPinnedSection(
    spaceView: ObjectWrapper.SpaceView,
    state: ObjectViewState.Success,
    isPinnedSectionCollapsed: Boolean,
    urlBuilder: UrlBuilder,
    storeOfObjectTypes: StoreOfObjectTypes
): List<Widget> = buildList {
    // Space chat widget for Shared Data Spaces
    val spaceChatId = state.config.spaceChatId
    if (!spaceChatId.isNullOrEmpty()
        && spaceView.isShared
        && spaceView.spaceUxType != SpaceUxType.CHAT
    ) {
        add(Widget.Chat(config = state.config))
    }

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
        // Always add section header
        add(Widget.Section.Pinned(config = state.config))

        // Add widgets only if section is expanded
        if (!isPinnedSectionCollapsed) {
            addAll(userPinnedWidgets)
        }
    }
}

/**
 * Builds the object type widgets section including type widgets and bin.
 */
private suspend fun buildTypeSection(
    state: ObjectViewState.Success,
    params: WidgetUiParams,
    isObjectTypeSectionCollapsed: Boolean,
    storeOfObjectTypes: StoreOfObjectTypes,
    isChatSpace: Boolean
): List<Widget> = buildList {
    // Always add section header
    add(Widget.Section.ObjectType(config = state.config))

    val sectionStateDesc = if (isObjectTypeSectionCollapsed) "collapsed" else "expanded"

    if (!isObjectTypeSectionCollapsed) {
        val types = mapSpaceTypesToWidgets(
            isOwnerOrEditor = params.isOwnerOrEditor,
            config = state.config,
            storeOfObjectTypes = storeOfObjectTypes,
            isChatSpace = isChatSpace
        )
        addAll(types)

        // Add bin widget for owners/editors
        if (params.isOwnerOrEditor) {
            add(
                Widget.Bin(
                    id = WIDGET_BIN_ID,
                    source = Widget.Source.Bundled.Bin,
                    config = state.config,
                    icon = ObjectIcon.None,
                    sectionType = SectionType.TYPES
                )
            )
        }

        Timber.d("ObjectType section: $sectionStateDesc, widgets added: ${types.size}")
    } else {
        Timber.d("ObjectType section: $sectionStateDesc, widgets: 0 (section collapsed)")
    }
}

internal suspend fun mapSpaceTypesToWidgets(
    isOwnerOrEditor: Boolean,
    config: Config,
    storeOfObjectTypes: StoreOfObjectTypes,
    isChatSpace: Boolean
): List<Widget> {
    val allTypes = storeOfObjectTypes.getAll()
    val filteredObjectTypes = allTypes
        .mapNotNull { objectType ->
            if (!objectType.isValid ||
                SupportedLayouts.excludedSpaceTypeLayouts.contains(objectType.recommendedLayout) ||
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

    // Define custom sort order based on uniqueKey
    val customUniqueKeyOrder = if (!isChatSpace) {
        listOf(
            ObjectTypeIds.PAGE,
            ObjectTypeIds.NOTE,
            ObjectTypeIds.TASK,
            ObjectTypeIds.COLLECTION,
            ObjectTypeIds.SET,
            ObjectTypeIds.BOOKMARK,
            ObjectTypeIds.PROJECT,
            ObjectTypeIds.IMAGE,
            ObjectTypeIds.FILE,
            ObjectTypeIds.VIDEO,
            ObjectTypeIds.AUDIO
        )
    } else {
        listOf(
            ObjectTypeIds.IMAGE,
            ObjectTypeIds.BOOKMARK,
            ObjectTypeIds.FILE,
            ObjectTypeIds.PAGE,
            ObjectTypeIds.NOTE,
            ObjectTypeIds.TASK,
            ObjectTypeIds.COLLECTION,
            ObjectTypeIds.SET,
            ObjectTypeIds.PROJECT,
            ObjectTypeIds.VIDEO,
            ObjectTypeIds.AUDIO
        )
    }

    val sortedTypes = sortObjectTypesByPriority(filteredObjectTypes, customUniqueKeyOrder)

    return sortedTypes.map { objectType ->
        createWidgetViewFromType(objectType, config)
    }
}

/** Sorts a list of ObjectWrapper.Type objects by priority.
 * 1. Primary: orderId (ascending, nulls at end)
 * 2. Secondary: customUniqueKeyOrder (position in list)
 * 3. Tertiary: name (ascending)
 *
 * @param types The list of ObjectWrapper.Type objects to sort.
 * @param customUniqueKeyOrder The custom order of unique keys to use for secondary sorting.
 * @return A new list of ObjectWrapper.Type objects sorted by the specified priority.
 */
private fun sortObjectTypesByPriority(
    types: List<ObjectWrapper.Type>,
    customUniqueKeyOrder: List<String>
): List<ObjectWrapper.Type> {
    return types.sortedWith(
        compareBy<ObjectWrapper.Type> { objectType ->
            // Primary sort: orderId presence (items with orderId come first)
            if (objectType.orderId != null) 0 else 1
        }.thenBy { objectType ->
            // Primary sort continuation: orderId value (for items that have orderId)
            objectType.orderId ?: ""
        }.thenBy { objectType ->
            // Secondary sort: custom order by uniqueKey
            val index = customUniqueKeyOrder.indexOf(objectType.uniqueKey)
            if (index >= 0) index else Int.MAX_VALUE
        }.thenBy { objectType ->
            // Tertiary sort: name (case-insensitive)
            objectType.name?.lowercase() ?: ""
        }
    )
}

/**
 * Creates a WidgetView from ObjectWrapper.Type based on the widget layout configuration.
 */
private fun createWidgetViewFromType(objectType: ObjectWrapper.Type, config: Config): Widget {
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
