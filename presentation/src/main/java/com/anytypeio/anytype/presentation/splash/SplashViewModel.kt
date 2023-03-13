package com.anytypeio.anytype.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary.openAccount
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.base.updateUserProperties
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.analytics.props.UserProperty
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_utils.ext.letNotNull
import com.anytypeio.anytype.domain.auth.interactor.CheckAuthorizationStatus
import com.anytypeio.anytype.domain.auth.interactor.GetLastOpenedObject
import com.anytypeio.anytype.domain.auth.interactor.LaunchAccount
import com.anytypeio.anytype.domain.auth.interactor.LaunchWallet
import com.anytypeio.anytype.domain.auth.model.AuthStatus
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.launch.GetDefaultPageType
import com.anytypeio.anytype.domain.misc.AppActionManager
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.search.ObjectTypesSubscriptionManager
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
import com.anytypeio.anytype.presentation.BuildConfig
import com.anytypeio.anytype.presentation.objects.SupportedLayouts
import kotlinx.coroutines.flow.MutableSharedFlow
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
    private val getDefaultPageType: GetDefaultPageType,
    private val createObject: CreateObject,
    private val appActionManager: AppActionManager,
    private val relationsSubscriptionManager: RelationsSubscriptionManager,
    private val objectTypesSubscriptionManager: ObjectTypesSubscriptionManager
) : ViewModel() {

    val commands = MutableSharedFlow<Command>(replay = 0)

    init {
        checkAuthorizationStatus()
    }

    private fun checkAuthorizationStatus() {
        viewModelScope.launch {
            checkAuthorizationStatus(Unit).process(
                failure = { e -> Timber.e(e, "Error while checking auth status") },
                success = { status ->
                    if (status == AuthStatus.UNAUTHORIZED)
                        commands.emit(Command.NavigateToLogin)
                    else
                        proceedWithLaunchingWallet()
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
                    commands.emit(Command.Error(e.toString()))
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
            launchAccount(BaseUseCase.None).proceed(
                success = { accountId ->
                    updateUserProps(accountId)
                    val props = Props.empty()
                    sendEvent(startTime, openAccount, props)
                    proceedWithGlobalSubscriptions()
                    setupShortcutsAndStartApp()
                },
                failure = { e ->
                    Timber.e(e, "Error while launching account")
                    commands.emit(Command.Error(ERROR_MESSAGE))
                }
            )
        }
    }

    private fun setupShortcutsAndStartApp() {
        viewModelScope.launch {
            getDefaultPageType.execute(Unit).fold(
                onSuccess = {
                    Pair(it.name, it.type).letNotNull { name, type ->
                        appActionManager.setup(
                            AppActionManager.Action.CreateNew(
                                type = type,
                                name = name
                            )
                        )
                    }
                    commands.emit(Command.CheckAppStartIntent)
                },
                onFailure = {
                    commands.emit(Command.CheckAppStartIntent)
                }
            )
        }
    }

    private fun proceedWithGlobalSubscriptions() {
        relationsSubscriptionManager.onStart()
        objectTypesSubscriptionManager.onStart()
    }

    fun onIntentCreateNewObject(type: Id) {
        viewModelScope.launch {
            createObject.execute(
                CreateObject.Param(type = type)
            ).fold(
                onFailure = { e ->
                    Timber.e(e, "Error while creating a new object with type:$type")
                    proceedWithNavigation()
                },
                onSuccess = { result ->
                    commands.emit(Command.NavigateToObject(result.objectId))
                }
            )
        }
    }

    fun onIntentActionNotFound() {
        proceedWithNavigation()
    }

    private fun proceedWithNavigation() {
        viewModelScope.launch {
            getLastOpenedObject(BaseUseCase.None).process(
                failure = {
                    Timber.e(it, "Error while getting last opened object")
                    proceedWithDashboardNavigation()
                },
                success = { response ->
                    when (response) {
                        is GetLastOpenedObject.Response.Success -> {
                            if (SupportedLayouts.layouts.contains(response.obj.layout)) {
                                val id = response.obj.id
                                when (response.obj.layout) {
                                    ObjectType.Layout.SET, ObjectType.Layout.COLLECTION ->
                                        commands.emit(Command.NavigateToObjectSet(id))
                                    else ->
                                        commands.emit(Command.NavigateToObject(id))
                                }
                            } else {
                                proceedWithDashboardNavigation()
                            }
                        }
                        else -> proceedWithDashboardNavigation()
                    }
                }
            )
        }
    }

    private suspend fun proceedWithDashboardNavigation() {
        if (BuildConfig.ENABLE_WIDGETS) {
            commands.emit(Command.NavigateToWidgets)
        } else {
            commands.emit(Command.NavigateToDashboard)
        }
    }

    private fun updateUserProps(id: String) {
        viewModelScope.updateUserProperties(
            analytics = analytics,
            userProperty = UserProperty.AccountId(id)
        )
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
        object NavigateToDashboard : Command()
        object NavigateToWidgets : Command()
        object NavigateToLogin : Command()
        object CheckAppStartIntent : Command()
        data class NavigateToObject(val id: Id) : Command()
        data class NavigateToObjectSet(val id: Id) : Command()
        data class Error(val msg: String) : Command()
    }

    companion object {
        const val ERROR_MESSAGE = "An error occurred while starting account..."
    }
}