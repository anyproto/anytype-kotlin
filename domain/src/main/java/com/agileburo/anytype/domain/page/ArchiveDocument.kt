package com.agileburo.anytype.domain.page

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.block.model.Command
import com.agileburo.anytype.domain.block.repo.BlockRepository
import com.agileburo.anytype.domain.common.Id

/**
 * Use-case for archiving a document
 */
class ArchiveDocument(
    private val repo: BlockRepository
) : BaseUseCase<Unit, ArchiveDocument.Params>() {

    override suspend fun run(params: Params) = try {
        repo.archiveDocument(
            command = Command.ArchiveDocument(
                context = params.context,
                target = params.targets,
                isArchived = params.isArchived
            )
        ).let {
            Either.Right(it)
        }
    } catch (t: Throwable) {
        Either.Left(t)
    }

    /**
     * Params for archiving a document
     * @property context id of the context
     * @property targets list of ids of the targets (document we want to change archive status)
     * @property isArchived defines whether we archived or unarchive our document
     */
    data class Params(
        val context: Id,
        val targets: List<Id>,
        val isArchived: Boolean
    )
}