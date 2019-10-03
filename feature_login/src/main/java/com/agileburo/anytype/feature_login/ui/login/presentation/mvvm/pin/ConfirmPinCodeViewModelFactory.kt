package com.agileburo.anytype.feature_login.ui.login.presentation.mvvm.pin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ConfirmPinCodeViewModelFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ConfirmPinCodeViewModel() as T
    }
}