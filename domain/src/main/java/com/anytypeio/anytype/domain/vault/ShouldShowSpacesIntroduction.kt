package com.anytypeio.anytype.domain.vault

import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.workspace.SpaceManager
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Determines whether the spaces introduction popup should be shown.
 * The popup is shown only once for existing users (users who have at least one space)
 * on the first app launch after the update.
 */
class ShouldShowSpacesIntroduction @Inject constructor(
    private val settings: UserSettingsRepository,
    private val auth: AuthRepository,
    private val spaceManager: SpaceManager,
    private val spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Unit, Boolean>(dispatchers.io) {

    override suspend fun doWork(params: Unit): Boolean {
        val account = auth.getCurrentAccount()

        // Check if we've already shown the introduction
        val hasShown = settings.getHasShownSpacesIntroduction(account)
        if (hasShown) {
            return false
        }

        // Only show for existing users (users who already have spaces)
        // Wait for space subscription to be ready
        val spaces = spaceViewSubscriptionContainer.observe().first()
        val hasSpaces = spaces.isNotEmpty()

        return hasSpaces
    }
}