package com.anytypeio.anytype.presentation.sets.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.Key
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.SearchResult
import com.anytypeio.anytype.core_models.restrictions.DataViewRestrictions
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.collections.AddObjectToCollection
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.domain.dataview.interactor.CreateDataViewObject
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.launch.GetDefaultPageType
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.`object`.ConvertObjectToCollection
import com.anytypeio.anytype.domain.`object`.DuplicateObjects
import com.anytypeio.anytype.domain.`object`.UpdateDetail
import com.anytypeio.anytype.domain.objects.DefaultObjectStore
import com.anytypeio.anytype.domain.objects.DefaultStoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.DefaultStoreOfRelations
import com.anytypeio.anytype.domain.objects.ObjectStore
import com.anytypeio.anytype.domain.objects.SetObjectListIsArchived
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.page.CloseBlock
import com.anytypeio.anytype.domain.page.CreateObject
import com.anytypeio.anytype.domain.search.CancelSearchSubscription
import com.anytypeio.anytype.domain.search.DataViewSubscriptionContainer
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.domain.sets.OpenObjectSet
import com.anytypeio.anytype.domain.sets.SetQueryToObjectSet
import com.anytypeio.anytype.domain.status.InterceptThreadStatus
import com.anytypeio.anytype.domain.templates.CreateTemplate
import com.anytypeio.anytype.domain.templates.GetTemplates
import com.anytypeio.anytype.domain.unsplash.DownloadUnsplashImage
import com.anytypeio.anytype.domain.workspace.WorkspaceManager
import com.anytypeio.anytype.presentation.collections.MockCollection
import com.anytypeio.anytype.presentation.collections.MockSet
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.sets.ObjectSetDatabase
import com.anytypeio.anytype.presentation.sets.ObjectSetPaginator
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModel
import com.anytypeio.anytype.presentation.sets.state.DefaultObjectStateReducer
import com.anytypeio.anytype.presentation.sets.subscription.DataViewSubscription
import com.anytypeio.anytype.presentation.sets.subscription.DefaultDataViewSubscription
import com.anytypeio.anytype.presentation.sets.updateFormatForSubscription
import com.anytypeio.anytype.presentation.sets.viewer.ViewerDelegate
import com.anytypeio.anytype.presentation.templates.ObjectTypeTemplatesContainer
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import net.bytebuddy.utility.RandomString
import org.junit.Rule
import org.mockito.Mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

open class ObjectSetViewModelTestSetup {

    val root: Id = "context-${RandomString.make()}"

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val rule = DefaultCoroutineTestRule()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Mock
    lateinit var openObjectSet: OpenObjectSet

    @Mock
    lateinit var closeBlock: CloseBlock

    @Mock
    lateinit var updateText: UpdateText

    @Mock
    lateinit var createDataViewObject: CreateDataViewObject

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
    lateinit var createObject: CreateObject

    @Mock
    lateinit var objectToCollection: ConvertObjectToCollection

    @Mock
    lateinit var setQueryToObjectSet: SetQueryToObjectSet

    @Mock
    lateinit var setObjectDetails: UpdateDetail

    @Mock
    lateinit var cancelSearchSubscription: CancelSearchSubscription

    //@Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var coverImageHashProvider: CoverImageHashProvider

    @Mock
    lateinit var subscriptionEventChannel: SubscriptionEventChannel

    @Mock
    lateinit var addObjectToCollection: AddObjectToCollection

    lateinit var storeOfObjectTypes: StoreOfObjectTypes

    @Mock
    lateinit var getDefaultPageType: GetDefaultPageType

    @Mock
    lateinit var duplicateObjects: DuplicateObjects

    @Mock
    lateinit var setObjectListIsArchived: SetObjectListIsArchived

    @Mock
    lateinit var templatesContainer: ObjectTypeTemplatesContainer

    @Mock
    lateinit var updateDataViewViewer: UpdateDataViewViewer

    @Mock
    lateinit var viewerDelegate: ViewerDelegate

    @Mock
    lateinit var createTemplate: CreateTemplate

    var stateReducer = DefaultObjectStateReducer()

    lateinit var dataViewSubscriptionContainer: DataViewSubscriptionContainer
    lateinit var dataViewSubscription: DataViewSubscription

    var workspaceManager: WorkspaceManager = WorkspaceManager.DefaultWorkspaceManager()

    val dispatcher = Dispatcher.Default<Payload>()
    private val delegator = Delegator.Default<Action>()
    val session = ObjectSetSession()
    private val paginator = ObjectSetPaginator()

    val objectStore: ObjectStore = DefaultObjectStore()
    protected val storeOfRelations: StoreOfRelations = DefaultStoreOfRelations()
    private val database = ObjectSetDatabase(objectStore)

    val urlBuilder: UrlBuilder get() = UrlBuilder(gateway)

    val defaultObjectPageType = MockDataFactory.randomString()
    val defaultObjectPageTypeName = MockDataFactory.randomString()

    lateinit var dispatchers: AppCoroutineDispatchers

