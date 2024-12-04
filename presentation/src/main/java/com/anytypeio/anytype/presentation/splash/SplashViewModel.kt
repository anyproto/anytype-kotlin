package com.anytypeio.anytype.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.CrashReporter
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsDictionary.openAccount
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.base.updateUserProperties
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.analytics.props.UserProperty
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.MarketplaceObjectTypeIds.SET
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds.COLLECTION
import com.anytypeio.anytype.core_models.exceptions.MigrationNeededException
import com.anytypeio.anytype.core_models.exceptions.NeedToUpdateApplicationException
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_utils.ui.ViewState
import com.anytypeio.anytype.domain.auth.interactor.CheckAuthorizationStatus
import com.anytypeio.anytype.domain.auth.interactor.GetLastOpenedObject
import com.anytypeio.anytype.domain.auth.interactor.LaunchAccount
import com.anytypeio.anytype.domain.auth.interactor.LaunchWallet
import com.anytypeio.anytype.domain.auth.model.AuthStatus
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.page.CreateObjectByTypeAndTemplate
import com.anytypeio.anytype.domain.spaces.GetLastOpenedSpace
import com.anytypeio.anytype.domain.subscriptions.GlobalSubscriptionManager
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.extension.sendAnalyticsObjectCreateEvent
import com.anytypeio.anytype.core_models.SupportedLayouts
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
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
    private val createObjectByTypeAndTemplate: CreateObjectByTypeAndTemplate
) : ViewModel(), AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate {

    val state = MutableStateFlow<ViewState<Any>>(ViewState.Init)

    val commands = MutableSharedFlow<Command>(replay = 0)

    val loadingState = MutableStateFlow(false)

    init {
        Timber.i("SplashViewModel, init")
        checkAuthorizationStatus()
    }

    fun onErrorClicked() {
        if (BuildConfig.DEBUG && state.value is ViewState.Error) {
            state.value = ViewState.Loading
            proceedWithLaunchingAccount()
        }
    }

    private fun checkAuthorizationStatus() {
        viewModelScope.launch {
            checkAuthorizationStatus(Unit).process(
                failure = { e -> Timber.e(e, "Error while checking auth status") },
                success = { status ->
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
        viewModelScope.launch {
            launchWallet(BaseUseCase.None).either(
                fnL = { retryLaunchingWallet() },
                fnR = {
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
                    state.value = ViewState.Error(msg)
                },
                success = {
                    proceedWithLaunchingAccount()
                }
            )
        }
    }

    private fun proceedWithLaunchingAccount() {
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            loadingState.value = true
            launchAccount(BaseUseCase.None).proceed(
                success = { analyticsId ->
                    loadingState.value = false
                    crashReporter.setUser(analyticsId)
                    updateUserProps(analyticsId)
                    val props = Props.empty()
                    sendEvent(startTime, openAccount, props)
                    proceedWithGlobalSubscriptions()
                    commands.emit(Command.CheckAppStartIntent)
                },
                failure = { e ->
                    loadingState.value = false
                    Timber.e(e, "Error while launching account")
                    when (e) {
                        is MigrationNeededException -> {
                            commands.emit(Command.NavigateToMigration)
                        }
                        is NeedToUpdateApplicationException -> {
                            state.value = ViewState.Error(ERROR_NEED_UPDATE)
                        }
                        else -> {
                            val msg = "$ERROR_MESSAGE : ${e.message ?: "Unknown error"}"
                            state.value = ViewState.Error(msg)
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
            val spaceId = spaceManager.get()
            if (spaceId.isEmpty()) {
                Timber.e("No space found")
                return@launch
            }
            val space = SpaceId(spaceId)
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
                            if (type == COLLECTION || type == SET) {
                                commands.emit(
                                    Command.NavigateToObjectSet(
                                        id = target,
                                        space = spaceId
                                    )
                                )
                            } else {
                                commands.emit(
                                    Command.NavigateToObject(
                                        id = target,
                                        space = spaceId
                                    )
                                )
                            }
                        }
                    }
                }
            )
        }
    }

    fun onIntentActionNotFound() {
        proceedWithNavigation()
    }

    fun onDeepLinkLaunch(deeplink: String) {
        Timber.d("onDeepLinkLaunch, deeplink:[$deeplink]")
        viewModelScope.launch {
            proceedWithVaultNavigation(deeplink)
        }
    }

    private fun proceedWithNavigation() {
        viewModelScope.launch {
            getLastOpenedObject(
                params = GetLastOpenedObject.Params(space = SpaceId(spaceManager.get()))
            ).process(
                failure = {
                    Timber.e(it, "Error while getting last opened object")
                    proceedWithVaultNavigation()
                },
                success = { response ->
                    when (response) {
                        is GetLastOpenedObject.Response.Success -> {
                            if (SupportedLayouts.layouts.contains(response.obj.layout)) {
                                val id = response.obj.id
                                val space = requireNotNull(response.obj.spaceId)
                                when (response.obj.layout) {
                                    ObjectType.Layout.SET, ObjectType.Layout.COLLECTION ->
                                        commands.emit(
                                            Command.NavigateToObjectSet(
                                                id = id,
                                                space = space
                                            )
                                        )
                                    else ->
                                        commands.emit(
                                            Command.NavigateToObject(
                                                id = id,
                                                space = space
                                            )
                                        )
                                }
                            } else {
                                proceedWithVaultNavigation()
                            }
                        }
                        else -> proceedWithVaultNavigation()
                    }
                }
            )
        }
    }

    /**
     * Before navigating to widgets, make sure space was opened successfully during launchAccount
     * @see [LaunchAccount] use-case
     */
    private suspend fun proceedWithVaultNavigation(deeplink: String? = null) {
        val space = getLastOpenedSpace.async(Unit).getOrNull()
        if (space != null && spaceManager.getState() != SpaceManager.State.NoSpace) {
            commands.emit(Command.NavigateToWidgets(
                space = space.id,
                deeplink = deeplink
            ))
        } else {
            commands.emit(Command.NavigateToVault(deeplink))
        }
    }

    private fun updateUserProps(id: String) {
        viewModelScope.updateUserProperties(
            analytics = analytics,
            userProperty = UserProperty.AccountId(id)
        )
        localeProvider.language().let { lang ->
            viewModelScope.updateUserProperties(
                analytics = analytics,
                userProperty = UserProperty.InterfaceLanguage(lang)
            )
        }
    }

    private fun sendEvent(startTime: Long, event: String, props: Props) {
        val middleTime = System.currentTimeMillis()
        viewModelScope.sendEvent(
            analytics = analytics,
            startTime = startTime,
            middleTime = middleTime,
            eventName = event,
            props = props
        )
    }

    sealed class Command {
        data class NavigateToWidgets(val space: Id, val deeplink: String? = null) : Command()
        data class NavigateToVault(val deeplink: String? = null) : Command()
        data object NavigateToAuthStart : Command()
        data object NavigateToMigration: Command()
        data object CheckAppStartIntent : Command()
        data class NavigateToObject(val id: Id, val space: Id) : Command()
        data class NavigateToObjectSet(val id: Id, val space: Id) : Command()
        data class Toast(val message: String) : Command()
    }

    companion object {
        const val ERROR_MESSAGE = "An error occurred while starting account"
        const val ERROR_NEED_UPDATE = "Unable to retrieve account. Please update Anytype to the latest version."
        const val ERROR_CREATE_OBJECT = "Error while creating object: object type not found"
    }
}