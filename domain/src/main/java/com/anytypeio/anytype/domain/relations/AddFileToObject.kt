package com.anytypeio.anytype.domain.relations

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.ext.addIds
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class AddFileToObject(
    private val repo: BlockRepository
) : BaseUseCase<Payload, AddFileToObject.Params>() {

    override suspend fun run(params: Params): Either<Throwable, Payload> = safe {
        val file = repo.uploadFile(
            command = Command.UploadFile(
                path = params.path,
                type = null,
                space = params.space
            )
        )
        val obj = params.obj
        val remaining = obj[params.relation].addIds(listOf(file.id))
        repo.setObjectDetail(
            ctx = params.ctx,
            key = params.relation,
            value = remaining
        )
    }

    class Params(
        val ctx: Id,
        val relation: Id,
        val obj: Map<String, Any?>,
        val path: String,
        val space: SpaceId
    )
}