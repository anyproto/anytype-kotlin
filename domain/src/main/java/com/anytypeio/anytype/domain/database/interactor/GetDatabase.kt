package com.anytypeio.anytype.domain.database.interactor

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.database.DatabaseMock
import com.anytypeio.anytype.domain.database.model.DatabaseView
import com.anytypeio.anytype.domain.database.repo.DatabaseRepository

class GetDatabase(
    private val databaseRepo: DatabaseRepository
) : BaseUseCase<DatabaseView, GetDatabase.Params>() {

    override suspend fun run(params: Params): Either<Throwable, DatabaseView> = try {
        databaseRepo.getDatabase(params.id).let {
            Either.Right(it)
        }

    } catch (e: Throwable) {
        Either.Left(e)
    }

    class Params(val id: String)
}