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
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Interactor
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.device.ClearFileCache
import com.anytypeio.anytype.domain.launch.GetDefaultPageType
import com.anytypeio.anytype.domain.launch.SetDefaultObjectType
import com.anytypeio.anytype.domain.misc.AppActionManager
import com.anytypeio.anytype.domain.workspace.SpaceManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class OtherSettingsViewModel(
    private val getDefaultPageType: GetDefaultPageType,
    private val setDefaultObjectType: SetDefaultObjectType,
    private val clearFileCache: ClearFileCache,
    private val appActionManager: AppActionManager,
    private val analytics: Analytics,
    private val spaceManager: SpaceManager
) : ViewModel() {

    val commands = MutableSharedFlow<Command>(replay = 0)
    private val isClearFileCacheInProgress = MutableStateFlow(false)
    val defaultObjectTypeName = MutableStateFlow<String?>(null)
    private val defaultObjectTypeKey = MutableStateFlow<TypeKey?>(null)

    init {
        viewModelScope.launch {
            getDefaultPageType.execute(Unit).fold(
                onFailure = { e ->
                    Timber.e(e, "Error while getting user settings")
                },
                onSuccess = {
                    defaultObjectTypeKey.value = it.type
                    defaultObjectTypeName.value = it.name
                }
            )
        }
    }

    fun onObjectTypeClicked() {
        viewModelScope.launch {
            commands.emit(
                Command.NavigateToObjectTypesScreen(
                    excludeTypes = listOf(defaultObjectTypeKey.value?.key.orEmpty())
                )
            )
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
            val params = SetDefaultObjectType.Params(
                space = SpaceId(spaceManager.get()),
                type = TypeId(type)
            )
            setDefaultObjectType.async(params).fold(
                onFailure = {
                    Timber.e(it, "Error while setting default object type")
                    commands.emit(Command.Toast(msg = "Error while setting default object type"))
                },
                onSuccess = {
                    defaultObjectTypeName.value = name
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
        data class Toast(val msg: String) : Command()
        data class NavigateToObjectTypesScreen(
            val excludeTypes: List<Id>
        ) : Command()

        object ShowClearCacheAlert : Command()
        object Exit : Command()
    }

    class Factory(
        private val getDefaultPageType: GetDefaultPageType,
        private val setDefaultObjectType: SetDefaultObjectType,
        private val clearFileCache: ClearFileCache,
        private val appActionManager: AppActionManager,
        private val analytics: Analytics,
        private val spaceManager: SpaceManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T = OtherSettingsViewModel(
            getDefaultPageType = getDefaultPageType,
            setDefaultObjectType = setDefaultObjectType,
            clearFileCache = clearFileCache,
            appActionManager = appActionManager,
            analytics = analytics,
            spaceManager = spaceManager
        ) as T
    }
}