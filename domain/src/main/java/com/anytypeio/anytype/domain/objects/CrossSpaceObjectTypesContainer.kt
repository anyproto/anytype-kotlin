package com.anytypeio.anytype.domain.objects

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.library.CrossSpaceSearchParams
import com.anytypeio.anytype.domain.library.CrossSpaceSubscriptionContainer
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Vault-wide object-types store: one cross-space subscription over all spaces'
 * type objects, kept for the app's lifetime. Unlike [StoreOfObjectTypes], which
 * re-subscribes per current space, this container serves cross-space search:
 * type captions on cross-space result rows, the global Types aggregate
 * (grouped by [Relations.UNIQUE_KEY]) and foreign-space type suggestions —
 * all resolved from memory, never per result row.
 *
 * Hidden types are deliberately kept in the store (a hidden instance in one
 * space must not suppress the visible aggregate group) — consumers filter at
 * aggregation time.
 */
interface CrossSpaceObjectTypesContainer {

    fun start()
    fun stop()

    fun observe(): Flow<List<ObjectWrapper.Type>>

    fun get(): List<ObjectWrapper.Type>

    class Default @Inject constructor(
        private val container: CrossSpaceSubscriptionContainer,
        private val scope: CoroutineScope,
        private val dispatchers: AppCoroutineDispatchers,
        private val awaitAccountStart: AwaitAccountStartManager,
        private val logger: Logger
    ) : CrossSpaceObjectTypesContainer {

        private val data = MutableStateFlow<List<ObjectWrapper.Type>>(emptyList())

        // Both fields are confined to the single account-state collector in
        // [init] — start/stop are never invoked concurrently.
        private var collector: Job? = null
        private var teardown: Job? = null

        init {
            awaitAccountStart.state().onEach { state ->
                when (state) {
                    AwaitAccountStartManager.State.Init -> {
                        // Waiting for account start
                    }
                    AwaitAccountStartManager.State.Started -> start()
                    AwaitAccountStartManager.State.Stopped -> stop()
                }
            }.launchIn(scope)
        }

        override fun observe(): Flow<List<ObjectWrapper.Type>> = data

        override fun get(): List<ObjectWrapper.Type> = data.value

        override fun start() {
            logger.logInfo("Starting CrossSpaceObjectTypesContainer")
            val pendingTeardown = teardown
            collector = scope.launch(dispatchers.io) {
                // A restart must never race the previous stop's unsubscribe —
                // it would tear the fresh subscription down at the middleware.
                pendingTeardown?.join()
                container.subscribe(
                    CrossSpaceSearchParams(
                        subscription = GLOBAL_TYPES_SUBSCRIPTION,
                        keys = listOf(
                            Relations.ID,
                            Relations.SPACE_ID,
                            Relations.UNIQUE_KEY,
                            Relations.NAME,
                            Relations.PLURAL_NAME,
                            Relations.ICON_EMOJI,
                            Relations.ICON_NAME,
                            Relations.ICON_OPTION,
                            Relations.RECOMMENDED_LAYOUT,
                            Relations.LAST_USED_DATE,
                            Relations.IS_HIDDEN
                        ),
                        filters = listOf(
                            DVFilter(
                                relation = Relations.LAYOUT,
                                value = listOf(ObjectType.Layout.OBJECT_TYPE.code.toDouble()),
                                condition = DVFilterCondition.IN
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
                            )
                        )
                    )
                ).catch { error ->
                    logger.logException(
                        e = error,
                        msg = "Failed to subscribe to cross-space object types"
                    )
                }.collect { objects ->
                    data.value = objects.map { ObjectWrapper.Type(it.map) }
                }
            }
        }

        override fun stop() {
            logger.logInfo("Stopping CrossSpaceObjectTypesContainer")
            val active = collector
            collector = null
            teardown = scope.launch(dispatchers.io) {
                // Cancel-and-join BEFORE clearing: a mapped batch already past
                // its cancellation check must not land after the clear and
                // leave the previous account's types in an app-wide store.
                active?.cancelAndJoin()
                data.value = emptyList()
                runCatching {
                    container.unsubscribe(GLOBAL_TYPES_SUBSCRIPTION)
                }.onFailure { error ->
                    logger.logException(
                        e = error,
                        msg = "Failed to unsubscribe from $GLOBAL_TYPES_SUBSCRIPTION"
                    )
                }
            }
        }

        companion object {
            const val GLOBAL_TYPES_SUBSCRIPTION = "global-object-types-subscription"
        }
    }
}
