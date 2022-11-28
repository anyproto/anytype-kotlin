package com.anytypeio.anytype.domain.launch

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.config.UserSettingsRepository

class SetDefaultEditorType(
    private val repo: UserSettingsRepository
) : BaseUseCase<Unit, SetDefaultEditorType.Params>() {

    override suspend fun run(params: Params) = safe {
        repo.setDefaultObjectType(params.type, params.name)
    }

    /**
    @property [type] object type
    @see ObjectTypeConst for possible values.
     **/
    data class Params(
        val type: Id,
        val name: String
    )
}