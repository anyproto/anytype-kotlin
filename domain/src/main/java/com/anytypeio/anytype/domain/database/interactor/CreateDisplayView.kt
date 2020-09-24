package com.anytypeio.anytype.domain.database.interactor

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.database.model.Display
import com.anytypeio.anytype.domain.database.model.ViewType
import com.anytypeio.anytype.domain.database.repo.DatabaseRepository

class CreateDisplayView(
    private val databaseRepo: DatabaseRepository
) : BaseUseCase<Display, CreateDisplayView.Params>() {

    override suspend fun run(params: Params): Either<Throwable, Display> {
        val displayView = Display(
            //todo update id
            id = "-1",
            name = params.name,
            type = params.type
        )
        return Either.Right(displayView)
    }

    data class Params(val name: String, val type: ViewType)
}