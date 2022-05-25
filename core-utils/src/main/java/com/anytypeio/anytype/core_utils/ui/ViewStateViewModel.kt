package com.anytypeio.anytype.core_utils.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

open class ViewStateViewModel<VS> : ViewModel() {
    protected val stateData = MutableLiveData<VS>()
    val state: LiveData<VS> = stateData
    fun update(update: VS) = stateData.postValue(update)
}