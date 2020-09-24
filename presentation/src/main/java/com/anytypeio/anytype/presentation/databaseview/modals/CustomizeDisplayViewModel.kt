package com.anytypeio.anytype.presentation.databaseview.modals

import androidx.lifecycle.*
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.domain.database.interactor.GetDatabase
import com.anytypeio.anytype.domain.database.model.Filter
import com.anytypeio.anytype.domain.database.model.Group
import com.anytypeio.anytype.domain.database.model.ViewType
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.SupportNavigation
import timber.log.Timber


sealed class CustomizeDisplayViewState {

    data class Init(val filters: List<Filter>, val groups: List<Group>, val type: ViewType) :
        CustomizeDisplayViewState()

    data class NavigateToSwitchScreen(val id: String) : CustomizeDisplayViewState()
    data class NavigateToPropertiesScreen(val id: String) : CustomizeDisplayViewState()
}

class CustomizeDisplayViewModel(
    private val id: String,
    private val getDatabase: GetDatabase
) : ViewModel(), SupportNavigation<EventWrapper<AppNavigation.Command>> {

    private val stateData = MutableLiveData<CustomizeDisplayViewState>()
    val state: LiveData<CustomizeDisplayViewState> = stateData
    override val navigation: MutableLiveData<EventWrapper<AppNavigation.Command>> = MutableLiveData()

    fun onViewCreated() = init()
    fun onViewTypeClick() = navigateToSwitch()
    fun onPropertiesClick() = navigateToProperties()

    private fun init() =
        getDatabase.invoke(viewModelScope, GetDatabase.Params(id = id)) { result ->
            result.either(
                fnL = { Timber.e(it, "Error getting database") },
                fnR = {
                    stateData.postValue(
                        CustomizeDisplayViewState.Init(
                            type = it.content.displays[0].type,
                            filters = it.content.displays[0].filters,
                            groups = it.content.displays[0].groups
                        )
                    )
                }
            )
        }

    private fun navigateToSwitch() {
        getDatabase.invoke(viewModelScope, GetDatabase.Params(id = id)) { result ->
            result.either(
                fnL = { Timber.e(it, "Error getting database") },
                fnR = { database ->
                    database.content.databaseId.let {
                        stateData.postValue(
                            CustomizeDisplayViewState.NavigateToSwitchScreen(
                                it
                            )
                        )
                    }
                }
            )
        }
    }

    private fun navigateToProperties() {
        getDatabase.invoke(viewModelScope, GetDatabase.Params(id = id)) { result ->
            result.either(
                fnL = { Timber.e(it, "Error getting database") },
                fnR = { database ->
                    database.content.databaseId.let {
                        stateData.postValue(
                            CustomizeDisplayViewState.NavigateToPropertiesScreen(
                                it
                            )
                        )
                    }
                }
            )
        }
    }
}

@Suppress("UNCHECKED_CAST")
class CustomizeDisplayViewModelFactory(
    private val id: String,
    private val getDatabase: GetDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CustomizeDisplayViewModel(
            id,
            getDatabase
        ) as T
    }
}