package com.anytypeio.anytype.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.CrashReporter
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.updateUserProperties
import com.anytypeio.anytype.analytics.props.UserProperty
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.SupportedLayouts
import com.anytypeio.anytype.core_models.exceptions.AccountMigrationNeededException
import com.anytypeio.anytype.core_models.exceptions.NeedToUpdateApplicationException
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.domain.auth.interactor.CheckAuthorizationStatus
import com.anytypeio.anytype.domain.auth.interactor.GetLastOpenedObject
import com.anytypeio.anytype.domain.auth.interactor.LaunchAccount
import com.anytypeio.anytype.domain.auth.interactor.LaunchWallet
import com.anytypeio.anytype.domain.auth.interactor.MigrateAccount
import com.anytypeio.anytype.domain.auth.model.AuthStatus
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.misc.DeepLinkResolver
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.page.CreateObjectByTypeAndTemplate
import com.anytypeio.anytype.domain.spaces.GetLastOpenedSpace
import com.anytypeio.anytype.domain.subscriptions.GlobalSubscriptionManager
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.auth.account.MigrationHelperDelegate
import com.anytypeio.anytype.presentation.extension.proceedWithAccountEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsObjectCreateEvent
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber

/**
 * Created by Konstantin Ivanov
 * email :  ki@agileburo.com
 * on 2019-10-21.
 */
