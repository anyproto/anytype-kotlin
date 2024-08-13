package com.anytypeio.anytype.di.feature

import android.content.Context
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.primitives.Space
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.feature.cover.UnsplashSubComponent
import com.anytypeio.anytype.di.feature.relations.RelationAddToDataViewSubComponent
import com.anytypeio.anytype.di.feature.relations.RelationAddToObjectSubComponent
import com.anytypeio.anytype.di.feature.relations.RelationCreateFromScratchForDataViewSubComponent
import com.anytypeio.anytype.di.feature.relations.RelationCreateFromScratchForObjectSubComponent
import com.anytypeio.anytype.di.feature.sets.CreateFilterSubComponent
import com.anytypeio.anytype.di.feature.sets.ModifyFilterSubComponent
import com.anytypeio.anytype.di.feature.sets.SelectFilterRelationSubComponent
import com.anytypeio.anytype.domain.auth.interactor.ClearLastOpenedObject
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.block.interactor.sets.GetObjectTypes
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.collections.AddObjectToCollection
import com.anytypeio.anytype.domain.config.ConfigStorage
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.domain.dataview.interactor.AddDataViewViewer
import com.anytypeio.anytype.domain.dataview.interactor.CreateDataViewObject
import com.anytypeio.anytype.domain.dataview.interactor.DeleteDataViewViewer
import com.anytypeio.anytype.domain.dataview.interactor.DuplicateDataViewViewer
import com.anytypeio.anytype.domain.dataview.interactor.SetDataViewViewerPosition
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.event.interactor.EventChannel
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.icon.SetDocumentImageIcon
import com.anytypeio.anytype.domain.launch.GetDefaultObjectType
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.ActiveSpaceMemberSubscriptionContainer
import com.anytypeio.anytype.domain.multiplayer.UserPermissionProvider
import com.anytypeio.anytype.domain.`object`.ConvertObjectToCollection
import com.anytypeio.anytype.domain.`object`.DuplicateObject
import com.anytypeio.anytype.domain.`object`.DuplicateObjects
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.DefaultObjectStore
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.SetObjectIsArchived
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.objects.options.GetOptions
import com.anytypeio.anytype.domain.page.CloseBlock
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.relations.AddFileToObject
import com.anytypeio.anytype.domain.relations.AddRelationToObject
import com.anytypeio.anytype.domain.relations.DeleteRelationFromDataView
import com.anytypeio.anytype.domain.search.CancelSearchSubscription
import com.anytypeio.anytype.domain.search.DataViewSubscriptionContainer
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.domain.sets.OpenObjectSet
import com.anytypeio.anytype.domain.sets.SetQueryToObjectSet
import com.anytypeio.anytype.domain.templates.CreateTemplate
import com.anytypeio.anytype.domain.templates.GetTemplates
import com.anytypeio.anytype.domain.unsplash.DownloadUnsplashImage
import com.anytypeio.anytype.domain.unsplash.UnsplashRepository
import com.anytypeio.anytype.domain.workspace.P2PStatusChannel
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.domain.workspace.SpaceSyncStatusChannel
import com.anytypeio.anytype.presentation.analytics.AnalyticSpaceHelperDelegate
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.objects.LockedStateProvider
import com.anytypeio.anytype.presentation.relations.providers.DataViewObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.DataViewObjectValueProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectDetailProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider.Companion.DATA_VIEW_PROVIDER_TYPE
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider.Companion.INTRINSIC_PROVIDER_TYPE
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.relations.providers.RelationListProvider
import com.anytypeio.anytype.presentation.relations.providers.SetOrCollectionObjectValueProvider
import com.anytypeio.anytype.presentation.relations.providers.SetOrCollectionRelationProvider
import com.anytypeio.anytype.presentation.sets.ObjectSetDatabase
import com.anytypeio.anytype.presentation.sets.ObjectSetPaginator
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModelFactory
import com.anytypeio.anytype.presentation.sets.state.DefaultObjectStateReducer
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.sets.state.ObjectStateReducer
import com.anytypeio.anytype.presentation.sets.subscription.DataViewSubscription
import com.anytypeio.anytype.presentation.sets.subscription.DefaultDataViewSubscription
import com.anytypeio.anytype.presentation.sets.viewer.DefaultViewerDelegate
import com.anytypeio.anytype.presentation.sets.viewer.ViewerDelegate
import com.anytypeio.anytype.presentation.sync.SpaceSyncAndP2PStatusProvider
import com.anytypeio.anytype.presentation.templates.ObjectTypeTemplatesContainer
import com.anytypeio.anytype.presentation.util.CopyFileToCacheDirectory
import com.anytypeio.anytype.presentation.util.DefaultCopyFileToCacheDirectory
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.presentation.util.downloader.UriFileProvider
import com.anytypeio.anytype.providers.DefaultCoverImageHashProvider
import com.anytypeio.anytype.providers.DefaultUriFileProvider
import com.anytypeio.anytype.ui.sets.ObjectSetFragment
import dagger.Binds
import dagger.BindsInstance
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import javax.inject.Named
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow

