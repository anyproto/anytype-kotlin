package com.anytypeio.anytype.domain.page

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.core_models.Id

/**
 * Use-case for updating document's title
 */
class UpdateTitle(
    private val repo: BlockRepository
) : BaseUseCase<Unit, UpdateTitle.Params>() {

    override suspend fun run(params: Params) = try {
        repo.updateDocumentTitle(
            command = Command.UpdateTitle(
                context = params.context,
                title = params.title
            )
        ).let {
            Either.Right(it)
        }
    } catch (t: Throwable) {
        Either.Left(t)
    }

    /**
     * Params for updating document's title
     * @property context id of the context
     * @property title new title for the document
     */
    data class Params(
        val context: Id,
        val title: String
    )
}