class SplashViewModel(
    private val analytics: Analytics,
    private val checkAuthorizationStatus: CheckAuthorizationStatus,
    private val launchWallet: LaunchWallet,
    private val launchAccount: LaunchAccount,
    private val getLastOpenedObject: GetLastOpenedObject,
    private val crashReporter: CrashReporter,
    private val localeProvider: LocaleProvider,
    private val spaceManager: SpaceManager,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val globalSubscriptionManager: GlobalSubscriptionManager,
    private val getLastOpenedSpace: GetLastOpenedSpace,
    private val createObjectByTypeAndTemplate: CreateObjectByTypeAndTemplate,
    private val spaceViews: SpaceViewSubscriptionContainer,
    private val migration: MigrationHelperDelegate,
    private val deepLinkResolver: DeepLinkResolver
) : ViewModel(),
    AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate,
    MigrationHelperDelegate by migration {

    val state = MutableStateFlow<State>(State.Init)
    val commands = MutableSharedFlow<Command>(replay = 0)

    private var migrationRetryCount: Int = 0

    init {
        Timber.i("SplashViewModel, init")
        checkAuthorizationStatus()
    }

    fun onErrorClicked() {
        if (state.value is State.Error) {
            proceedWithLaunchingAccount()
        }
    }

    fun onStartMigrationClicked() {
        viewModelScope.launch {
            if (state.value is State.Migration.AwaitingStart) {
                proceedWithAccountMigration()
            }
        }
    }

    fun onRetryMigrationClicked() {
        viewModelScope.launch {
            migrationRetryCount = migrationRetryCount + 1
            proceedWithAccountMigration()
        }
    }

    private suspend fun proceedWithAccountMigration() {
        if (migrationRetryCount <= 1) {
            proceedWithMigration(MigrateAccount.Params.Current).collect { migrationState ->
                when (migrationState) {
                    is MigrationHelperDelegate.State.Failed -> {
                        state.value = State.Migration.Failed(migrationState)
                    }
                    is MigrationHelperDelegate.State.Init -> {
                        // Do nothing.
                    }
                    is MigrationHelperDelegate.State.InProgress -> {
                        state.value = State.Migration.InProgress(
                            progress = migrationState.progress
                        )
                    }
                    is MigrationHelperDelegate.State.Migrated -> {
                        proceedWithLaunchingAccount()
                    }
                }
            }
        } else {
            Timber.e("Failed to migration account after retry")
        }
    }

    private fun checkAuthorizationStatus() {
        viewModelScope.launch {
            checkAuthorizationStatus.async(Unit).fold(
                onFailure = { e -> Timber.e(e, "Error while checking auth status") },
                onSuccess = { (status, account) ->
                    Timber.i("Authorization status: $status")
                    if (status == AuthStatus.UNAUTHORIZED) {
                        commands.emit(Command.NavigateToAuthStart)
                    } else {
                        proceedWithLaunchingWallet()
                    }
                }
            )
        }
    }

    private fun proceedWithLaunchingWallet() {
        Timber.i("proceedWithLaunchingWallet")
        viewModelScope.launch {
            launchWallet(BaseUseCase.None).either(
                fnL = {
                    Timber.e(it, "Error while launching wallet")
                    retryLaunchingWallet()
                },
                fnR = {
                    Timber.i("Wallet launched successfully")
                    proceedWithLaunchingAccount()
                }
            )
        }
    }

    private fun retryLaunchingWallet() {
        viewModelScope.launch {
            launchWallet(BaseUseCase.None).process(
                failure = { e ->
                    Timber.e(e, "Error while retrying launching wallet")
                    val msg = "Error while launching account: ${e.message}"
                    state.value = State.Error(msg)
                },
                success = {
                    proceedWithLaunchingAccount()
                }
            )
        }
    }

    private fun proceedWithLaunchingAccount() {
        Timber.i("proceedWithLaunchingAccount")
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            state.value = State.Loading
            launchAccount(BaseUseCase.None).proceed(
                success = { (analyticsId, networkId) ->
                    Timber.i("Account launched successfully, analyticsId: $analyticsId, networkId: $networkId")
                    crashReporter.setUser(analyticsId)
                    updateUserProps(analyticsId = analyticsId, networkId = networkId)
                    analytics.proceedWithAccountEvent(
                        startTime = startTime,
                        eventName = EventsDictionary.openAccount,
                        analyticsId = analyticsId
                    )
                    proceedWithGlobalSubscriptions()
                    commands.emit(Command.CheckAppStartIntent)
                },
                failure = { e ->
                    Timber.e(e, "Error while launching account")
                    when (e) {
                        is AccountMigrationNeededException -> {
                            state.value = State.Migration.AwaitingStart
                        }
                        is NeedToUpdateApplicationException -> {
                            state.value = State.Error(ERROR_NEED_UPDATE)
                        }
                        else -> {
                            val msg = "$ERROR_MESSAGE : ${e.message ?: "Unknown error"}"
                            state.value = State.Error(msg)
                        }
                    }
                }
            )
        }
    }

    private fun proceedWithGlobalSubscriptions() {
        globalSubscriptionManager.onStart()
    }

    fun onIntentCreateNewObject(type: Key) {
        Timber.d("onIntentCreateNewObject, type:[$type]")
        viewModelScope.launch {
            val startTime = System.currentTimeMillis()
            val space = getLastOpenedSpace.async(Unit).getOrNull()
            if (space == null) {
                Timber.e("No space found")
                proceedWithVaultNavigation()
                return@launch
            }
            val spaceId = space.id
            val params = CreateObjectByTypeAndTemplate.Param(
                typeKey = TypeKey(type),
                space = space,
                keys = ObjectSearchConstants.defaultKeysObjectType
            )
            createObjectByTypeAndTemplate.async(params).fold(
                onFailure = { e ->
                    Timber.e(e, "Error while creating a new object with type:$type")
                    commands.emit(Command.Toast(e.message ?: "Error while creating object"))
                    proceedWithNavigation()
                },
                onSuccess = { result ->
                    sendAnalyticsObjectCreateEvent(
                        objType = type,
                        analytics = analytics,
                        route = EventsDictionary.Routes.home,
                        startTime = startTime,
                        view = EventsDictionary.View.viewHome,
                        spaceParams = provideParams(spaceId)
                    )
                    when (result) {
                        CreateObjectByTypeAndTemplate.Result.ObjectTypeNotFound -> {
                            commands.emit(Command.Toast(ERROR_CREATE_OBJECT))
                            proceedWithVaultNavigation()
                        }
                        is CreateObjectByTypeAndTemplate.Result.Success -> {
                            val target = result.objectId
                            val view = awaitActiveSpaceView(space)
                            if (view != null) {
                                val chatId = resolveChatID(
                                    space = space,
                                    spaceView = view
                                )
                                // Layout may not be known here; open as an object and let UI resolve.
                                commands.emit(
                                    Command.NavigateToObject(
                                        id = target,
                                        space = spaceId,
                                        chat = chatId
                                    )
                                )
                            } else {
                                proceedWithVaultNavigation()
                            }
                        }
                    }
                }
            )
        }
    }

    fun onIntentTriggeredByChatPush(space: Id, chat: Id) {
        viewModelScope.launch {
            spaceManager.set(space = space)
                .onSuccess {
                    commands.emit(
                        Command.NavigateToChat(
                            space = space,
                            chat = chat
                        )
                    )
                }
                .onFailure {
                    Timber.e(it, "Error while setting space due to chat intent")
                }
        }
    }

    fun onIntentActionNotFound() {
        Timber.i("onIntentActionNotFound")
        proceedWithNavigation()
    }

    fun onDeepLinkLaunch(deeplink: String) {
        Timber.d("onDeepLinkLaunch, deeplink:[$deeplink]")
        viewModelScope.launch {
            proceedWithVaultNavigation(deeplink)
        }
    }

    private fun updateUserProps(analyticsId: String, networkId: String) {
        viewModelScope.updateUserProperties(
            analytics = analytics,
            userProperty = UserProperty.AccountId(analyticsId)
        )
        viewModelScope.updateUserProperties(
            analytics = analytics,
            userProperty = UserProperty.NetworkId(networkId)
        )
        localeProvider.language().let { lang ->
            viewModelScope.updateUserProperties(
                analytics = analytics,
                userProperty = UserProperty.InterfaceLanguage(lang)
            )
        }
    }

    //region NAVIGATION
    private suspend fun awaitActiveSpaceView(space: SpaceId) = withTimeoutOrNull(SPACE_LOADING_TIMEOUT) {
        spaceViews
            .observe(space)
            .onEach { view ->
                Timber.i(
                    "Observing space view for ${space.id}, isActive: ${view.isActive}, spaceUxType: ${view.spaceUxType}, chat: ${view.chatId}"
                )
            }
            .filter { view -> view.isActive }
            .take(1)
            .catch {
                Timber.w(it, "Error while observing space view for ${space.id}")
            }
            .firstOrNull()
    }

    private suspend fun emitNavigationForObject(
        id: Id,
        space: Id,
        layout: ObjectType.Layout?,
        chatId: Id?
    ) {
        when (layout) {
            ObjectType.Layout.SET,
            ObjectType.Layout.COLLECTION ->
                commands.emit(
                    Command.NavigateToObjectSet(
                        id = id,
                        space = space,
                        chat = chatId
                    )
                )
            ObjectType.Layout.DATE -> {
                commands.emit(
                    Command.NavigateToDateObject(
                        id = id,
                        space = space,
                        chat = chatId
                    )
                )
            }
            ObjectType.Layout.OBJECT_TYPE -> {
                commands.emit(
                    Command.NavigateToObjectType(
                        id = id,
                        space = space,
                        chat = chatId
                    )
                )
            }
            else ->
                commands.emit(
                    Command.NavigateToObject(
                        id = id,
                        space = space,
                        chat = chatId
                    )
                )
        }
    }

    private fun proceedWithNavigation() {
        Timber.i("proceedWithNavigation, get getLastOpenedObject")
        viewModelScope.launch {
            val space = getLastOpenedSpace.async(Unit).getOrNull()
            if (space == null) {
                Timber.w("No space found for last opened object navigation")
                proceedWithVaultNavigation()
                return@launch
            }
            val params = GetLastOpenedObject.Params(space = space)
            getLastOpenedObject(params = params).process(
                success = { response ->
                    Timber.i("Last opened object response: ${response.javaClass.name}")
                    when (response) {
                        is GetLastOpenedObject.Response.Success -> {
                            val obj = response.obj
                            if (!SupportedLayouts.lastOpenObjectLayouts.contains(obj.layout)) {
                                Timber.i("Last opened object layout not supported: ${obj.layout}")
                                proceedWithVaultNavigation()
                                return@process
                            }

                            val id = obj.id
                            val space = requireNotNull(obj.spaceId)

                            val view = awaitActiveSpaceView(SpaceId(space))
                            if (view != null) {
                                val chat = when(view.spaceUxType) {
                                    SpaceUxType.CHAT, SpaceUxType.ONE_TO_ONE -> {
                                        resolveChatID(
                                            space = SpaceId(space),
                                            spaceView = view
                                        )
                                    }
                                    else -> {
                                        null
                                    }
                                }
                                emitNavigationForObject(
                                    id = id,
                                    space = space,
                                    layout = obj.layout,
                                    chatId = chat
                                )
                            } else {
                                Timber.w("Space view not ready or timeout while restoring last opened object. Navigating to vault.")
                                proceedWithVaultNavigation()
                            }
                        }
                        else -> proceedWithVaultNavigation()
                    }
                },
                failure = {
                    Timber.e(it, "Error while getting last opened object")
                    proceedWithVaultNavigation()
                }
            )
        }
    }

    private suspend fun proceedWithVaultNavigation(deeplink: String? = null) {
        Timber.d("proceedWithVaultNavigation deep link: $deeplink")

        // For one-to-one chat deeplinks, always navigate to Vault
        // where the proper handling logic exists in VaultViewModel
        if (deeplink != null) {
            val action = deepLinkResolver.resolve(deeplink)
            if (action is DeepLinkResolver.Action.InitiateOneToOneChat) {
                Timber.d("One-to-one chat deeplink detected, navigating to Vault")
                commands.emit(Command.NavigateToVault(deeplink))
                return
            }
        }

        val space = getLastOpenedSpace.async(Unit).getOrNull()
        if (space != null) {
            val view = awaitActiveSpaceView(SpaceId(space.id))
            if (view != null) {
                Timber.i("Space view loaded: $view")
                when(view.spaceUxType) {
                    SpaceUxType.CHAT, SpaceUxType.ONE_TO_ONE -> {
                        val chat = resolveChatID(
                            space = space,
                            spaceView = view
                        )
                        if (chat != null) {
                            commands.emit(
                                Command.NavigateToChat(
                                    space = space.id,
                                    chat = chat,
                                    deeplink = deeplink
                                )
                            )
                        } else {
                            Timber.w("Could not resolve chat ID for chat spaces")
                            commands.emit(
                                Command.NavigateToWidgets(
                                    space = space.id,
                                    deeplink = deeplink
                                )
                            )
                        }
                    }
                    else -> {
                        commands.emit(
                            Command.NavigateToWidgets(
                                space = space.id,
                                deeplink = deeplink
                            )
                        )
                    }
                }
            } else {
                Timber.w("Timeout while waiting for active space view. Navigating to vault.")
                commands.emit(Command.NavigateToVault(deeplink))
            }
        } else {
            Timber.w("No space found or space manager state is NoSpace. Navigating to vault.")
            commands.emit(Command.NavigateToVault(deeplink))
        }
    }

    /**
     * Resolves chat ID for chat spaces.
     * Using space manager as fallback, assuming that the space has been opened already via space manager.
     */
    fun resolveChatID(
        space: SpaceId,
        spaceView: ObjectWrapper.SpaceView
    ) : Id? {
        return spaceView.chatId ?: spaceManager.getConfig(space)?.spaceChatId
    }

    //endregion

    sealed class Command {
        data class NavigateToVault(val deeplink: String? = null) : Command()
        data class NavigateToWidgets(val space: Id, val deeplink: String? = null) : Command()
        data class NavigateToChat(
            val space: Id,
            val chat: Id,
            val deeplink: String? = null
        ) : Command()
        data object NavigateToAuthStart : Command()
        data object CheckAppStartIntent : Command()
        data class NavigateToObject(val id: Id, val space: Id, val chat: Id?) : Command()
        data class NavigateToObjectSet(val id: Id, val space: Id, val chat: Id?) : Command()
        data class NavigateToDateObject(val id: Id, val space: Id, val chat: Id?) : Command()
        data class NavigateToObjectType(val id: Id, val space: Id, val chat: Id?) : Command()
        data class Toast(val message: String) : Command()
    }

    companion object {
        const val ERROR_MESSAGE = "An error occurred while starting account"
        const val ERROR_NEED_UPDATE =
            "Unable to retrieve account. Please update Anytype to the latest version."
        const val ERROR_CREATE_OBJECT = "Error while creating object: object type not found"
        const val SPACE_LOADING_TIMEOUT = 5000L
    }

    sealed class State {
        data object Init : State()
        data object Loading : State()
        data object Success : State()
        data class Error(val msg: String) : State()
        sealed class Migration : State() {
            data object AwaitingStart : Migration()
            data class InProgress(val progress: Float) : Migration()
            data class Failed(val state: MigrationHelperDelegate.State.Failed) : Migration()
        }
    }
}