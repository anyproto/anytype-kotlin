package com.agileburo.anytype.domain.image

import com.agileburo.anytype.domain.auth.model.Account
import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either

/**
 * Loads images for one or several accounts
 */
class LoadAccountImages(
    private val loader: ImageLoader
) : BaseUseCase<Map<Account, ByteArray?>, LoadAccountImages.Params>() {

    override suspend fun run(params: Params) = try {
        params.accounts.associateWith { account ->
            account.avatar?.let { avatar ->
                avatar.smallest?.let { size ->
                    try {
                        loader.load(avatar.id, size)
                    } catch (t: Throwable) {
                        null
                    }
                }
            }
        }.let { Either.Right(it) }
    } catch (t: Throwable) {
        Either.Left(t)
    }


    class Params(val accounts: List<Account>)
}