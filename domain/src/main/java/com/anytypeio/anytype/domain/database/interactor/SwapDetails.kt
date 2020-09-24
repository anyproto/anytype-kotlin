package com.anytypeio.anytype.domain.database.interactor

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.database.repo.DatabaseRepository

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 2019-12-19.
 */
class SwapDetails(
    private val databaseRepo: DatabaseRepository
) : BaseUseCase<Unit, SwapDetails.Params>() {

    override suspend fun run(params: Params): Either<Throwable, Unit> = try {

        databaseRepo.getDatabase(params.databaseId).let {
            val updatedDetails = it.content.details.toMutableList()
            updatedDetails.swap(params.from, params.to)
            databaseRepo.updateDatabase(
                it.copy(
                    content = it.content.copy(
                        details = updatedDetails.toList()
                    )
                )
            )
            Either.Right(Unit)
        }
    } catch (e: Throwable) {
        Either.Left(e)
    }

    data class Params(val from: Int, val to: Int, val databaseId: String)
}

fun <T> MutableList<T>.swap(index1: Int, index2: Int) {
    val tmp = this[index1] // 'this' corresponds to the list
    this[index1] = this[index2]
    this[index2] = tmp
}