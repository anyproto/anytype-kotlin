package com.anytypeio.anytype.domain.layout

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either

class GetSupportedObjectLayouts :
    BaseUseCase<List<ObjectType.Layout>, GetSupportedObjectLayouts.Params>() {

    override suspend fun run(params: Params): Either<Throwable, List<ObjectType.Layout>> = safe {
        listOf(
            ObjectType.Layout.BASIC,
            ObjectType.Layout.PROFILE,
            ObjectType.Layout.TODO,
            ObjectType.Layout.NOTE
        )
    }

    data class Params(val ctx: Id)
}