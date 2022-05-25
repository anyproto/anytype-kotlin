package com.anytypeio.anytype.features.sets.dv

import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import com.anytypeio.anytype.R
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVRecord
import com.anytypeio.anytype.core_models.Event
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Payload
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.domain.auth.repo.AuthRepository
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.Result
import com.anytypeio.anytype.domain.block.interactor.UpdateText
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.config.Gateway
import com.anytypeio.anytype.domain.cover.SetDocCoverImage
import com.anytypeio.anytype.domain.dataview.interactor.AddNewRelationToDataView
import com.anytypeio.anytype.domain.dataview.interactor.CreateDataViewRecord
import com.anytypeio.anytype.domain.dataview.interactor.SetActiveViewer
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewRecord
import com.anytypeio.anytype.domain.dataview.interactor.UpdateDataViewViewer
import com.anytypeio.anytype.domain.event.interactor.InterceptEvents
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.page.CloseBlock
import com.anytypeio.anytype.domain.sets.OpenObjectSet
import com.anytypeio.anytype.domain.status.InterceptThreadStatus
import com.anytypeio.anytype.domain.status.ThreadStatusChannel
import com.anytypeio.anytype.domain.templates.GetTemplates
import com.anytypeio.anytype.domain.unsplash.DownloadUnsplashImage
import com.anytypeio.anytype.domain.unsplash.UnsplashRepository
import com.anytypeio.anytype.emojifier.data.DefaultDocumentEmojiIconProvider
import com.anytypeio.anytype.presentation.common.Action
import com.anytypeio.anytype.presentation.common.Delegator
import com.anytypeio.anytype.presentation.sets.ObjectSetRecordCache
import com.anytypeio.anytype.presentation.sets.ObjectSetReducer
import com.anytypeio.anytype.presentation.sets.ObjectSetSession
import com.anytypeio.anytype.presentation.sets.ObjectSetViewModelFactory
import com.anytypeio.anytype.presentation.util.Dispatcher
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub

abstract class TestObjectSetSetup {

    private lateinit var openObjectSet: OpenObjectSet
    private lateinit var addDataViewRelation: AddNewRelationToDataView
    private lateinit var updateDataViewViewer: UpdateDataViewViewer
    private lateinit var updateDataViewRecord: UpdateDataViewRecord
    private lateinit var updateText: UpdateText
    private lateinit var createDataViewRecord: CreateDataViewRecord
    private lateinit var closeBlock: CloseBlock
    private lateinit var setActiveViewer: SetActiveViewer
    private lateinit var interceptThreadStatus: InterceptThreadStatus
    private lateinit var setDocCoverImage: SetDocCoverImage
    private lateinit var downloadUnsplashImage: DownloadUnsplashImage

    lateinit var urlBuilder: UrlBuilder

    @Mock
    lateinit var repo: BlockRepository

    @Mock
    lateinit var unsplashRepo: UnsplashRepository

    @Mock
    lateinit var auth: AuthRepository

    @Mock
    lateinit var gateway: Gateway
    @Mock
    lateinit var interceptEvents: InterceptEvents
    @Mock
    lateinit var threadStatusChannel: ThreadStatusChannel
    @Mock
    lateinit var analytics: Analytics

    lateinit var getTemplates: GetTemplates

    private val session = ObjectSetSession()
    private val reducer = ObjectSetReducer()
    private val dispatcher: Dispatcher<Payload> = Dispatcher.Default()
    private val objectSetRecordCache = ObjectSetRecordCache()

    val ctx : Id = MockDataFactory.randomUuid()

    abstract val title : Block

