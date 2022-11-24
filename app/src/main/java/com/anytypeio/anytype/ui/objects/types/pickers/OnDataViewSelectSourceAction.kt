package com.anytypeio.anytype.ui.objects.types.pickers

import com.anytypeio.anytype.core_models.Id

interface OnDataViewSelectSourceAction {
    fun onProceedWithSelectSource(id: Id)
}