package com.anytypeio.anytype.presentation.objects

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectTypeUniqueKeys
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.SupportedLayouts
import com.anytypeio.anytype.core_models.SupportedLayouts.getCreateObjectLayouts
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.presentation.common.BaseViewModel
import kotlinx.coroutines.FlowPreview
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
    private val vmParams: VmParams,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val dispatchers: AppCoroutineDispatchers,
    private val spaceViews: SpaceViewSubscriptionContainer
) : BaseViewModel() {

    private val userInput = MutableStateFlow(DEFAULT_INPUT)
    @OptIn(FlowPreview::class)
    private val searchQuery = userInput.take(1).onCompletion {
        emitAll(userInput.drop(1).debounce(DEBOUNCE_DURATION).distinctUntilChanged())
    }

    private val excludeTypes = MutableStateFlow(vmParams.excludeTypes)

    val viewState = MutableStateFlow<ObjectTypeChangeViewState>(ObjectTypeChangeViewState.Loading)
    val commands = MutableSharedFlow<Command>()

    private val pipeline = combine(
        searchQuery,
        excludeTypes,
        storeOfObjectTypes.trackChanges()
    ) { query, currentExcludeTypes, _ ->
        // Determine space UX type to decide whether CHAT types should be shown
        val spaceView = spaceViews.get(vmParams.spaceId)
        val spaceUxType = spaceView?.spaceUxType
        val createLayouts = getCreateObjectLayouts(spaceUxType)

        val recommendedLayouts = when (vmParams.screen) {
            Screen.DATA_VIEW_SOURCE,
            Screen.EMPTY_DATA_VIEW_SOURCE -> createLayouts + SupportedLayouts.fileLayouts
            Screen.EDITOR_OBJECT_TYPE_UPDATE -> SupportedLayouts.editorCreateObjectLayouts
            Screen.CREATE_OBJECT_FOR_COLLECTION -> createLayouts
            Screen.DEFAULT_OBJECT_TYPE -> listOf(
                ObjectType.Layout.BASIC,
                ObjectType.Layout.PROFILE,
                ObjectType.Layout.TODO,
                ObjectType.Layout.NOTE,
                ObjectType.Layout.BOOKMARK
            )
        }

        val filteredTypes = filterTypes(
            query = query,
            excludeTypes = currentExcludeTypes,
            recommendedLayouts = recommendedLayouts
        )

        // Sort types by priority before partitioning
        val isChatSpace = spaceUxType == SpaceUxType.CHAT || spaceUxType == SpaceUxType.ONE_TO_ONE
        val sortedTypes = filteredTypes.sortByTypePriority(isChatSpace)

        proceedWithBuildingViewState(
            types = sortedTypes,
            excludeTypes = currentExcludeTypes,
            query = query
        ).also {
            Timber.d("Built view state: $it")
        }
    }.catch {
        Timber.e(it, "Error in pipeline")
        sendToast("Error occurred: $it. Please try again later.")
    }

    private suspend fun filterTypes(
        query: String,
        excludeTypes: List<Id>,
        recommendedLayouts: List<ObjectType.Layout>
    ): List<ObjectWrapper.Type> {
        val allTypes = storeOfObjectTypes.getAll()

        val excludeParticipantAndTemplates = when (vmParams.screen) {
            Screen.DATA_VIEW_SOURCE,
            Screen.EMPTY_DATA_VIEW_SOURCE -> false
            Screen.EDITOR_OBJECT_TYPE_UPDATE,
            Screen.DEFAULT_OBJECT_TYPE,
            Screen.CREATE_OBJECT_FOR_COLLECTION -> true
        }

        return allTypes.filter { type ->
            // Filter by layout
            val layout = type.recommendedLayout
            val isLayoutMatch = layout != null && recommendedLayouts.contains(layout)

            // Filter by query (case-insensitive name search)
            val isQueryMatch = query.isEmpty() ||
                    type.name?.contains(query, ignoreCase = true) == true

            // Filter by excluded types
            val isNotExcluded = !excludeTypes.contains(type.id)

            // Filter participant and templates if needed
            val isNotParticipant = !excludeParticipantAndTemplates ||
                    type.recommendedLayout != ObjectType.Layout.PARTICIPANT
            val isNotTemplate = !excludeParticipantAndTemplates ||
                    type.uniqueKey != ObjectTypeIds.TEMPLATE

            isLayoutMatch && isQueryMatch && isNotExcluded && isNotParticipant && isNotTemplate
        }
    }

    init {
        Timber.i("ObjectTypeChangeViewModel, init, vmParams: $vmParams")

        viewModelScope.launch {
            // Processing on the io thread, collecting on the main thread.
            pipeline.flowOn(dispatchers.io).collect {
                Timber.d("Got view state: $it")
                viewState.value = it
            }
        }
    }

    fun onQueryChanged(input: String) {
        userInput.value = input
    }

    fun onItemClicked(item: ObjectTypeChangeItem.Type) {
        viewModelScope.launch {
            val objType = storeOfObjectTypes.get(item.id)
            if (objType == null) {
                Timber.e("Object Type Change Screen, type is not found in types list")
                sendToast("Error while choosing object type by key:${item.key}")
            } else {
                commands.emit(Command.DispatchType(objType))
            }
        }
    }

    private fun proceedWithBuildingViewState(
        types: List<ObjectWrapper.Type>,
        excludeTypes: List<Id>,
        query: String
    ): ObjectTypeChangeViewState {
        Timber.d("Types count: ${types.size}")

        val isWithListTypes = when (vmParams.screen) {
            Screen.DATA_VIEW_SOURCE,
            Screen.EMPTY_DATA_VIEW_SOURCE,
            Screen.CREATE_OBJECT_FOR_COLLECTION -> true
            Screen.EDITOR_OBJECT_TYPE_UPDATE,
            Screen.DEFAULT_OBJECT_TYPE -> false
        }
        val isWithBookmark = when (vmParams.screen) {
            Screen.DATA_VIEW_SOURCE,
            Screen.EMPTY_DATA_VIEW_SOURCE,
            Screen.EDITOR_OBJECT_TYPE_UPDATE,
            Screen.DEFAULT_OBJECT_TYPE,
            Screen.CREATE_OBJECT_FOR_COLLECTION -> true
        }

        if (types.isEmpty() && query.isNotEmpty()) {
            return ObjectTypeChangeViewState.Empty
        }

        if (types.isEmpty()) {
            return ObjectTypeChangeViewState.Loading
        }

        // Partition types: Lists (Set/Collection) vs Objects (everything else)
        val (listTypes, objectTypes) = types.partition { type ->
            type.uniqueKey == ObjectTypeUniqueKeys.SET ||
                    type.uniqueKey == ObjectTypeUniqueKeys.COLLECTION
        }

        val listItems = listTypes.toTypeItems(
            isWithListTypes = isWithListTypes,
            isWithBookmark = isWithBookmark,
            excludeTypes = excludeTypes
        )
        val objectItems = objectTypes.toTypeItems(
            isWithListTypes = isWithListTypes,
            isWithBookmark = isWithBookmark,
            excludeTypes = excludeTypes
        )

        val items = buildList {
            // Add "Lists" section only if there are filtered list items
            if (listItems.isNotEmpty()) {
                add(ObjectTypeChangeItem.Section.Lists)
                addAll(listItems)
            }

            // Add "Objects" section only if there are filtered object items
            if (objectItems.isNotEmpty()) {
                add(ObjectTypeChangeItem.Section.Objects)
                addAll(objectItems)
            }
        }

        return ObjectTypeChangeViewState.Content(items)
    }

    private fun List<ObjectWrapper.Type>.toTypeItems(
        isWithListTypes: Boolean,
        isWithBookmark: Boolean,
        excludeTypes: List<Id>
    ): List<ObjectTypeChangeItem.Type> {
        return this.toObjectTypeViews(
            includeListTypes = isWithListTypes,
            includeBookmarkType = isWithBookmark,
            excludedTypeIds = excludeTypes,
            selectedTypeIds = vmParams.selectedTypes
        ).map { typeView ->
            ObjectTypeChangeItem.Type(
                id = typeView.id,
                key = typeView.key,
                name = typeView.name,
                icon = typeView.icon,
                isSelected = typeView.isSelected
            )
        }
    }

    companion object {
        const val DEBOUNCE_DURATION = 300L
        const val DEFAULT_INPUT = ""
    }

    enum class Screen {
        EDITOR_OBJECT_TYPE_UPDATE,    // EditorObjectTypeUpdateFragment
        DATA_VIEW_SOURCE,             // DataViewSelectSourceFragment
        EMPTY_DATA_VIEW_SOURCE,       // EmptyDataViewSelectSourceFragment
        DEFAULT_OBJECT_TYPE,          // AppDefaultObjectTypeFragment
        CREATE_OBJECT_FOR_COLLECTION  // CollectionAddObjectTypeFragment
    }

    data class VmParams(
        val spaceId: SpaceId,
        val screen: Screen,
        val excludeTypes: List<Id> = emptyList(),
        val selectedTypes: List<Id> = emptyList()
    )

    sealed class Command {
        data class DispatchType(
            val item: ObjectWrapper.Type
        ) : Command()
    }
}

sealed class ObjectTypeChangeViewState {
    data object Loading : ObjectTypeChangeViewState()
    data object Empty : ObjectTypeChangeViewState()
    data class Content(val items: List<ObjectTypeChangeItem>) : ObjectTypeChangeViewState()
}

sealed class ObjectTypeChangeItem {
    sealed class Section : ObjectTypeChangeItem() {
        data object Lists : Section()
        data object Objects : Section()
    }

    data class Type(
        val id: Id,
        val key: Key,
        val name: String,
        val icon: ObjectIcon,
        val isSelected: Boolean = false
    ) : ObjectTypeChangeItem()
}
