package com.anytypeio.anytype.feature_object_type.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_ui.lists.objects.UiContentState
import com.anytypeio.anytype.core_ui.lists.objects.UiObjectsListState
import com.anytypeio.anytype.domain.event.interactor.SpaceSyncAndP2PStatusProvider
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.`object`.OpenObject
import com.anytypeio.anytype.domain.objects.ObjectWatcher
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.relations.GetObjectRelationListById
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.extension.sendAnalyticsScreenObjectType
import com.anytypeio.anytype.presentation.mapper.objectIcon
import com.anytypeio.anytype.presentation.objects.UiObjectsListItem
import com.anytypeio.anytype.presentation.objects.toUiObjectsListItem
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants.defaultKeys
import com.anytypeio.anytype.presentation.sync.SyncStatusWidgetState
import com.anytypeio.anytype.presentation.templates.ObjectTypeTemplatesContainer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Fragment: @see [ObjectTypeFragment]
 * Factory: @see [ObjectTypeVMFactory]
 * Models: @see [ObjectViewState]
 */
class ObjectTypeViewModel(
    private val vmParams: ObjectTypeVmParams,
    private val openObject: OpenObject,
    private val objectWatcher: ObjectWatcher,
    private val analytics: Analytics,
    private val urlBuilder: UrlBuilder,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val userPermissionProvider: UserPermissionProvider,
    private val getObjectRelationListById: GetObjectRelationListById,
    private val storeOfRelations: StoreOfRelations,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val storelessSubscriptionContainer: StorelessSubscriptionContainer,
    private val dateProvider: DateProvider,
    private val spaceSyncAndP2PStatusProvider: SpaceSyncAndP2PStatusProvider,
    private val createObject: CreateObject,
    private val fieldParser: FieldParser,
    private val setObjectListIsArchived: SetObjectListIsArchived,
    private val templatesContainer: ObjectTypeTemplatesContainer,
    private val coverImageHashProvider: CoverImageHashProvider
) : ViewModel(), AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate {

    //sync status
    val uiSyncStatusWidgetState =
        MutableStateFlow<SyncStatusWidgetState>(SyncStatusWidgetState.Hidden)
    val uiSyncStatusBadgeState =
        MutableStateFlow<UiSyncStatusBadgeState>(UiSyncStatusBadgeState.Hidden)

    //header
    val uiTitleState = MutableStateFlow<UiTitleState>(UiTitleState.EMPTY)
    val uiIconState = MutableStateFlow<UiIconState>(UiIconState.EMPTY)

    //layout and fields buttons
    val uiFieldsButtonState = MutableStateFlow<UiFieldsButtonState>(UiFieldsButtonState.Hidden)
    val uiLayoutButtonState = MutableStateFlow<UiLayoutButtonState>(UiLayoutButtonState.Hidden)

    //templates header
    val uiTemplatesHeaderState = MutableStateFlow<UiTemplatesHeaderState>(UiTemplatesHeaderState.EMPTY)
    val uiTemplatesAddIconState = MutableStateFlow<UiTemplatesAddIconState>(UiTemplatesAddIconState.Hidden)

    //templates list
    val uiTemplatesListState = MutableStateFlow<UiTemplatesListState>(UiTemplatesListState.EMPTY)

    //objects header
    val uiObjectsHeaderState = MutableStateFlow<UiObjectsHeaderState>(UiObjectsHeaderState.EMPTY)
    val uiObjectsAddIconState = MutableStateFlow<UiObjectsAddIconState>(UiObjectsAddIconState.Hidden)
    val uiObjectsSettingsIconState = MutableStateFlow<UiObjectsSettingsIconState>(UiObjectsSettingsIconState.Hidden)

    //objects list
    val uiObjectsListState = MutableStateFlow<UiObjectsListState>(UiObjectsListState.Empty)
    val uiContentState = MutableStateFlow<UiContentState>(UiContentState.Idle())

    //todo update to ObjectWrapper.Type in feature
    private val _detailsState = MutableStateFlow<ObjectWrapper.Basic?>(null)

    val uiEditButtonState = MutableStateFlow<UiEditButton>(UiEditButton.Hidden)


    val commands = MutableSharedFlow<ObjectTypeCommand>()
    val errorState = MutableStateFlow<UiErrorState>(UiErrorState.Hidden)

    private val _permission = MutableStateFlow(userPermissionProvider.get(vmParams.spaceId))

    init {
        proceedWithObservingSyncStatus()
        proceedWithObservingPermissions()
        proceedWithObservingObjectType()
        proceedWithObservingObjectDetails()
        proceedWithObservingTemplates()
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            objectWatcher.unwatch(
                target = vmParams.objectId,
                space = vmParams.spaceId
            )
        }
    }

    fun onStart() {
        Timber.d("onStart")
        proceedWithGettingObjects()
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
                    "ObjectsListSubscription-${vmParams.objectId}"
                )
            )
        }
        uiObjectsListState.value = UiObjectsListState.Empty
    }

    //region Initialization
    private fun proceedWithGettingObjects() {
        viewModelScope.launch {
            _detailsState.collect { objType ->
                if (objType?.isValid == true) {
                    val searchParams = StoreSearchParams(
                        filters = filtersForSearch(objectTypeId = vmParams.objectId),
                        space = vmParams.spaceId,
                        limit = 20,
                        keys = defaultKeys,
                        subscription = "ObjectsListSubscription-${vmParams.objectId}"
                    )
                    storelessSubscriptionContainer.subscribe(
                        searchParams = searchParams
                    ).catch {
                        Timber.e(it, "Error while observing objects")
                    }.collect { objects ->
                        val items = objects.map {
                            it.toUiObjectsListItem(
                                space = vmParams.spaceId,
                                urlBuilder = urlBuilder,
                                typeName = objType.name,
                                fieldParser = fieldParser,
                                isOwnerOrEditor = _permission.value?.isOwnerOrEditor() == true
                            )
                        }
                        if (items.isEmpty()) {
                            uiObjectsHeaderState.value = UiObjectsHeaderState.EMPTY
                            uiObjectsSettingsIconState.value = UiObjectsSettingsIconState.Hidden
                            uiObjectsAddIconState.value = UiObjectsAddIconState.Visible
                        } else {
                            uiObjectsHeaderState.value = UiObjectsHeaderState(
                                count = "${items.size}"
                            )
                            uiObjectsSettingsIconState.value = UiObjectsSettingsIconState.Visible
                            uiObjectsAddIconState.value = UiObjectsAddIconState.Visible
                        }
                        uiObjectsListState.value = UiObjectsListState(items = items)
                    }
                }
            }
        }
    }

    private fun proceedWithObservingTemplates() {
        viewModelScope.launch {
            templatesContainer.subscribeToTemplates(
                type = vmParams.objectId,
                space = vmParams.spaceId,
                subscription = "${vmParams.objectId}$SUBSCRIPTION_TEMPLATES_ID"
            ).catch {
                Timber.e(it, "Error while observing templates")
            }.collect { templates ->
                val views = templates.map {
                    it.toTemplateView(
                        objectId = vmParams.objectId,
                        urlBuilder = urlBuilder,
                        coverImageHashProvider = coverImageHashProvider,
                    )
                }
                Timber.d("Templates: ${templates.size}")
                uiTemplatesHeaderState.value = UiTemplatesHeaderState(
                    count = "${views.size}"
                )
                uiTemplatesListState.value = UiTemplatesListState(
                    items = views
                )
            }
        }
    }

    private fun proceedWithObservingObjectDetails() {
        viewModelScope.launch {
            combine(
                _permission,
                _detailsState
            ) { permission, details ->
                permission to details
            }.collect { (permission, state) ->
                val isOwnerOrEditor = permission?.isOwnerOrEditor() == true
                if (state?.isValid == true) {
                    uiTitleState.value = UiTitleState(
                        title = fieldParser.getObjectName(objectWrapper = state),
                        isEditable = isOwnerOrEditor
                    )
                    uiIconState.value = UiIconState(
                        icon = state.objectIcon(urlBuilder),
                        isEditable = isOwnerOrEditor
                    )
                    if (isOwnerOrEditor) {
                        uiEditButtonState.value = UiEditButton.Visible
                    }
                    val layout = state.layout
                    if (layout != null) {
                        uiLayoutButtonState.value = UiLayoutButtonState.Visible(layout = layout)
                    } else {
                        uiLayoutButtonState.value = UiLayoutButtonState.Hidden
                    }
                }
            }
        }
    }

    private fun proceedWithObservingObjectType() {
        viewModelScope.launch {
            objectWatcher.watch(
                target = vmParams.objectId,
                space = vmParams.spaceId
            ).catch {
                Timber.e(it, "Error while observing object")
                _detailsState.value = null
                errorState.value =
                    UiErrorState.Show(UiErrorState.Reason.ErrorGettingObjects(it.message ?: ""))
            }
                .collect { result ->
                    Timber.d("Object: $result")
                    val objDetailsStruct =
                        result.details.getOrDefault(vmParams.objectId, emptyMap())
                    val objDetails = ObjectWrapper.Basic(objDetailsStruct)
                    _detailsState.value = objDetails
                }
        }
    }

    private fun proceedWithObservingPermissions() {
        viewModelScope.launch {
            userPermissionProvider
                .observe(space = vmParams.spaceId)
                .collect { result ->
                    _permission.value = result
                }
        }
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

    //region Ui Actions
    fun closeObject() {
        viewModelScope.launch {
            commands.emit(ObjectTypeCommand.Back)
        }
    }
    //endregion

    //region Ui State
    fun hideError() {
        errorState.value = UiErrorState.Hidden
    }
    //endregion

    companion object {
        private const val SUBSCRIPTION_TEMPLATES_ID = "-SUBSCRIPTION_TEMPLATES_ID"
    }
}