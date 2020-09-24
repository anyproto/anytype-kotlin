package com.anytypeio.anytype.presentation.databaseview.modals

import androidx.lifecycle.*
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.core_utils.ext.timber
import com.anytypeio.anytype.domain.database.interactor.DeleteDatabase
import com.anytypeio.anytype.domain.database.interactor.DuplicateDatabase
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.SupportNavigation

sealed class EditDatabaseState {

    data class Init(val name: String) : EditDatabaseState()
    data class Duplicate(val name: String) : EditDatabaseState()
    data class Delete(val name: String) : EditDatabaseState()
}

class EditDatabaseViewModel(
    private val databaseId: String,
    private val databaseName: String,
    private val duplicateDatabase: DuplicateDatabase,
    private val deleteDatabase: DeleteDatabase
) : ViewModel(), SupportNavigation<EventWrapper<AppNavigation.Command>> {

    private val stateData = MutableLiveData<EditDatabaseState>()
    val state: LiveData<EditDatabaseState> = stateData
    override val navigation: MutableLiveData<EventWrapper<AppNavigation.Command>> = MutableLiveData()

    fun onViewCreated() {
        stateData.postValue(
            EditDatabaseState.Init(
                databaseName
            )
        )
    }

    fun onBackClick() {
        navigation.postValue(EventWrapper(AppNavigation.Command.Exit))
    }

    fun onDuplicateClick() {
        duplicateDatabase.invoke(
            viewModelScope,
            DuplicateDatabase.Params(id = databaseId, name = databaseName)
        ) { either ->
            either.either(
                fnL = { it.timber() },
                fnR = { stateData.postValue(
                    EditDatabaseState.Duplicate(
                        it
                    )
                ) }
            )
        }
    }

    fun onDeleteClick() {
        deleteDatabase.invoke(
            viewModelScope,
            DeleteDatabase.Params(id = databaseId, name = databaseName)
        ) { either ->
            either.either(
                fnL = { it.timber() },
                fnR = { stateData.postValue(
                    EditDatabaseState.Delete(
                        it
                    )
                ) }
            )
        }
    }
}

@Suppress("UNCHECKED_CAST")
class EditDatabaseViewModelFactory(
    private val databaseName: String,
    private val databaseId: String,
    private val duplicateDatabase: DuplicateDatabase,
    private val deleteDatabase: DeleteDatabase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return EditDatabaseViewModel(
            databaseName = databaseName,
            databaseId = databaseId,
            deleteDatabase = deleteDatabase,
            duplicateDatabase = duplicateDatabase
        ) as T
    }
}