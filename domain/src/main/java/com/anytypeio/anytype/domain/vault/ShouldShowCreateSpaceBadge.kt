package com.anytypeio.anytype.domain.vault

import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.ResultInteractor
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import javax.inject.Inject

/**
 * Determines whether the blue dot badge should be shown on the "Create a new space" button.
 * The badge is shown after the introduction popup is dismissed until the user taps the button.
 */
class ShouldShowCreateSpaceBadge @Inject constructor(
    private val settings: UserSettingsRepository,
    private val auth: AuthRepository,
    dispatchers: AppCoroutineDispatchers
) : ResultInteractor<Unit, Boolean>(dispatchers.io) {

    override suspend fun doWork(params: Unit): Boolean {
        val account = auth.getCurrentAccount()

        // Check if introduction has been shown
        val hasShownIntroduction = settings.getHasShownSpacesIntroduction(account)

        // Check if user has already seen the badge (clicked the button)
        val hasSeenBadge = settings.getHasSeenCreateSpaceBadge(account)

        // Show badge only if introduction was shown but user hasn't clicked the button yet
        return hasShownIntroduction && !hasSeenBadge
    }
}