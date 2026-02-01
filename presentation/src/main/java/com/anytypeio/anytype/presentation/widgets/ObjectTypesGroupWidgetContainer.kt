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
        Timber.d("ObjectTypesGroupWidget[${widget.id}]: isSessionActive=$isActive")
        if (isActive) {
            buildViewFlow().onStart {
                isWidgetCollapsed.take(1).collect { isCollapsed ->
                    Timber.d("ObjectTypesGroupWidget[${widget.id}]: Initial state - isCollapsed=$isCollapsed")
                    val loadingStateView = WidgetView.ObjectTypesGroup(
                        id = widget.id,
                        types = emptyList(),
                        isExpanded = !isCollapsed,
                        sectionType = widget.sectionType
                    )
                    if (isCollapsed) {
                        Timber.d("ObjectTypesGroupWidget[${widget.id}]: Emitting collapsed view")
                        emit(loadingStateView)
                    } else {
                        val cached = onRequestCache()
                        Timber.d("ObjectTypesGroupWidget[${widget.id}]: Emitting initial view (cached=${cached != null})")
                        emit(cached ?: loadingStateView)
                    }
                }
            }
        } else {
            Timber.d("ObjectTypesGroupWidget[${widget.id}]: Session inactive, emitting empty flow")
            emptyFlow()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun buildViewFlow() = isWidgetCollapsed.flatMapLatest { isCollapsed ->
        Timber.d("ObjectTypesGroupWidget[${widget.id}]: buildViewFlow - isCollapsed=$isCollapsed")
        if (isCollapsed) {
            Timber.d("ObjectTypesGroupWidget[${widget.id}]: Widget collapsed, emitting empty view")
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
            Timber.d("ObjectTypesGroupWidget[${widget.id}]: Widget expanded, tracking type changes")
            storeOfObjectTypes.trackChanges().flatMapLatest { change ->
                Timber.d("ObjectTypesGroupWidget[${widget.id}]: Type store changed: $change")
                buildTypesViewWithSubscriptions()
            }.catch { e ->
                Timber.e(e, "ObjectTypesGroupWidget[${widget.id}]: Error building view")
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
        Timber.d("ObjectTypesGroupWidget[${widget.id}]: buildTypesViewWithSubscriptions started")
        Timber.d("ObjectTypesGroupWidget[${widget.id}]: widget.typeIds = ${widget.typeIds}")
        
        // Get all types from store (needs to be done in suspend context)
        val allTypes = mutableListOf<ObjectWrapper.Type>()
        val typeIdsList: List<Id> = widget.typeIds
        for (typeId: Id in typeIdsList) {
            val type: ObjectWrapper.Type? = storeOfObjectTypes.get(typeId)
            Timber.d("ObjectTypesGroupWidget[${widget.id}]: Looking up typeId=$typeId, found=${type != null}, name=${type?.name}")
            if (type != null) {
                allTypes.add(type)
            }
        }

        if (allTypes.isEmpty()) {
            Timber.d("ObjectTypesGroupWidget[${widget.id}]: No types found for typeIds=${widget.typeIds}")
            return flowOf(
                WidgetView.ObjectTypesGroup(
                    id = widget.id,
                    types = emptyList(),
                    isExpanded = true,
                    sectionType = widget.sectionType
                )
            )
        }
        
        Timber.d("ObjectTypesGroupWidget[${widget.id}]: Found ${allTypes.size} types: ${allTypes.map { "${it.name}(${it.id})" }}")

        // Create a subscription for each type to track object existence
        // This is efficient: N types (usually 5-20) vs potentially thousands of objects
        Timber.d("ObjectTypesGroupWidget[${widget.id}]: Creating ${allTypes.size} subscriptions")
        val typeSubscriptions: List<Flow<Pair<String, Boolean>>> = allTypes.map { typeObj ->
            val typeId = typeObj.id
            val subscriptionId = "${widget.id}_type_$typeId"
            Timber.d("ObjectTypesGroupWidget[${widget.id}]: Creating subscription $subscriptionId for type ${typeObj.name}")
            storelessSubscriptionContainer.subscribe(
                StoreSearchParams(
                    subscription = subscriptionId,
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
                val hasObjects = objects.isNotEmpty()
                Timber.d("ObjectTypesGroupWidget[${widget.id}]: Subscription $subscriptionId returned ${objects.size} objects, hasObjects=$hasObjects")
                typeId to hasObjects
            }
        }

        // Combine all type subscriptions
        Timber.d("ObjectTypesGroupWidget[${widget.id}]: Combining ${typeSubscriptions.size} subscription flows")
        return combine(typeSubscriptions) { hasObjectsArray: Array<Pair<String, Boolean>> ->
            val hasObjectsMap: Map<String, Boolean> = hasObjectsArray.toMap()
            Timber.d("ObjectTypesGroupWidget[${widget.id}]: combine() called with hasObjectsMap=$hasObjectsMap")
            
            // Build type rows for types that have objects
            val typeRows = allTypes
                .filter { typeObj -> 
                    val hasObjects = hasObjectsMap[typeObj.id] == true
                    Timber.d("ObjectTypesGroupWidget[${widget.id}]: Type ${typeObj.name} (${typeObj.id}) hasObjects=$hasObjects")
                    hasObjects
                }
                .map { typeObj ->
                    val typeRow = WidgetView.ObjectTypesGroup.TypeRow(
                        id = typeObj.id,
                        icon = typeObj.objectIcon(),
                        name = buildWidgetName(
                            obj = ObjectWrapper.Basic(typeObj.map),
                            fieldParser = fieldParser
                        )
                    )
                    Timber.d("ObjectTypesGroupWidget[${widget.id}]: Created TypeRow: id=${typeRow.id}, name=${typeRow.name}")
                    typeRow
                }
            
            Timber.d("ObjectTypesGroupWidget[${widget.id}]: Built view with ${typeRows.size} type rows (total types: ${allTypes.size})")
            Timber.d("ObjectTypesGroupWidget[${widget.id}]: Type rows: ${typeRows.map { "${it.name}(${it.id})" }}")

            val view = WidgetView.ObjectTypesGroup(
                id = widget.id,
                types = typeRows,
                isExpanded = true,
                sectionType = widget.sectionType
            )
            Timber.d("ObjectTypesGroupWidget[${widget.id}]: Emitting view with ${view.types.size} types, isExpanded=${view.isExpanded}")
            view
        }
    }
}
