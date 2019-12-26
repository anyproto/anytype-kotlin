package com.agileburo.anytype.domain.database.interactor

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.database.DatabaseMock
import com.agileburo.anytype.domain.database.model.DatabaseView
import com.agileburo.anytype.domain.database.repo.DatabaseRepository

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