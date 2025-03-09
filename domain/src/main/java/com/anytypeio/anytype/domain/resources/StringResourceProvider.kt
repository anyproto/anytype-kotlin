package com.anytypeio.anytype.domain.resources

import com.anytypeio.anytype.core_models.RelativeDate

interface StringResourceProvider {
    fun getRelativeDateName(relativeDate: RelativeDate): String
    fun getDeletedObjectTitle(): String
    fun getUntitledObjectTitle(): String
    fun getSetOfObjectsTitle(): String
}