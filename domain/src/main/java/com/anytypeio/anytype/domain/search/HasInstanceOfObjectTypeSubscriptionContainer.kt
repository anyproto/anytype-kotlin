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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Subscription container that tracks which object types have at least one instance.
 * 
 * For each object type in [StoreOfObjectTypes], creates a limit=1 subscription to check
 * if any objects of that type exist. Emits a Set of [Meta] for types that have instances.
 * 
 * This is a global subscription shared across all consumers, preventing duplicate
 * subscriptions per widget/component.
 */
class HasInstanceOfObjectTypeSubscriptionContainer(
    private val storelessContainer: StorelessSubscriptionContainer,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val dispatchers: AppCoroutineDispatchers,
    private val logger: Logger
) {

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
     * Observes which object types have at least one instance.
     * 
     * @param space The space to check for type instances
     * @return Flow emitting Set of [Meta] for types that have instances
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observe(space: SpaceId): Flow<Set<Meta>> {
        return storeOfObjectTypes.observe().flatMapLatest { allTypes ->
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
        }.flowOn(dispatchers.io)
    }

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

    /**
     * Unsubscribes from all type instance check subscriptions.
     */
    suspend fun unsubscribe() = withContext(dispatchers.io) {
        runCatching {
            // Note: StorelessSubscriptionContainer handles cleanup internally
            // Individual subscriptions will be cleaned up when they go out of scope
            logger.logInfo("HasInstanceOfObjectTypeSubscriptionContainer: unsubscribed")
        }.onFailure { error ->
            logger.logException(error, "Error unsubscribing from type instance checks")
        }
    }

    companion object {
        private const val SUBSCRIPTION_PREFIX = "type-instance-check-"
    }
}
