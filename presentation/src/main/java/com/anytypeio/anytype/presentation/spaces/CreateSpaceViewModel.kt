package com.anytypeio.anytype.presentation.spaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.presentation.common.BaseViewModel
import javax.inject.Inject

class CreateSpaceViewModel @Inject constructor() : BaseViewModel() {

    class Factory @Inject constructor(
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ) = CreateSpaceViewModel() as T
    }
}