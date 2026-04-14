package com.anytypeio.anytype.domain.download

import com.anytypeio.anytype.domain.block.repo.BlockRepository
import com.anytypeio.anytype.domain.util.dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyBlocking

class FileAutoDownloadUseCasesTest {

    private val repo: BlockRepository = mock()

    @Test
    fun `FileSetAutoDownload enabled true wifiOnly true delegates to repository`() = runTest {
        val useCase = FileSetAutoDownload(repo, dispatchers)
        useCase.run(
            FileSetAutoDownload.Params(enabled = true, wifiOnly = true)
        )
        verifyBlocking(repo) {
            fileSetAutoDownload(enabled = true, wifiOnly = true)
        }
    }

    @Test
    fun `FileSetAutoDownload enabled false wifiOnly false delegates to repository`() = runTest {
        val useCase = FileSetAutoDownload(repo, dispatchers)
        useCase.run(
            FileSetAutoDownload.Params(enabled = false, wifiOnly = false)
        )
        verifyBlocking(repo) {
            fileSetAutoDownload(enabled = false, wifiOnly = false)
        }
    }

    @Test
    fun `FileAutoDownloadSetLimit delegates to repository with mebibytes`() = runTest {
        val useCase = FileAutoDownloadSetLimit(repo, dispatchers)
        useCase.run(
            FileAutoDownloadSetLimit.Params(sizeLimitMebibytes = 100L)
        )
        verifyBlocking(repo) {
            fileAutoDownloadSetLimit(sizeLimitMebibytes = 100L)
        }
    }

    @Test
    fun `FileAutoDownloadSetLimit zero is forwarded as no-limit sentinel`() = runTest {
        val useCase = FileAutoDownloadSetLimit(repo, dispatchers)
        useCase.run(
            FileAutoDownloadSetLimit.Params(sizeLimitMebibytes = 0L)
        )
        verifyBlocking(repo) {
            fileAutoDownloadSetLimit(sizeLimitMebibytes = 0L)
        }
    }
}
