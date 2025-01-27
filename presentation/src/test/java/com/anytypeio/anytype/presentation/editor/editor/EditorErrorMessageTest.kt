package com.anytypeio.anytype.presentation.editor.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.AllObjectsDetails
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubFile
import com.anytypeio.anytype.core_models.StubObject
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub

class EditorErrorMessageTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        proceedWithDefaultBeforeTestStubbing()
    }

    @Test
    fun `should not consume toast with message about downloading file if subscribed after message`() = runTest {

        val consumed = mutableListOf<String>()

        val fileObjectId = MockDataFactory.randomUuid()

        val fileBlock = StubFile(
            type = Block.Content.File.Type.FILE,
            state = Block.Content.File.State.DONE,
            targetObjectId = fileObjectId
        )

        val details = AllObjectsDetails(
            mapOf(
                fileObjectId to
                    mapOf(
                        Relations.ID to fileObjectId,
                        Relations.NAME to "file object",
                        Relations.SIZE_IN_BYTES to 10000.0,
                        Relations.FILE_MIME_TYPE to "pdf",
                        Relations.LAYOUT to ObjectType.Layout.FILE.code.toDouble()
                    )
                )
        )

        val doc = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart,
                children = listOf(fileBlock.id)
            ),
            fileBlock
        )

        stubOpenDocument(doc, details)
        stubInterceptEvents()
        stubDownloadFile()

        val vm = buildViewModel()

        advanceUntilIdle()

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        val subscription1 = launch { vm.toasts.collect { consumed.add(it) } }

        // Launching operation that triggers a toast

        vm.startDownloadingFileFromBlock(id = fileBlock.id)

        advanceUntilIdle()

        val subscription2 = launch { vm.toasts.collect { consumed.add(it) } }

        subscription1.cancel()
        subscription2.cancel()

        // Checking that we have only one toast event consumed by subscribers

        assertEquals(
            expected = 1,
            actual = consumed.size
        )
    }

    private fun stubDownloadFile() {
        downloadFile.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(Unit)
        }
    }

}