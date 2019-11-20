package com.agileburo.anytype.presentation.databaseview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.agileburo.anytype.core_utils.ui.ViewState
import com.agileburo.anytype.core_utils.ui.ViewStateViewModel
import com.agileburo.anytype.domain.database.interactor.GetDatabase
import com.agileburo.anytype.presentation.databaseview.mapper.toPresentation
import com.agileburo.anytype.presentation.databaseview.models.Table
import timber.log.Timber

class DatabaseViewModel(
    private val getDatabase: GetDatabase
) : ViewStateViewModel<ViewState<Table>>() {

    fun onViewCreated() {
        stateData.postValue(ViewState.Init)
    }

    fun getDatabaseView(id: String) {
        getDatabase.invoke(viewModelScope, GetDatabase.Params(id)) { result ->
            result.either(
                fnL = { e -> Timber.e("Error while getting database for id=$id ${e.message}") },
                fnR = { stateData.postValue(ViewState.Success(it.toPresentation())) }
            )
        }
    }
}

@Suppress("UNCHECKED_CAST")
class DatabaseViewModelFactory(
    private val getDatabase: GetDatabase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DatabaseViewModel(getDatabase = getDatabase) as T
    }
}