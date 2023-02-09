package com.anytypeio.anytype.presentation.navigation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.presentation.common.BaseViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

interface SupportNavigation<Navigation> {
    val navigation: MutableLiveData<Navigation>
    fun observeNavigation(): LiveData<Navigation> = navigation

    fun navigate(command: Navigation) {
        navigation.postValue(command)
    }
}

open class NavigationViewModel<Navigation> : BaseViewModel() {
    private val _navigation = MutableSharedFlow<Navigation>()
    val navigation: Flow<Navigation> get() = _navigation
    fun navigate(destination: Navigation) = viewModelScope.launch {
        _navigation.emit(destination)
    }
}