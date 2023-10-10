package com.anytypeio.anytype.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.core_models.Filepath
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_utils.ext.throttleFirst
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.debugging.DebugSpaceShareDownloader
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer.Companion.SUBSCRIPTION_SETTINGS
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.presentation.profile.ProfileIconView
import com.anytypeio.anytype.presentation.profile.profileIcon
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.spaces.spaceIcon
import javax.inject.Named
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber

class MainSettingsViewModel(
    private val analytics: Analytics,
    private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
    private val configStorage: ConfigStorage,
    private val urlBuilder: UrlBuilder,
    private val setObjectDetails: SetObjectDetails,
    private val spaceGradientProvider: SpaceGradientProvider,
    private val debugSpaceShareDownloader: DebugSpaceShareDownloader
) : ViewModel() {

    val events = MutableSharedFlow<Event>(replay = 0)
    val commands = MutableSharedFlow<Command>(replay = 0)

    private val profileId = configStorage.getOrNull()?.profile
    private val workspaceId = configStorage.getOrNull()?.workspace

    val workspaceAndAccount = if (workspaceId != null && profileId != null) {
        storelessSubscriptionContainer.subscribe(
            StoreSearchByIdsParams(
                subscription = SPACE_STORAGE_SUBSCRIPTION_ID,
                targets = listOf(workspaceId, profileId),
                keys = listOf(
                    Relations.ID,
                    Relations.NAME,
                    Relations.ICON_EMOJI,
                    Relations.ICON_IMAGE,
                    Relations.ICON_OPTION
                )
            )
        ).map { result ->
            val workspace = result.find { it.id == workspaceId }
            val profile = result.find { it.id == profileId }
            WorkspaceAndAccount.Account(
                space = workspace?.let {
                    WorkspaceAndAccount.SpaceData(
                        name = workspace.name ?: "",
                        icon = workspace.spaceIcon(urlBuilder, spaceGradientProvider)
                    )
                },
                profile = profile?.let {
                    WorkspaceAndAccount.ProfileData(
                        name = profile.name ?: "",
                        icon = profile.profileIcon(urlBuilder, spaceGradientProvider)
                    )
                }
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_SUBSCRIPTION_TIMEOUT),
            WorkspaceAndAccount.Idle
        )
    } else {
        MutableStateFlow(WorkspaceAndAccount.Idle)
    }

    init {
        events
            .throttleFirst()
            .onEach { event -> dispatchAnalyticEvent(event) }
            .onEach { event -> dispatchCommand(event) }
            .launchIn(viewModelScope)
    }

    fun onOptionClicked(event: Event) {
        viewModelScope.launch { events.emit(event) }
    }

    private suspend fun dispatchCommand(event: Event) {
        when (event) {
            Event.OnAboutClicked -> commands.emit(Command.OpenAboutScreen)
            Event.OnProfileClicked -> commands.emit(Command.OpenProfileScreen)
            Event.OnAppearanceClicked -> commands.emit(Command.OpenAppearanceScreen)
            Event.OnPersonalizationClicked -> commands.emit(Command.OpenPersonalizationScreen)
            Event.OnDebugClicked -> {
                proceedWithSpaceDebug()
            }
            Event.OnSpaceImageClicked -> {
                val config = configStorage.getOrNull()
                if (config != null) {
                    commands.emit(
                        Command.OpenSpaceImageSet(
                            id = config.workspace,
                            showRemoveButton = isShowRemoveButton()
                        )
                    )
                } else {
                    commands.emit(Command.Toast(COULD_NOT_GET_CONFIG_ERROR))
                }
            }
            Event.OnFilesStorageClicked -> {
                commands.emit(Command.OpenFilesStorageScreen)
            }
        }
    }

    private fun isShowRemoveButton() : Boolean {
        return (workspaceAndAccount.value as? WorkspaceAndAccount.Account)
            ?.space?.icon !is SpaceIconView.Gradient
    }

    private fun proceedWithSpaceDebug() {
        viewModelScope.launch {
            debugSpaceShareDownloader
                .stream(Unit)
                .collect { result ->
                    result.fold(
                        onLoading = {
                            commands.emit(
                                Command.Toast(SPACE_DEBUG_MSG, isLongDuration = true)
                            )
                        },
                        onSuccess = { path ->
                            commands.emit(
                                Command.ShareSpaceDebug(path)
                            )
                        }
                    )
                }
        }
    }

    private fun dispatchAnalyticEvent(event: Event) {
        when (event) {
            Event.OnAboutClicked -> {
                viewModelScope.sendEvent(
                    analytics = analytics,
                    eventName = EventsDictionary.MENU_HELP
                )
            }
            Event.OnProfileClicked -> {
                viewModelScope.sendEvent(
                    analytics = analytics,
                    eventName = EventsDictionary.accountDataSettingsShow
                )
            }
            Event.OnAppearanceClicked -> {
                viewModelScope.sendEvent(
                    analytics = analytics,
                    eventName = EventsDictionary.appearanceScreenShow
                )
            }
            Event.OnPersonalizationClicked -> {
                viewModelScope.sendEvent(
                    analytics = analytics,
                    eventName = EventsDictionary.personalisationSettingsShow
                )
            }
            Event.OnSpaceImageClicked -> {}
            Event.OnDebugClicked -> {}
            Event.OnFilesStorageClicked -> {}
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            storelessSubscriptionContainer.unsubscribe(
                listOf(SPACE_STORAGE_SUBSCRIPTION_ID)
            )
        }
    }

    fun onNameSet(name: String) {
        viewModelScope.launch {
            setObjectDetails.execute(
                SetObjectDetails.Params(
                    ctx = configStorage.get().workspace,
                    details = mapOf(
                        Relations.NAME to name
                    )
                )
            ).fold(
                onFailure = {
                    Timber.e(it, "Error while updating object details")
                },
                onSuccess = {
                    // do nothing
                }
            )
        }
    }

    class Factory(
        private val analytics: Analytics,
        private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
        private val configStorage: ConfigStorage,
        private val urlBuilder: UrlBuilder,
        private val setObjectDetails: SetObjectDetails,
        private val spaceGradientProvider: SpaceGradientProvider,
        private val debugSpaceShareDownloader: DebugSpaceShareDownloader
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ): T = MainSettingsViewModel(
            analytics = analytics,
            storelessSubscriptionContainer = storelessSubscriptionContainer,
            configStorage = configStorage,
            urlBuilder = urlBuilder,
            setObjectDetails = setObjectDetails,
            spaceGradientProvider = spaceGradientProvider,
            debugSpaceShareDownloader = debugSpaceShareDownloader
        ) as T
    }

    sealed class Event {
        object OnAboutClicked : Event()
        object OnAppearanceClicked : Event()
        object OnProfileClicked : Event()
        object OnPersonalizationClicked : Event()
        object OnDebugClicked : Event()
        object OnSpaceImageClicked : Event()
        object OnFilesStorageClicked : Event()
    }

    sealed class Command {
        object OpenAboutScreen : Command()
        object OpenAppearanceScreen : Command()
        object OpenProfileScreen : Command()
        object OpenPersonalizationScreen : Command()
        object OpenDebugScreen : Command()
        class OpenSpaceImageSet(val id: Id, val showRemoveButton: Boolean) : Command()
        object OpenFilesStorageScreen : Command()
        data class Toast(val msg: String, val isLongDuration: Boolean = false) : Command()
        data class ShareSpaceDebug(val path: Filepath): Command()
    }

    sealed class WorkspaceAndAccount {
        object Idle : WorkspaceAndAccount()
        class Account(
            val space: SpaceData?,
            val profile: ProfileData?
        ) : WorkspaceAndAccount()

        data class SpaceData(
            val name: String,
            val icon: SpaceIconView
        )

        data class ProfileData(
            val name: String,
            val icon: ProfileIconView,
        )
    }

    companion object {
        const val SPACE_DEBUG_MSG = "Kindly share this debug logs with Anytype developers."
        const val COULD_NOT_GET_CONFIG_ERROR = "Could not get config. Please, try again later."
    }
}

const val SPACE_STORAGE_SUBSCRIPTION_ID = "settings_space_storage_subscription"
const val STOP_SUBSCRIPTION_TIMEOUT = 1_000L