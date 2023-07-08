package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.GetObject
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class DataViewListWidgetContainer(
    private val widget: Widget.List,
    private val workspace: Id,
    private val getObject: GetObject,
    private val storage: StorelessSubscriptionContainer,
    private val urlBuilder: UrlBuilder,
    private val activeView: Flow<Id?>,
    private val isWidgetCollapsed: Flow<Boolean>,
    isSessionActive: Flow<Boolean>
) : WidgetContainer {

    override val view = isSessionActive.flatMapLatest { isActive ->
        if (isActive)
            buildViewFlow()
        else
            emptyFlow()
    }

    private fun buildViewFlow() = combine(
        activeView.distinctUntilChanged(),
        isWidgetCollapsed
    ) { view, isCollapsed -> view to isCollapsed }.flatMapLatest { (view, isCollapsed) ->
        when (val source = widget.source) {
            is Widget.Source.Bundled -> throw IllegalStateException("Bundled widgets do not support data view layout")
            is Widget.Source.Default -> {
                if (isCollapsed) {
                    flowOf(
                        WidgetView.SetOfObjects(
                            id = widget.id,
                            source = widget.source,
                            tabs = emptyList(),
                            elements = emptyList(),
                            isExpanded = false,
                            isCompact = widget.isCompact
                        )
                    )
                } else {
                    val obj = getObject.run(widget.source.id)
                    val params = obj.parse(viewer = view, source = source.obj)
                    if (params != null) {
                        storage.subscribe(params).map { objects ->
                            WidgetView.SetOfObjects(
                                id = widget.id,
                                source = widget.source,
                                tabs = obj.tabs(viewer = view),
                                elements = objects.map { obj ->
                                    WidgetView.SetOfObjects.Element(
                                        obj = obj,
                                        objectIcon = obj.widgetElementIcon(urlBuilder)
                                    )
                                },
                                isExpanded = true,
                                isCompact = widget.isCompact
                            )
                        }
                    } else {
                        flowOf(defaultEmptyState())
                    }
                }
            }
        }
    }.catch {
        emit(defaultEmptyState())
    }

    private fun defaultEmptyState() = WidgetView.SetOfObjects(
        id = widget.id,
        source = widget.source,
        tabs = emptyList(),
        elements = emptyList(),
        isExpanded = true,
        isCompact = widget.isCompact
    )

    fun ObjectView.tabs(viewer: Id?): List<WidgetView.SetOfObjects.Tab> = buildList {
        val block = blocks.find { it.content is DV }
        block?.content<DV>()?.viewers?.forEachIndexed { idx, view ->
            add(
                WidgetView.SetOfObjects.Tab(
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
        if (source.isArchived == true || source.isDeleted == true) return null
        val block = blocks.find { it.content is DV } ?: return null
        val dv = block.content<DV>()
        val view = dv.viewers.find { it.id == viewer } ?: dv.viewers.firstOrNull() ?: return null
        val dataViewKeys = dv.relationLinks.map { it.key }
        val defaultKeys = ObjectSearchConstants.defaultDataViewKeys
        return StoreSearchParams(
            subscription = widget.id,
            sorts = view.sorts,
            keys = buildList {
                addAll(defaultKeys)
                addAll(dataViewKeys)
                add(Relations.DESCRIPTION)
            }.distinct(),
            filters = buildList {
                addAll(view.filters)
                addAll(ObjectSearchConstants.defaultDataViewFilters(workspace))
                add(
                    DVFilter(
                        relation = Relations.TYPE,
                        condition = DVFilterCondition.NOT_IN,
                        value = listOf(
                            ObjectTypeIds.OBJECT_TYPE,
                            ObjectTypeIds.RELATION,
                            ObjectTypeIds.TEMPLATE,
                            ObjectTypeIds.IMAGE,
                            ObjectTypeIds.FILE,
                            ObjectTypeIds.VIDEO,
                            ObjectTypeIds.AUDIO,
                            ObjectTypeIds.DASHBOARD,
                            ObjectTypeIds.RELATION_OPTION,
                            ObjectTypeIds.DASHBOARD,
                            ObjectTypeIds.DATE
                        )
                    ),
                )
            },
            limit = resolveLimit(),
            source = source.setOf,
            collection = if (source.type.contains(ObjectTypeIds.COLLECTION))
                source.id
            else
                null
        )
    }

    private fun resolveLimit(): Int = WidgetConfig.resolveListWidgetLimit(
        isCompact = widget.isCompact,
        limit = widget.limit
    )
}
