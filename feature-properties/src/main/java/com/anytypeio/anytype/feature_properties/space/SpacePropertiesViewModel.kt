package com.anytypeio.anytype.feature_properties.space

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.primitives.RelationKey
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.core_ui.extensions.simpleIcon
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.objects.mapLimitObjectTypes
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.relations.CreateRelation
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.feature_properties.add.UiEditTypePropertiesItem.Format
import com.anytypeio.anytype.feature_properties.add.UiEditTypePropertiesState
import com.anytypeio.anytype.feature_properties.edit.UiEditPropertyState
import com.anytypeio.anytype.feature_properties.edit.UiEditPropertyState.Visible.New
import com.anytypeio.anytype.feature_properties.edit.UiEditPropertyState.Visible.View
import com.anytypeio.anytype.feature_properties.edit.UiEditPropertyState.Visible.Edit
import com.anytypeio.anytype.feature_properties.edit.UiPropertyFormatsListState
import com.anytypeio.anytype.feature_properties.edit.UiPropertyFormatsListState.Hidden
import com.anytypeio.anytype.feature_properties.edit.UiPropertyFormatsListState.Visible
import com.anytypeio.anytype.feature_properties.edit.UiPropertyLimitTypeItem
import com.anytypeio.anytype.feature_properties.getAllObjectTypesByFormat
import com.anytypeio.anytype.feature_properties.space.SpacePropertiesViewModel.Command.*
import com.anytypeio.anytype.feature_properties.space.ui.SpacePropertiesEvent
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.core_models.ui.objectIcon
import javax.inject.Inject
import kotlin.collections.sortedBy
import kotlin.text.orEmpty
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

