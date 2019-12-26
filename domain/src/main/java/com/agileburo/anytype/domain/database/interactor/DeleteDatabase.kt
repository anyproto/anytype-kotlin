package com.agileburo.anytype.domain.database.interactor

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.database.repo.DatabaseRepository

class DeleteDatabase(
    private val databaseRepo: DatabaseRepository
) : BaseUseCase<String, DeleteDatabase.Params>() {

    override suspend fun run(params: Params): Either<Throwable, String> = try {
        Either.Right("Delete database with id:${params.id}, name:{${params.name}}")
    } catch (e: Throwable) {
        Either.Left(e)
    }

    data class Params(val id: String, val name: String)
}