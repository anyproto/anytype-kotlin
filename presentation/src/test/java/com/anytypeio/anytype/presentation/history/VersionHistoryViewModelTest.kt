package com.anytypeio.anytype.presentation.history

import app.cash.turbine.turbineScope
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.StubSpaceMember
import com.anytypeio.anytype.core_models.history.Version
import com.anytypeio.anytype.core_models.multiplayer.ParticipantStatus
import com.anytypeio.anytype.core_models.multiplayer.SpaceMemberPermissions
import com.anytypeio.anytype.core_models.primitives.SpaceId
import com.anytypeio.anytype.core_models.primitives.TimestampInSeconds
import com.anytypeio.anytype.domain.base.AppCoroutineDispatchers
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.domain.history.GetVersions
import com.anytypeio.anytype.domain.history.SetVersion
import com.anytypeio.anytype.domain.history.ShowVersion
import com.anytypeio.anytype.domain.misc.DateProvider
import com.anytypeio.anytype.domain.misc.LocaleProvider
import com.anytypeio.anytype.core_models.UrlBuilder
import com.anytypeio.anytype.domain.objects.StoreOfObjectTypes
import com.anytypeio.anytype.domain.objects.StoreOfRelations
import com.anytypeio.anytype.domain.search.SearchObjects
import com.anytypeio.anytype.domain.workspace.SpaceManager
import com.anytypeio.anytype.presentation.editor.cover.CoverImageHashProvider
import com.anytypeio.anytype.presentation.editor.render.DefaultBlockViewRenderer
import com.anytypeio.anytype.presentation.history.VersionHistoryViewModel.Companion.GROUP_DATE_FORMAT_OTHER_YEAR
import com.anytypeio.anytype.presentation.history.VersionHistoryViewModel.Companion.VERSIONS_MAX_LIMIT
import com.anytypeio.anytype.core_models.ui.ObjectIcon
import com.anytypeio.anytype.presentation.search.ObjectSearchConstants
import com.anytypeio.anytype.presentation.sets.state.DefaultObjectStateReducer
import java.time.ZoneId
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
import org.junit.Before
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub

class VersionHistoryViewModelTest {

    private val objectId = "objectId-${RandomString.make()}"
    private val spaceId = "spaceId-${RandomString.make()}"
    private val vmParams = VersionHistoryViewModel.VmParams(
        objectId = objectId,
        spaceId = SpaceId("spaceId-${RandomString.make()}")
    )

    private val dispatcher = StandardTestDispatcher(TestCoroutineScheduler())

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
    lateinit var setVersion: SetVersion

    @Mock
    lateinit var showVersion: ShowVersion

    @Mock
    lateinit var urlBuilder: UrlBuilder

    @Mock
    lateinit var objectSearch: SearchObjects

    @Mock
    lateinit var dateProvider: DateProvider

    @Mock
    lateinit var localeProvider: LocaleProvider

    @Mock
    lateinit var renderer: DefaultBlockViewRenderer

    @Mock
    lateinit var storeOfRelations: StoreOfRelations

    @Mock
    lateinit var storeOfObjectTypes: StoreOfObjectTypes

    @Mock
    lateinit var coverImageHashProvider: CoverImageHashProvider

    lateinit var spaceManager: SpaceManager

    lateinit var vm: VersionHistoryViewModel

