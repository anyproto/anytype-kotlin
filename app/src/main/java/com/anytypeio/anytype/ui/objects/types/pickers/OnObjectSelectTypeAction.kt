package com.anytypeio.anytype.ui.objects.types.pickers

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key

interface OnObjectSelectTypeAction {
    fun onProceedWithUpdateType(id: Id, key: Key)
    fun onProceedWithDraftUpdateType(id: Id, key: Key)
}