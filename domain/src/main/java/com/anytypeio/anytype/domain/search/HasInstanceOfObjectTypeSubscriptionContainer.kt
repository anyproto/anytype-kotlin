package com.anytypeio.anytype.domain.search

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * Subscription container that tracks which object types have at least one instance.
 * 
 * For each object type in [StoreOfObjectTypes], creates a limit=1 subscription to check
 * if any objects of that type exist. Maintains a state of [Meta] for types that have instances.
 * 
 * This is a global subscription shared across all consumers, preventing duplicate
 * subscriptions per widget/component.
 * 
 * Lifecycle:
 * - [start] begins observing type instances for a space
 * - [stop] stops observing and clears state
 * - [get] returns current state synchronously
 * - [observe] returns Flow to observe state changes
 */
class HasInstanceOfObjectTypeSubscriptionContainer(
    private val storelessContainer: StorelessSubscriptionContainer,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val dispatchers: AppCoroutineDispatchers,
    private val logger: Logger,
    private val scope: CoroutineScope
) {

    private val _state = MutableStateFlow<Set<Meta>>(emptySet())
    private var job: Job? = null
    private val activeSubscriptions = mutableSetOf<String>()

    /**
     * Metadata for an object type with both ID and unique key.
     * 
     * @property id The object type ID
     * @property uniqueKey The object type unique key (e.g., "ot-page")
     */
    data class Meta(
        val id: Id,
        val uniqueKey: String
    )

    /**
     * Starts observing which object types have at least one instance for the given space.
     * Updates the internal state which can be accessed via [get] or [observe].
     * 
     * @param space The space to check for type instances
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun start(space: SpaceId) {
        stop() // Stop any existing job and clear subscriptions
        
        job = scope.launch(dispatchers.io) {
            storeOfObjectTypes.observe().flatMapLatest { allTypes ->
                if (allTypes.isEmpty()) {
                    flowOf(emptySet())
                } else {
                    // Create a subscription for each type to check if it has instances
                    val typeCheckFlows = allTypes.map { type ->
                        checkTypeHasInstances(
                            space = space,
                            typeId = type.id,
                            typeUniqueKey = type.uniqueKey
                        )
                    }
                    
                    // Combine all flows into a single Set of Meta with instances
                    combine(typeCheckFlows) { results ->
                        results.filterNotNull().toSet()
                    }
                }
            }.catch { error ->
                logger.logException(error, "Error observing type instances")
                emit(emptySet())
            }.collect { typesWithInstances ->
                _state.value = typesWithInstances
            }
        }
    }

    /**
     * Stops observing type instances and clears the state.
     * Blocks until all subscriptions are unsubscribed to ensure cleanup is complete.
     */
    fun stop() {
        job?.cancel()
        job = null
        
        // Unsubscribe from all active subscriptions synchronously
        if (activeSubscriptions.isNotEmpty()) {
            runBlocking(dispatchers.io) {
                runCatching {
                    storelessContainer.unsubscribe(activeSubscriptions.toList())
                }.onFailure { error ->
                    logger.logException(error, "Error unsubscribing from type instance checks")
                }
            }
            activeSubscriptions.clear()
        }
        
        _state.value = emptySet()
    }

    /**
     * Gets the current state of types with instances.
     * 
     * @return Set of [Meta] for types that have instances
     */
    fun get(): Set<Meta> = _state.value

    /**
     * Observes changes to types with instances.
     * 
     * @return Flow emitting Set of [Meta] for types that have instances
     */
    fun observe(): StateFlow<Set<Meta>> = _state.asStateFlow()

    /**
     * Creates a subscription to check if a specific type has any instances.
     * 
     * @param space The space to check
     * @param typeId The ID of the type
     * @param typeUniqueKey The unique key of the type to check
     * @return Flow emitting [Meta] if instances exist, null otherwise
     */
    private fun checkTypeHasInstances(
        space: SpaceId,
        typeId: Id,
        typeUniqueKey: String
    ): Flow<Meta?> {
        val subscriptionId = "$SUBSCRIPTION_PREFIX$typeUniqueKey"
        activeSubscriptions.add(subscriptionId)
        return storelessContainer.subscribe(
            StoreSearchParams(
                space = space,
                subscription = subscriptionId,
                filters = listOf(
                    DVFilter(
                        relation = Relations.TYPE,
                        condition = DVFilterCondition.EQUAL,
                        value = typeId
                    )
                ),
                sorts = emptyList(),
                keys = listOf(Relations.ID),
                limit = 1
            )
        ).map { objects ->
            if (objects.isNotEmpty()) {
                Meta(id = typeId, uniqueKey = typeUniqueKey)
            } else {
                null
            }
        }
    }


    companion object {
        private const val SUBSCRIPTION_PREFIX = "type-instance-check-"
    }
}
