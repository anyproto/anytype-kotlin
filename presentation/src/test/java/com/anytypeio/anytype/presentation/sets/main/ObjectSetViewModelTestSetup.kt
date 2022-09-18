package com.anytypeio.anytype.presentation.sets.main

import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVSort
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SearchResult
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.core_models.restrictions.DataViewRestrictions
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.domain.dataview.SetDataViewSource
import com.anytypeio.anytype.domain.dataview.interactor.AddNewRelationToDataView
import com.anytypeio.anytype.domain.dataview.interactor.CreateDataViewRecord
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.event.interactor.EventChannel
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.objects.DefaultObjectStore
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.page.CloseBlock
import com.anytypeio.anytype.domain.sets.OpenObjectSet
import com.anytypeio.anytype.domain.status.InterceptThreadStatus
import com.anytypeio.anytype.domain.templates.GetTemplates
import com.anytypeio.anytype.domain.unsplash.DownloadUnsplashImage
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.domain.page.CreateNewObject
import com.anytypeio.anytype.domain.search.CancelSearchSubscription
import com.anytypeio.anytype.domain.search.DataViewSubscriptionContainer
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.presentation.sets.ObjectSetDatabase
import com.anytypeio.anytype.presentation.sets.ObjectSetPaginator
import com.anytypeio.anytype.presentation.sets.ObjectSetRecordCache
import com.anytypeio.anytype.presentation.sets.ObjectSetReducer
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestDispatcher
import org.junit.Rule
import org.mockito.Mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub

open class ObjectSetViewModelTestSetup {

    val root: Id = MockDataFactory.randomString()

    @Mock
    lateinit var openObjectSet: OpenObjectSet

    @Mock
    lateinit var closeBlock: CloseBlock

    @Mock
    lateinit var addDataViewRelation: AddNewRelationToDataView

    @Mock
    lateinit var updateDataViewViewer: UpdateDataViewViewer

    @Mock
    lateinit var updateText: UpdateText

    @Mock
    lateinit var createDataViewRecord: CreateDataViewRecord

    @Mock
    lateinit var interceptEvents: InterceptEvents

    @Mock
    lateinit var interceptThreadStatus: InterceptThreadStatus

    @Mock
    lateinit var gateway: Gateway

    @Mock
    lateinit var analytics: Analytics

    @Mock
    lateinit var downloadUnsplashImage: DownloadUnsplashImage

    @Mock
    lateinit var setDocCoverImage: SetDocCoverImage

    @Mock
    lateinit var getTemplates: GetTemplates

    @Mock
    lateinit var createNewObject: CreateNewObject

    @Mock
    lateinit var setDataViewSource: SetDataViewSource

    @Mock
    lateinit var setObjectDetails: UpdateDetail

    @Mock
    lateinit var cancelSearchSubscription: CancelSearchSubscription

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var subscriptionEventChannel: SubscriptionEventChannel

    val dispatcher = Dispatcher.Default<Payload>()
    private val delegator = Delegator.Default<Action>()
    private val reducer = ObjectSetReducer()
    private val cache = ObjectSetRecordCache()
    val session = ObjectSetSession()
    private val paginator = ObjectSetPaginator()

    private val store: ObjectStore = DefaultObjectStore()
    private val database = ObjectSetDatabase(store)

    private lateinit var container: DataViewSubscriptionContainer

    val urlBuilder: UrlBuilder get() = UrlBuilder(gateway)

    fun initDataViewSubscriptionContainer() {
        container = DataViewSubscriptionContainer(
            repo = repo,
            store = store,
            channel = subscriptionEventChannel,
            dispatchers = AppCoroutineDispatchers(
                io = StandardTestDispatcher(),
                computation = StandardTestDispatcher(),
                main = StandardTestDispatcher()
            )
        )
    }