    lateinit var lockedZoneIdMocked: MockedStatic<ZoneId>

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
     * Start :
     *         Sun Jan 02 2022 00:02:00 GMT+0100 1641078120 User2
     *         Sun Jan 02 2022 00:01:02 GMT+0100 1641078062 User2
     *         Sun Jan 02 2022 00:01:01 GMT+0100 1641078061 User1
     *         Sun Jan 02 2022 00:01:00 GMT+0100 1641078060 User1
     *         Sun Jan 02 2022 00:00:59 GMT+0100 1641078059 User1
     *         Sun Jan 02 2022 00:00:04 GMT+0100 1641078004 User1
     *         Sun Jan 02 2022 00:00:03 GMT+0100 1641078003 User1
     *         Sun Jan 02 2022 00:00:01 GMT+0100 1641078001 User1
     *         Sun Jan 02 2022 00:00:00 GMT+0100 1641078000 User1
     *         --------new test ^^^
     *         Sat Jan 01 2022 00:00:03 GMT+0100 1640991603 User1
     *         Sat Jan 01 2022 00:00:02 GMT+0100 1640991602 User4
     *         Sat Jan 01 2022 00:00:01 GMT+0100 1640991601 User3
     *         Sat Jan 01 2022 00:00:00 GMT+0100 1640991600 User1
     *         Fri Dec 31 2021 23:59:59 GMT+0100 1640991599 User1
     *         Fri Dec 31 2021 23:59:58 GMT+0100 1640991598 User1
     *         Fri Dec 31 2021 23:59:57 GMT+0100 1640991597 User2
     */

    private val timestamp0 = TimestampInSeconds(1640991603L)
    private val timestamp1 = TimestampInSeconds(1640991602L)
    private val timestamp2 = TimestampInSeconds(1640991601L)
    private val timestamp3 = TimestampInSeconds(1640991600L)
    private val timestamp4 = TimestampInSeconds(1640991599L)
    private val timestamp5 = TimestampInSeconds(1640991598L)
    private val timestamp6 = TimestampInSeconds(1640991597L)

    private val timestamp7 = TimestampInSeconds(1641078000L)
    private val timestamp8 = TimestampInSeconds(1641078001L)
    private val timestamp9 = TimestampInSeconds(1641078003L)
    private val timestamp10 = TimestampInSeconds(1641078004L)
    private val timestamp11 = TimestampInSeconds(1641078059L)
    private val timestamp12 = TimestampInSeconds(1641078060L)
    private val timestamp13 = TimestampInSeconds(1641078061L)
    private val timestamp14 = TimestampInSeconds(1641078062L)
    private val timestamp15 = TimestampInSeconds(1641078120L)

    private val versions = listOf(
        StubVersion(
            id = "versionId-${timestamp0.time}",
            authorId = user1.id,
            timestamp = timestamp0
        ),
        StubVersion(
            id = "versionId-${timestamp1.time}",
            authorId = user4.id,
            timestamp = timestamp1
        ),
        StubVersion(
            id = "versionId-${timestamp2.time}",
            authorId = user3.id,
            timestamp = timestamp2
        ),
        StubVersion(
            id = "versionId-${timestamp3.time}",
            authorId = user1.id,
            timestamp = timestamp3
        ),
        StubVersion(
            id = "versionId-${timestamp4.time}",
            authorId = user1.id,
            timestamp = timestamp4
        ),
        StubVersion(
            id = "versionId-${timestamp5.time}",
            authorId = user1.id,
            timestamp = timestamp5
        ),
        StubVersion(
            id = "versionId-${timestamp6.time}",
            authorId = user2.id,
            timestamp = timestamp6
        )
    )

    private val versionsNew = listOf(
        StubVersion(
            id = "versionId-${timestamp7.time}",
            authorId = user1.id,
            timestamp = timestamp7
        ),
        StubVersion(
            id = "versionId-${timestamp8.time}",
            authorId = user1.id,
            timestamp = timestamp8
        ),
        StubVersion(
            id = "versionId-${timestamp9.time}",
            authorId = user1.id,
            timestamp = timestamp9
        ),
        StubVersion(
            id = "versionId-${timestamp10.time}",
            authorId = user1.id,
            timestamp = timestamp10
        ),
        StubVersion(
            id = "versionId-${timestamp11.time}",
            authorId = user1.id,
            timestamp = timestamp11
        ),
        StubVersion(
            id = "versionId-${timestamp12.time}",
            authorId = user1.id,
            timestamp = timestamp12
        ),
        StubVersion(
            id = "versionId-${timestamp13.time}",
            authorId = user1.id,
            timestamp = timestamp13
        ),
        StubVersion(
            id = "versionId-${timestamp14.time}",
            authorId = user2.id,
            timestamp = timestamp14
        ),
        StubVersion(
            id = "versionId-${timestamp15.time}",
            authorId = user2.id,
            timestamp = timestamp15
        )
    )

