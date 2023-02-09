package com.anytypeio.anytype.presentation.types

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.types.CreateType
import com.anytypeio.anytype.presentation.navigation.NavigationViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import timber.log.Timber

class TypeCreationViewModel(
    private val createTypeInteractor: CreateType,
) : NavigationViewModel<TypeCreationViewModel.Navigation>() {

    fun createType(name: String) {
        viewModelScope.launch {
            createTypeInteractor.execute(
                CreateType.Params(name)
            ).fold(
                onSuccess = {
                    navigate(Navigation.Back)
                },
                onFailure = {
                    Timber.e(it, "Error while creating type $name")
                    sendToast("Something went wrong. Please, try again later.")
                }
            )
        }
    }

    sealed class Navigation {
        object Back : Navigation()
    }

    class Factory @Inject constructor(
        private val createType: CreateType
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TypeCreationViewModel(createType) as T
        }
    }

}