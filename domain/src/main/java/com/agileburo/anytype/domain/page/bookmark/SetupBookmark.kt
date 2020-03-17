package com.agileburo.anytype.domain.page.bookmark

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.model.Command
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.common.Id

/**
 * Use-case for setting up (i.e. fetching) a bookmark from url.
 */
class SetupBookmark(
    private val repo: BlockRepository
) : BaseUseCase<Unit, SetupBookmark.Params>() {

    override suspend fun run(params: Params) = try {
        repo.setupBookmark(
            command = Command.SetupBookmark(
                context = params.context,
                target = params.target,
                url = params.url
            )
        ).let {
            Either.Right(it)
        }
    } catch (t: Throwable) {
        Either.Left(t)
    }

    /**
     * Params for setting up a bookmark from [url]
     * @property context id of the context
     * @property target id of the target block (future bookmark block)
     * @property url bookmark url
     */
    data class Params(
        val context: Id,
        val target: Id,
        val url: String
    )
}