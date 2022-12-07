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
import com.anytypeio.anytype.core_models.MarketplaceObjectTypeIds.MARKETPLACE_OBJECT_TYPE_PREFIX
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds.NOTE
import com.anytypeio.anytype.core_models.ObjectTypeIds.PAGE
import com.anytypeio.anytype.domain.auth.interactor.CheckAuthorizationStatus
import com.anytypeio.anytype.domain.auth.interactor.GetLastOpenedObject
import com.anytypeio.anytype.domain.auth.interactor.LaunchAccount
import com.anytypeio.anytype.domain.auth.interactor.LaunchWallet
import com.anytypeio.anytype.domain.auth.model.AuthStatus
import com.anytypeio.anytype.domain.base.BaseUseCase
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.launch.GetDefaultEditorType
import com.anytypeio.anytype.domain.launch.SetDefaultEditorType
import com.anytypeio.anytype.domain.misc.AppActionManager
import com.anytypeio.anytype.domain.page.CreatePage
import com.anytypeio.anytype.domain.search.ObjectTypesSubscriptionManager
import com.anytypeio.anytype.domain.search.RelationsSubscriptionManager
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
    private val getDefaultEditorType: GetDefaultEditorType,
    private val setDefaultEditorType: SetDefaultEditorType,
    private val createPage: CreatePage,
    private val appActionManager: AppActionManager,
    private val relationsSubscriptionManager: RelationsSubscriptionManager,
    private val objectTypesSubscriptionManager: ObjectTypesSubscriptionManager
) : ViewModel() {

    val commands = MutableSharedFlow<Command>(replay = 0)

    init {
        proceedWithUserSettings()
    }

    private fun proceedWithUserSettings() {
        viewModelScope.launch {
            getDefaultEditorType.execute(Unit).fold(
                onFailure = { e ->
                    Timber.e(e, "Error while getting default page type")
                    checkAuthorizationStatus()
                },
                onSuccess = { result ->
                    Timber.d("getDefaultPageType: ${result.type}")
                    val defaultObjectType = result.type
                    if (defaultObjectType == null) {
                        commands.emit(Command.CheckFirstInstall)
                    } else if (defaultObjectType.contains(MARKETPLACE_OBJECT_TYPE_PREFIX)) {
                        fallbackToNoteAsDefaultObjectType()
                    } else {
                        checkAuthorizationStatus()
                    }
                }
            )
        }
    }

    fun onFirstInstallStatusChecked(isFirstInstall: Boolean) {
        Timber.d("setDefaultUserSettings, isFirstInstall:[$isFirstInstall]")
        val (typeId, typeName) = if (isFirstInstall) {
            DEFAULT_TYPE_FIRST_INSTALL
        } else {
            DEFAULT_TYPE_UPDATE
        }
        proceedWithUpdatingDefaultObjectType(
            typeId = typeId,
            typeName = typeName
        )
    }

    private fun fallbackToNoteAsDefaultObjectType() {
        val (typeId, typeName) = DEFAULT_TYPE_FIRST_INSTALL
        proceedWithUpdatingDefaultObjectType(
            typeId = typeId,
            typeName = typeName
        )
    }

    private fun proceedWithUpdatingDefaultObjectType(
        typeId: String,
        typeName: String
    ) {
        appActionManager.setup(
            AppActionManager.Action.CreateNew(
                type = typeId,
                name = typeName
            )
        )
        viewModelScope.launch {
            val params = SetDefaultEditorType.Params(typeId, typeName)
            Timber.d("Start to update Default Page Type:${params.type}")
            setDefaultEditorType.invoke(params).process(
                failure = {
                    Timber.e(it, "Error while setting default page type")
                    checkAuthorizationStatus()
                },
                success = { checkAuthorizationStatus() }
            )
        }
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
                    commands.emit(Command.CheckAppStartIntent)
                },
                failure = { e ->
                    Timber.e(e, "Error while launching account")
                    commands.emit(Command.Error(ERROR_MESSAGE))
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
            createPage.execute(
                CreatePage.Params(
                    ctx = null,
                    emoji = null,
                    isDraft = true,
                    type = type
                )
            ).fold(
                onFailure = { e ->
                    proceedWithNavigation()
                },
                onSuccess = { target ->
                    commands.emit(Command.NavigateToObject(target))
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
                    commands.emit(Command.NavigateToDashboard)
                },
                success = { response ->
                    when (response) {
                        is GetLastOpenedObject.Response.Success -> {
                            if (SupportedLayouts.layouts.contains(response.obj.layout)) {
                                val id = response.obj.id
                                when (response.obj.layout) {
                                    ObjectType.Layout.SET ->
                                        commands.emit(Command.NavigateToObjectSet(id))
                                    else ->
                                        commands.emit(Command.NavigateToObject(id))
                                }
                            } else {
                                commands.emit(Command.NavigateToDashboard)
                            }
                        }
                        else -> commands.emit(Command.NavigateToDashboard)
                    }
                }
            )
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
        object CheckFirstInstall : Command()
        object NavigateToDashboard : Command()
        object NavigateToLogin : Command()
        object CheckAppStartIntent : Command()
        data class NavigateToObject(val id: Id) : Command()
        data class NavigateToObjectSet(val id: Id) : Command()
        data class Error(val msg: String) : Command()
    }

    companion object {
        const val ERROR_MESSAGE = "An error occurred while starting account..."
        val DEFAULT_TYPE_FIRST_INSTALL = Pair(NOTE, "Note")
        val DEFAULT_TYPE_UPDATE = Pair(PAGE, "Page")
    }
}