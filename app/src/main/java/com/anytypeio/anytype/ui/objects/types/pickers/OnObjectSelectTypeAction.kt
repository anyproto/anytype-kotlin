package com.anytypeio.anytype.ui.objects.types.pickers

import com.anytypeio.anytype.core_models.Id

interface OnObjectSelectTypeAction {
    fun onProceedWithUpdateType(id: Id)
    fun onProceedWithDraftUpdateType(id: Id)
}