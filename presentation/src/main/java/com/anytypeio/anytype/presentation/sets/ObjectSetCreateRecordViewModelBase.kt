package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.common.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow

abstract class ObjectSetCreateRecordViewModelBase : BaseViewModel() {

    val isCompleted = MutableStateFlow(false)

    abstract fun onComplete(ctx: Id, input: String)

    abstract fun onButtonClicked(ctx: Id, input: String)
}