    fun givenViewModel(): ObjectSetViewModel = ObjectSetViewModel(
        openObjectSet = openObjectSet,
        closeBlock = closeBlock,
        addDataViewRelation = addDataViewRelation,
        updateDataViewViewer = updateDataViewViewer,
        updateText = updateText,
        interceptEvents = interceptEvents,
        interceptThreadStatus = interceptThreadStatus,
        createDataViewRecord = createDataViewRecord,
        dispatcher = dispatcher,
        delegator = delegator,
        reducer = reducer,
        objectSetRecordCache = cache,
        urlBuilder = urlBuilder,
        session = session,
        analytics = analytics,
        downloadUnsplashImage = downloadUnsplashImage,
        setDocCoverImage = setDocCoverImage,
        getTemplates = getTemplates,
        createNewObject = createNewObject,
        setDataViewSource = setDataViewSource,
        setObjectDetails = setObjectDetails,
        paginator = paginator,
        cancelSearchSubscription = cancelSearchSubscription,
        database = database,
        dataViewSubscriptionContainer = container
    )

    fun stubInterceptEvents(
        params: InterceptEvents.Params = InterceptEvents.Params(context = root),
        flow: Flow<List<Event>> = flowOf()
    ) {
        interceptEvents.stub {
            onBlocking { build(params) } doReturn flow
        }
    }

    fun stubInterceptThreadStatus(
        params: InterceptThreadStatus.Params = InterceptThreadStatus.Params(ctx = root)
    ) {
        interceptThreadStatus.stub {
            onBlocking { build(params) } doReturn emptyFlow()
        }
    }

    fun stubOpenObjectSet(
        doc: List<Block> = emptyList(),
        details: Block.Details = Block.Details(),
        objectTypes: List<ObjectType> = emptyList(),
        relations: List<Relation> = emptyList(),
        additionalEvents: List<Event> = emptyList(),
        dataViewRestrictions: List<DataViewRestrictions> = emptyList()
    ) {
        openObjectSet.stub {
            onBlocking {
                invoke(any())
            } doReturn Either.Right(
                Result.Success(
                    Payload(
                        context = root,
                        events = listOf(
                            Event.Command.ShowObject(
                                context = root,
                                root = root,
                                blocks = doc,
                                details = details,
                                relations = relations,
                                objectTypes = objectTypes,
                                dataViewRestrictions = dataViewRestrictions,
                                type = SmartBlockType.SET
                            )
                        ) + additionalEvents
                    )
                )
            )
        }
    }

    fun stubUpdateDataViewViewer(
        events: List<Event> = emptyList()
    ) {
        updateDataViewViewer.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(
                Payload(
                    context = root,
                    events = events
                )
            )
        }
    }

    fun stubCloseBlock() {
        closeBlock.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(Unit)
        }
    }

    fun stubCreateDataViewRecord(
        record: Map<String, Any?> = emptyMap()
    ) {
        createDataViewRecord.stub {
            onBlocking {
                invoke(any())
            } doReturn Either.Right(record)
        }
    }

    fun stubGetTemplates(
        type: String,
        templates : List<Id> = emptyList()
    ) {
        getTemplates.stub {
            onBlocking {
                run(
                    GetTemplates.Params(type)
                )
            } doReturn templates.map {
                ObjectWrapper.Basic(
                    map = mapOf(
                        Relations.ID to it
                    )
                )
            }
        }
    }

    fun stubSearchWithSubscription(
        subscription: Id,
        filters: List<DVFilter>,
        sorts: List<DVSort>,
        afterId: Id? = null,
        beforeId: Id? = null,
        sources: List<Id> = emptyList(),
        keys: List<Key>,
        offset: Long,
        limit: Int,
        result: SearchResult
    ) {
        repo.stub {
            onBlocking {
                searchObjectsWithSubscription(
                    subscription = subscription,
                    filters = filters,
                    sorts = sorts,
                    afterId = afterId,
                    beforeId = beforeId,
                    source = sources,
                    keys = keys,
                    limit = limit,
                    offset = offset
                )
            } doReturn result
        }
    }

    fun stubSearchWithSubscription() {
        repo.stub {
            onBlocking {
                searchObjectsWithSubscription(
                    subscription = any(),
                    filters = any(),
                    sorts = any(),
                    afterId = any(),
                    beforeId = any(),
                    source = any(),
                    keys = any(),
                    limit = any(),
                    offset = any()
                )
            } doReturn SearchResult(
                results = emptyList(),
                dependencies = emptyList(),
                counter = null
            )
        }
    }

    fun stubSubscriptionEventChannel(
        flow: Flow<List<SubscriptionEvent>> = emptyFlow()
    ) {
        subscriptionEventChannel.stub {
            onBlocking {
                subscribe(listOf(root))
            } doReturn flow
        }
    }
}