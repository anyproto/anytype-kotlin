package com.anytypeio.anytype.feature_object_type.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anytypeio.anytype.feature_object_type.ui.create.UiCreateTypeState
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

class CreateObjectTypeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<UiCreateTypeState>(UiCreateTypeState.Empty)
    val uiState = _uiState.asStateFlow()

    init {
        Timber.d("CreateObjectTypeViewModel initialized")
    }

}


class CreateObjectTypeVMFactory @Inject constructor() : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = CreateObjectTypeViewModel() as T
}

