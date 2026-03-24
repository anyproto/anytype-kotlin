package com.anytypeio.anytype.feature_create_object.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SupportedLayouts
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_models.ui.objectIcon
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.page.CreateObjectByTypeAndTemplate
import com.anytypeio.anytype.presentation.objects.sortByTypePriority
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel for managing the create object screen state.
 * Handles fetching object types from the store and filtering them based on search query.
 * Types are sorted according to user's custom widget order (orderId), with fallback to
 * space-specific default order, then alphabetical.
 *
 * @param storeOfObjectTypes Store containing all available object types
 * @param spaceViewContainer Container for observing space view properties
 * @param vmParams Parameters including the current space ID
 * @param createObjectByTypeAndTemplate Use case for creating objects
 */
class NewCreateObjectViewModel @Inject constructor(
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val spaceViewContainer: SpaceViewSubscriptionContainer,
    private val vmParams: VmParams,
    private val createObjectByTypeAndTemplate: CreateObjectByTypeAndTemplate
) : ViewModel() {

    private val _state = MutableStateFlow(NewCreateObjectState())
    val state: StateFlow<NewCreateObjectState> = _state.asStateFlow()

    private val _navigation = Channel<CreateObjectNavigation>(Channel.BUFFERED)
    val navigation = _navigation.receiveAsFlow()

    init {
        observeObjectTypes()
        vmParams.typeKey?.let { key ->
            onCreateObject(typeKey = key, typeName = "")
        }
    }

    /**
     * Observes object types and space type, combining them to produce a sorted list.
     * The sort order respects user's custom widget ordering via orderId field.
     */
    private fun observeObjectTypes() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                combine(
                    storeOfObjectTypes.observe(),
                    spaceViewContainer.observe(
                        space = vmParams.spaceId,
                        keys = listOf(Relations.SPACE_UX_TYPE),
                        mapper = { it.spaceUxType }
                    )
                ) { allTypes, spaceUxType ->
                    // Determine if this is a chat space for sorting priority
                    val isChatSpace = spaceUxType == SpaceUxType.CHAT ||
                                      spaceUxType == SpaceUxType.ONE_TO_ONE

                    // Get excluded layouts based on space type
                    val systemLayouts = SupportedLayouts.getSystemLayouts(spaceUxType)
                    val excludedLayouts = systemLayouts + SupportedLayouts.dateLayouts + listOf(
                        ObjectType.Layout.OBJECT_TYPE,
                        ObjectType.Layout.PARTICIPANT
                    )

                    // Filter valid types
                    val filteredTypes = allTypes.filter { type ->
                        type.isValid &&
                        type.isDeleted != true &&
                        type.isArchived != true &&
                        type.uniqueKey != ObjectTypeIds.TEMPLATE &&
                        !excludedLayouts.contains(type.recommendedLayout)
                    }

                    // Sort using user's custom widget order, then map to UI model
                    filteredTypes
                        .sortByTypePriority(isChatSpace)
                        .map { type ->
                            ObjectTypeItem(
                                typeKey = type.uniqueKey,
                                name = type.name.orEmpty(),
                                icon = type.objectIcon()
                            )
                        }
                }.collect { types ->
                    _state.update {
                        it.copy(
                            objectTypes = types,
                            filteredObjectTypes = applySearchFilter(types, it.searchQuery),
                            isLoading = false
                        )
                    }
                    Timber.d("Loaded ${types.size} object types with custom sort order")
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load object types")
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error"
                    )
                }
            }
        }
    }

    /**
     * Clears the error state and retries loading object types.
     */
    private fun retry() {
        _state.update { it.copy(error = null) }
        observeObjectTypes()
    }

    /**
     * Applies the search filter to the provided list of types.
     */
    private fun applySearchFilter(
        types: List<ObjectTypeItem>,
        query: String
    ): List<ObjectTypeItem> {
        return if (query.isBlank()) {
            types
        } else {
            types.filter { it.name.contains(query, ignoreCase = true) }
        }
    }

    /**
     * Updates the search query and filters the object types list.
     * Filtering is case-insensitive and matches against the type name.
     *
     * @param query The search query entered by the user
     */
    fun onSearchQueryChanged(query: String) {
        _state.update { currentState ->
            currentState.copy(
                searchQuery = query,
                filteredObjectTypes = applySearchFilter(currentState.objectTypes, query)
            )
        }
    }

    /**
     * Handles actions from the UI.
     *
     * @param action The action to process
     */
    fun onAction(action: CreateObjectAction) {
        when (action) {
            is CreateObjectAction.UpdateSearch -> onSearchQueryChanged(action.query)
            is CreateObjectAction.Retry -> retry()
            is CreateObjectAction.CreateObjectOfType -> onCreateObject(
                typeKey = TypeKey(action.typeKey),
                typeName = action.typeName
            )
            else -> { /* Media actions handled by parent component */ }
        }
    }

    private fun onCreateObject(typeKey: TypeKey, typeName: String) {
        viewModelScope.launch {
            val resultat = createObjectByTypeAndTemplate.async(
                CreateObjectByTypeAndTemplate.Param(
                    typeKey = typeKey,
                    space = vmParams.spaceId,
                    keys = emptyList()
                )
            )
            when (resultat) {
                is Resultat.Success -> {
                    when (val result = resultat.value) {
                        is CreateObjectByTypeAndTemplate.Result.Success -> {
                            val layout = result.obj.layout
                            val nav = when (layout) {
                                ObjectType.Layout.COLLECTION,
                                ObjectType.Layout.SET -> CreateObjectNavigation.OpenSet(
                                    id = result.objectId,
                                    space = vmParams.spaceId
                                )
                                ObjectType.Layout.CHAT,
                                ObjectType.Layout.CHAT_DERIVED -> CreateObjectNavigation.OpenChat(
                                    id = result.objectId,
                                    space = vmParams.spaceId
                                )
                                else -> CreateObjectNavigation.OpenEditor(
                                    id = result.objectId,
                                    space = vmParams.spaceId
                                )
                            }
                            _navigation.send(nav)
                        }
                        is CreateObjectByTypeAndTemplate.Result.ObjectTypeNotFound -> {
                            _state.update { it.copy(error = "Object type not found") }
                        }
                    }
                }
                is Resultat.Failure -> {
                    _state.update {
                        it.copy(error = resultat.exception.message ?: "Failed to create object")
                    }
                }
                is Resultat.Loading -> {
                    // no-op: loading state not expected from async()
                }
            }
        }
    }

    /**
     * Parameters for the ViewModel.
     * @param spaceId The current space ID, used to determine space type for sorting
     * @param typeKey Optional pre-selected type key; if provided, object creation is triggered immediately
     */
    data class VmParams(
        val spaceId: SpaceId,
        val typeKey: TypeKey? = null
    )
}
