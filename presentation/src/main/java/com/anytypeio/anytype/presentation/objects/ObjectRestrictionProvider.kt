package com.anytypeio.anytype.presentation.objects

import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction

interface ObjectRestrictionProvider {
    fun provide() : List<ObjectRestriction>
}