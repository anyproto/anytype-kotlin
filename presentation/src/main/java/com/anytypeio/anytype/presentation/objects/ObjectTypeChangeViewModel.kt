package com.anytypeio.anytype.presentation.objects

import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.SupportedLayouts
import com.anytypeio.anytype.core_models.SupportedLayouts.getCreateObjectLayouts
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.launch.GetDefaultObjectType
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.presentation.common.BaseViewModel
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
    private val getDefaultObjectType: GetDefaultObjectType,
    private val spaceViews: SpaceViewSubscriptionContainer
) : BaseViewModel() {

    private val userInput = MutableStateFlow(DEFAULT_INPUT)
    private val searchQuery = userInput.take(1).onCompletion {
        emitAll(userInput.drop(1).debounce(DEBOUNCE_DURATION).distinctUntilChanged())
    }

    private val excludeTypes = MutableStateFlow(vmParams.excludeTypes)
    private val _objTypes = MutableStateFlow<List<ObjectWrapper.Type>>(emptyList())

    val views = MutableStateFlow<List<ObjectTypeItemView>>(emptyList())
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
            Screen.OBJECT_TYPE_CHANGE,
            Screen.DEFAULT_OBJECT_TYPE -> createLayouts
        }

        val filteredTypes = filterTypes(
            query = query,
            excludeTypes = currentExcludeTypes,
            recommendedLayouts = recommendedLayouts
        )
        _objTypes.value = filteredTypes

        proceedWithBuildingViews(
            types = filteredTypes,
            excludeTypes = currentExcludeTypes
        ).also {
            Timber.d("Built views: ${it.size}")
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
            Screen.OBJECT_TYPE_CHANGE,
            Screen.DEFAULT_OBJECT_TYPE -> true
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
                    type.recommendedLayout != ObjectType.Layout.OBJECT_TYPE

            isLayoutMatch && isQueryMatch && isNotExcluded && isNotParticipant && isNotTemplate
        }
    }

    init {
        Timber.i("ObjectTypeChangeViewModel, init, vmParams: $vmParams")

        // For DEFAULT_OBJECT_TYPE screen, fetch and exclude the current default type
        if (vmParams.screen == Screen.DEFAULT_OBJECT_TYPE) {
            viewModelScope.launch {
                getDefaultObjectType.execute(
                    vmParams.spaceId
                ).fold(
                    onFailure = { e ->
                        Timber.e(e, "Error while getting user settings")
                    },
                    onSuccess = { result ->
                        excludeTypes.value = vmParams.excludeTypes + result.type.key
                    }
                )
            }
        }

        viewModelScope.launch {
            // Processing on the io thread, collecting on the main thread.
            pipeline.flowOn(dispatchers.io).collect {
                Timber.d("Got views: ${it.size}")
                views.value = it
            }
        }
    }

    fun onQueryChanged(input: String) {
        userInput.value = input
    }

    fun onItemClicked(item: ObjectTypeView) {
        viewModelScope.launch {
            val objType = _objTypes.value.firstOrNull { it.id == item.id }
            if (objType == null) {
                Timber.e("Object Type Change Screen, type is not found in types list")
                sendToast("Error while choosing object type by key:${item.key}")
            } else {
                commands.emit(Command.DispatchType(objType))
            }
        }
    }

    private fun proceedWithBuildingViews(
        types: List<ObjectWrapper.Type>,
        excludeTypes: List<Id>
    ) = buildList {
        Timber.d("Types count: ${types.size}")

        val isWithCollection = when (vmParams.screen) {
            Screen.DATA_VIEW_SOURCE -> true
            Screen.OBJECT_TYPE_CHANGE,
            Screen.EMPTY_DATA_VIEW_SOURCE,
            Screen.DEFAULT_OBJECT_TYPE -> false
        }
        val isWithBookmark = when (vmParams.screen) {
            Screen.DATA_VIEW_SOURCE,
            Screen.EMPTY_DATA_VIEW_SOURCE -> true
            Screen.OBJECT_TYPE_CHANGE,
            Screen.DEFAULT_OBJECT_TYPE -> false
        }

        if (types.isNotEmpty()) {
            val views = types.getObjectTypeViewsForSBPage(
                isWithCollection = isWithCollection,
                isWithBookmark = isWithBookmark,
                excludeTypes = excludeTypes,
                selectedTypes = vmParams.selectedTypes,
                useCustomComparator = false
            ).map {
                ObjectTypeItemView.Type(it)
            }
            addAll(views)
        }
        if (isEmpty() && userInput.value.isNotEmpty()) {
            add(ObjectTypeItemView.EmptyState(userInput.value))
        }
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
