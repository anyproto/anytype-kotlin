package com.anytypeio.anytype.presentation.editor

import com.anytypeio.anytype.core_models.Id

sealed class Snack {
    data class UndoRedo(val message: String) : Snack()
}