package com.anytypeio.anytype.domain.download

import com.anytypeio.anytype.core_models.FileDownloadLimit
import com.anytypeio.anytype.domain.config.UserSettingsRepository
import com.anytypeio.anytype.domain.util.dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class FileDownloadLimitUseCasesTest {

    private val repo: UserSettingsRepository = mock()

    @Test
    fun `GetFileDownloadLimit delegates to repository`() = runTest {
        whenever(repo.getFileDownloadLimit()).thenReturn(FileDownloadLimit.MB_100)
        val useCase = GetFileDownloadLimit(repo, dispatchers)
        val result = useCase.run(Unit)
        assertEquals(FileDownloadLimit.MB_100, result)
        verify(repo).getFileDownloadLimit()
    }

    @Test
    fun `SetFileDownloadLimit delegates to repository`() = runTest {
        val useCase = SetFileDownloadLimit(repo, dispatchers)
        useCase.run(FileDownloadLimit.GB_1)
        verify(repo).setFileDownloadLimit(FileDownloadLimit.GB_1)
    }

    @Test
    fun `GetUseCellularForDownloads delegates to repository`() = runTest {
        whenever(repo.getUseCellularForDownloads()).thenReturn(true)
        val useCase = GetUseCellularForDownloads(repo, dispatchers)
        val result = useCase.run(Unit)
        assertEquals(true, result)
        verify(repo).getUseCellularForDownloads()
    }

    @Test
    fun `SetUseCellularForDownloads delegates to repository`() = runTest {
        val useCase = SetUseCellularForDownloads(repo, dispatchers)
        useCase.run(true)
        verify(repo).setUseCellularForDownloads(true)
    }
}
