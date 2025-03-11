package com.anytypeio.anytype.feature_properties

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_ui.extensions.simpleIcon
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.primitives.SetObjectTypeRecommendedFields
import com.anytypeio.anytype.domain.relations.CreateRelation
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.feature_properties.add.UiEditTypePropertiesEvent
import com.anytypeio.anytype.feature_properties.add.EditTypePropertiesVmParams
import com.anytypeio.anytype.feature_properties.add.UiEditTypePropertiesErrorState
import com.anytypeio.anytype.feature_properties.add.UiEditTypePropertiesItem
import com.anytypeio.anytype.feature_properties.add.UiEditTypePropertiesState
import com.anytypeio.anytype.feature_properties.add.mapToStateItem
import com.anytypeio.anytype.feature_properties.edit.UiEditPropertyState
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

class EditTypePropertiesViewModel(
    private val vmParams: EditTypePropertiesVmParams,
    private val storeOfRelations: StoreOfRelations,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val stringResourceProvider: StringResourceProvider,
    private val createRelation: CreateRelation,
    private val setObjectDetails: SetObjectDetails,
    private val setObjectTypeRecommendedFields: SetObjectTypeRecommendedFields
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiEditTypePropertiesState.Companion.EMPTY)
    val uiState = _uiState.asStateFlow()

    private val _errorState =
        MutableStateFlow<UiEditTypePropertiesErrorState>(UiEditTypePropertiesErrorState.Hidden)
    val errorState = _errorState.asStateFlow()

    val uiPropertyEditState =
        MutableStateFlow<UiEditPropertyState>(UiEditPropertyState.Hidden)

    private val _commands = MutableSharedFlow<EditTypePropertiesCommand>()
    val commands = _commands.asSharedFlow()

    private val input = MutableStateFlow("")
    @OptIn(FlowPreview::class)
    private val query = input.take(1).onCompletion {
        emitAll(
            input.drop(1).debounce(DEBOUNCE_DURATION).distinctUntilChanged()
        )
    }

    //region Init
    init {
        setupAddNewPropertiesState()
    }

    private fun setupAddNewPropertiesState() {
        viewModelScope.launch {
            combine(
                storeOfObjectTypes.trackChanges(),
                storeOfRelations.trackChanges(),
                query
            ) { _, _, queryText ->
                val objType = storeOfObjectTypes.get(id = vmParams.objectTypeId)
                if (objType != null) {
                    val typeKeys =
                        objType.recommendedRelations + objType.recommendedFeaturedRelations + objType.recommendedFileRelations + objType.recommendedHiddenRelations
                    queryText to filterProperties(
                        allProperties = storeOfRelations.getAll(),
                        typeKeys = typeKeys,
                        queryText = queryText
                    )
                } else {
                    Timber.w("Object type:[${vmParams.objectTypeId}] not found in the store")
                    queryText to emptyList()
                }
            }.catch {
                Timber.e(it, "Error while filtering properties")
                _errorState.value = UiEditTypePropertiesErrorState.Show(
                    UiEditTypePropertiesErrorState.Reason.Other("Error while filtering properties")
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
    //endregion

    //region Ui State
    private fun setUiState(
        queryText: String,
        sortedExistingProperties: List<UiEditTypePropertiesItem>
    ) {
        val items = buildList {
            if (queryText.isNotEmpty()) {
                add(UiEditTypePropertiesItem.Create(title = queryText))
                val propertiesFormatItems = filterPropertiesFormats(queryText)
                if (propertiesFormatItems.isNotEmpty()) {
                    add(UiEditTypePropertiesItem.Section.Types())
                    addAll(propertiesFormatItems)
                }
                if (sortedExistingProperties.isNotEmpty()) {
                    add(UiEditTypePropertiesItem.Section.Existing())
                    addAll(sortedExistingProperties)
                }
            } else {
                val propertiesFormatItems = filterPropertiesFormats(queryText)
                if (propertiesFormatItems.isNotEmpty()) {
                    add(UiEditTypePropertiesItem.Section.Types())
                    addAll(propertiesFormatItems)
                }
                if (sortedExistingProperties.isNotEmpty()) {
                    add(UiEditTypePropertiesItem.Section.Existing())
                    addAll(sortedExistingProperties)
                }
            }
        }

        _uiState.value = UiEditTypePropertiesState(items = items)
    }

    private fun filterPropertiesFormats(query: String): List<UiEditTypePropertiesItem.Format> {
        return if (query.isNotEmpty()) {
            UiEditTypePropertiesState.Companion.PROPERTIES_FORMATS.map { format ->
                UiEditTypePropertiesItem.Format(
                    format = format,
                    prettyName = stringResourceProvider.getPropertiesFormatPrettyString(format)
                )
            }.filter { it.prettyName.contains(query, ignoreCase = true) }
        } else {
            UiEditTypePropertiesState.Companion.PROPERTIES_FORMATS.map { format ->
                UiEditTypePropertiesItem.Format(
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

    fun hideError() {
        _errorState.value = UiEditTypePropertiesErrorState.Hidden
    }
    //endregion

    //region Ui Events
    fun onEvent(event: UiEditTypePropertiesEvent) {
        when (event) {
            is UiEditTypePropertiesEvent.OnCreate -> {
                val format = event.item.format
                uiPropertyEditState.value = UiEditPropertyState.Visible.New(
                    name = event.item.title,
                    formatName = stringResourceProvider.getPropertiesFormatPrettyString(format),
                    formatIcon = format.simpleIcon(),
                    format = format,
                )
            }

            is UiEditTypePropertiesEvent.OnExistingClicked -> {
                viewModelScope.launch {
                    val objType = storeOfObjectTypes.get(vmParams.objectTypeId)
                    if (objType != null) {
                        proceedWithSetRecommendedProperties(
                            properties = objType.recommendedRelations + event.item.id
                        )
                    }
                }
            }

            is UiEditTypePropertiesEvent.OnSearchQueryChanged -> {
                input.value = event.query
            }

            is UiEditTypePropertiesEvent.OnTypeClicked -> {
                val format = event.item.format
                uiPropertyEditState.value = UiEditPropertyState.Visible.New(
                    name = "",
                    formatName = stringResourceProvider.getPropertiesFormatPrettyString(format),
                    formatIcon = format.simpleIcon(),
                    format = format,
                )
            }

            UiEditTypePropertiesEvent.OnEditPropertyScreenDismissed -> {
                uiPropertyEditState.value = UiEditPropertyState.Hidden
            }

            UiEditTypePropertiesEvent.OnCreateNewButtonClicked -> {
                proceedWithCreatingRelation()
            }

            UiEditTypePropertiesEvent.OnSaveButtonClicked -> {
                proceedWithUpdatingRelation()
            }

            is UiEditTypePropertiesEvent.OnPropertyNameUpdate -> {
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

    //region Use Cases
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
                    _errorState.value = UiEditTypePropertiesErrorState.Show(
                        UiEditTypePropertiesErrorState.Reason.ErrorUpdatingProperty(error.message ?: "")
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
                prefilled = emptyMap()
            )
            createRelation(params).process(
                success = { relation ->
                    Timber.d("Relation created: $relation")
                    val objType = storeOfObjectTypes.get(vmParams.objectTypeId)
                    if (objType != null) {
                        proceedWithSetRecommendedProperties(
                            properties = objType.recommendedRelations + listOf(relation.id)
                        )
                    }
                    uiPropertyEditState.value = UiEditPropertyState.Hidden
                    _commands.emit(EditTypePropertiesCommand.Exit)
                },
                failure = { error ->
                    Timber.e(error, "Failed to create relation")
                    _errorState.value = UiEditTypePropertiesErrorState.Show(
                        UiEditTypePropertiesErrorState.Reason.ErrorCreatingProperty(error.message ?: "")
                    )
                }
            )
        }
    }

    private fun proceedWithSetRecommendedProperties(properties: List<Id>) {
        val params = SetObjectTypeRecommendedFields.Params(
            objectTypeId = vmParams.objectTypeId,
            fields = properties
        )
        viewModelScope.launch {
            setObjectTypeRecommendedFields.async(params).fold(
                onSuccess = {
                    Timber.d("Recommended fields set")
                    _commands.emit(EditTypePropertiesCommand.Exit)
                },
                onFailure = { error ->
                    Timber.e(error, "Error while setting recommended fields")
                    _errorState.value = UiEditTypePropertiesErrorState.Show(
                        UiEditTypePropertiesErrorState.Reason.ErrorAddingProperty(error.message ?: "")
                    )
                }
            )
        }
    }
    //endregion

    //region Commands
    sealed class EditTypePropertiesCommand {
        data object Exit : EditTypePropertiesCommand()
    }
    //endregion

    companion object {
        private const val DEBOUNCE_DURATION = 300L
    }
}

