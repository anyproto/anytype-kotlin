package com.anytypeio.anytype.presentation.onboarding.signup

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

    val toasts = MutableSharedFlow<String>()

    private val _navigationFlow = MutableSharedFlow<Navigation>()
    val navigationFlow: SharedFlow<Navigation> = _navigationFlow

    fun onGoToTheAppClicked(name: String) {
        proceedWithSettingAccountName(name)
    }

    private fun proceedWithSettingAccountName(name: String) {
        val config = configStorage.getOrNull()
        if (config != null) {
            viewModelScope.launch {
                setObjectDetails.execute(
                    SetObjectDetails.Params(
                        ctx = config.profile, details = mapOf(Relations.NAME to name)
                    )
                ).fold(
                    onFailure = {
                        Timber.e(it, "Error while updating object details")
                    },
                    onSuccess = {
                        proceedWithSettingWorkspaceName(name)
                    }
                )
            }
        } else {
            Timber.e(CONFIG_NOT_FOUND_ERROR).also {
                toast(CONFIG_NOT_FOUND_ERROR)
            }
        }
    }

    private fun proceedWithSettingWorkspaceName(name: String) {
        val config = configStorage.getOrNull()
        if (config != null) {
            viewModelScope.launch {
                setObjectDetails.execute(
                    SetObjectDetails.Params(
                        ctx = config.workspace,
                        details = mapOf(Relations.NAME to name)
                    )
                ).fold(
                    onFailure = {
                        Timber.e(it, "Error while updating object details")
                    },
                    onSuccess = {
                        _navigationFlow.emit(Navigation.OpenSoulCreationAnim(name))
                    }
                )
            }
        } else {
            Timber.e(CONFIG_NOT_FOUND_ERROR).also {
                toast(CONFIG_NOT_FOUND_ERROR)
            }
        }
    }

    private fun toast(msg: String) {
        viewModelScope.launch { toasts.emit(msg) }
    }

    sealed interface Navigation {
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

    companion object {
        const val CONFIG_NOT_FOUND_ERROR = "Something went wrong: config not found"
    }
}