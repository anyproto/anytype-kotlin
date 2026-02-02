package com.anytypeio.anytype.domain.search

import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.subscriptions.GlobalSubscription
import com.anytypeio.anytype.domain.workspace.SpaceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Manager for [HasInstanceOfObjectTypeSubscriptionContainer] that handles lifecycle
 * and integrates with [SpaceManager] state changes.
 * 
 * This manager ensures the subscription container is active only when a space is active,
 * and properly cleans up when switching spaces or when the app stops.
 */
class HasInstanceOfObjectTypeSubscriptionManager(
    private val scope: CoroutineScope = GlobalScope,
    private val container: HasInstanceOfObjectTypeSubscriptionContainer,
    private val spaceManager: SpaceManager
) : GlobalSubscription {

    private var job: Job? = null

    fun onStart() {
        job?.cancel()
        job = scope.launch {
            spaceManager.state().collect { state ->
                when (state) {
                    is SpaceManager.State.Space.Active -> {
                        // Active space: start observing type instances
                        container.start(space = SpaceId(state.config.space))
                    }
                    is SpaceManager.State.Space.Idle,
                    is SpaceManager.State.NoSpace -> {
                        // No active space: stop observing
                        container.stop()
                    }
                    is SpaceManager.State.Init -> {
                        // Init state: do nothing
                    }
                }
            }
        }
    }

    fun onStop() {
        container.stop()
        job?.cancel()
        job = null
    }
}
