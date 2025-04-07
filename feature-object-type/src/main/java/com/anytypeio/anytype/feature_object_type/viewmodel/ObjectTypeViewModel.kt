package com.anytypeio.anytype.feature_object_type.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.permissions.ObjectPermissions
import com.anytypeio.anytype.core_models.permissions.toObjectPermissionsForTypes
import com.anytypeio.anytype.core_ui.extensions.simpleIcon
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.event.interactor.SpaceSyncAndP2PStatusProvider
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.`object`.DuplicateObjects
import com.anytypeio.anytype.domain.`object`.SetObjectDetails
import com.anytypeio.anytype.domain.objects.DeleteObjects
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.primitives.GetObjectTypeConflictingFields
import com.anytypeio.anytype.domain.primitives.SetObjectTypeRecommendedFields
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.domain.templates.CreateTemplate
import com.anytypeio.anytype.feature_object_type.fields.FieldEvent
import com.anytypeio.anytype.feature_object_type.fields.UiFieldEditOrNewState.Visible.*
import com.anytypeio.anytype.feature_object_type.fields.UiFieldsListItem
import com.anytypeio.anytype.feature_object_type.fields.UiFieldsListState
import com.anytypeio.anytype.feature_object_type.fields.UiLocalsFieldsInfoState
import com.anytypeio.anytype.feature_object_type.ui.ObjectTypeCommand
import com.anytypeio.anytype.feature_object_type.ui.ObjectTypeCommand.*
import com.anytypeio.anytype.feature_object_type.ui.ObjectTypeVmParams
import com.anytypeio.anytype.feature_object_type.ui.TypeEvent
import com.anytypeio.anytype.feature_object_type.ui.UiDeleteAlertState
import com.anytypeio.anytype.feature_object_type.ui.UiEditButton
import com.anytypeio.anytype.feature_object_type.ui.UiErrorState
import com.anytypeio.anytype.feature_object_type.ui.UiErrorState.*
import com.anytypeio.anytype.feature_object_type.ui.UiErrorState.Reason.*
import com.anytypeio.anytype.feature_object_type.ui.UiHorizontalButtonsState
import com.anytypeio.anytype.feature_object_type.ui.UiPropertiesButtonState
import com.anytypeio.anytype.feature_object_type.ui.UiIconsPickerState
import com.anytypeio.anytype.feature_object_type.ui.UiIconState
import com.anytypeio.anytype.feature_object_type.ui.UiLayoutButtonState
import com.anytypeio.anytype.feature_object_type.ui.UiLayoutTypeState
import com.anytypeio.anytype.feature_object_type.ui.UiLayoutTypeState.*
import com.anytypeio.anytype.feature_object_type.ui.UiSyncStatusBadgeState
import com.anytypeio.anytype.feature_object_type.ui.UiTemplatesButtonState
import com.anytypeio.anytype.feature_object_type.ui.UiTemplatesModalListState
import com.anytypeio.anytype.feature_object_type.ui.UiTitleState
import com.anytypeio.anytype.feature_object_type.ui.buildUiPropertiesList
import com.anytypeio.anytype.feature_object_type.ui.create.UiTypeSetupTitleAndIconState
import com.anytypeio.anytype.feature_object_type.ui.toTemplateView
import com.anytypeio.anytype.feature_properties.edit.UiEditPropertyState
import com.anytypeio.anytype.feature_properties.edit.UiEditPropertyState.Visible.View
import com.anytypeio.anytype.feature_properties.edit.UiPropertyLimitTypeItem
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.extension.sendAnalyticsScreenObjectType
import com.anytypeio.anytype.presentation.mapper.objectIcon
import com.anytypeio.anytype.presentation.objects.custom_icon.CustomIconColor
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants.defaultKeys
import com.anytypeio.anytype.presentation.sync.SyncStatusWidgetState
import com.anytypeio.anytype.presentation.sync.toSyncStatusWidgetState
import com.anytypeio.anytype.presentation.sync.updateStatus
import com.anytypeio.anytype.presentation.templates.TemplateView
import kotlin.collections.map
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Fragment: @see [ObjectTypeFragment]
 * Factory: @see [ObjectTypeVMFactory]
 * Models: @see [ObjectViewState]
 */
