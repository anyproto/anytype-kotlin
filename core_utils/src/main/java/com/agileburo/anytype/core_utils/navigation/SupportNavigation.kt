package com.agileburo.anytype.core_utils.navigation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

interface SupportNavigation<Navigation> {
    val navigation: MutableLiveData<Navigation>
    fun observeNavigation(): LiveData<Navigation> = navigation
}