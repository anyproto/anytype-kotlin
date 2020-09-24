package com.anytypeio.anytype.domain.database.interactor

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.database.repo.DatabaseRepository

class DuplicateDatabase(
    private val databaseRepo: DatabaseRepository
) : BaseUseCase<String, DuplicateDatabase.Params>() {

    override suspend fun run(params: Params): Either<Throwable, String> = try {
        Either.Right("Duplicate database with id:${params.id}, name:{${params.name}}")
    } catch (e: Throwable) {
        Either.Left(e)
    }

    data class Params(val id: String, val name: String)
}