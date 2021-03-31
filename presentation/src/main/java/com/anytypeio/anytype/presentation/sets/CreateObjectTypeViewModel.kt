package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

sealed class CreateObjectTypeViewState {
    object Loading : CreateObjectTypeViewState()
    data class Success(val data: List<CreateObjectTypeView>) : CreateObjectTypeViewState()
    data class Exit(val type: CreateObjectTypeView, val name: String) : CreateObjectTypeViewState()
}

class CreateObjectTypeViewModel : ViewModel() {

    private val _state = MutableLiveData<CreateObjectTypeViewState>()
    val state: LiveData<CreateObjectTypeViewState> = _state

    fun init(data: List<CreateObjectTypeView>) {
        _state.postValue(CreateObjectTypeViewState.Success(data))
    }

    fun onCreateClicked(name: String) {
        if (name.isNotBlank()) {
            (_state.value as? CreateObjectTypeViewState.Success)?.let { state ->
                val type = state.data.first { it.isSelected }
                _state.postValue(CreateObjectTypeViewState.Exit(type, name))
            }
        }
    }

    fun onSelectType(layout: Int) {
        (_state.value as? CreateObjectTypeViewState.Success)?.let { state ->
            val copy = state.data.map {
                if (it.layout == layout) {
                    it.copy(isSelected = true)
                } else {
                    it.copy(isSelected = false)
                }
            }
            _state.postValue(CreateObjectTypeViewState.Success(copy))
        }
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return CreateObjectTypeViewModel() as T
        }
    }
}