package com.anytypeio.anytype.feature_os_widgets.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.ObjectView
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.ui.objectIcon
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.`object`.GetObject
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.feature_os_widgets.persistence.DataViewItemsFetcher
import com.anytypeio.anytype.feature_os_widgets.persistence.OsWidgetDataViewEntity
import com.anytypeio.anytype.feature_os_widgets.persistence.fetchObjectTypesForSpace
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

class DataViewWidgetConfigViewModel(
    private val appWidgetId: Int,
    private val spaceViews: SpaceViewSubscriptionContainer,
    private val urlBuilder: UrlBuilder,
    private val searchObjects: SearchObjects,
    private val getObject: GetObject,
    private val dataStore: DataViewWidgetConfigStore,
    private val itemsFetcher: DataViewItemsFetcher,
    private val widgetUpdater: DataViewWidgetUpdater
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

    private val _selectedObject = MutableStateFlow<ObjectItemView?>(null)
    val selectedObject: StateFlow<ObjectItemView?> = _selectedObject.asStateFlow()

    private val _viewers = MutableStateFlow<List<ViewerView>>(emptyList())
    val viewers: StateFlow<List<ViewerView>> = _viewers.asStateFlow()

    private val _commands = MutableSharedFlow<Command>()
    val commands: SharedFlow<Command> = _commands.asSharedFlow()

    private var searchJob: Job? = null
    private var typesMap: Map<Id, ObjectWrapper.Type> = emptyMap()
    private var lastObjectView: ObjectView? = null

    init {
        loadSpaces()
    }

    private fun loadSpaces() {
        val allSpaces = spaceViews.get()
        _spaces.value = allSpaces
            .filter { it.isActive && !it.isOneToOneSpace }
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
        _selectedObject.value = item
        val space = _selectedSpace.value ?: return
        loadViewers(
            objectId = item.obj.id,
            spaceId = space.targetSpaceId.orEmpty()
        )
    }

    fun onViewerSelected(viewer: ViewerView) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val space = _selectedSpace.value ?: return@launch
                val obj = _selectedObject.value ?: return@launch
                val spaceId = space.targetSpaceId.orEmpty()
                val objectView = lastObjectView

                // Fetch items from the data view
                val items = if (objectView != null) {
                    itemsFetcher.fetchItems(
                        objectView = objectView,
                        viewerId = viewer.id,
                        spaceId = spaceId,
                        typesMap = typesMap,
                        subscriptionKey = appWidgetId.toString()
                    )
                } else {
                    emptyList()
                }

                val config = OsWidgetDataViewEntity(
                    appWidgetId = appWidgetId,
                    spaceId = spaceId,
                    objectId = obj.obj.id,
                    objectName = obj.obj.name.orEmpty(),
                    objectLayout = obj.obj.layout?.code ?: 0,
                    viewerId = viewer.id,
                    viewerName = viewer.name,
                    items = items
                )
                dataStore.save(config)
                widgetUpdater.update(appWidgetId)
                _commands.emit(Command.FinishWithSuccess(appWidgetId))
            } catch (e: Exception) {
                Timber.e(e, "Error saving data view widget config")
                _commands.emit(Command.ShowError(e.message ?: "Unknown error"))
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onBack() {
        when (_screenState.value) {
            ScreenState.ViewerSelection -> {
                _selectedObject.value = null
                _viewers.value = emptyList()
                _screenState.value = ScreenState.ObjectSelection
            }
            ScreenState.ObjectSelection -> {
                searchJob?.cancel()
                _selectedSpace.value = null
                _objectItems.value = emptyList()
                typesMap = emptyMap()
                _searchQuery.value = ""
                _screenState.value = ScreenState.SpaceSelection
            }
            ScreenState.SpaceSelection -> {
                // Nothing to go back to
            }
        }
    }

    private fun loadViewers(objectId: Id, spaceId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val objectView = getObject.run(
                    GetObject.Params(
                        target = objectId,
                        space = SpaceId(spaceId),
                        saveAsLastOpened = false
                    )
                )
                lastObjectView = objectView
                val dataViewBlock = objectView.blocks.firstOrNull { block ->
                    block.content is Block.Content.DataView
                }
                val dataView = dataViewBlock?.content as? Block.Content.DataView
                if (dataView != null && dataView.viewers.isNotEmpty()) {
                    _viewers.value = dataView.viewers.map { viewer ->
                        ViewerView(
                            id = viewer.id,
                            name = viewer.name,
                            type = viewer.type
                        )
                    }
                    _screenState.value = ScreenState.ViewerSelection
                } else {
                    Timber.w("No DataView block or no viewers found for object $objectId")
                    _commands.emit(Command.ShowError("No views found for this object"))
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading viewers for object $objectId")
                _commands.emit(Command.ShowError(e.message ?: "Unknown error"))
            } finally {
                _isLoading.value = false
            }
        }
    }


    private fun searchObjectsInSpace(spaceId: String, query: String, fetchTypes: Boolean) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _isLoading.value = true
            delay(300) // Debounce

            try {
                if (fetchTypes) {
                    typesMap = fetchObjectTypesForSpace(
                        searchObjects = searchObjects,
                        spaceId = SpaceId(spaceId)
                    )
                }

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
                    // Only show SET and COLLECTION layouts
                    add(DVFilter(
                        relation = Relations.LAYOUT,
                        condition = DVFilterCondition.IN,
                        value = listOf(
                            ObjectType.Layout.SET.code.toDouble(),
                            ObjectType.Layout.COLLECTION.code.toDouble()
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

    /**
     * UI model for a DataView viewer entry.
     */
    data class ViewerView(
        val id: String,
        val name: String,
        val type: Block.Content.DataView.Viewer.Type
    )

    sealed class ScreenState {
        data object SpaceSelection : ScreenState()
        data object ObjectSelection : ScreenState()
        data object ViewerSelection : ScreenState()
    }

    sealed class Command {
        data class FinishWithSuccess(val appWidgetId: Int) : Command()
        data class ShowError(val message: String) : Command()
    }

    class Factory @Inject constructor(
        private val spaceViews: SpaceViewSubscriptionContainer,
        private val urlBuilder: UrlBuilder,
        private val searchObjects: SearchObjects,
        private val getObject: GetObject,
        private val dataStore: DataViewWidgetConfigStore,
        private val itemsFetcher: DataViewItemsFetcher,
        private val widgetUpdater: DataViewWidgetUpdater
    ) {
        fun create(appWidgetId: Int): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DataViewWidgetConfigViewModel(
                        appWidgetId = appWidgetId,
                        spaceViews = spaceViews,
                        urlBuilder = urlBuilder,
                        searchObjects = searchObjects,
                        getObject = getObject,
                        dataStore = dataStore,
                        itemsFetcher = itemsFetcher,
                        widgetUpdater = widgetUpdater
                    ) as T
                }
            }
        }
    }
}
