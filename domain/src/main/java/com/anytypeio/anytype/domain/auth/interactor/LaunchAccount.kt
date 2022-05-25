package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.config.FeaturesConfigProvider
import com.anytypeio.anytype.domain.device.PathProvider
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

/**
 * Sets current account for current application session.
 */
class LaunchAccount(
    private val repository: AuthRepository,
    private val pathProvider: PathProvider,
    private val context: CoroutineContext = Dispatchers.IO,
    private val featuresConfigProvider: FeaturesConfigProvider
) : BaseUseCase<String, BaseUseCase.None>(context) {

    override suspend fun run(params: None) = try {
        repository.startAccount(
            id = repository.getCurrentAccountId(),
            path = pathProvider.providePath()
        ).let { pair ->
            val (account, config, status) = pair
            repository.updateAccount(account)
            featuresConfigProvider.set(
                enableDataView = config.enableDataView ?: false,
                enableDebug = config.enableDebug ?: false,
                enableChannelSwitch = config.enableChannelSwitch ?: false,
                enableSpaces = config.enableSpaces ?: false
            )
            Either.Right(account.id)
        }
    } catch (e: Throwable) {
        Either.Left(e)
    }
}