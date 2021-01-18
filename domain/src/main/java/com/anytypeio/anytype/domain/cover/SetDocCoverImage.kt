package com.anytypeio.anytype.domain.cover

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.model.Block
import com.anytypeio.anytype.domain.block.model.Command
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.common.Hash
import com.anytypeio.anytype.domain.event.model.Payload

class SetDocCoverImage(
    private val repo: BlockRepository
) : BaseUseCase<Payload, SetDocCoverImage.Params>() {

    override suspend fun run(params: Params) = safe {
        when (params) {
            is Params.FromPath -> {
                val hash = repo.uploadFile(
                    command = Command.UploadFile(
                        path = params.path,
                        type = Block.Content.File.Type.IMAGE
                    )
                )
                repo.setDocumentCoverImage(
                    ctx = params.context,
                    hash = hash
                )
            }
            is Params.FromHash -> {
                repo.setDocumentCoverImage(
                    ctx = params.context,
                    hash = params.hash
                )
            }
        }
    }

    /**
     * Params for for setting document's image cover
     * @property path image path in file system
     * @property context id of the context for this operation
     */
    sealed class Params {
        abstract val context: String

        data class FromPath(override val context: String, val path: String) : Params()
        data class FromHash(override val context: String, val hash: Hash) : Params()
    }
}