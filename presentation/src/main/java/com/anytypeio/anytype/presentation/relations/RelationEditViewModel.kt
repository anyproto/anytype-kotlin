package com.anytypeio.anytype.presentation.relations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.navigation.NavigationViewModel
import com.anytypeio.anytype.presentation.types.TypeIcon
import com.anytypeio.anytype.presentation.types.TypeId
import com.anytypeio.anytype.presentation.types.TypeName
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class RelationEditViewModel(
    @TypeId private val id: Id,
    @TypeName private val name: String,
    @TypeIcon private val icon: Int
) : NavigationViewModel<RelationEditViewModel.Navigation>() {

    private val originalNameFlow = MutableStateFlow(name)

    val uiState: StateFlow<RelationEditState> =
        originalNameFlow.map {
            RelationEditState.Data(
                typeName = it,
                objectIcon = icon,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_SUBSCRIPTION_TIMEOUT),
            RelationEditState.Idle
        )

    fun uninstallRelation() {
        navigate(Navigation.BackWithUninstall(id))
    }

    fun updateRelationDetails(name: String) {
        navigate(Navigation.BackWithModify(id, name))
    }

    sealed class Navigation {
        data class BackWithUninstall(val id: Id) : Navigation()
        data class BackWithModify(val id: Id, val name: String) : Navigation()
    }

    class Factory @Inject constructor(
        @TypeId private val id: Id,
        @TypeName private val name: String,
        @TypeIcon private val icon: Int
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RelationEditViewModel(
                id = id,
                name = name,
                icon = icon
            ) as T
        }
    }

}

private const val STOP_SUBSCRIPTION_TIMEOUT = 1_000L

sealed class RelationEditState {
    data class Data(
        val typeName: String,
        val objectIcon: Int
    ) : RelationEditState()

    object Idle : RelationEditState()
}