package com.anytypeio.anytype.presentation.widgets

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.ui.objectIcon
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.primitives.FieldParser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import timber.log.Timber

/**
 * Container for the grouped object types widget.
 * This widget displays object types as a simple navigation list - no nested objects are fetched.
 * 
 * Uses per-type subscriptions for reactive object existence tracking:
 * - More efficient than all-objects subscription (N types vs thousands of objects)
 * - Each subscription only fetches object IDs
 * - Automatically updates when objects are created/deleted
 * 
 * @property widget the ObjectTypesGroup widget model
 * @property storeOfObjectTypes store for accessing object type metadata
 * @property storelessSubscriptionContainer for per-type subscriptions
 * @property isWidgetCollapsed flow indicating if the widget is collapsed
 * @property fieldParser for parsing object names
 * @property isSessionActive flow indicating if the widget session is active
 * @property onRequestCache optional callback to retrieve cached widget view
 */
class ObjectTypesGroupWidgetContainer(
    private val widget: Widget.ObjectTypesGroup,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
    private val isWidgetCollapsed: Flow<Boolean>,
    private val fieldParser: FieldParser,
    isSessionActive: Flow<Boolean>,
    onRequestCache: () -> WidgetView.ObjectTypesGroup? = { null }
) : WidgetContainer {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val view: Flow<WidgetView> = isSessionActive.flatMapLatest { isActive ->
        if (isActive) {
            buildViewFlow().onStart {
                isWidgetCollapsed.take(1).collect { isCollapsed ->
                    val loadingStateView = WidgetView.ObjectTypesGroup(
                        id = widget.id,
                        types = emptyList(),
                        isExpanded = !isCollapsed,
                        sectionType = widget.sectionType
                    )
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
    private fun buildViewFlow() = isWidgetCollapsed.flatMapLatest { isCollapsed ->
        if (isCollapsed) {
            flowOf(
                WidgetView.ObjectTypesGroup(
                    id = widget.id,
                    types = emptyList(),
                    isExpanded = false,
                    sectionType = widget.sectionType
                )
            )
        } else {
            // React to type metadata changes
            storeOfObjectTypes.trackChanges().flatMapLatest {
                buildTypesViewWithSubscriptions()
            }.catch { e ->
                Timber.e(e, "Error building object types group view")
                emit(
                    WidgetView.ObjectTypesGroup(
                        id = widget.id,
                        types = emptyList(),
                        isExpanded = true,
                        sectionType = widget.sectionType
                    )
                )
            }
        }
    }

    /**
     * Builds the types view with per-type subscriptions.
     * More efficient than subscribing to all objects since users have few types but many objects.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun buildTypesViewWithSubscriptions(): Flow<WidgetView.ObjectTypesGroup> {
        // Get all types from store (needs to be done in suspend context)
        val allTypes = mutableListOf<ObjectWrapper.Type>()
        val typeIdsList: List<Id> = widget.typeIds
        for (typeId: Id in typeIdsList) {
            val type: ObjectWrapper.Type? = storeOfObjectTypes.get(typeId)
            if (type != null) {
                allTypes.add(type)
            }
        }

        if (allTypes.isEmpty()) {
            Timber.d("ObjectTypesGroupWidget: No types found for typeIds=${widget.typeIds}")
            return flowOf(
                WidgetView.ObjectTypesGroup(
                    id = widget.id,
                    types = emptyList(),
                    isExpanded = true,
                    sectionType = widget.sectionType
                )
            )
        }

        // Create a subscription for each type to track object existence
        // This is efficient: N types (usually 5-20) vs potentially thousands of objects
        val typeSubscriptions: List<Flow<Pair<String, Boolean>>> = allTypes.map { typeObj ->
            val typeId = typeObj.id
            storelessSubscriptionContainer.subscribe(
                StoreSearchParams(
                    subscription = "${widget.id}_type_$typeId",
                    space = SpaceId(widget.config.space),
                    keys = listOf(Relations.ID), // Only need ID to check existence
                    filters = listOf(
                        DVFilter(
                            relation = Relations.TYPE,
                            condition = DVFilterCondition.EQUAL,
                            value = typeId
                        ),
                        DVFilter(
                            relation = Relations.IS_ARCHIVED,
                            condition = DVFilterCondition.NOT_EQUAL,
                            value = true
                        ),
                        DVFilter(
                            relation = Relations.IS_DELETED,
                            condition = DVFilterCondition.NOT_EQUAL,
                            value = true
                        ),
                        DVFilter(
                            relation = Relations.TYPE,
                            condition = DVFilterCondition.NOT_EQUAL,
                            value = ObjectTypeIds.TEMPLATE
                        )
                    )
                )
            ).map { objects: List<ObjectWrapper.Basic> ->
                typeId to objects.isNotEmpty()
            }
        }

        // Combine all type subscriptions
        return combine(typeSubscriptions) { hasObjectsArray: Array<Pair<String, Boolean>> ->
            val hasObjectsMap: Map<String, Boolean> = hasObjectsArray.toMap()
            
            // Build type rows for types that have objects
            val typeRows = allTypes
                .filter { typeObj -> hasObjectsMap[typeObj.id] == true }
                .map { typeObj ->
                    WidgetView.ObjectTypesGroup.TypeRow(
                        id = typeObj.id,
                        icon = typeObj.objectIcon(),
                        name = buildWidgetName(
                            obj = ObjectWrapper.Basic(typeObj.map),
                            fieldParser = fieldParser
                        )
                    )
                }
            
            Timber.d("ObjectTypesGroupWidget: Built view with ${typeRows.size} type rows (total types: ${allTypes.size}, hasObjectsMap: ${hasObjectsMap})")

            WidgetView.ObjectTypesGroup(
                id = widget.id,
                types = typeRows,
                isExpanded = true,
                sectionType = widget.sectionType
            )
        }
    }
}
