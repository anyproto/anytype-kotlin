package com.anytypeio.anytype.domain.chats

import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

/**
 * Container for subscribing to and observing all chat objects (Layout.CHAT_DERIVED).
 * Maintains a global subscription to chat objects across all spaces.
 */
interface ChatsDetailsSubscriptionContainer {

    fun start()
    fun stop()
    fun observe(): Flow<List<ObjectWrapper.Basic>>
    fun observe(chatId: Id): Flow<ObjectWrapper.Basic>

    fun get(): List<ObjectWrapper.Basic>
    fun get(chatId: Id): ObjectWrapper.Basic?

    class Default @Inject constructor(
        private val container: CrossSpaceSubscriptionContainer,
        private val scope: CoroutineScope,
        private val dispatchers: AppCoroutineDispatchers,
        private val awaitAccountStart: AwaitAccountStartManager,
        private val logger: Logger
    ) : ChatsDetailsSubscriptionContainer {

        private val data = MutableStateFlow<List<ObjectWrapper.Basic>>(emptyList())
        private val jobs = mutableListOf<Job>()

        init {
            logger.logInfo("ChatsDetailsSubscriptionContainer initialized")
            scope.launch {
                awaitAccountStart.state().collect { state ->
                    when (state) {
                        AwaitAccountStartManager.State.Init -> {
                            logger.logInfo("ChatsDetailsSubscriptionContainer, AwaitAccountStartManager.State.Init - waiting for account start")
                            // Do nothing
                        }

                        AwaitAccountStartManager.State.Started -> {
                            logger.logInfo("ChatsDetailsSubscriptionContainer, AwaitAccountStartManager.State.Started - starting chat details subscription")
                            start()
                        }

                        AwaitAccountStartManager.State.Stopped -> {
                            logger.logInfo("ChatsDetailsSubscriptionContainer, AwaitAccountStartManager.State.Stopped - stopping chat details subscription")
                            stop()
                        }
                    }
                }
            }
        }

        override fun observe(): Flow<List<ObjectWrapper.Basic>> {
            return data
        }

        override fun observe(chatId: Id): Flow<ObjectWrapper.Basic> {
            return data.mapNotNull { all ->
                all.firstOrNull { chat -> chat.id == chatId }
            }
        }

        override fun get(): List<ObjectWrapper.Basic> {
            return data.value
        }

        override fun get(chatId: Id): ObjectWrapper.Basic? {
            return data.value.find { chat -> chat.id == chatId }
        }

        override fun start() {
            logger.logInfo("Starting ChatsDetailsSubscriptionContainer")
            jobs += scope.launch(dispatchers.io) {
                proceedWithSubscription()
            }
        }

        private suspend fun proceedWithSubscription() {
            container.subscribe(
                CrossSpaceSearchParams(
                    subscription = GLOBAL_CHATS_SUBSCRIPTION,
                    keys = listOf(
                        Relations.ID,
                        Relations.NAME,
                        Relations.PLURAL_NAME
                    ),
                    filters = listOf(
                        DVFilter(
                            relation = Relations.LAYOUT,
                            value = listOf(ObjectType.Layout.CHAT_DERIVED.code.toDouble()),
                            condition = DVFilterCondition.IN
                        ),
                        DVFilter(
                            relation = Relations.IS_ARCHIVED,
                            condition = DVFilterCondition.NOT_EQUAL,
                            value = true
                        ),
                        DVFilter(
                            relation = Relations.IS_HIDDEN,
                            condition = DVFilterCondition.NOT_EQUAL,
                            value = true
                        ),
                        DVFilter(
                            relation = Relations.IS_DELETED,
                            condition = DVFilterCondition.NOT_EQUAL,
                            value = true
                        ),
                        DVFilter(
                            relation = Relations.IS_HIDDEN_DISCOVERY,
                            condition = DVFilterCondition.NOT_EQUAL,
                            value = true
                        )
                    )
                )
            ).catch { error ->
                logger.logException(
                    e = error,
                    msg = "Failed to subscribe to chats details"
                )
            }.collect {
                data.value = it
            }
        }

        override fun stop() {
            logger.logInfo("Stopping ChatsDetailsSubscriptionContainer")
            jobs.forEach { it.cancel() }
            scope.launch(dispatchers.io) {
                runCatching {
                    container.unsubscribe(GLOBAL_CHATS_SUBSCRIPTION)
                }.onFailure { error ->
                    logger.logException(
                        e = error,
                        msg = "Failed to unsubscribe from $GLOBAL_CHATS_SUBSCRIPTION"
                    )
                }
                    .onSuccess { logger.logInfo("Successfully unsubscribed from $GLOBAL_CHATS_SUBSCRIPTION") }
            }
        }

        companion object {
            const val GLOBAL_CHATS_SUBSCRIPTION = "global-chats-details-subscription"
        }
    }
}
