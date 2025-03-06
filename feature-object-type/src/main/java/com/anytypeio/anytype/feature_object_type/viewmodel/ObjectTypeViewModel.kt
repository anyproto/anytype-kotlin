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
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_ui.extensions.simpleIcon
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.block.interactor.sets.CreateObjectSet
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
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.primitives.GetObjectTypeConflictingFields
import com.anytypeio.anytype.domain.primitives.SetObjectTypeRecommendedFields
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.domain.templates.CreateTemplate
import com.anytypeio.anytype.feature_object_type.fields.FieldEvent
import com.anytypeio.anytype.feature_object_type.fields.UiEditPropertyState
import com.anytypeio.anytype.feature_object_type.fields.UiFieldEditOrNewState.Visible.*
import com.anytypeio.anytype.feature_object_type.fields.UiFieldsListItem
import com.anytypeio.anytype.feature_object_type.fields.UiFieldsListState
import com.anytypeio.anytype.feature_object_type.fields.UiLocalsFieldsInfoState
import com.anytypeio.anytype.feature_object_type.fields.toUiPropertyItemState
import com.anytypeio.anytype.feature_object_type.properties.add.UiAddPropertyItem
import com.anytypeio.anytype.feature_object_type.ui.ObjectTypeCommand
import com.anytypeio.anytype.feature_object_type.ui.ObjectTypeCommand.OpenEmojiPicker
import com.anytypeio.anytype.feature_object_type.ui.ObjectTypeVmParams
import com.anytypeio.anytype.feature_object_type.ui.TypeEvent
import com.anytypeio.anytype.feature_object_type.ui.UiDeleteAlertState
import com.anytypeio.anytype.feature_object_type.ui.UiEditButton
import com.anytypeio.anytype.feature_object_type.ui.UiErrorState
import com.anytypeio.anytype.feature_object_type.ui.UiFieldsButtonState
import com.anytypeio.anytype.feature_object_type.ui.UiIconState
import com.anytypeio.anytype.feature_object_type.ui.UiLayoutButtonState
import com.anytypeio.anytype.feature_object_type.ui.UiLayoutTypeState
import com.anytypeio.anytype.feature_object_type.ui.UiLayoutTypeState.*
import com.anytypeio.anytype.feature_object_type.ui.UiSyncStatusBadgeState
import com.anytypeio.anytype.feature_object_type.ui.UiTemplatesAddIconState
import com.anytypeio.anytype.feature_object_type.ui.UiTemplatesHeaderState
import com.anytypeio.anytype.feature_object_type.ui.UiTitleState
import com.anytypeio.anytype.feature_object_type.ui.buildUiFieldsList
import com.anytypeio.anytype.feature_object_type.ui.toTemplateView
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.editor.cover.UnsplashViewModel.Companion.DEBOUNCE_DURATION
import com.anytypeio.anytype.presentation.extension.sendAnalyticsScreenObjectType
import com.anytypeio.anytype.presentation.home.OpenObjectNavigation
import com.anytypeio.anytype.presentation.home.navigation
import com.anytypeio.anytype.presentation.mapper.objectIcon
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants.defaultKeys
import com.anytypeio.anytype.presentation.sync.SyncStatusWidgetState
import com.anytypeio.anytype.presentation.sync.toSyncStatusWidgetState
import com.anytypeio.anytype.presentation.sync.updateStatus
import com.anytypeio.anytype.presentation.templates.TemplateView
import kotlin.collections.map
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Fragment: @see [ObjectTypeFragment]
 * Factory: @see [ObjectTypeVMFactory]
 * Models: @see [ObjectViewState]
 */
