package com.anytypeio.anytype.presentation.editor.editor.file_layout

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.StubFile
import com.anytypeio.anytype.core_models.StubHeader
import com.anytypeio.anytype.core_models.StubObject
import com.anytypeio.anytype.core_models.StubSmartBlock
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.domain.base.Resultat
import com.anytypeio.anytype.core_models.ObjectViewDetails
import com.anytypeio.anytype.presentation.editor.editor.Command
import com.anytypeio.anytype.presentation.editor.editor.EditorPresentationTestSetup
import com.anytypeio.anytype.presentation.editor.editor.ViewState
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
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
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(RobolectricTestRunner::class)
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

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        proceedWithDefaultBeforeTestStubbing()
    }

    //region Editor view state
    @Test
    fun `should change file block to file open block`() = runTest {

        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))
        val fileBlock = StubFile(
            id = "fileBlockId-${MockDataFactory.randomUuid()}",
            targetObjectId = root,
            state = Block.Content.File.State.DONE,
            type = Block.Content.File.Type.FILE
        )
        val page = StubSmartBlock(id = root, children = listOf(header.id, fileBlock.id))
        val document = listOf(page, header, title, fileBlock)

        val fileExt = "pdf"

        val fileObject = StubObject(
            id = root,
            space = defaultSpace,
            name = "fileObjectName-${RandomString.make(5)}",
            layout = ObjectType.Layout.FILE.code.toDouble(),
            fileExt = fileExt
        )

        val detailsList = ObjectViewDetails(
            details = mapOf(
                fileObject.id to fileObject.map
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
                BlockView.Title.File(
                    mode = BlockView.Mode.READ, //in this case for a File Object title is always locked!
                    isFocused = false,
                    id = title.id,
                    text = "${fileObject.name}.$fileExt",
                    icon = ObjectIcon.File(
                        mime = fileObject.fileMimeType,
                        extensions = fileExt
                    )
                ),
                BlockView.ButtonOpenFile.FileButton(
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

        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))
        val fileBlock = StubFile(
            id = "fileBlockId-${MockDataFactory.randomUuid()}",
            targetObjectId = root,
            state = Block.Content.File.State.DONE,
            type = Block.Content.File.Type.IMAGE
        )
        val page = StubSmartBlock(id = root, children = listOf(header.id, fileBlock.id))
        val document = listOf(page, header, title, fileBlock)

        val fileExt = "jpg"

        val fileObject = StubObject(
            id = root,
            space = defaultSpace,
            name = "fileObjectImageName-${RandomString.make(5)}",
            layout = ObjectType.Layout.IMAGE.code.toDouble(),
            fileExt = fileExt
        )

        val detailsList = ObjectViewDetails(
            details = mapOf(
                fileObject.id to fileObject.map
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
                BlockView.Title.Image(
                    isFocused = false,
                    id = title.id,
                    text = title.content.asText().text,
                    mode = BlockView.Mode.READ,
                    icon = ObjectIcon.None
                ),
                BlockView.ButtonOpenFile.ImageButton(
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

        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))
        val fileBlock = StubFile(
            id = "fileBlockId-${MockDataFactory.randomUuid()}",
            targetObjectId = root,
            state = Block.Content.File.State.DONE,
            type = Block.Content.File.Type.FILE
        )
        val page = StubSmartBlock(id = root, children = listOf(header.id, fileBlock.id))
        val document = listOf(page, header, title, fileBlock)

        val fileObject = StubObject(
            id = root,
            space = defaultSpace,
            name = "fileObjectName-${RandomString.make(5)}",
            layout = ObjectType.Layout.FILE.code.toDouble(),
            fileExt = "pdf"
        )

        val detailsList = ObjectViewDetails(
            details = mapOf(
                fileObject.id to fileObject.map
            )
        )

        stubOpenDocument(
            document = document,
            details = detailsList
        )

        val objName = fieldParser.getObjectName(fileObject)

        val downloadParams = MiddlewareShareDownloader.Params(
            objectId = root,
            name = objName
        )

        documentFileShareDownloader.stub {
            onBlocking { async(downloadParams) } doReturn Resultat.success(
                MiddlewareShareDownloader.Response(
                    Uri.EMPTY,
                    ""
                )
            )
        }

        val vm = buildViewModel()
        advanceUntilIdle()
        vm.onStart(id = fileObject.id, space = defaultSpace)
        advanceUntilIdle()

        vm.startSharingFile(id = fileBlock.id)
        advanceUntilIdle()

        verify(documentFileShareDownloader, times(1)).async(
            params = eq(
                MiddlewareShareDownloader.Params(
                    name = objName,
                    objectId = fileObject.id,
                )
            )
        )
    }

    //region Open file click test
    @Test
    fun `should proceed with opening fullscreen image on open image click`() = runTest {

        val title = StubTitle()
        val header = StubHeader(children = listOf(title.id))
        val fileBlock = StubFile(
            id = "fileBlockId-${MockDataFactory.randomUuid()}",
            targetObjectId = root,
            state = Block.Content.File.State.DONE,
            type = Block.Content.File.Type.IMAGE
        )
        val page = StubSmartBlock(id = root, children = listOf(header.id, fileBlock.id))
        val document = listOf(page, header, title, fileBlock)

        val fileObject = StubObject(
            id = root,
            space = defaultSpace,
            name = "imageObjectName-${RandomString.make(5)}",
            layout = ObjectType.Layout.IMAGE.code.toDouble(),
            fileExt = "jpg"
        )

        val detailsList = ObjectViewDetails(
            details = mapOf(
                fileObject.id to fileObject.map
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

        vm.onClickListener(
            clicked = ListenerType.Picture.View(
                obj = fileObject.id,
                target = fileBlock.id
            )
        )
        advanceUntilIdle()

        vm.commands.test().assertValue { value ->
            value is EventWrapper && value.peekContent() == Command.OpenFullScreenImage(
                obj = fileObject.id,
                url = builder.large(fileObject.id)
            )
        }
    }

    //endregion
}