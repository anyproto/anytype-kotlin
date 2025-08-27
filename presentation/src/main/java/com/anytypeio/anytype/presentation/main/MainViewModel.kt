package com.anytypeio.anytype.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.updateUserProperties
import com.anytypeio.anytype.analytics.props.UserProperty
import com.anytypeio.anytype.core_models.AccountStatus
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Notification
import com.anytypeio.anytype.core_models.NotificationPayload
import com.anytypeio.anytype.core_models.NotificationStatus
import com.anytypeio.anytype.core_models.Wallpaper
import com.anytypeio.anytype.core_models.exceptions.NeedToUpdateApplicationException
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.domain.account.AwaitAccountStartManager
import com.anytypeio.anytype.domain.account.InterceptAccountStatus
import com.anytypeio.anytype.domain.auth.interactor.CheckAuthorizationStatus
import com.anytypeio.anytype.domain.auth.interactor.Logout
import com.anytypeio.anytype.domain.auth.interactor.ResumeAccount
import com.anytypeio.anytype.domain.auth.model.AuthStatus
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Interactor
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.deeplink.PendingIntentStore
import com.anytypeio.anytype.domain.misc.DeepLinkResolver
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.multiplayer.SpaceInviteResolver
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.notifications.SystemNotificationService
import com.anytypeio.anytype.domain.subscriptions.GlobalSubscriptionManager
import com.anytypeio.anytype.domain.wallpaper.ObserveWallpaper
import com.anytypeio.anytype.domain.wallpaper.RestoreWallpaper
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.home.OpenObjectNavigation
import com.anytypeio.anytype.presentation.home.navigation
import com.anytypeio.anytype.presentation.membership.provider.MembershipProvider
import com.anytypeio.anytype.presentation.navigation.DeepLinkToObjectDelegate
import com.anytypeio.anytype.presentation.notifications.NotificationAction
import com.anytypeio.anytype.presentation.notifications.NotificationActionDelegate
import com.anytypeio.anytype.presentation.notifications.NotificationsProvider
import com.anytypeio.anytype.presentation.splash.SplashViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber

