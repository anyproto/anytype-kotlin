package com.anytypeio.anytype.ui.objects.types.pickers

import com.anytypeio.anytype.presentation.objects.ObjectTypeView

interface OnObjectSelectTypeAction {
    fun onProceedWithUpdateType(item: ObjectTypeView)
    fun onProceedWithDraftUpdateType(item: ObjectTypeView)
}