@Subcomponent(modules = [ObjectSetModule::class])
@PerScreen
interface ObjectSetSubComponent {

    @Subcomponent.Builder
    interface Builder {
        @BindsInstance
        fun withParams(param: ObjectSetViewModel.Params) : Builder
        fun module(module: ObjectSetModule): Builder
        fun build(): ObjectSetSubComponent
    }

    fun inject(fragment: ObjectSetFragment)

    fun objectSetRecordComponent(): ObjectSetRecordSubComponent.Builder
    fun objectSetCreateBookmarkRecordComponent(): ObjectSetCreateBookmarkRecordSubComponent.Builder
    fun viewerFilterBySubComponent(): ViewerFilterSubComponent.Builder

    fun objectSetSettingsComponent(): ObjectSetSettingsSubComponent.Builder
    fun relationAddToDataViewComponent(): RelationAddToDataViewSubComponent.Builder
    fun relationCreateFromScratchForDataViewComponent(): RelationCreateFromScratchForDataViewSubComponent.Builder
    fun selectSortRelationComponent(): SelectSortRelationSubComponent.Builder
    fun selectFilterRelationComponent(): SelectFilterRelationSubComponent.Builder
    fun createFilterComponent(): CreateFilterSubComponent.Builder
    fun modifyFilterComponent(): ModifyFilterSubComponent.Builder
    fun viewerSortComponent(): ViewerSortSubComponent.Builder
    fun modifyViewerSortComponent(): ModifyViewerSortSubComponent.Builder

    fun relationTextValueComponent(): RelationTextValueSubComponent.Builder
    fun relationDataViewTextValueComponent(): RelationDataViewTextValueSubComponent.Builder

    fun relationDateValueComponent(): DefaultRelationDataValueSubComponent.Builder
    fun dataViewRelationDateValueComponent(): DataViewRelationDataValueSubComponent.Builder

    fun objectSetMenuComponent(): ObjectSetMenuComponent.Builder
    fun objectSetIconPickerComponent(): ObjectSetIconPickerComponent.Builder
    fun objectSetCoverComponent(): SelectCoverObjectSetSubComponent.Builder
    fun objectUnsplashComponent(): UnsplashSubComponent.Builder

    fun objectRelationListComponent(): ObjectRelationListComponent.Builder
    fun relationAddToObjectComponent(): RelationAddToObjectSubComponent.Builder
    fun relationCreateFromScratchForObjectComponent(): RelationCreateFromScratchForObjectSubComponent.Builder

    fun tagStatusSetComponent(): TagOrStatusValueSetComponent.Builder
    fun tagStatusDataViewComponent(): TagOrStatusValueDataViewComponent.Builder
    fun optionSetComponent(): CreateOrEditOptionSetComponent.Builder
    fun optionDataViewComponent(): CreateOrEditOptionDataViewComponent.Builder
    fun objectValueSetComponent(): ObjectValueSetComponent.Builder
    fun objectValueDataViewComponent(): ObjectValueDataViewComponent.Builder
}

