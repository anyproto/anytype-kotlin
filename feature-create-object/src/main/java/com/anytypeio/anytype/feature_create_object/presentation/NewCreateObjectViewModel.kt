package com.anytypeio.anytype.feature_create_object.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.ui.objectIcon
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel for managing the create object screen state.
 * Handles fetching object types from the store and filtering them based on search query.
 *
 * @param storeOfObjectTypes Store containing all available object types
 */
class NewCreateObjectViewModel @Inject constructor(
    private val storeOfObjectTypes: StoreOfObjectTypes
) : ViewModel() {

    private val _state = MutableStateFlow(NewCreateObjectState())
    val state: StateFlow<NewCreateObjectState> = _state.asStateFlow()

    init {
        loadObjectTypes()
    }

    /**
     * Loads all object types from the store.
     * Filters out invalid, deleted, or archived types and sorts them alphabetically.
     */
    private fun loadObjectTypes() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                val types = storeOfObjectTypes.getAll()
                    .filter { type ->
                        // Only show valid, non-deleted, non-archived types
                        type.isValid && type.isDeleted != true && type.isArchived != true
                    }
                    .map { type ->
                        ObjectTypeItem(
                            typeKey = type.uniqueKey,
                            name = type.name.orEmpty(),
                            icon = type.objectIcon()
                        )
                    }
                    .sortedBy { it.name }

                _state.update {
                    it.copy(
                        objectTypes = types,
                        filteredObjectTypes = types,
                        isLoading = false
                    )
                }

                Timber.d("Loaded ${types.size} object types")
            } catch (e: Exception) {
                Timber.e(e, "Failed to load object types")
                _state.update { it.copy(isLoading = false) }
            }
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
            val filtered = if (query.isBlank()) {
                currentState.objectTypes
            } else {
                currentState.objectTypes.filter { type ->
                    type.name.contains(query, ignoreCase = true)
                }
            }

            currentState.copy(
                searchQuery = query,
                filteredObjectTypes = filtered
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
            // Other actions (media, create object, attach) are handled by the parent component
            else -> { /* No-op */
            }
        }
    }
}
