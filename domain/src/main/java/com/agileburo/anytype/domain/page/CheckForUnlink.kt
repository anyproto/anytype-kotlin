package com.agileburo.anytype.domain.page

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 2020-01-23.
 */
/**
 * Use-case for unlinking urls from text.
 */

class CheckForUnlink : BaseUseCase<Boolean, CheckForUnlink.Params>() {

    override suspend fun run(params: Params): Either<Throwable, Boolean> = try {
        if (params.link.isNullOrEmpty()) {
            Either.Left(NothingToUnlinkException())
        } else {
            Either.Right(true)
        }
    } catch (e: Throwable) {
        Either.Left(e)
    }

    class Params(val link: String?)
}

class NothingToUnlinkException : Exception("No text to unlink")