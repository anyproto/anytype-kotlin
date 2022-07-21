package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.core_models.Id

class ObjectSetCreateBookmarkRecordViewModel : ObjectSetCreateRecordViewModelBase() {

    override fun onComplete(ctx: Id, input: String) {
        TODO()
    }

    override fun onButtonClicked(ctx: Id, input: String) {
        TODO()
    }

    class Factory() : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ObjectSetCreateBookmarkRecordViewModel() as T
        }
    }

}