package com.anytypeio.anytype.presentation.editor.editor.file_layout

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.StubFile
import com.anytypeio.anytype.core_models.StubHeader
import com.anytypeio.anytype.core_models.StubObject
import com.anytypeio.anytype.core_models.StubSmartBlock
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.presentation.editor.editor.EditorPresentationTestSetup
import com.anytypeio.anytype.presentation.editor.editor.ViewState
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.presentation.util.TXT
import com.anytypeio.anytype.presentation.util.downloader.MiddlewareShareDownloader
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.jraska.livedata.test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.bytebuddy.utility.RandomString
import net.lachlanmckee.timberjunit.TimberTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class FileLayoutTest : EditorPresentationTestSetup() {

    @get:Rule
    val timberTestRule: TimberTestRule = TimberTestRule.builder()
        .minPriority(Log.DEBUG)
        .showThread(true)
        .showTimestamp(false)
        .onlyLogWhenTestFails(true)
        .build()

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    val title = StubTitle()
    val header = StubHeader(children = listOf(title.id))
    val fileBlock = StubFile(
        id = "fileBlockId-${MockDataFactory.randomUuid()}",
        targetObjectId = root,
        state = Block.Content.File.State.DONE
    )
    val page = StubSmartBlock(id = root, children = listOf(header.id, fileBlock.id))
    val document = listOf(page, header, title, fileBlock)

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        proceedWithDefaultBeforeTestStubbing()
    }

    //region Editor view state
    @Test
    fun `should change file block to file open block`() = runTest {

        val fileObject = StubObject(
            id = root,
            space = defaultSpace,
            name = "fileObjectName-${RandomString.make(5)}",
            layout = ObjectType.Layout.FILE.code.toDouble(),
            fileExt = "pdf"
        )

        val detailsList = Block.Details(
            details = mapOf(
                fileObject.id to Block.Fields(fileObject.map)
            )
        )

        stubOpenDocument(
            document = document,
            details = detailsList
        )

        val vm = buildViewModel()
        advanceUntilIdle()

        vm.onStart(id = fileObject.id, space = defaultSpace)
        val loadingState = vm.state.test()
        assertEquals(ViewState.Loading, loadingState.value())

        advanceUntilIdle()

        val firstTimeExpected = ViewState.Success(
            listOf(
                BlockView.Title.Basic(
                    isFocused = false,
                    id = title.id,
                    text = title.content<TXT>().text
                ),
                BlockView.OpenFile.File(
                    id = fileBlock.id,
                    targetId = fileObject.id
                )
            )
        )

        val pageState = vm.state.test()
        assertEquals(firstTimeExpected, pageState.value())
    }

    @Test
    fun `should change file block to image open block`() = runTest {

        val fileObject = StubObject(
            id = root,
            space = defaultSpace,
            name = "fileObjectName-${RandomString.make(5)}",
            layout = ObjectType.Layout.IMAGE.code.toDouble(),
            fileExt = "jpg"
        )

        val detailsList = Block.Details(
            details = mapOf(
                fileObject.id to Block.Fields(fileObject.map)
            )
        )

        stubOpenDocument(
            document = document,
            details = detailsList
        )

        val vm = buildViewModel()
        advanceUntilIdle()

        vm.onStart(id = fileObject.id, space = defaultSpace)
        val loadingState = vm.state.test()
        assertEquals(ViewState.Loading, loadingState.value())

        advanceUntilIdle()

        val firstTimeExpected = ViewState.Success(
            listOf(
                BlockView.Title.Basic(
                    isFocused = false,
                    id = title.id,
                    text = title.content<TXT>().text
                ),
                BlockView.OpenFile.Image(
                    id = fileBlock.id,
                    targetId = fileObject.id
                )
            )
        )

        val pageState = vm.state.test()
        assertEquals(firstTimeExpected, pageState.value())
    }
    //endregion

    //region Open file click test
    @Test
    fun `should start sharing file on open file click`() = runTest {

        val fileObject = StubObject(
            id = root,
            space = defaultSpace,
            name = "fileObjectName-${RandomString.make(5)}",
            layout = ObjectType.Layout.FILE.code.toDouble(),
            fileExt = "pdf"
        )

        val detailsList = Block.Details(
            details = mapOf(
                fileObject.id to Block.Fields(fileObject.map)
            )
        )

        stubOpenDocument(
            document = document,
            details = detailsList
        )

        val vm = buildViewModel()
        advanceUntilIdle()
        vm.onStart(id = fileObject.id, space = defaultSpace)
        advanceUntilIdle()

        vm.startSharingFile(id = fileBlock.id)
        advanceUntilIdle()

        val objName = fieldParser.getObjectName(fileObject)

        verify(documentFileShareDownloader, times(1)).async(
            params = eq(
                MiddlewareShareDownloader.Params(
                    name = objName,
                    objectId = fileObject.id,
                )
            )
        )
    }
}