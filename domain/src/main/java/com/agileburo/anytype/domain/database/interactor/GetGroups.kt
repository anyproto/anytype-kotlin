package com.agileburo.anytype.domain.database.interactor

import com.agileburo.anytype.domain.base.BaseUseCase
import com.agileburo.anytype.domain.base.Either
import com.agileburo.anytype.domain.database.FilterMock
import com.agileburo.anytype.domain.database.model.Group

class GetGroups : BaseUseCase<List<Group>, BaseUseCase.None>() {

    override suspend fun run(params: None): Either<Throwable, List<Group>> = try {
        Either.Right(FilterMock.groups)
    } catch (e: Throwable) {
        Either.Left(e)
    }
}