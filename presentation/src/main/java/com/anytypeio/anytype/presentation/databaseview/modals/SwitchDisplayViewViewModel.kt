package com.anytypeio.anytype.presentation.databaseview.modals

import androidx.lifecycle.*
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.core_utils.ext.timber
import com.anytypeio.anytype.domain.database.interactor.GetDatabase
import com.anytypeio.anytype.domain.database.interactor.SwitchDisplayView
import com.anytypeio.anytype.domain.database.model.ViewType
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.SupportNavigation
import timber.log.Timber

sealed class SwitchDisplayViewState {

    data class Init(val type: ViewType) : SwitchDisplayViewState()
    data class NavigateToCustomize(val id: String) : SwitchDisplayViewState()
    object GalleryChosen : SwitchDisplayViewState()
    object ListChosen : SwitchDisplayViewState()
    object KanbanChosen : SwitchDisplayViewState()
    object TableChosen : SwitchDisplayViewState()
}

class SwitchDisplayViewViewModel(
    private val id: String,
    private val getDatabase: GetDatabase,
    private val switchDisplayView: SwitchDisplayView
) : ViewModel(), SupportNavigation<EventWrapper<AppNavigation.Command>> {

    private val stateData = MutableLiveData<SwitchDisplayViewState>()
    val state: LiveData<SwitchDisplayViewState> = stateData
    override val navigation: MutableLiveData<EventWrapper<AppNavigation.Command>> =
        MutableLiveData()

    fun onViewCreated() = init(id)

    fun onCancelClick() {
        getDatabase.invoke(viewModelScope, GetDatabase.Params(id = id)) { result ->
            result.either(
                fnL = { Timber.e(it, "Error getting database") },
                fnR = {
                    stateData.postValue(
                        SwitchDisplayViewState.NavigateToCustomize(
                            id = it.content.databaseId
                        )
                    )
                }
            )
        }
    }

    fun onGalleryClick() {
        startSwitchDisplayView(id, ViewType.GALLERY)
    }

    fun onKanbanClick() {
        startSwitchDisplayView(id, ViewType.BOARD)
    }

    fun onTableClick() {
        startSwitchDisplayView(id, ViewType.GRID)
    }

    fun onListClick() {
        startSwitchDisplayView(id, ViewType.LIST)
    }

    private fun init(id: String) =
        getDatabase.invoke(viewModelScope, GetDatabase.Params(id = id)) { result ->
            result.either(
                fnL = { Timber.e(it, "Error getting database") },
                fnR = {
                    stateData.postValue(
                        SwitchDisplayViewState.Init(
                            type = it.content.displays[0].type
                        )
                    )
                }
            )
        }

    private fun startSwitchDisplayView(id: String, type: ViewType) {
        switchDisplayView(viewModelScope, SwitchDisplayView.Params(id, type)) { either ->
            either.either(
                fnL = { it.timber() },
                fnR = {
                    when (type) {
                        ViewType.GRID -> stateData.postValue(SwitchDisplayViewState.TableChosen)
                        ViewType.BOARD -> stateData.postValue(SwitchDisplayViewState.KanbanChosen)
                        ViewType.GALLERY -> stateData.postValue(SwitchDisplayViewState.GalleryChosen)
                        ViewType.LIST -> stateData.postValue(SwitchDisplayViewState.ListChosen)
                    }
                }
            )
        }
    }
}

@Suppress("UNCHECKED_CAST")
class SwitchDisplayViewViewModelFactory(
    private val getDatabase: GetDatabase,
    private val id: String,
    private val switchDisplayView: SwitchDisplayView
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return SwitchDisplayViewViewModel(
            getDatabase = getDatabase,
            id = id,
            switchDisplayView = switchDisplayView
        ) as T
    }
}