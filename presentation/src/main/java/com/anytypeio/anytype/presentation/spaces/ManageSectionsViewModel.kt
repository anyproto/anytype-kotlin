package com.anytypeio.anytype.presentation.spaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.WidgetSectionConfig
import com.anytypeio.anytype.core_models.WidgetSections
import com.anytypeio.anytype.core_models.WidgetSectionType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber

class ManageSectionsViewModel(
    private val spaceManager: SpaceManager,
    private val userSettingsRepository: UserSettingsRepository,
    private val analytics: Analytics
) : BaseViewModel() {

    private val _uiState = MutableStateFlow<ManageSectionsState>(ManageSectionsState.Loading)
    val uiState: StateFlow<ManageSectionsState> = _uiState.asStateFlow()

    val commands = MutableSharedFlow<Command>(replay = 0)

    init {
        loadSections()
    }

    private fun loadSections() {
        viewModelScope.launch {
            spaceManager.observe()
                .catch { e ->
                    Timber.e(e, "Error observing current space")
                    _uiState.value = ManageSectionsState.Error
                }
                .collect { config ->
                    val spaceId = SpaceId(config.space)
                    loadSectionsForSpace(spaceId)
                }
        }
    }

    private fun loadSectionsForSpace(spaceId: SpaceId) {
        viewModelScope.launch {
            userSettingsRepository.observeWidgetSections(spaceId)
                .catch { e ->
                    Timber.e(e, "Error loading widget sections")
                    _uiState.value = ManageSectionsState.Error
                }
                .collect { widgetSections ->
                    val items = widgetSections.withDefaults().sections
                        .map { config ->
                            SectionItem(
                                type = config.id,
                                isVisible = config.isVisible,
                                order = config.order,
                                canReorder = false,
                                canToggle = config.isUserConfigurable
                            )
                        }
                        .sortedBy { it.order }
                    
                    _uiState.value = ManageSectionsState.Content(
                        sections = items,
                        spaceId = spaceId
                    )
                }
        }
    }

    fun onSectionVisibilityChanged(sectionType: WidgetSectionType, isVisible: Boolean) {
        val state = _uiState.value
        if (state !is ManageSectionsState.Content) return

        val updatedSections = state.sections.map { item ->
            if (item.type == sectionType) {
                item.copy(isVisible = isVisible)
            } else {
                item
            }
        }

        _uiState.value = state.copy(sections = updatedSections)
        saveSections(state.spaceId, updatedSections)
    }

    fun onSectionsReordered(reorderedSections: List<SectionItem>) {
        val state = _uiState.value
        if (state !is ManageSectionsState.Content) return

        val finalSections = reorderedSections.mapIndexed { index, item ->
            item.copy(order = index)
        }

        _uiState.value = state.copy(sections = finalSections)
        saveSections(state.spaceId, finalSections)
    }

    private fun saveSections(spaceId: SpaceId, sections: List<SectionItem>) {
        viewModelScope.launch {
            val widgetSections = WidgetSections(
                sections = sections.map { item ->
                    WidgetSectionConfig(
                        id = item.type,
                        isVisible = item.isVisible,
                        order = item.order,
                        isUserConfigurable = item.type.isUserConfigurable()
                    )
                }
            )
            
            userSettingsRepository.setWidgetSections(spaceId, widgetSections)
        }
    }

    fun onBackPressed() {
        viewModelScope.launch {
            commands.emit(Command.Dismiss)
        }
    }

    sealed class Command {
        object Dismiss : Command()
    }

    class Factory @Inject constructor(
        private val spaceManager: SpaceManager,
        private val userSettingsRepository: UserSettingsRepository,
        private val analytics: Analytics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ManageSectionsViewModel(
                spaceManager = spaceManager,
                userSettingsRepository = userSettingsRepository,
                analytics = analytics
            ) as T
        }
    }
}

sealed class ManageSectionsState {
    object Loading : ManageSectionsState()
    object Error : ManageSectionsState()
    data class Content(
        val sections: List<SectionItem>,
        val spaceId: SpaceId
    ) : ManageSectionsState()
}

data class SectionItem(
    val type: WidgetSectionType,
    val isVisible: Boolean,
    val order: Int,
    val canReorder: Boolean,
    val canToggle: Boolean
)
