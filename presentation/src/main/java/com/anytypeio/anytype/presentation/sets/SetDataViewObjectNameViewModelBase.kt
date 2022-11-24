package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.common.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow

abstract class SetDataViewObjectNameViewModelBase : BaseViewModel() {
    val isCompleted = MutableStateFlow(false)

    /**
     * @param [target] used when object is already created and we need to set name for it.
     */
    abstract fun onActionDone(target: Id, input: String)
    abstract fun onActionDone(input: String)
    /**
     * @param [target] used when object is already created and we need to set name for it.
     */
    abstract fun onButtonClicked(target: Id, input: String)
    abstract fun onButtonClicked(input: String)
}