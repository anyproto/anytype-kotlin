package com.anytypeio.anytype.domain.multiplayer

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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

/**
 * Container for subscribing to and observing all participant objects (Layout.PARTICIPANT)
 * across all spaces. Maintains a global subscription to participant objects.
 */
interface ParticipantSubscriptionContainer {

    fun start()
    fun stop()
    fun observe(): Flow<List<ObjectWrapper.SpaceMember>>
    fun observe(identity: Id): Flow<ObjectWrapper.SpaceMember>
    
    /**
     * Observes all participants and maps to a property using the provided mapper.
     * Applies distinctUntilChanged to only emit when the mapped value changes.
     * 
     * @param keys List of relation keys being observed (informational, for documentation)
     * @param mapper Function to extract the desired property from the participants list
     * @return Flow that emits only when the mapped value changes
     */
    fun <T> observe(
        keys: List<String>,
        mapper: (List<ObjectWrapper.SpaceMember>) -> T
    ): Flow<T>
    
    /**
     * Observes a specific participant and maps to a property using the provided mapper.
     * Applies distinctUntilChanged to only emit when the mapped value changes.
     * 
     * @param identity The participant identity to observe
     * @param keys List of relation keys being observed (informational, for documentation)
     * @param mapper Function to extract the desired property from the participant
     * @return Flow that emits only when the mapped value changes
     */
    fun <T> observe(
        identity: Id,
        keys: List<String>,
        mapper: (ObjectWrapper.SpaceMember) -> T
    ): Flow<T>

    fun get(): List<ObjectWrapper.SpaceMember>
    fun get(identity: Id): ObjectWrapper.SpaceMember?

    class Default @Inject constructor(
        private val container: CrossSpaceSubscriptionContainer,
        private val scope: CoroutineScope,
        private val dispatchers: AppCoroutineDispatchers,
        private val awaitAccountStart: AwaitAccountStartManager,
        private val logger: Logger
    ) : ParticipantSubscriptionContainer {

        private val data = MutableStateFlow<List<ObjectWrapper.SpaceMember>>(emptyList())
        private val jobs = mutableListOf<Job>()

        init {
            logger.logInfo("ParticipantSubscriptionContainer initialized")
            scope.launch {
                awaitAccountStart.state().collect { state ->
                    when (state) {
                        AwaitAccountStartManager.State.Init -> {
                            logger.logInfo("ParticipantSubscriptionContainer, AwaitAccountStartManager.State.Init - waiting for account start")
                            // Do nothing
                        }

                        AwaitAccountStartManager.State.Started -> {
                            logger.logInfo("ParticipantSubscriptionContainer, AwaitAccountStartManager.State.Started - starting participant subscription")
                            start()
                        }

                        AwaitAccountStartManager.State.Stopped -> {
                            logger.logInfo("ParticipantSubscriptionContainer, AwaitAccountStartManager.State.Stopped - stopping participant subscription")
                            stop()
                        }
                    }
                }
            }
        }

        override fun observe(): Flow<List<ObjectWrapper.SpaceMember>> {
            return data
        }

        override fun observe(identity: Id): Flow<ObjectWrapper.SpaceMember> {
            return data.mapNotNull { all ->
                all.firstOrNull { participant -> participant.identity == identity }
            }
        }

        override fun get(): List<ObjectWrapper.SpaceMember> {
            return data.value
        }

        override fun get(identity: Id): ObjectWrapper.SpaceMember? {
            return data.value.find { participant -> participant.identity == identity }
        }
        
        override fun <T> observe(
            keys: List<String>,
            mapper: (List<ObjectWrapper.SpaceMember>) -> T
        ): Flow<T> {
            return observe()
                .map(mapper)
                .distinctUntilChanged()
        }
        
        override fun <T> observe(
            identity: Id,
            keys: List<String>,
            mapper: (ObjectWrapper.SpaceMember) -> T
        ): Flow<T> {
            return observe(identity)
                .map(mapper)
                .distinctUntilChanged()
        }

        override fun start() {
            logger.logInfo("Starting ParticipantSubscriptionContainer")
            jobs += scope.launch(dispatchers.io) {
                proceedWithSubscription()
            }
        }

        private suspend fun proceedWithSubscription() {
            container.subscribe(
                CrossSpaceSearchParams(
                    subscription = GLOBAL_PARTICIPANTS_SUBSCRIPTION,
                    keys = listOf(
                        Relations.ID,
                        Relations.SPACE_ID,
                        Relations.IDENTITY,
                        Relations.IDENTITY_PROFILE_LINK,
                        Relations.NAME,
                        Relations.GLOBAL_NAME,
                        Relations.PARTICIPANT_STATUS,
                        Relations.PARTICIPANT_PERMISSIONS
                    ),
                    filters = listOf(
                        DVFilter(
                            relation = Relations.LAYOUT,
                            value = listOf(ObjectType.Layout.PARTICIPANT.code.toDouble()),
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
            ).map { basics ->
                basics.map { basic -> ObjectWrapper.SpaceMember(basic.map) }
            }.catch { error ->
                logger.logException(
                    e = error,
                    msg = "Failed to subscribe to participants"
                )
            }.collect {
                data.value = it
            }
        }

        override fun stop() {
            logger.logInfo("Stopping ParticipantSubscriptionContainer")
            jobs.forEach { it.cancel() }
            jobs.clear()
            scope.launch(dispatchers.io) {
                runCatching {
                    container.unsubscribe(GLOBAL_PARTICIPANTS_SUBSCRIPTION)
                }.onFailure { error ->
                    logger.logException(
                        e = error,
                        msg = "Failed to unsubscribe from $GLOBAL_PARTICIPANTS_SUBSCRIPTION"
                    )
                }
                    .onSuccess { logger.logInfo("Successfully unsubscribed from $GLOBAL_PARTICIPANTS_SUBSCRIPTION") }
            }
        }

        companion object {
            const val GLOBAL_PARTICIPANTS_SUBSCRIPTION = "global-participants-subscription"
        }
    }
}
