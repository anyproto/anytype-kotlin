package com.anytypeio.anytype.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary.defaultTypeChanged
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.event.EventAnalytics
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Interactor
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.device.ClearFileCache
import com.anytypeio.anytype.domain.launch.GetDefaultObjectType
import com.anytypeio.anytype.domain.launch.SetDefaultObjectType
import com.anytypeio.anytype.domain.misc.AppActionManager
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.workspace.SpaceManager
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class PersonalizationSettingsViewModel(
    private val getDefaultObjectType: GetDefaultObjectType,
    private val setDefaultObjectType: SetDefaultObjectType,
    private val clearFileCache: ClearFileCache,
    private val appActionManager: AppActionManager,
    private val analytics: Analytics,
    private val spaceManager: SpaceManager,
    private val searchObjects: SearchObjects
) : ViewModel() {

    val commands = MutableSharedFlow<Command>(replay = 0)
    private val isClearFileCacheInProgress = MutableStateFlow(false)
    val defaultObjectTypeName = MutableStateFlow<String?>(null)
    private val defaultObjectTypeKey = MutableStateFlow<TypeKey?>(null)

    init {
        viewModelScope.launch {
            getDefaultObjectType.execute(Unit).fold(
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

    fun onWallpaperClicked() {
        viewModelScope.launch {
            commands.emit(Command.NavigateToWallpaperScreen)
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

    fun proceedWithUpdateType(type: Id, key: Key, name: String?) {
        viewModelScope.launch {
            val keys = buildSet {
                add(key)
                add(ObjectTypeUniqueKeys.NOTE)
                add(ObjectTypeUniqueKeys.PAGE)
                add(ObjectTypeUniqueKeys.TASK)
            }
            searchObjects(
                SearchObjects.Params(
                    keys = buildList {
                        add(Relations.ID)
                        add(Relations.UNIQUE_KEY)
                        add(Relations.NAME)
                    },
                    filters = buildList {
                        add(
                            DVFilter(
                                relation = Relations.SPACE_ID,
                                value = spaceManager.get(),
                                condition = DVFilterCondition.EQUAL
                            )
                        )
                        add(
                            DVFilter(
                                relation = Relations.LAYOUT,
                                value = ObjectType.Layout.OBJECT_TYPE.code.toDouble(),
                                condition = DVFilterCondition.EQUAL
                            )
                        )
                        add(
                            DVFilter(
                                relation = Relations.UNIQUE_KEY,
                                value = keys.toList(),
                                condition = DVFilterCondition.IN
                            )
                        )
                    }
                )
            ).process(
                success = { wrappers ->
                    val types = wrappers
                        .map { ObjectWrapper.Type(it.map) }
                        .sortedBy { keys.indexOf(it.uniqueKey) }

                    val actions = types.map { type ->
                        AppActionManager.Action.CreateNew(
                            type = TypeKey(type.uniqueKey),
                            name = type.name.orEmpty()
                        )
                    }
                    appActionManager.setup(actions = actions)
                },
                failure = {
                    Timber.e(it, "Error while searching for types")
                }
            )
        }
        viewModelScope.launch {
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
                            props = Props(
                                mapOf(
                                    EventsPropertiesKey.objectType to key,
                                    EventsPropertiesKey.route to "Settings"
                                )
                            ),
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
        object NavigateToWallpaperScreen: Command()
        object ShowClearCacheAlert : Command()
        object Exit : Command()
    }

    class Factory @Inject constructor(
        private val getDefaultObjectType: GetDefaultObjectType,
        private val setDefaultObjectType: SetDefaultObjectType,
        private val clearFileCache: ClearFileCache,
        private val appActionManager: AppActionManager,
        private val analytics: Analytics,
        private val spaceManager: SpaceManager,
        private val searchObjects: SearchObjects
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T = PersonalizationSettingsViewModel(
            getDefaultObjectType = getDefaultObjectType,
            setDefaultObjectType = setDefaultObjectType,
            clearFileCache = clearFileCache,
            appActionManager = appActionManager,
            analytics = analytics,
            spaceManager = spaceManager,
            searchObjects = searchObjects
        ) as T
    }
}