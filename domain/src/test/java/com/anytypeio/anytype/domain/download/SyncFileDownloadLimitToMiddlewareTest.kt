package com.anytypeio.anytype.domain.download

import com.anytypeio.anytype.core_models.FileDownloadLimit
import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.util.dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoMoreInteractions

class SyncFileDownloadLimitToMiddlewareTest {

    private val repo: BlockRepository = mock()
    private val useCase = SyncFileDownloadLimitToMiddleware(repo, dispatchers)

    @Test
    fun `OFF sends disabled with wifi-only and skips SetLimit`() = runTest {
        useCase.run(
            SyncFileDownloadLimitToMiddleware.Params(
                limit = FileDownloadLimit.OFF,
                useCellular = false
            )
        )
        verifyBlocking(repo) {
            fileSetAutoDownload(enabled = false, wifiOnly = true)
        }
        verifyBlocking(repo, never()) {
            fileAutoDownloadSetLimit(any())
        }
    }

    @Test
    fun `MB_100 with wifi-only sends enabled plus SetLimit 100`() = runTest {
        useCase.run(
            SyncFileDownloadLimitToMiddleware.Params(
                limit = FileDownloadLimit.MB_100,
                useCellular = false
            )
        )
        verifyBlocking(repo) {
            fileSetAutoDownload(enabled = true, wifiOnly = true)
        }
        verifyBlocking(repo) {
            fileAutoDownloadSetLimit(sizeLimitMebibytes = 100L)
        }
    }

    @Test
    fun `MB_250 with cellular sends enabled wifiOnly false plus SetLimit 250`() = runTest {
        useCase.run(
            SyncFileDownloadLimitToMiddleware.Params(
                limit = FileDownloadLimit.MB_250,
                useCellular = true
            )
        )
        verifyBlocking(repo) {
            fileSetAutoDownload(enabled = true, wifiOnly = false)
        }
        verifyBlocking(repo) {
            fileAutoDownloadSetLimit(sizeLimitMebibytes = 250L)
        }
    }

    @Test
    fun `UNLIMITED sends enabled plus SetLimit 0 sentinel`() = runTest {
        useCase.run(
            SyncFileDownloadLimitToMiddleware.Params(
                limit = FileDownloadLimit.UNLIMITED,
                useCellular = true
            )
        )
        verifyBlocking(repo) {
            fileSetAutoDownload(enabled = true, wifiOnly = false)
        }
        verifyBlocking(repo) {
            fileAutoDownloadSetLimit(sizeLimitMebibytes = 0L)
        }
    }

    @Test
    fun `SetAutoDownload failure propagates and skips SetLimit`() = runTest {
        repo.stub {
            onBlocking { fileSetAutoDownload(any(), any()) } doThrow RuntimeException("boom")
        }
        try {
            useCase.run(
                SyncFileDownloadLimitToMiddleware.Params(
                    limit = FileDownloadLimit.MB_100,
                    useCellular = false
                )
            )
            fail("Expected RuntimeException to propagate")
        } catch (e: RuntimeException) {
            assertEquals("boom", e.message)
        }
        verifyBlocking(repo, never()) {
            fileAutoDownloadSetLimit(any())
        }
    }
}
