package com.anytypeio.anytype.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExperimentalFeaturesViewModel(
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    private val _isCompactModeEnabled = MutableStateFlow(false)
    val isCompactModeEnabled: StateFlow<Boolean> = _isCompactModeEnabled.asStateFlow()

    init {
        viewModelScope.launch {
            _isCompactModeEnabled.value = userSettingsRepository.getCompactModeEnabled()
        }
    }

    fun onCompactModeToggled(enabled: Boolean) {
        viewModelScope.launch {
            userSettingsRepository.setCompactModeEnabled(enabled)
            _isCompactModeEnabled.value = enabled
        }
    }

    class Factory @Inject constructor(
        private val userSettingsRepository: UserSettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ExperimentalFeaturesViewModel(
                userSettingsRepository = userSettingsRepository
            ) as T
        }
    }
}
