package com.anytypeio.anytype.presentation.spaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.presentation.common.BaseViewModel
import javax.inject.Inject

class SelectSpaceViewModel @Inject constructor(
    private val searchObjects: SearchObjects
) : BaseViewModel() {


    class Factory @Inject constructor(
        private val searchObjects: SearchObjects
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ) = SelectSpaceViewModel(
            searchObjects = searchObjects
        ) as T
    }
}