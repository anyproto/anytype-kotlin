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
import com.anytypeio.anytype.core_models.primitives.Space
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.base.onFailure
import com.anytypeio.anytype.domain.base.onSuccess
import com.anytypeio.anytype.domain.misc.AppActionManager
import com.anytypeio.anytype.domain.misc.DeepLinkResolver
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.SpaceInviteResolver
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.search.ProfileSubscriptionManager
import com.anytypeio.anytype.domain.spaces.SaveCurrentSpace
import com.anytypeio.anytype.domain.vault.GetVaultSettings
import com.anytypeio.anytype.domain.vault.ObserveVaultSettings
import com.anytypeio.anytype.domain.vault.SetVaultSettings
import com.anytypeio.anytype.domain.vault.SetVaultSpaceOrder
import com.anytypeio.anytype.domain.wallpaper.GetSpaceWallpapers
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.confgs.ChatConfig
import com.anytypeio.anytype.presentation.home.OpenObjectNavigation
import com.anytypeio.anytype.presentation.home.navigation
import com.anytypeio.anytype.presentation.navigation.DeepLinkToObjectDelegate
import com.anytypeio.anytype.presentation.navigation.NavigationViewModel
import com.anytypeio.anytype.presentation.profile.AccountProfile
import com.anytypeio.anytype.presentation.profile.profileIcon
import com.anytypeio.anytype.presentation.spaces.SpaceGradientProvider
import com.anytypeio.anytype.presentation.spaces.SpaceIconView
import com.anytypeio.anytype.presentation.spaces.spaceIcon
import com.anytypeio.anytype.presentation.vault.VaultViewModel.Navigation.*
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
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
    private val appActionManager: AppActionManager,
    private val spaceInviteResolver: SpaceInviteResolver,
    private val profileContainer: ProfileSubscriptionManager
) : NavigationViewModel<VaultViewModel.Navigation>(), DeepLinkToObjectDelegate by deepLinkToObjectDelegate {

    val spaces = MutableStateFlow<List<VaultSpaceView>>(emptyList())
    val commands = MutableSharedFlow<Command>(replay = 0)

    val profileView = profileContainer.observe().map { obj ->
        AccountProfile.Data(
            name = obj.name.orEmpty(),
            icon = obj.profileIcon(urlBuilder)
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(1000L),
        AccountProfile.Idle
    )

    init {
        Timber.i("VaultViewModel, init")
        viewModelScope.launch {
            val wallpapers = getSpaceWallpapers.async(Unit).getOrNull() ?: emptyMap()
            spaceViewSubscriptionContainer
                .observe()
                .take(1)
                .onCompletion {
                    emitAll(
                        spaceViewSubscriptionContainer
                            .observe()
                            .debounce(SPACE_VAULT_DEBOUNCE_DURATION)
                    )
                }
                .combine(observeVaultSettings.flow()) { spaces, settings ->
                    spaces
                        .filter { space -> (space.isActive || space.isLoading) && !space.isJoining }
                        .distinctBy { it.id }
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
                analytics.sendEvent(eventName = EventsDictionary.switchSpace)
                spaceManager.set(targetSpace).fold(
                    onFailure = {
                        Timber.e(it, "Could not select space")
                    },
                    onSuccess = {
                        proceedWithSavingCurrentSpace(
                            targetSpace = targetSpace,
                            chat = view.space.chatId?.ifEmpty { null }
                        )
                    }
                )
            } else {
                Timber.e("Missing target space")
            }
        }
    }

    fun onSettingsClicked() {
        viewModelScope.launch {
            commands.emit(Command.OpenProfileSettings)
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
        Timber.d("onResume")
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
                    onDeepLinkToObjectAwait(
                        obj = deeplink.obj,
                        space = deeplink.space,
                        switchSpaceIfObjectFound = true
                    ).collect { result ->
                        when(result) {
                            is DeepLinkToObjectDelegate.Result.Error -> {
                                val link = deeplink.invite
                                if (link != null && result is DeepLinkToObjectDelegate.Result.Error.PermissionNeeded) {
                                    commands.emit(
                                        Command.Deeplink.Invite(
                                            link = spaceInviteResolver.createInviteLink(
                                                contentId = link.cid,
                                                encryptionKey = link.key
                                            )
                                        )
                                    )
                                } else {
                                    commands.emit(Command.Deeplink.DeepLinkToObjectNotWorking)
                                }
                            }
                            is DeepLinkToObjectDelegate.Result.Success -> {
                                proceedWithNavigation(result.obj.navigation())
                            }
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
        viewModelScope.launch {
            appActionManager.setup(AppActionManager.Action.ClearAll)
        }
    }

    private suspend fun proceedWithSavingCurrentSpace(
        targetSpace: String,
        chat: Id?
    ) {
        saveCurrentSpace.async(
            SaveCurrentSpace.Params(SpaceId(targetSpace))
        ).fold(
            onFailure = {
                Timber.e(it, "Error while saving current space on vault screen")
            },
            onSuccess = {
                if (chat != null && ChatConfig.isChatAllowed(space = targetSpace)) {
                    commands.emit(
                        Command.EnterSpaceLevelChat(
                            space = Space(targetSpace),
                            chat = chat
                        )
                    )
                } else {
                    commands.emit(
                        Command.EnterSpaceHomeScreen(
                            space = Space(targetSpace)
                        )
                    )
                }
            }
        )
    }

    private fun proceedWithNavigation(navigation: OpenObjectNavigation) {
        when(navigation) {
            is OpenObjectNavigation.OpenDataView -> {
                navigate(
                    OpenSet(
                        ctx = navigation.target,
                        space = navigation.space,
                        view = null
                    )
                )
            }
            is OpenObjectNavigation.OpenEditor -> {
                navigate(
                    OpenObject(
                        ctx = navigation.target,
                        space = navigation.space
                    )
                )
            }
            is OpenObjectNavigation.OpenChat -> {
                navigate(
                    OpenChat(
                        ctx = navigation.target,
                        space = navigation.space
                    )
                )
            }
            is OpenObjectNavigation.UnexpectedLayoutError -> {
                sendToast("Unexpected layout: ${navigation.layout}")
            }
            OpenObjectNavigation.NonValidObject -> {
                sendToast("Object id is missing")
            }
            is OpenObjectNavigation.OpenDateObject -> {
                navigate(
                    OpenDateObject(
                        ctx = navigation.target,
                        space = navigation.space
                    )
                )
            }
            is OpenObjectNavigation.OpenParticipant -> {
                navigate(
                    OpenParticipant(
                        ctx = navigation.target,
                        space = navigation.space
                    )
                )
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
        private val deepLinkToObjectDelegate: DeepLinkToObjectDelegate,
        private val appActionManager: AppActionManager,
        private val spaceInviteResolver: SpaceInviteResolver,
        private val profileContainer: ProfileSubscriptionManager
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
            deepLinkToObjectDelegate = deepLinkToObjectDelegate,
            appActionManager = appActionManager,
            spaceInviteResolver = spaceInviteResolver,
            profileContainer = profileContainer
        ) as T
    }

    data class VaultSpaceView(
        val space: ObjectWrapper.SpaceView,
        val icon: SpaceIconView,
        val wallpaper: Wallpaper = Wallpaper.Default
    )

    sealed class Command {
        data class EnterSpaceHomeScreen(val space: Space): Command()
        data class EnterSpaceLevelChat(val space: Space, val chat: Id): Command()
        data object CreateNewSpace: Command()
        data object OpenProfileSettings: Command()

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
        data class OpenChat(val ctx: Id, val space: Id) : Navigation()
        data class OpenObject(val ctx: Id, val space: Id) : Navigation()
        data class OpenSet(val ctx: Id, val space: Id, val view: Id?) : Navigation()
        data class OpenDateObject(val ctx: Id, val space: Id) : Navigation()
        data class OpenParticipant(val ctx: Id, val space: Id) : Navigation()
    }

    companion object {
        const val SPACE_VAULT_DEBOUNCE_DURATION = 300L
    }
}