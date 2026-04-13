package com.anytypeio.anytype.presentation.settings

import app.cash.turbine.test
import com.anytypeio.anytype.analytics.base.Analytics
import com.anytypeio.anytype.core_models.FileDownloadLimit
import com.anytypeio.anytype.domain.account.DeleteAccount
import com.anytypeio.anytype.domain.device.ClearFileCache
import com.anytypeio.anytype.domain.download.GetFileDownloadLimit
import com.anytypeio.anytype.domain.download.GetUseCellularForDownloads
import com.anytypeio.anytype.domain.download.SetFileDownloadLimit
import com.anytypeio.anytype.domain.download.SetUseCellularForDownloads
import com.anytypeio.anytype.domain.workspace.InterceptFileLimitEvents
import com.anytypeio.anytype.domain.workspace.SpacesUsageInfo
import com.anytypeio.anytype.presentation.device.BuildProvider
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.mockito.kotlin.verifyBlocking
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class FilesStorageViewModelTest {

    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    @Mock lateinit var analytics: Analytics
    @Mock lateinit var clearFileCache: ClearFileCache
    @Mock lateinit var spacesUsageInfo: SpacesUsageInfo
    @Mock lateinit var interceptFileLimitEvents: InterceptFileLimitEvents
    @Mock lateinit var buildProvider: BuildProvider
    @Mock lateinit var deleteAccount: DeleteAccount
    @Mock lateinit var getFileDownloadLimit: GetFileDownloadLimit
    @Mock lateinit var setFileDownloadLimit: SetFileDownloadLimit
    @Mock lateinit var getUseCellularForDownloads: GetUseCellularForDownloads
    @Mock lateinit var setUseCellularForDownloads: SetUseCellularForDownloads

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        // Default stubs to keep onStart() from hanging on hot streams
        spacesUsageInfo.stub {
            on { stream(Unit) } doReturn emptyFlow()
        }
        interceptFileLimitEvents.stub {
            onBlocking { run(Unit) } doReturn emptyFlow()
        }
        buildProvider.stub {
            on { getManufacturer() } doReturn "Pixel"
            on { getModel() } doReturn "Pixel 7"
        }
        // Default download settings stubs (each test can override)
        getFileDownloadLimit.stub {
            onBlocking { run(Unit) } doReturn FileDownloadLimit.DEFAULT
        }
        getUseCellularForDownloads.stub {
            onBlocking { run(Unit) } doReturn false
        }
        setFileDownloadLimit.stub {
            onBlocking { run(any()) } doReturn Unit
        }
        setUseCellularForDownloads.stub {
            onBlocking { run(any()) } doReturn Unit
        }
    }

    @Test
    fun `initial state reflects persisted limit and cellular toggle`() = runTest {
        // Given
        getFileDownloadLimit.stub {
            onBlocking { run(Unit) } doReturn FileDownloadLimit.MB_100
        }
        getUseCellularForDownloads.stub {
            onBlocking { run(Unit) } doReturn true
        }

        // When
        val vm = buildViewModel()
        vm.onStart()
        advanceUntilIdle()

        // Then
        assertEquals(FileDownloadLimit.MB_100, vm.downloadLimit.value)
        assertEquals(true, vm.useCellular.value)
    }

    @Test
    fun `onOfflineDownloadsClicked emits ShowOfflineDownloadsSelector with current value`() =
        runTest {
            // Given
            getFileDownloadLimit.stub {
                onBlocking { run(Unit) } doReturn FileDownloadLimit.GB_1
            }

            val vm = buildViewModel()
            vm.onStart()
            advanceUntilIdle()

            // When / Then
            vm.commands.test {
                vm.event(FilesStorageViewModel.Event.OnOfflineDownloadsClicked)
                advanceUntilIdle()
                assertEquals(
                    FilesStorageViewModel.Command.ShowOfflineDownloadsSelector(
                        FileDownloadLimit.GB_1
                    ),
                    awaitItem()
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `onOfflineDownloadsValueSelected persists value and updates state`() = runTest {
        // Given
        val vm = buildViewModel()
        vm.onStart()
        advanceUntilIdle()

        // When
        vm.onOfflineDownloadsValueSelected(FileDownloadLimit.MB_250)
        advanceUntilIdle()

        // Then
        verifyBlocking(setFileDownloadLimit) { run(FileDownloadLimit.MB_250) }
        assertEquals(FileDownloadLimit.MB_250, vm.downloadLimit.value)
    }

    @Test
    fun `onUseCellularToggled persists and updates state`() = runTest {
        // Given
        val vm = buildViewModel()
        vm.onStart()
        advanceUntilIdle()

        // When
        vm.onUseCellularToggled(true)
        advanceUntilIdle()

        // Then
        verifyBlocking(setUseCellularForDownloads) { run(true) }
        assertEquals(true, vm.useCellular.value)
    }

    private fun buildViewModel() = FilesStorageViewModel(
        analytics = analytics,
        clearFileCache = clearFileCache,
        spacesUsageInfo = spacesUsageInfo,
        interceptFileLimitEvents = interceptFileLimitEvents,
        buildProvider = buildProvider,
        deleteAccount = deleteAccount,
        getFileDownloadLimit = getFileDownloadLimit,
        setFileDownloadLimit = setFileDownloadLimit,
        getUseCellularForDownloads = getUseCellularForDownloads,
        setUseCellularForDownloads = setUseCellularForDownloads
    )
}
