package com.anytypeio.anytype.feature_object_type.properties.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.presentation.editor.cover.UnsplashViewModel.Companion.DEBOUNCE_DURATION
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

class AddPropertyViewModel(
    private val addPropertyVmParams: AddPropertyVmParams,
    private val typePropertiesProvider: TypePropertiesProvider,
    private val storeOfRelations: StoreOfRelations,
    private val stringResourceProvider: StringResourceProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiAddPropertyScreenState.EMPTY)
    val uiState = _uiState.asStateFlow()

    private val input = MutableStateFlow("")

    @OptIn(FlowPreview::class)
    private val query = input.take(1).onCompletion {
        emitAll(
            input.drop(1).debounce(DEBOUNCE_DURATION).distinctUntilChanged()
        )
    }

    init {
        setupAddNewPropertiesState()
    }

    private fun onQueryChanged(query: String) {
        input.value = query
    }

    /**
     * Loads the available properties from type, applies filtering based on a search query,
     * and then updates the UI state.
     */
    private fun setupAddNewPropertiesState() {
        viewModelScope.launch {
            combine(
                typePropertiesProvider.observeKeys(),
                query,
                storeOfRelations.trackChanges()
            ) { typeKeys, queryText, _ ->
                queryText to filterProperties(
                    allProperties = storeOfRelations.getAll(),
                    typeKeys = typeKeys,
                    queryText = queryText
                )
            }.collect { (queryText, filteredProperties) ->

                val sortedExistingItems = filteredProperties.mapNotNull { field ->
                    field.mapToStateItem(
                        stringResourceProvider = stringResourceProvider
                    )
                }.sortedBy { it.title }

                setUiState(
                    queryText = queryText,
                    sortedExistingProperties = sortedExistingItems
                )
            }
        }
    }

    private fun setUiState(
        queryText: String,
        sortedExistingProperties: List<UiAddPropertyItem>
    ) {
        val items = buildList {
            if (queryText.isNotEmpty()) {
                add(UiAddPropertyItem.Create(title = queryText))
                val propertiesFormatItems = filterPropertiesFormats(queryText)
                if (propertiesFormatItems.isNotEmpty()) {
                    add(UiAddPropertyItem.Section.Types())
                    addAll(propertiesFormatItems)
                }
                if (sortedExistingProperties.isNotEmpty()) {
                    add(UiAddPropertyItem.Section.Existing())
                    addAll(sortedExistingProperties)
                }
            } else {
                val propertiesFormatItems = filterPropertiesFormats(queryText)
                if (propertiesFormatItems.isNotEmpty()) {
                    add(UiAddPropertyItem.Section.Types())
                    addAll(propertiesFormatItems)
                }
                if (sortedExistingProperties.isNotEmpty()) {
                    add(UiAddPropertyItem.Section.Existing())
                    addAll(sortedExistingProperties)
                }
            }
        }

        _uiState.value = UiAddPropertyScreenState(items = items)
    }

    private fun filterPropertiesFormats(query: String): List<UiAddPropertyItem.Format> {
        return if (query.isNotEmpty()) {
            UiAddPropertyScreenState.PROPERTIES_FORMATS.map { format ->
                UiAddPropertyItem.Format(
                    format = format,
                    prettyName = stringResourceProvider.getPropertiesFormatPrettyString(format)
                )
            }.filter { it.prettyName.contains(query, ignoreCase = true) }
        } else {
            UiAddPropertyScreenState.PROPERTIES_FORMATS.map { format ->
                UiAddPropertyItem.Format(
                    format = format,
                    prettyName = stringResourceProvider.getPropertiesFormatPrettyString(format)
                )
            }
        }
    }

    private fun filterProperties(
        allProperties: List<ObjectWrapper.Relation>,
        typeKeys: List<Key>,
        queryText: String
    ): List<ObjectWrapper.Relation> = allProperties.filter { field ->
        field.key !in typeKeys &&
                field.isValidToUse &&
                (queryText.isBlank() || field.name?.contains(queryText, ignoreCase = true) == true)
    }

    //region UI Events
    fun onEvent(event: AddPropertyEvent) {
        when (event) {
            is AddPropertyEvent.OnCreate -> TODO()
            is AddPropertyEvent.OnExistingClicked -> TODO()
            is AddPropertyEvent.OnSearchQueryChanged -> {
                onQueryChanged(event.query)
            }

            is AddPropertyEvent.OnTypeClicked -> TODO()
        }
    }
    //endregion
}

