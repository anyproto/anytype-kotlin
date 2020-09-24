package com.anytypeio.anytype.domain.database.interactor

import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.database.FilterMock
import com.anytypeio.anytype.domain.database.model.Group

class GetGroups : BaseUseCase<List<Group>, BaseUseCase.None>() {

    override suspend fun run(params: None): Either<Throwable, List<Group>> = try {
        Either.Right(FilterMock.groups)
    } catch (e: Throwable) {
        Either.Left(e)
    }
}