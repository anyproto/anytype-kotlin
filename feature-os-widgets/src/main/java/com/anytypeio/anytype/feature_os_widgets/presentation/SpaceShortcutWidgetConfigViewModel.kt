package com.anytypeio.anytype.feature_os_widgets.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetSpaceShortcutEntity
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
    private val spaceViews: SpaceViewSubscriptionContainer,
    private val urlBuilder: UrlBuilder,
    private val configStore: SpaceShortcutWidgetConfigStore,
    private val iconCache: SpaceShortcutIconCache,
    private val widgetUpdater: SpaceShortcutWidgetUpdater
) : ViewModel() {

    private val _spaces = MutableStateFlow<List<ObjectWrapper.SpaceView>>(emptyList())
    val spaces: StateFlow<List<ObjectWrapper.SpaceView>> = _spaces.asStateFlow()

    private val _commands = MutableSharedFlow<Command>()
    val commands: SharedFlow<Command> = _commands.asSharedFlow()

    init {
        Timber.d("$TAG init: appWidgetId=$appWidgetId")
        loadSpaces()
    }

    private fun loadSpaces() {
        val allSpaces = spaceViews.get()
        Timber.d("$TAG loadSpaces: total=${allSpaces.size}")
        val filtered = allSpaces
            .filter { it.isActive && it.spaceUxType != SpaceUxType.CHAT && it.spaceUxType != SpaceUxType.ONE_TO_ONE }
            .sortedWith(compareBy(nullsLast()) { it.spaceOrder })
        Timber.d("$TAG loadSpaces: filtered=${filtered.size}")
        _spaces.value = filtered
    }

    fun onSpaceSelected(space: ObjectWrapper.SpaceView) {
        Timber.d("$TAG onSpaceSelected: space=${space.name}, targetSpaceId=${space.targetSpaceId}, appWidgetId=$appWidgetId")
        viewModelScope.launch {
            try {
                // Cache the icon image if available
                val cachedIconPath = space.iconImage?.takeIf { it.isNotEmpty() }?.let { iconHash ->
                    val iconUrl = urlBuilder.thumbnail(iconHash)
                    Timber.d("$TAG caching icon: hash=$iconHash, url=$iconUrl")
                    val path = iconCache.cacheForWidget(iconUrl, appWidgetId)
                    Timber.d("$TAG icon cached: path=$path")
                    path
                }

                val config = OsWidgetSpaceShortcutEntity(
                    appWidgetId = appWidgetId,
                    spaceId = space.targetSpaceId.orEmpty(),
                    spaceName = space.name.orEmpty(),
                    spaceIconImage = space.iconImage,
                    spaceIconOption = space.iconOption?.toInt(),
                    cachedIconPath = cachedIconPath
                )
                Timber.d("$TAG saving config: $config")

                // Save the configuration
                configStore.save(config)
                Timber.d("$TAG config saved")

                // Trigger widget update so it picks up the saved config
                Timber.d("$TAG triggering widget update")
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
        private const val TAG = "SpaceShortcutConfig"
    }

    sealed class Command {
        data class FinishWithSuccess(val appWidgetId: Int) : Command()
        data class ShowError(val message: String) : Command()
    }

    class Factory @Inject constructor(
        private val spaceViews: SpaceViewSubscriptionContainer,
        private val urlBuilder: UrlBuilder,
        private val configStore: SpaceShortcutWidgetConfigStore,
        private val iconCache: SpaceShortcutIconCache,
        private val widgetUpdater: SpaceShortcutWidgetUpdater
    ) {
        fun create(appWidgetId: Int): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SpaceShortcutWidgetConfigViewModel(
                        appWidgetId = appWidgetId,
                        spaceViews = spaceViews,
                        urlBuilder = urlBuilder,
                        configStore = configStore,
                        iconCache = iconCache,
                        widgetUpdater = widgetUpdater
                    ) as T
                }
            }
        }
    }
}
