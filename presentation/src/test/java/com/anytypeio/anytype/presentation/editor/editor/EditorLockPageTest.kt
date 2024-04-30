package com.anytypeio.anytype.presentation.editor.editor

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.core_models.StubBookmark
import com.anytypeio.anytype.core_models.StubTitle
import com.anytypeio.anytype.core_models.ThemeColor
import com.anytypeio.anytype.core_models.ext.content
import com.anytypeio.anytype.core_utils.common.EventWrapper
import com.anytypeio.anytype.presentation.MockBlockFactory
import com.anytypeio.anytype.presentation.editor.editor.listener.ListenerType
import com.anytypeio.anytype.presentation.editor.editor.model.BlockView
import com.anytypeio.anytype.presentation.editor.render.parseThemeBackgroundColor
import com.anytypeio.anytype.presentation.navigation.AppNavigation
import com.anytypeio.anytype.presentation.objects.ObjectIcon
import com.anytypeio.anytype.presentation.util.DefaultCoroutineTestRule
import com.anytypeio.anytype.presentation.util.TXT
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.jraska.livedata.test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.MockitoAnnotations

class EditorLockPageTest : EditorPresentationTestSetup() {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineTestRule = DefaultCoroutineTestRule()

    val title = StubTitle()

    val header = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Layout(
            type = Block.Content.Layout.Type.HEADER
        ),
        fields = Block.Fields.empty(),
        children = listOf(title.id)
    )

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        proceedWithDefaultBeforeTestStubbing()
    }

    @Test
    fun `all views should be in edit mode when locked key is missing`() = runTest {

        // SETUP

        val style = Block.Content.Text.Style.BULLET

        val child = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = style
            ),
            children = emptyList()
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart,
                children = listOf(header.id, child.id)
            ),
            header,
            title,
            child
        )

        stubInterceptEvents()
        stubOpenDocument(page)

        val vm = buildViewModel()

        // TESTING

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        val expected = listOf(
            BlockView.Title.Basic(
                id = title.id,
                text = title.content<TXT>().text,
                mode = BlockView.Mode.EDIT
            ),
            BlockView.Text.Bulleted(
                id = child.id,
                text = child.content<TXT>().text,
                mode = BlockView.Mode.EDIT,
                decorations = listOf(
                    BlockView.Decoration(
                        background = child.parseThemeBackgroundColor()
                    )
                )
            )
        )

        assertEquals(
            expected = expected,
            actual = vm.views
        )
    }

    @Test
    fun `all views should be in edit mode when locked key is set to false`() = runTest {

        // SETUP

        val style = Block.Content.Text.Style.BULLET

        val child = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(
                mapOf(Block.Fields.IS_LOCKED_KEY to true)
            ),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = style
            ),
            children = emptyList()
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields.empty(),
                content = Block.Content.Smart,
                children = listOf(header.id, child.id)
            ),
            header,
            title,
            child
        )

        stubInterceptEvents()
        stubOpenDocument(page)

        val vm = buildViewModel()

        // TESTING

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        val expected = listOf(
            BlockView.Title.Basic(
                id = title.id,
                text = title.content<TXT>().text,
                mode = BlockView.Mode.EDIT
            ),
            BlockView.Text.Bulleted(
                id = child.id,
                text = child.content<TXT>().text,
                mode = BlockView.Mode.EDIT,
                decorations = listOf(
                    BlockView.Decoration(
                        background = child.parseThemeBackgroundColor()
                    )
                )
            )
        )

        assertEquals(
            expected = expected,
            actual = vm.views
        )
    }

    @Test
    fun `all views should be in read mode`() = runTest {

        // SETUP

        val style = Block.Content.Text.Style.BULLET

        val child = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = style
            ),
            children = emptyList()
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(
                    mapOf(Block.Fields.IS_LOCKED_KEY to true)
                ),
                content = Block.Content.Smart,
                children = listOf(header.id, child.id)
            ),
            header,
            title,
            child
        )

        stubInterceptEvents()
        stubOpenDocument(page)

        val vm = buildViewModel()

        // TESTING

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        val expected = listOf(
            BlockView.Title.Basic(
                id = title.id,
                text = title.content<TXT>().text,
                mode = BlockView.Mode.READ
            ),
            BlockView.Text.Bulleted(
                id = child.id,
                text = child.content<TXT>().text,
                mode = BlockView.Mode.READ,
                decorations = listOf(
                    BlockView.Decoration(
                        background = child.parseThemeBackgroundColor()
                    )
                )
            )
        )

        assertEquals(
            expected = expected,
            actual = vm.views
        )
    }

    @Test
    fun `should navigate to target when clicking on link-to-object when page is locked`() = runTest {
        // SETUP
        val link = MockBlockFactory.link()
        val target = link.content.asLink().target

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(
                    mapOf(Block.Fields.IS_LOCKED_KEY to true)
                ),
                content = Block.Content.Smart,
                children = listOf(header.id, link.id)
            ),
            header,
            title,
            link
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubClosePage()
        stubOpenDocument(
            document = page,
            details = Block.Details(
                mapOf(
                    target to Block.Fields(
                        mapOf(
                            Relations.ID to target,
                            Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble()
                        )
                    )
                )
            )
        )

        val vm = buildViewModel()

        // TESTING

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        // TESTING

        val expected = listOf(
            BlockView.Title.Basic(
                id = title.id,
                text = title.content<TXT>().text,
                mode = BlockView.Mode.READ
            ),
            BlockView.LinkToObject.Default.Text(
                id = link.id,
                icon = ObjectIcon.None,
                decorations = listOf(
                    BlockView.Decoration(
                        background = link.parseThemeBackgroundColor()
                    )
                )
            )
        )

        // checking that rendering is correct

        assertEquals(
            expected = expected,
            actual = vm.views
        )

        val testObserver = vm.navigation.test()

        vm.onClickListener(
            ListenerType.LinkToObject(
                target = link.id
            )
        )

        advanceUntilIdle()

        // checking navigation command

        testObserver.assertValue { value ->
            value is EventWrapper && value.peekContent() == AppNavigation.Command.OpenObject(
                target = target,
                space = defaultSpace
            )
        }
    }

    @Test
    fun `should navigate to target when clicking on mention when page is locked`() = runTest {

        // SETUP

        val target = MockDataFactory.randomUuid()

        val paragraph = Block(
            id = MockDataFactory.randomUuid(),
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                style = Block.Content.Text.Style.P,
                marks = listOf(
                    Block.Content.Text.Mark(
                        range = 0..5,
                        param = target,
                        type = Block.Content.Text.Mark.Type.MENTION
                    )
                )
            ),
            children = emptyList()
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(
                    mapOf(Block.Fields.IS_LOCKED_KEY to true)
                ),
                content = Block.Content.Smart,
                children = listOf(header.id, paragraph.id)
            ),
            header,
            title,
            paragraph
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubClosePage()
        stubOpenDocument(
            document = page,
            details = Block.Details(
                mapOf(
                    target to Block.Fields(
                        mapOf(
                            Relations.ID to target,
                            Relations.LAYOUT to ObjectType.Layout.BASIC.code.toDouble()
                        )
                    )
                )
            )
        )

        val vm = buildViewModel()

        // TESTING

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        // TESTING

        val expected = listOf(
            BlockView.Title.Basic(
                id = title.id,
                text = title.content<TXT>().text,
                mode = BlockView.Mode.READ
            ),
            BlockView.Text.Paragraph(
                id = paragraph.id,
                text = paragraph.content<TXT>().text,
                marks = listOf(
                    Markup.Mark.Mention.Base(
                        from = 0,
                        to = 5,
                        param = target,
                        isArchived = false
                    )
                ),
                mode = BlockView.Mode.READ,
                decorations = listOf(
                    BlockView.Decoration(
                        background = paragraph.parseThemeBackgroundColor()
                    )
                )
            )
        )

        // checking that rendering is correct

        assertEquals(
            expected = expected,
            actual = vm.views
        )

        val testObserver = vm.navigation.test()

        vm.onClickListener(
            ListenerType.Mention(
                target = target
            )
        )

        advanceUntilIdle()

        // checking navigation command

        testObserver.assertValue { value ->
            value is EventWrapper && value.peekContent() == AppNavigation.Command.OpenObject(
                target = target,
                space = defaultSpace
            )
        }
    }

    @Test
    fun `should open bookmark when clicking on bookmark when page is locked`() = runTest {

        // SETUP

        val bookmarkUrl = "https://anytype.io"
        val bookmarkDescription = "Operating system for life"
        val bookmarkTitle = "Anytype"
        val bookmarkObjectId = MockDataFactory.randomUuid()

        val bookmark = StubBookmark(
            url = bookmarkUrl,
            description = bookmarkDescription,
            title = bookmarkTitle,
            targetObjectId = bookmarkObjectId,
            state = Block.Content.Bookmark.State.DONE
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(
                    mapOf(Block.Fields.IS_LOCKED_KEY to true)
                ),
                content = Block.Content.Smart,
                children = listOf(header.id, bookmark.id)
            ),
            header,
            title,
            bookmark
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubClosePage()
        stubOpenDocument(
            document = page,
            details = Block.Details(
                mapOf(
                    bookmarkObjectId to Block.Fields(
                        mapOf(
                            Relations.NAME to bookmarkTitle,
                            Relations.DESCRIPTION to bookmarkDescription,
                            Relations.SOURCE to bookmarkUrl
                        )
                    )
                )
            )
        )

        val vm = buildViewModel()

        // TESTING

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        // TESTING

        val expected = listOf(
            BlockView.Title.Basic(
                id = title.id,
                text = title.content<TXT>().text,
                mode = BlockView.Mode.READ
            ),
            BlockView.Media.Bookmark(
                id = bookmark.id,
                url = bookmarkUrl,
                faviconUrl = null,
                imageUrl = null,
                description = bookmarkDescription,
                title = bookmarkTitle,
                mode = BlockView.Mode.READ,
                isPreviousBlockMedia = false,
                decorations = listOf(
                    BlockView.Decoration(
                        background = bookmark.parseThemeBackgroundColor(),
                        style = BlockView.Decoration.Style.Card
                    )
                )
            )
        )

        // checking that rendering is correct

        assertEquals(
            expected = expected,
            actual = vm.views
        )

        val testObserver = vm.commands.test()

        vm.onClickListener(
            ListenerType.Bookmark.View(
                item = expected.last() as BlockView.Media.Bookmark
            )
        )

        advanceUntilIdle()

        // checking browsing command

        testObserver.assertValue { value ->
            value is EventWrapper && value.peekContent() == Command.Browse(bookmarkUrl)
        }
    }

    @Test
    fun `should open file when clicking on file when page is locked`() = runTest {

        // SETUP

        val fileBlockId = MockDataFactory.randomUuid()
        val targetObjectId = MockDataFactory.randomUuid()

        val file = Block(
            id = fileBlockId,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.File(
                targetObjectId = targetObjectId,
                type = Block.Content.File.Type.FILE,
                state = Block.Content.File.State.DONE
            ),
            children = emptyList()
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(
                    mapOf(Block.Fields.IS_LOCKED_KEY to true)
                ),
                content = Block.Content.Smart,
                children = listOf(header.id, fileBlockId)
            ),
            header,
            title,
            file
        )

        val mimeType = "application/pdf"
        val fileName = "file.pdf"
        val fileSize = 1000.0

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(
            document = page,
            details = Block.Details(
                mapOf(
                    targetObjectId to Block.Fields(
                        mapOf(
                            Relations.ID to targetObjectId,
                            Relations.FILE_MIME_TYPE to mimeType,
                            Relations.NAME to fileName,
                            Relations.SIZE_IN_BYTES to fileSize
                        )
                    )
                )
            )
        )

        val vm = buildViewModel()

        // TESTING

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        // TESTING

        val expected = listOf(
            BlockView.Title.Basic(
                id = title.id,
                text = title.content<TXT>().text,
                mode = BlockView.Mode.READ
            ),
            BlockView.Media.File(
                id = fileBlockId,
                mode = BlockView.Mode.READ,
                targetObjectId = targetObjectId,
                mime = mimeType,
                name = fileName,
                size = fileSize.toLong(),
                url = builder.file(targetObjectId),
                decorations = listOf(
                    BlockView.Decoration(
                        background = ThemeColor.DEFAULT,
                        style = BlockView.Decoration.Style.None
                    )
                )
            )
        )

        // checking that rendering is correct

        assertEquals(
            expected = expected,
            actual = vm.views
        )

        val testObserver = vm.commands.test()

        vm.onClickListener(ListenerType.File.View(fileBlockId))

        advanceUntilIdle()

        testObserver.assertValue { value ->
            value is EventWrapper && value.peekContent() == Command.OpenFileByDefaultApp(
                id = fileBlockId,
                uri = builder.file(targetObjectId)
            )
        }
    }

    @Test
    fun `should open picture in fullscreen when clicking on file when page is locked`() = runTest {

        // SETUP

        val fileBlockId = MockDataFactory.randomUuid()
        val targetObjectId = MockDataFactory.randomUuid()
        val mimeType = "*/png"
        val fileName = "image.png"
        val fileSize = 1000.0

        val picture = Block(
            id = fileBlockId,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.File(
                targetObjectId = targetObjectId,
                type = Block.Content.File.Type.IMAGE,
                state = Block.Content.File.State.DONE
            ),
            children = emptyList()
        )

        val page = listOf(
            Block(
                id = root,
                fields = Block.Fields(
                    mapOf(Block.Fields.IS_LOCKED_KEY to true)
                ),
                content = Block.Content.Smart,
                children = listOf(header.id, picture.id)
            ),
            header,
            title,
            picture
        )

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenDocument(
            document = page,
            details = Block.Details(
                mapOf(
                    targetObjectId to Block.Fields(
                        mapOf(
                            Relations.ID to targetObjectId,
                            Relations.FILE_MIME_TYPE to mimeType,
                            Relations.NAME to fileName,
                            Relations.SIZE_IN_BYTES to fileSize
                        )
                    )
                )
            )
        )

        val vm = buildViewModel()

        // TESTING

        vm.onStart(id = root, space = defaultSpace)

        advanceUntilIdle()

        // TESTING

        val expected = listOf(
            BlockView.Title.Basic(
                id = title.id,
                text = title.content<TXT>().text,
                mode = BlockView.Mode.READ
            ),
            BlockView.Media.Picture(
                id = picture.id,
                mode = BlockView.Mode.READ,
                targetObjectId = targetObjectId,
                mime = mimeType,
                name = fileName,
                size = fileSize.toLong(),
                url = builder.image(targetObjectId),
                indent = 0,
                decorations = listOf(
                    BlockView.Decoration(
                        background = ThemeColor.DEFAULT,
                        style = BlockView.Decoration.Style.Card
                    )
                )
            )
        )

        // checking that rendering is correct

        assertEquals(
            expected = expected,
            actual = vm.views
        )

        val testObserver = vm.commands.test()

        vm.onClickListener(ListenerType.Picture.View(picture.id))

        advanceUntilIdle()

        // checking open-by-default-app command

        testObserver.assertValue { value ->
            value is EventWrapper && value.peekContent() == Command.OpenFullScreenImage(
                target = picture.id,
                url = builder.original(targetObjectId)
            )
        }
    }
}