class MainViewModel(
    private val resumeAccount: ResumeAccount,
    private val restoreWallpaper: RestoreWallpaper,
    private val analytics: Analytics,
    private val interceptAccountStatus: InterceptAccountStatus,
    private val logout: Logout,
    private val checkAuthorizationStatus: CheckAuthorizationStatus,
    private val configStorage: ConfigStorage,
    private val localeProvider: LocaleProvider,
    private val notificationsProvider: NotificationsProvider,
    private val notificator: SystemNotificationService,
    private val notificationActionDelegate: NotificationActionDelegate,
    private val deepLinkToObjectDelegate: DeepLinkToObjectDelegate,
    private val awaitAccountStartManager: AwaitAccountStartManager,
    private val membershipProvider: MembershipProvider,
    private val globalSubscriptionManager: GlobalSubscriptionManager,
    private val spaceInviteResolver: SpaceInviteResolver,
    private val spaceManager: SpaceManager,
    private val spaceViews: SpaceViewSubscriptionContainer,
    private val pendingIntentStore: PendingIntentStore
) : ViewModel(),
    NotificationActionDelegate by notificationActionDelegate,
    DeepLinkToObjectDelegate by deepLinkToObjectDelegate {

    private val deepLinkJobs = mutableListOf<Job>()

    val commands = MutableSharedFlow<Command>(replay = 0)
    val toasts = MutableSharedFlow<String>(replay = 0)

    init {
        viewModelScope.launch {
            restoreWallpaper.flow().collect {
                // Do nothing, just run pipeline.
            }
        }
        viewModelScope.launch {
            interceptAccountStatus.build().collect { status ->
                when (status) {
                    is AccountStatus.PendingDeletion -> {
                        commands.emit(
                            Command.ShowDeletedAccountScreen(
                                deadline = status.deadline
                            )
                        )
                    }
                    is AccountStatus.Deleted -> {
                        proceedWithLogoutDueToAccountDeletion()
                    }
                    else -> {
                        // Do nothing
                    }
                }
            }
        }
        viewModelScope.launch {
            notificationsProvider.events.collect { notifications ->
                notifications.forEach { event ->
                    handleNotification(event)
                }
            }
        }
        viewModelScope.launch {
            membershipProvider.activeTier().collect { result ->
                updateUserProperties(
                    analytics = analytics,
                    userProperty = UserProperty.Tier(result.value)
                )
            }
        }
    }

    private suspend fun handleNotification(event: Notification.Event) {
        val notification = event.notification
        if (notification != null) {
            if (notification.payload is NotificationPayload.GalleryImport) {
                // TODO migrate to system notifications
                delay(DELAY_BEFORE_SHOWING_NOTIFICATION_SCREEN)
                commands.emit(Command.Notifications)
            } else if (notification.status == NotificationStatus.CREATED) {
                if (notificator.areNotificationsEnabled) {
                    notificator.notify(notification)
                } else {
                    commands.emit(Command.RequestNotificationPermission).also {
                        notificator.setPendingNotification(notification)
                    }
                }
            }
        }
    }

    fun onNotificationPermissionGranted() {
        viewModelScope.launch {
            notificator.notifyIfPending()
        }
    }

    fun onNotificationPermissionDenied() {
        viewModelScope.launch {
            notificator.clearPendingNotification()
        }
    }

    private fun proceedWithLogoutDueToAccountDeletion() {
        viewModelScope.launch {
            logout(Logout.Params(clearLocalRepositoryData = false)).collect { status ->
                when (status) {
                    is Interactor.Status.Error -> {
                        toasts.emit("Error while logging out due to account deletion")
                    }
                    is Interactor.Status.Started -> {
                        toasts.emit("Your account is deleted. Logging out...")
                    }
                    is Interactor.Status.Success -> {
                        unsubscribeFromGlobalSubscriptions()
                        commands.emit(Command.LogoutDueToAccountDeletion)
                    }
                }
            }
        }
    }

    private fun unsubscribeFromGlobalSubscriptions() {
        globalSubscriptionManager.onStop()
    }

    fun onRestore() {
        Timber.d("onRestoreCalled")
        /***
         * Before fragment backstack and screen states are restored by the OS,
         * We need to resume account session in a blocking manner.
         * Otherwise, we might run into illegal states, where account is not ready when we try:
         * 1) to open an object, profile or dashboard
         * 2) to execute queries and searches
         * etc.
         */
        runBlocking {
            resumeAccount.run(params = BaseUseCase.None).process(
                success = {
                    globalSubscriptionManager.onStart()
                    val analyticsID = configStorage.getOrNull()?.analytics
                    if (analyticsID != null) {
                        updateUserProperties(
                            analytics = analytics,
                            userProperty = UserProperty.AccountId(analyticsID)
                        )
                        localeProvider.language()?.let { lang ->
                            updateUserProperties(
                                analytics,
                                userProperty = UserProperty.InterfaceLanguage(lang)
                            )
                        }
                    }
                    Timber.d("Restored account after activity recreation")
                },
                failure = { error ->
                    when (error) {
                        is NeedToUpdateApplicationException -> {
                            commands.emit(Command.Error(SplashViewModel.ERROR_NEED_UPDATE))
                        }
                        else -> {
                            commands.emit(Command.Error(SplashViewModel.ERROR_MESSAGE))
                        }
                    }
                    Timber.e(error, "Error while launching account after activity recreation")
                }
            )
        }
    }

    fun onIntentCreateObject(type: Id) {
        Timber.d("onIntentCreateObject: $type")
        viewModelScope.launch {
            checkAuthorizationStatus(Unit).process(
                failure = { e -> Timber.e(e, "Error while checking auth status") },
                success = { status ->
                    if (status == AuthStatus.AUTHORIZED) {
                        commands.emit(Command.OpenCreateNewType(type))
                    }
                }
            )
        }
    }

    fun onIntentTextShare(data: String) {
        viewModelScope.launch {
            checkAuthorizationStatus(Unit).process(
                failure = { e -> Timber.e(e, "Error while checking auth status") },
                success = { status ->
                    if (status == AuthStatus.AUTHORIZED) {
                        commands.emit(Command.Sharing.Text(data))
                    }
                }
            )
        }
    }

    fun onIntentMultipleFilesShare(uris: List<String>) {
        Timber.d("onIntentFileShare: $uris")
        viewModelScope.launch {
            checkAuthorizationStatus(Unit).process(
                failure = { e -> Timber.e(e, "Error while checking auth status") },
                success = { status ->
                    if (status == AuthStatus.AUTHORIZED) {
                        if (uris.size == 1) {
                            commands.emit(Command.Sharing.File(uris.first()))
                        } else {
                            commands.emit(Command.Sharing.Files(uris))
                        }
                    }
                }
            )
        }
    }

    fun onIntentMultipleImageShare(uris: List<String>) {
        Timber.d("onIntentImageShare: $uris")
        viewModelScope.launch {
            checkAuthorizationStatus(Unit).process(
                failure = { e -> Timber.e(e, "Error while checking auth status") },
                success = { status ->
                    if (status == AuthStatus.AUTHORIZED) {
                        if (uris.size == 1) {
                            commands.emit(Command.Sharing.Image(uris.first()))
                        } else {
                            commands.emit(Command.Sharing.Images(uris))
                        }
                    }
                }
            )
        }
    }

    fun onIntentMultipleVideoShare(uris: List<String>) {
        Timber.d("onIntentVideoShare: $uris")
        viewModelScope.launch {
            checkAuthorizationStatus(Unit).process(
                failure = { e -> Timber.e(e, "Error while checking auth status") },
                success = { status ->
                    if (status == AuthStatus.AUTHORIZED) {
                        commands.emit(Command.Sharing.Videos(uris))
                    }
                }
            )
        }
    }

    fun onInterceptNotificationAction(action: NotificationAction) {
        viewModelScope.launch {
            proceedWithNotificationAction(action)
        }
    }

    fun handleNewDeepLink(deeplink: DeepLinkResolver.Action) {
        deepLinkJobs.cancel()
        viewModelScope.launch {
            checkAuthorizationStatus(Unit).process(
                failure = { Timber.e(it, "Failed to check authentication status") },
                success = { authStatus -> processDeepLinkBasedOnAuth(authStatus, deeplink) }
            )
        }
    }

    private fun processDeepLinkBasedOnAuth(
        authStatus: AuthStatus,
        deeplink: DeepLinkResolver.Action
    ) {
        if (authStatus == AuthStatus.UNAUTHORIZED && deeplink is DeepLinkResolver.Action.Invite) {
            saveInviteDeepLinkForLater(deeplink)
        } else {
            Timber.d("Proceeding with deeplink: $deeplink")
            launchDeepLinkProcessing(deeplink)
        }
    }

    private fun saveInviteDeepLinkForLater(deeplink: DeepLinkResolver.Action.Invite) {
        pendingIntentStore.setDeepLinkInvite(deeplink.link)
        Timber.d("Saved invite deeplink for later processing: ${deeplink.link}")
    }

    private fun launchDeepLinkProcessing(deeplink: DeepLinkResolver.Action) {
        deepLinkJobs += viewModelScope.launch {
            awaitAccountStartManager
                .awaitStart()
                .onEach { delay(NEW_DEEP_LINK_DELAY) }
                .take(1)
                .collect {
                    proceedWithNewDeepLink(deeplink)
                }
        }
    }

    private suspend fun proceedWithNewDeepLink(deeplink: DeepLinkResolver.Action) {
        Timber.d("Proceeding with the new deep link")
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
                commands.emit(Command.Deeplink.Invite(deeplink.link))
            }
            is DeepLinkResolver.Action.DeepLinkToObject -> {
                val result = onDeepLinkToObject(
                    obj = deeplink.obj,
                    space = deeplink.space,
                    switchSpaceIfObjectFound = false
                )
                when (result) {
                    is DeepLinkToObjectDelegate.Result.Error -> {
                        val link = deeplink.invite
                        if (link != null) {
                            commands.emit(
                                Command.Deeplink.Invite(
                                    spaceInviteResolver.createInviteLink(
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
                        val targetSpace = result.obj.spaceId
                        if (targetSpace != null) {
                            val currentState = spaceManager.getState()
                            Timber.d("Space manager state before processing deep link: $currentState")
                            when(currentState) {
                                SpaceManager.State.Init -> {
                                    // Do nothing.
                                }
                                SpaceManager.State.NoSpace -> {
                                    proceedWithSpaceSwitchDueToDeepLinkToObject(
                                        targetSpace = targetSpace,
                                        deeplink = deeplink,
                                        result = result
                                    )
                                }
                                is SpaceManager.State.Space.Idle -> {
                                    if (currentState.space.id != deeplink.space.id) {
                                        proceedWithSpaceSwitchDueToDeepLinkToObject(
                                            targetSpace = targetSpace,
                                            deeplink = deeplink,
                                            result = result
                                        )
                                    } else {
                                        commands.emit(
                                            Command.Deeplink.DeepLinkToObject(
                                                navigation = result.obj.navigation(),
                                                sideEffect = null,
                                                space = deeplink.space.id,
                                                obj = deeplink.obj
                                            ),
                                        )
                                    }
                                }
                                is SpaceManager.State.Space.Active -> {
                                    if (currentState.config.space != deeplink.space.id) {
                                        proceedWithSpaceSwitchDueToDeepLinkToObject(
                                            targetSpace = targetSpace,
                                            deeplink = deeplink,
                                            result = result
                                        )
                                    } else {
                                        commands.emit(
                                            Command.Deeplink.DeepLinkToObject(
                                                navigation = result.obj.navigation(),
                                                sideEffect = null,
                                                space = deeplink.space.id,
                                                obj = deeplink.obj
                                            ),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            is DeepLinkResolver.Action.DeepLinkToMembership -> {
                commands.emit(
                    Command.Deeplink.MembershipScreen(tierId = deeplink.tierId)
                )
            }
            else -> {
                Timber.d("No deep link")
            }
        }
    }

    private suspend fun proceedWithSpaceSwitchDueToDeepLinkToObject(
        targetSpace: Id,
        deeplink: DeepLinkResolver.Action.DeepLinkToObject,
        result: DeepLinkToObjectDelegate.Result.Success
    ) {
        spaceManager
            .set(targetSpace)
            .onSuccess { config ->
                val home = config.home
                val spaceView = spaceViews
                    .get(deeplink.space)
                val chat = spaceView?.chatId?.ifEmpty { null }
                val sideEffect = Command.Deeplink.DeepLinkToObject.SideEffect.SwitchSpace(
                    chat = chat,
                    home = home
                )
                commands.emit(
                    Command.Deeplink.DeepLinkToObject(
                        navigation = result.obj.navigation(),
                        sideEffect = sideEffect,
                        space = deeplink.space.id,
                        obj = deeplink.obj
                    ),
                )
            }
    }

    fun onOpenChatTriggeredByPush(chatId: String, spaceId: String) {
        Timber.d("onOpenChatTriggeredByPush: $chatId, $spaceId")
        viewModelScope.launch {
            if (spaceManager.get() != spaceId) {
                spaceManager.set(spaceId)
                    .onSuccess {
                        commands.emit(
                            Command.LaunchChat(
                                space = spaceId,
                                chat = chatId
                            )
                        )
                    }.onFailure {
                        Timber.e(it, "Error while switching space when launching chat from push notification")
                    }
            } else {
                commands.emit(
                    Command.LaunchChat(
                        space = spaceId,
                        chat = chatId
                    )
                )
            }
        }
    }

    sealed class Command {
        data class ShowDeletedAccountScreen(val deadline: Long) : Command()
        data object LogoutDueToAccountDeletion : Command()
        class OpenCreateNewType(val type: Id) : Command()
        data class Error(val msg: String) : Command()
        sealed class Sharing : Command() {
            data class Text(val data: String) : Sharing()
            data class Image(val uri: String): Sharing()
            data class Images(val uris: List<String>): Sharing()
            data class Videos(val uris: List<String>): Sharing()
            data class File(val uri: String): Sharing()
            data class Files(val uris: List<String>): Sharing()
        }
        data object Notifications: Command()
        data object RequestNotificationPermission: Command()

        data class LaunchChat(
            val space: Id,
            val chat: Id
        ): Command()

        data class Navigate(val destination: OpenObjectNavigation): Command()

        sealed class Deeplink : Command() {
            data object DeepLinkToObjectNotWorking: Deeplink()
            data class DeepLinkToObject(
                val obj: Id,
                val space: Id,
                val navigation: OpenObjectNavigation,
                val sideEffect: SideEffect? = null
            ) : Deeplink() {
                sealed class SideEffect {
                    data class SwitchSpace(
                        val home: Id,
                        val chat: Id? = null,
                        val spaceUxType: SpaceUxType? = null
                    ): SideEffect()
                }
            }
            data class Invite(val link: String) : Deeplink()
            data class GalleryInstallation(
                val deepLinkType: String,
                val deepLinkSource: String
            ) : Deeplink()
            data class MembershipScreen(val tierId: String?) : Deeplink()
        }
    }

    companion object {
        const val DELAY_BEFORE_SHOWING_NOTIFICATION_SCREEN = 200L
        const val NEW_DEEP_LINK_DELAY = 1000L
    }
}