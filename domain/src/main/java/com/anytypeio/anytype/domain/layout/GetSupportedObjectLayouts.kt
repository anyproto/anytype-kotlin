package com.anytypeio.anytype.domain.layout

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Either

class GetSupportedObjectLayouts :
    BaseUseCase<List<ObjectType.Layout>, GetSupportedObjectLayouts.Params>() {

    override suspend fun run(params: Params) = safe {
        listOf(
            ObjectType.Layout.NOTE,
            ObjectType.Layout.BASIC,
            ObjectType.Layout.PROFILE,
            ObjectType.Layout.TODO,
        )
    }

    data class Params(val ctx: Id)
}