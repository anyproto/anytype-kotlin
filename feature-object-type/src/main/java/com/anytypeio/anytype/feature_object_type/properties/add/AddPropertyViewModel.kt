package com.anytypeio.anytype.feature_object_type.properties.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_ui.extensions.simpleIcon
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.primitives.SetObjectTypeRecommendedFields
import com.anytypeio.anytype.domain.relations.CreateRelation
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.feature_object_type.properties.add.AddPropertyViewModel.AddPropertyCommand.*
import com.anytypeio.anytype.feature_object_type.properties.edit.UiEditPropertyState
import com.anytypeio.anytype.feature_object_type.viewmodel.ObjectTypeStore
import com.anytypeio.anytype.presentation.editor.cover.UnsplashViewModel.Companion.DEBOUNCE_DURATION
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import timber.log.Timber

class AddPropertyViewModel(
    private val vmParams: AddPropertyVmParams,
    private val provider: TypePropertiesProvider,
    private val storeOfRelations: StoreOfRelations,
    private val stringResourceProvider: StringResourceProvider,
    private val createRelation: CreateRelation,
    private val setObjectDetails: SetObjectDetails,
    private val objectTypesStore: ObjectTypeStore,
    private val setObjectTypeRecommendedFields: SetObjectTypeRecommendedFields
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiAddPropertyScreenState.EMPTY)
    val uiState = _uiState.asStateFlow()

    private val _errorState =
        MutableStateFlow<UiAddPropertyErrorState>(UiAddPropertyErrorState.Hidden)
    val errorState = _errorState.asStateFlow()

    val uiPropertyEditState =
        MutableStateFlow<UiEditPropertyState>(UiEditPropertyState.Hidden)

    private val _commands = MutableSharedFlow<AddPropertyCommand>()
    val commands = _commands.asSharedFlow()

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
                provider.observeKeys(),
                query,
                storeOfRelations.trackChanges()
            ) { typeKeys, queryText, _ ->
                queryText to filterProperties(
                    allProperties = storeOfRelations.getAll(),
                    typeKeys = typeKeys,
                    queryText = queryText
                )
            }.catch {
                Timber.e(it, "Error while filtering properties")
                _errorState.value = UiAddPropertyErrorState.Show(
                    UiAddPropertyErrorState.Reason.Other("Error while filtering properties")
                )
            }
                .collect { (queryText, filteredProperties) ->

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

    fun hideError() {
        _errorState.value = UiAddPropertyErrorState.Hidden
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
            is AddPropertyEvent.OnCreate -> {
                val format = event.item.format
                uiPropertyEditState.value = UiEditPropertyState.Visible.New(
                    name = event.item.title,
                    formatName = stringResourceProvider.getPropertiesFormatPrettyString(format),
                    formatIcon = format.simpleIcon(),
                    format = format,
                )
            }

            is AddPropertyEvent.OnExistingClicked -> {
                proceedWithSetRecommendedFields(
                    fields = objectTypesStore.recommendedPropertiesFlow.value + event.item.id
                )
            }

            is AddPropertyEvent.OnSearchQueryChanged -> {
                onQueryChanged(event.query)
            }

            is AddPropertyEvent.OnTypeClicked -> {
                val format = event.item.format
                uiPropertyEditState.value = UiEditPropertyState.Visible.New(
                    name = "",
                    formatName = stringResourceProvider.getPropertiesFormatPrettyString(format),
                    formatIcon = format.simpleIcon(),
                    format = format,
                )
            }

            AddPropertyEvent.OnEditPropertyScreenDismissed -> {
                uiPropertyEditState.value = UiEditPropertyState.Hidden
            }

            AddPropertyEvent.OnCreateNewButtonClicked -> {
                proceedWithCreatingRelation()
            }

            AddPropertyEvent.OnSaveButtonClicked -> {
                proceedWithUpdatingRelation()
            }

            is AddPropertyEvent.OnPropertyNameUpdate -> {
                val state = uiPropertyEditState.value as? UiEditPropertyState.Visible ?: return
                uiPropertyEditState.value = when (state) {
                    is UiEditPropertyState.Visible.Edit -> state.copy(name = event.name)
                    is UiEditPropertyState.Visible.New -> state.copy(name = event.name)
                    is UiEditPropertyState.Visible.View -> state
                }
            }
        }
    }
    //endregion

    //region USE Case
    private fun proceedWithUpdatingRelation() {
        val state = uiPropertyEditState.value as? UiEditPropertyState.Visible.Edit ?: return
        viewModelScope.launch {
            val params = SetObjectDetails.Params(
                ctx = state.id,
                details = mapOf(
                    Relations.NAME to state.name,
                    Relations.RELATION_FORMAT to state.format
                )
            )
            setObjectDetails.async(params).fold(
                onSuccess = {
                    Timber.d("Relation updated: $it")
                },
                onFailure = { error ->
                    Timber.e(error, "Failed to update relation")
                    _errorState.value = UiAddPropertyErrorState.Show(
                        UiAddPropertyErrorState.Reason.ErrorUpdatingProperty(error.message ?: "")
                    )
                }
            )
        }
    }

    private fun proceedWithCreatingRelation() {
        viewModelScope.launch {
            val state = uiPropertyEditState.value as? UiEditPropertyState.Visible ?: return@launch
            val (name, format) = when (state) {
                is UiEditPropertyState.Visible.Edit -> state.name to state.format
                is UiEditPropertyState.Visible.View -> state.name to state.format
                is UiEditPropertyState.Visible.New -> state.name to state.format
            }
            val params = CreateRelation.Params(
                space = vmParams.spaceId.id,
                format = format,
                name = name,
                limitObjectTypes = emptyList(),
                //todo добавить limit object types!
                //state.limitObjectTypes.map { it.id },
                prefilled = emptyMap()
            )
            createRelation(params).process(
                success = { relation ->
                    Timber.d("Relation created: $relation")
                    proceedWithSetRecommendedFields(
                        fields = objectTypesStore.recommendedPropertiesFlow.value + relation.id
                    )
                    uiPropertyEditState.value = UiEditPropertyState.Hidden
                    _commands.emit(Exit)
                },
                failure = { error ->
                    Timber.e(error, "Failed to create relation")
                    _errorState.value = UiAddPropertyErrorState.Show(
                        UiAddPropertyErrorState.Reason.ErrorCreatingProperty(error.message ?: "")
                    )
                }
            )
        }
    }

    private fun proceedWithSetRecommendedFields(fields: List<Id>) {
        val params = SetObjectTypeRecommendedFields.Params(
            objectTypeId = vmParams.objectTypeId,
            fields = fields
        )
        viewModelScope.launch {
            setObjectTypeRecommendedFields.async(params).fold(
                onSuccess = {
                    Timber.d("Recommended fields set")
                    _commands.emit(Exit)
                },
                onFailure = { error ->
                    Timber.e(error, "Error while setting recommended fields")
                    _errorState.value = UiAddPropertyErrorState.Show(
                        UiAddPropertyErrorState.Reason.ErrorAddingProperty(error.message ?: "")
                    )
                }
            )
        }
    }
    //endregion

    sealed class AddPropertyCommand {
        data object Exit : AddPropertyCommand()
    }
}

