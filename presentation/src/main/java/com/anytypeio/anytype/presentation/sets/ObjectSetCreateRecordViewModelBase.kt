package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import com.anytypeio.anytype.core_models.Id
import kotlinx.coroutines.flow.MutableStateFlow

abstract class ObjectSetCreateRecordViewModelBase : ViewModel() {

    val isCompleted = MutableStateFlow(false)

    abstract fun onComplete(ctx: Id, input: String)

    abstract fun onButtonClicked(ctx: Id, input: String)
}