class SpacePropertiesViewModel(
    private val vmParams: VmParams,
    private val analytics: Analytics,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val fieldParser: FieldParser,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val userPermissionProvider: UserPermissionProvider,
    private val storeOfRelations: StoreOfRelations,
    private val stringResourceProvider: StringResourceProvider,
    private val setObjectDetails: SetObjectDetails,
    private val createRelation: CreateRelation,
    private val setObjectListIsArchived: SetObjectListIsArchived
) : ViewModel(), AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate {

    // Main UI states
    val uiItemsState =
        MutableStateFlow<UiSpacePropertiesScreenState>(UiSpacePropertiesScreenState.Empty)
    val uiEditPropertyScreen = MutableStateFlow<UiEditPropertyState>(UiEditPropertyState.Hidden)
    val uiPropertyFormatsListState =
        MutableStateFlow<UiPropertyFormatsListState>(Hidden)
    val commands = MutableSharedFlow<Command>()

    private val permission = MutableStateFlow(userPermissionProvider.get(vmParams.spaceId))

    init {
        Timber.d("Space Properties ViewModel init")
        setupUIState()
    }

    private fun setupUIState() {
        viewModelScope.launch {
            storeOfRelations.trackChanges()
                .collectLatest { event ->
                    val allProperties = storeOfRelations.getAll().mapNotNull { property ->
                        if (property.isHidden == true || property.isDeleted == true || property.isArchived == true) {
                            null
                        } else {
                            property
                        }
                    }
                    val (myProperties, systemProperties) =
                        allProperties.partition { !it.restrictions.contains(ObjectRestriction.DELETE) }
                    val list = buildList {
                        add(UiSpacePropertyItem.Section.MyProperties())
                        addAll(myProperties.map { property ->
                            UiSpacePropertyItem.Item(
                                id = property.id,
                                key = RelationKey(property.key),
                                name = property.name.orEmpty(),
                                format = property.format,
                                isEditableField = fieldParser.isPropertyEditable(property),
                                limitObjectTypes = storeOfObjectTypes.mapLimitObjectTypes(property = property),
                                isPossibleMoveToBin = true
                            )
                        }.sortedBy { it.name })
                        add(UiSpacePropertyItem.Section.SystemProperties())
                        addAll(systemProperties.map { property ->
                            UiSpacePropertyItem.Item(
                                id = property.id,
                                key = RelationKey(property.key),
                                name = property.name.orEmpty(),
                                format = property.format,
                                isEditableField = fieldParser.isPropertyEditable(property),
                                limitObjectTypes = storeOfObjectTypes.mapLimitObjectTypes(property = property),
                                isPossibleMoveToBin = false
                            )
                        }.sortedBy { it.name })
                    }

                    uiItemsState.value = UiSpacePropertiesScreenState(list)
                }
        }
    }

    fun onMoveToBinProperty(item: UiSpacePropertyItem.Item) {
        val propertyId = item.id
        viewModelScope.launch {
            val params = SetObjectListIsArchived.Params(
                targets = listOf(propertyId),
                isArchived = true
            )
            setObjectListIsArchived.async(params).fold(
                onSuccess = {
                    Timber.d("Property $propertyId moved to bin")
                },
                onFailure = {
                    Timber.e(it, "Error while moving property $propertyId to bin")
                }
            )
        }
    }

    fun onBackClicked() {
        viewModelScope.launch {
            commands.emit(Command.Back)
        }
    }

    fun onCreateNewPropertyClicked() {
        if (permission.value?.isOwnerOrEditor() == true) {
            createNewProperty()
        } else {
            viewModelScope.launch {
                commands.emit(ShowToast("You don't have permission to create new properties"))
            }
        }
    }

    private fun createNewProperty() {
        viewModelScope.launch {
            val format = DEFAULT_NEW_PROPERTY_FORMAT
            uiEditPropertyScreen.value = New(
                name = "",
                formatName = stringResourceProvider.getPropertiesFormatPrettyString(format),
                formatIcon = format.simpleIcon(),
                format = format,
                showLimitTypes = false,
                limitObjectTypes = format.getAllObjectTypesByFormat(storeOfObjectTypes)
            )
        }
    }

    fun onPropertyClicked(item: UiSpacePropertyItem.Item) {
        viewModelScope.launch {
            val computedLimitTypes = computeLimitTypes(item = item)
            val formatName = stringResourceProvider.getPropertiesFormatPrettyString(item.format)
            val formatIcon = item.format.simpleIcon()
            uiEditPropertyScreen.value =
                if (permission.value?.isOwnerOrEditor() == true && item.isEditableField) {
                    Edit(
                        id = item.id,
                        key = item.key.key,
                        name = item.name,
                        formatName = formatName,
                        formatIcon = formatIcon,
                        format = item.format,
                        limitObjectTypes = computedLimitTypes,
                        isPossibleToUnlinkFromType = false,
                        showLimitTypes = false
                    )
                } else {
                    View(
                        id = item.id,
                        key = item.key.key,
                        name = item.name,
                        formatName = formatName,
                        formatIcon = formatIcon,
                        format = item.format,
                        limitObjectTypes = computedLimitTypes,
                        isPossibleToUnlinkFromType = false,
                        showLimitTypes = false
                    )
                }
        }
    }

    private suspend fun computeLimitTypes(item: UiSpacePropertyItem.Item): List<UiPropertyLimitTypeItem> {
        return item.limitObjectTypes.mapNotNull { id ->
            storeOfObjectTypes.get(id = id)?.let { objType ->
                UiPropertyLimitTypeItem(
                    id = objType.id,
                    name = fieldParser.getObjectName(objectWrapper = objType),
                    icon = objType.objectIcon(),
                    uniqueKey = objType.uniqueKey
                )
            }
        }
    }

    //region Edit or Create Property
    fun onDismissPropertyScreen() {
        uiEditPropertyScreen.value = UiEditPropertyState.Hidden
    }

    fun onEvent(event: SpacePropertiesEvent) {
        when (event) {
            is SpacePropertiesEvent.OnPropertyNameUpdate -> updatePropertyName(event.name)
            SpacePropertiesEvent.OnSaveButtonClicked -> saveProperty()
            SpacePropertiesEvent.OnLimitTypesClick -> toggleLimitTypes(show = true)
            SpacePropertiesEvent.OnLimitTypesDismiss -> toggleLimitTypes(show = false)
            is SpacePropertiesEvent.OnLimitTypesDoneClick -> applyLimitTypes(event.items)
            SpacePropertiesEvent.OnPropertyFormatClick -> showPropertyFormatsList()
            SpacePropertiesEvent.OnPropertyFormatsListDismiss -> {
                uiPropertyFormatsListState.value = Hidden
            }

            is SpacePropertiesEvent.OnPropertyFormatSelected -> updatePropertyFormat(event.format)
            SpacePropertiesEvent.OnCreateNewButtonClicked -> proceedWithCreatingProperty()
        }
    }

    private fun updatePropertyName(newName: String) {
        val state = uiEditPropertyScreen.value as? UiEditPropertyState.Visible ?: return
        uiEditPropertyScreen.value = when (state) {
            is Edit -> state.copy(name = newName)
            is New -> state.copy(name = newName)
            is View -> state // Not editable
        }
    }

    private fun saveProperty() {
        val state = uiEditPropertyScreen.value as? Edit ?: return
        viewModelScope.launch {
            val params = SetObjectDetails.Params(
                ctx = state.id,
                details = mapOf(Relations.NAME to state.name)
            )
            setObjectDetails.async(params).fold(
                onSuccess = {
                    Timber.d("Property updated: $it")
                    uiEditPropertyScreen.value = UiEditPropertyState.Hidden
                },
                onFailure = { error ->
                    Timber.e(error, "Failed to update property")
                    commands.emit(ShowToast("Failed to update property"))
                }
            )
        }
    }

    private fun toggleLimitTypes(show: Boolean) {
        uiEditPropertyScreen.value = when (val state = uiEditPropertyScreen.value) {
            is Edit -> state.copy(showLimitTypes = show)
            is New -> state.copy(showLimitTypes = show)
            is View -> state.copy(showLimitTypes = show)
            else -> state
        }
    }

    private fun applyLimitTypes(items: List<Id>) {
        val state = uiEditPropertyScreen.value as? New ?: run {
            Timber.w("Possible only for New state")
            return
        }
        uiEditPropertyScreen.value = state.copy(
            selectedLimitTypeIds = items,
            showLimitTypes = false
        )
    }

    private fun showPropertyFormatsList() {
        uiEditPropertyScreen.value as? New ?: run {
            Timber.w("Possible only for New state")
            return
        }
        uiPropertyFormatsListState.value = Visible(
            items = UiEditTypePropertiesState.PROPERTIES_FORMATS.map { format ->
                Format(
                    format = format,
                    prettyName = stringResourceProvider.getPropertiesFormatPrettyString(format)
                )
            }
        )
    }

    private fun updatePropertyFormat(selectedFormat: Format) {
        viewModelScope.launch {
            uiPropertyFormatsListState.value = Hidden
            val state = uiEditPropertyScreen.value as? UiEditPropertyState.Visible ?: return@launch
            uiEditPropertyScreen.value = when (state) {
                is New -> {
                    val newFormat = selectedFormat.format
                    state.copy(
                        formatName = stringResourceProvider.getPropertiesFormatPrettyString(
                            newFormat
                        ),
                        formatIcon = newFormat.simpleIcon(),
                        format = newFormat,
                        limitObjectTypes = newFormat.getAllObjectTypesByFormat(storeOfObjectTypes),
                        selectedLimitTypeIds = emptyList(),
                        showLimitTypes = false
                    )
                }

                else -> state
            }
        }
    }

    private fun proceedWithCreatingProperty() {
        viewModelScope.launch {
            val state = uiEditPropertyScreen.value as? New ?: return@launch
            val params = CreateRelation.Params(
                space = vmParams.spaceId.id,
                format = state.format,
                name = state.name,
                limitObjectTypes = state.selectedLimitTypeIds,
                prefilled = emptyMap()
            )
            createRelation(params).process(
                success = { property ->
                    Timber.d("Property created: $property")
                    uiEditPropertyScreen.value = UiEditPropertyState.Hidden
                },
                failure = { error ->
                    Timber.e(error, "Failed to create property")
                    commands.emit(Command.ShowToast("Error while creating property"))
                }
            )
        }
    }
    //endregion

    sealed class Command {
        data object Back : Command()
        data class ShowToast(val message: String) : Command()
    }

    companion object {
        val DEFAULT_NEW_PROPERTY_FORMAT = RelationFormat.LONG_TEXT
    }

    data class VmParams(
        val spaceId: SpaceId
    )
}

