package com.anytypeio.anytype.presentation.publishtoweb

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.inject.Inject

class PublishToWebViewModel : ViewModel() {

    // Empty constructor - will add logic later

    class Factory @Inject constructor(
        // Empty constructor - dependencies will be added later
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PublishToWebViewModel() as T
        }
    }
}
