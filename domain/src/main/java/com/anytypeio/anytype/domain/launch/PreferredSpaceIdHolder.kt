package com.anytypeio.anytype.domain.launch

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
        @Volatile
        private var spaceId: String? = null

        override fun set(spaceId: String) {
            this.spaceId = spaceId
        }

        override fun consume(): String? {
            val current = spaceId
            spaceId = null
            return current
        }

        override fun clear() {
            spaceId = null
        }
    }
}
