package com.anytypeio.anytype.feature_object_type.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.DVSortType
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectOrigin
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.permissions.ObjectPermissions
import com.anytypeio.anytype.core_models.permissions.toObjectPermissionsForTypes
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_ui.lists.objects.UiContentState
import com.anytypeio.anytype.core_ui.lists.objects.UiObjectsListState
import com.anytypeio.anytype.core_utils.ext.orNull
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.block.interactor.sets.CreateObjectSet
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.event.interactor.SpaceSyncAndP2PStatusProvider
import com.anytypeio.anytype.domain.library.StoreSearchByIdsParams
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
import com.anytypeio.anytype.domain.resources.StringResourceProvider
import com.anytypeio.anytype.domain.templates.CreateTemplate
import com.anytypeio.anytype.feature_object_type.models.ObjectTypeVmParams
import com.anytypeio.anytype.feature_object_type.models.UiDeleteAlertState
import com.anytypeio.anytype.feature_object_type.models.UiEditButton
import com.anytypeio.anytype.feature_object_type.models.UiErrorState
import com.anytypeio.anytype.feature_object_type.models.UiFieldsButtonState
import com.anytypeio.anytype.feature_object_type.models.UiIconState
import com.anytypeio.anytype.feature_object_type.models.UiLayoutButtonState
import com.anytypeio.anytype.feature_object_type.models.UiLayoutTypeState
import com.anytypeio.anytype.feature_object_type.models.UiLayoutTypeState.*
import com.anytypeio.anytype.feature_object_type.models.UiMenuSetItem
import com.anytypeio.anytype.feature_object_type.models.UiMenuState
import com.anytypeio.anytype.feature_object_type.models.UiObjectsAddIconState
import com.anytypeio.anytype.feature_object_type.models.UiObjectsHeaderState
import com.anytypeio.anytype.feature_object_type.models.UiObjectsSettingsIconState
import com.anytypeio.anytype.feature_object_type.models.UiSyncStatusBadgeState
import com.anytypeio.anytype.feature_object_type.models.UiTemplatesAddIconState
import com.anytypeio.anytype.feature_object_type.models.UiTemplatesHeaderState
import com.anytypeio.anytype.feature_object_type.models.UiTemplatesListState
import com.anytypeio.anytype.feature_object_type.models.UiTitleState
import com.anytypeio.anytype.feature_object_type.models.toTemplateView
import com.anytypeio.anytype.feature_object_type.ui.TypeEvent
import com.anytypeio.anytype.feature_object_type.viewmodel.ObjectTypeCommand.OpenEmojiPicker
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.extension.sendAnalyticsScreenObjectType
import com.anytypeio.anytype.presentation.home.OpenObjectNavigation
import com.anytypeio.anytype.presentation.home.navigation
import com.anytypeio.anytype.presentation.mapper.objectIcon
import com.anytypeio.anytype.presentation.objects.MenuSortsItem
import com.anytypeio.anytype.presentation.objects.ObjectsListSort
import com.anytypeio.anytype.presentation.objects.UiObjectsListItem
import com.anytypeio.anytype.presentation.objects.toDVSort
import com.anytypeio.anytype.presentation.objects.toUiObjectsListItem
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants.defaultKeys
import com.anytypeio.anytype.presentation.sync.SyncStatusWidgetState
import com.anytypeio.anytype.presentation.sync.toSyncStatusWidgetState
import com.anytypeio.anytype.presentation.templates.ObjectTypeTemplatesContainer
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
import kotlinx.coroutines.flow.onStart
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
    private val templatesContainer: ObjectTypeTemplatesContainer,
    private val coverImageHashProvider: CoverImageHashProvider,
    private val userSettingsRepository: UserSettingsRepository,
    private val deleteObjects: DeleteObjects,
    private val setObjectDetails: SetObjectDetails,
    private val createObjectSet: CreateObjectSet,
    private val stringResourceProvider: StringResourceProvider,
    private val createTemplate: CreateTemplate,
    private val duplicateObjects: DuplicateObjects
) : ViewModel(), AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate {

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
    val uiTypeLayoutsState = MutableStateFlow<UiLayoutTypeState>(UiLayoutTypeState.Hidden)

    //templates header
    val uiTemplatesHeaderState =
        MutableStateFlow<UiTemplatesHeaderState>(UiTemplatesHeaderState.Hidden)
    val uiTemplatesAddIconState =
        MutableStateFlow<UiTemplatesAddIconState>(UiTemplatesAddIconState.Hidden)

    //templates list
    val uiTemplatesListState = MutableStateFlow<UiTemplatesListState>(UiTemplatesListState.Companion.EMPTY)

    //objects header
    val uiObjectsHeaderState = MutableStateFlow<UiObjectsHeaderState>(UiObjectsHeaderState.Companion.EMPTY)
    val uiObjectsAddIconState =
        MutableStateFlow<UiObjectsAddIconState>(UiObjectsAddIconState.Hidden)
    val uiObjectsSettingsIconState =
        MutableStateFlow<UiObjectsSettingsIconState>(UiObjectsSettingsIconState.Hidden)
    val uiMenuState = MutableStateFlow<UiMenuState>(UiMenuState.Companion.EMPTY)

    //objects list
    val uiObjectsListState = MutableStateFlow<UiObjectsListState>(UiObjectsListState.Empty)
    val uiContentState = MutableStateFlow<UiContentState>(UiContentState.Idle())
    private val restartSubscription = MutableStateFlow(0L)
    private var shouldScrollToTopItems = false
    private val _sortState = MutableStateFlow<ObjectsListSort>(ObjectsListSort.ByName())

    //alerts
    val uiAlertState = MutableStateFlow<UiDeleteAlertState>(UiDeleteAlertState.Hidden)

    private val _objTypeState = MutableStateFlow<ObjectWrapper.Type?>(null)
    private val _objectTypePermissionsState = MutableStateFlow<ObjectPermissions?>(null)

    val commands = MutableSharedFlow<ObjectTypeCommand>()
    val errorState = MutableStateFlow<UiErrorState>(UiErrorState.Hidden)

    init {
        viewModelScope.launch {
            //Just for the testing purpose
            userSettingsRepository.setLastOpenedObject(
                id = vmParams.objectId,
                space = vmParams.spaceId
            )
        }
        proceedWithObservingSyncStatus()
        proceedWithObservingObjectType()
        setupObjectsMenuFlow()
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            storelessSubscriptionContainer.unsubscribe(
                subscriptions = listOf("ObjectTypeSubscription-${vmParams.objectId}")
            )
        }
    }

    fun onStart() {
        Timber.d("onStart, vmParams: $vmParams")
        setupSubscriptionToObjects()
        setupSubscriptionToSets()
        setupSubscriptionToTemplates()
        viewModelScope.launch {
            sendAnalyticsScreenObjectType(
                analytics = analytics
            )
        }
    }

    fun onStop() {
        Timber.d("onStop")
        viewModelScope.launch {
            storelessSubscriptionContainer.unsubscribe(
                listOf(
                    "ObjectsListSubscription-${vmParams.objectId}",
                    "ObjectTypeSetByTypeSubscription-${vmParams.objectId}",
                    "ObjectTypeTemplatesSubscription-${vmParams.objectId}"
                )
            )
        }
        uiObjectsListState.value = UiObjectsListState.Empty
    }

    //region SETUP
    private fun setupObjectsMenuFlow() {
        viewModelScope.launch {
            _sortState.collect { sort ->
                val container = MenuSortsItem.Container(sort = sort)
                val uiSorts = listOf(
                    MenuSortsItem.Sort(
                        sort = ObjectsListSort.ByDateUpdated(isSelected = sort is ObjectsListSort.ByDateUpdated)
                    ),
                    MenuSortsItem.Sort(
                        sort = ObjectsListSort.ByDateCreated(isSelected = sort is ObjectsListSort.ByDateCreated)
                    ),
                    MenuSortsItem.Sort(
                        sort = ObjectsListSort.ByName(isSelected = sort is ObjectsListSort.ByName)
                    )
                )
                val uiSortTypes = listOf(
                    MenuSortsItem.SortType(
                        sort = sort,
                        sortType = DVSortType.ASC,
                        isSelected = sort.sortType == DVSortType.ASC
                    ),
                    MenuSortsItem.SortType(
                        sort = sort,
                        sortType = DVSortType.DESC,
                        isSelected = sort.sortType == DVSortType.DESC
                    )
                )
                uiMenuState.value = uiMenuState.value.copy(
                    container = container,
                    sorts = uiSorts,
                    types = uiSortTypes,
                )
            }
        }
    }

    private fun proceedWithObservingObjectType() {
        viewModelScope.launch {
            combine(
                storelessSubscriptionContainer.subscribe(
                    StoreSearchByIdsParams(
                        targets = listOf(vmParams.objectId),
                        subscription = "ObjectTypeSubscription-${vmParams.objectId}",
                        keys = defaultTypeKeys,
                        space = vmParams.spaceId
                    )
                ),
                userPermissionProvider.observe(space = vmParams.spaceId)
            ) { objWrapper, permission ->
                objWrapper to permission
            }.catch {
                Timber.e(it, "Error while observing object")
                _objTypeState.value = null
                errorState.value =
                    UiErrorState.Show(UiErrorState.Reason.ErrorGettingObjects(it.message ?: ""))
            }
                .collect { (objWrapper, permission) ->
                    if (permission != null) {

                        if (objWrapper.isNotEmpty()) {

                            val objType = ObjectWrapper.Type(objWrapper[0].map)

                            _objTypeState.value = objType

                            val objectPermissions = objType.toObjectPermissionsForTypes(
                                participantCanEdit = permission.isOwnerOrEditor()
                            )
                            _objectTypePermissionsState.value = objectPermissions

                            if (!objectPermissions.canCreateTemplatesForThisType) {
                                uiTemplatesHeaderState.value = UiTemplatesHeaderState.Hidden
                                uiTemplatesListState.value = UiTemplatesListState.Companion.EMPTY
                                uiTemplatesAddIconState.value = UiTemplatesAddIconState.Hidden
                            }

                            uiTitleState.value = UiTitleState(
                                title = fieldParser.getObjectName(objectWrapper = objType),
                                isEditable = objectPermissions.canEditDetails
                            )
                            uiIconState.value = UiIconState(
                                icon = objType.objectIcon(urlBuilder),
                                isEditable = objectPermissions.canEditDetails
                            )
                            //todo некоторые параметры меню зависят от настроек доступа - но не от всех, например создание Сета запрещено для Viewers но разрешено для Owners + Files(хотя и есть ObjectRestriction.Details)
                            if (objectPermissions.canCreateObjectThisType) {
                                uiObjectsAddIconState.value = UiObjectsAddIconState.Visible
                            }
                            uiObjectsSettingsIconState.value = UiObjectsSettingsIconState.Visible
                            if (objectPermissions.canDelete) {
                                uiEditButtonState.value = UiEditButton.Visible
                            }
                            val layout = objType.recommendedLayout ?: ObjectType.Layout.BASIC
                            uiLayoutButtonState.value = UiLayoutButtonState.Visible(layout = layout)
                            updateDefaultTemplates(
                                defaultTemplate = objType.defaultTemplateId
                            )
                        } else {
                            _objTypeState.value = null
                            errorState.value =
                                UiErrorState.Show(UiErrorState.Reason.ErrorGettingObjects("Type details are empty"))
                        }
                    }
                }
        }
    }

    private fun updateDefaultTemplates(defaultTemplate: Id?) {
        val templates = uiTemplatesListState.value.items
        uiTemplatesListState.value = uiTemplatesListState.value.copy(
            templates.map { template ->
                when (template) {
                    is TemplateView.Blank -> template
                    is TemplateView.New -> template
                    is TemplateView.Template -> {
                        template.copy(isDefault = template.id == defaultTemplate)
                    }
                }
            }
        )
    }

    private fun proceedWithObservingSyncStatus() {
        viewModelScope.launch {
            spaceSyncAndP2PStatusProvider
                .observe()
                .catch { Timber.e(it, "Error while observing sync status") }
                .collect { syncAndP2pState ->
                    Timber.d("Sync status: $syncAndP2pState")
                    uiSyncStatusBadgeState.value = UiSyncStatusBadgeState.Visible(syncAndP2pState)
                    val state = uiSyncStatusWidgetState.value
//                    uiSyncStatusWidgetState.value = when (state) {
//                        UiSyncStatusWidgetState.Hidden -> UiSyncStatusWidgetState.Hidden
//                        is UiSyncStatusWidgetState.Visible -> state.copy(
//                            status = syncAndP2pState.toSyncStatusWidgetState()
//                        )
//                    }
                }
        }
    }
    //endregion

    //region Objects subscription
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun setupSubscriptionToObjects() {
        viewModelScope.launch {
            combine(
                restartSubscription,
                _objTypeState,
                _objectTypePermissionsState
            ) { restart, objType, permission ->
                objType to permission
            }.flatMapLatest { (objType, permission) ->
                if (objType == null || permission == null) {
                    emptyFlow()
                } else {
                    loadData(
                        typeName = objType.name.orEmpty(),
                        permissions = permission
                    ).map { items ->
                        items to permission
                    }
                }
            }.catch {
                Timber.e(it, "Error while observing objects")
                errorState.value =
                    UiErrorState.Show(UiErrorState.Reason.ErrorGettingObjects(it.message ?: ""))
            }.collect { (items, permission) ->
                if (items.isEmpty()) {
                    uiObjectsListState.value = UiObjectsListState.Empty
                    uiContentState.value = UiContentState.Idle()
                    uiObjectsHeaderState.value = UiObjectsHeaderState(
                        count = "0"
                    )
                    uiObjectsSettingsIconState.value = UiObjectsSettingsIconState.Visible
                } else {
                    uiContentState.value = UiContentState.Idle(
                        scrollToTop = shouldScrollToTopItems.also {
                            shouldScrollToTopItems = false
                        }
                    )
                    uiObjectsListState.value = UiObjectsListState(items = items)
                    uiObjectsHeaderState.value = UiObjectsHeaderState(
                        count = "${items.size}"
                    )
                    uiObjectsSettingsIconState.value = UiObjectsSettingsIconState.Visible
                }
                if (permission.canCreateObjectThisType) {
                    uiObjectsAddIconState.value = UiObjectsAddIconState.Visible
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun setupSubscriptionToSets() {
        viewModelScope.launch {
            _objectTypePermissionsState
                .flatMapLatest { permissions ->
                    if (permissions != null) {
                        loadSets().map {
                            it to permissions
                        }
                    } else {
                        emptyFlow()
                    }
                }.collect { (items, permissions) ->
                    Timber.d("items: $items, permissions: $permissions")
                    if (!permissions.participantCanEdit) {
                        if (items.isEmpty()) {
                            uiMenuState.value = uiMenuState.value.copy(
                                objSetItem = UiMenuSetItem.Hidden
                            )
                        } else {
                            uiMenuState.value = uiMenuState.value.copy(
                                objSetItem = UiMenuSetItem.OpenSet(setId = items[0].id)
                            )
                        }
                    } else {
                        if (items.isEmpty()) {
                            uiMenuState.value = uiMenuState.value.copy(
                                objSetItem = UiMenuSetItem.CreateSet
                            )
                        } else {
                            uiMenuState.value = uiMenuState.value.copy(
                                objSetItem = UiMenuSetItem.OpenSet(setId = items[0].id)
                            )
                        }
                    }
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun setupSubscriptionToTemplates() {
        viewModelScope.launch {
            _objectTypePermissionsState
                .flatMapLatest { permissions ->
                    if (permissions != null && permissions.canCreateTemplatesForThisType) {
                        loadTemplates(typeId = vmParams.objectId).map {
                            it to permissions
                        }
                    } else {
                        emptyFlow()
                    }
                }.collect { (templates, permissions) ->
                    uiTemplatesHeaderState.value = UiTemplatesHeaderState.Visible(
                        count = "${templates.size}"
                    )
                    val updated = templates.map { template ->
                        when (template) {
                            is TemplateView.Blank -> template
                            is TemplateView.New -> template
                            is TemplateView.Template -> {
                                template.copy(isDefault = template.id == _objTypeState.value?.defaultTemplateId)
                            }
                        }
                    }
                    val result = buildList<TemplateView> {
                        addAll(updated)
                        if (permissions.participantCanEdit) {
                            add(
                                TemplateView.New(
                                    targetTypeId = TypeId(vmParams.objectId),
                                    targetTypeKey = TypeKey(vmParams.objectId)
                                )
                            )
                            uiTemplatesAddIconState.value = UiTemplatesAddIconState.Visible
                        }
                    }
                    uiTemplatesListState.value = UiTemplatesListState(
                        items = result
                    )
                }
        }
    }

    private fun loadData(
        typeName: String,
        permissions: ObjectPermissions
    ): Flow<List<UiObjectsListItem>> {

        val activeSort = _sortState.value

        val searchParams = StoreSearchParams(
            filters = filtersForSearch(objectTypeId = vmParams.objectId),
            sorts = listOf(activeSort.toDVSort()),
            space = vmParams.spaceId,
            limit = 20,
            keys = defaultKeys,
            subscription = "ObjectsListSubscription-${vmParams.objectId}"
        )

        return storelessSubscriptionContainer.subscribe(searchParams)
            .onStart {
                uiContentState.value = UiContentState.Paging
            }
            .map { objWrappers ->
                val items = objWrappers.map {
                    it.toUiObjectsListItem(
                        space = vmParams.spaceId,
                        urlBuilder = urlBuilder,
                        typeName = typeName,
                        fieldParser = fieldParser,
                        isOwnerOrEditor = permissions.participantCanEdit
                    )
                }
                items
            }.catch { e ->
                handleError(e)
            }
    }

    private fun loadSets(): Flow<List<ObjectWrapper.Basic>> {

        val searchParams = StoreSearchParams(
            filters = filtersForSetsSearch(objectTypeId = vmParams.objectId),
            sorts = listOf(sortForSetSearch()),
            space = vmParams.spaceId,
            limit = 1,
            keys = defaultKeys,
            subscription = "ObjectTypeSetByTypeSubscription-${vmParams.objectId}"
        )

        return storelessSubscriptionContainer.subscribe(searchParams)
            .catch {
                handleError(it)
                emit(emptyList())
            }
    }

    private suspend fun loadTemplates(typeId: Id): Flow<List<TemplateView>> {

        val searchParams = StoreSearchParams(
            filters = filtersForTemplatesSearch(objectTypeId = vmParams.objectId),
            sorts = listOf(sortForTemplatesSearch()),
            space = vmParams.spaceId,
            limit = 200,
            keys = defaultKeys,
            subscription = "ObjectTypeTemplatesSubscription-${vmParams.objectId}"
        )

        return storelessSubscriptionContainer.subscribe(searchParams).map { templates ->
            templates.map {
                it.toTemplateView(
                    objectId = vmParams.objectId,
                    urlBuilder = urlBuilder,
                    coverImageHashProvider = coverImageHashProvider,
                )
            }
        }

        templatesContainer.subscribeToTemplates(
            type = typeId,
            space = vmParams.spaceId,
            subscription = "${vmParams.objectId}$SUBSCRIPTION_TEMPLATES_ID"
        ).catch {
            Timber.e(it, "Error while observing templates")
        }.collect { templates ->
            templates.map { objWrapper ->
                objWrapper.toTemplateView(
                    objectId = vmParams.objectId,
                    urlBuilder = urlBuilder,
                    coverImageHashProvider = coverImageHashProvider,
                )
            }
        }

    }

    private fun handleError(e: Throwable) {
//        uiContentState.value = UiContentState.(
//            message = e.message ?: "An error occurred while loading data."
//        )
    }
    //endregion

    //region Ui STATE
    fun hideError() {
        errorState.value = UiErrorState.Hidden
    }
    //endregion

    //region Ui ACTIONS
    fun onTypeEvent(event: TypeEvent) {
        when (event) {
            TypeEvent.OnFieldsButtonClick -> TODO()
            TypeEvent.OnLayoutButtonClick -> {
                val permissions = _objectTypePermissionsState.value
                if (permissions?.canChangeRecommendedLayoutForThisType == true) {
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

            is TypeEvent.OnSortClick -> onSortClicked(event.sort)
            TypeEvent.OnObjectsSettingsIconClick -> {
            }

            TypeEvent.OnCreateSetClick -> {
                onCreateSet()
            }

            is TypeEvent.OnOpenSetClick -> {
                proceedWithNavigation(
                    objectId = event.setId,
                    objectLayout = ObjectType.Layout.SET
                )
            }

            TypeEvent.OnCreateObjectIconClick -> {
                shouldScrollToTopItems = true
                proceedWithCreateObjectOfThisType()
            }

            TypeEvent.OnMenuItemDeleteClick -> {
                uiAlertState.value = UiDeleteAlertState.Show
            }

            TypeEvent.OnAlertDeleteConfirm -> {
                uiAlertState.value = UiDeleteAlertState.Hidden
                onDeletionObjectTypeAccepted()
            }

            TypeEvent.OnAlertDeleteDismiss -> {
                uiAlertState.value = UiDeleteAlertState.Hidden
            }

            is TypeEvent.OnObjectItemClick -> {
                when (event.item) {
                    is UiObjectsListItem.Item -> {
                        proceedWithNavigation(
                            objectId = event.item.id,
                            objectLayout = event.item.layout
                        )
                    }

                    is UiObjectsListItem.Loading -> {
                        //do nothing
                    }
                }
            }

            TypeEvent.OnObjectTypeIconClick -> {
                viewModelScope.launch{
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

            is TypeEvent.OnTemplateMenuDeleteClick -> {
                proceedWithTemplateDelete(
                    template = event.item.id
                )
            }
            is TypeEvent.OnTemplateMenuDuplicateClick -> {
                proceedWithTemplateDuplicate(
                    template = event.item.id
                )
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

    fun onSortClicked(sort: ObjectsListSort) {
        Timber.d("onSortClicked: $sort")
        val newSort = when (sort) {
            is ObjectsListSort.ByDateCreated -> {
                sort.copy(isSelected = true)
            }

            is ObjectsListSort.ByDateUpdated -> {
                sort.copy(isSelected = true)
            }

            is ObjectsListSort.ByName -> {
                sort.copy(isSelected = true)
            }

            is ObjectsListSort.ByDateUsed -> {
                sort.copy(isSelected = true)
            }
        }
        shouldScrollToTopItems = true
        //uiItemsState.value = UiItemsState.Empty
        _sortState.value = newSort
        //proceedWithSortSaving(uiTabsState.value, newSort)
        restartSubscription.value++
//        viewModelScope.launch {
//            sendAnalyticsAllContentChangeSort(
//                analytics = analytics,
//                type = sort.toAnalyticsSortType().first,
//                sort = sort.toAnalyticsSortType().second
//            )
//        }
    }

    private fun onDeletionObjectTypeAccepted() {
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

    private fun onCreateSet() {
        val typeName = _objTypeState.value?.name.orEmpty()
        val emoji = _objTypeState.value?.iconEmoji.orNull()
        val params = CreateObjectSet.Params(
            space = vmParams.spaceId.id,
            type = vmParams.objectId,
            details = mapOf(
                Relations.NAME to "${stringResourceProvider.getSetOfObjectsTitle()} $typeName",
                Relations.ICON_EMOJI to emoji
            )
        )
        viewModelScope.launch {
            createObjectSet.run(params).process(
                failure = {},
                success = { response ->
                    val obj = ObjectWrapper.Basic(response.details)
                    proceedWithNavigation(
                        objectId = obj.id,
                        objectLayout = obj.layout
                    )
                }
            )
        }
    }

    private fun proceedWithCreateObjectOfThisType() {
        val uniqueKeys = _objTypeState.value?.uniqueKey ?: return
        val defaultTemplate =
            uiTemplatesListState.value.items.firstOrNull { it.isDefault } as? TemplateView.Template
        val params = CreateObject.Param(
            space = vmParams.spaceId,
            type = TypeKey(uniqueKeys),
            template = defaultTemplate?.id,
            prefilled = mapOf(
                Relations.ORIGIN to ObjectOrigin.BUILT_IN.code.toDouble()
            )
        )
        viewModelScope.launch {
            createObject.async(params).fold(
                onSuccess = { result ->
                    //todo need to check this logic
//                    proceedWithNavigation(
//                        objectId = result.objectId,
//                        objectLayout = result.obj.layout
//                    )
                },
                onFailure = {
                    Timber.e(it, "Error while creating object")
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

    //region Navigation
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

    companion object {
        private const val SUBSCRIPTION_TEMPLATES_ID = "-SUBSCRIPTION_TEMPLATES_ID"
    }
}