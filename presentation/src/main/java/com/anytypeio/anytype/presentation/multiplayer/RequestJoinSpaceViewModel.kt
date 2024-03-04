package com.anytypeio.anytype.presentation.multiplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.presentation.common.BaseViewModel
import javax.inject.Inject

class RequestJoinSpaceViewModel : BaseViewModel() {

    class Factory @Inject constructor(

    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = RequestJoinSpaceViewModel(

        ) as T
    }

    data class Params(val link: String)
}