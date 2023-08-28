package com.anytypeio.anytype.presentation.objects

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.MarketplaceObjectTypeIds
import com.anytypeio.anytype.core_models.MarketplaceObjectTypeIds.MARKETPLACE_OBJECT_TYPE_PREFIX
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.launch.GetDefaultPageType
import com.anytypeio.anytype.domain.workspace.AddObjectToWorkspace
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.presentation.common.BaseViewModel
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import timber.log.Timber

class ObjectTypeChangeViewModel(
    private val getObjectTypes: GetObjectTypes,
    private val addObjectToWorkspace: AddObjectToWorkspace,
    private val dispatchers: AppCoroutineDispatchers,
    private val workspaceManager: WorkspaceManager,
    private val getDefaultPageType: GetDefaultPageType
) : BaseViewModel() {

    private val userInput = MutableStateFlow(DEFAULT_INPUT)
    private val searchQuery = userInput.take(1).onCompletion {
        emitAll(userInput.drop(1).debounce(DEBOUNCE_DURATION).distinctUntilChanged())
    }

    private val setup = MutableSharedFlow<Setup>(replay = 0)

    val views = MutableStateFlow<List<ObjectTypeItemView>>(emptyList())
    val commands = MutableSharedFlow<Command>()

    private val pipeline = combine(searchQuery, setup) { query, setup ->
        val myTypes = proceedWithGettingMyTypes(
            query = query
        )
        val marketplaceTypes = proceedWithGettingMarketplaceTypes(
            myTypes = myTypes,
            setup = setup,
            query = query
        )
        val filteredLibraryTypes = filterLibraryTypesByExcluded(
            libraryTypes = myTypes,
            excludeTypes = setup.excludeTypes
        )
        proceedWithBuildingViews(
            myTypes = filteredLibraryTypes,
            marketplaceTypes = marketplaceTypes,
            setup = setup
        )
    }.catch {
        sendToast("Error occurred: $it. Please try again later.")
    }

    private fun filterLibraryTypesByExcluded(
        libraryTypes: List<ObjectWrapper.Type>,
        excludeTypes: List<Id>
    ) = libraryTypes.filter { !excludeTypes.contains(it.id) }

    init {
        viewModelScope.launch {
            // Processing on the io thread, collecting on the main thread.
            pipeline.flowOn(dispatchers.io).collect { views.value = it }
        }
    }

    fun onStart(
        isWithCollection: Boolean,
        isWithBookmark: Boolean,
        excludeTypes: List<Id>,
        selectedTypes: List<Id>,
        isSetSource: Boolean
    ) {
        viewModelScope.launch {
            setup.emit(
                Setup(
                    isWithCollection = isWithCollection,
                    isWithBookmark = isWithBookmark,
                    excludeTypes = excludeTypes,
                    selectedTypes = selectedTypes,
                    isSetSource = isSetSource
                )
            )
        }
    }

    fun onStart(
        isWithCollection: Boolean,
        isWithBookmark: Boolean,
        isSetSource: Boolean
    ) {
        viewModelScope.launch {
            getDefaultPageType.execute(Unit).fold(
                onFailure = { e ->
                    Timber.e(e, "Error while getting user settings")
                },
                onSuccess = {
                    setup.emit(
                        Setup(
                            isWithCollection = isWithCollection,
                            isWithBookmark = isWithBookmark,
                            excludeTypes = listOf(it.type.orEmpty()),
                            selectedTypes = emptyList(),
                            isSetSource = isSetSource
                        )
                    )
                }
            )
        }
    }

    fun onQueryChanged(input: String) {
        userInput.value = input
    }

    fun onItemClicked(id: String, name: String) {
        viewModelScope.launch {
            if (id.contains(MARKETPLACE_OBJECT_TYPE_PREFIX)) {
                val params = AddObjectToWorkspace.Params(listOf(id))
                addObjectToWorkspace(params = params).process(
                    success = { objects ->
                        if (objects.isNotEmpty()) {
                            commands.emit(Command.TypeAdded(type = name))
                            proceedWithDispatchingType(objects.first(), name)
                        }
                    },
                    failure = {
                        sendToast("Something went wrong. Please, try again later.")
                    }
                )
            } else {
                proceedWithDispatchingType(id, name)
            }
        }
    }

    private suspend fun proceedWithDispatchingType(id: String, name: String) {
        commands.emit(
            Command.DispatchType(
                id = id,
                name = name
            )
        )
    }

    private fun proceedWithBuildingViews(
        myTypes: List<ObjectWrapper.Type>,
        setup: Setup,
        marketplaceTypes: List<ObjectWrapper.Type>
    ) = buildList {
        if (myTypes.isNotEmpty()) {
            val views = myTypes.getObjectTypeViewsForSBPage(
                isWithCollection = setup.isWithCollection,
                isWithBookmark = setup.isWithBookmark,
                excludeTypes = setup.excludeTypes,
                selectedTypes = setup.selectedTypes
            ).map {
                ObjectTypeItemView.Type(it)
            }
            if (views.isNotEmpty()) {
                add(ObjectTypeItemView.Section.Library)
            }
            addAll(views)
        }
        if (marketplaceTypes.isNotEmpty()) {
            val views = marketplaceTypes.getObjectTypeViewsForSBPage(
                isWithCollection = setup.isWithCollection,
                isWithBookmark = setup.isWithBookmark,
                excludeTypes = setup.excludeTypes,
                selectedTypes = setup.selectedTypes
            ).map {
                ObjectTypeItemView.Type(it)
            }
            if (views.isNotEmpty()) {
                add(ObjectTypeItemView.Section.Marketplace)
            }
            addAll(views)
        }
        if (isEmpty() && userInput.value.isNotEmpty()) {
            add(ObjectTypeItemView.EmptyState(userInput.value))
        }
    }

    private suspend fun proceedWithGettingMarketplaceTypes(
        myTypes: List<ObjectWrapper.Type>,
        setup: Setup,
        query: String
    ): List<ObjectWrapper.Type> {
        val excludedMarketplaceTypes = buildList {
            addAll(myTypes.mapNotNull { it.sourceObject })
            if (!setup.isWithBookmark) {
                add(MarketplaceObjectTypeIds.BOOKMARK)
            }
        }
        val marketplaceTypes = getObjectTypes.run(
            GetObjectTypes.Params(
                filters = buildList {
                    addAll(ObjectSearchConstants.filterObjectTypeMarketplace)
                    if (excludedMarketplaceTypes.isNotEmpty()) {
                        add(
                            DVFilter(
                                relation = Relations.ID,
                                condition = DVFilterCondition.NOT_IN,
                                value = excludedMarketplaceTypes
                            )
                        )
                    }
                    add(
                        DVFilter(
                            relation = Relations.RECOMMENDED_LAYOUT,
                            condition = DVFilterCondition.IN,
                            value = SupportedLayouts.editorLayouts.map {
                                it.code.toDouble()
                            }
                        )
                    )
                },
                sorts = ObjectSearchConstants.defaultObjectSearchSorts(),
                query = query,
                keys = ObjectSearchConstants.defaultKeysObjectType
            )
        )
        return marketplaceTypes
    }

    private suspend fun proceedWithGettingMyTypes(
        query: String
    ) = getObjectTypes.run(
        GetObjectTypes.Params(
            filters = buildList {
                addAll(
                    ObjectSearchConstants.filterObjectTypeLibrary(
                        // TODO MULTISPACES fix object types
                        space = workspaceManager.getCurrentWorkspace()
                    )
                )
                add(
                    DVFilter(
                        relation = Relations.RECOMMENDED_LAYOUT,
                        condition = DVFilterCondition.IN,
                        value = SupportedLayouts.editorLayouts.map {
                            it.code.toDouble()
                        }
                    )
                )
            },
            sorts = ObjectSearchConstants.defaultObjectSearchSorts(),
            query = query,
            keys = ObjectSearchConstants.defaultKeysObjectType
        )
    )

    companion object {
        const val DEBOUNCE_DURATION = 300L
        const val DEFAULT_INPUT = ""
    }

    data class Setup(
        val isWithCollection: Boolean,
        val isWithBookmark: Boolean,
        val excludeTypes: List<Id>,
        val selectedTypes: List<Id>,
        val isSetSource: Boolean
    )

    sealed class Command {
        data class DispatchType(
            val id: Id,
            val name: String
        ) : Command()

        data class TypeAdded(val type: String) : Command()
    }
}