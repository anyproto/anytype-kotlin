package com.anytypeio.anytype.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary.defaultTypeChanged
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.event.EventAnalytics
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Interactor
import com.anytypeio.anytype.domain.device.ClearFileCache
import com.anytypeio.anytype.domain.launch.GetDefaultEditorType
import com.anytypeio.anytype.domain.launch.SetDefaultEditorType
import com.anytypeio.anytype.domain.misc.AppActionManager
import com.anytypeio.anytype.presentation.splash.SplashViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class OtherSettingsViewModel(
    private val getDefaultEditorType: GetDefaultEditorType,
    private val setDefaultEditorType: SetDefaultEditorType,
    private val clearFileCache: ClearFileCache,
    private val appActionManager: AppActionManager,
    private val analytics: Analytics
) : ViewModel() {

    val commands = MutableSharedFlow<Command>(replay = 0)
    val isClearFileCacheInProgress = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            getDefaultEditorType.execute(Unit).fold(
                onFailure = { e ->
                    Timber.e(e, "Error while getting user settings")
                },
                onSuccess = {
                    commands.emit(Command.SetObjectType(name = it.name))
                }
            )
        }
    }

    fun onObjectTypeClicked() {
        viewModelScope.launch {
            commands.emit(
                Command.NavigateToObjectTypesScreen(
                    smartBlockType = DEFAULT_SETTINGS_SMART_BLOCK_TYPE,
                    exclucedTypes = listOf(ObjectType.BOOKMARK_TYPE)
                )
            )
        }
    }

    fun onClearCacheClicked() {
        viewModelScope.launch {
            commands.emit(Command.ShowClearCacheAlert)
        }
    }

    fun proceedWithClearCache() {
        viewModelScope.launch {
            clearFileCache(BaseUseCase.None).collect { status ->
                when (status) {
                    is Interactor.Status.Started -> {
                        isClearFileCacheInProgress.value = true
                    }
                    is Interactor.Status.Error -> {
                        isClearFileCacheInProgress.value = false
                        val msg = "Error while clearing file cache: ${status.throwable.message}"
                        Timber.e(status.throwable, "Error while clearing file cache")
                        commands.emit(Command.Toast(msg))
                    }
                    Interactor.Status.Success -> {
                        isClearFileCacheInProgress.value = false
                    }
                }
            }
        }
    }

    fun proceedWithUpdateType(type: Id?, name: String?) {
        if (type == null || name == null) {
            viewModelScope.launch {
                commands.emit(Command.Toast("Cannot change default object type"))
            }
            return
        }
        viewModelScope.launch {
            appActionManager.setup(
                AppActionManager.Action.CreateNew(
                    type = type,
                    name = name
                )
            )
            setDefaultEditorType.invoke(SetDefaultEditorType.Params(type, name)).process(
                failure = {
                    Timber.e(it, "Error while setting default object type")
                    commands.emit(Command.Toast(msg = "Error while setting default object type"))
                },
                success = {
                    commands.emit(Command.SetObjectType(name = name))
                    analytics.registerEvent(
                        EventAnalytics.Anytype(
                            name = defaultTypeChanged,
                            props = Props(mapOf(EventsPropertiesKey.objectType to type)),
                            duration = null
                        )
                    )
                }
            )
        }
    }

    sealed class Command {
        data class SetObjectType(val name: String?) : Command()
        data class Toast(val msg: String) : Command()
        data class NavigateToObjectTypesScreen(
            val smartBlockType: SmartBlockType,
            val exclucedTypes: List<Id>
        ) : Command()

        object ShowClearCacheAlert : Command()
        object Exit : Command()
    }

    companion object {
        private val DEFAULT_SETTINGS_SMART_BLOCK_TYPE = SmartBlockType.PAGE
    }

    class Factory(
        private val getDefaultEditorType: GetDefaultEditorType,
        private val setDefaultEditorType: SetDefaultEditorType,
        private val clearFileCache: ClearFileCache,
        private val appActionManager: AppActionManager,
        private val analytics: Analytics
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T = OtherSettingsViewModel(
            getDefaultEditorType = getDefaultEditorType,
            setDefaultEditorType = setDefaultEditorType,
            clearFileCache = clearFileCache,
            appActionManager = appActionManager,
            analytics = analytics
        ) as T
    }
}