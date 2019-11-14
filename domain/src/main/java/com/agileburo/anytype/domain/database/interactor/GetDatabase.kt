package com.agileburo.anytype.domain.database.interactor

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.database.DatabaseMock
import com.agileburo.anytype.domain.database.model.DatabaseView

class GetDatabase : BaseUseCase<DatabaseView, GetDatabase.Params>() {

    override suspend fun run(params: Params): Either<Throwable, DatabaseView> = try {
        DatabaseMock.getDatabaseView(id = params.id).let {
            Either.Right(it)
        }
    } catch (e: Throwable) {
        Either.Left(e)
    }

    class Params(val id: String)
}