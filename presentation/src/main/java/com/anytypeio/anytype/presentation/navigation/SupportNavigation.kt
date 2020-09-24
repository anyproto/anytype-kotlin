package com.anytypeio.anytype.presentation.navigation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

interface SupportNavigation<Navigation> {
    val navigation: MutableLiveData<Navigation>
    fun observeNavigation(): LiveData<Navigation> = navigation

    fun navigate(command: Navigation) {
        navigation.postValue(command)
    }
}