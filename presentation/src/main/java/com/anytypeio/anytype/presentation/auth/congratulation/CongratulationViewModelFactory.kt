package com.anytypeio.anytype.presentation.auth.congratulation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CongratulationViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CongratulationViewModel() as T
    }
}