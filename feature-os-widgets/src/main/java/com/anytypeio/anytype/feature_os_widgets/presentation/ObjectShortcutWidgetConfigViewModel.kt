package com.anytypeio.anytype.feature_os_widgets.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.ext.mapToObjectWrapperType
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.ui.objectIcon
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetIconCache
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetObjectShortcutEntity
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetsDataStore
import com.anytypeio.anytype.feature_os_widgets.ui.OsObjectShortcutWidgetUpdater
import com.anytypeio.anytype.feature_os_widgets.ui.config.ObjectItemView
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ObjectShortcutWidgetConfigViewModel(
    private val appWidgetId: Int,
    private val context: Context,
    private val spaceViews: SpaceViewSubscriptionContainer,
    private val urlBuilder: UrlBuilder,
    private val searchObjects: SearchObjects
) : ViewModel() {

    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.SpaceSelection)
    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()

    private val _spaces = MutableStateFlow<List<ObjectWrapper.SpaceView>>(emptyList())
    val spaces: StateFlow<List<ObjectWrapper.SpaceView>> = _spaces.asStateFlow()

    private val _objectItems = MutableStateFlow<List<ObjectItemView>>(emptyList())
    val objectItems: StateFlow<List<ObjectItemView>> = _objectItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedSpace = MutableStateFlow<ObjectWrapper.SpaceView?>(null)
    val selectedSpace: StateFlow<ObjectWrapper.SpaceView?> = _selectedSpace.asStateFlow()

    private val _commands = MutableSharedFlow<Command>()
    val commands: SharedFlow<Command> = _commands.asSharedFlow()

    private var searchJob: Job? = null
    private var typesMap: Map<Id, ObjectWrapper.Type> = emptyMap()

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
        _screenState.value = ScreenState.ObjectSelection
        searchObjectsInSpace(
            spaceId = space.targetSpaceId.orEmpty(),
            query = "",
            fetchTypes = true
        )
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        val spaceId = _selectedSpace.value?.targetSpaceId.orEmpty()
        searchObjectsInSpace(spaceId, query, fetchTypes = false)
    }

    fun onObjectSelected(item: ObjectItemView) {
        val space = _selectedSpace.value ?: return
        viewModelScope.launch {
            try {
                // Cache the icon image if available
                val iconCache = OsWidgetIconCache(context)
                val cachedIconPath = item.obj.iconImage?.takeIf { it.isNotEmpty() }?.let { iconHash ->
                    val iconUrl = urlBuilder.thumbnail(iconHash)
                    iconCache.cacheShortcutIcon(
                        url = iconUrl,
                        widgetId = appWidgetId,
                        prefix = OsWidgetIconCache.PREFIX_OBJECT
                    )
                }

                val config = OsWidgetObjectShortcutEntity(
                    appWidgetId = appWidgetId,
                    spaceId = space.targetSpaceId.orEmpty(),
                    spaceName = space.name.orEmpty(),
                    objectId = item.obj.id,
                    objectName = item.obj.name.orEmpty(),
                    objectIconEmoji = item.obj.iconEmoji,
                    objectIconImage = item.obj.iconImage,
                    objectIconName = item.obj.iconName,
                    objectIconOption = item.obj.iconOption?.toInt(),
                    objectLayout = item.obj.layout?.code,
                    cachedIconPath = cachedIconPath
                )

                OsWidgetsDataStore(context).saveObjectShortcutConfig(config)
                OsObjectShortcutWidgetUpdater.update(context, appWidgetId)

                _commands.emit(Command.FinishWithSuccess(appWidgetId))
            } catch (e: Exception) {
                Timber.e(e, "Error saving widget config")
                _commands.emit(Command.ShowError(e.message ?: "Unknown error"))
            }
        }
    }

    fun onBack() {
        searchJob?.cancel()
        _selectedSpace.value = null
        _objectItems.value = emptyList()
        typesMap = emptyMap()
        _searchQuery.value = ""
        _screenState.value = ScreenState.SpaceSelection
    }

    private fun searchObjectsInSpace(spaceId: String, query: String, fetchTypes: Boolean) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _isLoading.value = true
            delay(300) // Debounce

            try {
                // Fetch types once when entering the space
                if (fetchTypes) {
                    typesMap = fetchObjectTypesForSpace(SpaceId(spaceId))
                }

                val filters = buildList {
                    // Exclude deleted
                    add(DVFilter(
                        relation = Relations.IS_DELETED,
                        condition = DVFilterCondition.NOT_EQUAL,
                        value = true
                    ))
                    // Exclude archived
                    add(DVFilter(
                        relation = Relations.IS_ARCHIVED,
                        condition = DVFilterCondition.NOT_EQUAL,
                        value = true
                    ))
                    // Exclude templates
                    add(DVFilter(
                        relation = Relations.TYPE_UNIQUE_KEY,
                        condition = DVFilterCondition.NOT_EQUAL,
                        value = ObjectTypeUniqueKeys.TEMPLATE
                    ))
                    // Include common layouts
                    add(DVFilter(
                        relation = Relations.LAYOUT,
                        condition = DVFilterCondition.IN,
                        value = listOf(
                            ObjectType.Layout.BASIC.code.toDouble(),
                            ObjectType.Layout.PROFILE.code.toDouble(),
                            ObjectType.Layout.TODO.code.toDouble(),
                            ObjectType.Layout.NOTE.code.toDouble(),
                            ObjectType.Layout.BOOKMARK.code.toDouble(),
                            ObjectType.Layout.SET.code.toDouble(),
                            ObjectType.Layout.COLLECTION.code.toDouble(),
                            ObjectType.Layout.IMAGE.code.toDouble(),
                            ObjectType.Layout.FILE.code.toDouble(),
                            ObjectType.Layout.PDF.code.toDouble(),
                            ObjectType.Layout.AUDIO.code.toDouble(),
                            ObjectType.Layout.VIDEO.code.toDouble(),
                            ObjectType.Layout.CHAT_DERIVED.code.toDouble()
                        )
                    ))
                }

                val sorts = listOf(
                    DVSort(
                        relationKey = Relations.LAST_OPENED_DATE,
                        type = DVSortType.DESC,
                        relationFormat = RelationFormat.DATE,
                        includeTime = true
                    )
                )

                val params = SearchObjects.Params(
                    space = SpaceId(spaceId),
                    filters = filters,
                    sorts = sorts,
                    fulltext = query,
                    keys = ObjectSearchConstants.defaultKeys,
                    limit = 100
                )

                val result = searchObjects(params)
                result.process(
                    failure = { error ->
                        Timber.e(error, "Error searching objects")
                        _objectItems.value = emptyList()
                    },
                    success = { foundObjects ->
                        _objectItems.value = foundObjects.map { obj ->
                            val typeId = obj.type.firstOrNull()
                            val objType = typeId?.let { typesMap[it] }
                            ObjectItemView(
                                obj = obj,
                                icon = obj.objectIcon(builder = urlBuilder, objType = objType),
                                typeName = objType?.name.orEmpty()
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "Error searching objects")
                _objectItems.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun fetchObjectTypesForSpace(spaceId: SpaceId): Map<Id, ObjectWrapper.Type> {
        val filters = buildList {
            add(DVFilter(
                relation = Relations.IS_DELETED,
                condition = DVFilterCondition.NOT_EQUAL,
                value = true
            ))
            add(DVFilter(
                relation = Relations.IS_ARCHIVED,
                condition = DVFilterCondition.NOT_EQUAL,
                value = true
            ))
            add(DVFilter(
                relation = Relations.TYPE_UNIQUE_KEY,
                condition = DVFilterCondition.NOT_EQUAL,
                value = ObjectTypeUniqueKeys.TEMPLATE
            ))
            add(DVFilter(
                relation = Relations.LAYOUT,
                condition = DVFilterCondition.EQUAL,
                value = ObjectType.Layout.OBJECT_TYPE.code.toDouble()
            ))
            add(DVFilter(
                relation = Relations.UNIQUE_KEY,
                condition = DVFilterCondition.NOT_EMPTY
            ))
        }

        val params = SearchObjects.Params(
            space = spaceId,
            filters = filters,
            sorts = emptyList(),
            keys = ObjectSearchConstants.defaultKeysObjectType,
            limit = 0
        )

        return try {
            val results = searchObjects(params).getOrNull() ?: emptyList()
            results.mapNotNull { obj ->
                obj.map.mapToObjectWrapperType()?.let { type ->
                    type.id to type
                }
            }.toMap()
        } catch (e: Exception) {
            Timber.e(e, "Error fetching object types for space")
            emptyMap()
        }
    }

    sealed class ScreenState {
        data object SpaceSelection : ScreenState()
        data object ObjectSelection : ScreenState()
    }

    sealed class Command {
        data class FinishWithSuccess(val appWidgetId: Int) : Command()
        data class ShowError(val message: String) : Command()
    }

    class Factory @Inject constructor(
        private val context: Context,
        private val spaceViews: SpaceViewSubscriptionContainer,
        private val urlBuilder: UrlBuilder,
        private val searchObjects: SearchObjects
    ) : ViewModelProvider.Factory {

        var appWidgetId: Int = -1

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ObjectShortcutWidgetConfigViewModel(
                appWidgetId = appWidgetId,
                context = context,
                spaceViews = spaceViews,
                urlBuilder = urlBuilder,
                searchObjects = searchObjects
            ) as T
        }
    }
}