    val header get() = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Layout(
            type = Block.Content.Layout.Type.HEADER
        ),
        fields = Block.Fields.empty(),
        children = listOf(title.id)
    )

    val defaultDetails = Block.Details(
        mapOf(
            ctx to Block.Fields(
                mapOf(
                    "iconEmoji" to DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random()
                )
            )
        )
    )

    val delegator = Delegator.Default<Action>()

    open fun setup() {
        MockitoAnnotations.openMocks(this)

        addDataViewRelation = AddNewRelationToDataView(repo)
        updateText = UpdateText(repo)
        openObjectSet = OpenObjectSet(repo, auth)
        createDataViewRecord = CreateDataViewRecord(repo)
        updateDataViewRecord = UpdateDataViewRecord(repo)
        updateDataViewViewer = UpdateDataViewViewer(repo)
        setActiveViewer = SetActiveViewer(repo)
        interceptThreadStatus = InterceptThreadStatus(channel = threadStatusChannel)
        closeBlock = CloseBlock(repo)
        urlBuilder = UrlBuilder(gateway)
        downloadUnsplashImage = DownloadUnsplashImage(unsplashRepo)
        setDocCoverImage = SetDocCoverImage(repo)
        getTemplates = GetTemplates(
            repo = repo,
            dispatchers = AppCoroutineDispatchers(
                io = StandardTestDispatcher(),
                main = StandardTestDispatcher(),
                computation = StandardTestDispatcher()
            )
        )

        TestObjectSetFragment.testVmFactory = ObjectSetViewModelFactory(
            openObjectSet = openObjectSet,
            closeBlock = closeBlock,
            addDataViewRelation = addDataViewRelation,
            interceptEvents = interceptEvents,
            interceptThreadStatus = interceptThreadStatus,
            updateDataViewViewer = updateDataViewViewer,
            setActiveViewer = setActiveViewer,
            createDataViewRecord = createDataViewRecord,
            updateDataViewRecord = updateDataViewRecord,
            updateText = updateText,
            urlBuilder = urlBuilder,
            session = session,
            dispatcher = dispatcher,
            reducer = reducer,
            objectSetRecordCache = objectSetRecordCache,
            analytics = analytics,
            downloadUnsplashImage = downloadUnsplashImage,
            setDocCoverImage = setDocCoverImage,
            delegator = delegator,
            getTemplates = getTemplates
        )
    }

    fun stubInterceptEvents() {
        interceptEvents.stub {
            onBlocking { build(any()) } doReturn emptyFlow()
        }
    }

    fun stubSetActiveViewer() {
        repo.stub {
            onBlocking {
                setActiveDataViewViewer(
                    context = any(),
                    block = any(),
                    view = any(),
                    offset = any(),
                    limit = any()
                )
            } doReturn Payload(
                context = ctx,
                events = emptyList()
            )
        }
    }

    fun stubInterceptThreadStatus() {
        threadStatusChannel.stub {
            onBlocking { observe(any()) } doReturn emptyFlow()
        }
    }

    fun stubOpenObjectSet(
        set: List<Block>,
        details: Block.Details = Block.Details(),
        relations: List<Relation> = emptyList()
    ) {
        repo.stub {
            onBlocking { openObjectSet(ctx) } doReturn Result.Success(
                Payload(
                    context = ctx,
                    events = listOf(
                        Event.Command.ShowObject(
                            context = ctx,
                            root = ctx,
                            details = details,
                            blocks = set,
                            relations = relations
                        )
                    )
                )
            )
        }
    }

    fun stubOpenObjectSetWithRecord(
        set: List<Block>,
        details: Block.Details = Block.Details(),
        relations: List<Relation> = emptyList(),
        dataview: Id,
        viewer: Id,
        total: Int,
        records: List<DVRecord>,
        objectTypes: List<ObjectType>
    ) {
        repo.stub {
            onBlocking { openObjectSet(ctx) } doReturn Result.Success(
                Payload(
                    context = ctx,
                    events = listOf(
                        Event.Command.ShowObject(
                            context = ctx,
                            root = ctx,
                            details = details,
                            blocks = set,
                            relations = relations,
                            objectTypes = objectTypes
                        ),
                        Event.Command.DataView.SetRecords(
                            context = ctx,
                            id = dataview,
                            view = viewer,
                            total = total,
                            records = records
                        )
                    )
                )
            )
        }
    }

    fun launchFragment(args: Bundle): FragmentScenario<TestObjectSetFragment> {
        return launchFragmentInContainer(
            fragmentArgs = args,
            themeResId = R.style.AppTheme
        )
    }
}