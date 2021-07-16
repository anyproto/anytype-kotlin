package com.anytypeio.anytype.di.feature

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_utils.di.scope.PerScreen
import com.anytypeio.anytype.di.feature.relations.RelationAddToDataViewSubComponent
import com.anytypeio.anytype.di.feature.relations.RelationCreateFromScratchForDataViewSubComponent
import com.anytypeio.anytype.di.feature.sets.CreateFilterSubComponent
import com.anytypeio.anytype.di.feature.sets.ModifyFilterSubComponent
import com.anytypeio.anytype.di.feature.sets.SelectFilterRelationSubComponent
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.dataview.interactor.*
import com.anytypeio.anytype.domain.event.interactor.EventChannel
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.page.ArchiveDocument
import com.anytypeio.anytype.domain.page.CloseBlock
import com.anytypeio.anytype.domain.relations.AddFileToRecord
import com.anytypeio.anytype.domain.sets.OpenObjectSet
import com.anytypeio.anytype.domain.status.InterceptThreadStatus
import com.anytypeio.anytype.domain.status.ThreadStatusChannel
import com.anytypeio.anytype.presentation.relations.providers.*
import com.anytypeio.anytype.presentation.sets.*
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.ui.sets.ObjectSetFragment
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow

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
    fun viewerCustomizeSubComponent(): ViewerCustomizeSubComponent.Builder
    fun viewerSortBySubComponent(): ViewerSortBySubComponent.Builder
    fun viewerFilterBySubComponent(): ViewerFilterSubComponent.Builder
    fun createDataViewViewerSubComponent(): CreateDataViewViewerSubComponent.Builder
    fun editDataViewViewerComponent(): EditDataViewViewerSubComponent.Builder
    fun objectRelationValueComponent(): ObjectSetObjectRelationValueSubComponent.Builder
    fun manageViewerComponent(): ManageViewerSubComponent.Builder
    fun viewerRelationsComponent(): ViewerRelationsSubComponent.Builder
    fun relationAddToDataViewComponent() : RelationAddToDataViewSubComponent.Builder
    fun relationCreateFromScratchForDataViewComponent() : RelationCreateFromScratchForDataViewSubComponent.Builder
    fun dataviewViewerActionComponent(): DataViewViewerActionSubComponent.Builder
    fun selectSortRelationComponent(): SelectSortRelationSubComponent.Builder
    fun selectFilterRelationComponent(): SelectFilterRelationSubComponent.Builder
    fun createFilterComponent(): CreateFilterSubComponent.Builder
    fun modifyFilterComponent(): ModifyFilterSubComponent.Builder
    fun viewerSortComponent(): ViewerSortSubComponent.Builder
    fun modifyViewerSortComponent(): ModifyViewerSortSubComponent.Builder
    fun relationTextValueComponent(): RelationTextValueSubComponent.Builder
    fun relationDateValueComponent(): RelationDataValueSubComponent.Builder
    fun objectSetMenuComponent() : ObjectSetMenuComponent.Builder
}

@Module
object ObjectSetModule {

    @JvmStatic
    @Provides
    @PerScreen
    fun provideObjectSetViewModelFactory(
        openObjectSet: OpenObjectSet,
        closeBlock: CloseBlock,
        setActiveViewer: SetActiveViewer,
        addDataViewRelation: AddNewRelationToDataView,
        updateDataViewViewer: UpdateDataViewViewer,
        updateDataViewRecord: UpdateDataViewRecord,
        updateText: UpdateText,
        interceptEvents: InterceptEvents,
        interceptThreadStatus: InterceptThreadStatus,
        createDataViewRecord: CreateDataViewRecord,
        reducer: ObjectSetReducer,
        dispatcher: Dispatcher<Payload>,
        objectSetRecordCache: ObjectSetRecordCache,
        urlBuilder: UrlBuilder,
        session: ObjectSetSession
    ): ObjectSetViewModelFactory = ObjectSetViewModelFactory(
        openObjectSet = openObjectSet,
        closeBlock = closeBlock,
        setActiveViewer = setActiveViewer,
        addDataViewRelation = addDataViewRelation,
        updateDataViewViewer = updateDataViewViewer,
        updateDataViewRecord = updateDataViewRecord,
        createDataViewRecord = createDataViewRecord,
        updateText = updateText,
        interceptEvents = interceptEvents,
        interceptThreadStatus = interceptThreadStatus,
        reducer = reducer,
        dispatcher = dispatcher,
        objectSetRecordCache = objectSetRecordCache,
        urlBuilder = urlBuilder,
        session = session
    )

    @JvmStatic
    @Provides
    @PerScreen
    fun provideOpenObjectSetUseCase(repo: BlockRepository): OpenObjectSet = OpenObjectSet(repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideSetActiveViewerUseCase(
        repo: BlockRepository
    ): SetActiveViewer = SetActiveViewer(repo)

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
    fun provideUpdateDataViewRecordUseCase(
        repo: BlockRepository
    ): UpdateDataViewRecord = UpdateDataViewRecord(repo = repo)

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
    ) : InterceptThreadStatus = InterceptThreadStatus(
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
    fun provideObjectSetRecordCache(): ObjectSetRecordCache = ObjectSetRecordCache()

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDataViewObjectRelationProvider(
        state: StateFlow<ObjectSet>
    ) : ObjectRelationProvider = DataViewObjectRelationProvider(state)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideDataViewObjectValueProvider(
        state: StateFlow<ObjectSet>,
        session: ObjectSetSession
    ) : ObjectValueProvider = DataViewObjectValueProvider(state, session)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideObjectTypeProvider(
        state: StateFlow<ObjectSet>,
    ) : ObjectTypeProvider = object : ObjectTypeProvider {
        override fun provide(): List<ObjectType> = state.value.objectTypes
    }

    @JvmStatic
    @Provides
    @PerScreen
    fun provideObjectDetailProvider(
        state: StateFlow<ObjectSet>,
    ) : ObjectDetailProvider = object : ObjectDetailProvider {
        override fun provide(): Map<Id, Block.Fields> = state.value.details
    }

    @JvmStatic
    @Provides
    @PerScreen
    fun provideUpdateDetailUseCase(
        repository: BlockRepository
    ) : UpdateDetail = UpdateDetail(repository)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideAddFileToRecordUseCase(
        repo: BlockRepository
    ): AddFileToRecord = AddFileToRecord(repo = repo)

    @JvmStatic
    @Provides
    @PerScreen
    fun provideArchiveDocumentUseCase(
        repo: BlockRepository
    ): ArchiveDocument = ArchiveDocument(
        repo = repo
    )
}