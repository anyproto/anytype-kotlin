package com.anytypeio.anytype.presentation.sets

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.analytics.base.EventsDictionary
import com.anytypeio.anytype.analytics.base.EventsPropertiesKey
import com.anytypeio.anytype.analytics.base.sendEvent
import com.anytypeio.anytype.analytics.props.Props
import com.anytypeio.anytype.analytics.props.Props.Companion.OBJ_TYPE_CUSTOM
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DVViewerCardSize
import com.anytypeio.anytype.core_models.DVViewerType
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectTypeIds
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SupportedLayouts.getCreateObjectLayouts
import com.anytypeio.anytype.core_models.TimeInMillis
import com.anytypeio.anytype.core_models.isDataView
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.multiplayer.SpaceSyncAndP2PStatusState
import com.anytypeio.anytype.core_models.multiplayer.SpaceUxType
import com.anytypeio.anytype.core_models.permissions.layoutsSupportsEmojiAndImages
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TypeId
import com.anytypeio.anytype.core_models.primitives.TypeKey
import com.anytypeio.anytype.core_models.restrictions.DataViewRestriction
import com.anytypeio.anytype.core_models.restrictions.ObjectRestriction
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.core_utils.ext.cancel
import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.domain.base.fold
import com.anytypeio.anytype.domain.block.interactor.CreateBlock
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.collections.AddObjectToCollection
import com.anytypeio.anytype.domain.collections.RemoveObjectFromCollection
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.domain.dataview.SetDataViewProperties
import com.anytypeio.anytype.domain.dataview.interactor.CreateDataViewObject
import com.anytypeio.anytype.domain.error.Error
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.event.interactor.SpaceSyncAndP2PStatusProvider
import com.anytypeio.anytype.domain.launch.GetDefaultObjectType
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.DeepLinkResolver
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.SpaceViewSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.`object`.ConvertObjectToCollection
import com.anytypeio.anytype.domain.`object`.DuplicateObjects
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.objects.getTypeOfObject
import com.anytypeio.anytype.domain.page.CloseObject
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.primitives.FieldParser
import com.anytypeio.anytype.domain.search.DataViewState
import com.anytypeio.anytype.domain.search.DataViewSubscriptionContainer
import com.anytypeio.anytype.domain.sets.OpenObjectSet
import com.anytypeio.anytype.domain.sets.SetQueryToObjectSet
import com.anytypeio.anytype.domain.templates.CreateTemplate
import com.anytypeio.anytype.domain.unsplash.DownloadUnsplashImage
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.emojifier.data.EmojiProvider
import com.anytypeio.anytype.emojifier.suggest.EmojiSuggester
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.model.TextUpdate
import com.anytypeio.anytype.presentation.extension.ObjectStateAnalyticsEvent
import com.anytypeio.anytype.presentation.extension.getObject
import com.anytypeio.anytype.presentation.extension.logEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsObjectCreateEvent
import com.anytypeio.anytype.presentation.extension.sendAnalyticsRelationEvent
import com.anytypeio.anytype.presentation.home.HomeScreenViewModel.Companion.HOME_SCREEN_PROFILE_OBJECT_SUBSCRIPTION
import com.anytypeio.anytype.core_models.ui.objectIcon
import com.anytypeio.anytype.presentation.mapper.toTemplateObjectTypeViewItems
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.navigation.NavPanelState
import com.anytypeio.anytype.presentation.navigation.SupportNavigation
import com.anytypeio.anytype.presentation.navigation.leftButtonClickAnalytics
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.presentation.objects.getCreateObjectParams
import com.anytypeio.anytype.presentation.objects.getTypeForObjectAndTargetTypeForTemplate
import com.anytypeio.anytype.presentation.objects.hasLayoutConflict
import com.anytypeio.anytype.presentation.objects.isCreateObjectAllowed
import com.anytypeio.anytype.presentation.objects.isTemplateObject
import com.anytypeio.anytype.presentation.objects.isTemplatesAllowed
import com.anytypeio.anytype.presentation.objects.sortByTypePriority
import com.anytypeio.anytype.presentation.objects.toFeaturedPropertiesViews
import com.anytypeio.anytype.presentation.relations.ObjectRelationView
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig.DEFAULT_LIMIT
import com.anytypeio.anytype.presentation.relations.RelationListViewModel
import com.anytypeio.anytype.presentation.relations.render
import com.anytypeio.anytype.presentation.sets.model.CellView
import com.anytypeio.anytype.presentation.sets.model.Viewer
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.sets.state.ObjectState.Companion.VIEW_DEFAULT_OBJECT_TYPE
import com.anytypeio.anytype.presentation.sets.state.ObjectStateReducer
import com.anytypeio.anytype.presentation.sets.subscription.DataViewSubscription
import com.anytypeio.anytype.presentation.sets.subscription.DefaultDataViewSubscription.Companion.getDataViewSubscriptionId
import com.anytypeio.anytype.presentation.sets.viewer.ViewerDelegate
import com.anytypeio.anytype.presentation.sets.viewer.ViewerEvent
import com.anytypeio.anytype.presentation.sets.viewer.ViewerView
import com.anytypeio.anytype.presentation.sync.SyncStatusWidgetState
import com.anytypeio.anytype.presentation.sync.toSyncStatusWidgetState
import com.anytypeio.anytype.presentation.sync.updateStatus
import com.anytypeio.anytype.presentation.templates.ObjectTypeTemplatesContainer
import com.anytypeio.anytype.presentation.templates.TemplateMenuClick
import com.anytypeio.anytype.presentation.templates.TemplateObjectTypeView
import com.anytypeio.anytype.presentation.templates.TemplateView
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.widgets.TypeTemplatesWidgetUI
import com.anytypeio.anytype.presentation.widgets.TypeTemplatesWidgetUIAction
import com.anytypeio.anytype.presentation.widgets.enterEditing
import com.anytypeio.anytype.presentation.widgets.exitEditing
import com.anytypeio.anytype.presentation.widgets.hideMoreMenu
import com.anytypeio.anytype.presentation.widgets.showMoreMenu
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

