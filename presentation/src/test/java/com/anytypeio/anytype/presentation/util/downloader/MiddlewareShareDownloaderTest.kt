package com.anytypeio.anytype.presentation.util.downloader

import android.content.Context
import android.net.Uri
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.presentation.util.dispatchers
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class MiddlewareShareDownloaderTest {

    @get:Rule
    val folder = TemporaryFolder()

    private lateinit var cache: File
    private lateinit var context: Context

    private val uri = mock<Uri>()
    private val requestedFiles = mutableListOf<File>()
    private val uriFileProvider = object : UriFileProvider {
        override fun getUriForFile(file: File): Uri {
            requestedFiles.add(file)
            return uri
        }
    }

    @Before
    fun setup() {
        cache = folder.newFolder("cache")
        context = mock { on { cacheDir } doReturn cache }
    }

    @Test
    fun `should move the downloaded file to the result path and return its uri`() = runTest {
        val downloader = downloader { _, path ->
            File(path, "file.txt").apply { writeText(CONTENT) }.path
        }

        val response = downloader.run(params)

        val expected = File(cache, "$OBJECT_ID/$NAME")
        assertEquals(uri, response.uri)
        assertEquals(expected.path, File(response.path).path)
        assertEquals(CONTENT, expected.readText())
        assertEquals(listOf(expected.path), requestedFiles.map { it.path })
    }

    @Test
    fun `should fail when the middleware returns a path to a missing file`() = runTest {
        val downloader = downloader { _, path -> File(path, "missing.txt").path }

        val result = downloader.async(params)

        assertTrue(result.isFailure)
        assertTrue(requestedFiles.isEmpty())
    }

    @Test
    fun `should download again when the cached file is empty`() = runTest {
        File(cache, "$OBJECT_ID/$NAME").apply {
            parentFile!!.mkdirs()
            createNewFile()
        }
        var downloads = 0
        val downloader = downloader { _, path ->
            downloads++
            File(path, "file.txt").apply { writeText(CONTENT) }.path
        }

        downloader.run(params)

        assertEquals(1, downloads)
        assertEquals(CONTENT, File(cache, "$OBJECT_ID/$NAME").readText())
    }

    @Test
    fun `should reuse the cached file without downloading it`() = runTest {
        File(cache, "$OBJECT_ID/$NAME").apply {
            parentFile!!.mkdirs()
            writeText(CONTENT)
        }
        var downloads = 0
        val downloader = downloader { _, path ->
            downloads++
            path
        }

        val response = downloader.run(params)

        assertEquals(0, downloads)
        assertEquals(uri, response.uri)
    }

    private fun downloader(
        download: (Id, String) -> String
    ) = object : MiddlewareShareDownloader(context, uriFileProvider, dispatchers) {
        override suspend fun downloadFile(objectId: Id, path: String): String =
            download(objectId, path)
    }

    companion object {
        const val OBJECT_ID = "file-object-id"
        const val NAME = "sample.txt"
        const val CONTENT = "content"
        val params = MiddlewareShareDownloader.Params(objectId = OBJECT_ID, name = NAME)
    }
}
