package com.anytypeio.anytype.presentation.relations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.domain.relations.CreateRelation
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.relations.model.CreateFromScratchState
import com.anytypeio.anytype.presentation.relations.model.StateHolder
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class RelationCreateFromLibraryViewModel(
    private val createFromScratchState: StateHolder<CreateFromScratchState>,
    private val createRelation: CreateRelation,
    private val spaceManager: SpaceManager
): RelationCreateFromScratchBaseViewModel() {

    override val createFromScratchSession get() = createFromScratchState.state

    val navigation = MutableStateFlow<Navigation>(Navigation.Idle)

    fun onCreateRelationClicked() {
        proceedWithCreatingRelation()
    }

    private fun proceedWithCreatingRelation() {
        viewModelScope.launch {
            val state = createFromScratchState.state.value
            val format = state.format
            createRelation(
                CreateRelation.Params(
                    space = spaceManager.get(),
                    format = format,
                    name = name.value,
                    limitObjectTypes = state.limitObjectTypes.map { it.id },
                    prefilled = emptyMap()
                )
            ).process(
                success = {
                    navigation.value = Navigation.Back
                },
                failure = {
                    Timber.e(it, ACTION_FAILED_ERROR).also { _toasts.emit(ACTION_FAILED_ERROR) }
                }
            )
        }
    }

    sealed class Navigation {
        object Idle: Navigation()
        object Back : Navigation()
    }

    class Factory @Inject constructor(
        private val createFromScratchState: StateHolder<CreateFromScratchState>,
        private val createRelation: CreateRelation,
        private val spaceManager: SpaceManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RelationCreateFromLibraryViewModel(
                spaceManager = spaceManager,
                createFromScratchState = createFromScratchState,
                createRelation = createRelation,
            ) as T
        }
    }
}