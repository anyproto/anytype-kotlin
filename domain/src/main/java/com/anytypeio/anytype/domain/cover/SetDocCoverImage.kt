package com.anytypeio.anytype.domain.cover

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Hash
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class SetDocCoverImage(
    private val repo: BlockRepository
) : BaseUseCase<Payload, SetDocCoverImage.Params>() {

    override suspend fun run(params: Params) = safe {
        when (params) {
            is Params.FromPath -> {
                val hash = repo.uploadFile(
                    command = Command.UploadFile(
                        path = params.path,
                        type = Block.Content.File.Type.IMAGE,
                        space = params.space
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
     */
    sealed class Params {
        abstract val context: String
        /**
         * @property path image path in file system
         * @property context id of the context for this operation
         * @property space target space
         */
        data class FromPath(
            override val context: String,
            val path: String,
            val space: SpaceId
        ) : Params()
        data class FromHash(
            override val context: String,
            val hash: Hash
        ) : Params()
    }
}