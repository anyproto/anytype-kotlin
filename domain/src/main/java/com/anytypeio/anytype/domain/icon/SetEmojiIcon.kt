package com.anytypeio.anytype.domain.icon

import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.domain.base.BaseUseCase

abstract class SetEmojiIcon<T> :
    BaseUseCase<Payload, SetEmojiIcon.Params<T>>() {

    /**
     * @property emoji emoji's unicode
     * @property target i.e. object or text block
     */
    data class Params<T>(
        val target: T,
        val emoji: String
    )
}