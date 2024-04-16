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
import com.anytypeio.anytype.core_models.Wallpaper
import com.anytypeio.anytype.core_models.exceptions.NeedToUpdateApplicationException
import com.anytypeio.anytype.domain.account.InterceptAccountStatus
import com.anytypeio.anytype.domain.auth.interactor.CheckAuthorizationStatus
import com.anytypeio.anytype.domain.auth.interactor.Logout
import com.anytypeio.anytype.domain.auth.interactor.ResumeAccount
import com.anytypeio.anytype.domain.auth.model.AuthStatus
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.Interactor
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.notifications.SystemNotificationService
import com.anytypeio.anytype.domain.search.ObjectTypesSubscriptionManager
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
import com.anytypeio.anytype.domain.spaces.SpaceDeletedStatusWatcher
import com.anytypeio.anytype.domain.wallpaper.ObserveWallpaper
import com.anytypeio.anytype.domain.wallpaper.RestoreWallpaper
import com.anytypeio.anytype.presentation.notifications.NotificationAction
import com.anytypeio.anytype.presentation.notifications.NotificationActionDelegate
import com.anytypeio.anytype.presentation.notifications.NotificationsProvider
import com.anytypeio.anytype.presentation.splash.SplashViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber

class MainViewModel(
    private val resumeAccount: ResumeAccount,
    private val observeWallpaper: ObserveWallpaper,
    private val restoreWallpaper: RestoreWallpaper,
    private val analytics: Analytics,
    private val interceptAccountStatus: InterceptAccountStatus,
    private val logout: Logout,
    private val relationsSubscriptionManager: RelationsSubscriptionManager,
    private val objectTypesSubscriptionManager: ObjectTypesSubscriptionManager,
    private val checkAuthorizationStatus: CheckAuthorizationStatus,
    private val configStorage: ConfigStorage,
    private val spaceDeletedStatusWatcher: SpaceDeletedStatusWatcher,
    private val localeProvider: LocaleProvider,
    private val userPermissionProvider: UserPermissionProvider,
    private val notificationsProvider: NotificationsProvider,
    private val notificator: SystemNotificationService,
    private val notificationActionDelegate: NotificationActionDelegate
) : ViewModel(), NotificationActionDelegate by notificationActionDelegate {

    val wallpaper = MutableStateFlow<Wallpaper>(Wallpaper.Default)
    val commands = MutableSharedFlow<Command>(replay = 0)
    val toasts = MutableSharedFlow<String>(replay = 0)

    init {
        viewModelScope.launch {
            restoreWallpaper.flow().collect {
                // Do nothing, just run pipeline.
            }
        }
        viewModelScope.launch {
            observeWallpaper.build(BaseUseCase.None).collect {
                wallpaper.value = it
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
    }

    private suspend fun handleNotification(event: Notification.Event) {
        val notification = event.notification
        if (notification != null) {
            if (notification.payload is NotificationPayload.GalleryImport) {
                // TODO migrate to system notifications
                delay(DELAY_BEFORE_SHOWING_NOTIFICATION_SCREEN)
                commands.emit(Command.Notifications)
            } else {
                notificator.notify(notification)
            }
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
        relationsSubscriptionManager.onStop()
        objectTypesSubscriptionManager.onStop()
        spaceDeletedStatusWatcher.onStop()
        userPermissionProvider.stop()
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
                    relationsSubscriptionManager.onStart()
                    objectTypesSubscriptionManager.onStart()
                    spaceDeletedStatusWatcher.onStart()
                    userPermissionProvider.start()
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

    fun onInterceptNotificationAction(action: NotificationAction) {
        viewModelScope.launch {
            proceedWithNotificationAction(action)
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
            data class File(val uri: String): Sharing()
            data class Files(val uris: List<String>): Sharing()
        }
        data object Notifications : Command()
    }

    companion object {
        const val DELAY_BEFORE_SHOWING_NOTIFICATION_SCREEN = 200L
    }
}