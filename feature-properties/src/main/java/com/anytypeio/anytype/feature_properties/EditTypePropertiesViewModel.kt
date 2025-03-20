package com.anytypeio.anytype.feature_properties

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_ui.extensions.simpleIcon
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.misc.UrlBuilder
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
import com.anytypeio.anytype.feature_properties.add.UiEditTypePropertiesItem.*
import com.anytypeio.anytype.feature_properties.add.UiEditTypePropertiesState
import com.anytypeio.anytype.feature_properties.add.mapToStateItem
import com.anytypeio.anytype.feature_properties.edit.UiEditPropertyState
import com.anytypeio.anytype.feature_properties.edit.UiEditPropertyState.Visible.*
import com.anytypeio.anytype.feature_properties.edit.UiPropertyFormatsListState
import com.anytypeio.anytype.feature_properties.edit.UiPropertyFormatsListState.*
import com.anytypeio.anytype.feature_properties.edit.UiPropertyLimitTypeItem
import com.anytypeio.anytype.presentation.mapper.objectIcon
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
    private val setObjectTypeRecommendedFields: SetObjectTypeRecommendedFields,
    private val urlBuilder: UrlBuilder
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiEditTypePropertiesState.Companion.EMPTY)
    val uiState = _uiState.asStateFlow()

    private val _errorState =
        MutableStateFlow<UiEditTypePropertiesErrorState>(UiEditTypePropertiesErrorState.Hidden)
    val errorState = _errorState.asStateFlow()

    val uiPropertyEditState =
        MutableStateFlow<UiEditPropertyState>(UiEditPropertyState.Hidden)

    val uiPropertyFormatsListState =
        MutableStateFlow<UiPropertyFormatsListState>(UiPropertyFormatsListState.Hidden)

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
                    val filteredAllSpaceProperties = filterSpacePropertiesByTypeIds(
                        allSpaceProperties = storeOfRelations.getAll(),
                        objTypeIds = objType.allRecommendedRelations,
                        queryText = queryText
                    )
                    queryText to filteredAllSpaceProperties
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
                .collect { (queryText, filteredAllSpaceProperties) ->

                    val sortedExistingItems = filteredAllSpaceProperties.mapNotNull { property ->
                        property.mapToStateItem(
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

    private fun filterSpacePropertiesByTypeIds(
        allSpaceProperties: List<ObjectWrapper.Relation>,
        objTypeIds: List<Id>,
        queryText: String
    ): List<ObjectWrapper.Relation> = allSpaceProperties.filter { property ->
        property.id !in objTypeIds &&
                property.isValidToUse &&
                (queryText.isBlank() || property.name?.contains(queryText, ignoreCase = true) == true)
    }

    fun hideError() {
        _errorState.value = UiEditTypePropertiesErrorState.Hidden
    }
    //endregion

    //region Ui Events
    fun onEvent(event: UiEditTypePropertiesEvent) {
        Timber.d("UiEditTypePropertiesEvent: $event")
        when (event) {
            is UiEditTypePropertiesEvent.OnCreate -> {
                val format = event.item.format
                viewModelScope.launch {
                    uiPropertyEditState.value = New(
                        name = event.item.title,
                        formatName = stringResourceProvider.getPropertiesFormatPrettyString(format),
                        formatIcon = format.simpleIcon(),
                        format = format,
                        showLimitTypes = false,
                        limitObjectTypes = getAllObjectTypesByFormat(format)
                    )
                }
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
                viewModelScope.launch {
                    val format = event.item.format
                    uiPropertyEditState.value = New(
                        name = "",
                        formatName = stringResourceProvider.getPropertiesFormatPrettyString(format),
                        formatIcon = format.simpleIcon(),
                        format = format,
                        showLimitTypes = false,
                        limitObjectTypes = getAllObjectTypesByFormat(format)
                    )
                }
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
                    is Edit -> state.copy(name = event.name)
                    is New -> state.copy(name = event.name)
                    is View -> state
                }
            }

            UiEditTypePropertiesEvent.OnPropertyFormatClick -> {
                uiPropertyFormatsListState.value = Visible(
                    items = UiEditTypePropertiesState.Companion.PROPERTIES_FORMATS.map { format ->
                        Format(
                            format = format,
                            prettyName = stringResourceProvider.getPropertiesFormatPrettyString(format)
                        )
                    }
                )
            }

            UiEditTypePropertiesEvent.OnPropertyFormatsListDismiss -> {
                uiPropertyFormatsListState.value = Hidden
            }

            is UiEditTypePropertiesEvent.OnPropertyFormatSelected -> {
                uiPropertyFormatsListState.value = Hidden
                val state = uiPropertyEditState.value as? UiEditPropertyState.Visible ?: return
                uiPropertyEditState.value = when (state) {
                    is New -> {
                        val newFormat = event.format.format
                        state.copy(
                            formatName = stringResourceProvider.getPropertiesFormatPrettyString(
                                newFormat
                            ),
                            formatIcon = newFormat.simpleIcon(),
                            format = newFormat,
                        )
                    }

                    else -> state
                }
            }

            UiEditTypePropertiesEvent.OnLimitTypesClick -> {
                uiPropertyEditState.value = when (val state = uiPropertyEditState.value) {
                    is New -> state.copy(showLimitTypes = true)
                    else -> state
                }
            }

            UiEditTypePropertiesEvent.OnLimitTypesDismiss -> {
                uiPropertyEditState.value = when (val state = uiPropertyEditState.value) {
                    is New -> state.copy(showLimitTypes = false)
                    else -> state
                }
            }

            is UiEditTypePropertiesEvent.OnLimitTypesDoneClick -> {
                val state = uiPropertyEditState.value as? UiEditPropertyState.Visible.New ?: return
                uiPropertyEditState.value = state.copy(
                    selectedLimitTypeIds = event.items,
                    showLimitTypes = false
                )
            }
        }
    }
    //endregion

    //region Use Cases
    private fun proceedWithUpdatingRelation() {
        val state = uiPropertyEditState.value as? Edit ?: return
        viewModelScope.launch {
            val params = SetObjectDetails.Params(
                ctx = state.id,
                details = mapOf(
                    Relations.NAME to state.name
                )
            )
            setObjectDetails.async(params).fold(
                onSuccess = { payload ->
                    Timber.d("Property updated :[$payload]")
                },
                onFailure = { error ->
                    Timber.e(error, "Failed to update property")
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
                success = { property ->
                    Timber.d("Property created: $property")
                    val objType = storeOfObjectTypes.get(vmParams.objectTypeId)
                    if (objType != null) {
                        proceedWithSetRecommendedProperties(
                            properties = objType.recommendedRelations + listOf(property.id)
                        )
                    }
                    uiPropertyEditState.value = UiEditPropertyState.Hidden
                },
                failure = { error ->
                    Timber.e(error, "Failed to create property")
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

    //region Limit Object Types

    private suspend fun getAllObjectTypesByFormat(format: RelationFormat): List<UiPropertyLimitTypeItem> {
        if (format != RelationFormat.OBJECT) return emptyList()
        return storeOfObjectTypes.getAll().map {
            UiPropertyLimitTypeItem(
                id = it.id,
                name = it.name.orEmpty(),
                icon = it.objectIcon(urlBuilder),
                uniqueKey = it.uniqueKey
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

