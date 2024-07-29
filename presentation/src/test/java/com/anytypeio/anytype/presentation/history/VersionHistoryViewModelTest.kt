package com.anytypeio.anytype.presentation.history

import android.util.Log
import app.cash.turbine.turbineScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.StubSpaceMember
import com.anytypeio.anytype.core_models.history.Version
import com.anytypeio.anytype.core_models.multiplayer.ParticipantStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.primitives.TimeInSeconds
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.history.GetVersions
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.domain.misc.UrlBuilder
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.widgets.collection.DateProviderImpl
import java.util.Locale
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import net.bytebuddy.utility.RandomString
import net.lachlanmckee.timberjunit.TimberTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

class VersionHistoryViewModelTest {

    private val objectId = "objectId-${RandomString.make()}"
    private val spaceId = "spaceId-${RandomString.make()}"
    private val vmParams = VersionHistoryViewModel.VmParams(
        objectId = objectId,
        spaceId = "spaceId-${RandomString.make()}"
    )

    private val dispatcher = StandardTestDispatcher(TestCoroutineScheduler())

    @get:Rule
    val timberTestRule: TimberTestRule = TimberTestRule.builder()
        .minPriority(Log.DEBUG)
        .showThread(true)
        .showTimestamp(false)
        .onlyLogWhenTestFails(false)
        .build()

    @OptIn(ExperimentalCoroutinesApi::class)
    val dispatchers = AppCoroutineDispatchers(
        io = dispatcher,
        main = dispatcher,
        computation = dispatcher
    ).also { Dispatchers.setMain(dispatcher) }

    @Mock
    lateinit var analytics: Analytics

    @Mock
    lateinit var getVersions: GetVersions

    @Mock
    lateinit var urlBuilder: UrlBuilder

    @Mock
    lateinit var objectSearch: SearchObjects

    private val dateProvider: DateProvider = DateProviderImpl()

    @Mock
    lateinit var localeProvider: LocaleProvider

    lateinit var spaceManager: SpaceManager

    lateinit var vm: VersionHistoryViewModel

    private val user1 = StubSpaceMember(
        space = spaceId,
        name = "user1",
        iconEmoji = "👩‍🎤",
        identity = "identity1",
        memberStatus = ParticipantStatus.ACTIVE,
        memberPermissions = listOf(SpaceMemberPermissions.OWNER)
    )

    private val user2 = StubSpaceMember(
        space = spaceId,
        name = "user2",
        iconEmoji = "👨‍🎤",
        identity = "identity2",
        memberStatus = ParticipantStatus.ACTIVE,
        memberPermissions = listOf(SpaceMemberPermissions.WRITER)
    )

    private val user3 = StubSpaceMember(
        space = spaceId,
        name = "user3",
        iconEmoji = "👩‍🎤",
        identity = "identity3",
        memberStatus = ParticipantStatus.ACTIVE,
        memberPermissions = listOf(SpaceMemberPermissions.READER)
    )

    private val user4 = StubSpaceMember(
        space = spaceId,
        name = "user4",
        iconEmoji = "👩‍🎤",
        identity = "identity4",
        memberStatus = ParticipantStatus.REMOVED,
        memberPermissions = listOf(SpaceMemberPermissions.READER)
    )

    /**
     * Timeline, GMT+0200
     * Start : Sat Jan 01 2022 00:00:03 GMT+0100 1640991603 User1
     *         Sat Jan 01 2022 00:00:02 GMT+0100 1640991602 User4
     *         Sat Jan 01 2022 00:00:01 GMT+0100 1640991601 User3
     *         Sat Jan 01 2022 00:00:00 GMT+0100 1640991600 User1
     *         Fri Dec 31 2021 23:59:59 GMT+0100 1640991599 User1
     *         Fri Dec 31 2021 23:59:58 GMT+0100 1640991598 User1
     *         Fri Dec 31 2021 23:59:57 GMT+0100 1640991597 User2
     */

    private val timestamp0 = TimeInSeconds(1640991603L)
    private val timestamp1 = TimeInSeconds(1640991602L)
    private val timestamp2 = TimeInSeconds(1640991601L)
    private val timestamp3 = TimeInSeconds(1640991600L)
    private val timestamp4 = TimeInSeconds(1640991599L)
    private val timestamp5 = TimeInSeconds(1640991598L)
    private val timestamp6 = TimeInSeconds(1640991597L)

    private val versions = listOf(
        StubVersion(
            authorId = user1.id,
            timestamp = timestamp0
        ),
        StubVersion(
            authorId = user4.id,
            timestamp = timestamp1
        ),
        StubVersion(
            authorId = user3.id,
            timestamp = timestamp2
        ),
        StubVersion(
            authorId = user1.id,
            timestamp = timestamp3
        ),
        StubVersion(
            authorId = user1.id,
            timestamp = timestamp4
        ),
        StubVersion(
            authorId = user1.id,
            timestamp = timestamp5
        ),
        StubVersion(
            authorId = user2.id,
            timestamp = timestamp6
        )
    )

    @Before
    fun before() {
        MockitoAnnotations.openMocks(this)
        spaceManager = mock(verboseLogging = true)
        stubLocale(locale = Locale.CANADA)
    }

