package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
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
import com.anytypeio.anytype.domain.`object`.DuplicateObject
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.domain.dataview.SetDataViewSource
import com.anytypeio.anytype.domain.dataview.interactor.AddNewRelationToDataView
import com.anytypeio.anytype.domain.dataview.interactor.CreateDataViewRecord
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.event.interactor.EventChannel
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.icon.SetDocumentImageIcon
import com.anytypeio.anytype.domain.launch.GetDefaultEditorType
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.DefaultObjectStore
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.SetObjectIsArchived
import com.anytypeio.anytype.domain.page.CloseBlock
import com.anytypeio.anytype.domain.page.CreateNewObject
import com.anytypeio.anytype.domain.page.CreatePage
import com.anytypeio.anytype.domain.relations.AddFileToObject
import com.anytypeio.anytype.domain.relations.DeleteRelationFromDataView
import com.anytypeio.anytype.domain.search.CancelSearchSubscription
import com.anytypeio.anytype.domain.search.DataViewSubscriptionContainer
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.domain.sets.OpenObjectSet
import com.anytypeio.anytype.domain.status.InterceptThreadStatus
import com.anytypeio.anytype.domain.status.ThreadStatusChannel
import com.anytypeio.anytype.domain.templates.GetTemplates
import com.anytypeio.anytype.domain.unsplash.DownloadUnsplashImage
import com.anytypeio.anytype.domain.unsplash.UnsplashRepository
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.presentation.relations.providers.DataViewObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.DataViewObjectValueProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectDetailProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectRelationProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectTypeProvider
import com.anytypeio.anytype.presentation.relations.providers.ObjectValueProvider
import com.anytypeio.anytype.presentation.sets.ObjectSet
import com.anytypeio.anytype.presentation.sets.ObjectSetDatabase
import com.anytypeio.anytype.presentation.sets.ObjectSetPaginator
import com.anytypeio.anytype.presentation.sets.ObjectSetRecordCache
import com.anytypeio.anytype.presentation.sets.ObjectSetReducer
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModelFactory
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.sets.ObjectSetFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Named

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
    fun viewerCustomizeSubComponent(): ViewerCustomizeSubComponent.Builder
    fun viewerSortBySubComponent(): ViewerSortBySubComponent.Builder
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
    fun dataviewViewerActionComponent(): DataViewViewerActionSubComponent.Builder
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
}

