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

    private val _isCompactModeEnabled = MutableStateFlow(true)
    val isCompactModeEnabled: StateFlow<Boolean> = _isCompactModeEnabled.asStateFlow()

    private val _isKanbanEnabled = MutableStateFlow(true)
    val isKanbanEnabled: StateFlow<Boolean> = _isKanbanEnabled.asStateFlow()

    private val _isQuickCaptureEnabled = MutableStateFlow(false)
    val isQuickCaptureEnabled: StateFlow<Boolean> = _isQuickCaptureEnabled.asStateFlow()

    private val _isQuickCaptureAiEnabled = MutableStateFlow(false)
    val isQuickCaptureAiEnabled: StateFlow<Boolean> = _isQuickCaptureAiEnabled.asStateFlow()

    init {
        viewModelScope.launch {
            _isCompactModeEnabled.value = userSettingsRepository.getCompactModeEnabled()
            _isKanbanEnabled.value = userSettingsRepository.getKanbanEnabled()
            _isQuickCaptureEnabled.value = userSettingsRepository.getQuickCaptureEnabled()
            _isQuickCaptureAiEnabled.value = userSettingsRepository.getQuickCaptureAiEnabled()
        }
    }

    fun onCompactModeToggled(enabled: Boolean) {
        viewModelScope.launch {
            userSettingsRepository.setCompactModeEnabled(enabled)
            _isCompactModeEnabled.value = enabled
        }
    }

    fun onKanbanToggled(enabled: Boolean) {
        viewModelScope.launch {
            userSettingsRepository.setKanbanEnabled(enabled)
            _isKanbanEnabled.value = enabled
        }
    }

    fun onQuickCaptureToggled(enabled: Boolean) {
        viewModelScope.launch {
            userSettingsRepository.setQuickCaptureEnabled(enabled)
            _isQuickCaptureEnabled.value = enabled
        }
    }

    fun onQuickCaptureAiToggled(enabled: Boolean) {
        viewModelScope.launch {
            userSettingsRepository.setQuickCaptureAiEnabled(enabled)
            _isQuickCaptureAiEnabled.value = enabled
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
