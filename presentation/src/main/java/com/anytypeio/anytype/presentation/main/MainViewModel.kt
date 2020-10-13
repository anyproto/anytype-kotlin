package com.anytypeio.anytype.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.domain.auth.interactor.LaunchAccount
import com.anytypeio.anytype.domain.base.BaseUseCase
import kotlinx.coroutines.launch
import timber.log.Timber

class MainViewModel(
    private val launchAccount: LaunchAccount
) : ViewModel() {

    fun onRestore() {
        viewModelScope.launch {
            launchAccount(BaseUseCase.None).either(
                fnR = { Timber.d("Restored account after activity recreation") },
                fnL = { Timber.e(it, "Error while launching account after activity recreation") }
            )
        }
    }
}