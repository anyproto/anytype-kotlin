package com.anytypeio.anytype.presentation.editor.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.domain.base.Either
import com.anytypeio.anytype.presentation.util.CoroutinesTestRule
import com.anytypeio.anytype.test_utils.MockDataFactory
import kotlin.test.assertEquals
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
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

    @get:Rule
    val coroutineTestRule = CoroutinesTestRule()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `should not consume toast with message about downloading file if subscribed after message`() {

        val consumed = mutableListOf<String>()

        val file = Block(
            id = MockDataFactory.randomUuid(),
            content = Block.Content.File(
                targetObjectId = MockDataFactory.randomUuid(),
                type = Block.Content.File.Type.FILE,
                state = Block.Content.File.State.DONE
            ),
            fields = Block.Fields.empty(),
            children = emptyList()
        )

        val doc = listOf(
            Block(
                id = root,
                fields = Block.Fields(emptyMap()),
                content = Block.Content.Smart,
                children = listOf(file.id)
            ),
            file
        )

        stubOpenDocument(doc)
        stubInterceptEvents()
        stubDownloadFile()

        val vm = buildViewModel()

        vm.onStart(root)

        runBlockingTest {

            val subscription1 = launch { vm.toasts.collect { consumed.add(it) } }

            // Launching operation that triggers a toast

            vm.startDownloadingFile(blockId = file.id)

            val subscription2 = launch { vm.toasts.collect { consumed.add(it) } }

            subscription1.cancel()
            subscription2.cancel()

            // Checking that we have only one toast event consumed by subscribers

            assertEquals(
                expected = 1,
                actual = consumed.size
            )
        }
    }

    private fun stubDownloadFile() {
        downloadFile.stub {
            onBlocking { invoke(any()) } doReturn Either.Right(Unit)
        }
    }

}