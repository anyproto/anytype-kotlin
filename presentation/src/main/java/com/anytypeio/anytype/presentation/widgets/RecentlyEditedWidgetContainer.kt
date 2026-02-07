package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.getTypeOfObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.spaces.GetSpaceView
import com.anytypeio.anytype.core_models.ui.objectIcon
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import timber.log.Timber

/**
 * Container for the Recently Edited section widget.
 * Displays objects sorted by last modified date.
 */
class RecentlyEditedWidgetContainer(
    private val widget: Widget.RecentlyEdited,
    private val storage: StorelessSubscriptionContainer,
    private val urlBuilder: UrlBuilder,
    private val isWidgetCollapsed: Flow<Boolean>,
    private val getSpaceView: GetSpaceView,
    private val fieldParser: FieldParser,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val isSessionActive: Flow<Boolean>,
    private val onRequestCache: () -> WidgetView.RecentlyEdited? = { null }
) : WidgetContainer {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val view: Flow<WidgetView> = isSessionActive.flatMapLatest { isActive ->
        if (isActive) {
            buildViewFlow().onStart {
                isWidgetCollapsed.take(1).collect { isCollapsed ->
                    val loadingStateView = createEmptyView(isCollapsed = isCollapsed)
                    if (isCollapsed) {
                        emit(loadingStateView)
                    } else {
                        emit(onRequestCache() ?: loadingStateView)
                    }
                }
            }
        } else {
            emptyFlow()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun buildViewFlow(): Flow<WidgetView> = isWidgetCollapsed.flatMapLatest { isCollapsed ->
        if (isCollapsed) {
            flowOf(createEmptyView(isCollapsed = true))
        } else {
            subscribeToRecentlyEdited()
        }
    }

    private suspend fun subscribeToRecentlyEdited(): Flow<WidgetView.RecentlyEdited> {
        val spaceView = getSpaceView.async(
            GetSpaceView.Params.BySpaceViewId(widget.config.spaceView)
        ).getOrNull()
        
        val spaceCreationDateInSeconds = spaceView
            ?.getValue<Double?>(Relations.CREATED_DATE)
            ?.toLong()

        val params = StoreSearchParams(
            space = SpaceId(widget.config.space),
            subscription = SUBSCRIPTION_ID,
            sorts = ObjectSearchConstants.sortTabRecent,
            filters = ObjectSearchConstants.filterTabRecent(
                spaceCreationDateInSeconds = spaceCreationDateInSeconds
            ),
            keys = ObjectSearchConstants.defaultKeys + listOf(Relations.DESCRIPTION),
            limit = LIMIT
        )

        return storage.subscribe(params)
            .map { objects -> buildWidgetView(objects) }
            .onCompletion {
                storage.unsubscribe(listOf(SUBSCRIPTION_ID))
            }
            .catch { e ->
                Timber.e(e, "Error subscribing to recently edited objects")
                emit(createEmptyView(isCollapsed = false))
            }
    }

    private suspend fun buildWidgetView(
        objects: List<ObjectWrapper.Basic>
    ): WidgetView.RecentlyEdited {
        val elements = objects.map { obj ->
            WidgetView.RecentlyEdited.Element(
                obj = obj,
                objectIcon = obj.objectIcon(
                    builder = urlBuilder,
                    objType = storeOfObjectTypes.getTypeOfObject(obj)
                ),
                name = buildWidgetName(
                    obj = obj,
                    fieldParser = fieldParser
                )
            )
        }

        return WidgetView.RecentlyEdited(
            id = widget.id,
            source = widget.source,
            elements = elements,
            isExpanded = true,
            icon = widget.icon,
            sectionType = widget.sectionType
        )
    }

    private fun createEmptyView(isCollapsed: Boolean): WidgetView.RecentlyEdited {
        return WidgetView.RecentlyEdited(
            id = widget.id,
            source = widget.source,
            elements = emptyList(),
            isExpanded = !isCollapsed,
            icon = widget.icon,
            sectionType = widget.sectionType
        )
    }

    companion object {
        private const val SUBSCRIPTION_ID = "subscription.widget.recently_edited"
        private const val LIMIT = 10
    }
}
