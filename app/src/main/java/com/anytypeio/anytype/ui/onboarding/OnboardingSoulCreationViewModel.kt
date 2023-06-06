package com.anytypeio.anytype.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class OnboardingSoulCreationViewModel @Inject constructor(
    private val setObjectDetails: SetObjectDetails,
    private val configStorage: ConfigStorage
) : ViewModel() {

    private val accountId = configStorage.get().profile
    private val workspaceId = configStorage.get().workspace

    private val _navigationFlow = MutableSharedFlow<Navigation>()
    val navigationFlow: SharedFlow<Navigation> = _navigationFlow

    fun setAccountAndSpaceName(name: String) {
        viewModelScope.launch {
            setObjectDetails.execute(
                SetObjectDetails.Params(ctx = accountId, details = mapOf(Relations.NAME to name))
            ).fold(
                onFailure = {
                    Timber.e(it, "Error while updating object details")
                },
                onSuccess = {
                    setWorkspaceName(name)
                }
            )
        }
    }

    private fun setWorkspaceName(name: String) {
        viewModelScope.launch {
            setObjectDetails.execute(
                SetObjectDetails.Params(ctx = workspaceId, details = mapOf(Relations.NAME to name))
            ).fold(
                onFailure = {
                    Timber.e(it, "Error while updating object details")
                },
                onSuccess = {
                    _navigationFlow.emit(Navigation.OpenSoulCreationAnim(name))
                }
            )
        }
    }

    sealed interface Navigation {
        object Idle: Navigation
        class OpenSoulCreationAnim(val name: String): Navigation
    }


    class Factory @Inject constructor(
        private val setObjectDetails: SetObjectDetails,
        private val configStorage: ConfigStorage
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OnboardingSoulCreationViewModel(
                setObjectDetails = setObjectDetails,
                configStorage = configStorage
            ) as T
        }
    }
}