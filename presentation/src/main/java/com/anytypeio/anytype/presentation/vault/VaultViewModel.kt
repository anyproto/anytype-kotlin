package com.anytypeio.anytype.presentation.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Wallpaper
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.restrictions.SpaceStatus
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.base.onSuccess
import com.anytypeio.anytype.domain.misc.DeepLinkResolver
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.spaces.SaveCurrentSpace
import com.anytypeio.anytype.domain.vault.GetVaultSettings
import com.anytypeio.anytype.domain.vault.ObserveVaultSettings
import com.anytypeio.anytype.domain.vault.SetVaultSettings
import com.anytypeio.anytype.domain.vault.SetVaultSpaceOrder
import com.anytypeio.anytype.domain.wallpaper.GetSpaceWallpapers
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.home.Command
import com.anytypeio.anytype.presentation.home.OpenObjectNavigation
import com.anytypeio.anytype.presentation.home.navigation
import com.anytypeio.anytype.presentation.navigation.DeepLinkToObjectDelegate
import com.anytypeio.anytype.presentation.navigation.NavigationViewModel
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.spaces.spaceIcon
import com.anytypeio.anytype.presentation.widgets.collection.Subscription
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber

class VaultViewModel(
    private val spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer,
    private val urlBuilder: UrlBuilder,
    private val getSpaceWallpapers: GetSpaceWallpapers,
    private val spaceManager: SpaceManager,
    private val saveCurrentSpace: SaveCurrentSpace,
    private val getVaultSettings: GetVaultSettings,
    private val setVaultSettings: SetVaultSettings,
    private val observeVaultSettings: ObserveVaultSettings,
    private val setVaultSpaceOrder: SetVaultSpaceOrder,
    private val analytics: Analytics,
    private val deepLinkToObjectDelegate: DeepLinkToObjectDelegate,
) : NavigationViewModel<VaultViewModel.Navigation>(), DeepLinkToObjectDelegate by deepLinkToObjectDelegate {

    val spaces = MutableStateFlow<List<VaultSpaceView>>(emptyList())
    val commands = MutableSharedFlow<Command>(replay = 0)

    init {
        Timber.i("VaultViewModel, init")
        viewModelScope.launch {
            val wallpapers = getSpaceWallpapers.async(Unit).getOrNull() ?: emptyMap()
            spaceViewSubscriptionContainer
                .observe()
                .combine(observeVaultSettings.flow()) { spaces, settings ->
                    spaces
                        .filter { space ->
                            space.spaceLocalStatus == SpaceStatus.OK
                                    && !space.spaceAccountStatus.isDeletedOrRemoving()
                        }
                        .map { space ->
                            VaultSpaceView(
                                space = space,
                                icon = space.spaceIcon(
                                    builder = urlBuilder,
                                    spaceGradientProvider = SpaceGradientProvider.Default
                                ),
                                wallpaper = wallpapers.getOrDefault(
                                    key = space.targetSpaceId,
                                    defaultValue = Wallpaper.Default
                                )
                            )
                        }.sortedBy { space ->
                            val idx = settings.orderOfSpaces.indexOf(
                                space.space.id
                            )
                            if (idx == -1) {
                                Int.MIN_VALUE
                            } else {
                                idx
                            }
                        }
                }.collect {
                    spaces.value = it
                }
        }
    }

    fun onSpaceClicked(view: VaultSpaceView) {
        Timber.i("onSpaceClicked")
        viewModelScope.launch {
            val targetSpace = view.space.targetSpaceId
            if (targetSpace != null) {
                spaceManager.set(targetSpace).fold(
                    onFailure = {
                        Timber.e(it, "Could not select space")
                    },
                    onSuccess = {
                        proceedWithSavingCurrentSpace(targetSpace)
                    }
                )
            } else {
                Timber.e("Missing target space")
            }
        }
    }

    fun onSettingsClicked() {
        viewModelScope.launch {
            val entrySpaceView = spaces.value.find { space ->
                space.space.spaceAccessType == SpaceAccessType.DEFAULT
            }
            if (entrySpaceView != null && entrySpaceView.space.targetSpaceId != null) {
                commands.emit(
                    Command.OpenProfileSettings(
                        space = SpaceId(requireNotNull(entrySpaceView.space.targetSpaceId))
                    )
                )
            } else {
                Timber.w("Entry space not found")
            }
        }
    }

    fun onOrderChanged(order: List<Id>) {
        viewModelScope.launch {
            setVaultSpaceOrder.async(
                params = order
            )
        }
    }

    fun onCreateSpaceClicked() {
        viewModelScope.launch { commands.emit(Command.CreateNewSpace) }
    }

    fun onResume(deeplink: DeepLinkResolver.Action? = null) {
        viewModelScope.launch {
            analytics.sendEvent(
                eventName = EventsDictionary.screenVault,
                props = Props(
                    map = mapOf(
                        EventsPropertiesKey.type to EventsDictionary.Type.general
                    )
                )
            )
        }
        viewModelScope.launch {
            getVaultSettings.async(Unit).onSuccess { settings ->
                if (settings.showIntroduceVault) {
                    commands.emit(Command.ShowIntroduceVault)
                    setVaultSettings.async(
                        params = settings.copy(
                            showIntroduceVault = false
                        )
                    )
                }
            }
        }
        viewModelScope.launch {
            when (deeplink) {
                is DeepLinkResolver.Action.Import.Experience -> {
                    commands.emit(
                        Command.Deeplink.GalleryInstallation(
                            deepLinkType = deeplink.type,
                            deepLinkSource = deeplink.source
                        )
                    )
                }

                is DeepLinkResolver.Action.Invite -> {
                    delay(1000)
                    commands.emit(Command.Deeplink.Invite(deeplink.link))
                }
                is DeepLinkResolver.Action.Unknown -> {
                    if (BuildConfig.DEBUG) {
                        sendToast("Could not resolve deeplink")
                    }
                }
                is DeepLinkResolver.Action.DeepLinkToObject -> {
                    val result = onDeepLinkToObject(
                        obj = deeplink.obj,
                        space = deeplink.space,
                        switchSpaceIfObjectFound = true
                    )
                    when(result) {
                        is DeepLinkToObjectDelegate.Result.Error -> {
                            commands.emit(Command.Deeplink.DeepLinkToObjectNotWorking)
                        }
                        is DeepLinkToObjectDelegate.Result.Success -> {
                            proceedWithNavigation(result.obj.navigation())
                        }
                    }
                }
                is DeepLinkResolver.Action.DeepLinkToMembership -> {
                    commands.emit(
                        Command.Deeplink.MembershipScreen(
                            tierId = deeplink.tierId
                        )
                    )
                }
                else -> {
                    Timber.d("No deep link")
                }
            }
        }
    }

    private suspend fun proceedWithSavingCurrentSpace(targetSpace: String) {
        saveCurrentSpace.async(
            SaveCurrentSpace.Params(SpaceId(targetSpace))
        ).fold(
            onFailure = {
                Timber.e(it, "Error while saving current space on vault screen")
            },
            onSuccess = {
                commands.emit(Command.EnterSpaceHomeScreen)
            }
        )
    }

    private fun proceedWithNavigation(navigation: OpenObjectNavigation) {
        when(navigation) {
            is OpenObjectNavigation.OpenDataView -> {
                navigate(
                    Navigation.OpenSet(
                        ctx = navigation.target,
                        space = navigation.space,
                        view = null
                    )
                )
            }
            is OpenObjectNavigation.OpenEditor -> {
                navigate(
                    Navigation.OpenObject(
                        ctx = navigation.target,
                        space = navigation.space
                    )
                )
            }
            is OpenObjectNavigation.UnexpectedLayoutError -> {
                sendToast("Unexpected layout: ${navigation.layout}")
            }
        }
    }

    class Factory @Inject constructor(
        private val spaceViewSubscriptionContainer: SpaceViewSubscriptionContainer,
        private val getSpaceWallpapers: GetSpaceWallpapers,
        private val urlBuilder: UrlBuilder,
        private val spaceManager: SpaceManager,
        private val saveCurrentSpace: SaveCurrentSpace,
        private val getVaultSettings: GetVaultSettings,
        private val setVaultSettings: SetVaultSettings,
        private val setVaultSpaceOrder: SetVaultSpaceOrder,
        private val observeVaultSettings: ObserveVaultSettings,
        private val analytics: Analytics,
        private val deepLinkToObjectDelegate: DeepLinkToObjectDelegate
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>
        ) = VaultViewModel(
            spaceViewSubscriptionContainer = spaceViewSubscriptionContainer,
            getSpaceWallpapers = getSpaceWallpapers,
            urlBuilder = urlBuilder,
            spaceManager = spaceManager,
            saveCurrentSpace = saveCurrentSpace,
            getVaultSettings = getVaultSettings,
            setVaultSettings = setVaultSettings,
            setVaultSpaceOrder = setVaultSpaceOrder,
            observeVaultSettings = observeVaultSettings,
            analytics = analytics,
            deepLinkToObjectDelegate = deepLinkToObjectDelegate
        ) as T
    }

    data class VaultSpaceView(
        val space: ObjectWrapper.SpaceView,
        val icon: SpaceIconView,
        val wallpaper: Wallpaper = Wallpaper.Default
    )

    sealed class Command {
        data object EnterSpaceHomeScreen: Command()
        data object CreateNewSpace: Command()
        data class OpenProfileSettings(val space: SpaceId): Command()
        data object ShowIntroduceVault : Command()

        sealed class Deeplink : Command() {
            data object DeepLinkToObjectNotWorking: Deeplink()
            data class Invite(val link: String) : Deeplink()
            data class GalleryInstallation(
                val deepLinkType: String,
                val deepLinkSource: String
            ) : Deeplink()
            data class MembershipScreen(val tierId: String?) : Deeplink()
        }
    }

    sealed class Navigation {
        data class OpenObject(val ctx: Id, val space: Id) : Navigation()
        data class OpenSet(val ctx: Id, val space: Id, val view: Id?) : Navigation()
    }
}