class SpacePropertiesVmFactory @Inject constructor(
    private val vmParams: SpacePropertiesViewModel.VmParams,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val analytics: Analytics,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val userPermissionProvider: UserPermissionProvider,
    private val fieldParser: FieldParser,
    private val storeOfRelations: StoreOfRelations,
    private val stringResourceProvider: StringResourceProvider,
    private val setObjectDetails: SetObjectDetails,
    private val createRelation: CreateRelation,
    private val setObjectListIsArchived: SetObjectListIsArchived
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        SpacePropertiesViewModel(
            vmParams = vmParams,
            storeOfObjectTypes = storeOfObjectTypes,
            analytics = analytics,
            analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
            userPermissionProvider = userPermissionProvider,
            fieldParser = fieldParser,
            storeOfRelations = storeOfRelations,
            stringResourceProvider = stringResourceProvider,
            setObjectDetails = setObjectDetails,
            createRelation = createRelation,
            setObjectListIsArchived = setObjectListIsArchived
        ) as T
}

data class UiSpacePropertiesScreenState(
    val items: List<UiSpacePropertyItem>
) {
    companion object {
        val Empty = UiSpacePropertiesScreenState(emptyList())
    }
}

sealed class UiSpacePropertyItem{

    abstract val id: Id

    sealed class Section : UiSpacePropertyItem() {
        data class MyProperties(override val id: Id = "section_my_properties") : Section()
        data class SystemProperties(override val id: Id = "section_system_properties") : Section()
    }

    data class Item(
        override val id: Id,
        val key: RelationKey,
        val name: String,
        val format: RelationFormat,
        val isEditableField: Boolean,
        val limitObjectTypes: List<Id>,
        val isPossibleMoveToBin: Boolean = false
    ) : UiSpacePropertyItem()
}