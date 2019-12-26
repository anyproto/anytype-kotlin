package com.agileburo.anytype.domain.database.interactor

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.database.model.Detail
import com.agileburo.anytype.domain.database.repo.DatabaseRepository
import java.util.*

class DuplicateDetail(
    private val databaseRepo: DatabaseRepository
) : BaseUseCase<Unit, DuplicateDetail.Params>() {

    override suspend fun run(params: Params): Either<Throwable, Unit> = try {
        databaseRepo.getDatabase(params.databaseId).let {
            val details = it.content.details

            val index = getDetailIndex(details, params.detailId)
            if (index == -1) {
                Either.Left(DuplicateDetailError())
            } else {
                val detail: Detail = details[index]

                databaseRepo.updateDatabase(
                    it.copy(
                        content = it.content.copy(
                            details = details.toMutableList().apply {
                                add(duplicate(detail))
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

    private fun duplicate(detail: Detail): Detail =
        when (detail) {
            is Detail.Title -> detail.copy(id = UUID.randomUUID().toString())
            is Detail.Text -> detail.copy(id = UUID.randomUUID().toString())
            is Detail.Number -> detail.copy(id = UUID.randomUUID().toString())
            is Detail.Date -> detail.copy(id = UUID.randomUUID().toString())
            is Detail.Select -> detail.copy(id = UUID.randomUUID().toString())
            is Detail.Multiple -> detail.copy(id = UUID.randomUUID().toString())
            is Detail.Person -> detail.copy(id = UUID.randomUUID().toString())
            is Detail.File -> detail.copy(id = UUID.randomUUID().toString())
            is Detail.Bool -> detail.copy(id = UUID.randomUUID().toString())
            is Detail.Link -> detail.copy(id = UUID.randomUUID().toString())
            is Detail.Email -> detail.copy(id = UUID.randomUUID().toString())
            is Detail.Phone -> detail.copy(id = UUID.randomUUID().toString())
        }

    class Params(val databaseId: String, val detailId: String)
    class DuplicateDetailError : Throwable()
}