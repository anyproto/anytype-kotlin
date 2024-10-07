package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.ObjectWatcher
import com.anytypeio.anytype.domain.spaces.GetSpaceView
import com.anytypeio.anytype.presentation.mapper.objectIcon
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants.collectionsSorts
import com.anytypeio.anytype.presentation.search.Subscriptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import timber.log.Timber

class ListWidgetContainer(
    private val widget: Widget.List,
    private val subscription: Id,
    private val storage: StorelessSubscriptionContainer,
    private val urlBuilder: UrlBuilder,
    private val isWidgetCollapsed: Flow<Boolean>,
    private val objectWatcher: ObjectWatcher,
    private val getSpaceView: GetSpaceView,
    isSessionActive: Flow<Boolean>,
    onRequestCache: () -> WidgetView.ListOfObjects? = { null }
) : WidgetContainer {

    override val view: Flow<WidgetView> = isSessionActive.flatMapLatest { isActive ->
        if (isActive)
            buildViewFlow().onStart {
                isWidgetCollapsed.take(1).collect { isCollapsed ->
                    val loadingStateView = WidgetView.ListOfObjects(
                        id = widget.id,
                        source = widget.source,
                        type = resolveType(),
                        elements = emptyList(),
                        isExpanded = !isCollapsed,
                        isCompact = widget.isCompact,
                        isLoading = true
                    )
                    if (isCollapsed) {
                        emit(loadingStateView)
                    } else {
                        emit(onRequestCache() ?: loadingStateView)
                    }
                }
            }
        else
            emptyFlow()
    }

    private fun buildViewFlow() = isWidgetCollapsed.flatMapLatest { isCollapsed ->
        if (isCollapsed) {
            flowOf(
                WidgetView.ListOfObjects(
                    id = widget.id,
                    source = widget.source,
                    type = resolveType(),
                    elements = emptyList(),
                    isExpanded = false,
                    isCompact = widget.isCompact
                )
            )
        } else {
            when (subscription) {
                BundledWidgetSourceIds.FAVORITE -> {
                    // Objects from favorites have custom sorting logic.
                    objectWatcher
                        .watch(widget.config.home)
                        .map { obj -> obj.orderOfRootObjects(obj.root) }
                        .catch {
                            Timber.e(it)
                            emit(emptyMap())
                        }
                        .flatMapLatest { order ->
                            storage.subscribe(
                                StoreSearchByIdsParams(
                                    subscription = subscription,
                                    keys = keys,
                                    targets = order.keys
                                        .sortedBy { key -> order[key] }
                                        .take(resolveLimit()),
                                )
                            ).map { objects ->
                                buildWidgetViewWithElements(
                                    objects = objects
                                        .filter { obj ->
                                            obj.isArchived != true && obj.isDeleted != true
                                        }
                                        .sortedBy { obj -> order[obj.id] }
                                )
                            }
                        }
                        .catch {
                            Timber.e(it, "Failed to load favorite objects")
                        }
                }
                BundledWidgetSourceIds.RECENT -> {
                    val spaceView = getSpaceView.async(
                        GetSpaceView.Params.BySpaceViewId(widget.config.spaceView)
                    ).getOrNull()
                    val spaceViewCreationDate = spaceView
                        ?.getValue<Double?>(Relations.CREATED_DATE)
                        ?.toLong()
                    storage.subscribe(
                        buildParams(
                            spaceCreationDateInSeconds = spaceViewCreationDate
                        )
                    ).map { objects ->
                        buildWidgetViewWithElements(
                            objects = objects
                        )
                    }
                }
                else -> {
                    storage.subscribe(buildParams()).map { objects ->
                        buildWidgetViewWithElements(
                            objects = objects
                        )
                    }
                }
            }
        }
    }

    private fun buildWidgetViewWithElements(
        objects: List<ObjectWrapper.Basic>,
    ) = WidgetView.ListOfObjects(
        id = widget.id,
        source = widget.source,
        type = resolveType(),
        elements = objects.map { obj ->
            WidgetView.ListOfObjects.Element(
                obj = obj,
                objectIcon = obj.objectIcon(
                    builder = urlBuilder
                )
            )
        },
        isExpanded = true,
        isCompact = widget.isCompact
    )

    private fun buildParams(
        customFavoritesOrder: List<Id> = emptyList(),
        spaceCreationDateInSeconds: Long? = null
    ) = params(
        subscription = subscription,
        spaces = buildList {
            add(widget.config.space)
            add(widget.config.techSpace)
        },
        keys = keys,
        limit = resolveLimit(),
        customFavoritesOrder = customFavoritesOrder,
        spaceCreationDateInSeconds = spaceCreationDateInSeconds
    )

    private fun resolveType() = when (subscription) {
        BundledWidgetSourceIds.RECENT -> WidgetView.ListOfObjects.Type.Recent
        BundledWidgetSourceIds.RECENT_LOCAL -> WidgetView.ListOfObjects.Type.RecentLocal
        BundledWidgetSourceIds.SETS -> WidgetView.ListOfObjects.Type.Sets
        BundledWidgetSourceIds.FAVORITE -> WidgetView.ListOfObjects.Type.Favorites
        BundledWidgetSourceIds.COLLECTIONS -> WidgetView.ListOfObjects.Type.Collections
        else -> throw IllegalStateException("Unexpected subscription: $subscription")
    }

    private fun resolveLimit(): Int = WidgetConfig.resolveListWidgetLimit(
        isCompact = widget.isCompact,
        limit = widget.limit
    )

    companion object {

        fun params(
            subscription: Id,
            spaces: List<Id>,
            keys: List<Id>,
            limit: Int,
            customFavoritesOrder: List<Id> = emptyList(),
            spaceCreationDateInSeconds: Long? = null
        ) : StoreSearchParams = when (subscription) {
            BundledWidgetSourceIds.RECENT -> {
                StoreSearchParams(
                    subscription = subscription,
                    sorts = ObjectSearchConstants.sortTabRecent,
                    filters = ObjectSearchConstants.filterTabRecent(
                        spaces = spaces,
                        spaceCreationDateInSeconds = spaceCreationDateInSeconds
                    ),
                    keys = keys,
                    limit = limit
                )
            }
            BundledWidgetSourceIds.RECENT_LOCAL -> {
                StoreSearchParams(
                    subscription = subscription,
                    sorts = ObjectSearchConstants.sortTabRecentLocal,
                    filters = ObjectSearchConstants.filterTabRecentLocal(spaces),
                    keys = keys,
                    limit = limit
                )
            }
            BundledWidgetSourceIds.SETS -> {
                StoreSearchParams(
                    subscription = subscription,
                    sorts = ObjectSearchConstants.sortTabSets,
                    filters = ObjectSearchConstants.filterTabSets(spaces),
                    keys = keys,
                    limit = limit
                )
            }
            BundledWidgetSourceIds.FAVORITE -> {
                StoreSearchParams(
                    subscription = subscription,
                    sorts = buildList {
                        if (customFavoritesOrder.isNotEmpty()) {
                            add(
                                DVSort(
                                    relationKey = Relations.ID,
                                    type = DVSortType.CUSTOM,
                                    customOrder = customFavoritesOrder,
                                    relationFormat = RelationFormat.OBJECT
                                )
                            )
                        }
                    },
                    filters = ObjectSearchConstants.filterTabFavorites(spaces),
                    keys = keys,
                    limit = limit
                )
            }
            BundledWidgetSourceIds.COLLECTIONS -> {
                StoreSearchParams(
                    subscription = subscription,
                    sorts = collectionsSorts,
                    filters = ObjectSearchConstants.collectionFilters(spaces),
                    keys = keys,
                    limit = limit
                )
            }
            Subscriptions.SUBSCRIPTION_ARCHIVED -> {
                StoreSearchParams(
                    subscription = subscription,
                    sorts = ObjectSearchConstants.sortTabArchive,
                    filters = ObjectSearchConstants.filterTabArchive(spaces),
                    keys = keys,
                    limit = limit
                )
            }
            else -> throw IllegalStateException("Unexpected subscription: $subscription")
        }

        val keys = buildList {
            addAll(ObjectSearchConstants.defaultKeys)
            add(Relations.DESCRIPTION)
        }
    }
}

fun ObjectView.orderOfRootObjects(root: Id) : Map<Id, Int> {
    val parent = blocks.find { it.id == root }
    return if (parent != null) {
        val order = parent.children.withIndex().associate { (index, id) -> id to index }
        buildMap {
            blocks.forEach { block ->
                val content = block.content
                if (order.containsKey(block.id) && content is Block.Content.Link) {
                    put(content.target, order.getValue(block.id))
                }
            }
        }
    } else {
        emptyMap()
    }
}