@Module
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
    fun provideObjectSetViewModelFactory(
        openObjectSet: OpenObjectSet,
        closeBlock: CloseBlock,
        addDataViewRelation: AddNewRelationToDataView,
        updateDataViewViewer: UpdateDataViewViewer,
        setObjectDetails: UpdateDetail,
        updateText: UpdateText,
        interceptEvents: InterceptEvents,
        interceptThreadStatus: InterceptThreadStatus,
        createDataViewRecord: CreateDataViewRecord,
        reducer: ObjectSetReducer,
        dispatcher: Dispatcher<Payload>,
        delegator: Delegator<Action>,
        objectSetRecordCache: ObjectSetRecordCache,
        urlBuilder: UrlBuilder,
        session: ObjectSetSession,
        analytics: Analytics,
        downloadUnsplashImage: DownloadUnsplashImage,
        setDocCoverImage: SetDocCoverImage,
        getTemplates: GetTemplates,
        dataViewSubscriptionContainer: DataViewSubscriptionContainer,
        cancelSearchSubscription: CancelSearchSubscription,
        createNewObject: CreateNewObject,
        setDataViewSource: SetDataViewSource,
        database: ObjectSetDatabase,
        paginator: ObjectSetPaginator
    ): ObjectSetViewModelFactory = ObjectSetViewModelFactory(
        openObjectSet = openObjectSet,
        closeBlock = closeBlock,
        addDataViewRelation = addDataViewRelation,
        updateDataViewViewer = updateDataViewViewer,
        setObjectDetails = setObjectDetails,
        createDataViewRecord = createDataViewRecord,
        updateText = updateText,
        interceptEvents = interceptEvents,
        interceptThreadStatus = interceptThreadStatus,
        reducer = reducer,
        dispatcher = dispatcher,
        delegator = delegator,
        objectSetRecordCache = objectSetRecordCache,
        urlBuilder = urlBuilder,
        session = session,
        analytics = analytics,
        downloadUnsplashImage = downloadUnsplashImage,
        setDocCoverImage = setDocCoverImage,
        getTemplates = getTemplates,
        createNewObject = createNewObject,
        dataViewSubscriptionContainer = dataViewSubscriptionContainer,
        cancelSearchSubscription = cancelSearchSubscription,
        setDataViewSource = setDataViewSource,
        database = database,
        paginator = paginator
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideCreateNewObject(
        getDefaultEditorType: GetDefaultEditorType,
        getTemplates: GetTemplates,
        createPage: CreatePage,
    ): CreateNewObject = CreateNewObject(
        getDefaultEditorType,
        getTemplates,
        createPage
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSetDataViewSource(
        repo: BlockRepository
    ): SetDataViewSource = SetDataViewSource(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideCreatePageUseCase(
        repo: BlockRepository
    ): CreatePage = CreatePage(
        repo = repo
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideGetDefaultPageType(repo: UserSettingsRepository): GetDefaultEditorType =
        GetDefaultEditorType(repo)

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
    fun provideAddDataViewRelationUseCase(
        repo: BlockRepository
    ): AddNewRelationToDataView = AddNewRelationToDataView(repo = repo)

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
        repo: BlockRepository
    ): CreateDataViewRecord = CreateDataViewRecord(repo = repo)

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
    fun provideCloseBlockUseCase(
        repo: BlockRepository
    ): CloseBlock = CloseBlock(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideObjectSetReducer(): ObjectSetReducer = ObjectSetReducer()

    @JvmStatic
    @Provides
    @PerScreen
    fun provideState(
        reducer: ObjectSetReducer
    ): StateFlow<ObjectSet> = reducer.state

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
    fun provideObjectSetRecordCache(): ObjectSetRecordCache = ObjectSetRecordCache()

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDataViewObjectRelationProvider(
        state: StateFlow<ObjectSet>
    ): ObjectRelationProvider = DataViewObjectRelationProvider(state)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDataViewObjectValueProvider(
        db: ObjectSetDatabase
    ) : ObjectValueProvider = DataViewObjectValueProvider(
        db = db
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideObjectTypeProvider(
        state: StateFlow<ObjectSet>,
    ): ObjectTypeProvider = object : ObjectTypeProvider {
        override fun provide(): List<ObjectType> = state.value.objectTypes
    }

    @JvmStatic
    @Provides
    @PerScreen
    fun provideObjectDetailProvider(
        state: StateFlow<ObjectSet>,
    ): ObjectDetailProvider = object : ObjectDetailProvider {
        override fun provide(): Map<Id, Block.Fields> = state.value.details
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
    fun provideGetTemplates(repo: BlockRepository): GetTemplates = GetTemplates(
        repo = repo,
        dispatchers = AppCoroutineDispatchers(
            io = Dispatchers.IO,
            computation = Dispatchers.Default,
            main = Dispatchers.Main
        )
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun objectSearchSubscriptionContainer(
        repo: BlockRepository,
        channel: SubscriptionEventChannel,
        @Named("object-set-store") store: ObjectStore
    ): DataViewSubscriptionContainer = DataViewSubscriptionContainer(
        repo = repo,
        channel = channel,
        store = store,
        dispatchers = AppCoroutineDispatchers(
            io = Dispatchers.IO,
            computation = Dispatchers.Default,
            main = Dispatchers.Main
        )
    )

    @JvmStatic
    @Provides
    @PerScreen
    @Named("object-set-store")
    fun provideObjectStore() : ObjectStore = DefaultObjectStore()

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
    fun providePaginator() : ObjectSetPaginator = ObjectSetPaginator()

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
}