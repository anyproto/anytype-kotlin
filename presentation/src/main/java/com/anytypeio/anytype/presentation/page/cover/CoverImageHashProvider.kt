package com.anytypeio.anytype.presentation.page.cover

import com.anytypeio.anytype.domain.common.Hash
import com.anytypeio.anytype.domain.common.Id

interface CoverImageHashProvider {
    fun provide(id: Id): Hash?
}