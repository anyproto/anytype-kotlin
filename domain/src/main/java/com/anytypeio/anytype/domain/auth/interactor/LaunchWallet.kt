package com.anytypeio.anytype.domain.auth.interactor

import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.device.PathProvider
import kotlinx.coroutines.withTimeout

/**
 * Sets current wallet for current application session.
 */
class LaunchWallet(
    private val repository: AuthRepository,
    private val pathProvider: PathProvider
) : BaseUseCase<Unit, BaseUseCase.None>() {

    override suspend fun run(params: None) = try {
        withTimeout(TIMEOUT_DURATION) {
            repository.recoverWallet(
                mnemonic = repository.getMnemonic(),
                path = pathProvider.providePath()
            ).let {
                Either.Right(it)
            }
        }
    } catch (e: Throwable) {
        Either.Left(e)
    }

    companion object {
        const val TIMEOUT_DURATION = 10000L
    }
}