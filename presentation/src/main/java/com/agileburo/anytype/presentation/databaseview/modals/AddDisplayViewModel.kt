package com.agileburo.anytype.presentation.databaseview.modals

import androidx.lifecycle.*
import com.agileburo.anytype.core_utils.common.EventWrapper
import com.agileburo.anytype.core_utils.ext.timber
import com.agileburo.anytype.domain.database.interactor.CreateDisplayView
import com.agileburo.anytype.domain.database.model.Display
import com.agileburo.anytype.domain.database.model.ViewType
import com.agileburo.anytype.presentation.navigation.AppNavigation
import com.agileburo.anytype.presentation.navigation.SupportNavigation

enum class ChosenButtons { GALLERY, KANBAN, TABLE, LIST }

val DEFAULT_CLICKED =
    ChosenButtons.GALLERY

sealed class AddViewState {

    data class Init(val defaultType: ChosenButtons) : AddViewState()
    data class Create(val display: Display) : AddViewState()
    object EmptyName : AddViewState()
    object GalleryChosen : AddViewState()
    object ListChosen : AddViewState()
    object KanbanChosen : AddViewState()
    object TableChosen : AddViewState()
}

class AddDisplayViewModel(
    private val createDisplayView: CreateDisplayView
) : ViewModel(), SupportNavigation<EventWrapper<AppNavigation.Command>> {

    private var clicked: ChosenButtons =
        ChosenButtons.GALLERY
    private val stateData = MutableLiveData<AddViewState>()
    val state: LiveData<AddViewState> = stateData

    fun onViewCreated() {
        stateData.postValue(
            AddViewState.Init(
                DEFAULT_CLICKED
            )
        )
    }

    fun onAddClick(name: String) {
        if (isScreenStateValid(name)) {
            createDatabaseView(name, clicked.toViewType())
        } else {
            stateData.postValue(AddViewState.EmptyName)
        }
    }

    fun onCancelClick() {
        navigation.postValue(EventWrapper(AppNavigation.Command.Exit))
    }

    fun onGalleryClick() {
        clicked =
            ChosenButtons.GALLERY
        stateData.postValue(AddViewState.GalleryChosen)
    }

    fun onKanbanClick() {
        clicked =
            ChosenButtons.KANBAN
        stateData.postValue(AddViewState.KanbanChosen)
    }

    fun onTableClick() {
        clicked =
            ChosenButtons.TABLE
        stateData.postValue(AddViewState.TableChosen)
    }

    fun onListClick() {
        clicked =
            ChosenButtons.LIST
        stateData.postValue(AddViewState.ListChosen)
    }

    override val navigation: MutableLiveData<EventWrapper<AppNavigation.Command>> =
        MutableLiveData()

    private fun createDatabaseView(name: String, type: ViewType) {
        createDisplayView.invoke(viewModelScope, CreateDisplayView.Params(name, type)) { either ->
            either.either(
                fnL = { it.timber() },
                fnR = {
                    stateData.postValue(
                        AddViewState.Create(
                            it
                        )
                    )
                }
            )
        }
    }

    private fun isScreenStateValid(name: String): Boolean = name.isNotEmpty()
}

fun ChosenButtons.toViewType() = when (this) {
    ChosenButtons.GALLERY -> ViewType.GALLERY
    ChosenButtons.KANBAN -> ViewType.BOARD
    ChosenButtons.TABLE -> ViewType.GRID
    ChosenButtons.LIST -> ViewType.LIST
}

@Suppress("UNCHECKED_CAST")
class AddDisplayViewModelFactory(
    private val createDisplayView: CreateDisplayView
) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AddDisplayViewModel(
            createDisplayView
        ) as T
    }
}