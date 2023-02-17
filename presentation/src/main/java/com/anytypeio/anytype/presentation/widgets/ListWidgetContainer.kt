package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.GetObject
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class ListWidgetContainer(
    private val widget: Widget.List,
    private val getObject: GetObject,
    private val storage: StorelessSubscriptionContainer,
    private val urlBuilder: UrlBuilder,
    activeView: Flow<Id?>,
    isWidgetCollapsed: Flow<Boolean>
) : WidgetContainer {

    override val view: Flow<WidgetView> = combine(
        activeView.distinctUntilChanged(),
        isWidgetCollapsed
    ) { view, isCollapsed -> Pair(view, isCollapsed) }.flatMapLatest { (view, isCollapsed) ->
        if (isCollapsed) {
            flowOf(
                WidgetView.Set(
                    id = widget.id,
                    obj = widget.source,
                    tabs = emptyList(),
                    elements = emptyList(),
                    isExpanded = false
                )
            )
        } else {
            val obj = getObject.run(widget.source.id)
            val params = obj.parse(viewer = view, source = widget.source)
            if (params != null) {
                storage.subscribe(params).map { objects ->
                    WidgetView.Set(
                        id = widget.id,
                        obj = widget.source,
                        tabs = obj.tabs(viewer = view),
                        elements = objects.map { obj ->
                            WidgetView.Set.Element(
                                obj = obj,
                                icon = ObjectIcon.from(
                                    obj = obj,
                                    layout = obj.layout,
                                    builder = urlBuilder
                                )
                            )
                        },
                        isExpanded = true
                    )
                }
            } else {
                flowOf(
                    WidgetView.Set(
                        id = widget.id,
                        obj = widget.source,
                        tabs = emptyList(),
                        elements = emptyList(),
                        isExpanded = true
                    )
                )
            }
        }
    }

    fun ObjectView.tabs(viewer: Id?): List<WidgetView.Set.Tab> = buildList {
        val block = blocks.find { it.content is DV }
        block?.content<DV>()?.viewers?.forEachIndexed { idx, view ->
            add(
                WidgetView.Set.Tab(
                    id = view.id,
                    name = view.name,
                    isSelected = if (viewer != null) view.id == viewer else idx == 0
                )
            )
        }
    }

    fun ObjectView.parse(
        source: ObjectWrapper.Basic,
        viewer: Id?
    ): StoreSearchParams? {
        val block = blocks.find { it.content is DV } ?: return null
        val dv = block.content<DV>()
        val view = dv.viewers.find { it.id == viewer } ?: dv.viewers.firstOrNull() ?: return null
        val dataViewKeys = dv.relationsIndex.map { it.key }
        val defaultKeys = ObjectSearchConstants.defaultDataViewKeys
        return StoreSearchParams(
            subscription = widget.id,
            sorts = view.sorts,
            keys = defaultKeys + dataViewKeys,
            filters = buildList {
                addAll(view.filters)
                addAll(ObjectSearchConstants.defaultDataViewFilters())
            },
            limit = MAX_COUNT,
            source = source.setOf
        )
    }

    companion object {
        const val MAX_COUNT = 3
    }
}
