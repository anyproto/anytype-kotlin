package com.anytypeio.anytype.domain.launch

import com.anytypeio.anytype.domain.auth.interactor.PreloadRemainingSpaces
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Fires AccountPreloadRemainingSpaces exactly once per process, after a short
 * delay following the moment the cold-start route's destination screen is
 * reached. Idempotent: safe to call [scheduleOnce] from multiple navigation
 * paths. Heart's own 10s timer is the correctness backstop, so a swallowed
 * failure only loses optimization.
 */
@Singleton
class RemainingSpacesPreloader @Inject constructor(
    private val preloadRemainingSpaces: PreloadRemainingSpaces
) {
    private val triggered = AtomicBoolean(false)

    fun scheduleOnce(scope: CoroutineScope, delayMillis: Long = DEFAULT_DELAY_MILLIS) {
        if (!triggered.compareAndSet(false, true)) return
        scope.launch {
            delay(delayMillis)
            preloadRemainingSpaces.async(Unit)
        }
    }

    /**
     * Resets the once-per-process guard so a subsequent cold start within the
     * same process (e.g. logout then login) preloads remaining spaces again.
     */
    fun reset() {
        triggered.set(false)
    }

    companion object {
        const val DEFAULT_DELAY_MILLIS = 2_000L
    }
}
