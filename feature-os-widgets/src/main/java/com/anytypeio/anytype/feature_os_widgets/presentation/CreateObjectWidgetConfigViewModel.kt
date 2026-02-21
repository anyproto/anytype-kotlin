package com.anytypeio.anytype.feature_os_widgets.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetCreateObjectEntity
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetsDataStore
import com.anytypeio.anytype.feature_os_widgets.ui.OsCreateObjectWidgetUpdater
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class CreateObjectWidgetConfigViewModel(
    private val appWidgetId: Int,
    private val context: Context,
    private val spaceViews: SpaceViewSubscriptionContainer,
    private val urlBuilder: UrlBuilder
) : ViewModel() {

    private val _spaces = MutableStateFlow<List<ObjectWrapper.SpaceView>>(emptyList())
    val spaces: StateFlow<List<ObjectWrapper.SpaceView>> = _spaces.asStateFlow()

    private val _selectedSpace = MutableStateFlow<ObjectWrapper.SpaceView?>(null)
    val selectedSpace: StateFlow<ObjectWrapper.SpaceView?> = _selectedSpace.asStateFlow()

    private val _commands = MutableSharedFlow<Command>()
    val commands: SharedFlow<Command> = _commands.asSharedFlow()

    init {
        loadSpaces()
    }

    private fun loadSpaces() {
        val allSpaces = spaceViews.get()
        _spaces.value = allSpaces
            .filter { it.isActive && it.spaceUxType != SpaceUxType.CHAT && it.spaceUxType != SpaceUxType.ONE_TO_ONE }
            .sortedWith(compareBy(nullsLast()) { it.spaceOrder })
    }

    fun onSpaceSelected(space: ObjectWrapper.SpaceView) {
        _selectedSpace.value = space
        viewModelScope.launch {
            _commands.emit(Command.ShowTypeSelection(space.targetSpaceId.orEmpty()))
        }
    }

    fun onTypeSelected(objType: ObjectWrapper.Type) {
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
            spaceName = space.name.orEmpty()
        )

        viewModelScope.launch {
            try {
                // Save the configuration
                OsWidgetsDataStore(context).saveCreateObjectConfig(config)

                // Trigger widget update so it picks up the saved config
                OsCreateObjectWidgetUpdater.update(context, appWidgetId)

                _commands.emit(Command.FinishWithSuccess(appWidgetId))
            } catch (e: Exception) {
                Timber.e(e, "Error saving widget config")
                _commands.emit(Command.ShowError(e.message ?: "Unknown error"))
            }
        }
    }

    sealed class Command {
        data class ShowTypeSelection(val spaceId: String) : Command()
        data class FinishWithSuccess(val appWidgetId: Int) : Command()
        data class ShowError(val message: String) : Command()
    }

    class Factory @Inject constructor(
        private val context: Context,
        private val spaceViews: SpaceViewSubscriptionContainer,
        private val urlBuilder: UrlBuilder
    ) : ViewModelProvider.Factory {

        var appWidgetId: Int = -1

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CreateObjectWidgetConfigViewModel(
                appWidgetId = appWidgetId,
                context = context,
                spaceViews = spaceViews,
                urlBuilder = urlBuilder
            ) as T
        }
    }
}
