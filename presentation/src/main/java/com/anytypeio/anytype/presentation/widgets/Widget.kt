package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Config
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.Struct
import com.anytypeio.anytype.core_models.ext.asMap
import com.anytypeio.anytype.core_models.SupportedLayouts.isSupportedForWidgets
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.presentation.widgets.WidgetView.Name

sealed class Widget {

    abstract val id: Id

    abstract val source: Source
    abstract val config: Config

    /**
     * @property [id] id of the widget
     * @property [source] source for this widget - root object for a tree of objects.
     */
    data class Tree(
        override val id: Id,
        override val source: Source,
        override val config: Config,
        val limit: Int = 0
    ) : Widget()

    /**
     * @property [id] id of the widget
     * @property [source] source for this widget - one specific object associate with this widget.
     */
    data class Link(
        override val id: Id,
        override val source: Source,
        override val config: Config,
    ) : Widget()

    /**
     * @property [id] id of the widget
     * @property [source] source for this widget - one specific object associate with this widget.
     */
    data class List(
        override val id: Id,
        override val source: Source,
        override val config: Config,
        val isCompact: Boolean = false,
        val limit: Int = 0
    ) : Widget()

    data class View(
        override val id: Id,
        override val source: Source.Default,
        override val config: Config,
        val limit: Int
    ) : Widget()

    sealed class Source {

        abstract val id: Id
        abstract val type: Id?

        data class Default(
            val obj: ObjectWrapper.Basic
        ) : Source() {
            override val id: Id = obj.id
            override val type: Id? = obj.type.firstOrNull()
        }

        sealed class Bundled : Source() {
            data object Favorites : Bundled() {
                override val id: Id = BundledWidgetSourceIds.FAVORITE
                override val type: Id? = null
            }

            data object Sets : Bundled() {
                override val id: Id = BundledWidgetSourceIds.SETS
                override val type: Id? = null
            }

            data object Collections : Bundled() {
                override val id: Id = BundledWidgetSourceIds.COLLECTIONS
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
        }
    }
}

fun Widget.hasValidLayout(): Boolean = when (val widgetSource = source) {
    is Widget.Source.Default -> isSupportedForWidgets(widgetSource.obj.layout)
    is Widget.Source.Bundled -> true
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

fun List<Block>.parseWidgets(
    root: Id,
    details: Map<Id, Struct>,
    config: Config
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
                    val source = if (BundledWidgetSourceIds.ids.contains(target)) {
                        target.bundled()
                    } else {
                        Widget.Source.Default(ObjectWrapper.Basic(raw))
                    }
                    val hasValidSource = when(source) {
                        is Widget.Source.Bundled -> true
                        is Widget.Source.Default -> source.obj.isValid && source.obj.notDeletedNorArchived
                    }
                    if (hasValidSource && !WidgetConfig.excludedTypes.contains(source.type)) {
                        when (widgetContent.layout) {
                            Block.Content.Widget.Layout.TREE -> {
                                add(
                                    Widget.Tree(
                                        id = w.id,
                                        source = source,
                                        limit = widgetContent.limit,
                                        config = config
                                    )
                                )
                            }
                            Block.Content.Widget.Layout.LINK -> {
                                add(
                                    Widget.Link(
                                        id = w.id,
                                        source = source,
                                        config = config
                                    )
                                )
                            }
                            Block.Content.Widget.Layout.LIST -> {
                                add(
                                    Widget.List(
                                        id = w.id,
                                        source = source,
                                        limit = widgetContent.limit,
                                        config = config
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
                                        config = config
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
                                            config = config
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

fun Id.bundled() : Widget.Source.Bundled = when (this) {
    BundledWidgetSourceIds.RECENT -> Widget.Source.Bundled.Recent
    BundledWidgetSourceIds.RECENT_LOCAL -> Widget.Source.Bundled.RecentLocal
    BundledWidgetSourceIds.SETS -> Widget.Source.Bundled.Sets
    BundledWidgetSourceIds.COLLECTIONS -> Widget.Source.Bundled.Collections
    BundledWidgetSourceIds.FAVORITE -> Widget.Source.Bundled.Favorites
    else -> throw IllegalStateException("Widget bundled id can't be $this")
}

fun buildWidgetName(
    obj: ObjectWrapper.Basic,
    fieldParser: FieldParser
): Name {
    return if (obj.layout == ObjectType.Layout.DATE) {
        val timestamp = obj.getSingleValue<Double>(Relations.TIMESTAMP)?.toLong()
        if (timestamp != null) {
            Name.Date(
                relativeDate = fieldParser.calculateRelativeDate(timeStampInSeconds = timestamp)
            )
        } else {
            createDefaultName(obj, fieldParser)
        }
    } else {
        createDefaultName(obj, fieldParser)
    }
}

private fun createDefaultName(
    obj: ObjectWrapper.Basic,
    fieldParser: FieldParser
): Name.Default {
    return Name.Default(
        prettyPrintName = fieldParser.getObjectName(obj)
    )
}

typealias WidgetId = Id
typealias ViewId = Id
typealias FromIndex = Int
typealias ToIndex = Int
