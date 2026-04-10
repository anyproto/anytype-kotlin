package com.anytypeio.anytype.feature_os_widgets.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.domain.auth.interactor.LaunchAccount
import com.anytypeio.anytype.domain.auth.interactor.LaunchWallet
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetCreateObjectEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

class CreateObjectWidgetConfigViewModel(
    private val appWidgetId: Int,
    private val spaceViews: SpaceViewSubscriptionContainer,
    private val urlBuilder: UrlBuilder,
    private val configStore: CreateObjectWidgetConfigStore,
    private val widgetUpdater: CreateObjectWidgetUpdater,
    private val launchWallet: LaunchWallet,
    private val launchAccount: LaunchAccount
) : ViewModel() {

    private val _spaces = MutableStateFlow<List<ObjectWrapper.SpaceView>?>(null)
    val spaces: StateFlow<List<ObjectWrapper.SpaceView>?> = _spaces.asStateFlow()

    private val _selectedSpace = MutableStateFlow<ObjectWrapper.SpaceView?>(null)
    val selectedSpace: StateFlow<ObjectWrapper.SpaceView?> = _selectedSpace.asStateFlow()

    private val _commands = MutableSharedFlow<Command>(extraBufferCapacity = 1)
    val commands: SharedFlow<Command> = _commands.asSharedFlow()

    init {
        Timber.d("$TAG init: appWidgetId=$appWidgetId")
        viewModelScope.launch {
            val result = launchMiddlewareForConfig(TAG, launchWallet, launchAccount)
            if (result is MiddlewareLaunchResult.Failure) {
                _commands.emit(Command.FinishWithFailure(result.message))
                return@launch
            }
            awaitFirstSpacesEmission(spaceViews.observe())
            spaceViews.observe()
                .map { allSpaces ->
                    allSpaces
                        .filter { it.isActive && it.spaceUxType != SpaceUxType.CHAT && it.spaceUxType != SpaceUxType.ONE_TO_ONE }
                        .sortedWith(compareBy(nullsLast()) { it.spaceOrder })
                }
                .collect { filtered ->
                    Timber.d("$TAG loadSpaces: filtered=${filtered.size}")
                    _spaces.value = filtered
                }
        }
    }

    fun onSpaceSelected(space: ObjectWrapper.SpaceView) {
        _selectedSpace.value = space
        viewModelScope.launch {
            _commands.emit(Command.ShowTypeSelection(space.targetSpaceId.orEmpty()))
        }
    }

    fun onTypeSelected(objType: ObjectWrapper.Type) {
        Timber.d("$TAG onTypeSelected: type=${objType.name}, key=${objType.uniqueKey}, appWidgetId=$appWidgetId")
        val space = _selectedSpace.value ?: run {
            viewModelScope.launch {
                _commands.emit(Command.ShowError("No space selected"))
            }
            return
        }

        val config = OsWidgetCreateObjectEntity(
            appWidgetId = appWidgetId,
            spaceId = space.targetSpaceId.orEmpty(),
            typeKey = objType.uniqueKey.orEmpty(),
            typeName = objType.name.orEmpty(),
            typeIconEmoji = objType.iconEmoji,
            typeIconName = objType.iconName,
            typeIconOption = objType.iconOption?.toInt(),
            spaceName = space.name.orEmpty(),
            // Token is embedded into widget deeplink and validated before create flow.
            deepLinkToken = UUID.randomUUID().toString()
        )

        viewModelScope.launch {
            try {
                Timber.d("$TAG saving config: spaceId=${config.spaceId}, typeKey=${config.typeKey}")
                configStore.save(config)
                Timber.d("$TAG config saved, triggering widget update")
                widgetUpdater.update(appWidgetId)
                Timber.d("$TAG emitting FinishWithSuccess")
                _commands.emit(Command.FinishWithSuccess(appWidgetId))
            } catch (e: Exception) {
                Timber.e(e, "$TAG Error saving widget config")
                _commands.emit(Command.ShowError(e.message ?: "Unknown error"))
            }
        }
    }

    companion object {
        private const val TAG = "CreateObjectConfig"
    }

    sealed class Command {
        data class ShowTypeSelection(val spaceId: String) : Command()
        data class FinishWithSuccess(val appWidgetId: Int) : Command()
        data class ShowError(val message: String) : Command()
        data class FinishWithFailure(val message: String) : Command()
    }

    class Factory @Inject constructor(
        private val spaceViews: SpaceViewSubscriptionContainer,
        private val urlBuilder: UrlBuilder,
        private val configStore: CreateObjectWidgetConfigStore,
        private val widgetUpdater: CreateObjectWidgetUpdater,
        private val launchWallet: LaunchWallet,
        private val launchAccount: LaunchAccount
    ) {
        fun create(appWidgetId: Int): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CreateObjectWidgetConfigViewModel(
                        appWidgetId = appWidgetId,
                        spaceViews = spaceViews,
                        urlBuilder = urlBuilder,
                        configStore = configStore,
                        widgetUpdater = widgetUpdater,
                        launchWallet = launchWallet,
                        launchAccount = launchAccount
                    ) as T
                }
            }
        }
    }
}