class ObjectTypeViewModel(
    val vmParams: ObjectTypeVmParams,
    private val analytics: Analytics,
    private val urlBuilder: UrlBuilder,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val userPermissionProvider: UserPermissionProvider,
    private val storeOfRelations: StoreOfRelations,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
    private val spaceSyncAndP2PStatusProvider: SpaceSyncAndP2PStatusProvider,
    private val fieldParser: FieldParser,
    private val coverImageHashProvider: CoverImageHashProvider,
    private val deleteObjects: DeleteObjects,
    private val setObjectDetails: SetObjectDetails,
    private val stringResourceProvider: StringResourceProvider,
    private val createTemplate: CreateTemplate,
    private val duplicateObjects: DuplicateObjects,
    private val getObjectTypeConflictingFields: GetObjectTypeConflictingFields,
    private val objectTypeSetRecommendedFields: SetObjectTypeRecommendedFields
) : ViewModel(), AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate {

    //region UI STATE
    //top bar
    val uiSyncStatusWidgetState =
        MutableStateFlow<SyncStatusWidgetState>(SyncStatusWidgetState.Hidden)
    val uiSyncStatusBadgeState =
        MutableStateFlow<UiSyncStatusBadgeState>(UiSyncStatusBadgeState.Hidden)
    val uiEditButtonState = MutableStateFlow<UiEditButton>(UiEditButton.Hidden)

    //header
    val uiTitleState = MutableStateFlow<UiTitleState>(UiTitleState.Companion.EMPTY)
    val uiIconState = MutableStateFlow<UiIconState>(UiIconState.Companion.EMPTY)

    //layout, properties and templates buttons
    val uiHorizontalButtonsState =
        MutableStateFlow<UiHorizontalButtonsState>(
            UiHorizontalButtonsState(
                uiLayoutButtonState = UiLayoutButtonState.Hidden,
                uiPropertiesButtonState = UiPropertiesButtonState.Hidden,
                uiTemplatesButtonState = UiTemplatesButtonState.Hidden,
                isVisible = false
            )
        )

    //type layouts
    val uiTypeLayoutsState = MutableStateFlow<UiLayoutTypeState>(UiLayoutTypeState.Hidden)

    //templates modal list state
    val uiTemplatesModalListState =
        MutableStateFlow<UiTemplatesModalListState>(UiTemplatesModalListState.Hidden.EMPTY)

    //alerts
    val uiAlertState = MutableStateFlow<UiDeleteAlertState>(UiDeleteAlertState.Hidden)
    val uiFieldLocalInfoState =
        MutableStateFlow<UiLocalsFieldsInfoState>(UiLocalsFieldsInfoState.Hidden)

    //properties list
    val uiTypePropertiesListState = MutableStateFlow<UiFieldsListState>(UiFieldsListState.EMPTY)

    //edit property
    val uiEditPropertyScreen = MutableStateFlow<UiEditPropertyState>(UiEditPropertyState.Hidden)

    //icons picker screen
    val uiIconsPickerScreen = MutableStateFlow<UiIconsPickerState>(UiIconsPickerState.Hidden)

    //title and icon update screen
    val uiTitleAndIconUpdateState =
        MutableStateFlow<UiTypeSetupTitleAndIconState>(UiTypeSetupTitleAndIconState.Hidden)

    //error
    val errorState = MutableStateFlow<UiErrorState>(UiErrorState.Hidden)
    //endregion

    //region INNER STATE
    private val _objTypeState = MutableStateFlow<ObjectWrapper.Type?>(null)
    private val _objectTypePermissionsState = MutableStateFlow<ObjectPermissions?>(null)
    private val _objectTypeConflictingFieldIds = MutableStateFlow<List<Id>>(emptyList())
    //endregion

    val commands = MutableSharedFlow<ObjectTypeCommand>()

    //region INIT AND LIFE CYCLE
    init {
        Timber.d("init, vmParams: $vmParams")
        proceedWithObservingSyncStatus()
        proceedWithObservingObjectType()
        proceedWithGetObjectTypeConflictingFields()
    }

    fun onStart() {
        Timber.d("onStart, vmParams: $vmParams")
        startSubscriptions()
        viewModelScope.launch {
            sendAnalyticsScreenObjectType(
                analytics = analytics
            )
        }
    }

    fun onStop() {
        Timber.d("onStop")
        stopSubscriptions()
    }
    //endregion

    //region DATA
    private fun proceedWithObservingObjectType() {
        viewModelScope.launch {
            combine(
                storeOfObjectTypes.trackChanges(),
                storeOfRelations.trackChanges(),
                userPermissionProvider.observe(space = vmParams.spaceId),
                _objectTypeConflictingFieldIds,
            ) { _, _, permission, conflictingFields ->
                permission to conflictingFields
            }.catch {
                Timber.e(it, "Error while observing object type")
                _objTypeState.value = null
                errorState.value =
                    UiErrorState.Show(UiErrorState.Reason.ErrorGettingObjects(it.message ?: ""))
            }
                .collect { (permission, conflictingFields) ->
                    permission?.let {
                        val objType = storeOfObjectTypes.get(vmParams.objectId)
                        if (objType != null) {
                            val objectPermissions = objType.toObjectPermissionsForTypes(
                                participantCanEdit = it.isOwnerOrEditor()
                            )
                            mapObjectTypeToUi(
                                objType = objType,
                                objectPermissions = objectPermissions,
                                conflictingFields = conflictingFields,
                                fieldParser = fieldParser
                            )
                        } else {
                            Timber.w(
                                "Error while observing object type [${vmParams.objectId}], " +
                                        "objType is not present in store"
                            )
                            _objTypeState.value = null
                            errorState.value = UiErrorState.Show(
                                UiErrorState.Reason.ErrorGettingObjects("Type details are empty")
                            )
                        }
                    }
                }
        }
    }

    private fun proceedWithObservingSyncStatus() {
        viewModelScope.launch {
            spaceSyncAndP2PStatusProvider
                .observe()
                .catch { Timber.e(it, "Error while observing sync status") }
                .collect { syncAndP2pState ->
                    uiSyncStatusBadgeState.value = UiSyncStatusBadgeState.Visible(syncAndP2pState)
                    uiSyncStatusWidgetState.value =
                        uiSyncStatusWidgetState.value.updateStatus(syncAndP2pState)
                }
        }
    }

    private fun startSubscriptions() {
        startTemplatesSubscription()
    }

    private fun stopSubscriptions() {
        viewModelScope.launch {
            storelessSubscriptionContainer.unsubscribe(
                listOf(
                    templatesSubId(vmParams.objectId),
                )
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun startTemplatesSubscription() {
        viewModelScope.launch {
            combine(
                _objectTypePermissionsState,
                storeOfObjectTypes.trackChanges()
            ) { permissions, _ ->
                permissions
            }.flatMapLatest { permissions ->
                val objType = storeOfObjectTypes.get(vmParams.objectId)
                if (objType != null && permissions != null && permissions.canCreateTemplatesForThisType) {
                    loadTemplates(objType = objType)
                        .map { templates -> Triple(objType, templates, permissions) }
                } else {
                    emptyFlow()
                }
            }.collect { (objType, templates, permissions) ->
                mapTemplatesSubscriptionToUi(objType, templates, permissions)
            }
        }
    }

    private fun loadTemplates(objType: ObjectWrapper.Type): Flow<List<TemplateView>> {

        val searchParams = StoreSearchParams(
            filters = filtersForTemplatesSearch(objectTypeId = vmParams.objectId),
            sorts = listOf(sortForTemplatesSearch()),
            space = vmParams.spaceId,
            limit = TEMPLATE_MAX_COUNT,
            keys = defaultKeys,
            subscription = templatesSubId(vmParams.objectId)
        )

        return storelessSubscriptionContainer.subscribe(searchParams).map { templates ->
            templates.map {
                it.toTemplateView(
                    objType = objType,
                    urlBuilder = urlBuilder,
                    coverImageHashProvider = coverImageHashProvider,
                )
            }
        }
    }

    private fun handleError(e: Throwable) {
        errorState.value = UiErrorState.Show(
            reason = UiErrorState.Reason.Other(e.message ?: "")
        )
    }
    //endregion

    //region UI STATE
    private fun updateDefaultTemplates(defaultTemplate: Id?) {
    }

    private suspend fun mapObjectTypeToUi(
        objType: ObjectWrapper.Type,
        objectPermissions: ObjectPermissions,
        conflictingFields: List<Id>,
        fieldParser: FieldParser
    ) {
        _objTypeState.value = objType
        _objectTypePermissionsState.value = objectPermissions

        uiTitleState.value = UiTitleState(
            title = objType.pluralName.orEmpty(),
            isEditable = objectPermissions.canEditDetails
        )
        val newIcon = objType.objectIcon()
        uiIconState.value = UiIconState(
            icon = newIcon,
            isEditable = objectPermissions.canEditDetails
        )
        (uiTitleAndIconUpdateState.value as? UiTypeSetupTitleAndIconState.Visible.EditType)?.let {
            uiTitleAndIconUpdateState.value = it.copy(icon = newIcon)
        }
        if (objectPermissions.canDelete) {
            uiEditButtonState.value = UiEditButton.Visible
        }
        objType.recommendedLayout?.let { layout ->
            if (_objectTypePermissionsState.value?.canChangeRecommendedLayoutForThisType == true) {
                uiHorizontalButtonsState.value =
                    uiHorizontalButtonsState.value.copy(
                        uiLayoutButtonState = UiLayoutButtonState.Visible(layout = layout),
                        isVisible = true
                    )
            }
        }
        updateDefaultTemplates(defaultTemplate = objType.defaultTemplateId)
        val items = buildUiPropertiesList(
            objType = objType,
            stringResourceProvider = stringResourceProvider,
            fieldParser = fieldParser,
            storeOfObjectTypes = storeOfObjectTypes,
            storeOfRelations = storeOfRelations,
            objectTypeConflictingPropertiesIds = conflictingFields,
            showHiddenProperty = vmParams.showHiddenFields,
            objectPermissions = objectPermissions
        )
        uiTypePropertiesListState.value = UiFieldsListState(items = items)
        if (objectPermissions.canEditRelationsList) {
            uiHorizontalButtonsState.value =
                uiHorizontalButtonsState.value.copy(
                    uiPropertiesButtonState = UiPropertiesButtonState.Visible(
                        count = items.count { it is UiFieldsListItem.Item }
                    ),
                    isVisible = true
                )
        }
    }

    private fun mapTemplatesSubscriptionToUi(
        objType: ObjectWrapper.Type,
        templates: List<TemplateView>,
        permissions: ObjectPermissions
    ) {

        uiHorizontalButtonsState.value = uiHorizontalButtonsState.value.copy(
            uiTemplatesButtonState = UiTemplatesButtonState.Visible(count = templates.size),
            isVisible = true
        )

        val updatedTemplates = templates.mapNotNull { template ->
            if (template is TemplateView.Template) {
                template.copy(
                    isDefault = template.id == objType.defaultTemplateId
                )
            } else {
                null
            }
        }

        val currentValue = uiTemplatesModalListState.value
        uiTemplatesModalListState.value = when (currentValue) {
            is UiTemplatesModalListState.Hidden -> currentValue.copy(updatedTemplates)
            is UiTemplatesModalListState.Visible -> currentValue.copy(
                updatedTemplates,
                showAddIcon = permissions.canCreateTemplatesForThisType
            )
        }
    }

    fun hideError() {
        errorState.value = UiErrorState.Hidden
    }

    fun setupUiEditPropertyScreen(item: UiFieldsListItem.Item) {
        viewModelScope.launch {
            val permissions = _objectTypePermissionsState.value

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

            uiEditPropertyScreen.value =
                if (permissions?.participantCanEdit == true && item.isEditableField) {
                    UiEditPropertyState.Visible.Edit(
                        id = item.id,
                        key = item.fieldKey,
                        name = item.fieldTitle,
                        formatName = formatName,
                        formatIcon = formatIcon,
                        format = item.format,
                        limitObjectTypes = computedLimitTypes,
                        isPossibleToUnlinkFromType = item.isPossibleToUnlinkFromType,
                        showLimitTypes = false
                    )
                } else {
                    UiEditPropertyState.Visible.View(
                        id = item.id,
                        key = item.fieldKey,
                        name = item.fieldTitle,
                        formatName = formatName,
                        formatIcon = formatIcon,
                        format = item.format,
                        limitObjectTypes = computedLimitTypes,
                        isPossibleToUnlinkFromType = item.isPossibleToUnlinkFromType,
                        showLimitTypes = false
                    )
                }
        }
    }
    //endregion

    //region Ui EVENTS - TYPES
    fun onTypeEvent(event: TypeEvent) {
        Timber.d("onTypeEvent: $event")
        when (event) {
            TypeEvent.OnFieldsButtonClick -> {
                viewModelScope.launch {
                    commands.emit(ObjectTypeCommand.OpenFieldsScreen)
                }
            }

            TypeEvent.OnLayoutButtonClick -> {
                if (_objTypeState.value?.recommendedLayout == ObjectType.Layout.NOTE) {
                    uiTypeLayoutsState.value = Visible(
                        layouts = listOf(ObjectType.Layout.NOTE),
                        selectedLayout = _objTypeState.value?.recommendedLayout
                    )
                } else {
                    uiTypeLayoutsState.value = Visible(
                        layouts = listOf(
                            ObjectType.Layout.BASIC,
                            //DROID-3485, NOTE layout is not supported for now
                            //ObjectType.Layout.NOTE,
                            ObjectType.Layout.PROFILE,
                            ObjectType.Layout.TODO
                        ),
                        selectedLayout = _objTypeState.value?.recommendedLayout
                    )
                }
            }

            is TypeEvent.OnSyncStatusClick -> {
                uiSyncStatusWidgetState.value =
                    event.status.toSyncStatusWidgetState()
            }

            TypeEvent.OnSyncStatusDismiss -> {
                uiSyncStatusWidgetState.value = SyncStatusWidgetState.Hidden
            }

            TypeEvent.OnTemplatesAddIconClick -> {
                proceedWithCreateTemplate()
            }

            is TypeEvent.OnObjectTypeTitleUpdate -> {
                uiTitleState.value = uiTitleState.value.copy(title = event.title)
                updateTitle(event.title)
            }

            is TypeEvent.OnObjectTypeTitleClick -> {
                showTitleAndIconUpdateScreen()
            }

            TypeEvent.OnMenuItemDeleteClick -> {
                uiAlertState.value = UiDeleteAlertState.Show
            }

            TypeEvent.OnAlertDeleteConfirm -> {
                uiAlertState.value = UiDeleteAlertState.Hidden
                proceedWithObjectTypeDelete()
            }

            TypeEvent.OnAlertDeleteDismiss -> {
                uiAlertState.value = UiDeleteAlertState.Hidden
            }

            TypeEvent.OnObjectTypeIconClick -> {
                uiIconsPickerScreen.value = UiIconsPickerState.Visible
            }

            is TypeEvent.OnTemplateItemClick -> {
                onTemplateItemClick(event.item)
            }

            TypeEvent.OnLayoutTypeDismiss -> {
                uiTypeLayoutsState.value = UiLayoutTypeState.Hidden
            }

            is TypeEvent.OnLayoutTypeItemClick -> {
                proceedWithUpdatingLayout(layout = event.item)
            }

            TypeEvent.OnBackClick -> {
                viewModelScope.launch {
                    commands.emit(ObjectTypeCommand.Back)
                }
            }

            is TypeEvent.OnTemplateMenuClick -> proceedWithTemplateMenuClick(event)

            TypeEvent.OnTemplatesModalListDismiss -> {
                uiTemplatesModalListState.value = UiTemplatesModalListState.Hidden(
                    items = uiTemplatesModalListState.value.items
                )
            }

            TypeEvent.OnTemplatesButtonClick -> {
                viewModelScope.launch {
                    val currentState = uiTemplatesModalListState.value
                    uiTemplatesModalListState.value = UiTemplatesModalListState.Visible(
                        items = currentState.items,
                        showAddIcon = _objectTypePermissionsState.value?.canCreateTemplatesForThisType == true
                    )
                }
            }

            TypeEvent.OnIconPickerDismiss -> {
                uiIconsPickerScreen.value = UiIconsPickerState.Hidden
            }

            is TypeEvent.OnIconPickerItemClick -> {
                uiIconsPickerScreen.value = UiIconsPickerState.Hidden
                updateIcon(iconName = event.iconName, newColor = event.color)
            }

            TypeEvent.OnIconPickerRemovedClick -> {
                uiIconsPickerScreen.value = UiIconsPickerState.Hidden
                removeIcon()
            }
        }
    }

    private fun proceedWithTemplateMenuClick(event: TypeEvent.OnTemplateMenuClick) {
        when (event) {
            is TypeEvent.OnTemplateMenuClick.Delete -> {
                if (event.item is TemplateView.Template) {
                    proceedWithTemplateDelete(
                        template = event.item.id
                    )
                }
            }

            is TypeEvent.OnTemplateMenuClick.Duplicate -> {
                if (event.item is TemplateView.Template) {
                    proceedWithDuplicateObject(
                        objectId = event.item.id
                    )
                }
            }

            is TypeEvent.OnTemplateMenuClick.Edit -> {
                onTemplateItemClick(event.item)
            }

            is TypeEvent.OnTemplateMenuClick.SetAsDefault -> {
                if (event.item is TemplateView.Template) {
                    proceedWithSetDefaultTemplate(
                        template = event.item.id
                    )
                }
            }
        }

    }

    private fun onTemplateItemClick(item: TemplateView) {
        when (item) {
            is TemplateView.Blank -> {
                //do nothing
            }

            is TemplateView.New -> {
                proceedWithCreateTemplate()
            }

            is TemplateView.Template -> {
                val typeKey = _objTypeState.value?.uniqueKey ?: return
                val command = ObjectTypeCommand.OpenTemplate(
                    templateId = item.id,
                    typeId = vmParams.objectId,
                    typeKey = typeKey,
                    spaceId = vmParams.spaceId.id
                )
                viewModelScope.launch {
                    commands.emit(command)
                }
            }
        }
    }

    private fun proceedWithUpdatingLayout(layout: ObjectType.Layout) {
        viewModelScope.launch {
            val params = SetObjectDetails.Params(
                ctx = vmParams.objectId,
                details = mapOf(Relations.RECOMMENDED_LAYOUT to layout.code.toDouble())
            )
            setObjectDetails.async(params).fold(
                onFailure = { error ->
                    Timber.e(error, "Error while updating object type recommended layout")
                },
                onSuccess = {
                    Timber.d("Object type recommended layout updated to layout: $layout")
                }
            )
        }
    }

    private fun updateTitle(input: String) {
        viewModelScope.launch {
            val params = SetObjectDetails.Params(
                ctx = vmParams.objectId,
                details = mapOf(Relations.NAME to input)
            )
            setObjectDetails.async(params).fold(
                onFailure = { error ->
                    Timber.e(error, "Error while updating data view record")
                },
                onSuccess = {

                }
            )
        }
    }

    private fun updateIcon(
        iconName: String,
        newColor: CustomIconColor?
    ) {
        viewModelScope.launch {
            val params = SetObjectDetails.Params(
                ctx = vmParams.objectId,
                details = mapOf(
                    Relations.ICON_EMOJI to null,
                    Relations.ICON_NAME to iconName,
                    Relations.ICON_OPTION to newColor?.iconOption?.toDouble()
                )
            )
            setObjectDetails.async(params).fold(
                onFailure = { error ->
                    Timber.e(error, "Error while updating data view record")
                },
                onSuccess = {
                    Timber.d("Object type icon updated to icon: $iconName")
                }
            )
        }
    }

    private fun removeIcon() {
        viewModelScope.launch {
            val params = SetObjectDetails.Params(
                ctx = vmParams.objectId,
                details = mapOf(
                    Relations.ICON_EMOJI to null,
                    Relations.ICON_NAME to null,
                    Relations.ICON_OPTION to null
                )
            )
            setObjectDetails.async(params).fold(
                onFailure = { error ->
                    Timber.e(error, "Error while updating data view record")
                },
                onSuccess = {
                }
            )
        }
    }

    fun closeObject() {
        viewModelScope.launch {
            commands.emit(ObjectTypeCommand.Back)
        }
    }
    //endregion

    //region Ui EVENTS - FIELDS
    fun onFieldEvent(event: FieldEvent) {
        Timber.d("onFieldEvent: $event")
        when (event) {
            FieldEvent.OnEditPropertyScreenDismiss -> {
                uiEditPropertyScreen.value = UiEditPropertyState.Hidden
            }

            is FieldEvent.OnFieldItemClick -> {
                when (event.item) {
                    is UiFieldsListItem.Item -> setupUiEditPropertyScreen(item = event.item)
                    else -> {}
                }
            }

            is FieldEvent.FieldItemMenu -> proceedWithFieldItemMenuClick(event)
            FieldEvent.FieldLocalInfo.OnDismiss -> {
                uiFieldLocalInfoState.value = UiLocalsFieldsInfoState.Hidden
            }

            FieldEvent.Section.OnLocalInfoClick -> {
                uiFieldLocalInfoState.value = UiLocalsFieldsInfoState.Visible
            }

            FieldEvent.Section.OnAddToSidebarIconClick -> {
                viewModelScope.launch {
                    commands.emit(
                        OpenEditTypePropertiesScreen(
                            typeId = vmParams.objectId,
                            space = vmParams.spaceId.id,
                        )
                    )
                }
            }

            FieldEvent.DragEvent.OnDragEnd -> {
                val newItems = uiTypePropertiesListState.value.items
                val headerItems = mutableListOf<Id>()
                val sideBarItems = mutableListOf<Id>()
                val hiddenItems = mutableListOf<Id>()
                val filesItems = mutableListOf<Id>()
                var currentSection: UiFieldsListItem.Section? = null
                newItems.forEach { item ->
                    when (item) {
                        is UiFieldsListItem.Item -> {
                            when (currentSection) {
                                is UiFieldsListItem.Section.Header -> headerItems.add(item.id)
                                is UiFieldsListItem.Section.SideBar -> sideBarItems.add(item.id)
                                is UiFieldsListItem.Section.Hidden -> hiddenItems.add(item.id)
                                is UiFieldsListItem.Section.File -> filesItems.add(item.id)
                                else -> {}
                            }
                        }

                        is UiFieldsListItem.Section -> currentSection = item
                    }
                }
                proceedWithUpdatingTypeFields(
                    headerFields = headerItems,
                    sidebarFields = sideBarItems,
                    hiddenFields = hiddenItems,
                    fileFields = filesItems
                )
            }

            is FieldEvent.DragEvent.OnMove -> {
                val currentList = uiTypePropertiesListState.value.items.toMutableList()
                val fromIndex = currentList.indexOfFirst { it.id == event.fromKey }
                val toIndex = currentList.indexOfFirst { it.id == event.toKey }
                if ((fromIndex == -1) || (toIndex == -1)) return
                val item = currentList.removeAt(fromIndex)
                currentList.add(toIndex, item)
                uiTypePropertiesListState.value = UiFieldsListState(items = currentList)
            }

            is FieldEvent.EditProperty -> proceedWithEditPropertyEvent(event)
            FieldEvent.OnBackClick -> {
                viewModelScope.launch {
                    commands.emit(ObjectTypeCommand.CloseFieldsScreen)
                }
            }
        }
    }

    private fun proceedWithFieldItemMenuClick(event: FieldEvent.FieldItemMenu) {
        when (event) {
            is FieldEvent.FieldItemMenu.OnDeleteFromTypeClick -> {
                val deleteId = event.id
                val headerItems = mutableListOf<Id>()
                val sideBarItems = mutableListOf<Id>()
                val hiddenItems = mutableListOf<Id>()
                val filesItems = mutableListOf<Id>()
                var currentSection: UiFieldsListItem.Section? = null
                uiTypePropertiesListState.value.items.forEach { item ->
                    when (item) {
                        is UiFieldsListItem.Item -> {
                            when (currentSection) {
                                is UiFieldsListItem.Section.Header -> {
                                    if (item.id != deleteId) headerItems.add(item.id)
                                }

                                is UiFieldsListItem.Section.SideBar -> {
                                    if (item.id != deleteId) sideBarItems.add(item.id)
                                }

                                is UiFieldsListItem.Section.Hidden -> {
                                    if (item.id != deleteId) hiddenItems.add(item.id)
                                }

                                is UiFieldsListItem.Section.File -> {
                                    if (item.id != deleteId) filesItems.add(item.id)
                                }

                                else -> {}
                            }
                        }

                        is UiFieldsListItem.Section -> currentSection = item
                    }
                }
                proceedWithUpdatingTypeFields(
                    headerFields = headerItems,
                    sidebarFields = sideBarItems,
                    hiddenFields = hiddenItems,
                    fileFields = filesItems
                )
                uiEditPropertyScreen.value = UiEditPropertyState.Hidden
            }

            is FieldEvent.FieldItemMenu.OnAddLocalToTypeClick -> {
                val currentRecommendedFields = _objTypeState.value?.recommendedRelations.orEmpty()
                val newRecommendedFields = currentRecommendedFields + event.item.id
                proceedWithSetRecommendedFields(newRecommendedFields)
            }
        }
    }

    private fun proceedWithEditPropertyEvent(event: FieldEvent.EditProperty) {
        when (event) {
            is FieldEvent.EditProperty.OnPropertyNameUpdate -> {
                val state = uiEditPropertyScreen.value as? UiEditPropertyState.Visible ?: return
                uiEditPropertyScreen.value = when (state) {
                    is UiEditPropertyState.Visible.Edit -> state.copy(name = event.name)
                    is UiEditPropertyState.Visible.New -> state.copy(name = event.name)
                    is View -> state
                }
            }

            FieldEvent.EditProperty.OnSaveButtonClicked -> {
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
                            Timber.d("Relation updated: $it")
                            uiEditPropertyScreen.value = UiEditPropertyState.Hidden
                        },
                        onFailure = { error ->
                            Timber.e(error, "Failed to update relation")
                            errorState.value = Show(
                                reason = Other(error.message ?: "")
                            )
                        }
                    )
                }
            }

            FieldEvent.EditProperty.OnLimitTypesClick -> {
                uiEditPropertyScreen.value = when (val state = uiEditPropertyScreen.value) {
                    is UiEditPropertyState.Visible.Edit -> state.copy(showLimitTypes = true)
                    is UiEditPropertyState.Visible.New -> state.copy(showLimitTypes = true)
                    is View -> state.copy(showLimitTypes = true)
                    else -> state
                }
            }

            FieldEvent.EditProperty.OnLimitTypesDismiss -> {
                uiEditPropertyScreen.value = when (val state = uiEditPropertyScreen.value) {
                    is UiEditPropertyState.Visible.Edit -> state.copy(showLimitTypes = false)
                    is UiEditPropertyState.Visible.New -> state.copy(showLimitTypes = false)
                    is View -> state.copy(showLimitTypes = false)
                    else -> state
                }
            }
        }
    }
    //endregion

    //region USE CASES
    private fun proceedWithUpdatingTypeFields(
        headerFields: List<Id>,
        sidebarFields: List<Id>,
        hiddenFields: List<Id>,
        fileFields: List<Id>
    ) {
        Timber.d("proceedWithUpdatingTypeFields")
        viewModelScope.launch {
            val params = SetObjectDetails.Params(
                ctx = vmParams.objectId,
                details = mapOf(
                    Relations.RECOMMENDED_FEATURED_RELATIONS to headerFields,
                    Relations.RECOMMENDED_RELATIONS to sidebarFields,
                    Relations.RECOMMENDED_HIDDEN_RELATIONS to hiddenFields,
                    Relations.RECOMMENDED_FILE_RELATIONS to fileFields
                )
            )
            setObjectDetails.async(params).fold(
                onSuccess = {
                    Timber.d("Fields updated")
                },
                onFailure = {
                    Timber.e(it, "Error while updating fields")
                }
            )
        }
    }

    private fun proceedWithGetObjectTypeConflictingFields() {
        viewModelScope.launch {
            getObjectTypeConflictingFields.async(
                GetObjectTypeConflictingFields.Params(
                    objectTypeId = vmParams.objectId,
                    spaceId = vmParams.spaceId.id
                )
            ).fold(
                onSuccess = { fields ->
                    _objectTypeConflictingFieldIds.value = fields
                },
                onFailure = {
                    Timber.e(it, "Error while getting conflicting fields")
                }
            )
        }
    }

    private fun proceedWithObjectTypeDelete() {
        val params = DeleteObjects.Params(
            targets = listOf(vmParams.objectId)
        )
        viewModelScope.launch {
            deleteObjects.async(params).fold(
                onSuccess = {
                    Timber.d("Object ${vmParams.objectId} deleted")
                    commands.emit(ObjectTypeCommand.Back)
                },
                onFailure = {
                    Timber.e(it, "Error while deleting object ${vmParams.objectId}")
                }
            )
        }
    }

    private fun proceedWithTemplateDelete(template: Id) {
        val params = DeleteObjects.Params(
            targets = listOf(template)
        )
        viewModelScope.launch {
            deleteObjects.async(params).fold(
                onSuccess = {
                    Timber.d("Template $template deleted")
                },
                onFailure = {
                    Timber.e(it, "Error while deleting template $template")
                }
            )
        }
    }

    private fun proceedWithCreateTemplate() {
        val params = CreateTemplate.Params(
            targetObjectTypeId = vmParams.objectId,
            spaceId = vmParams.spaceId
        )
        viewModelScope.launch {
            createTemplate.async(params).fold(
                onSuccess = { template ->
                    val typeKey = _objTypeState.value?.uniqueKey
                    if (typeKey != null) {
                        val command = ObjectTypeCommand.OpenTemplate(
                            templateId = template.id,
                            typeId = vmParams.objectId,
                            typeKey = typeKey,
                            spaceId = vmParams.spaceId.id
                        )
                        commands.emit(command)
                    }
                },
                onFailure = {
                    Timber.e(it, "Error while creating template")
                }
            )
        }
    }

    private fun proceedWithDuplicateObject(objectId: Id) {
        val params = DuplicateObjects.Params(
            ids = listOf(objectId)
        )
        viewModelScope.launch {
            duplicateObjects.async(params).fold(
                onSuccess = {
                    Timber.d("Object $objectId duplicated")
                },
                onFailure = {
                    Timber.e(it, "Error while duplicating object $objectId")
                }
            )
        }
    }

    private fun proceedWithSetDefaultTemplate(template: Id) {
        val params = SetObjectDetails.Params(
            ctx = vmParams.objectId,
            details = mapOf(Relations.DEFAULT_TEMPLATE_ID to template)
        )
        viewModelScope.launch {
            setObjectDetails.async(params).fold(
                onSuccess = {
                    Timber.d("Template $template set as default")
                },
                onFailure = {
                    Timber.e(it, "Error while setting template $template as default")
                }
            )
        }
    }

    private fun proceedWithSetRecommendedFields(fields: List<Id>) {
        val params = SetObjectTypeRecommendedFields.Params(
            objectTypeId = vmParams.objectId,
            fields = fields
        )
        viewModelScope.launch {
            objectTypeSetRecommendedFields.async(params).fold(
                onSuccess = {
                    Timber.d("Recommended fields set")
                },
                onFailure = {
                    Timber.e(it, "Error while setting recommended fields")
                }
            )
        }
    }
    //endregion

    //region Title and Icon Update Screen

    private fun showTitleAndIconUpdateScreen() {
        val objType = _objTypeState.value ?: return
        val initialIcon = uiIconState.value.icon
        val initialTitle = objType.name
        val initialPlural = objType.pluralName
        Timber.d(
            "showTitleAndIconUpdateScreen, initialIcon: $initialIcon, " +
                    "initialTitle: $initialTitle, initialPlural: $initialPlural"
        )
        uiTitleAndIconUpdateState.value = UiTypeSetupTitleAndIconState.Visible.EditType(
            icon = initialIcon,
            initialTitle = initialTitle,
            initialPlural = initialPlural
        )
    }

    fun onDismissTitleAndIconScreen() {
        uiTitleAndIconUpdateState.value = UiTypeSetupTitleAndIconState.Hidden
    }

    fun onIconClickedTitleAndIconScreen() {
        uiIconsPickerScreen.value = UiIconsPickerState.Visible
    }

    fun onButtonClickedTitleAndIconScreen(title: String, plurals: String) {
        val objType = _objTypeState.value ?: return
        Timber.d("onButtonClickedTitleAndIconScreen, title: $title, plural: $plurals")
        val params = SetObjectDetails.Params(
            ctx = objType.id,
            details = mapOf(
                Relations.NAME to title,
                Relations.PLURAL_NAME to plurals
            )
        )
        viewModelScope.launch {
            setObjectDetails.async(params).fold(
                onSuccess = {
                    Timber.d("Object type title updated")
                    uiTitleAndIconUpdateState.value = UiTypeSetupTitleAndIconState.Hidden
                },
                onFailure = {
                    Timber.e(it, "Error while updating object type title")
                }
            )
        }
    }

    //endregion

    companion object {
        const val TEMPLATE_MAX_COUNT = 100

        fun templatesSubId(objectId: Id) = "TYPE-TEMPLATES-SUB-ID--$objectId"
    }
}