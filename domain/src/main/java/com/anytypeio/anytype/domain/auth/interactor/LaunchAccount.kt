package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.device.PathProvider
import com.anytypeio.anytype.domain.launch.PreferredSpaceIdHolder
import com.anytypeio.anytype.domain.platform.InitialParamsProvider
import com.anytypeio.anytype.domain.workspace.SpaceManager
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers

/**
 * Sets current account for current application session.
 */
class LaunchAccount @Inject constructor(
    private val repository: AuthRepository,
    private val pathProvider: PathProvider,
    private val configStorage: ConfigStorage,
    private val spaceManager: SpaceManager,
    private val initialParamsProvider: InitialParamsProvider,
    private val settings: UserSettingsRepository,
    private val awaitAccountStartManager: AwaitAccountStartManager,
    private val preferredSpaceIdHolder: PreferredSpaceIdHolder,
    context: CoroutineContext = Dispatchers.IO,
    private val clock: () -> Long = { System.currentTimeMillis() / 1000 }
    ) : BaseUseCase<Pair<String, String>, BaseUseCase.None>(context) {

    override suspend fun run(params: None) = safe {
        expireLastOpenedSpaceRouteIfStale()

        repository.setInitialParams(initialParamsProvider.toCommand())

        val networkMode = repository.getNetworkMode()

        val currentAccountId = repository.getCurrentAccountId()

        val preferredSpaceId = preferredSpaceIdHolder.consume()
            ?: settings.getCurrentSpace()?.id

        val command = Command.AccountSelect(
            id = currentAccountId,
            path = pathProvider.providePath(),
            networkMode = networkMode.networkMode,
            networkConfigFilePath = networkMode.storedFilePath,
            preferredSpaceId = preferredSpaceId
        )

        repository.selectAccount(command).let { setup ->
            repository.updateAccount(setup.account)
            configStorage.set(config = setup.config, accountId = currentAccountId)
            val lastSessionSpace = settings.getCurrentSpace()
            if (lastSessionSpace != null) {
                spaceManager.set(lastSessionSpace.id)
            }
            awaitAccountStartManager.setState(AwaitAccountStartManager.State.Started)
            setup.config.analytics to setup.config.network
        }
    }

    /**
     * Drops the stored "last opened space" once it is older than
     * [LAST_SPACE_ROUTE_TTL_SECONDS], so a user coming back later lands on the vault
     * instead of wherever they happened to close the app.
     *
     * Done here, before the space is read, because three things downstream depend on
     * it: AccountSelect.preferredSpaceId (heart's lazy-load hint), spaceManager, and
     * the splash route. Expiring only the route would leave heart deferring every
     * other space for a space we then never open.
     *
     * A missing stamp means a fresh install or an upgrade from a build that never
     * wrote one — treated as not expired, so updating does not bounce people to the
     * vault once for no reason. A stamp in the future (clock moved backwards) is
     * likewise treated as not expired.
     */
    private suspend fun expireLastOpenedSpaceRouteIfStale() {
        val backgroundedAt = settings.getLastBackgroundedAt() ?: return
        val elapsed = clock() - backgroundedAt
        if (elapsed > LAST_SPACE_ROUTE_TTL_SECONDS) {
            settings.clearCurrentSpace()
        }
    }

    companion object {
        const val LAST_SPACE_ROUTE_TTL_SECONDS = 60L * 60L
    }
}