    fun givenViewModel(): ObjectSetViewModel {
        repo = mock(verboseLogging = true)
        dispatchers = AppCoroutineDispatchers(
            io = rule.dispatcher,
            computation = rule.dispatcher,
            main = rule.dispatcher
        )
        dataViewSubscriptionContainer = DataViewSubscriptionContainer(
            repo = repo,
            channel = subscriptionEventChannel,
            store = objectStore,
            dispatchers = dispatchers
        )
        dataViewSubscription = DefaultDataViewSubscription(dataViewSubscriptionContainer)
        storeOfObjectTypes = DefaultStoreOfObjectTypes()
        return ObjectSetViewModel(
            openObjectSet = openObjectSet,
            closeBlock = closeBlock,
            updateText = updateText,
            interceptEvents = interceptEvents,
            interceptThreadStatus = interceptThreadStatus,
            createDataViewObject = createDataViewObject,
            dispatcher = dispatcher,
            delegator = delegator,
            coverImageHashProvider = coverImageHashProvider,
            urlBuilder = urlBuilder,
            session = session,
            analytics = analytics,
            downloadUnsplashImage = downloadUnsplashImage,
            setDocCoverImage = setDocCoverImage,
            createObject = createObject,
            setObjectDetails = setObjectDetails,
            paginator = paginator,
            cancelSearchSubscription = cancelSearchSubscription,
            database = database,
            dataViewSubscriptionContainer = dataViewSubscriptionContainer,
            storeOfRelations = storeOfRelations,
            stateReducer = stateReducer,
            dataViewSubscription = dataViewSubscription,
            workspaceManager = workspaceManager,
            objectStore = objectStore,
            addObjectToCollection = addObjectToCollection,
            objectToCollection = objectToCollection,
            setQueryToObjectSet = setQueryToObjectSet,
            storeOfObjectTypes = storeOfObjectTypes,
            getDefaultPageType = getDefaultPageType,
            updateDataViewViewer = updateDataViewViewer,
            templatesContainer = templatesContainer,
            setObjectListIsArchived = setObjectListIsArchived,
            duplicateObjects = duplicateObjects,
            viewerDelegate = viewerDelegate,
            createTemplate = createTemplate
        )
    }

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

    fun stubOpenObject(
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
                                dataViewRestrictions = dataViewRestrictions
                            )
                        ) + additionalEvents
                    )
                )
            )
        }
    }

    suspend fun stubWorkspaceManager(workspace: Id) {
        workspaceManager.setCurrentWorkspace(workspace)
    }

    suspend fun stubSubscriptionResults(
        subscription: Id = MockDataFactory.randomString(),
        workspace: Id,
        collection: Id? = null,
        objects: List<ObjectWrapper.Basic> = emptyList(),
        dependencies: List<ObjectWrapper.Basic>  = listOf(),
        dvFilters: List<Block.Content.DataView.Filter> = emptyList(),
        dvSorts: List<Block.Content.DataView.Sort> = emptyList(),
        storeOfRelations: StoreOfRelations,
        keys: List<Key> = emptyList(),
        sources: List<Id> = emptyList()
    ) {
        val dvKeys = ObjectSearchConstants.defaultDataViewKeys + keys
        doReturn(
            SearchResult(
                results = objects,
                dependencies = dependencies
            )
        ).`when`(repo).searchObjectsWithSubscription(
            subscription = subscription,
            collection = collection,
            filters = dvFilters.updateFormatForSubscription(storeOfRelations) + ObjectSearchConstants.defaultDataViewFilters(
                workspaceId = workspace
            ),
            sorts = dvSorts,
            keys = dvKeys,
            source = sources,
            limit = ObjectSetConfig.DEFAULT_LIMIT,
            offset = 0L,
            afterId = null,
            beforeId = null,
            noDepSubscription = null,
            ignoreWorkspace = null
        )
        subscriptionEventChannel.stub {
            on {
                subscribe(listOf(subscription))
            } doReturn flow {
                emit(listOf())
            }
        }
    }

    protected suspend fun stubStoreOfRelations(mockObjectCollection: MockCollection) {
        storeOfRelations.merge(
            listOf(
                mockObjectCollection.relationObject1,
                mockObjectCollection.relationObject2,
                mockObjectCollection.relationObject3,
                mockObjectCollection.relationObject4,
                mockObjectCollection.relationObject5,
                mockObjectCollection.relationObject6
            )
        )
    }

    protected suspend fun stubStoreOfRelations(mockObjectSet: MockSet) {
        storeOfRelations.merge(
            listOf(
                mockObjectSet.relationObject1,
                mockObjectSet.relationObject2,
                mockObjectSet.relationObject3,
                mockObjectSet.relationObject4,
                mockObjectSet.relationObject5
            )
        )
    }

    suspend fun stubStoreOfObjectTypes(id: String = "", map: Map<String, Any?> = emptyMap()) {
        storeOfObjectTypes.set(id, map)
    }

    fun stubGetDefaultPageType(type: String = defaultObjectPageType, name: String = defaultObjectPageTypeName) {
        getDefaultPageType.stub {
            onBlocking { run(Unit) } doReturn GetDefaultPageType.Response(type = type, name = name)
        }
    }

    fun stubTemplatesForTemplatesContainer(
        type: String = MockDataFactory.randomString(),
        templates: List<ObjectWrapper.Basic> = emptyList()
    ) {
        templatesContainer.stub {
            onBlocking { subscribeToTemplates(type) }.thenReturn(flowOf(templates))
        }
    }

    fun stubTypesForTemplatesContainer(
        objTypes: List<ObjectWrapper.Basic> = emptyList()
    ) {
        templatesContainer.stub {
            onBlocking { subscribeToTypes() }.thenReturn(flowOf(objTypes))
        }
    }

    fun stubCreateDataViewObject(
        objectId: Id = MockDataFactory.randomString(),
        objectType: Id = MockDataFactory.randomString()
    ) {
        createDataViewObject.stub {
            onBlocking { async(any()) }.thenReturn(
                Resultat.success(
                    CreateDataViewObject.Result(
                        objectId = objectId,
                        objectType = objectType
                    )
                )
            )
        }
    }
}