package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.feature.cover.UnsplashSubComponent
import com.anytypeio.anytype.di.feature.relations.RelationAddToDataViewSubComponent
import com.anytypeio.anytype.di.feature.relations.RelationCreateFromScratchForDataViewSubComponent
import com.anytypeio.anytype.di.feature.sets.CreateFilterSubComponent
import com.anytypeio.anytype.di.feature.sets.ModifyFilterSubComponent
import com.anytypeio.anytype.di.feature.sets.SelectFilterRelationSubComponent
import com.anytypeio.anytype.di.feature.sets.viewer.ViewerCardSizeSelectSubcomponent
import com.anytypeio.anytype.di.feature.sets.viewer.ViewerImagePreviewSelectSubcomponent
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.collections.AddObjectToCollection
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.domain.dataview.interactor.CreateDataViewObject
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.event.interactor.EventChannel
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.icon.SetDocumentImageIcon
import com.anytypeio.anytype.domain.launch.GetDefaultPageType
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.ConvertObjectToCollection
import com.anytypeio.anytype.domain.`object`.DuplicateObject
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.DefaultObjectStore
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.SetObjectIsArchived
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.objects.options.GetOptions
import com.anytypeio.anytype.domain.page.CloseBlock
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.relations.AddFileToObject
import com.anytypeio.anytype.domain.relations.DeleteRelationFromDataView
import com.anytypeio.anytype.domain.search.CancelSearchSubscription
import com.anytypeio.anytype.domain.search.DataViewSubscriptionContainer
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.domain.sets.OpenObjectSet
import com.anytypeio.anytype.domain.sets.SetQueryToObjectSet
import com.anytypeio.anytype.domain.status.InterceptThreadStatus
import com.anytypeio.anytype.domain.status.ThreadStatusChannel
import com.anytypeio.anytype.domain.templates.GetTemplates
import com.anytypeio.anytype.domain.unsplash.DownloadUnsplashImage
import com.anytypeio.anytype.domain.unsplash.UnsplashRepository
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.objects.LockedStateProvider
import com.anytypeio.anytype.presentation.relations.providers.DataViewObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.DataViewObjectValueProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectDetailProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.relations.providers.RelationListProvider
import com.anytypeio.anytype.presentation.sets.ObjectSetDatabase
import com.anytypeio.anytype.presentation.sets.ObjectSetPaginator
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModelFactory
import com.anytypeio.anytype.presentation.sets.state.DefaultObjectStateReducer
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.presentation.sets.state.ObjectStateReducer
import com.anytypeio.anytype.presentation.sets.subscription.DataViewSubscription
import com.anytypeio.anytype.presentation.sets.subscription.DefaultDataViewSubscription
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.providers.DefaultCoverImageHashProvider
import com.anytypeio.anytype.ui.sets.ObjectSetFragment
import dagger.Binds
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
        fun module(module: ObjectSetModule): Builder
        fun build(): ObjectSetSubComponent
    }

    fun inject(fragment: ObjectSetFragment)

    fun objectSetRecordComponent(): ObjectSetRecordSubComponent.Builder
    fun objectSetCreateBookmarkRecordComponent(): ObjectSetCreateBookmarkRecordSubComponent.Builder
    fun viewerFilterBySubComponent(): ViewerFilterSubComponent.Builder
    fun createDataViewViewerSubComponent(): CreateDataViewViewerSubComponent.Builder
    fun editDataViewViewerComponent(): EditDataViewViewerSubComponent.Builder
    fun objectRelationValueComponent(): ObjectSetObjectRelationValueSubComponent.Builder
    fun manageViewerComponent(): ManageViewerSubComponent.Builder
    fun objectSetSettingsComponent(): ObjectSetSettingsSubComponent.Builder
    fun viewerCardSizeSelectComponent(): ViewerCardSizeSelectSubcomponent.Builder
    fun viewerImagePreviewSelectComponent(): ViewerImagePreviewSelectSubcomponent.Builder
    fun relationAddToDataViewComponent(): RelationAddToDataViewSubComponent.Builder
    fun relationCreateFromScratchForDataViewComponent(): RelationCreateFromScratchForDataViewSubComponent.Builder
    fun selectSortRelationComponent(): SelectSortRelationSubComponent.Builder
    fun selectFilterRelationComponent(): SelectFilterRelationSubComponent.Builder
    fun createFilterComponent(): CreateFilterSubComponent.Builder
    fun modifyFilterComponent(): ModifyFilterSubComponent.Builder
    fun viewerSortComponent(): ViewerSortSubComponent.Builder
    fun modifyViewerSortComponent(): ModifyViewerSortSubComponent.Builder
    fun relationTextValueComponent(): RelationTextValueSubComponent.Builder
    fun relationDateValueComponent(): RelationDataValueSubComponent.Builder

    fun objectSetMenuComponent(): ObjectSetMenuComponent.Builder
    fun objectSetIconPickerComponent(): ObjectSetIconPickerComponent.Builder
    fun objectSetCoverComponent(): SelectCoverObjectSetSubComponent.Builder
    fun objectUnsplashComponent(): UnsplashSubComponent.Builder

    fun objectRelationListComponent(): ObjectRelationListComponent.Builder
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
    fun provideObjectSetViewModelFactory(
        openObjectSet: OpenObjectSet,
        closeBlock: CloseBlock,
        setObjectDetails: UpdateDetail,
        updateText: UpdateText,
        interceptEvents: InterceptEvents,
        interceptThreadStatus: InterceptThreadStatus,
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
        cancelSearchSubscription: CancelSearchSubscription,
        setQueryToObjectSet: SetQueryToObjectSet,
        database: ObjectSetDatabase,
        paginator: ObjectSetPaginator,
        storeOfRelations: StoreOfRelations,
        objectStateReducer: ObjectStateReducer,
        dataViewSubscription: DataViewSubscription,
        workspaceManager: WorkspaceManager,
        @Named("object-set-store") objectStore: ObjectStore,
        addObjectToCollection: AddObjectToCollection,
        convertObjectToCollection: ConvertObjectToCollection
    ): ObjectSetViewModelFactory = ObjectSetViewModelFactory(
        openObjectSet = openObjectSet,
        closeBlock = closeBlock,
        setObjectDetails = setObjectDetails,
        createDataViewObject = createDataViewObject,
        updateText = updateText,
        interceptEvents = interceptEvents,
        interceptThreadStatus = interceptThreadStatus,
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
        cancelSearchSubscription = cancelSearchSubscription,
        setQueryToObjectSet = setQueryToObjectSet,
        database = database,
        paginator = paginator,
        storeOfRelations = storeOfRelations,
        objectStateReducer = objectStateReducer,
        dataViewSubscription = dataViewSubscription,
        workspaceManager = workspaceManager,
        objectStore = objectStore,
        addObjectToCollection = addObjectToCollection,
        objectToCollection = convertObjectToCollection
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
        getTemplates: GetTemplates,
        getDefaultPageType: GetDefaultPageType,
        dispatchers: AppCoroutineDispatchers
    ): CreateObject = CreateObject(
        repo = repo,
        getTemplates = getTemplates,
        getDefaultPageType = getDefaultPageType,
        dispatchers = dispatchers
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
        workspaceManager: WorkspaceManager,
        dispatchers: AppCoroutineDispatchers
    ): GetDefaultPageType = GetDefaultPageType(
        userSettingsRepository = userSettingsRepository,
        blockRepository = blockRepository,
        workspaceManager = workspaceManager,
        dispatchers = dispatchers
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideOpenObjectSetUseCase(
        repo: BlockRepository,
        auth: AuthRepository
    ): OpenObjectSet = OpenObjectSet(repo = repo, auth = auth)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideUpdateDataViewViewerUseCase(
        repo: BlockRepository
    ): UpdateDataViewViewer = UpdateDataViewViewer(repo = repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideCreateDataViewRecordUseCase(
        repo: BlockRepository,
        storeOfRelations: StoreOfRelations,
        getDefaultPageType: GetDefaultPageType,
        getTemplates: GetTemplates,
        dispatchers: AppCoroutineDispatchers
    ): CreateDataViewObject = CreateDataViewObject(
        repo = repo,
        getDefaultPageType = getDefaultPageType,
        getTemplates = getTemplates,
        storeOfRelations = storeOfRelations,
        dispatchers = dispatchers
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
    fun provideInterceptThreadStatus(
        channel: ThreadStatusChannel
    ): InterceptThreadStatus = InterceptThreadStatus(
        channel = channel
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
    @Provides
    @PerScreen
    fun provideGetTemplates(
        repo: BlockRepository,
        dispatchers: AppCoroutineDispatchers
    ): GetTemplates = GetTemplates(
        repo = repo,
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
    ) : RelationListProvider = RelationListProvider.DataViewRelationListProvider(
        objectStates = objectStateFlow
    )

    @Module
    interface Bindings {

        @PerScreen
        @Binds
        fun bindCoverImageHashProvider(
            defaultProvider: DefaultCoverImageHashProvider
        ): CoverImageHashProvider
    }
}