    @Before
    fun before() {
        MockitoAnnotations.openMocks(this)
        spaceManager = mock(verboseLogging = true)
        lockedZoneIdMocked = Mockito.mockStatic(ZoneId::class.java, Mockito.CALLS_REAL_METHODS)
        val zoneId = ZoneId.of("Europe/Berlin")
        lockedZoneIdMocked.`when`<ZoneId> { ZoneId.systemDefault() }
            .thenReturn(zoneId)
        stubLocale(locale = Locale.CANADA)
    }

    //@Test
    fun `should has proper date`() = runTest {
        turbineScope {

            stubVersions(stubbedVersions = versions + versionsNew)
            stubSpaceMembers()
            vm = buildViewModel()

            val locale = localeProvider.locale()

            val (date0, time0) = dateProvider.formatTimestampToDateAndTime(
                timestamp = versions[0].timestamp.inMillis,
            )
            val (date1, time1) = dateProvider.formatTimestampToDateAndTime(
                timestamp = versions[1].timestamp.inMillis,

            )
            val (date2, time2) = dateProvider.formatTimestampToDateAndTime(
                timestamp = versions[2].timestamp.inMillis,

            )
            val (date3, time3) = dateProvider.formatTimestampToDateAndTime(
                timestamp = versions[3].timestamp.inMillis,

            )
            val (date4, time4) = dateProvider.formatTimestampToDateAndTime(
                timestamp = versions[4].timestamp.inMillis,

            )
            val (date6, time6) = dateProvider.formatTimestampToDateAndTime(
                timestamp = versions[6].timestamp.inMillis,

            )

            val viewStateFlow = vm.viewState.testIn(backgroundScope)

            assertIs<VersionHistoryState.Loading>(viewStateFlow.awaitItem())

            val expected = VersionHistoryState.Success(
                groups = buildList {
                    add(
                        VersionHistoryGroup(
                            id = versionsNew[0].id,
                            title = VersionHistoryGroup.GroupTitle.Date(
                                dateProvider.formatToDateString(
                                    timestamp = versionsNew[8].timestamp.inMillis,
                                    pattern = GROUP_DATE_FORMAT_OTHER_YEAR
                                )
                            ),
                            icons = listOf(ObjectIcon.None, ObjectIcon.None),
                            isExpanded = true,
                            items = buildList {
                                val versionsNew8Format = dateProvider.formatTimestampToDateAndTime(
                                    timestamp = versionsNew[8].timestamp.inMillis,

                                )
                                add(
                                    VersionHistoryGroup.Item(
                                        id = versionsNew[8].id,
                                        spaceMember = user2.id,
                                        spaceMemberName = user2.name!!,
                                        timeStamp = versionsNew[8].timestamp,
                                        icon = ObjectIcon.None,
                                        versions = listOf(versionsNew[8]),
                                        timeFormatted = versionsNew8Format.second,
                                        dateFormatted = versionsNew8Format.first
                                    )
                                )
                                val versionsNew7Format = dateProvider.formatTimestampToDateAndTime(
                                    timestamp = versionsNew[7].timestamp.inMillis,
                                )
                                add(
                                    VersionHistoryGroup.Item(
                                        id = versionsNew[7].id,
                                        spaceMember = user2.id,
                                        spaceMemberName = user2.name!!,
                                        timeStamp = versionsNew[7].timestamp,
                                        icon = ObjectIcon.None,
                                        versions = listOf(versionsNew[7]),
                                        timeFormatted = versionsNew7Format.second,
                                        dateFormatted = versionsNew7Format.first
                                    )
                                )
                                val versionsNew6Format = dateProvider.formatTimestampToDateAndTime(
                                    timestamp = versionsNew[6].timestamp.inMillis,
                                )
                                add(
                                    VersionHistoryGroup.Item(
                                        id = versionsNew[6].id,
                                        spaceMember = user1.id,
                                        spaceMemberName = user1.name!!,
                                        timeStamp = versionsNew[6].timestamp,
                                        icon = ObjectIcon.None,
                                        versions = listOf(versionsNew[6], versionsNew[5]),
                                        timeFormatted = versionsNew6Format.second,
                                        dateFormatted = versionsNew6Format.first
                                    )
                                )
                                val versionsNew4Format = dateProvider.formatTimestampToDateAndTime(
                                    timestamp = versionsNew[4].timestamp.inMillis,
                                )
                                add(
                                    VersionHistoryGroup.Item(
                                        id = versionsNew[4].id,
                                        spaceMember = user1.id,
                                        spaceMemberName = user1.name!!,
                                        timeStamp = versionsNew[4].timestamp,
                                        icon = ObjectIcon.None,
                                        versions = listOf(versionsNew[4], versionsNew[3], versionsNew[2], versionsNew[1], versionsNew[0]),
                                        timeFormatted = versionsNew4Format.second,
                                        dateFormatted = versionsNew4Format.first
                                    )
                                )
                            }

                        )
                    )
                    add(
                        VersionHistoryGroup(
                            id = versions[3].id,
                            title = VersionHistoryGroup.GroupTitle.Date(dateProvider.formatToDateString(
                                timestamp = versions[0].timestamp.inMillis,
                                pattern = GROUP_DATE_FORMAT_OTHER_YEAR,
                            )),
                            icons = listOf(ObjectIcon.None, ObjectIcon.None, ObjectIcon.None),
                            items = buildList {
                                add(
                                    VersionHistoryGroup.Item(
                                        id = versions[0].id,
                                        spaceMember = user1.id,
                                        spaceMemberName = user1.name!!,
                                        timeStamp = versions[0].timestamp,
                                        icon = ObjectIcon.None,
                                        versions = listOf(versions[0]),
                                        timeFormatted = time0,
                                        dateFormatted = date0
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
                                        timeFormatted = time1,
                                        dateFormatted = date1
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
                                        timeFormatted = time2,
                                        dateFormatted = date2
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
                                        timeFormatted = time3,
                                        dateFormatted = date3
                                    )
                                )
                            }
                        )
                    )
                    add(
                        VersionHistoryGroup(
                            id = versions[6].id,
                            title = VersionHistoryGroup.GroupTitle.Date(
                                dateProvider.formatToDateString(
                                    timestamp = versions[4].timestamp.inMillis,
                                    pattern = GROUP_DATE_FORMAT_OTHER_YEAR
                                )
                            ),
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
                                        timeFormatted = time4,
                                        dateFormatted = date4
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
                                        timeFormatted = time6,
                                        dateFormatted = date6
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
            ObjectSearchConstants.filterParticipants(vmParams.spaceId)
        val params = SearchObjects.Params(
            space = vmParams.spaceId,
            filters = filters,
            keys = ObjectSearchConstants.spaceMemberKeys
        )
        objectSearch.stub {
            onBlocking { invoke(params) } doReturn Either.Right(listOf(user1, user2, user3, user4))
        }
    }

    private fun stubVersions(stubbedVersions: List<Version>) {
        val params = GetVersions.Params(
            objectId = objectId,
            lastVersion = "",
            limit = VERSIONS_MAX_LIMIT
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
            vmParams = vmParams,
            urlBuilder = urlBuilder,
            renderer = renderer,
            setVersion = setVersion,
            showVersion = showVersion,
            setStateReducer = DefaultObjectStateReducer(),
            storeOfRelations = storeOfRelations,
            storeOfObjectTypes = storeOfObjectTypes,
        )
    }
}