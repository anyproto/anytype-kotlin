package com.anytypeio.anytype.domain.vault

import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import javax.inject.Inject

/**
 * Marks the create space badge as seen device-wide.
 * This is a one-time flag per device, not per account.
 * Called when the user taps the "Create a new space" button.
 */
class SetCreateSpaceBadgeSeen @Inject constructor(
    private val settings: UserSettingsRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Unit, Unit>(dispatchers.io) {

    override suspend fun doWork(params: Unit) {
        settings.setHasSeenCreateSpaceBadge(true)
    }
}