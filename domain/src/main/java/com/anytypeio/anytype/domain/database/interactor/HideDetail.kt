package com.anytypeio.anytype.domain.database.interactor

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.database.model.Detail
import com.anytypeio.anytype.domain.database.repo.DatabaseRepository

class HideDetail(
    private val databaseRepo: DatabaseRepository
) : BaseUseCase<Unit, HideDetail.Params>() {

    override suspend fun run(params: Params): Either<Throwable, Unit> = try {
        databaseRepo.getDatabase(params.databaseId).let {
            val details = it.content.details

            val index = getDetailIndex(details, params.detailId)
            if (index == -1) {
                Either.Left(HideDetailError())
            } else {

                val detail: Detail = details[index]
                databaseRepo.updateDatabase(
                    it.copy(
                        content = it.content.copy(
                            details = details.toMutableList().apply {
                                set(index, changeShow(detail))
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

    private fun changeShow(detail: Detail): Detail =
        when (detail) {
            is Detail.Title -> detail.copy(show = !detail.show)
            is Detail.Text -> detail.copy(show = !detail.show)
            is Detail.Number -> detail.copy(show = !detail.show)
            is Detail.Date -> detail.copy(show = !detail.show)
            is Detail.Select -> detail.copy(show = !detail.show)
            is Detail.Multiple -> detail.copy(show = !detail.show)
            is Detail.Person -> detail.copy(show = !detail.show)
            is Detail.File -> detail.copy(show = !detail.show)
            is Detail.Bool -> detail.copy(show = !detail.show)
            is Detail.Link -> detail.copy(show = !detail.show)
            is Detail.Email -> detail.copy(show = !detail.show)
            is Detail.Phone -> detail.copy(show = !detail.show)
        }

    class Params(val databaseId: String, val detailId: String)
    class HideDetailError : Throwable()
}