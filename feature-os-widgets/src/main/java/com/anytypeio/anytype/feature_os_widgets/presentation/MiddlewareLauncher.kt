package com.anytypeio.anytype.feature_os_widgets.presentation

import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.auth.interactor.LaunchAccount
import com.anytypeio.anytype.domain.auth.interactor.LaunchWallet
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber

/**
 * Max time to wait for the space subscription to deliver its first real emission
 * before switching to live collection. Guards against the "No spaces available"
 * flash caused by the container's backing StateFlow starting with [emptyList]
 * and only getting populated after middleware resolves the subscription.
 */
private const val SPACE_SUBSCRIPTION_WARMUP_MS = 2_000L

/**
 * Skips the initial [emptyList] from [spaces] and waits (up to
 * [SPACE_SUBSCRIPTION_WARMUP_MS]) for the first real emission. Callers should
 * start collecting normally after this returns.
 */
suspend fun awaitFirstSpacesEmission(
    spaces: Flow<List<ObjectWrapper.SpaceView>>
) {
    withTimeoutOrNull(SPACE_SUBSCRIPTION_WARMUP_MS) {
        spaces.first { it.isNotEmpty() }
    }
}

/**
 * Result of launching middleware for an OS widget config activity.
 */
sealed class MiddlewareLaunchResult {
    data object Success : MiddlewareLaunchResult()
    data class Failure(val message: String) : MiddlewareLaunchResult()
}

/**
 * Starts middleware (wallet + account) for self-contained OS widget config activities.
 *
 * OS widget config activities may launch when the main app is cold, so each one must
 * bring up middleware itself before subscribing to data.
 */
suspend fun launchMiddlewareForConfig(
    tag: String,
    launchWallet: LaunchWallet,
    launchAccount: LaunchAccount
): MiddlewareLaunchResult {
    Timber.d("$tag launching wallet...")
    when (val walletResult = launchWallet(BaseUseCase.None)) {
        is Either.Left -> {
            Timber.e(walletResult.a, "$tag wallet launch failed")
            return MiddlewareLaunchResult.Failure(
                "Failed to start: ${walletResult.a.message}"
            )
        }
        is Either.Right -> Timber.d("$tag wallet launched")
    }
    Timber.d("$tag launching account...")
    when (val accountResult = launchAccount(BaseUseCase.None)) {
        is Either.Left -> {
            Timber.e(accountResult.a, "$tag account launch failed")
            return MiddlewareLaunchResult.Failure(
                "Failed to start: ${accountResult.a.message}"
            )
        }
        is Either.Right -> Timber.d("$tag account launched")
    }
    return MiddlewareLaunchResult.Success
}
