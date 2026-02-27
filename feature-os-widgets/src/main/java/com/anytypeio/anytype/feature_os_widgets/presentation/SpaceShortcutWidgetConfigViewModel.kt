package com.anytypeio.anytype.feature_os_widgets.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetIconCache
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetSpaceShortcutEntity
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetsDataStore
import com.anytypeio.anytype.feature_os_widgets.ui.OsSpaceShortcutWidgetUpdater
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SpaceShortcutWidgetConfigViewModel(
    private val appWidgetId: Int,
    private val context: Context,
    private val spaceViews: SpaceViewSubscriptionContainer,
    private val urlBuilder: UrlBuilder
) : ViewModel() {

    private val _spaces = MutableStateFlow<List<ObjectWrapper.SpaceView>>(emptyList())
    val spaces: StateFlow<List<ObjectWrapper.SpaceView>> = _spaces.asStateFlow()

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
        viewModelScope.launch {
            try {
                // Cache the icon image if available
                val iconCache = OsWidgetIconCache(context)
                val cachedIconPath = space.iconImage?.takeIf { it.isNotEmpty() }?.let { iconHash ->
                    val iconUrl = urlBuilder.thumbnail(iconHash)
                    iconCache.cacheShortcutIcon(
                        url = iconUrl,
                        widgetId = appWidgetId,
                        prefix = OsWidgetIconCache.PREFIX_SPACE
                    )
                }

                val config = OsWidgetSpaceShortcutEntity(
                    appWidgetId = appWidgetId,
                    spaceId = space.targetSpaceId.orEmpty(),
                    spaceName = space.name.orEmpty(),
                    spaceIconImage = space.iconImage,
                    spaceIconOption = space.iconOption?.toInt(),
                    cachedIconPath = cachedIconPath
                )

                // Save the configuration
                OsWidgetsDataStore(context).saveSpaceShortcutConfig(config)

                // Trigger widget update so it picks up the saved config
                OsSpaceShortcutWidgetUpdater.update(context, appWidgetId)

                _commands.emit(Command.FinishWithSuccess(appWidgetId))
            } catch (e: Exception) {
                Timber.e(e, "Error saving widget config")
                _commands.emit(Command.ShowError(e.message ?: "Unknown error"))
            }
        }
    }

    sealed class Command {
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
            return SpaceShortcutWidgetConfigViewModel(
                appWidgetId = appWidgetId,
                context = context,
                spaceViews = spaceViews,
                urlBuilder = urlBuilder
            ) as T
        }
    }
}
