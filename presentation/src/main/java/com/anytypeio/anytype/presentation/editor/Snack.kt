package com.anytypeio.anytype.presentation.editor

import com.anytypeio.anytype.core_models.Id

sealed class Snack {
    data class ObjectSetNotFound(val type: Id) : Snack()
}