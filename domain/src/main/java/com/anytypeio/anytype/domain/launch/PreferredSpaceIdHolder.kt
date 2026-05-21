package com.anytypeio.anytype.domain.launch

import java.util.concurrent.atomic.AtomicReference

/**
 * Holds the space id the user is cold-starting into (deeplink / chat push),
 * so [com.anytypeio.anytype.domain.auth.interactor.LaunchAccount] can pass it
 * as AccountSelect.preferredSpaceId and heart can defer loading other spaces.
 *
 * Set as early and synchronously as possible by the cold-start entry point;
 * consumed exactly once by LaunchAccount. In-memory, app-scoped singleton.
 */
interface PreferredSpaceIdHolder {

    fun set(spaceId: String)

    /** Returns the held value and clears it. Null if nothing was set. */
    fun consume(): String?

    fun clear()

    object Default : PreferredSpaceIdHolder {
        private val ref = AtomicReference<String?>(null)

        override fun set(spaceId: String) {
            ref.set(spaceId)
        }

        override fun consume(): String? = ref.getAndSet(null)

        override fun clear() {
            ref.set(null)
        }
    }
}
