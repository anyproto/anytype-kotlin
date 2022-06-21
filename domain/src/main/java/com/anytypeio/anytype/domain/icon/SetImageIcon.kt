package com.anytypeio.anytype.domain.icon

import com.anytypeio.anytype.core_models.Hash
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.BaseUseCase

abstract class SetImageIcon<T> : BaseUseCase<Pair<Payload, Hash>, SetImageIcon.Params<T>>() {
    /**
     * Params for for setting document's image icon
     * @property path image path in file system
     * @property target i.e. id of the context or text blockId
     */
    data class Params<T>(
        val target: T,
        val path: String
    )
}