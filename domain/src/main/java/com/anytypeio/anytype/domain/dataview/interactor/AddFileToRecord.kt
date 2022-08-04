package com.anytypeio.anytype.domain.dataview.interactor

import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ext.addIds
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.repo.BlockRepository

@Deprecated("Part of soon-to-be-deprecated API")
class AddFileToRecord(
    private val repo: BlockRepository
) : BaseUseCase<Unit, AddFileToRecord.Params>() {

    override suspend fun run(params: Params): Either<Throwable, Unit> = safe {
        val hash = repo.uploadFile(
            command = Command.UploadFile(
                path = params.path,
                type = null
            )
        )
        val files = params.value[params.relation].addIds(listOf(hash))
        val updated = mapOf(params.relation to files)
        repo.updateDataViewRecord(
            context = params.context,
            target = params.target,
            record = params.record,
            values = updated
        )
    }

    class Params(
        val context: Id,
        val target: Id,
        val path: String,
        val record: Id,
        val relation: Id,
        val value: Map<String, Any?>
    )
}