class ObjectTypeViewModel(
    private val vmParams: ObjectTypeVmParams,
    private val analytics: Analytics,
    private val urlBuilder: UrlBuilder,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val userPermissionProvider: UserPermissionProvider,
    private val storeOfRelations: StoreOfRelations,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
    private val spaceSyncAndP2PStatusProvider: SpaceSyncAndP2PStatusProvider,
    private val createObject: CreateObject,
    private val fieldParser: FieldParser,
    private val coverImageHashProvider: CoverImageHashProvider,
    private val deleteObjects: DeleteObjects,
    private val setObjectDetails: SetObjectDetails,
    private val createObjectSet: CreateObjectSet,
    private val stringResourceProvider: StringResourceProvider,
    private val createTemplate: CreateTemplate,
    private val duplicateObjects: DuplicateObjects,
    private val getObjectTypeConflictingFields: GetObjectTypeConflictingFields,
    private val objectTypeSetRecommendedFields: SetObjectTypeRecommendedFields,
    private val objectTypeStore: ObjectTypeStore
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

    //layout and fields buttons
    val uiFieldsButtonState = MutableStateFlow<UiFieldsButtonState>(UiFieldsButtonState.Hidden)
    val uiLayoutButtonState = MutableStateFlow<UiLayoutButtonState>(UiLayoutButtonState.Hidden)

    //type layouts
    val uiTypeLayoutsState = MutableStateFlow<UiLayoutTypeState>(Hidden)

    //templates header
    val uiTemplatesHeaderState =
        MutableStateFlow<UiTemplatesHeaderState>(UiTemplatesHeaderState.Hidden)
    val uiTemplatesAddIconState =
        MutableStateFlow<UiTemplatesAddIconState>(UiTemplatesAddIconState.Hidden)

    //alerts
    val uiAlertState = MutableStateFlow<UiDeleteAlertState>(UiDeleteAlertState.Hidden)
    val uiFieldLocalInfoState =
        MutableStateFlow<UiLocalsFieldsInfoState>(UiLocalsFieldsInfoState.Hidden)

    //fields
    val uiFieldsListState = MutableStateFlow<UiFieldsListState>(UiFieldsListState.EMPTY)

    //properties
    val uiEditPropertyScreen = MutableStateFlow<UiEditPropertyState>(UiEditPropertyState.Hidden)

    //error
    val errorState = MutableStateFlow<UiErrorState>(UiErrorState.Hidden)
    //endregion

    //region INNER STATE
    private val _objTypeState = MutableStateFlow<ObjectWrapper.Type?>(null)
    private val _objectTypePermissionsState = MutableStateFlow<ObjectPermissions?>(null)
    private val _objectTypeConflictingFieldIds = MutableStateFlow<List<Id>>(emptyList())
    //endregion

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
                                conflictingFields = conflictingFields
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
        conflictingFields: List<Id>
    ) {
        _objTypeState.value = objType
        _objectTypePermissionsState.value = objectPermissions

        if (!objectPermissions.canCreateTemplatesForThisType) {
            uiTemplatesHeaderState.value = UiTemplatesHeaderState.Hidden
            uiTemplatesAddIconState.value = UiTemplatesAddIconState.Hidden
        }
        uiTitleState.value = UiTitleState(
            title = objType.name.orEmpty(),
            isEditable = objectPermissions.canEditDetails
        )
        uiIconState.value = UiIconState(
            icon = objType.objectIcon(urlBuilder),
            isEditable = objectPermissions.canEditDetails
        )
        if (objectPermissions.canDelete) {
            uiEditButtonState.value = UiEditButton.Visible
        }
        objType.recommendedLayout?.let { layout ->
            if (_objectTypePermissionsState.value?.canChangeRecommendedLayoutForThisType == true) {
                uiLayoutButtonState.value = UiLayoutButtonState.Visible(layout = layout)
            }
        }
        updateDefaultTemplates(defaultTemplate = objType.defaultTemplateId)
        val items = buildUiFieldsList(
            objType = objType,
            stringResourceProvider = stringResourceProvider,
            urlBuilder = urlBuilder,
            fieldParser = fieldParser,
            storeOfObjectTypes = storeOfObjectTypes,
            storeOfRelations = storeOfRelations,
            objTypeConflictingFields = conflictingFields,
            showHiddenFields = vmParams.showHiddenFields
        )
        objectTypeStore.setProperties(items.mapNotNull { (it as? UiFieldsListItem.Item)?.fieldKey })
        uiFieldsListState.value = UiFieldsListState(items = items)
        uiFieldsButtonState.value = UiFieldsButtonState.Visible(
            count = items.count { it is UiFieldsListItem.Item }
        )
    }

    private fun mapTemplatesSubscriptionToUi(
        objType: ObjectWrapper.Type,
        templates: List<TemplateView>,
        permissions: ObjectPermissions
    ) {
        uiTemplatesHeaderState.value = UiTemplatesHeaderState.Visible(count = "${templates.size}")

        // Update each template view regarding default selection.
        val updatedTemplates = templates.map { template ->
            when (template) {
                is TemplateView.Blank -> template
                is TemplateView.New -> template
                is TemplateView.Template -> template.copy(
                    isDefault = template.id == objType.defaultTemplateId
                )
            }
        }

        // Build final list with an extra "new template" item if allowed.
        val finalTemplates = buildList<TemplateView> {
            addAll(updatedTemplates)
            if (permissions.participantCanEdit) {
                add(
                    TemplateView.New(
                        targetTypeId = TypeId(objType.id),
                        targetTypeKey = TypeKey(objType.uniqueKey)
                    )
                )
                uiTemplatesAddIconState.value = UiTemplatesAddIconState.Visible
            }
        }
    }

    fun hideError() {
        errorState.value = UiErrorState.Hidden
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
                uiTypeLayoutsState.value = Visible(
                    layouts = listOf(
                        ObjectType.Layout.BASIC,
                        ObjectType.Layout.NOTE,
                        ObjectType.Layout.PROFILE,
                        ObjectType.Layout.TODO
                    ),
                    selectedLayout = _objTypeState.value?.recommendedLayout
                )
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
                updateTitle(event.title)
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
                viewModelScope.launch {
                    commands.emit(OpenEmojiPicker)
                }
            }

            is TypeEvent.OnTemplateItemClick -> {
                onTemplateItemClick(event.item)
            }

            TypeEvent.OnLayoutTypeDismiss -> {
                uiTypeLayoutsState.value = Hidden
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
                    proceedWithTemplateDuplicate(
                        template = event.item.id
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

    fun updateIcon(
        emoji: String
    ) {
        viewModelScope.launch {
            val params = SetObjectDetails.Params(
                ctx = vmParams.objectId,
                details = mapOf(Relations.ICON_EMOJI to emoji)
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

    fun removeIcon() {
        viewModelScope.launch {
            val params = SetObjectDetails.Params(
                ctx = vmParams.objectId,
                details = mapOf(Relations.ICON_EMOJI to null)
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
            FieldEvent.OnChangeTypeClick -> TODO()
            FieldEvent.OnEditPropertyScreenDismiss -> {
                uiEditPropertyScreen.value = UiEditPropertyState.Hidden
            }

            is FieldEvent.OnFieldItemClick -> {
                when (event.item) {
                    is UiFieldsListItem.Item -> {
                        val item = event.item
                        val propertyItem = toUiPropertyItemState(
                            propertyId = item.id,
                            propertyKey = item.fieldKey,
                            name = item.fieldTitle,
                            formatName = stringResourceProvider.getPropertiesFormatPrettyString(item.format),
                            formatIcon = item.format.simpleIcon(),
                            format = item.format,
                            limitObjectTypesCount = item.limitObjectTypes.size
                        ) ?: return
                        val permissions = _objectTypePermissionsState.value
                        if (permissions?.participantCanEdit == true && event.item.isEditableField) {
                            uiEditPropertyScreen.value = UiEditPropertyState.Visible.Edit(propertyItem)
                        } else {
                            uiEditPropertyScreen.value = UiEditPropertyState.Visible.View(propertyItem)
                        }
                    }
                    else -> {}
                }
            }

            FieldEvent.OnLimitTypesClick -> TODO()
            is FieldEvent.OnSaveButtonClicked -> TODO()

            is FieldEvent.FieldItemMenu -> proceedWithFieldItemMenuClick(event)
            FieldEvent.FieldLocalInfo.OnDismiss -> {
                uiFieldLocalInfoState.value = UiLocalsFieldsInfoState.Hidden
            }

            FieldEvent.Section.OnLocalInfoClick -> {
                uiFieldLocalInfoState.value = UiLocalsFieldsInfoState.Visible
            }

            FieldEvent.Section.OnAddToSidebarIconClick -> {
                proceedWithAddFieldToSidebarScreen()
            }

            FieldEvent.DragEvent.OnDragEnd -> {
                val newItems = uiFieldsListState.value.items
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
                val currentList = uiFieldsListState.value.items.toMutableList()
                val fromIndex = currentList.indexOfFirst { it.id == event.fromKey }
                val toIndex = currentList.indexOfFirst { it.id == event.toKey }
                if ((fromIndex == -1) || (toIndex == -1)) return
                val item = currentList.removeAt(fromIndex)
                currentList.add(toIndex, item)
                uiFieldsListState.value = UiFieldsListState(items = currentList)
            }
        }
    }

//    private fun proceedWithAddNewPropertyEvent(event: FieldEvent.AddNewProperty) {
//        when (event) {
//            is FieldEvent.AddNewProperty.OnCreate -> TODO()
//            is FieldEvent.AddNewProperty.OnExistingClicked -> {
//                onAddToSidebarFieldClicked(item = event.item)
//                hideAddNewFieldScreen()
//            }
//            is FieldEvent.AddNewProperty.OnSearchQueryChanged -> onQueryChanged(query = event.query)
//            is FieldEvent.AddNewProperty.OnTypeClicked -> TODO()
//        }
//    }

    private fun proceedWithFieldItemMenuClick(event: FieldEvent.FieldItemMenu) {
        when (event) {
            is FieldEvent.FieldItemMenu.OnDeleteFromTypeClick -> {
                val deleteId = event.item.id
                val headerItems = mutableListOf<Id>()
                val sideBarItems = mutableListOf<Id>()
                val hiddenItems = mutableListOf<Id>()
                val filesItems = mutableListOf<Id>()
                var currentSection: UiFieldsListItem.Section? = null
                uiFieldsListState.value.items.forEach { item ->
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
            }

            is FieldEvent.FieldItemMenu.OnAddLocalToTypeClick -> {
                val currentRecommendedFields = _objTypeState.value?.recommendedRelations.orEmpty()
                val newRecommendedFields = currentRecommendedFields + event.item.id
                proceedWithSetRecommendedFields(newRecommendedFields)
            }

            is FieldEvent.FieldItemMenu.OnRemoveLocalClick -> TODO()
        }
    }
    //endregion

    //region NAVIGATION
    val commands = MutableSharedFlow<ObjectTypeCommand>()
    val navigation = MutableSharedFlow<OpenObjectNavigation>()

    private fun proceedWithNavigation(
        objectId: Id,
        objectLayout: ObjectType.Layout?
    ) {
        Timber.d("proceedWithNavigation, objectId: $objectId, objectLayout: $objectLayout")
        val destination = objectLayout?.navigation(
            target = objectId,
            space = vmParams.spaceId.id
        )
        if (destination != null) {
            viewModelScope.launch {
                navigation.emit(destination)
            }
        } else {
            Timber.w("No navigation destination found for object $objectId with layout $objectLayout")
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

    private fun proceedWithTemplateDuplicate(template: Id) {
        val params = DuplicateObjects.Params(
            ids = listOf(template)
        )
        viewModelScope.launch {
            duplicateObjects.async(params).fold(
                onSuccess = {
                    Timber.d("Template $template duplicated")
                },
                onFailure = {
                    Timber.e(it, "Error while duplicating template $template")
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

    //region ADD NEW FIELD
    private val input = MutableStateFlow("")

    @OptIn(FlowPreview::class)
    private val query = input.take(1).onCompletion {
        emitAll(
            input.drop(1).debounce(DEBOUNCE_DURATION).distinctUntilChanged()
        )
    }

    fun proceedWithAddFieldToSidebarScreen() {
        viewModelScope.launch {
            commands.emit(
                ObjectTypeCommand.OpenAddPropertyScreen(
                    typeId = vmParams.objectId,
                    space = vmParams.spaceId.id,
                )
            )
        }
    }

    private fun updateFieldRecommendations(
        currentFields: List<String>?,
        item: UiAddPropertyItem.Default,
        updateAction: (List<String>) -> Unit
    ) {
        val newFields = currentFields.orEmpty() + item.id
        updateAction(newFields)
    }

    private fun onAddToSidebarFieldClicked(item: UiAddPropertyItem.Default) {
        updateFieldRecommendations(
            currentFields = _objTypeState.value?.recommendedRelations,
            item = item,
            updateAction = ::proceedWithSetRecommendedFields
        )
    }
    //endregion

    companion object {
        const val TEMPLATE_MAX_COUNT = 100

        fun templatesSubId(objectId: Id) = "TYPE-TEMPLATES-SUB-ID--$objectId"
    }
}