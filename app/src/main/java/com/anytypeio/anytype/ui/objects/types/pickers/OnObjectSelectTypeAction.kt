package com.anytypeio.anytype.ui.objects.types.pickers

import com.anytypeio.anytype.core_models.ObjectWrapper


interface OnObjectSelectTypeAction {
    fun onProceedWithUpdateType(objType: ObjectWrapper.Type)
}