    @Test
    fun `should has proper date`() = runTest {
        turbineScope {

            stubVersions(stubbedVersions = versions)
            stubSpaceMembers()
            vm = buildViewModel()

            val locale = localeProvider.locale()

            val (date0, time0) = dateProvider.formatTimestampToDateAndTime(
                timestamp = versions[0].timestamp.inMillis,
                locale = locale
            )
            val (date1, time1) = dateProvider.formatTimestampToDateAndTime(
                timestamp = versions[1].timestamp.inMillis,
                locale = locale
            )
            val (date2, time2) = dateProvider.formatTimestampToDateAndTime(
                timestamp = versions[2].timestamp.inMillis,
                locale = locale
            )
            val (date3, time3) = dateProvider.formatTimestampToDateAndTime(
                timestamp = versions[3].timestamp.inMillis,
                locale = locale
            )
            val (date4, time4) = dateProvider.formatTimestampToDateAndTime(
                timestamp = versions[4].timestamp.inMillis,
                locale = locale
            )
            val (date5, time5) = dateProvider.formatTimestampToDateAndTime(
                timestamp = versions[5].timestamp.inMillis,
                locale = locale
            )
            val (date6, time6) = dateProvider.formatTimestampToDateAndTime(
                timestamp = versions[6].timestamp.inMillis,
                locale = locale
            )

            val viewStateFlow = vm.viewState.testIn(backgroundScope)

            assertIs<VersionHistoryState.Loading>(viewStateFlow.awaitItem())

            val expected = VersionHistoryState.Success(
                groups = buildList {
                    add(
                        VersionHistoryGroup(
                            id = versions[0].id,
                            title = date0,
                            icons = listOf(ObjectIcon.None, ObjectIcon.None, ObjectIcon.None, ObjectIcon.None),
                            items = buildList {
                                add(
                                    VersionHistoryGroup.Item(
                                        id = versions[0].id,
                                        spaceMember = user1.id,
                                        spaceMemberName = user1.name!!,
                                        timeStamp = versions[0].timestamp,
                                        icon = ObjectIcon.None,
                                        versions = listOf(versions[0]),
                                        timeFormatted = time0
                                    )
                                )
                                add(
                                    VersionHistoryGroup.Item(
                                        id = versions[1].id,
                                        spaceMember = user4.id,
                                        spaceMemberName = user4.name!!,
                                        timeStamp = versions[1].timestamp,
                                        icon = ObjectIcon.None,
                                        versions = listOf(versions[1]),
                                        timeFormatted = time1
                                    )
                                )
                                add(
                                    VersionHistoryGroup.Item(
                                        id = versions[2].id,
                                        spaceMember = user3.id,
                                        spaceMemberName = user3.name!!,
                                        timeStamp = versions[2].timestamp,
                                        icon = ObjectIcon.None,
                                        versions = listOf(versions[2]),
                                        timeFormatted = time2
                                    )
                                )
                                add(
                                    VersionHistoryGroup.Item(
                                        id = versions[3].id,
                                        spaceMember = user1.id,
                                        spaceMemberName = user1.name!!,
                                        timeStamp = versions[3].timestamp,
                                        icon = ObjectIcon.None,
                                        versions = listOf(versions[3]),
                                        timeFormatted = time3
                                    )
                                )
                            }
                        )
                    )
                    add(
                        VersionHistoryGroup(
                            id = versions[4].id,
                            title = date4,
                            icons = listOf(ObjectIcon.None, ObjectIcon.None),
                            items = buildList {
                                add(
                                    VersionHistoryGroup.Item(
                                        id = versions[4].id,
                                        spaceMember = user1.id,
                                        spaceMemberName = user1.name!!,
                                        timeStamp = versions[4].timestamp,
                                        icon = ObjectIcon.None,
                                        versions = listOf(versions[4], versions[5]),
                                        timeFormatted = time4
                                    )
                                )
                                add(
                                    VersionHistoryGroup.Item(
                                        id = versions[6].id,
                                        spaceMember = user2.id,
                                        spaceMemberName = user2.name!!,
                                        timeStamp = versions[6].timestamp,
                                        icon = ObjectIcon.None,
                                        versions = listOf(versions[6]),
                                        timeFormatted = time6
                                    )
                                )
                            }
                        )
                    )
                }
            )

            assertEquals(
                expected = expected,
                actual = viewStateFlow.awaitItem()
            )
        }
    }

    private fun stubSpaceMembers() {
        val filters =
            ObjectSearchConstants.filterParticipants(spaces = listOf(vmParams.spaceId))
        val params = SearchObjects.Params(
            filters = filters,
            keys = ObjectSearchConstants.spaceMemberKeys
        )
        objectSearch.stub {
            onBlocking { invoke(params) } doReturn Either.Right(listOf(user1, user2, user3, user4))
        }
    }

    private fun stubVersions(stubbedVersions: List<Version>) {
        val params = GetVersions.Params(
            objectId = objectId
        )
        getVersions.stub {
            onBlocking { async(params) } doReturn Resultat.success(stubbedVersions)
        }
    }

    private fun stubLocale(locale: Locale) {
        localeProvider.stub {
            on { locale() } doReturn locale
        }
    }

    private fun buildViewModel(): VersionHistoryViewModel {
        return VersionHistoryViewModel(
            analytics = analytics,
            getVersions = getVersions,
            objectSearch = objectSearch,
            dateProvider = dateProvider,
            localeProvider = localeProvider,
            vmParams = vmParams,
            urlBuilder = urlBuilder
        )
    }
}