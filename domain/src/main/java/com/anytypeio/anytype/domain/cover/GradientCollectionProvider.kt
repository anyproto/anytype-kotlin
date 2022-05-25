package com.anytypeio.anytype.domain.cover

import com.anytypeio.anytype.core_models.Id


interface GradientCollectionProvider {
    fun provide(): List<Id>
}