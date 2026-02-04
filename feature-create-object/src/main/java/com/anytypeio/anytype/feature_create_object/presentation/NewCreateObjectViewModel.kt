package com.anytypeio.anytype.feature_create_object.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SupportedLayouts
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.ui.objectIcon
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.presentation.objects.sortByTypePriority
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
 */
class NewCreateObjectViewModel @Inject constructor(
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val spaceViewContainer: SpaceViewSubscriptionContainer,
    private val vmParams: VmParams
) : ViewModel() {

    private val _state = MutableStateFlow(NewCreateObjectState())
    val state: StateFlow<NewCreateObjectState> = _state.asStateFlow()

    init {
        observeObjectTypes()
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
     * Only processes search actions internally; other actions are handled by parent components.
     *
     * @param action The action to process
     */
    fun onAction(action: CreateObjectAction) {
        when (action) {
            is CreateObjectAction.UpdateSearch -> onSearchQueryChanged(action.query)
            is CreateObjectAction.Retry -> retry()
            // Other actions (media, create object, attach) are handled by the parent component
            else -> { /* No-op */
            }
        }
    }

    /**
     * Parameters for the ViewModel.
     * @param spaceId The current space ID, used to determine space type for sorting
     */
    data class VmParams(
        val spaceId: SpaceId
    )
}
