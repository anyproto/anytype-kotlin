package com.anytypeio.anytype.domain.cover

import com.anytypeio.anytype.domain.common.Id

interface GradientCollectionProvider {
    fun provide(): List<Id>
}