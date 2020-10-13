package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.device.PathProvider
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

/**
 * Sets current account for current application session.
 */
class LaunchAccount(
    private val repository: AuthRepository,
    private val pathProvider: PathProvider,
    private val context: CoroutineContext = Dispatchers.IO
) : BaseUseCase<String, BaseUseCase.None>(context) {

    override suspend fun run(params: None) = try {
        repository.startAccount(
            id = repository.getCurrentAccountId(),
            path = pathProvider.providePath()
        ).let { account ->
            repository.updateAccount(account)
            Either.Right(account.id)
        }
    } catch (e: Throwable) {
        Either.Left(e)
    }
}