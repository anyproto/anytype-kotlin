package com.anytypeio.anytype.presentation.auth.pin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ChoosePinCodeViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChoosePinCodeViewModel() as T
    }
}