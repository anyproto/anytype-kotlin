package com.agileburo.anytype.domain.launch

import com.agileburo.anytype.domain.auth.repo.AuthRepository
import com.agileburo.anytype.domain.auth.repo.PathProvider
import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either

/**
 * Sets current wallet for current application session.
 */
class LaunchWallet(
    private val repository: AuthRepository,
    private val pathProvider: PathProvider
) : BaseUseCase<Unit, BaseUseCase.None>() {

    override suspend fun run(params: None) = try {
        repository.recoverWallet(
            mnemonic = repository.getMnemonic(),
            path = pathProvider.providePath()
        ).let {
            Either.Right(it)
        }
    } catch (e: Throwable) {
        Either.Left(e)
    }
}