@Module(
    includes = [ObjectSetModule.Bindings::class]
)
object ObjectSetModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSetDocumentImageIconUseCase(
        repo: BlockRepository
    ): SetDocumentImageIcon = SetDocumentImageIcon(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDataViewSubscription(
        dataViewSubscriptionContainer: DataViewSubscriptionContainer
    ): DataViewSubscription = DefaultDataViewSubscription(
        dataViewSubscriptionContainer
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDuplicateObjectsListUseCase(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): DuplicateObjects = DuplicateObjects(
        repo = repo,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideGetObjectTypesUseCase(
        repository: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): GetObjectTypes = GetObjectTypes(repository, dispatchers)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideObjectSetViewModelFactory(
        params: ObjectSetViewModel.Params,
        openObjectSet: OpenObjectSet,
        closeBlock: CloseBlock,
        setObjectDetails: UpdateDetail,
        updateText: UpdateText,
        interceptEvents: InterceptEvents,
        createDataViewObject: CreateDataViewObject,
        createObject: CreateObject,
        dispatcher: Dispatcher<Payload>,
        delegator: Delegator<Action>,
        urlBuilder: UrlBuilder,
        coverImageHashProvider: CoverImageHashProvider,
        session: ObjectSetSession,
        analytics: Analytics,
        downloadUnsplashImage: DownloadUnsplashImage,
        setDocCoverImage: SetDocCoverImage,
        dataViewSubscriptionContainer: DataViewSubscriptionContainer,
        setQueryToObjectSet: SetQueryToObjectSet,
        database: ObjectSetDatabase,
        paginator: ObjectSetPaginator,
        storeOfRelations: StoreOfRelations,
        objectStateReducer: ObjectStateReducer,
        dataViewSubscription: DataViewSubscription,
        @Named("object-set-store") objectStore: ObjectStore,
        addObjectToCollection: AddObjectToCollection,
        convertObjectToCollection: ConvertObjectToCollection,
        storeOfObjectTypes: StoreOfObjectTypes,
        getObjectTypes: GetObjectTypes,
        duplicateObjects: DuplicateObjects,
        templatesContainer: ObjectTypeTemplatesContainer,
        setObjectListIsArchived: SetObjectListIsArchived,
        createTemplate: CreateTemplate,
        viewerDelegate: ViewerDelegate,
        spaceManager: SpaceManager,
        storelessSubscriptionContainer: StorelessSubscriptionContainer,
        dispatchers: AppCoroutineDispatchers,
        dateProvider: DateProvider,
        permissions: UserPermissionProvider,
        clearLastOpenedObject: ClearLastOpenedObject,
        analyticSpaceHelperDelegate: AnalyticSpaceHelperDelegate,
        spaceSyncAndP2PStatusProvider: SpaceSyncAndP2PStatusProvider
    ): ObjectSetViewModelFactory = ObjectSetViewModelFactory(
        params = params,
        openObjectSet = openObjectSet,
        closeBlock = closeBlock,
        setObjectDetails = setObjectDetails,
        createDataViewObject = createDataViewObject,
        updateText = updateText,
        interceptEvents = interceptEvents,
        dispatcher = dispatcher,
        delegator = delegator,
        coverImageHashProvider = coverImageHashProvider,
        urlBuilder = urlBuilder,
        session = session,
        analytics = analytics,
        downloadUnsplashImage = downloadUnsplashImage,
        setDocCoverImage = setDocCoverImage,
        createObject = createObject,
        dataViewSubscriptionContainer = dataViewSubscriptionContainer,
        setQueryToObjectSet = setQueryToObjectSet,
        database = database,
        paginator = paginator,
        storeOfRelations = storeOfRelations,
        objectStateReducer = objectStateReducer,
        dataViewSubscription = dataViewSubscription,
        objectStore = objectStore,
        addObjectToCollection = addObjectToCollection,
        objectToCollection = convertObjectToCollection,
        storeOfObjectTypes = storeOfObjectTypes,
        getObjectTypes = getObjectTypes,
        duplicateObjects = duplicateObjects,
        templatesContainer = templatesContainer,
        setObjectListIsArchived = setObjectListIsArchived,
        viewerDelegate = viewerDelegate,
        spaceManager = spaceManager,
        createTemplate = createTemplate,
        storelessSubscriptionContainer = storelessSubscriptionContainer,
        dispatchers = dispatchers,
        dateProvider = dateProvider,
        permissions = permissions,
        analyticSpaceHelperDelegate = analyticSpaceHelperDelegate,
        spaceSyncAndP2PStatusProvider = spaceSyncAndP2PStatusProvider,
        clearLastOpenedObject = clearLastOpenedObject
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideConvertObjectToCollection(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): ConvertObjectToCollection = ConvertObjectToCollection(
        repo = repo,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun getCreateObject(
        repo: BlockRepository,
        getDefaultObjectType: GetDefaultObjectType,
        dispatchers: AppCoroutineDispatchers,
        spaceManager: SpaceManager
    ): CreateObject = CreateObject(
        repo = repo,
        getDefaultObjectType = getDefaultObjectType,
        dispatchers = dispatchers,
        spaceManager = spaceManager
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSetQueryToSet(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): SetQueryToObjectSet = SetQueryToObjectSet(repo, dispatchers)

    @JvmStatic
    @PerScreen
    @Provides
    fun provideGetDefaultPageType(
        userSettingsRepository: UserSettingsRepository,
        blockRepository: BlockRepository,
        dispatchers: AppCoroutineDispatchers,
        spaceManager: SpaceManager,
        configStorage: ConfigStorage
    ): GetDefaultObjectType = GetDefaultObjectType(
        userSettingsRepository = userSettingsRepository,
        blockRepository = blockRepository,
        dispatchers = dispatchers,
        spaceManager = spaceManager,
        configStorage = configStorage
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideOpenObjectSetUseCase(
        repo: BlockRepository,
        settings: UserSettingsRepository
    ): OpenObjectSet = OpenObjectSet(
        repo = repo,
        settings = settings
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideUpdateTextUseCase(
        repo: BlockRepository
    ): UpdateText = UpdateText(repo = repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideInterceptEventsUseCase(
        channel: EventChannel
    ): InterceptEvents = InterceptEvents(
        channel = channel,
        context = Dispatchers.IO
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideClosePageUseCase(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): CloseBlock = CloseBlock(repo, dispatchers)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideState(reducer: ObjectStateReducer): MutableStateFlow<ObjectState> = reducer.state

    @JvmStatic
    @Provides
    @PerScreen
    fun provideObjectStateReducer(): ObjectStateReducer = DefaultObjectStateReducer()

    @JvmStatic
    @Provides
    @PerScreen
    fun provideObjectSetSession(): ObjectSetSession = ObjectSetSession()

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDispatcher(): Dispatcher<Payload> = Dispatcher.Default()

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDelegator(): Delegator<Action> = Delegator.Default()

    @JvmStatic
    @Provides
    @PerScreen
    @Named(DATA_VIEW_PROVIDER_TYPE)
    fun provideDataViewObjectRelationProvider(
        state: MutableStateFlow<ObjectState>,
        storeOfRelations: StoreOfRelations
    ): ObjectRelationProvider = DataViewObjectRelationProvider(
        objectState = state,
        storeOfRelations = storeOfRelations
    )

    @JvmStatic
    @Provides
    @PerScreen
    @Named(INTRINSIC_PROVIDER_TYPE)
    fun provideObjectRelationProvider(
        state: MutableStateFlow<ObjectState>,
        storeOfRelations: StoreOfRelations
    ): ObjectRelationProvider = SetOrCollectionRelationProvider(
        objectState = state,
        storeOfRelations = storeOfRelations
    )

    @JvmStatic
    @Provides
    @PerScreen
    @Named(DATA_VIEW_PROVIDER_TYPE)
    fun dataViewObjectValueProvider(
        db: ObjectSetDatabase,
        objectState: MutableStateFlow<ObjectState>,
    ): ObjectValueProvider = DataViewObjectValueProvider(
        objectState = objectState,
        db = db
    )

    @JvmStatic
    @Provides
    @PerScreen
    @Named(INTRINSIC_PROVIDER_TYPE)
    fun setOrCollectionObjectValueProvider(
        db: ObjectSetDatabase,
        objectState: MutableStateFlow<ObjectState>,
    ): ObjectValueProvider = SetOrCollectionObjectValueProvider(
        objectState = objectState,
        db = db
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideObjectDetailProvider(
        objectState: MutableStateFlow<ObjectState>,
    ): ObjectDetailProvider = object : ObjectDetailProvider {
        override fun provide(): Map<Id, Block.Fields> {
            return when (val state = objectState.value) {
                is ObjectState.DataView -> state.details
                else -> emptyMap()
            }
        }
    }

    @JvmStatic
    @Provides
    @PerScreen
    fun provideUpdateDetailUseCase(
        repository: BlockRepository
    ): UpdateDetail = UpdateDetail(repository)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideArchiveDocumentUseCase(
        repo: BlockRepository
    ): SetObjectIsArchived = SetObjectIsArchived(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSearchObjectsUseCase(
        repo: BlockRepository
    ): SearchObjects = SearchObjects(repo = repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDeleteRelationFromDataViewUseCase(
        repo: BlockRepository
    ): DeleteRelationFromDataView = DeleteRelationFromDataView(repo = repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSetDocCoverImageUseCase(
        repo: BlockRepository
    ): SetDocCoverImage = SetDocCoverImage(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDownload(repo: UnsplashRepository): DownloadUnsplashImage = DownloadUnsplashImage(
        repo = repo
    )

    @JvmStatic
    @PerScreen
    @Provides
    fun getSetObjectListIsArchived(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): SetObjectListIsArchived = SetObjectListIsArchived(repo, dispatchers)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideGetTemplates(
        repo: BlockRepository,
        spaceManager: SpaceManager,
        dispatchers: AppCoroutineDispatchers
    ): GetTemplates = GetTemplates(
        repo = repo,
        spaceManager = spaceManager,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun getOptions(repo: BlockRepository) = GetOptions(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun objectSearchSubscriptionContainer(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers,
        channel: SubscriptionEventChannel,
        @Named("object-set-store") store: ObjectStore
    ): DataViewSubscriptionContainer = DataViewSubscriptionContainer(
        repo = repo,
        channel = channel,
        store = store,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @PerScreen
    @Named("object-set-store")
    fun provideObjectStore(): ObjectStore = DefaultObjectStore()

    @JvmStatic
    @Provides
    @PerScreen
    fun cancelSearchSubscription(
        repo: BlockRepository,
        @Named("object-set-store") store: ObjectStore
    ): CancelSearchSubscription = CancelSearchSubscription(
        repo = repo,
        store = store
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideObjectSetDatabase(
        @Named("object-set-store") store: ObjectStore
    ): ObjectSetDatabase = ObjectSetDatabase(
        store = store
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun providePaginator(): ObjectSetPaginator = ObjectSetPaginator()

    @JvmStatic
    @Provides
    @PerScreen
    fun provideAddFileToObjectUseCase(
        repo: BlockRepository
    ): AddFileToObject = AddFileToObject(repo = repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun duplicateObject(
        repo: BlockRepository
    ): DuplicateObject = DuplicateObject(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideAddObjectToCollection(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): AddObjectToCollection = AddObjectToCollection(
        repo = repo,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun lockedStateProvider() : LockedStateProvider = LockedStateProvider.DataViewLockedStateProvider

    @JvmStatic
    @Provides
    @PerScreen
    fun dataViewRelationListProvider(
        objectStateFlow: MutableStateFlow<ObjectState>
    ) : RelationListProvider = RelationListProvider.ObjectSetRelationListProvider(
        objectStates = objectStateFlow
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideAddRelationToObject(repo: BlockRepository) = AddRelationToObject(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideCopyFileToCache(
        context: Context
    ): CopyFileToCacheDirectory = DefaultCopyFileToCacheDirectory(context)

    @Module
    interface Bindings {

        @PerScreen
        @Binds
        fun bindCoverImageHashProvider(
            defaultProvider: DefaultCoverImageHashProvider
        ): CoverImageHashProvider

        @PerScreen
        @Binds
        fun bindUriFileProvider(
            defaultProvider: DefaultUriFileProvider
        ): UriFileProvider
    }

    @JvmStatic
    @Provides
    @PerScreen
    fun provideAddDataViewViewerUseCase(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): AddDataViewViewer = AddDataViewViewer(repo = repo, dispatchers = dispatchers)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideRenameDataViewViewerUseCase(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): UpdateDataViewViewer = UpdateDataViewViewer(repo = repo, dispatchers = dispatchers)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDuplicateDataViewViewerUseCase(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): DuplicateDataViewViewer = DuplicateDataViewViewer(repo = repo, dispatchers = dispatchers)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDeleteDataViewViewerUseCase(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): DeleteDataViewViewer = DeleteDataViewViewer(repo = repo, dispatchers = dispatchers)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSetDataViewViewerPositionUseCase(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): SetDataViewViewerPosition = SetDataViewViewerPosition(repo = repo, dispatchers = dispatchers)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideViewerDelegate(
        session: ObjectSetSession,
        addDataViewViewer: AddDataViewViewer,
        duplicateDataViewViewer: DuplicateDataViewViewer,
        deleteDataViewViewer: DeleteDataViewViewer,
        setDataViewViewerPosition: SetDataViewViewerPosition,
        analytics: Analytics,
        dispatcher: Dispatcher<Payload>,
        updateDataViewViewer: UpdateDataViewViewer
    ): ViewerDelegate = DefaultViewerDelegate(
        session = session,
        addDataViewViewer = addDataViewViewer,
        updateDataViewViewer = updateDataViewViewer,
        duplicateDataViewViewer = duplicateDataViewViewer,
        deleteDataViewViewer = deleteDataViewViewer,
        setDataViewViewerPosition = setDataViewViewerPosition,
        analytics = analytics,
        dispatcher = dispatcher
    )

    @Provides
    @PerScreen
    fun provideSpaceSyncStatusProvider(
        activeSpace: ActiveSpaceMemberSubscriptionContainer,
        syncChannel: SpaceSyncStatusChannel,
        p2PStatusChannel: P2PStatusChannel
    ): SpaceSyncAndP2PStatusProvider = SpaceSyncAndP2PStatusProvider.Impl(
        activeSpace = activeSpace,
        spaceSyncStatusChannel = syncChannel,
        p2PStatusChannel = p2PStatusChannel
    )
}

data class DefaultComponentParam(
    val ctx: Id,
    val space: Space
)

