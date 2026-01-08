package com.anytypeio.anytype.presentation.objects

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Marketplace
import com.anytypeio.anytype.core_models.MarketplaceObjectTypeIds
import com.anytypeio.anytype.core_models.MarketplaceObjectTypeIds.MARKETPLACE_OBJECT_TYPE_PREFIX
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SupportedLayouts
import com.anytypeio.anytype.core_models.SupportedLayouts.getCreateObjectLayouts
import com.anytypeio.anytype.core_models.ext.mapToObjectWrapperType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.launch.GetDefaultObjectType
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.spaces.AddObjectToSpace
import com.anytypeio.anytype.domain.spaces.AddObjectTypeToSpace
import com.anytypeio.anytype.domain.workspace.SpaceManager
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
    private val addObjectTypeToSpace: AddObjectTypeToSpace,
    private val dispatchers: AppCoroutineDispatchers,
    private val spaceManager: SpaceManager,
    private val getDefaultObjectType: GetDefaultObjectType,
    private val urlBuilder: UrlBuilder,
    private val spaceViews: SpaceViewSubscriptionContainer
) : BaseViewModel() {

    private val userInput = MutableStateFlow(DEFAULT_INPUT)
    private val searchQuery = userInput.take(1).onCompletion {
        emitAll(userInput.drop(1).debounce(DEBOUNCE_DURATION).distinctUntilChanged())
    }

    private val setup = MutableSharedFlow<Setup>(replay = 0)
    private val _objTypes = MutableStateFlow<List<ObjectWrapper.Type>>(emptyList())

    val views = MutableStateFlow<List<ObjectTypeItemView>>(emptyList())
    val commands = MutableSharedFlow<Command>()

    private val pipeline = combine(searchQuery, setup) { query, setup ->
        // Determine space UX type to decide whether CHAT types should be shown
        val spaceView = spaceViews.get(SpaceId(spaceManager.get()))
        val spaceUxType = spaceView?.spaceUxType
        val createLayouts = getCreateObjectLayouts(spaceUxType)

        val recommendedLayouts = when (setup.screen) {
            Screen.DATA_VIEW_SOURCE,
            Screen.EMPTY_DATA_VIEW_SOURCE -> createLayouts + SupportedLayouts.fileLayouts
            Screen.OBJECT_TYPE_CHANGE,
            Screen.DEFAULT_OBJECT_TYPE -> createLayouts
        }
        val myTypes = proceedWithGettingMyTypes(
            query = query,
            setup = setup,
            recommendedLayouts = recommendedLayouts
        )
        val marketplaceTypes = proceedWithGettingMarketplaceTypes(
            myTypes = myTypes,
            setup = setup,
            query = query,
            createLayouts = createLayouts
        )
        _objTypes.value = myTypes + marketplaceTypes
        val filteredLibraryTypes = filterLibraryTypesByExcluded(
            libraryTypes = myTypes,
            excludeTypes = setup.excludeTypes
        )
        proceedWithBuildingViews(
            myTypes = filteredLibraryTypes,
            marketplaceTypes = marketplaceTypes,
            setup = setup
        ).also {
            Timber.d("Built views: ${it.size}")
        }
    }.catch {
        Timber.e(it, "Error in pipeline")
        sendToast("Error occurred: $it. Please try again later.")
    }

    private fun filterLibraryTypesByExcluded(
        libraryTypes: List<ObjectWrapper.Type>,
        excludeTypes: List<Id>
    ) = libraryTypes.filter { !excludeTypes.contains(it.id) }

    init {
        Timber.i("ObjectTypeChangeViewModel, init")
        viewModelScope.launch {
            // Processing on the io thread, collecting on the main thread.
            pipeline.flowOn(dispatchers.io).collect {
                Timber.d("Got views: ${it.size}")
                views.value = it
            }
        }
    }

    fun onStart(
        screen: Screen,
        excludeTypes: List<Id> = emptyList(),
        selectedTypes: List<Id> = emptyList()
    ) {
        Timber.d("Starting with params: screen=$screen, excludeTypes=$excludeTypes, selectedTypes=$selectedTypes")
        viewModelScope.launch {
            when (screen) {
                Screen.DEFAULT_OBJECT_TYPE -> {
                    getDefaultObjectType.execute(
                        SpaceId(spaceManager.get())
                    ).fold(
                        onFailure = { e ->
                            Timber.e(e, "Error while getting user settings")
                        },
                        onSuccess = {
                            setup.emit(
                                Setup(
                                    screen = screen,
                                    excludeTypes = excludeTypes + it.type.key,
                                    selectedTypes = selectedTypes
                                )
                            )
                        }
                    )
                }
                else -> {
                    setup.emit(
                        Setup(
                            screen = screen,
                            excludeTypes = excludeTypes,
                            selectedTypes = selectedTypes
                        )
                    )
                }
            }
        }
    }

    fun onQueryChanged(input: String) {
        userInput.value = input
    }

    fun onItemClicked(item: ObjectTypeView) {
        viewModelScope.launch {
            if (item.id.contains(MARKETPLACE_OBJECT_TYPE_PREFIX)) {
                val params = AddObjectToSpace.Params(
                    obj = item.id,
                    space = spaceManager.get()
                )
                addObjectTypeToSpace.async(params = params).fold(
                    onSuccess = {
                        proceedWithNewlyAddedObjectType(it)
                    },
                    onFailure = {
                        Timber.e(it, "Error while adding object by id:${item.id} to space")
                        sendToast("Error while adding object by id:${item.id} to space")
                    }
                )
            } else {
                val objType = _objTypes.value.firstOrNull { it.id == item.id }
                if (objType == null) {
                    Timber.e("Object Type Change Screen, type is not found in types list")
                    sendToast("Error while choosing object type by key:${item.key}")
                } else {
                    proceedWithDispatchingType(item = objType)
                }
            }
        }
    }

    private suspend fun proceedWithNewlyAddedObjectType(result: AddObjectToSpace.Result) {
        val struct = result.type
        val type = struct?.mapToObjectWrapperType()
        if (type != null) {
            commands.emit(Command.TypeAdded(type = type.name.orEmpty()))
            proceedWithDispatchingType(item = type)
        } else {
            Timber.e("Type is not valid")
            sendToast("Error while adding object type by id:${result.id} to space")
        }
    }

    private suspend fun proceedWithDispatchingType(
        item: ObjectWrapper.Type
    ) {
        commands.emit(Command.DispatchType(item))
    }

    private fun proceedWithBuildingViews(
        myTypes: List<ObjectWrapper.Type>,
        setup: Setup,
        marketplaceTypes: List<ObjectWrapper.Type>
    ) = buildList {
        Timber.d("My types: ${myTypes.size}")
        Timber.d("Marketplace types: ${marketplaceTypes.size}")

        val isWithCollection = when (setup.screen) {
            Screen.DATA_VIEW_SOURCE -> true
            Screen.OBJECT_TYPE_CHANGE,
            Screen.EMPTY_DATA_VIEW_SOURCE,
            Screen.DEFAULT_OBJECT_TYPE -> false
        }
        val isWithBookmark = when (setup.screen) {
            Screen.DATA_VIEW_SOURCE,
            Screen.EMPTY_DATA_VIEW_SOURCE -> true
            Screen.OBJECT_TYPE_CHANGE,
            Screen.DEFAULT_OBJECT_TYPE -> false
        }

        if (myTypes.isNotEmpty()) {
            val views = myTypes.getObjectTypeViewsForSBPage(
                isWithCollection = isWithCollection,
                isWithBookmark = isWithBookmark,
                excludeTypes = setup.excludeTypes,
                selectedTypes = setup.selectedTypes,
                useCustomComparator = false
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
                isWithCollection = isWithCollection,
                isWithBookmark = isWithBookmark,
                excludeTypes = setup.excludeTypes,
                selectedTypes = setup.selectedTypes,
                useCustomComparator = false
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
        query: String,
        createLayouts: List<ObjectType.Layout>
    ): List<ObjectWrapper.Type> {
        val excludedMarketplaceTypes = buildList {
            addAll(myTypes.map { it.uniqueKey })
            when (setup.screen) {
                Screen.OBJECT_TYPE_CHANGE,
                Screen.DEFAULT_OBJECT_TYPE -> add(MarketplaceObjectTypeIds.BOOKMARK)
                Screen.DATA_VIEW_SOURCE,
                Screen.EMPTY_DATA_VIEW_SOURCE -> { /* include bookmark */ }
            }
        }
        // For marketplace, still respect the same create layouts logic so UI is consistent
        val marketplaceTypes = getObjectTypes.run(
            GetObjectTypes.Params(
                space = SpaceId(Marketplace.MARKETPLACE_SPACE_ID),
                filters = buildList {
                    addAll(
                        ObjectSearchConstants.filterTypes(
                            recommendedLayouts = createLayouts
                        )
                    )
                    if (excludedMarketplaceTypes.isNotEmpty()) {
                        add(
                            DVFilter(
                                relation = Relations.UNIQUE_KEY,
                                condition = DVFilterCondition.NOT_IN,
                                value = excludedMarketplaceTypes
                            )
                        )
                    }
                },
                sorts = ObjectSearchConstants.defaultObjectTypeSearchSorts(),
                query = query,
                keys = ObjectSearchConstants.defaultKeysObjectType
            )
        )
        return marketplaceTypes
    }

    private suspend fun proceedWithGettingMyTypes(
        query: String,
        setup: Setup,
        recommendedLayouts: List<ObjectType.Layout>
    ): List<ObjectWrapper.Type> {
        val excludeParticipantAndTemplates = when (setup.screen) {
            Screen.DATA_VIEW_SOURCE,
            Screen.EMPTY_DATA_VIEW_SOURCE -> false
            Screen.OBJECT_TYPE_CHANGE,
            Screen.DEFAULT_OBJECT_TYPE -> true
        }
        return getObjectTypes.run(
            GetObjectTypes.Params(
                // TODO DROID-2916 Provide space id to vm params
                space = SpaceId(spaceManager.get()),
                filters = ObjectSearchConstants.filterTypes(
                    recommendedLayouts = recommendedLayouts,
                    excludeParticipant = excludeParticipantAndTemplates,
                    excludeTemplates = excludeParticipantAndTemplates
                ),
                sorts = ObjectSearchConstants.defaultObjectTypeSearchSorts(),
                query = query,
                keys = ObjectSearchConstants.defaultKeysObjectType
            )
        )
    }

    companion object {
        const val DEBOUNCE_DURATION = 300L
        const val DEFAULT_INPUT = ""
    }

    enum class Screen {
        OBJECT_TYPE_CHANGE,        // ObjectSelectTypeFragment
        DATA_VIEW_SOURCE,          // DataViewSelectSourceFragment
        EMPTY_DATA_VIEW_SOURCE,    // EmptyDataViewSelectSourceFragment
        DEFAULT_OBJECT_TYPE        // AppDefaultObjectTypeFragment
    }

    data class Setup(
        val screen: Screen,
        val excludeTypes: List<Id> = emptyList(),
        val selectedTypes: List<Id> = emptyList()
    )

    sealed class Command {
        data class DispatchType(
            val item: ObjectWrapper.Type
        ) : Command()

        data class TypeAdded(val type: String) : Command()
    }
}