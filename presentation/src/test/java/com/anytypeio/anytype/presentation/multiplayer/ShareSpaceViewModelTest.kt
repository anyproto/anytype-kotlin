package com.anytypeio.anytype.presentation.multiplayer

import android.util.Log
import app.cash.turbine.test
import com.anytypeio.anytype.core_models.DVFilter
import com.anytypeio.anytype.core_models.DVFilterCondition
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.SearchResult
import com.anytypeio.anytype.core_models.StubObject
import com.anytypeio.anytype.core_models.StubObjectMinim
import com.anytypeio.anytype.core_models.StubSpaceView
import com.anytypeio.anytype.core_models.SubscriptionEvent
import com.anytypeio.anytype.core_models.multiplayer.SpaceAccessType
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.domain.auth.interactor.GetAccount
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.debugging.Logger
import com.anytypeio.anytype.domain.library.StoreSearchParams
import com.anytypeio.anytype.domain.library.StorelessSubscriptionContainer
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.multiplayer.ApproveLeaveSpaceRequest
import com.anytypeio.anytype.domain.multiplayer.ChangeSpaceMemberPermissions
import com.anytypeio.anytype.domain.multiplayer.GenerateSpaceInviteLink
import com.anytypeio.anytype.domain.multiplayer.GetSpaceInviteLink
import com.anytypeio.anytype.domain.multiplayer.RemoveSpaceMembers
import com.anytypeio.anytype.domain.multiplayer.RevokeSpaceInviteLink
import com.anytypeio.anytype.domain.multiplayer.StopSharingSpace
import com.anytypeio.anytype.domain.search.SubscriptionEventChannel
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import net.bytebuddy.utility.RandomString
import net.lachlanmckee.timberjunit.TimberTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub

class ShareSpaceViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    @get:Rule
    val timberTestRule: TimberTestRule = TimberTestRule.builder()
        .minPriority(Log.DEBUG)
        .showThread(false)
        .showTimestamp(false)
        .onlyLogWhenTestFails(false)
        .build()

    val spaceId = "spaceId-${RandomString.make()}"

    @Mock
    lateinit var getSpaceInviteLink: GetSpaceInviteLink
    @Mock
    lateinit var generateSpaceInviteLink: GenerateSpaceInviteLink
    @Mock
    lateinit var revokeSpaceInviteLink: RevokeSpaceInviteLink
    @Mock
    lateinit var removeSpaceMembers: RemoveSpaceMembers
    @Mock
    lateinit var approveLeaveSpaceRequest: ApproveLeaveSpaceRequest
    @Mock
    lateinit var changeSpaceMemberPermissions: ChangeSpaceMemberPermissions
    @Mock
    lateinit var stopSharingSpace: StopSharingSpace
    @Mock
    lateinit var getAccount: GetAccount
    @Mock
    lateinit var urlBuilder: UrlBuilder

    @Mock
    lateinit var repo: BlockRepository
    @Mock
    lateinit var eventChannel: SubscriptionEventChannel

    lateinit var container: StorelessSubscriptionContainer.Impl
    lateinit var dispatchers: AppCoroutineDispatchers

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun before() {
        MockitoAnnotations.openMocks(this)
        dispatchers = AppCoroutineDispatchers(
            io = coroutineTestRule.dispatcher,
            computation = coroutineTestRule.dispatcher,
            main = coroutineTestRule.dispatcher
        )
        container = StorelessSubscriptionContainer.Impl(
            repo = repo,
            channel = eventChannel,
            logger = TestLogger,
            dispatchers = dispatchers
        )
    }

    @Test
    fun `test ShareSpaceViewModel`() = runTest {
        val viewModel = buildViewModel()

        val obj1 = StubObject(
            id = spaceId,
            name = "Space Name",
            iconImage = null,
            iconOption = null,
            targetSpaceId = null,
            spaceAccessType = SpaceAccessType.SHARED.code.toDouble(),
            spaceAccountStatus = null,
            writersLimit = null,
            readersLimit = null
        )
        val obj2 = StubObjectMinim()
        val givenResults = listOf(obj1, obj2)

        stubSearchWithSubscription(
            results = givenResults,
            params = StoreSearchParams(
                filters = buildList {
                    add(
                        DVFilter(
                            relation = Relations.TARGET_SPACE_ID,
                            value = spaceId,
                            condition = DVFilterCondition.EQUAL
                        )
                    )
                },
                sorts = emptyList(),
                subscription = ShareSpaceViewModel.SHARE_SPACE_SPACE_SUBSCRIPTION,
                keys = ObjectSearchConstants.spaceViewKeys,
                limit = 1
            )
        )

        viewModel.shareLinkViewState.test {
            val first = awaitItem()
            assert(first is ShareSpaceViewModel.ShareLinkViewState.Init)
            val second = awaitItem()
            assert(second is ShareSpaceViewModel.ShareLinkViewState.Shared)
        }
    }

    private fun stubSearchWithSubscription(
        results: List<ObjectWrapper.Basic>,
        params: StoreSearchParams
    ) {
        repo.stub {
            onBlocking {
                searchObjectsWithSubscription(
                    subscription = params.subscription,
                    sorts = params.sorts,
                    filters = params.filters,
                    limit = params.limit,
                    offset = params.offset,
                    keys = params.keys,
                    afterId = null,
                    beforeId = null,
                    noDepSubscription = true,
                    ignoreWorkspace = null,
                    collection = null,
                    source = emptyList()
                )
            } doReturn SearchResult(
                results = results,
                dependencies = emptyList()
            )
        }
    }

    private fun stubSubscriptionEventChannel(
        subscription: Id,
        events: Flow<List<SubscriptionEvent>> = emptyFlow()
    ) {
        eventChannel.stub {
            on {
                subscribe(listOf(subscription))
            } doReturn events
        }
    }

    private fun buildViewModel(): ShareSpaceViewModel {
        return ShareSpaceViewModel(
            params = ShareSpaceViewModel.Params(SpaceId(spaceId)),
            getSpaceInviteLink = getSpaceInviteLink,
            generateSpaceInviteLink = generateSpaceInviteLink,
            revokeSpaceInviteLink = revokeSpaceInviteLink,
            removeSpaceMembers = removeSpaceMembers,
            approveLeaveSpaceRequest = approveLeaveSpaceRequest,
            changeSpaceMemberPermissions = changeSpaceMemberPermissions,
            stopSharingSpace = stopSharingSpace,
            container = container,
            getAccount = getAccount,
            urlBuilder = urlBuilder,
        )
    }
}

object TestLogger : Logger {
    override fun logWarning(msg: String) {
        println("Warning: $msg")
    }

    override fun logException(e: Throwable) {
        println("Error: ${e.message}")
    }
}