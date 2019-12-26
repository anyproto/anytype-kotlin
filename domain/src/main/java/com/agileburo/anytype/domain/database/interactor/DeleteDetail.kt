package com.agileburo.anytype.domain.database.interactor

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.database.model.Detail
import com.agileburo.anytype.domain.database.repo.DatabaseRepository

class DeleteDetail(
    private val databaseRepo: DatabaseRepository
) : BaseUseCase<Unit, DeleteDetail.Params>() {

    override suspend fun run(params: Params): Either<Throwable, Unit> = try {
        databaseRepo.getDatabase(params.databaseId).let {
            val details = it.content.details

            val index = getDetailIndex(details, params.detailId)
            if (index == -1) {
                Either.Left(DeleteDetailError())
            } else {
                databaseRepo.updateDatabase(
                    it.copy(
                        content = it.content.copy(
                            details = details.toMutableList().apply {
                                removeAt(index)
                            })
                    )
                )
                Either.Right(Unit)
            }
        }

    } catch (e: Throwable) {
        Either.Left(e)
    }

    private fun getDetailIndex(details: List<Detail>, id: String): Int =
        details.indexOfFirst { it.id == id }

    class Params(val databaseId: String, val detailId: String)
    class DeleteDetailError: Throwable()
}