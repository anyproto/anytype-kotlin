package com.anytypeio.anytype.presentation.auth.pin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class EnterPinCodeViewModelFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EnterPinCodeViewModel() as T
    }
}