@OptIn(ExperimentalCoroutinesApi::class)
class ObjectSetViewModel(
    private val vmParams: Params,
    private val permissions: UserPermissionProvider,
    private val database: ObjectSetDatabase,
    private val openObjectSet: OpenObjectSet,
    private val closeObject: CloseObject,
    private val setObjectDetails: UpdateDetail,
    private val downloadUnsplashImage: DownloadUnsplashImage,
    private val setDocCoverImage: SetDocCoverImage,
    private val updateText: UpdateText,
    private val createBlock: CreateBlock,
    private val interceptEvents: InterceptEvents,
    private val dispatcher: Dispatcher<Payload>,
    private val delegator: Delegator<Action>,
    private val urlBuilder: UrlBuilder,
    private val coverImageHashProvider: CoverImageHashProvider,
    private val session: ObjectSetSession,
    private val analytics: Analytics,
    private val createDataViewObject: CreateDataViewObject,
    private val createObject: CreateObject,
    private val dataViewSubscriptionContainer: DataViewSubscriptionContainer,
    private val setQueryToObjectSet: SetQueryToObjectSet,
    private val paginator: ObjectSetPaginator,
    private val storeOfRelations: StoreOfRelations,
    private val stateReducer: ObjectStateReducer,
    private val dataViewSubscription: DataViewSubscription,
    private val objectStore: ObjectStore,
    private val addObjectToCollection: AddObjectToCollection,
    private val removeObjectFromCollection: RemoveObjectFromCollection,
    private val objectToCollection: ConvertObjectToCollection,
    private val storeOfObjectTypes: StoreOfObjectTypes,
    private val duplicateObjects: DuplicateObjects,
    private val templatesContainer: ObjectTypeTemplatesContainer,
    private val setObjectListIsArchived: SetObjectListIsArchived,
    private val spaceManager: SpaceManager,
    private val viewerDelegate: ViewerDelegate,
    private val createTemplate: CreateTemplate,
    private val dateProvider: DateProvider,
    private val analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
    private val spaceSyncAndP2PStatusProvider: SpaceSyncAndP2PStatusProvider,
    private val fieldParser: FieldParser,
    private val spaceViews: SpaceViewSubscriptionContainer,
    private val deepLinkResolver: DeepLinkResolver,
    private val setDataViewProperties: SetDataViewProperties,
    private val emojiProvider: EmojiProvider,
    private val emojiSuggester: EmojiSuggester,
    private val getDefaultObjectType: GetDefaultObjectType
) : ViewModel(), SupportNavigation<EventWrapper<AppNavigation.Command>>,
    ViewerDelegate by viewerDelegate,
    AnalyticSpaceHelperDelegate by analyticSpaceHelperDelegate
{

    val permission = MutableStateFlow<SpaceMemberPermissions?>(SpaceMemberPermissions.NO_PERMISSIONS)

    private val isOwnerOrEditor get() = permission.value?.isOwnerOrEditor() ==  true

    val error = MutableStateFlow<String?>(null)

    val featured = MutableStateFlow<BlockView.FeaturedRelation?>(null)

    override val navigation = MutableLiveData<EventWrapper<AppNavigation.Command>>()

    private val titleUpdateChannel = Channel<TextUpdate>()

    private val defaultPayloadConsumer: suspend (Payload) -> Unit = { payload ->
        stateReducer.dispatch(payload.events)
    }

    val pagination get() = paginator.pagination

    private val jobs = mutableListOf<Job>()

    private val _commands = MutableSharedFlow<ObjectSetCommand>(replay = 0)
    val commands: SharedFlow<ObjectSetCommand> = _commands
    val toasts = MutableSharedFlow<String>(replay = 0)

    private val _currentViewer: MutableStateFlow<DataViewViewState> =
        MutableStateFlow(DataViewViewState.Init)
    val currentViewer = _currentViewer

    private val _dvViews = MutableStateFlow<List<ViewerView>>(emptyList())

    private val _header = MutableStateFlow<SetOrCollectionHeaderState>(
        SetOrCollectionHeaderState.None
    )
    val header: StateFlow<SetOrCollectionHeaderState> = _header

    val isCustomizeViewPanelVisible = MutableStateFlow(false)
    val typeTemplatesWidgetState: MutableStateFlow<TypeTemplatesWidgetUI> = MutableStateFlow(TypeTemplatesWidgetUI.Init())
    val viewersWidgetState = MutableStateFlow(ViewersWidgetUi.init())
    val viewerEditWidgetState = MutableStateFlow<ViewerEditWidgetUi>(ViewerEditWidgetUi.Init)
    val viewerLayoutWidgetState = MutableStateFlow(ViewerLayoutWidgetUi.init())
    private val widgetViewerId = MutableStateFlow<String?>(null)
    val isTitleToolbarVisible = MutableStateFlow(false)

    @Deprecated("could be deleted")
    val isLoading = MutableStateFlow(false)

    private val selectedTypeFlow: MutableStateFlow<ObjectWrapper.Type?> = MutableStateFlow(null)


    private val pendingScrollToObject = MutableStateFlow<Id?>(null)

    /**
     * State for the Set Object Name bottom sheet.
     */
    data class SetObjectNameState(
        val isVisible: Boolean = false,
        val targetObjectId: Id? = null,
        val currentIcon: ObjectIcon = ObjectIcon.None,
        val inputText: String = "",
        val isIconChangeAllowed: Boolean = false,
        val targetBlockId: Id? = null  // For Note objects, stores the blockId
    )

    private val _setObjectNameState = MutableStateFlow(SetObjectNameState())
    val setObjectNameState: StateFlow<SetObjectNameState> = _setObjectNameState.asStateFlow()

    val navPanelState = permission.map { permission ->
        NavPanelState.fromPermission(
            permission = permission,
            spaceUxType = spaceViews.get(space = vmParams.space)?.spaceUxType ?: SpaceUxType.DATA,
        )
    }

    init {
        Timber.i("ObjectSetViewModel, init")

        proceedWIthObservingPermissions()

        viewModelScope.launch {
            stateReducer.state
                .filterIsInstance<ObjectState.DataView>()
                .distinctUntilChanged()
                .combine(permission) { state, permission ->
                    state to permission
                }
                .collectLatest { (state, permission) ->
                    val featuredBlock = toFeaturedPropertiesViews(
                        objectId = vmParams.ctx,
                        urlBuilder = urlBuilder,
                        fieldParser = fieldParser,
                        storeOfObjectTypes = storeOfObjectTypes,
                        storeOfRelations = storeOfRelations,
                        blocks = state.blocks,
                        details = state.details,
                        participantCanEdit = permission?.isOwnerOrEditor() == true
                    )
                    featured.value = featuredBlock
                    _header.value = state.header(
                        ctx = vmParams.ctx,
                        urlBuilder = urlBuilder,
                        coverImageHashProvider = coverImageHashProvider,
                        isReadOnlyMode = permission == SpaceMemberPermissions.NO_PERMISSIONS || permission == SpaceMemberPermissions.READER
                    )
                    updateLayoutConflictState(featuredBlock = featuredBlock)
                }
        }

        subscribeToObjectState()
        subscribeToDataViewViewer()

        viewModelScope.launch {
            dispatcher.flow().collect { defaultPayloadConsumer(it) }
        }

        viewModelScope.launch {
            dataViewSubscriptionContainer.counter.collect { counter ->
                Timber.d("SET-DB: counter —>\n$counter")
                paginator.total.value = counter.total
            }
        }

        viewModelScope.launch {
            stateReducer.effects.collect { effects ->
                effects.forEach { effect ->
                    Timber.d("Received side effect: $effect")
                }
            }
        }

        viewModelScope.launch { stateReducer.run() }

        // Title updates pipeline

        viewModelScope.launch {
            titleUpdateChannel
                .consumeAsFlow()
                .distinctUntilChanged()
                .map {
                    UpdateText.Params(
                        context = vmParams.ctx,
                        target = it.target,
                        text = it.text,
                        marks = emptyList()
                    )
                }
                .mapLatest { params ->
                    updateText(params).process(
                        failure = { e -> Timber.e(e, "Error while updating title") },
                        success = {
                            Timber.d("Sets' title updated successfully") }
                    )
                }
                .collect()
        }

        viewModelScope.launch {
            delegator.receive().collect { action ->
                when (action) {
                    is Action.SetUnsplashImage -> {
                        proceedWithSettingUnsplashImage(action)
                    }
                    is Action.OpenObject -> proceedWithOpeningObject(
                        target = action.target,
                        space = action.space
                    )
                    is Action.OpenCollection -> proceedWithOpeningObjectCollection(
                        target = action.target,
                        space = action.space
                    )
                    is Action.Duplicate -> proceedWithNavigation(
                        target = action.target,
                        layout = ObjectType.Layout.SET,
                        space = action.space
                    )
                    else -> {}
                }
            }
        }

        viewModelScope.launch {
            combine(
                _dvViews,
                permission
            ) {  views, permissions ->
                views to permissions
            }.collect { (views, permissions) ->
                viewersWidgetState.value = viewersWidgetState.value.copy(
                    items = views,
                    isReadOnly = permissions?.isOwnerOrEditor() != true
                )
            }
        }

        viewModelScope.launch {
            combine(
                widgetViewerId,
                stateReducer.state,
            ) { viewId, state ->
                if (viewId != null) {
                    val dataView = state.dataViewState()
                    val pair = dataView?.viewerAndIndexById(viewId)
                    if (dataView != null && pair != null) {
                        viewerEditWidgetState.value = pair.first.toViewerEditWidgetState(
                            storeOfRelations = storeOfRelations,
                            index = pair.second,
                            session = session
                        )
                        viewerLayoutWidgetState.value = viewerLayoutWidgetState.value.updateState(
                            viewer = pair.first,
                            storeOfRelations = storeOfRelations,
                            relationLinks = dataView.dataViewContent.relationLinks
                        )
                    } else {
                        viewerEditWidgetState.value = ViewerEditWidgetUi.Init
                        viewerLayoutWidgetState.value = viewerLayoutWidgetState.value.empty()
                    }
                }
            }.collect()
        }

        // Observe icon changes for SetObjectNameBottomSheet
        viewModelScope.launch {
            _setObjectNameState
                .filter { it.isVisible && it.targetObjectId != null }
                .flatMapLatest { sheetState ->
                    database.observe(sheetState.targetObjectId!!)
                        .map { obj ->
                            obj.objectIcon(
                                builder = urlBuilder,
                                objType = storeOfObjectTypes.getTypeOfObject(obj)
                            )
                        }
                }
                .distinctUntilChanged()
                .catch {
                    Timber.w(it, "Error while observing object icon")
                }
                .collect { icon ->
                    _setObjectNameState.update { it.copy(currentIcon = icon) }
                }
        }

        subscribeToSelectedType()
        subscribeToSyncTypeRelations()
    }

    /**
     * Syncs type's recommended relations to DataView's relationLinks.
     * This matches desktop's behavior of calling BlockDataviewRelationSet
     * when opening a TypeSet to ensure type relations (like "Done") are available as filter options.
     */
    private fun subscribeToSyncTypeRelations() {
        viewModelScope.launch {
            stateReducer.state
                .filterIsInstance<ObjectState.DataView>()
                .distinctUntilChanged { old, new ->
                    // Skip emissions where isInitialized remains the same.
                    // Combined with the `if (state.isInitialized)` check below,
                    // this ensures we only sync when transitioning to initialized state.
                    // So the emission only passes when isInitialized changes (either direction):
                    // - false → true ✅ passes through
                    // - true → false ✅ passes through
                    // - true → true ❌ filtered out
                    // - false → false ❌ filtered out

                    old.isInitialized == new.isInitialized
                }
                .collect { state ->
                    if (state.isInitialized) {
                        proceedWithSyncingTypeRelations(state)
                    }
                }
        }
    }

    private suspend fun proceedWithSyncingTypeRelations(state: ObjectState.DataView) {
        val typeId = when (state) {
            is ObjectState.DataView.TypeSet -> {
                // For TypeSet, the context is the type itself
                vmParams.ctx
            }
            is ObjectState.DataView.Set,
            is ObjectState.DataView.Collection -> {
                // Sets and Collections don't need type relation syncing
                return
            }
        }

        val type = storeOfObjectTypes.get(typeId) ?: return

        // Build relation keys list: ['name', 'description'] + type's recommended relations
        val typeRelationKeys = type.allRecommendedRelations.mapNotNull { relationId ->
            storeOfRelations.getById(relationId)?.key
        }

        val relationKeys = buildList {
            add(Relations.NAME)
            add(Relations.DESCRIPTION)
            addAll(typeRelationKeys)
        }.distinct()

        Timber.d("Syncing type relations to DataView: $relationKeys")

        setDataViewProperties.async(
            SetDataViewProperties.Params(
                objectId = vmParams.ctx,
                properties = relationKeys
            )
        ).fold(
            onSuccess = { payload ->
                Timber.d("Successfully synced type relations to DataView")
                dispatcher.send(payload)
            },
            onFailure = { error ->
                Timber.e(error, "Failed to sync type relations to DataView")
            }
        )
    }

    private fun proceedWIthObservingPermissions() {
        viewModelScope.launch {
            permissions
                .observe(vmParams.space)
                .collect {
                    permission.value = it
                }
        }
    }

    private suspend fun proceedWithSettingUnsplashImage(
        action: Action.SetUnsplashImage
    ) {
        downloadUnsplashImage(
            DownloadUnsplashImage.Params(
                picture = action.img,
                space = vmParams.space
            )
        ).process(
            failure = {
                Timber.e(it, "Error while download unsplash image")
            },
            success = { hash ->
                setDocCoverImage(
                    SetDocCoverImage.Params.FromHash(
                        context = vmParams.ctx,
                        hash = hash
                    )
                ).process(
                    failure = {
                        Timber.e(it, "Error while setting unsplash image")
                    },
                    success = { payload -> dispatcher.send(payload) }
                )
            }
        )
    }

    private suspend fun updateLayoutConflictState(
        featuredBlock: BlockView.FeaturedRelation?,
    ) {
        val state = stateReducer.state.value.dataViewState() ?: return
        val objectWrapper = state.details.getObject(vmParams.ctx)

        val blocks = featuredBlock?.let { listOf(it) } ?: emptyList()

        val hasConflict = hasLayoutConflict(
            currentObject = objectWrapper,
            blocks = blocks,
            storeOfObjectTypes = storeOfObjectTypes
        )

        dispatcher.send(
            Payload(
                context = vmParams.ctx,
                events = listOf(
                    Event.Command.DataView.UpdateConflictState(
                        context = vmParams.ctx,
                        hasConflict = hasConflict
                    )
                )
            )
        )
    }

    fun onStart(view: Id? = null) {
        Timber.d("onStart, ctx:[${vmParams.ctx}], space:[${vmParams.space}], view:[$view]")
        if (view != null) {
            session.currentViewerId.value = view
        }
        subscribeToEvents(ctx = vmParams.ctx)
        proceedWithOpeningCurrentObject(ctx = vmParams.ctx)
        proceedWithObservingSyncStatus()
    }

    private fun subscribeToEvents(ctx: Id) {
        jobs += viewModelScope.launch {
            interceptEvents
                .build(InterceptEvents.Params(ctx))
                .collect { events -> stateReducer.dispatch(events) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun subscribeToObjectState() {
        Timber.d("subscribeToObjectState, ctx:[${vmParams.ctx}]")
        viewModelScope.launch {
            combine(
                stateReducer.state,
                paginator.offset,
                session.currentViewerId,
            ) { state, offset, view ->
                Query(
                    state = state,
                    offset = offset,
                    currentViewerId = view
                )
            }.flatMapLatest { query  ->
                when (query.state) {
                    is ObjectState.DataView.Collection -> {
                        Timber.d("subscribeToObjectState, NEW COLLECTION STATE")
                        if (query.state.isInitialized) {
                            dataViewSubscription.startObjectCollectionSubscription(
                                space = vmParams.space.id,
                                context = vmParams.ctx,
                                collection = vmParams.ctx,
                                state = query.state,
                                currentViewerId = query.currentViewerId,
                                offset = query.offset,
                                storeOfRelations = storeOfRelations
                            )
                        } else {
                            emptyFlow()
                        }
                    }

                    is ObjectState.DataView.Set -> {
                        Timber.d("subscribeToObjectState, NEW SET STATE")
                        if (query.state.isInitialized) {
                            dataViewSubscription.startObjectSetSubscription(
                                space = vmParams.space.id,
                                context = vmParams.ctx,
                                state = query.state,
                                currentViewerId = query.currentViewerId,
                                offset = query.offset,
                                storeOfRelations = storeOfRelations
                            )
                        } else {
                            emptyFlow()
                        }
                    }

                    is ObjectState.DataView.TypeSet -> {
                        Timber.d("subscribeToObjectState, NEW TYPE SET STATE")
                        if (query.state.isInitialized) {
                            dataViewSubscription.startObjectTypeSetSubscription(
                                space = vmParams.space.id,
                                context = vmParams.ctx,
                                state = query.state,
                                currentViewerId = query.currentViewerId,
                                offset = query.offset,
                                storeOfRelations = storeOfRelations
                            )
                        } else {
                            emptyFlow()
                        }
                    }

                    else -> {
                        Timber.d("subscribeToObjectState, NEW STATE, ${query.state}")
                        emptyFlow()
                    }
                }
            }.onEach { dataViewState ->
                if (dataViewState is DataViewState.Loaded) {
                    Timber.d("subscribeToObjectState, New index size: ${dataViewState.objects.size}")
                }
                database.update(dataViewState)
            }
                .catch { error ->
                    Timber.e("subscribeToObjectState error : $error")
                    _currentViewer.value =
                        DataViewViewState.Error("Error while getting objects:\n${error.message}")
                }
                .collect()
        }
    }

    private fun proceedWithOpeningCurrentObject(ctx: Id) {
        Timber.d("proceedWithOpeningCurrentObject, ctx:[$ctx]")
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            openObjectSet(
                OpenObjectSet.Params(
                    obj = ctx,
                    space = vmParams.space
                )
            ).process(
                success = { result ->
                    when (result) {
                        is Result.Failure -> {
                            when (result.error) {
                                Error.BackwardCompatibility -> {
                                    navigation.postValue(
                                        EventWrapper(AppNavigation.Command.OpenUpdateAppScreen)
                                    )
                                }
                                Error.NotFoundObject -> {
                                    toast(TOAST_SET_NOT_EXIST).also {
                                        dispatch(AppNavigation.Command.Exit(vmParams.space.id))
                                    }
                                }
                            }
                        }
                        is Result.Success -> {
                            Timber.d("proceedWithOpeningCurrentObject, ctx:[$ctx] SUCCESS")
                            defaultPayloadConsumer(result.data)
                            logEvent(
                                state = stateReducer.state.value,
                                analytics = analytics,
                                event = ObjectStateAnalyticsEvent.OPEN_OBJECT,
                                startTime = startTime,
                                currentViewId = session.currentViewerId.value,
                                spaceParams = provideParams(vmParams.space.id)
                            )
                        }
                    }
                },
                failure = {
                    Timber.e(it, "Error while opening object set: $ctx")
                }
            )
        }
    }

    private fun subscribeToDataViewViewer() {
        Timber.d("subscribeToDataViewViewer, START SUBSCRIPTION by ctx:[${vmParams.ctx}]")
        viewModelScope.launch {
            combine(
                database.index,
                stateReducer.state,
                session.currentViewerId,
                permission
            ) { dataViewState, objectState, currentViewId, permission ->
                processViewState(dataViewState, objectState, currentViewId, permission)
            }.distinctUntilChanged().collect { viewState ->
                Timber.d("subscribeToDataViewViewer, newViewerState:[$viewState]")
                _currentViewer.value = viewState
                pendingScrollToObject.value?.let { objectId ->
                    pendingScrollToObject.value = null
                    dispatch(ObjectSetCommand.ScrollToObject(objectId))
                }
            }
        }
    }

    private suspend fun processViewState(
        dataViewState: DataViewState,
        objectState: ObjectState,
        currentViewId: String?,
        permission: SpaceMemberPermissions?
    ): DataViewViewState {
        return when (objectState) {
            is ObjectState.DataView.Collection -> processCollectionState(
                dataViewState = dataViewState,
                objectState = objectState,
                currentViewId = currentViewId,
                permission = permission
            )
            is ObjectState.DataView.Set -> processSetState(
                dataViewState = dataViewState,
                objectState = objectState,
                currentViewId = currentViewId,
                permission = permission
            )
            ObjectState.Init -> DataViewViewState.Init
            ObjectState.ErrorLayout -> DataViewViewState.Error(msg = "Wrong layout, couldn't open object")
            is ObjectState.DataView.TypeSet -> processTypeSetState(
                dataViewState = dataViewState,
                objectState = objectState,
                currentViewId = currentViewId,
                permission = permission
            )
        }
    }

    private suspend fun processCollectionState(
        dataViewState: DataViewState,
        objectState: ObjectState.DataView.Collection,
        currentViewId: String?,
        permission: SpaceMemberPermissions?
    ): DataViewViewState {
        if (!objectState.isInitialized) return DataViewViewState.Init

        val dvViewer = objectState.viewerByIdOrFirst(currentViewId)

        return when (dataViewState) {
            DataViewState.Init -> {
                _dvViews.value = emptyList()
                if (dvViewer == null) {
                    DataViewViewState.Collection.NoView(
                        isCreateObjectAllowed = permission?.isOwnerOrEditor() == true,
                        isEditingViewAllowed = permission?.isOwnerOrEditor() == true
                    )
                } else {
                    DataViewViewState.Init
                }
            }
            is DataViewState.Loaded -> {
                _dvViews.value = objectState.dataViewState()?.toViewersView(
                    ctx = vmParams.ctx,
                    session = session,
                    storeOfRelations = storeOfRelations
                ) ?: emptyList()
                val relations = objectState.dataViewContent.relationLinks.mapNotNull {
                    storeOfRelations.getByKey(it.key)
                }
                val viewer = renderViewer(objectState, dataViewState, dvViewer, relations)

                when {
                    viewer == null -> DataViewViewState.Collection.NoView(
                        isCreateObjectAllowed = permission?.isOwnerOrEditor() == true,
                        isEditingViewAllowed = permission?.isOwnerOrEditor() == true
                    )
                    viewer.isEmpty() -> {
                        val isCreateObjectAllowed = objectState.isCreateObjectAllowed() && permission?.isOwnerOrEditor() == true
                        DataViewViewState.Collection.NoItems(
                            title = viewer.title,
                            isCreateObjectAllowed = isCreateObjectAllowed,
                            isEditingViewAllowed = permission?.isOwnerOrEditor() == true
                        )
                    }
                    else -> {
                        val isCreateObjectAllowed = objectState.isCreateObjectAllowed() && permission?.isOwnerOrEditor() == true
                        DataViewViewState.Collection.Default(
                            viewer = viewer,
                            isCreateObjectAllowed = isCreateObjectAllowed,
                            isEditingViewAllowed = permission?.isOwnerOrEditor() == true
                        )
                    }
                }
            }
        }
    }

    private suspend fun processSetState(
        dataViewState: DataViewState,
        objectState: ObjectState.DataView.Set,
        currentViewId: String?,
        permission: SpaceMemberPermissions?
    ): DataViewViewState {
        if (!objectState.isInitialized) return DataViewViewState.Init

        val setOfValue = objectState.getSetOfValue(ctx = vmParams.ctx)
        val query = objectState.filterOutDeletedAndMissingObjects(query = setOfValue)
        val viewer = objectState.viewerByIdOrFirst(currentViewId)

        return when (dataViewState) {
            DataViewState.Init -> {
                _dvViews.value = emptyList()
                when {
                    setOfValue.isEmpty() || query.isEmpty() -> DataViewViewState.Set.NoQuery(
                        isCreateObjectAllowed = permission?.isOwnerOrEditor() == true,
                        isEditingViewAllowed = permission?.isOwnerOrEditor() == true
                    )
                    viewer == null -> DataViewViewState.Set.NoView(
                        isCreateObjectAllowed = permission?.isOwnerOrEditor() == true,
                        isEditingViewAllowed = permission?.isOwnerOrEditor() == true
                    )
                    else -> DataViewViewState.Init
                }
            }
            is DataViewState.Loaded -> {
                _dvViews.value = objectState.dataViewState()?.toViewersView(
                    ctx = vmParams.ctx,
                    session = session,
                    storeOfRelations = storeOfRelations
                ) ?: emptyList()
                val relations = objectState.dataViewContent.relationLinks.mapNotNull {
                    storeOfRelations.getByKey(it.key)
                }
                val render = viewer?.render(
                    coverImageHashProvider = coverImageHashProvider,
                    builder = urlBuilder,
                    objects = dataViewState.objects,
                    dataViewRelations = relations,
                    store = objectStore,
                    storeOfRelations = storeOfRelations,
                    fieldParser = fieldParser,
                    storeOfObjectTypes = storeOfObjectTypes
                )

                when {
                    query.isEmpty() || setOfValue.isEmpty() -> DataViewViewState.Set.NoQuery(
                        isCreateObjectAllowed = permission?.isOwnerOrEditor() == true,
                        isEditingViewAllowed = permission?.isOwnerOrEditor() == true
                    )
                    render == null -> DataViewViewState.Set.NoView(
                        isCreateObjectAllowed = permission?.isOwnerOrEditor() == true,
                        isEditingViewAllowed = permission?.isOwnerOrEditor() == true
                    )
                    render.isEmpty() -> {
                        val (defType, _) = objectState.getActiveViewTypeAndTemplate(
                            vmParams.ctx, viewer, storeOfObjectTypes
                        )
                        DataViewViewState.Set.NoItems(
                            title = render.title,
                            isCreateObjectAllowed = objectState.isCreateObjectAllowed(defType) && (permission?.isOwnerOrEditor() == true),
                            isEditingViewAllowed = permission?.isOwnerOrEditor() == true
                        )
                    }
                    else -> {
                        val (defType, _) = objectState.getActiveViewTypeAndTemplate(
                            vmParams.ctx, viewer, storeOfObjectTypes
                        )
                        DataViewViewState.Set.Default(
                            viewer = render,
                            isCreateObjectAllowed = objectState.isCreateObjectAllowed(defType) && (permission?.isOwnerOrEditor() == true),
                            isEditingViewAllowed = permission?.isOwnerOrEditor() == true
                        )
                    }
                }
            }
        }
    }

    private suspend fun processTypeSetState(
        dataViewState: DataViewState,
        objectState: ObjectState.DataView.TypeSet,
        currentViewId: String?,
        permission: SpaceMemberPermissions?
    ): DataViewViewState {
        if (!objectState.isInitialized) return DataViewViewState.Init

        val setOfValue = objectState.getSetOfValue(ctx = vmParams.ctx)
        val query = objectState.filterOutDeletedAndMissingObjects(query = setOfValue)
        val viewer = objectState.viewerByIdOrFirst(currentViewId)

        return when (dataViewState) {
            DataViewState.Init -> {
                _dvViews.value = emptyList()
                when {
                    setOfValue.isEmpty() || query.isEmpty() || viewer == null ->
                        DataViewViewState.TypeSet.Error(msg = "Error while rendering viewer")
                    else -> DataViewViewState.Init
                }
            }
            is DataViewState.Loaded -> {
                _dvViews.value = objectState.dataViewState()?.toViewersView(
                    ctx = vmParams.ctx,
                    session = session,
                    storeOfRelations = storeOfRelations
                ) ?: emptyList()
                val relations = objectState.dataViewContent.relationLinks.mapNotNull {
                    storeOfRelations.getByKey(it.key)
                }
                val render = viewer?.render(
                    coverImageHashProvider = coverImageHashProvider,
                    builder = urlBuilder,
                    objects = dataViewState.objects,
                    dataViewRelations = relations,
                    store = objectStore,
                    storeOfRelations = storeOfRelations,
                    fieldParser = fieldParser,
                    storeOfObjectTypes = storeOfObjectTypes
                )

                when {
                    render == null || query.isEmpty() || setOfValue.isEmpty() -> DataViewViewState.TypeSet.Error(
                        msg = "Error while rendering viewer",
                    )
                    render.isEmpty() -> {
                        val (defType, _) = objectState.getActiveViewTypeAndTemplate(
                            vmParams.ctx, viewer, storeOfObjectTypes
                        )
                        DataViewViewState.TypeSet.NoItems(
                            title = render.title,
                            isCreateObjectAllowed = objectState.isCreateObjectAllowed(defType) && (permission?.isOwnerOrEditor() == true),
                            isEditingViewAllowed = permission?.isOwnerOrEditor() == true
                        )
                    }
                    else -> {
                        val (defType, _) = objectState.getActiveViewTypeAndTemplate(
                            vmParams.ctx, viewer, storeOfObjectTypes
                        )
                        DataViewViewState.TypeSet.Default(
                            viewer = render,
                            isCreateObjectAllowed = objectState.isCreateObjectAllowed(defType) && (permission?.isOwnerOrEditor() == true),
                            isEditingViewAllowed = permission?.isOwnerOrEditor() == true
                        )
                    }
                }
            }
        }
    }

    private suspend fun renderViewer(
        objectState: ObjectState.DataView.Collection,
        dataViewState: DataViewState.Loaded,
        dvViewer: DVViewer?,
        relations: List<ObjectWrapper.Relation>
    ): Viewer? {
        return dvViewer?.let {
            val objectOrderIds = objectState.getObjectOrderIds(dvViewer.id)
            it.render(
                coverImageHashProvider = coverImageHashProvider,
                builder = urlBuilder,
                objects = dataViewState.objects,
                dataViewRelations = relations,
                store = objectStore,
                objectOrderIds = objectOrderIds,
                storeOfRelations = storeOfRelations,
                fieldParser = fieldParser,
                storeOfObjectTypes = storeOfObjectTypes
            )
        }
    }

    fun onStop() {
        Timber.d("onStop, ")
        hideTemplatesWidget()
        unsubscribeFromAllSubscriptions()
        jobs.cancel()
    }

    fun onCloseObject() {
        Timber.d("onCloseObject, id:[${vmParams.ctx}]")
        viewModelScope.launch {
            closeObject.async(
                CloseObject.Params(
                    target = vmParams.ctx,
                    space = vmParams.space
                )
            ).fold(
                onSuccess = {
                    Timber.d("Object [${vmParams.ctx}] closed successfully")
                },
                onFailure = {
                    Timber.w(it, "Error while closing object set: ${vmParams.ctx}")
                }
            )
        }
    }

    fun onSystemBackPressed() {
        Timber.d("onSystemBackPressed, ")
        proceedWithClosingAndExit()
    }

    private fun unsubscribeFromAllSubscriptions() {
        viewModelScope.launch {
            val ids = listOf(
                getDataViewSubscriptionId(vmParams.ctx),
                HOME_SCREEN_PROFILE_OBJECT_SUBSCRIPTION,
                "${vmParams.ctx}$SUBSCRIPTION_TEMPLATES_ID"
            )
            dataViewSubscription.unsubscribe(ids)
        }
    }

    private fun proceedWithClosingAndExit() {
        viewModelScope.launch {
            closeObject.async(
                CloseObject.Params(
                    target = vmParams.ctx,
                    space = vmParams.space
                )
            ).fold(
                onSuccess = { dispatch(AppNavigation.Command.Exit(vmParams.space.id)) },
                onFailure = {
                    Timber.e(it, "Error while closing object set: ${vmParams.ctx}").also {
                        dispatch(AppNavigation.Command.Exit(vmParams.space.id))
                    }
                }
            )
        }
    }

    fun onTitleChanged(txt: String) {
        Timber.d("onTitleChanged, txt:[$txt]")

        val target = (header.value as? SetOrCollectionHeaderState.Default)?.title?.id
        if (target != null) {
            viewModelScope.launch {
                titleUpdateChannel.send(
                    TextUpdate.Default(
                        text = txt,
                        target = target,
                        markup = emptyList()
                    )
                )
            }
        } else {
            viewModelScope.launch {
                setObjectDetails(
                    UpdateDetail.Params(
                        target = vmParams.ctx,
                        key = Relations.NAME,
                        value = txt
                    )
                ).process(
                    success = { dispatcher.send(it) },
                    failure = {
                        Timber.e(it, "Error while updating object set name")
                    }
                )
            }
        }
    }

    fun onTitleFocusChanged(hasFocus: Boolean) {
        isTitleToolbarVisible.value = hasFocus
    }

    fun hideTitleToolbar() {
        isTitleToolbarVisible.value = false
    }

    fun onDescriptionChanged(text: String) {
        viewModelScope.launch {
            setObjectDetails(
                UpdateDetail.Params(
                    target = vmParams.ctx,
                    key = Relations.DESCRIPTION,
                    value = text
                )
            ).process(
                failure = {
                    Timber.e(it, "Error while updating description")
                },
                success = defaultPayloadConsumer
            )
        }
    }

    fun onGridCellClicked(cell: CellView) {
        Timber.d("onGridCellClicked, cell:[$cell]")
        if (cell.relationKey == Relations.NAME) return
        val state = stateReducer.state.value.dataViewState() ?: return
        viewModelScope.launch {
            val viewer = state.viewerByIdOrFirst(session.currentViewerId.value)
            val relation = storeOfRelations.getByKey(cell.relationKey)
            if (relation == null) {
                toast("Could not found this relation. Please, try again later.")
                Timber.e("onGridCellClicked, Relation [${cell.relationKey}] is empty")
                return@launch
            }
            if (viewer == null) {
                Timber.e("onGridCellClicked, Viewer is empty")
                return@launch
            }
            when (cell) {
                is CellView.Description,
                is CellView.Number,
                is CellView.Email,
                is CellView.Url,
                is CellView.Phone -> {
                    dispatch(
                        ObjectSetCommand.Modal.EditGridTextCell(
                            ctx = vmParams.ctx,
                            relationKey = cell.relationKey,
                            recordId = cell.id,
                            space = vmParams.space.id
                        )
                    )
                }
                is CellView.Date -> {
                    if (relation.isReadonlyValue)  {
                        val timeInMillis = cell.relativeDate?.initialTimeInMillis
                        handleReadOnlyValue(timeInMillis)
                    } else {
                        dispatch(
                            ObjectSetCommand.Modal.EditGridDateCell(
                                ctx = vmParams.ctx,
                                objectId = cell.id,
                                relationKey = cell.relationKey,
                                space = vmParams.space.id
                            )
                        )
                    }
                }
                is CellView.Tag, is CellView.Status -> {
                    dispatch(
                        ObjectSetCommand.Modal.EditTagOrStatusCell(
                            ctx = vmParams.ctx,
                            target = cell.id,
                            relationKey = cell.relationKey,
                            space = vmParams.space.id
                        )
                    )
                }
                is CellView.Object, is CellView.File -> {
                    if (cell.relationKey != Relations.TYPE) {
                        dispatch(
                            ObjectSetCommand.Modal.EditObjectCell(
                                ctx = vmParams.ctx,
                                target = cell.id,
                                relationKey = cell.relationKey,
                                space = vmParams.space.id
                            )
                        )
                    } else {
                        toast("You cannot change type from here.")
                    }
                }
                is CellView.Checkbox -> {
                    if (relation.isReadonlyValue) {
                        Timber.d("onGridCellClicked, relation is ReadOnly")
                        toast(NOT_ALLOWED_CELL)
                        return@launch
                    }
                    setObjectDetails(
                        UpdateDetail.Params(
                            target = cell.id,
                            key = cell.relationKey,
                            value = !cell.isChecked
                        )
                    ).process(
                        failure = { Timber.e(it, "Error while updating data view record") },
                        success = { Timber.d("Data view record updated successfully") }
                    )
                }
            }
        }
    }

    /**
     * @param [target] object is a record contained in this set.
     */
    fun onObjectHeaderClicked(target: Id) {
        Timber.d("onObjectHeaderClicked, id:[$target]")
        stateReducer.state.value.dataViewState() ?: return
        viewModelScope.launch {
            val obj = objectStore.get(target)
            if (obj != null) {
                proceedWithNavigation(
                    obj = obj,
                    identityProfileLink = obj.getSingleValue(Relations.IDENTITY_PROFILE_LINK)
                )
            } else {
                toast("Record not found. Please, try again later.")
            }
        }
    }

    fun onTaskCheckboxClicked(target: Id) {
        Timber.d("onTaskCheckboxClicked: $target")
        stateReducer.state.value.dataViewState() ?: return
        viewModelScope.launch {
            val obj = database.store.get(target)
            if (obj != null) {
                setObjectDetails(
                    UpdateDetail.Params(
                        target = target,
                        key = Relations.DONE,
                        value = !(obj.done ?: false)
                    )
                ).process(
                    failure = {
                        Timber.e(it, "Error while updating checkbox")
                    },
                    success = {
                        Timber.d("Checkbox successfully updated for record: $target")
                    }
                )
            } else {
                toast("Object not found")
            }
        }
    }

    /**
     * Called when user long-clicks on a row header in a view (e.g., grid, gallery, or list view)
     * Shows a context menu with the following options:
     * - "Open as Object"
     * - "Copy Link"
     * - "Move to Bin" (if allowed)
     * - "Unlink from Collection" (if allowed)
     */
    fun onObjectHeaderLongClicked(objectId: Id) {
        Timber.d("onObjectHeaderLongClicked, id:[$objectId]")
        viewModelScope.launch {

            // Check object DELETE restriction
            val obj = objectStore.get(objectId)
            val hasDeleteRestriction = obj?.restrictions?.contains(ObjectRestriction.DELETE) == true

            // Can move to bin only if: user is owner/editor AND object allows delete
            val canMoveToBin = isOwnerOrEditor && !hasDeleteRestriction

            // Check if current state is a Collection AND user has edit permission
            val isCollection = stateReducer.state.value is ObjectState.DataView.Collection
            val canRemoveFromCollection = isCollection && isOwnerOrEditor

            dispatch(
                ObjectSetCommand.Modal.ShowObjectHeaderContextMenu(
                    objectId = objectId,
                    canMoveToBin = canMoveToBin,
                    isCollection = canRemoveFromCollection
                )
            )
        }
    }

    /**
     * Opens the object as an Anytype object (not as a URL for bookmarks).
     * This bypasses the default bookmark behavior of opening URLs in browser.
     */
    fun onOpenAsObject(targetId: Id) {
        Timber.d("onOpenAsObject, id:[$targetId]")
        viewModelScope.launch {
            val obj = objectStore.get(targetId)
            if (obj != null) {
                navigateByLayout(
                    target = targetId,
                    space = vmParams.space.id,
                    layout = obj.layout
                )
            } else {
                toast("Object not found. Please, try again later.")
            }
        }
    }

    /**
     * Copies the object's deeplink to clipboard.
     */
    fun onCopyLink(targetId: Id) {
        Timber.d("onCopyLink, id:[$targetId]")
        viewModelScope.launch {
            val link = deepLinkResolver.createObjectDeepLink(
                obj = targetId,
                space = vmParams.space
            )
            dispatch(ObjectSetCommand.CopyLinkToClipboard(link = link))
        }
    }

    /**
     * Moves the object to bin (archives it).
     * Only Owner or Editor can perform this action, and the object must not have DELETE restriction.
     */
    fun onMoveToBin(targetId: Id) {
        Timber.d("onMoveToBin, id:[$targetId]")
        viewModelScope.launch {
            // Defensive permission check
            if (!isOwnerOrEditor) {
                toast(NOT_ALLOWED)
                return@launch
            }

            // Defensive restriction check
            val obj = objectStore.get(targetId)
            if (obj?.restrictions?.contains(ObjectRestriction.DELETE) == true) {
                toast(NOT_ALLOWED)
                return@launch
            }

            val params = SetObjectListIsArchived.Params(
                targets = listOf(targetId),
                isArchived = true
            )
            setObjectListIsArchived.async(params).fold(
                onSuccess = {
                    Timber.d("Successfully moved object to bin: $targetId")
                },
                onFailure = { e ->
                    Timber.e(e, "Error while moving object to bin")
                    toast("Error while moving to bin. Please try again.")
                }
            )
        }
    }

    /**
     * Removes the object from the current collection (does not delete the object).
     * Only available when viewing a Collection.
     */
    fun onRemoveFromCollection(targetId: Id) {
        Timber.d("onRemoveFromCollection, id:[$targetId]")
        viewModelScope.launch {
            val params = RemoveObjectFromCollection.Params(
                collectionId = vmParams.ctx,
                objectIdsToRemove = listOf(targetId)
            )
            removeObjectFromCollection.async(params).fold(
                onSuccess = {
                    Timber.d("Successfully removed object from collection: $targetId")
                },
                onFailure = { e ->
                    Timber.e(e, "Error removing object from collection")
                    toast("Error removing from collection. Please try again.")
                }
            )
        }
    }

    fun onRelationTextValueChanged(
        value: Any?,
        objectId: Id,
        relationKey: Id
    ) {
        Timber.d("onRelationTextValueChanged, objectId:[$objectId], relationKey:[$relationKey], value:[$value]")
        stateReducer.state.value.dataViewState() ?: return
        viewModelScope.launch {
            setObjectDetails(
                UpdateDetail.Params(
                    target = objectId,
                    key = relationKey,
                    value = value
                )
            ).process(
                failure = { Timber.e(it, "Error while updating data view record") },
                success = {
                    dispatcher.send(it)
                    Timber.d("Relation text value updated successfully")
                }
            )
        }
    }

    fun onNewButtonIconClicked() {
        Timber.d("onNewButtonIconClicked, ")
        showTypeTemplatesWidgetForObjectCreation()
    }

    // TODO Multispaces refactor this method
    private suspend fun proceedWithCreatingSetObject(currentState: ObjectState.DataView, templateChosenBy: Id?) {
        if (isRestrictionPresent(DataViewRestriction.CREATE_OBJECT)) {
            toast(NOT_ALLOWED)
        } else {
            val setObject = currentState.details.getObject(vmParams.ctx)
            val viewer = currentState.viewerByIdOrFirst(session.currentViewerId.value)
            if (viewer == null) {
                Timber.e("onCreateNewDataViewObject, Viewer is empty")
                return
            }

            // Get dataview block ID for cleanup
            val dvBlock = currentState.dataViewBlock.id

            val (resolvedType, defaultTemplate) = currentState.getActiveViewTypeAndTemplate(
                ctx = vmParams.ctx,
                activeView = viewer,
                storeOfObjectTypes = storeOfObjectTypes,
                onDeletedTypeDetected = { deletedViewer ->
                    // Cleanup deleted type in viewer (fire-and-forget)
                    cleanupDeletedTypeInViewer(
                        ctx = vmParams.ctx,
                        dv = dvBlock,
                        viewer = deletedViewer
                    )
                }
            )

            // Resolve type and template with fallback handling
            val (defaultObjectType, resolvedTemplate) = resolveTypeAndTemplateWithFallback(
                resolvedType = resolvedType,
                defaultTemplate = defaultTemplate,
                userChosenTemplate = templateChosenBy
            )

            if (defaultObjectType == null) {
                toast("Unable to create object: default type not found. Please try again or contact support.")
                Timber.e("Both resolved type and space default type are null")
                return
            }

            val objectTypeUniqueKey = defaultObjectType.uniqueKey

            val sourceId = setObject?.setOf?.singleOrNull()
            if (sourceId == null) {
                toast("Unable to define a source for a new object.")
            } else {
                val wrapper = currentState.details.getObject(sourceId)
                if (wrapper != null) {
                    when (wrapper.layout) {
                        ObjectType.Layout.OBJECT_TYPE -> {
                            val uniqueKey = wrapper.getValue<Key>(Relations.UNIQUE_KEY)
                            if (uniqueKey == null) {
                                toast("Could not found key for given type")
                                return
                            }
                            if (uniqueKey == ObjectTypeIds.BOOKMARK) {
                                dispatch(
                                    ObjectSetCommand.Modal
                                        .CreateBookmark(
                                            ctx = vmParams.ctx,
                                            space = requireNotNull(wrapper.spaceId)
                                        )
                                )
                            } else {
                                val validTemplateId = resolvedTemplate
                                val prefilled = viewer.prefillNewObjectDetails(
                                    storeOfRelations = storeOfRelations,
                                    dateProvider = dateProvider
                                )
                                proceedWithCreatingDataViewObject(
                                    CreateDataViewObject.Params.SetByType(
                                        type = TypeKey(uniqueKey),
                                        filters = viewer.filters,
                                        template = validTemplateId,
                                        prefilled = prefilled
                                    )
                                )
                            }
                        }
                        ObjectType.Layout.RELATION -> {
                            if (objectTypeUniqueKey == ObjectTypeIds.BOOKMARK) {
                                dispatch(
                                    ObjectSetCommand.Modal.CreateBookmark(
                                        ctx = vmParams.ctx,
                                        space = requireNotNull(wrapper.spaceId)
                                    )
                                )
                            } else {
                                val validTemplateId = resolvedTemplate
                                val prefilled = viewer.resolveSetByRelationPrefilledObjectData(
                                    storeOfRelations = storeOfRelations,
                                    dateProvider = dateProvider,
                                    objSetByRelation = ObjectWrapper.Relation(wrapper.map)
                                )
                                proceedWithCreatingDataViewObject(
                                    CreateDataViewObject.Params.SetByRelation(
                                        filters = viewer.filters,
                                        template = validTemplateId,
                                        type = TypeKey(objectTypeUniqueKey),
                                        prefilled = prefilled
                                    )
                                )
                            }
                        }
                        else -> toast("Unable to define a source for a new object.")
                    }
                } else {
                    toast("Unable to define a source for a new object.")
                }
            }
        }
    }

    private suspend fun proceedWithCreatingObjectTypeSetObject(
        currentState: ObjectState.DataView.TypeSet,
        templateChosenBy: String?
    ) {
        val objectType = storeOfObjectTypes.get(vmParams.ctx)

        val objectTypeUniqueKey = objectType?.uniqueKey ?: return

        if (objectTypeUniqueKey == ObjectTypeIds.BOOKMARK) {
            dispatch(
                ObjectSetCommand.Modal
                    .CreateBookmark(
                        ctx = vmParams.ctx,
                        space = vmParams.space.id
                    )
            )
        } else {
            val viewer = currentState.viewerByIdOrFirst(session.currentViewerId.value) ?: return
            val prefilled = viewer.prefillNewObjectDetails(
                storeOfRelations = storeOfRelations,
                dateProvider = dateProvider
            )
            proceedWithCreatingDataViewObject(
                CreateDataViewObject.Params.SetByType(
                    type = TypeKey(objectTypeUniqueKey),
                    filters = viewer.filters,
                    template = templateChosenBy ?: objectType.defaultTemplateId,
                    prefilled = prefilled
                )
            )
        }
    }

    fun onSelectQueryButtonClicked() {
        dispatch(ObjectSetCommand.Modal.OpenEmptyDataViewSelectQueryScreen)
    }

    private suspend fun proceedWithAddingObjectToCollection(
        typeChosenByUser: TypeKey? = null,
        templateChosenBy: Id? = null
    ) {
        val state = stateReducer.state.value.dataViewState() ?: return
        val viewer = state.viewerByIdOrFirst(session.currentViewerId.value) ?: return

        // Get dataview block ID for cleanup
        val dvBlock = state.dataViewBlock.id

        val (resolvedType, defaultTemplate) = state.getActiveViewTypeAndTemplate(
            ctx = vmParams.ctx,
            activeView = viewer,
            storeOfObjectTypes = storeOfObjectTypes,
            onDeletedTypeDetected = { deletedViewer ->
                // Cleanup deleted type in viewer (fire-and-forget)
                cleanupDeletedTypeInViewer(
                    ctx = vmParams.ctx,
                    dv = dvBlock,
                    viewer = deletedViewer
                )
            }
        )

        // Resolve type and template with fallback handling
        val (defaultObjectType, resolvedTemplate) = resolveTypeAndTemplateWithFallback(
            resolvedType = resolvedType,
            defaultTemplate = defaultTemplate,
            userChosenTemplate = templateChosenBy
        )

        val defaultObjectTypeUniqueKey = defaultObjectType?.uniqueKey?.let {
            TypeKey(it)
        }

        if (typeChosenByUser == null && defaultObjectTypeUniqueKey == null) {
            toast("Could not define type for new object")
            return
        }

        val validTemplateId = resolvedTemplate
        val prefilled = viewer.prefillNewObjectDetails(
            storeOfRelations = storeOfRelations,
            dateProvider = dateProvider
        )
        val type = typeChosenByUser ?: defaultObjectTypeUniqueKey!!
        val createObjectParams = CreateDataViewObject.Params.Collection(
            template = validTemplateId,
            type = type,
            filters = viewer.filters,
            prefilled = prefilled
        )
        if (type.key == ObjectTypeIds.BOOKMARK) {
            dispatch(
                ObjectSetCommand.Modal.CreateBookmark(
                    ctx = vmParams.ctx,
                    space = vmParams.space.id
                )
            )
        } else {
            proceedWithCreatingDataViewObject(createObjectParams) { result ->
                val params = AddObjectToCollection.Params(
                    ctx = vmParams.ctx,
                    after = "",
                    targets = listOf(result.objectId)
                )
                viewModelScope.launch {
                    addObjectToCollection.async(params).fold(
                        onSuccess = { payload -> dispatcher.send(payload) },
                        onFailure = { Timber.e(it, "Error while adding object to collection") }
                    )
                }
            }
        }
    }

    private suspend fun proceedWithCreatingDataViewObject(
        params: CreateDataViewObject.Params,
        action: ((CreateDataViewObject.Result) -> Unit)? = null
    ) {
        val startTime = System.currentTimeMillis()
        createDataViewObject.async(params).fold(
            onFailure = { Timber.e(it, "Error while creating new record") },
            onSuccess = { result ->
                action?.invoke(result)
                pendingScrollToObject.value = result.objectId
                proceedWithNewDataViewObject(result)
                sendAnalyticsObjectCreateEvent(
                    startTime = startTime,
                    typeKey = result.objectType.key,
                )
            }
        )
    }

    private suspend fun proceedWithNewDataViewObject(
        response: CreateDataViewObject.Result,
    ) {
        val obj = ObjectWrapper.Basic(response.struct.orEmpty())
        when (obj.layout) {
            ObjectType.Layout.CHAT_DERIVED -> {
                proceedWithOpeningChat(
                    target = obj.id,
                    space = vmParams.space.id
                )
            }
            ObjectType.Layout.NOTE -> {
                proceedWithCreatingNoteObject(obj = obj)
            }
            else -> {
                val isIconChangeAllowed = obj.layout in layoutsSupportsEmojiAndImages
                val icon = obj.objectIcon(
                    builder = urlBuilder,
                    objType = storeOfObjectTypes.getTypeOfObject(obj)
                )
                showSetObjectNameSheet(
                    objectId = response.objectId,
                    icon = icon,
                    isIconChangeAllowed = isIconChangeAllowed
                )
            }
        }
    }

    fun onViewerCustomizeButtonClicked() {
        Timber.d("onViewerCustomizeButtonClicked, ")
        val dataView = stateReducer.state.value.dataViewState() ?: return
        val activeViewer = dataView.viewerByIdOrFirst(session.currentViewerId.value) ?: return
        widgetViewerId.value = activeViewer.id
        showViewerEditWidget()
    }

    fun onHideViewerCustomizeSwiped() {
        Timber.d("onHideViewerCustomizeSwiped, ")
        isCustomizeViewPanelVisible.value = false
    }

    fun onExpandViewerMenuClicked() {
        Timber.d("onExpandViewerMenuClicked, ")
        val state = stateReducer.state.value.dataViewState() ?: return
        if (isRestrictionPresent(DataViewRestriction.VIEWS)
        ) {
            toast(NOT_ALLOWED)
        } else {
            viewersWidgetState.value = viewersWidgetState.value.copy(showWidget = true)
        }
    }

    fun onViewerEditClicked() {
        Timber.d("onViewerEditClicked, ")
        val state = stateReducer.state.value.dataViewState() ?: return
        val viewer = state.viewerByIdOrFirst(session.currentViewerId.value) ?: return
    }

    fun onMenuClicked() {
        Timber.d("onMenuClicked, ")
        val state = stateReducer.state.value.dataViewState() ?: return
        val wrapper = state.details.getObject(vmParams.ctx) ?: return
        Timber.d("Wrapper: $wrapper")
        val space = wrapper.spaceId
        if (space != null) {
            dispatch(
                ObjectSetCommand.Modal.Menu(
                    ctx = vmParams.ctx,
                    space = space,
                    isArchived = wrapper.isArchived == true,
                    isFavorite = wrapper.isFavorite == true,
                    isReadOnly = !isOwnerOrEditor
                )
            )
        } else {
            Timber.e("Space not found").also {
                toast("Space not found")
            }
        }
    }

    fun onObjectIconClicked() {
        Timber.d("onIconClicked, ")
        val state = stateReducer.state.value.dataViewState() ?: return
        val wrapper = state.details.getObject(vmParams.ctx)
        val space = wrapper?.spaceId
        if (space != null) {
            dispatch(
                ObjectSetCommand.Modal.OpenIconActionMenu(
                    target = vmParams.ctx,
                    space = space
                )
            )
        } else {
            Timber.e("Space not found").also {
                toast("Space not found")
            }
        }
    }

    fun onCoverClicked() {
        Timber.d("onCoverClicked, ")
        dispatch(
            ObjectSetCommand.Modal.OpenCoverActionMenu(
                ctx = vmParams.ctx,
                space = vmParams.space.id
            )
        )
    }

    fun onViewerSettingsClicked(viewer: Id) {
        Timber.d("onViewerSettingsClicked, viewer: [$viewer]")
        if (isRestrictionPresent(DataViewRestriction.RELATION)) {
            toast(NOT_ALLOWED)
        } else {
            val state = stateReducer.state.value.dataViewState() ?: return
            val dataViewBlock = state.dataViewBlock
            dispatch(
                ObjectSetCommand.Modal.OpenSettings(
                    ctx = vmParams.ctx,
                    dv = dataViewBlock.id,
                    viewer = viewer
                )
            )
        }
    }

    fun onViewerFiltersClicked() {
        Timber.d("onViewerFiltersClicked, ")
        openViewerFilters()
    }

    fun onViewerSortsClicked() {
        Timber.d("onViewerSortsClicked, ")
        openViewerSorts()
    }

    private fun dispatch(command: ObjectSetCommand) {
        viewModelScope.launch { _commands.emit(command) }
    }

    private fun dispatch(command: AppNavigation.Command) {
        navigate(EventWrapper(command))
    }

    private fun toast(toast: String) {
        viewModelScope.launch { toasts.emit(toast) }
    }

    private fun isRestrictionPresent(restriction: DataViewRestriction): Boolean {
        val state = stateReducer.state.value.dataViewState() ?: return false
        val block = state.dataViewBlock
        val dVRestrictions = state.dataViewRestrictions.firstOrNull { it.block == block.id }
        return dVRestrictions != null && dVRestrictions.restrictions.any { it == restriction }
    }

    //region { PAGINATION LOGIC }

    fun onPaginatorToolbarNumberClicked(number: Int, isSelected: Boolean) {
        Timber.d("onPaginatorToolbarNumberClicked, number:[$number], isSelected:[$isSelected]")
        if (isSelected) {
            Timber.d("This page is already selected")
        } else {
            viewModelScope.launch {
                paginator.offset.value = number.toLong() * DEFAULT_LIMIT
            }
        }
    }

    fun onPaginatorNextElsePrevious(next: Boolean) {
        Timber.d("onPaginatorNextElsePrevious, next:[$next]")
        viewModelScope.launch {
            paginator.offset.value = if (next) {
                paginator.offset.value + DEFAULT_LIMIT
            } else {
                paginator.offset.value - DEFAULT_LIMIT
            }
        }
    }

    //endregion

    //region NAVIGATION

    private suspend fun proceedWithOpeningObject(
        target: Id,
        space: Id,
        layout: ObjectType.Layout? = null
    ) {
        Timber.d("proceedWithOpeningObject, target:[$target], layout:[$layout]")
        if (target == vmParams.ctx) {
            toast("You are already here")
            Timber.d("proceedWithOpeningObject, target == vmParams.ctx")
            return
        }
        isCustomizeViewPanelVisible.value = false
        val navigateCommand = AppNavigation.Command.OpenObject(
            target = target,
            space = space
        )
        closeObject.async(
            CloseObject.Params(
                target = vmParams.ctx,
                space = vmParams.space
            )
        ).fold(
            onSuccess = { navigate(EventWrapper(navigateCommand)) },
            onFailure = {
                Timber.e(it, "Error while closing object set: ${vmParams.ctx}")
                navigate(EventWrapper(navigateCommand))
            }
        )
    }

    private suspend fun proceedWithOpeningChat(
        target: Id,
        space: Id
    ) {
        isCustomizeViewPanelVisible.value = false
        val navigateCommand = AppNavigation.Command.OpenChat(
            target = target,
            space = space,
            popUpToVault = false
        )
        closeObject.async(
            CloseObject.Params(
                target = vmParams.ctx,
                space = vmParams.space
            )
        ).fold(
            onSuccess = { navigate(EventWrapper(navigateCommand)) },
            onFailure = {
                Timber.e(it, "Error while closing object set: ${vmParams.ctx}")
                navigate(EventWrapper(navigateCommand))
            }
        )
    }

    /**
     * Handles creation flow for Note objects by creating the first text block.
     * Note objects don't have titles - content goes directly into text blocks.
     */
    private suspend fun proceedWithCreatingNoteObject(obj: ObjectWrapper.Basic) {
        val icon = obj.objectIcon(
            builder = urlBuilder,
            objType = storeOfObjectTypes.getTypeOfObject(obj)
        )
        createBlock.async(
            CreateBlock.Params(
                context = obj.id,
                target = "header",
                position = Position.BOTTOM,
                prototype = Block.Prototype.Text(style = Block.Content.Text.Style.P)
            )
        ).fold(
            onFailure = { error ->
                Timber.e(error, "Error creating text block for Note object")
                // Fallback: show sheet without blockId
                showSetObjectNameSheet(
                    objectId = obj.id,
                    icon = icon,
                    isIconChangeAllowed = false
                )
            },
            onSuccess = { (blockId, payload) ->
                // Update local state with payload
                dispatcher.send(payload)

                // Show name sheet with blockId
                showSetObjectNameSheet(
                    objectId = obj.id,
                    icon = icon,
                    isIconChangeAllowed = false,
                    targetBlockId = blockId
                )
            }
        )
    }

    private fun proceedWithOpeningTemplate(target: Id, targetTypeId: Id, targetTypeKey: Id) {
        isCustomizeViewPanelVisible.value = false
        val event = AppNavigation.Command.OpenModalTemplateSelect(
            template = target,
            space = vmParams.space.id,
            templateTypeId = targetTypeId,
            templateTypeKey = targetTypeKey
        )
        navigate(EventWrapper(event))
    }

    private suspend fun proceedWithOpeningObjectCollection(
        target: Id,
        space: Id
    ) {
        if (target == vmParams.ctx) {
            toast("You are already here")
            Timber.d("proceedWithOpeningObject, target == vmParams.ctx")
            return
        }
        isCustomizeViewPanelVisible.value = false
        jobs += viewModelScope.launch {
            closeObject.async(
                CloseObject.Params(
                    target = vmParams.ctx,
                    space = vmParams.space
                )
            ).fold(
                onSuccess = {
                    navigate(
                        EventWrapper(
                            AppNavigation.Command.OpenSetOrCollection(
                                target = target,
                                space = space
                            )
                        )
                    )
                },
                onFailure = {
                    Timber.e(it, "Error while closing object set: ${vmParams.ctx}")
                    navigate(
                        EventWrapper(
                            AppNavigation.Command.OpenSetOrCollection(
                                target = target,
                                space = space
                            )
                        )
                    )
                }
            )
        }
    }

    private suspend fun navigateByLayout(
        target: Id,
        space: Id,
        layout: ObjectType.Layout?,
        identityProfileLink: Id? = null
    ) {
        when (layout) {
            ObjectType.Layout.BASIC,
            ObjectType.Layout.TODO,
            ObjectType.Layout.NOTE,
            ObjectType.Layout.IMAGE,
            ObjectType.Layout.FILE,
            ObjectType.Layout.VIDEO,
            ObjectType.Layout.AUDIO,
            ObjectType.Layout.PDF,
            ObjectType.Layout.BOOKMARK -> proceedWithOpeningObject(
                target = target,
                space = space
            )
            ObjectType.Layout.PARTICIPANT -> {
                navigate(
                    EventWrapper(
                        AppNavigation.Command.OpenParticipant(
                            objectId = target,
                            space = space
                        )
                    )
                )
            }
            ObjectType.Layout.OBJECT_TYPE -> {
                navigate(
                    EventWrapper(
                        AppNavigation.Command.OpenTypeObject(
                            target = target,
                            space = space
                        )
                    )
                )
            }
            ObjectType.Layout.PROFILE -> proceedWithOpeningObject(
                target = identityProfileLink ?: target,
                space = space
            )
            ObjectType.Layout.SET, ObjectType.Layout.COLLECTION -> {
                closeObject.async(
                    CloseObject.Params(
                        target = vmParams.ctx,
                        space = vmParams.space
                    )
                ).fold(
                    onSuccess = {
                        navigate(
                            EventWrapper(
                                AppNavigation.Command.OpenSetOrCollection(
                                    target = target,
                                    space = space
                                )
                            )
                        )
                    },
                    onFailure = {
                        Timber.e(it, "Error while closing object set: ${vmParams.ctx}")
                        navigate(
                            EventWrapper(
                                AppNavigation.Command.OpenSetOrCollection(
                                    target = target,
                                    space = space
                                )
                            )
                        )
                    }
                )
            }
            ObjectType.Layout.DATE -> {
                navigate(
                    EventWrapper(
                        AppNavigation.Command.OpenDateObject(
                            objectId = target,
                            space = space
                        )
                    )
                )
            }
            ObjectType.Layout.CHAT_DERIVED -> {
                navigate(
                    EventWrapper(
                        AppNavigation.Command.OpenChat(
                            target = target,
                            space = space,
                            popUpToVault = false
                        )
                    )
                )
            }
            else -> {
                toast("Unexpected layout: $layout")
                Timber.e("Unexpected layout: $layout")
            }
        }
    }

    private suspend fun proceedWithNavigation(
        target: Id,
        space: Id,
        layout: ObjectType.Layout?,
        identityProfileLink: Id? = null
    ) {
        if (target == vmParams.ctx) {
            toast("You are already here")
            Timber.d("proceedWithNavigation, target == vmParams.ctx")
            return
        }
        navigateByLayout(target, space, layout, identityProfileLink)
    }

    private suspend fun proceedWithNavigation(obj: ObjectWrapper.Basic, identityProfileLink: Id? = null) {
        // If the object is a bookmark, open its URL in a Custom Tab
        if (obj.layout == ObjectType.Layout.BOOKMARK) {
            val url = obj.getSingleValue<String>(Relations.SOURCE)
            if (!url.isNullOrBlank()) {
                dispatch(ObjectSetCommand.Browse(url))
                return
            }
        }
        if (obj.id == vmParams.ctx) {
            toast("You are already here")
            Timber.d("proceedWithNavigation, target == vmParams.ctx")
            return
        }

        val target = obj.id
        val space = vmParams.space.id

        // If the object is a Template (and not a Set or Collection), open it in the Modal Template Screen
        if (obj.isTemplateObject(storeOfObjectTypes = storeOfObjectTypes) && !obj.layout.isDataView()) {
            obj.getTypeForObjectAndTargetTypeForTemplate(storeOfObjectTypes = storeOfObjectTypes)
                ?.let { objType ->
                    val event = AppNavigation.Command.OpenModalTemplateEdit(
                        template = target,
                        space = vmParams.space.id,
                        templateTypeId = objType.id,
                        templateTypeKey = objType.uniqueKey
                    )
                    navigate(EventWrapper(event))
                    return
                }
        }

        navigateByLayout(
            target = target,
            space = space,
            layout = obj.layout,
            identityProfileLink = identityProfileLink
        )
    }
    //endregion NAVIGATION

    fun onUnsupportedViewErrorClicked() {
        // Do nothing
    }

    override fun onCleared() {
        Timber.d("onCleared, ")
        super.onCleared()
        titleUpdateChannel.cancel()
        stateReducer.clear()
    }

    fun onBackButtonClicked() {
        proceedWithClosingAndExit()
    }

    fun onAddNewDocumentClicked(objType: ObjectWrapper.Type? = null) {
        Timber.d("onAddNewDocumentClicked, objType:[$objType]")

        val startTime = System.currentTimeMillis()
        val params = objType?.uniqueKey.getCreateObjectParams(
            space = vmParams.space,
            objType?.defaultTemplateId
        )
        jobs += viewModelScope.launch {
            createObject.async(params).fold(
                onSuccess = { result ->
                    delegator.delegate(
                        if (objType?.recommendedLayout.isDataView())
                            Action.OpenCollection(
                                target = result.objectId,
                                space = requireNotNull(result.obj.spaceId)
                            )
                        else
                            Action.OpenObject(
                                target = result.objectId,
                                space = requireNotNull(result.obj.spaceId)
                            )
                    )
                    sendAnalyticsObjectCreateEvent(
                        analytics = analytics,
                        route = EventsDictionary.Routes.navigation,
                        startTime = startTime,
                        view = EventsDictionary.View.viewNavbar,
                        objType = objType ?: storeOfObjectTypes.getByKey(result.typeKey.key),
                        spaceParams = provideParams(vmParams.space.id)
                    )
                },
                onFailure = { e ->
                    Timber.e(e, "Error while creating a new object")
                    toast("Error while creating a new object")
                }
            )
        }
    }

    private fun sendAnalyticsObjectCreateEvent(startTime: Long, typeKey: Key?) {
        viewModelScope.launch {
            val objType = typeKey?.let { storeOfObjectTypes.getByKey(it) }
            logEvent(
                state = stateReducer.state.value,
                analytics = analytics,
                event = ObjectStateAnalyticsEvent.OBJECT_CREATE,
                startTime = startTime,
                type = objType?.sourceObject ?: OBJ_TYPE_CUSTOM,
                spaceParams = provideParams(vmParams.space.id)
            )
        }
    }

    fun onSearchButtonClicked() {
        viewModelScope.sendEvent(
            analytics = analytics,
            eventName = EventsDictionary.searchScreenShow,
            props = Props(mapOf(
                EventsPropertiesKey.route to EventsDictionary.Routes.navigation,
                EventsPropertiesKey.spaceId to vmParams.space.id
            ))
        )
        viewModelScope.launch {
            dispatch(
                AppNavigation.Command.OpenGlobalSearch(
                    space = vmParams.space.id
                )
            )
        }
    }

    fun onHomeButtonClicked() {
        viewModelScope.launch {
            navPanelState.firstOrNull()?.leftButtonClickAnalytics(analytics)
        }
        viewModelScope.launch {
            dispatch(AppNavigation.Command.ExitToSpaceHome)
        }
    }

    fun onShareButtonClicked() {
        viewModelScope.launch {
            navPanelState.firstOrNull()?.leftButtonClickAnalytics(analytics)
        }
        viewModelScope.launch {
            dispatch(AppNavigation.Command.OpenShareScreen(vmParams.space))
        }
    }

    fun onClickListener(clicked: ListenerType) {
        Timber.d("onClickListener, clicked:[$clicked]")
        when (clicked) {
            is ListenerType.Relation.SetQuery -> {
                if (isOwnerOrEditor) {
                    val queries = clicked.queries.map { it.id }
                    val command = if (queries.isEmpty()) {
                        ObjectSetCommand.Modal.OpenEmptyDataViewSelectQueryScreen
                    } else {
                        ObjectSetCommand.Modal.OpenDataViewSelectQueryScreen(
                            selectedTypes = queries
                        )
                    }
                    dispatch(command)
                } else {
                    dispatch(ObjectSetCommand.ShowOnlyAccessError)
                }
            }
            is ListenerType.Relation.ChangeQueryByRelation -> {
                toast(clicked.msg)
            }
            is ListenerType.Relation.ObjectType -> {
                when (clicked.relation) {
                    is ObjectRelationView.ObjectType.Base -> {
                        val state = stateReducer.state.value.dataViewState() ?: return
                        when (state) {
                            is ObjectState.DataView.Collection -> {
                                //do nothing
                            }
                            is ObjectState.DataView.Set -> {
                                if (isOwnerOrEditor) {
                                    val setOfValue = state.getSetOfValue(vmParams.ctx)
                                    val command =
                                        if (state.isSetByRelation(setOfValue = setOfValue)) {
                                            ObjectSetCommand.Modal.ShowObjectSetRelationPopupMenu(
                                                ctx = clicked.relation.id,
                                                anchor = clicked.viewId
                                            )
                                        } else {
                                            ObjectSetCommand.Modal.ShowObjectSetTypePopupMenu(
                                                ctx = clicked.relation.id,
                                                anchor = clicked.viewId
                                            )
                                        }
                                    dispatch(command)
                                } else {
                                    dispatch(ObjectSetCommand.ShowOnlyAccessError)
                                }
                            }

                            is ObjectState.DataView.TypeSet -> {
                                //do nothing
                            }
                        }
                    }
                    else -> {
                        Timber.d("Ignoring click on relation, relation:[${clicked.relation}]")
                    }
                }
            }
            is ListenerType.Relation.Featured -> {
                onRelationClickedListMode(
                    ctx = vmParams.ctx,
                    view = clicked.relation
                )
            }
            else -> {
                Timber.d("Ignoring click, listener:[${clicked}]")
            }
        }
    }

    private fun proceedWithTogglingRelationCheckboxValue(view: ObjectRelationView, ctx: Id) {
        viewModelScope.launch {
            check(view is ObjectRelationView.Checkbox)
            setObjectDetails(
                UpdateDetail.Params(
                    target = ctx,
                    key = view.key,
                    value = !view.isChecked
                )
            ).process(
                success = {
                    dispatcher.send(it)
                    analytics.sendAnalyticsRelationEvent(
                        eventName = EventsDictionary.relationChangeValue,
                        storeOfRelations = storeOfRelations,
                        relationKey = view.key,
                        spaceParams = provideParams(spaceManager.get())
                    )
                },
                failure = { Timber.e(it, "Error while updating checkbox relation") }
            )
        }
    }

    private fun onRelationClickedListMode(ctx: Id, view: ObjectRelationView) {
        viewModelScope.launch {
            val relation = storeOfRelations.getById(view.id)
            if (relation == null) {
                Timber.w("Couldn't find relation in store by id:${view.id}")
                return@launch
            }
            when (relation.format) {
                RelationFormat.SHORT_TEXT,
                RelationFormat.LONG_TEXT,
                RelationFormat.NUMBER,
                RelationFormat.URL,
                RelationFormat.EMAIL,
                RelationFormat.PHONE -> {
                    _commands.emit(
                        ObjectSetCommand.Modal.EditIntrinsicTextRelation(
                            ctx = ctx,
                            relation = relation.key,
                            space = requireNotNull(relation.spaceId)
                        )
                    )
                }
                RelationFormat.CHECKBOX -> {
                    if (relation.isReadonlyValue) {
                        toast(RelationListViewModel.NOT_ALLOWED_FOR_RELATION)
                        Timber.d("No interaction allowed with this relation")
                        return@launch
                    }
                    proceedWithTogglingRelationCheckboxValue(view, ctx)
                }
                RelationFormat.DATE -> {
                    if (relation.isReadonlyValue)  {
                        val timeInMillis =
                            (view as? ObjectRelationView.Date)?.relativeDate?.initialTimeInMillis
                        handleReadOnlyValue(timeInMillis = timeInMillis)
                    } else {
                        _commands.emit(
                            ObjectSetCommand.Modal.EditGridDateCell(
                                ctx = vmParams.ctx,
                                objectId = vmParams.ctx,
                                relationKey = relation.key,
                                space = vmParams.space.id
                            )
                        )
                    }
                }
                RelationFormat.STATUS,
                RelationFormat.TAG -> {
                    _commands.emit(
                        ObjectSetCommand.Modal.EditTagOrStatusRelationValue(
                            ctx = vmParams.ctx,
                            relation = relation.key,
                            space = requireNotNull(relation.spaceId)
                        )
                    )
                }
                RelationFormat.FILE,
                RelationFormat.OBJECT -> {
                    _commands.emit(
                        ObjectSetCommand.Modal.EditObjectRelationValue(
                            ctx = vmParams.ctx,
                            relation = relation.key,
                            space = requireNotNull(relation.spaceId)
                        )
                    )
                }
                RelationFormat.EMOJI,
                RelationFormat.RELATIONS,
                RelationFormat.UNDEFINED -> {
                    toast(RelationListViewModel.NOT_SUPPORTED_UPDATE_VALUE)
                    Timber.d("Update value of relation with format:[${relation.format}] is not supported")
                }
                else -> {
                    Timber.d("Ignoring")
                }
            }
        }
    }

    fun onObjectSetQueryPicked(query: Id) {
        Timber.d("onObjectSetQueryPicked, query:[$query]")
        val startTime = System.currentTimeMillis()
        viewModelScope.launch {
            val params = SetQueryToObjectSet.Params(
                ctx = vmParams.ctx,
                query = query
            )
            setQueryToObjectSet.async(params).fold(
                onSuccess = { payload ->
                    logEvent(
                        state = stateReducer.state.value,
                        analytics = analytics,
                        event = ObjectStateAnalyticsEvent.SELECT_QUERY,
                        type = query,
                        startTime = startTime,
                        spaceParams = provideParams(vmParams.space.id)
                    )
                    defaultPayloadConsumer(payload)
                },
                onFailure = { e -> Timber.e(e, "Error while setting Set query") }
            )
        }
    }

    fun proceedWithConvertingToCollection() {
        val startTime = System.currentTimeMillis()
        val params = ConvertObjectToCollection.Params(ctx = vmParams.ctx)
        viewModelScope.launch {
            objectToCollection.async(params).fold(
                onFailure = { error -> Timber.e(error, "Error convert object to collection") },
                onSuccess = {
                    isCustomizeViewPanelVisible.value = false
                    logEvent(
                        state = stateReducer.state.value,
                        analytics = analytics,
                        event = ObjectStateAnalyticsEvent.TURN_INTO_COLLECTION,
                        startTime = startTime,
                        spaceParams = provideParams(vmParams.space.id)
                    )
                }
            )
        }
    }

    //region TYPES AND TEMPLATES WIDGET
    fun onNewTypeForViewerClicked(objType: ObjectWrapper.Type) {
        Timber.d("onNewTypeForViewerClicked, objType:[$objType]")
        selectedTypeFlow.value = objType
    }

    private fun showTypeTemplatesWidgetForObjectCreation() {
        val isPossibleToChangeType = stateReducer.state.value.dataViewState()?.isChangingDefaultTypeAvailable()
        showTypeTemplatesWidget(
            getViewer = { it?.viewerByIdOrFirst(session.currentViewerId.value) },
            createState = { viewer ->
                TypeTemplatesWidgetUI.Data(
                    showWidget = true,
                    isEditing = false,
                    viewerId = viewer.id,
                    isPossibleToChangeType = isPossibleToChangeType == true,
                    isPossibleToChangeTemplate = false
                )
            }
        )
    }

    private fun showTypeTemplatesWidget(
        getViewer: (ObjectState.DataView?) -> DVViewer?,
        createState: (DVViewer) -> TypeTemplatesWidgetUI.Data
    ) {
        viewModelScope.launch {
            val dataView = stateReducer.state.value.dataViewState() ?: return@launch
            val viewer = getViewer(dataView) ?: return@launch

            // Get dataview block ID for cleanup
            val dvBlock = when (dataView) {
                is ObjectState.DataView -> dataView.dataViewBlock.id
                else -> null
            }

            val (type, _) = dataView.getActiveViewTypeAndTemplate(
                ctx = vmParams.ctx,
                activeView = viewer,
                storeOfObjectTypes = storeOfObjectTypes,
                onDeletedTypeDetected = { deletedViewer ->
                    // Cleanup deleted type in viewer (fire-and-forget)
                    if (dvBlock != null) {
                        cleanupDeletedTypeInViewer(
                            ctx = vmParams.ctx,
                            dv = dvBlock,
                            viewer = deletedViewer
                        )
                    }
                }
            )

            // If type is null due to deleted type, use space default
            val effectiveType = type ?: getSpaceDefaultType()
            if (effectiveType == null) return@launch

            typeTemplatesWidgetState.value = createState(viewer)
            selectedTypeFlow.value = effectiveType
        }
        logEvent(ObjectStateAnalyticsEvent.SHOW_TEMPLATES)
        logEvent(ObjectStateAnalyticsEvent.SCREEN_TYPE_TEMPLATE_SELECTOR)
    }

    fun onTypeTemplatesWidgetAction(action: TypeTemplatesWidgetUIAction) {
        Timber.d("onTypeTemplatesWidgetAction, action:[$action]")
        val uiState = typeTemplatesWidgetState.value
        when (action) {
            is TypeTemplatesWidgetUIAction.TypeClick.Item -> {
                when (uiState) {
                    is TypeTemplatesWidgetUI.Data -> {
                        selectedTypeFlow.value = action.type
                    }

                    is TypeTemplatesWidgetUI.Init -> Unit
                }
            }
            TypeTemplatesWidgetUIAction.TypeClick.Search -> {
                viewModelScope.launch {
                    _commands.emit(
                        ObjectSetCommand.Modal.OpenSelectTypeScreen
                    )
                }
            }
            is TypeTemplatesWidgetUIAction.TemplateClick -> {
                viewModelScope.launch {
                    uiState.onTemplateClick(action.template)
                }
            }
        }
    }

    private suspend fun TypeTemplatesWidgetUI.onTemplateClick(
        templateView: TemplateView
    ) {
        if (this is TypeTemplatesWidgetUI.Data && moreMenuItem != null) {
            typeTemplatesWidgetState.value = hideMoreMenu()
            return
        }
        if (this is TypeTemplatesWidgetUI.Data) {
            typeTemplatesWidgetState.value = copy(showWidget = false)
        }
        selectedTypeFlow.value = null
        delay(DELAY_BEFORE_CREATING_TEMPLATE)
        when (templateView) {
            is TemplateView.Blank -> {
                logEvent(
                    event = ObjectStateAnalyticsEvent.SET_AS_DEFAULT_TYPE,
                    type = templateView.targetTypeKey.key
                )
                logEvent(ObjectStateAnalyticsEvent.CHANGE_DEFAULT_TEMPLATE)
                proceedWithUpdateViewer(
                    viewerId = getWidgetViewerId()
                ) {
                    it.copy(
                        defaultTemplate = templateView.id,
                        defaultObjectType = templateView.targetTypeId.id
                    )
                }
                proceedWithDataViewObjectCreate(
                    typeChosenBy = templateView.targetTypeKey,
                    templateId = templateView.id
                )

            }
            is TemplateView.Template -> {
                logEvent(
                    event = ObjectStateAnalyticsEvent.SET_AS_DEFAULT_TYPE,
                    type = templateView.targetTypeKey.key
                )
                logEvent(ObjectStateAnalyticsEvent.CHANGE_DEFAULT_TEMPLATE)
                proceedWithUpdateViewer(
                    viewerId = getWidgetViewerId()
                ) {
                    it.copy(
                        defaultTemplate = templateView.id,
                        defaultObjectType = templateView.targetTypeId.id
                    )
                }
                proceedWithDataViewObjectCreate(
                    typeChosenBy = templateView.targetTypeKey,
                    templateId = templateView.id
                )
            }
            is TemplateView.New -> {
                proceedWithCreatingTemplate(
                    targetTypeId = templateView.targetTypeId.id,
                    targetTypeKey = templateView.targetTypeKey.key
                )
            }
        }
    }

    private fun logEvent(event: ObjectStateAnalyticsEvent, type: Id? = null) {
        viewModelScope.launch {
            logEvent(
                state = stateReducer.state.value,
                analytics = analytics,
                event = event,
                type = type,
                spaceParams = provideParams(vmParams.space.id)
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun subscribeToSelectedType() {
        viewModelScope.launch {
            selectedTypeFlow
                .filterNotNull()
                .onEach {
                    updateTypesForTypeTemplatesWidget(it.id)
                }
                .flatMapLatest { selectedType ->
                    templatesContainer.subscribeToTemplates(
                        type = selectedType.id,
                        space = vmParams.space,
                        subscription = "${vmParams.ctx}$SUBSCRIPTION_TEMPLATES_ID"
                    )
                }.map { templates ->
                    val state = stateReducer.state.value
                    val viewerId = typeTemplatesWidgetState.value.getWidgetViewerId()
                    val dataView = state.dataViewState() ?: return@map emptyList<TemplateView>()
                    val viewer = dataView.viewerById(viewerId) ?: return@map emptyList<TemplateView>()
                    val selectedTypeId = selectedTypeFlow.value?.id ?: return@map emptyList<TemplateView>()

                    // Get dataview block ID for cleanup
                    val dvBlock = when (dataView) {
                        is ObjectState.DataView -> dataView.dataViewBlock.id
                        else -> null
                    }

                    val (type, template) = dataView.getActiveViewTypeAndTemplate(
                        ctx = vmParams.ctx,
                        activeView = viewer,
                        storeOfObjectTypes = storeOfObjectTypes,
                        onDeletedTypeDetected = { deletedViewer ->
                            // Cleanup deleted type in viewer (fire-and-forget)
                            if (dvBlock != null) {
                                cleanupDeletedTypeInViewer(
                                    ctx = vmParams.ctx,
                                    dv = dvBlock,
                                    viewer = deletedViewer
                                )
                            }
                        }
                    )

                    // If type is null due to deleted type, use space default
                    val effectiveType = type ?: getSpaceDefaultType()

                    when (typeTemplatesWidgetState.value) {
                        is TypeTemplatesWidgetUI.Data -> {
                            if (effectiveType?.id == selectedTypeFlow.value?.id) {
                                processTemplates(
                                    templates = templates,
                                    viewerDefType = effectiveType ?: storeOfObjectTypes.get(
                                        selectedTypeId
                                    ),
                                    viewerDefTemplate = template
                                        ?: selectedTypeFlow.value?.defaultTemplateId
                                )
                            } else {
                                processTemplates(
                                    templates = templates,
                                    viewerDefType = storeOfObjectTypes.get(selectedTypeId),
                                    viewerDefTemplate = selectedTypeFlow.value?.defaultTemplateId
                                )
                            }
                        }

                        is TypeTemplatesWidgetUI.Init -> emptyList()
                    }
                }
                .catch {
                    Timber.e(it, "Error while processing templates")
                }
                .collect{ templateViews ->
                    typeTemplatesWidgetState.value =
                        when (val uistate = typeTemplatesWidgetState.value) {
                            is TypeTemplatesWidgetUI.Data -> uistate.copy(templates = templateViews)
                            is TypeTemplatesWidgetUI.Init -> uistate
                        }
                }
        }
    }

    private suspend fun updateTypesForTypeTemplatesWidget(selectedType: Id) {
        when (val widgetState = typeTemplatesWidgetState.value) {
            is TypeTemplatesWidgetUI.Data -> {
                if (widgetState.isPossibleToChangeType) {
                    updateWidgetStateWithTypes(selectedType, widgetState)
                }
            }
            else -> {
                // Do nothing
            }
        }
    }

    private suspend fun updateWidgetStateWithTypes(selectedType: Id, widgetState: TypeTemplatesWidgetUI.Data) {
        val objectTypes = widgetState.objectTypes
        val isTypePresent = objectTypes.filterIsInstance<TemplateObjectTypeView.Item>()
            .any { it.type.id == selectedType }
        if (objectTypes.isNotEmpty() && isTypePresent) {
            updateExistingTypes(selectedType, widgetState)
        } else {
            fetchAndProcessObjectTypes(selectedType, widgetState)
        }
    }

    private fun updateExistingTypes(selectedType: Id, widgetState: TypeTemplatesWidgetUI.Data) {
        val types = widgetState.objectTypes.map { it.updateSelectionState(selectedType) }
        typeTemplatesWidgetState.value = widgetState.copy(objectTypes = types)
    }

    private fun TemplateObjectTypeView.updateSelectionState(selectedType: Id): TemplateObjectTypeView {
        return when (this) {
            is TemplateObjectTypeView.Item -> this.copy(isSelected = this.type.id == selectedType)
            is TemplateObjectTypeView.Search -> this
        }
    }

    private suspend fun fetchAndProcessObjectTypes(selectedType: Id, widgetState: TypeTemplatesWidgetUI.Data) {
        // Get space UX type for context-aware filtering
        val spaceView = spaceViews.get(vmParams.space)
        val spaceUxType = spaceView?.spaceUxType
        val createLayouts = getCreateObjectLayouts(spaceUxType)

        val allTypes = storeOfObjectTypes.getAll()
        val filteredTypes = allTypes.filter { type ->
            val layout = type.recommendedLayout
            layout != null && createLayouts.contains(layout)
                && type.recommendedLayout != ObjectType.Layout.PARTICIPANT
                && type.uniqueKey != ObjectTypeIds.TEMPLATE
        }
        val isChatSpace = spaceUxType == SpaceUxType.CHAT || spaceUxType == SpaceUxType.ONE_TO_ONE
        val sortedTypes = filteredTypes.sortByTypePriority(isChatSpace)
        val list = buildList {
            add(TemplateObjectTypeView.Search)
            addAll(sortedTypes.toTemplateObjectTypeViewItems(selectedType))
        }
        typeTemplatesWidgetState.value = widgetState.copy(objectTypes = list)
    }

    fun proceedWithSelectedTemplate(
        template: Id,
        typeId: Id,
        typeKey: Id
    ) {
        Timber.d("proceedWithSelectedTemplate, template:[$template], typeId:[$typeId], typeKey:[$typeKey]")
        val templateView = TemplateView.Template(
            id = template,
            targetTypeId = TypeId(typeId),
            targetTypeKey = TypeKey(typeKey),
            name = ""
        )
        onTypeTemplatesWidgetAction(action = TypeTemplatesWidgetUIAction.TemplateClick(templateView))
    }

    /**
     * Gets the space's default object type with fallback to PAGE type.
     * Used when viewer's type is deleted or not available.
     */
    private suspend fun getSpaceDefaultType(): ObjectWrapper.Type? {
        val typeKey = getDefaultObjectType.async(vmParams.space).getOrNull()?.type
        if (typeKey == null) {
            Timber.w("Space default type key is null, falling back to VIEW_DEFAULT_OBJECT_TYPE type")
            return storeOfObjectTypes.get(VIEW_DEFAULT_OBJECT_TYPE)
        }
        val typeObject = storeOfObjectTypes.getByKey(typeKey.key)
        if (typeObject == null) {
            Timber.w("Space default type not found in store, falling back to VIEW_DEFAULT_OBJECT_TYPE type")
            return storeOfObjectTypes.get(VIEW_DEFAULT_OBJECT_TYPE)
        }
        return typeObject
    }

    /**
     * Resolves type and template with fallback to space default.
     *
     * When the viewer's type is deleted (resolvedType is null), falls back to the space
     * default type AND its default template to maintain consistency between type and template.
     *
     * @param resolvedType The type resolved from the viewer (null if deleted)
     * @param defaultTemplate The template from the viewer's type (may be invalid if type is deleted)
     * @param userChosenTemplate Optional template explicitly chosen by the user (highest priority)
     * @return Pair of (finalType, finalTemplate) where both are aligned to the same type
     */
    private suspend fun resolveTypeAndTemplateWithFallback(
        resolvedType: ObjectWrapper.Type?,
        defaultTemplate: Id?,
        userChosenTemplate: Id?
    ): Pair<ObjectWrapper.Type?, Id?> {
        // User-chosen template always has highest priority
        if (userChosenTemplate != null) {
            val finalType = resolvedType ?: getSpaceDefaultType()
            return Pair(finalType, userChosenTemplate)
        }

        // If type is null (deleted), use space default type AND its template
        if (resolvedType == null) {
            val spaceDefaultType = getSpaceDefaultType()
            val spaceDefaultTemplate = spaceDefaultType?.defaultTemplateId
            return Pair(spaceDefaultType, spaceDefaultTemplate)
        }

        // Normal case: type exists, use its template
        return Pair(resolvedType, defaultTemplate)
    }

    /**
     * Cleans up a viewer's defaultObjectType when it references a deleted type.
     * This is a fire-and-forget operation that updates the view asynchronously.
     *
     * @param ctx The context (object) ID
     * @param dv The dataview block ID
     * @param viewer The viewer with stale type ID
     */
    private fun cleanupDeletedTypeInViewer(
        ctx: Id,
        dv: Id,
        viewer: DVViewer
    ) {
        viewModelScope.launch {
            try {
                Timber.d("Cleaning up deleted type in viewer ${viewer.id}")
                val updatedViewer = viewer.copy(defaultObjectType = null)
                onEvent(
                    ViewerEvent.UpdateView(
                        ctx = ctx,
                        dv = dv,
                        viewer = updatedViewer,
                        onResult = {
                            Timber.d("Successfully cleaned up deleted type in viewer ${viewer.id}")
                        }
                    )
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to cleanup deleted type in viewer ${viewer.id}")
            }
        }
    }

    /**
     * Refreshes the TypeTemplatesWidget to show only valid types.
     * Called when a deleted type is detected to update the UI.
     */
    private fun refreshTypeTemplatesWidgetTypes() {
        viewModelScope.launch {
            when (val widgetState = typeTemplatesWidgetState.value) {
                is TypeTemplatesWidgetUI.Data -> {
                    // Trigger refresh of available types using existing logic
                    val selectedType = selectedTypeFlow.value?.id ?: VIEW_DEFAULT_OBJECT_TYPE
                    updateTypesForTypeTemplatesWidget(selectedType)
                }

                else -> {
                    // Widget not active, do nothing
                }
            }
        }
    }

    private fun processTemplates(
        templates: List<ObjectWrapper.Basic>,
        viewerDefType: ObjectWrapper.Type?,
        viewerDefTemplate: Id?
    ): List<TemplateView> {

        if (viewerDefType == null) {
            Timber.w("processTemplates, Viewer def type is null (likely deleted type, fallback should have been applied)")
            // Refresh widget to show valid types
            refreshTypeTemplatesWidgetTypes()
            return emptyList()
        }

        val viewerDefTypeId = viewerDefType.id
        val viewerDefTypeKey = TypeKey(viewerDefType.uniqueKey)

        val isTemplatesAllowed = viewerDefType.isTemplatesAllowed()

        val state = typeTemplatesWidgetState.value

        val isPossibleToChangeType = when (state) {
            is TypeTemplatesWidgetUI.Data -> state.isPossibleToChangeType
            is TypeTemplatesWidgetUI.Init -> false
        }

        typeTemplatesWidgetState.value = when (state) {
            is TypeTemplatesWidgetUI.Data -> state.copy(
                isPossibleToChangeTemplate = isTemplatesAllowed
            )
            is TypeTemplatesWidgetUI.Init -> state
        }

        val newTemplate = if (!isTemplatesAllowed) {
            Timber.d("processTemplates, Templates are not allowed for this type")
            emptyList()
        } else {
            listOf(
                TemplateView.New(
                    targetTypeId = TypeId(viewerDefTypeId),
                    targetTypeKey = viewerDefTypeKey
                )
            )
        }

        val blankTemplate = TemplateView.Blank(
            id = TemplateView.DEFAULT_TEMPLATE_ID_BLANK,
            targetTypeId = TypeId(viewerDefTypeId),
            targetTypeKey = viewerDefTypeKey,
            layout = viewerDefType.recommendedLayout?.code ?: ObjectType.Layout.BASIC.code,
            isDefault = viewerDefTemplate.isNullOrEmpty()
                    || viewerDefTemplate == TemplateView.DEFAULT_TEMPLATE_ID_BLANK,
        )

        return if (templates.size == 1 && templates.first().id == viewerDefTemplate) {
            templates.map { objTemplate ->
                objTemplate.toTemplateView(
                    urlBuilder = urlBuilder,
                    coverImageHashProvider = coverImageHashProvider,
                    viewerDefTemplateId = viewerDefTemplate,
                    viewerDefTypeKey = viewerDefTypeKey
                )
            } + newTemplate
        } else {
            buildList {
                if (isPossibleToChangeType) {
                    add(blankTemplate)
                }
                addAll(
                    templates.map { objTemplate ->
                        objTemplate.toTemplateView(
                            urlBuilder = urlBuilder,
                            coverImageHashProvider = coverImageHashProvider,
                            viewerDefTemplateId = viewerDefTemplate,
                            viewerDefTypeKey = viewerDefTypeKey
                        )
                    }
                )
                addAll(newTemplate)
            }
        }
    }

    private suspend fun proceedWithCreatingTemplate(targetTypeId: Id, targetTypeKey: Id) {
        delay(DELAY_BEFORE_CREATING_TEMPLATE)
        val params = CreateTemplate.Params(
            targetObjectTypeId = targetTypeId,
            spaceId = vmParams.space
        )
        createTemplate.async(params).fold(
            onSuccess = { createObjectResult ->
                viewModelScope.logEvent(
                    state = stateReducer.state.value,
                    analytics = analytics,
                    event = ObjectStateAnalyticsEvent.CREATE_TEMPLATE,
                    type = storeOfObjectTypes.get(targetTypeId)?.sourceObject,
                    spaceParams = provideParams(vmParams.space.id)
                )
                proceedWithOpeningTemplate(
                    target = createObjectResult.id,
                    targetTypeId = targetTypeId,
                    targetTypeKey = targetTypeKey
                )
            },
            onFailure = { e ->
                Timber.e(e, "Error while creating new template")
                toast("Error while creating new template")
            }
        )
    }

    fun onEditTemplateButtonClicked() {
        typeTemplatesWidgetState.value = when (val value = typeTemplatesWidgetState.value) {
            is TypeTemplatesWidgetUI.Data -> value.enterEditing()
            else -> value
        }
    }

    fun onDoneTemplateButtonClicked() {
        Timber.d("onDoneTemplateButtonClicked, ")
        typeTemplatesWidgetState.value = when (val value = typeTemplatesWidgetState.value) {
            is TypeTemplatesWidgetUI.Data -> value.exitEditing()
            else -> value
        }
    }

    fun onMoreTemplateButtonClicked(template: TemplateView) {
        Timber.d("onMoreTemplateButtonClicked, template:[$template]")
        val uiState = typeTemplatesWidgetState.value as? TypeTemplatesWidgetUI.Data ?: return
        typeTemplatesWidgetState.value = if (uiState.moreMenuItem != null) {
            uiState.hideMoreMenu()
        } else {
            uiState.showMoreMenu(template)
        }
    }

    fun onDismissTemplatesWidget() {
        Timber.d("onDismissTemplatesWidget, ")
        typeTemplatesWidgetState.value = when (val state = typeTemplatesWidgetState.value) {
            is TypeTemplatesWidgetUI.Data -> {
                when {
                    state.moreMenuItem != null -> state.hideMoreMenu()
                    state.showWidget -> {
                        selectedTypeFlow.value = null
                        TypeTemplatesWidgetUI.Init()
                    }
                    else -> state
                }
            }
            is TypeTemplatesWidgetUI.Init -> TypeTemplatesWidgetUI.Init()
        }
    }

    private fun hideTemplatesWidget() {
        selectedTypeFlow.value = null
        typeTemplatesWidgetState.value = TypeTemplatesWidgetUI.Init()
    }

    fun onMoreMenuClicked(click: TemplateMenuClick) {
        Timber.d("onMoreMenuClicked, click:[$click]")
        viewModelScope.launch {
            when (click) {
                is TemplateMenuClick.Default -> proceedWithUpdatingViewDefaultTemplate()
                is TemplateMenuClick.Delete -> proceedWithDeletionTemplate()
                is TemplateMenuClick.Duplicate -> proceedWithDuplicateTemplate()
                is TemplateMenuClick.Edit -> proceedWithEditingTemplate()
            }
        }
    }

    private suspend fun proceedWithUpdatingViewDefaultTemplate() {
        when (val uiState = typeTemplatesWidgetState.value) {
            is TypeTemplatesWidgetUI.Data -> {
                when (val templateToSetAsDefault = uiState.moreMenuItem) {
                    is TemplateView.Blank -> {
                        typeTemplatesWidgetState.value = uiState.exitEditing()
                        proceedWithUpdateViewer(viewerId = uiState.viewerId) {
                            it.copy(defaultTemplate = templateToSetAsDefault.id)
                        }
                    }
                    is TemplateView.Template -> {
                        typeTemplatesWidgetState.value = uiState.exitEditing()
                        proceedWithUpdateViewer(viewerId = uiState.viewerId) {
                            it.copy(defaultTemplate = templateToSetAsDefault.id)
                        }
                    }
                    else -> Unit
                }
            }
            is TypeTemplatesWidgetUI.Init -> Unit
        }
    }

    private fun proceedWithDuplicateTemplate() {
        val uiState =  (typeTemplatesWidgetState.value as? TypeTemplatesWidgetUI.Data) ?: return
        val template = (uiState.moreMenuItem as? TemplateView.Template) ?: return
        typeTemplatesWidgetState.value = uiState.exitEditing()
        val params = DuplicateObjects.Params(
            ids = listOf(template.id)
        )
        viewModelScope.launch {
            duplicateObjects.async(params).fold(
                onSuccess = { ids ->
                    logEvent(
                        state = stateReducer.state.value,
                        analytics = analytics,
                        event = ObjectStateAnalyticsEvent.DUPLICATE_TEMPLATE,
                        type = template.targetTypeId.id,
                        spaceParams = provideParams(vmParams.space.id)
                    )
                    Timber.d("Successfully duplicated templates: $ids")
                },
                onFailure = { e ->
                    Timber.e(e, "Error while duplicating templates")
                    toast("Error while duplicating templates")
                }
            )
        }
    }

    private fun proceedWithDeletionTemplate() {
        val uiState = (typeTemplatesWidgetState.value as? TypeTemplatesWidgetUI.Data) ?: return
        val template = (uiState.moreMenuItem as? TemplateView.Template) ?: return
        typeTemplatesWidgetState.value = uiState.exitEditing()
        val params = SetObjectListIsArchived.Params(
            targets = listOf(template.id),
            isArchived = true
        )
        viewModelScope.launch {
            setObjectListIsArchived.async(params).fold(
                onSuccess = { ids ->
                    logEvent(
                        state = stateReducer.state.value,
                        analytics = analytics,
                        event = ObjectStateAnalyticsEvent.DELETE_TEMPLATE,
                        type = template.targetTypeKey.key,
                        spaceParams = provideParams(vmParams.space.id)
                    )
                    Timber.d("Successfully archived templates: $ids")
                },
                onFailure = { e ->
                    Timber.e(e, "Error while deleting templates")
                    toast("Error while deleting templates")
                }
            )
        }
    }

    private suspend fun proceedWithEditingTemplate() {
        val uiState = (typeTemplatesWidgetState.value as? TypeTemplatesWidgetUI.Data) ?: return
        val template = uiState.moreMenuItem ?: return
        typeTemplatesWidgetState.value = uiState.exitEditing()
        when (template) {
            is TemplateView.Template -> {
                delay(DELAY_BEFORE_CREATING_TEMPLATE)
                proceedWithOpeningTemplate(
                    target = template.id,
                    targetTypeId = template.targetTypeId.id,
                    targetTypeKey = template.targetTypeKey.key
                )
                viewModelScope.launch {
                    logEvent(
                        state = stateReducer.state.value,
                        analytics = analytics,
                        event = ObjectStateAnalyticsEvent.EDIT_TEMPLATE,
                        type = template.targetTypeKey.key,
                        spaceParams = provideParams(vmParams.space.id)
                    )
                }
            }
            else -> Unit
        }
    }
    //endregion

    // region VIEWS
    fun onViewersWidgetAction(action: ViewersWidgetUi.Action) {
        Timber.d("onViewersWidgetAction, action:[$action]")
        val state = stateReducer.state.value.dataViewState() ?: return
        when (action) {
            ViewersWidgetUi.Action.Dismiss -> {
                viewersWidgetState.value = viewersWidgetState.value.copy(
                    showWidget = false,
                    isEditing = false
                )
            }
            ViewersWidgetUi.Action.DoneMode -> {
                viewersWidgetState.value = viewersWidgetState.value.copy(isEditing = false)
            }
            ViewersWidgetUi.Action.EditMode -> {
                viewersWidgetState.value = viewersWidgetState.value.copy(isEditing = true)
            }
            is ViewersWidgetUi.Action.Delete -> {
                viewModelScope.launch {
                    val startTime = System.currentTimeMillis()
                    onEvent(
                        ViewerEvent.Delete(
                            ctx = vmParams.ctx,
                            dv = state.dataViewBlock.id,
                            viewer = action.viewer,
                            onResult = {
                                logEvent(
                                    state = state,
                                    analytics = analytics,
                                    event = ObjectStateAnalyticsEvent.REMOVE_VIEW,
                                    startTime = startTime,
                                    spaceParams = provideParams(vmParams.space.id)
                                )
                            }
                        )
                    )
                }
            }
            is ViewersWidgetUi.Action.Edit -> {
                widgetViewerId.value = action.id
                showViewerEditWidget()
            }
            is ViewersWidgetUi.Action.OnMove -> {
                if (action.from == action.to) return
                viewModelScope.launch {
                    val startTime = System.currentTimeMillis()
                    val type = action.currentViews[action.to].type
                    viewerDelegate.onEvent(
                        ViewerEvent.UpdatePosition(
                            ctx = vmParams.ctx,
                            dv = state.dataViewBlock.id,
                            viewer = action.currentViews[action.to].id,
                            position = action.to,
                            onResult = {
                                logEvent(
                                    state = state,
                                    analytics = analytics,
                                    event = ObjectStateAnalyticsEvent.REPOSITION_VIEW,
                                    startTime = startTime,
                                    type = type.formattedName,
                                    spaceParams = provideParams(vmParams.space.id)
                                )
                            }
                        )
                    )
                    session.currentViewerId.value = action.currentViews.firstOrNull()?.id
                }
            }
            is ViewersWidgetUi.Action.SetActive -> {
                val startTime = System.currentTimeMillis()
                viewModelScope.launch {
                    onEvent(ViewerEvent.SetActive(
                        viewer = action.id,
                        onResult = {
                            viewersWidgetState.value = viewersWidgetState.value.copy(
                                showWidget = false,
                                isEditing = false
                            )
                            logEvent(
                                state = state,
                                analytics = analytics,
                                event = ObjectStateAnalyticsEvent.SWITCH_VIEW,
                                startTime = startTime,
                                type = action.type.formattedName,
                                spaceParams = provideParams(vmParams.space.id)
                            )
                        }
                    ))
                }
            }

            ViewersWidgetUi.Action.Plus -> {
                val activeView = state.viewerByIdOrFirst(session.currentViewerId.value) ?: return
                val newView = activeView.copy(
                    id = "",
                    name = "",
                    type = DVViewerType.GRID,
                    filters = emptyList(),
                    sorts = emptyList()
                )
                viewModelScope.launch {
                    val startTime = System.currentTimeMillis()
                    viewerDelegate.onEvent(
                        ViewerEvent.AddNew(
                            ctx = vmParams.ctx,
                            dv = state.dataViewBlock.id,
                            viewer = newView,
                            onResult = { newViewId ->
                                logEvent(
                                    state = state,
                                    analytics = analytics,
                                    event = ObjectStateAnalyticsEvent.ADD_VIEW,
                                    startTime = startTime,
                                    type = newView.type.formattedName,
                                    spaceParams = provideParams(vmParams.space.id)
                                )
                                widgetViewerId.value = newViewId
                                showViewerEditWidgetForNewView()
                            }
                        )
                    )
                }
            }
        }
    }

    fun openViewerFilters(viewerId: Id? = null) {
        val state = stateReducer.state.value.dataViewState() ?: return
        if (state.viewers.isNotEmpty()) {
            if (isRestrictionPresent(DataViewRestriction.VIEWS)) {
                toast(NOT_ALLOWED)
            } else {
                val viewer = viewerId ?: state.viewerByIdOrFirst(session.currentViewerId.value)?.id ?: return
                dispatch(
                    ObjectSetCommand.Modal.ModifyViewerFilters(
                        ctx = vmParams.ctx,
                        viewer = viewer
                    )
                )
            }
        } else {
            toast(DATA_VIEW_HAS_NO_VIEW_MSG)
        }
    }

    fun openViewerSorts(viewerId: Id? = null) {
        val state = stateReducer.state.value.dataViewState() ?: return
        if (state.viewers.isNotEmpty()) {
            if (isRestrictionPresent(DataViewRestriction.VIEWS)) {
                toast(NOT_ALLOWED)
            } else {
                val viewer = viewerId ?: state.viewerByIdOrFirst(session.currentViewerId.value)?.id ?: return
                dispatch(
                    ObjectSetCommand.Modal.ModifyViewerSorts(
                        ctx = vmParams.ctx,
                        viewer = viewer
                    )
                )
            }
        } else {
            toast(DATA_VIEW_HAS_NO_VIEW_MSG)
        }
    }

    fun onViewerEditWidgetAction(action: ViewEditAction) {
        Timber.d("onViewerEditWidgetAction, action:[$action]")
        when (action) {
            ViewEditAction.Dismiss -> { hideViewerEditWidget() }
            is ViewEditAction.Filters -> {
                viewModelScope.launch {
                    delay(DELAY_BEFORE_CREATING_TEMPLATE)
                    openViewerFilters(viewerId = action.id)
                }
            }
            is ViewEditAction.Layout -> {
                viewerLayoutWidgetState.value = viewerLayoutWidgetState.value.copy(showWidget = true)
            }
            is ViewEditAction.Relations -> {
                viewModelScope.launch {
                    delay(DELAY_BEFORE_CREATING_TEMPLATE)
                    onViewerSettingsClicked(action.id)
                }
            }
            is ViewEditAction.Sorts -> {
                viewModelScope.launch {
                    delay(DELAY_BEFORE_CREATING_TEMPLATE)
                    openViewerSorts(viewerId = action.id)
                }
            }
            is ViewEditAction.UpdateName -> {
                val state = stateReducer.state.value.dataViewState() ?: return
                val viewer = state.viewerById(action.id) ?: return
                viewModelScope.launch {
                    viewerDelegate.onEvent(
                        ViewerEvent.UpdateView(
                            ctx = vmParams.ctx,
                            dv = state.dataViewBlock.id,
                            viewer = viewer.copy(name = action.name),
                            onResult = {}
                        )
                    )
                }
            }

            ViewEditAction.More -> {
                updateViewerEditMoreMenu()
            }
            is ViewEditAction.Delete -> {
                val state = stateReducer.state.value.dataViewState() ?: return
                hideViewerEditWidget()
                viewModelScope.launch {
                    val startTime = System.currentTimeMillis()
                    viewerDelegate.onEvent(
                        ViewerEvent.Delete(
                            ctx = vmParams.ctx,
                            dv = state.dataViewBlock.id,
                            viewer = action.id,
                            onResult = {
                                logEvent(
                                    state = state,
                                    analytics = analytics,
                                    event = ObjectStateAnalyticsEvent.REMOVE_VIEW,
                                    startTime = startTime,
                                    spaceParams = provideParams(vmParams.space.id)
                                )
                            }
                        )
                    )
                }
            }
            is ViewEditAction.Duplicate -> {
                val state = stateReducer.state.value.dataViewState() ?: return
                val viewer = state.viewerById(action.id) ?: return
                viewModelScope.launch {
                    val startTime = System.currentTimeMillis()
                    viewerDelegate.onEvent(
                        ViewerEvent.Duplicate(
                            ctx = vmParams.ctx,
                            dv = state.dataViewBlock.id,
                            viewer = viewer,
                            onResult = {
                                logEvent(
                                    state = state,
                                    analytics = analytics,
                                    event = ObjectStateAnalyticsEvent.DUPLICATE_VIEW,
                                    startTime = startTime,
                                    type = viewer.type.formattedName,
                                    spaceParams = provideParams(vmParams.space.id)
                                )
                            }
                        )
                    )
                }
            }
        }
    }

    private fun showViewerEditWidget() {
        val uiState = viewerEditWidgetState.value
        viewerEditWidgetState.value = when (uiState) {
            is ViewerEditWidgetUi.Data -> uiState.copy(showWidget = true)
            ViewerEditWidgetUi.Init -> uiState
        }
    }

    private fun showViewerEditWidgetForNewView() {
        val show = (viewerEditWidgetState.value as? ViewerEditWidgetUi.Data)?.copy(showWidget = true, isNewMode = true)
            ?: ViewerEditWidgetUi.Init
        viewerEditWidgetState.value = show
    }

    private fun hideViewerEditWidget() {
        widgetViewerId.value = null
        viewerEditWidgetState.value = ViewerEditWidgetUi.Init
    }

    private fun updateViewerEditMoreMenu() {
        when (val value = viewerEditWidgetState.value) {
            is ViewerEditWidgetUi.Data -> {
                val isMoreMenuVisible = value.showMore
                viewerEditWidgetState.value = value.copy(showMore = !isMoreMenuVisible)
            }
            ViewerEditWidgetUi.Init -> {}
        }
    }
    //endregion

    // region CREATE OBJECT
    fun proceedWithDataViewObjectCreate(typeChosenBy: TypeKey? = null, templateId: Id? = null) {
        Timber.d("proceedWithDataViewObjectCreate, typeChosenBy:[$typeChosenBy], templateId:[$templateId]")

        if (isRestrictionPresent(DataViewRestriction.CREATE_OBJECT)) {
            toast(NOT_ALLOWED)
            return
        }

        val state = stateReducer.state.value.dataViewState() ?: return

        viewModelScope.launch {
            when (state) {
                is ObjectState.DataView.Collection -> {
                    proceedWithAddingObjectToCollection(
                        typeChosenByUser = typeChosenBy,
                        templateChosenBy = templateId
                    )
                }
                is ObjectState.DataView.Set -> {
                    proceedWithCreatingSetObject(
                        currentState = state,
                        templateChosenBy = templateId
                    )
                }

                is ObjectState.DataView.TypeSet -> {
                    proceedWithCreatingObjectTypeSetObject(
                        currentState = state,
                        templateChosenBy = templateId
                    )
                }
            }
        }
    }

    //region Viewer Layout Widget
    fun onViewerLayoutWidgetAction(action: ViewerLayoutWidgetUi.Action) {
        Timber.d("onViewerLayoutWidgetAction, action:[$action]")
        when (action) {
            ViewerLayoutWidgetUi.Action.Dismiss -> {
                val isCoverMenuVisible = viewerLayoutWidgetState.value.showCoverMenu
                viewerLayoutWidgetState.value = if (isCoverMenuVisible) {
                    viewerLayoutWidgetState.value.copy(showCoverMenu = false)
                } else {
                    viewerLayoutWidgetState.value.copy(
                        showWidget = false,
                        showCardSize = false,
                        showCoverMenu = false
                    )
                }
            }
            ViewerLayoutWidgetUi.Action.CardSizeMenu -> {
                val isCardSizeMenuVisible = viewerLayoutWidgetState.value.showCardSize
                viewerLayoutWidgetState.value =
                    viewerLayoutWidgetState.value.copy(showCardSize = !isCardSizeMenuVisible)
            }
            ViewerLayoutWidgetUi.Action.CoverMenu -> {
                val isCoverMenuVisible = viewerLayoutWidgetState.value.showCoverMenu
                viewerLayoutWidgetState.value =
                    viewerLayoutWidgetState.value.copy(showCoverMenu = !isCoverMenuVisible)
            }
            is ViewerLayoutWidgetUi.Action.FitImage -> {
                viewModelScope.launch {
                    proceedWithUpdateViewer(
                        viewerId = viewerLayoutWidgetState.value.viewer
                    ) { it.copy(coverFit = action.toggled) }
                }
            }
            is ViewerLayoutWidgetUi.Action.Icon -> {
                viewModelScope.launch {
                    proceedWithUpdateViewer(
                        viewerId = viewerLayoutWidgetState.value.viewer
                    ) { it.copy(hideIcon = !action.toggled) }
                }
            }
            is ViewerLayoutWidgetUi.Action.CardSize -> {
                viewerLayoutWidgetState.value =
                    viewerLayoutWidgetState.value.copy(showCardSize = false)
                when (action.cardSize) {
                    ViewerLayoutWidgetUi.State.CardSize.Small -> {
                        viewModelScope.launch {
                            proceedWithUpdateViewer(
                                viewerId = viewerLayoutWidgetState.value.viewer
                            ) { it.copy(cardSize = DVViewerCardSize.SMALL) }
                        }
                    }
                    ViewerLayoutWidgetUi.State.CardSize.Large -> {
                        viewModelScope.launch {
                            proceedWithUpdateViewer(
                                viewerId = viewerLayoutWidgetState.value.viewer
                            ) { it.copy(cardSize = DVViewerCardSize.LARGE) }
                        }
                    }
                }
            }
            is ViewerLayoutWidgetUi.Action.ImagePreviewUpdate -> {
                when (action.item) {
                    is ViewerLayoutWidgetUi.State.ImagePreview.PageCover -> {
                        val itemIsChecked = action.item.isChecked
                        if (!itemIsChecked) {
                            viewModelScope.launch {
                                proceedWithUpdateViewer(
                                    viewerId = viewerLayoutWidgetState.value.viewer
                                ) { it.copy(coverRelationKey = action.item.relationKey.key) }
                            }
                        } else {
                            Timber.i("Page cover is already set")
                        }
                    }

                    is ViewerLayoutWidgetUi.State.ImagePreview.Custom -> {
                        val itemIsChecked = action.item.isChecked
                        if (!itemIsChecked) {
                            viewModelScope.launch {
                                proceedWithUpdateViewer(
                                    viewerId = viewerLayoutWidgetState.value.viewer
                                ) { it.copy(coverRelationKey = action.item.relationKey.key) }
                            }
                        } else {
                            Timber.i("Custom cover [${action.item.relationKey.key}] is already set")
                        }
                    }

                    is ViewerLayoutWidgetUi.State.ImagePreview.None -> {
                        val itemIsChecked = action.item.isChecked
                        if (!itemIsChecked) {
                            viewModelScope.launch {
                                proceedWithUpdateViewer(
                                    viewerId = viewerLayoutWidgetState.value.viewer
                                ) { it.copy(coverRelationKey = null) }
                            }
                        } else {
                            Timber.i("No cover is already set")
                        }
                    }
                }
            }
            is ViewerLayoutWidgetUi.Action.Type -> {
                viewModelScope.launch {
                    proceedWithUpdateViewer(
                        action = {
                            val startTime = System.currentTimeMillis()
                            viewModelScope.launch {
                                logEvent(
                                    state = stateReducer.state.value,
                                    analytics = analytics,
                                    event = ObjectStateAnalyticsEvent.CHANGE_VIEW_TYPE,
                                    startTime = startTime,
                                    type = action.type.formattedName,
                                    spaceParams = provideParams(vmParams.space.id)
                                )
                            }
                        },
                        viewerId = viewerLayoutWidgetState.value.viewer
                    ) { it.copy(type = action.type) }
                }
            }

            ViewerLayoutWidgetUi.Action.DismissCoverMenu -> viewerLayoutWidgetState.value =
                viewerLayoutWidgetState.value.copy(
                    showCoverMenu = false
                )
        }
    }

    private suspend fun proceedWithUpdateViewer(
        action: () -> Unit = {},
        viewerId: Id?,
        update: (DVViewer) -> DVViewer
    ) {
        val state = stateReducer.state.value.dataViewState() ?: return
        val viewer = state.viewerById(viewerId)
        if (viewer == null) {
            Timber.e("Couldn't find viewer by id: ${viewerLayoutWidgetState.value.viewer}")
            return
        }
        viewerDelegate.onEvent(
            ViewerEvent.UpdateView(
                ctx = vmParams.ctx,
                dv = state.dataViewBlock.id,
                viewer = update.invoke(viewer),
                onResult = action
            )
        )
    }
    //endregion

    //region SYNC STATUS
    val spaceSyncStatus = MutableStateFlow<SpaceSyncAndP2PStatusState>(SpaceSyncAndP2PStatusState.Init)
    val syncStatusWidget = MutableStateFlow<SyncStatusWidgetState>(SyncStatusWidgetState.Hidden)

    fun onSyncStatusBadgeClicked() {
        Timber.d("onSyncStatusBadgeClicked, ")
        syncStatusWidget.value = spaceSyncStatus.value.toSyncStatusWidgetState()
    }

    private fun proceedWithObservingSyncStatus() {
        jobs += viewModelScope.launch {
            spaceSyncAndP2PStatusProvider
                .observe()
                .catch {
                    Timber.e(it, "Error while observing sync status")
                }
                .collect { syncAndP2pState ->
                    spaceSyncStatus.value = syncAndP2pState
                    syncStatusWidget.value = syncStatusWidget.value.updateStatus(syncAndP2pState)
                }
        }
    }

    fun onSyncWidgetDismiss() {
        syncStatusWidget.value = SyncStatusWidgetState.Hidden
    }

    fun onUpdateAppClick() {
        dispatch(command = ObjectSetCommand.Intent.OpenAppStore)
    }
    //endregion

    //region Date Field
    private suspend fun handleReadOnlyValue(
        timeInMillis: TimeInMillis?,
    ) {
        if ((timeInMillis != null)) {
            fieldParser.getDateObjectByTimeInSeconds(
                timeInSeconds = timeInMillis / 1000,
                spaceId = vmParams.space,
                actionSuccess = { obj ->
                    proceedWithNavigation(obj = obj)
                },
                actionFailure = {
                    toast("Error while parsing date object")
                    Timber.e(it, "Error while parsing date object")
                }
            )
        } else {
            toast("Date is not set")
        }
    }

    fun onOpenDateObjectByTimeInMillis(timeInMillis: TimeInMillis) {
        Timber.d("onOpenDateObjectByTimeInMillis, timeInMillis:[$timeInMillis]")
        viewModelScope.launch {
            handleReadOnlyValue(timeInMillis = timeInMillis)
        }
    }
    //endregion

    //region SET OBJECT NAME BOTTOM SHEET

    /**
     * Shows the set object name bottom sheet for a newly created object.
     */
    fun showSetObjectNameSheet(
        objectId: Id,
        icon: ObjectIcon,
        isIconChangeAllowed: Boolean,
        targetBlockId: Id? = null
    ) {
        _setObjectNameState.value = SetObjectNameState(
            isVisible = true,
            targetObjectId = objectId,
            currentIcon = icon,
            inputText = "",
            isIconChangeAllowed = isIconChangeAllowed,
            targetBlockId = targetBlockId
        )
    }

    /**
     * Called when user types in the name field. Auto-saves to backend.
     */
    fun onSetObjectNameChanged(text: String) {
        val state = _setObjectNameState.value
        val targetId = state.targetObjectId ?: return

        _setObjectNameState.value = state.copy(inputText = text)

        viewModelScope.launch {
            if (state.targetBlockId != null) {
                // Note object: update text block content
                updateText(
                    UpdateText.Params(
                        context = targetId,
                        target = state.targetBlockId,
                        text = text,
                        marks = emptyList()
                    )
                ).process(
                    failure = { Timber.e(it, "Error while updating note text block") },
                    success = { /* saved successfully */ }
                )
            } else {
                // Other layouts: update name relation
                setObjectDetails(
                    UpdateDetail.Params(
                        target = targetId,
                        key = Relations.NAME,
                        value = text
                    )
                ).process(
                    failure = { Timber.e(it, "Error while updating object name") },
                    success = { /* saved successfully */ }
                )
            }
        }
    }

    /**
     * Called when bottom sheet is dismissed (Done pressed or swipe).
     * Triggers scroll to the object.
     */
    fun onSetObjectNameDismissed() {
        val objectId = _setObjectNameState.value.targetObjectId
        _setObjectNameState.value = SetObjectNameState()

        if (objectId != null) {
            // Dispatch scroll command directly instead of using pendingScrollToObject
            dispatch(ObjectSetCommand.ScrollToObject(objectId))
        }
    }

    /**
     * Opens the icon picker for the newly created object.
     */
    fun onSetObjectNameIconClicked() {
        Timber.d("onSetObjectNameIconClicked, ")
        val objectId = _setObjectNameState.value.targetObjectId ?: return
        dispatch(
            ObjectSetCommand.Modal.OpenIconActionMenu(
                target = objectId,
                space = vmParams.space.id
            )
        )
    }

    /**
     * Opens the object in full editor from the name sheet.
     */
    fun onSetObjectNameOpenClicked() {
        val state = _setObjectNameState.value
        val targetId = state.targetObjectId ?: return

        _setObjectNameState.value = SetObjectNameState()

        viewModelScope.launch {
            proceedWithOpeningObject(
                target = targetId,
                layout = null,
                space = vmParams.space.id
            )
        }
    }

    //endregion

    companion object {
        const val NOT_ALLOWED = "Not allowed for this set"
        const val NOT_ALLOWED_CELL = "Not allowed for this cell"
        const val DATA_VIEW_HAS_NO_VIEW_MSG = "Data view has no view."
        const val TOAST_SET_NOT_EXIST = "This object doesn't exist"
        const val DELAY_BEFORE_CREATING_TEMPLATE = 200L
        private const val SUBSCRIPTION_TEMPLATES_ID = "-SUBSCRIPTION_TEMPLATES_ID"
    }

    data class Params(
        val ctx: Id,
        val space: SpaceId
    )

    data class Query(
        val state: ObjectState,
        val offset: Long,
        val currentViewerId: Id?
    )
}