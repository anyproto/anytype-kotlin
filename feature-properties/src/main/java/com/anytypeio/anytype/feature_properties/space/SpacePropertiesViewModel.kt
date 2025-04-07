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
import com.anytypeio.anytype.core_ui.extensions.simpleIcon
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
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
import com.anytypeio.anytype.feature_properties.edit.UiPropertyFormatsListState
import com.anytypeio.anytype.feature_properties.edit.UiPropertyFormatsListState.Hidden
import com.anytypeio.anytype.feature_properties.edit.UiPropertyFormatsListState.Visible
import com.anytypeio.anytype.feature_properties.edit.UiPropertyLimitTypeItem
import com.anytypeio.anytype.feature_properties.getAllObjectTypesByFormat
import com.anytypeio.anytype.feature_properties.space.SpacePropertiesViewModel.Command.*
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.mapper.objectIcon
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
    private val createRelation: CreateRelation
) : ViewModel(), AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate {

    //main screen
    val uiItemsState =
        MutableStateFlow<UiSpacePropertiesScreenState>(UiSpacePropertiesScreenState.Empty)

    //edit property
    val uiEditPropertyScreen = MutableStateFlow<UiEditPropertyState>(UiEditPropertyState.Hidden)

    //property formats list
    val uiPropertyFormatsListState =
        MutableStateFlow<UiPropertyFormatsListState>(UiPropertyFormatsListState.Hidden)

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
                        if (property.isHidden == true) {
                            return@mapNotNull null
                        } else {
                            UiSpacePropertyItem(
                                id = property.id,
                                key = RelationKey(property.key),
                                name = property.name.orEmpty(),
                                format = property.format,
                                isEditableField = fieldParser.isPropertyEditable(property),
                                limitObjectTypes = storeOfObjectTypes.mapLimitObjectTypes(
                                    property = property
                                )
                            )
                        }
                    }.sortedBy { it.name }

                    uiItemsState.value = UiSpacePropertiesScreenState(allProperties)
                }
        }
    }

    fun onBackClicked() {
        viewModelScope.launch {
            commands.emit(Command.Back)
        }
    }

    fun onCreateNewPropertyClicked() {
        if (permission.value?.isOwnerOrEditor() == true) {
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
        } else {
            viewModelScope.launch {
                commands.emit(Command.ShowToast("You don't have permission to create new properties"))
            }
        }
    }

    fun onPropertyClicked(item: UiSpacePropertyItem) {
        viewModelScope.launch {

            val computedLimitTypes = item.limitObjectTypes.mapNotNull { id ->
                storeOfObjectTypes.get(id = id)?.let { objType ->
                    UiPropertyLimitTypeItem(
                        id = objType.id,
                        name = fieldParser.getObjectName(objectWrapper = objType),
                        icon = objType.objectIcon(),
                        uniqueKey = objType.uniqueKey
                    )
                }
            }
            val formatName = stringResourceProvider.getPropertiesFormatPrettyString(item.format)
            val formatIcon = item.format.simpleIcon()
            uiEditPropertyScreen.value = if (permission.value?.isOwnerOrEditor() == true && item.isEditableField) {
                UiEditPropertyState.Visible.Edit(
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
                UiEditPropertyState.Visible.View(
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

    data class VmParams(
        val spaceId: SpaceId
    )

    //region Edit or Create Property
    fun onDismissPropertyScreen() {
        uiEditPropertyScreen.value = UiEditPropertyState.Hidden
    }

    fun proceedWithEditPropertyEvent(event: SpacePropertiesEvent) {
        when (event) {
            is SpacePropertiesEvent.OnPropertyNameUpdate -> {
                val state = uiEditPropertyScreen.value as? UiEditPropertyState.Visible ?: return
                uiEditPropertyScreen.value = when (state) {
                    is UiEditPropertyState.Visible.Edit -> state.copy(name = event.name)
                    is New -> state.copy(name = event.name)
                    is View -> state
                }
            }

            SpacePropertiesEvent.OnSaveButtonClicked -> {
                val state =
                    uiEditPropertyScreen.value as? UiEditPropertyState.Visible.Edit ?: return
                viewModelScope.launch {
                    val params = SetObjectDetails.Params(
                        ctx = state.id,
                        details = mapOf(
                            Relations.NAME to state.name
                        )
                    )
                    setObjectDetails.async(params).fold(
                        onSuccess = {
                            Timber.d("Property updated: $it")
                            uiEditPropertyScreen.value = UiEditPropertyState.Hidden
                        },
                        onFailure = { error ->
                            Timber.e(error, "Failed to update property")
                            commands.emit(
                                ShowToast(message = error.message.orEmpty())
                            )
                        }
                    )
                }
            }

            SpacePropertiesEvent.OnLimitTypesClick -> {
                uiEditPropertyScreen.value = when (val state = uiEditPropertyScreen.value) {
                    is UiEditPropertyState.Visible.Edit -> state.copy(showLimitTypes = true)
                    is New -> state.copy(showLimitTypes = true)
                    is View -> state.copy(showLimitTypes = true)
                    else -> state
                }
            }

            SpacePropertiesEvent.OnLimitTypesDismiss -> {
                uiEditPropertyScreen.value = when (val state = uiEditPropertyScreen.value) {
                    is UiEditPropertyState.Visible.Edit -> state.copy(showLimitTypes = false)
                    is New -> state.copy(showLimitTypes = false)
                    is View -> state.copy(showLimitTypes = false)
                    else -> state
                }
            }

            is SpacePropertiesEvent.OnLimitTypesDoneClick -> {
                val state = uiEditPropertyScreen.value as? New ?: return.also {
                    Timber.e("Possible only for New state")
                }
                uiEditPropertyScreen.value = state.copy(
                    selectedLimitTypeIds = event.items,
                    showLimitTypes = false
                )
            }
            SpacePropertiesEvent.OnPropertyFormatClick -> {
                val state = uiEditPropertyScreen.value as? UiEditPropertyState.Visible.New ?: return
                uiPropertyFormatsListState.value = Visible(
                    items = UiEditTypePropertiesState.Companion.PROPERTIES_FORMATS.map { format ->
                        Format(
                            format = format,
                            prettyName = stringResourceProvider.getPropertiesFormatPrettyString(format)
                        )
                    }
                )
            }

            SpacePropertiesEvent.OnPropertyFormatsListDismiss -> {
                uiPropertyFormatsListState.value = Hidden
            }

            is SpacePropertiesEvent.OnPropertyFormatSelected -> {
                viewModelScope.launch {
                    uiPropertyFormatsListState.value = Hidden
                    val state = uiEditPropertyScreen.value as? UiEditPropertyState.Visible ?: return@launch
                    uiEditPropertyScreen.value = when (state) {
                        is New -> {
                            val newFormat = event.format.format
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

            SpacePropertiesEvent.OnCreateNewButtonClicked -> {
                proceedWithCreatingProperty()
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
    private val createRelation: CreateRelation
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
            createRelation = createRelation
        ) as T
}

data class UiSpacePropertiesScreenState(
    val items: List<UiSpacePropertyItem>
) {
    companion object {
        val Empty = UiSpacePropertiesScreenState(emptyList())
    }
}

data class UiSpacePropertyItem(
    val id: Id,
    val key: RelationKey,
    val name: String,
    val format: RelationFormat,
    val isEditableField: Boolean,
    val limitObjectTypes: List<Id>
)

sealed class SpacePropertiesEvent {

    data class OnPropertyNameUpdate(val name: String) : SpacePropertiesEvent()
    data object OnSaveButtonClicked : SpacePropertiesEvent()
    data object OnCreateNewButtonClicked : SpacePropertiesEvent()

    data object OnPropertyFormatClick : SpacePropertiesEvent()
    data object OnPropertyFormatsListDismiss: SpacePropertiesEvent()
    data class OnPropertyFormatSelected(val format: Format) : SpacePropertiesEvent()

    data object OnLimitTypesClick : SpacePropertiesEvent()
    data object OnLimitTypesDismiss : SpacePropertiesEvent()
    data class OnLimitTypesDoneClick(val items: List<Id>) : SpacePropertiesEvent()
}
