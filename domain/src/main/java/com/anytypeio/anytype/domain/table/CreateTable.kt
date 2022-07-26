package com.anytypeio.anytype.domain.table

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.block.repo.BlockRepository

class CreateTable(
    private val repo: BlockRepository
) : BaseUseCase<Payload, CreateTable.Params>() {

    override suspend fun run(params: Params): Either<Throwable, Payload> = safe {
        repo.createTable(
            ctx = params.ctx,
            target = params.target,
            position = params.position,
            rowCount = params.rowCount ?: DEFAULT_ROW_COUNT,
            columnCount = params.columnCount ?: DEFAULT_COLUMN_COUNT
        )
    }

    data class Params(
        val ctx: Id,
        val target: Id,
        val position: Position,
        val rowCount: Int?,
        val columnCount: Int?
    )

    companion object {
        const val DEFAULT_ROW_COUNT = 3
        const val DEFAULT_COLUMN_COUNT = 3

        const val DEFAULT_MAX_ROW_COUNT = 25
        const val DEFAULT_MAX_COLUMN_COUNT = 25
    }
}