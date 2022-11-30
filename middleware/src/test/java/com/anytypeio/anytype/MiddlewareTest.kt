package com.anytypeio.anytype

import anytype.ResponseEvent
import anytype.Rpc
import anytype.model.Block
import anytype.model.Range
import com.anytypeio.anytype.core_models.BlockSplitMode
import com.anytypeio.anytype.core_models.Command
import com.anytypeio.anytype.core_models.Position
import com.anytypeio.anytype.middleware.interactor.Middleware
import com.anytypeio.anytype.middleware.interactor.MiddlewareFactory
import com.anytypeio.anytype.middleware.service.MiddlewareService
import com.anytypeio.anytype.test_utils.MockDataFactory
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import kotlin.test.assertEquals
import kotlin.test.assertTrue

typealias CBlock = com.anytypeio.anytype.core_models.Block
typealias CFields = com.anytypeio.anytype.core_models.Block.Fields
typealias CBlockFileType = com.anytypeio.anytype.core_models.Block.Content.File.Type
typealias CBlockPrototypeText = com.anytypeio.anytype.core_models.Block.Prototype.Text
typealias CBlockStyle = com.anytypeio.anytype.core_models.Block.Content.Text.Style

class MiddlewareTest {

    @Mock
    lateinit var service: MiddlewareService

    private lateinit var middleware: Middleware

    private val factory = MiddlewareFactory()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        middleware = Middleware(service, factory, mock(), mock())
    }

    @Test
    fun `should call account-stop method when logging out`() {

        // SETUP

        val request = Rpc.Account.Stop.Request()

        service.stub {
            on { accountStop(request) } doReturn Rpc.Account.Stop.Response()
        }

        // TESTING

        middleware.accountStop(false)

        verify(service, times(1)).accountStop(request)
        verifyNoMoreInteractions(service)
    }

    @Test
    fun `should create request to create new document and return pair of ids`() {

        // SETUP

        val command = Command.CreateDocument(
            context = MockDataFactory.randomUuid(),
            target = MockDataFactory.randomUuid(),
            position = Position.INNER,
            emoji = null,
            type = null,
            layout = null
        )

        val response = Rpc.BlockLink.CreateWithObject.Response(
            blockId = MockDataFactory.randomUuid(),
            targetId = MockDataFactory.randomUuid(),
            event = ResponseEvent()
        )

        val request = Rpc.BlockLink.CreateWithObject.Request(
            contextId = command.context,
            targetId = command.target,
            position = Block.Position.Inner,
            details = mapOf<String, Any?>()
        )

        service.stub {
            on { blockLinkCreateWithObject(any()) } doReturn response
        }

        // TESTING

        val (block, target) = middleware.blockLinkCreateWithObject(command)

        verify(service, times(1)).blockLinkCreateWithObject(request)

        assertEquals(
            expected = response.blockId,
            actual = block
        )

        assertEquals(
            expected = response.targetId,
            actual = target
        )
    }

    @Test
    fun `should create request to create new document with emoji`() {

        // SETUP

        val emoji = "🎒"

        val command = Command.CreateDocument(
            context = MockDataFactory.randomUuid(),
            target = MockDataFactory.randomUuid(),
            position = Position.INNER,
            emoji = emoji,
            type = null,
            layout = null
        )

        val response = Rpc.BlockLink.CreateWithObject.Response(
            blockId = MockDataFactory.randomUuid(),
            targetId = MockDataFactory.randomUuid(),
            event = ResponseEvent()
        )

        val request = Rpc.BlockLink.CreateWithObject.Request(
            contextId = command.context,
            targetId = command.target,
            position = Block.Position.Inner,
            details = mapOf("iconEmoji" to emoji)
        )

        service.stub {
            on { blockLinkCreateWithObject(any()) } doReturn response
        }

        // TESTING

        val (block, target) = middleware.blockLinkCreateWithObject(command)

        verify(service, times(1)).blockLinkCreateWithObject(request)

        assertEquals(
            expected = response.blockId,
            actual = block
        )

        assertEquals(
            expected = response.targetId,
            actual = target
        )
    }

    @Test
    fun `should create a request for replacing target block and return id of the new block`() {

        // SETUP

        val command = Command.Replace(
            context = MockDataFactory.randomUuid(),
            target = MockDataFactory.randomUuid(),
            prototype = CBlockPrototypeText(
                style = CBlockStyle.NUMBERED
            )
        )

        val response = Rpc.Block.Create.Response(
            blockId = MockDataFactory.randomUuid(),
            event = ResponseEvent()
        )

        val model = Block(
            text = Block.Content.Text(
                style = Block.Content.Text.Style.Numbered
            )
        )

        val request = Rpc.Block.Create.Request(
            contextId = command.context,
            targetId = command.target,
            position = Block.Position.Replace,
            block = model
        )

        service.stub {
            on { blockCreate(any()) } doReturn response
        }

        // TESTING

        val result = middleware.blockReplace(command)

        verify(service, times(1)).blockCreate(request)

        assertEquals(
            expected = response.blockId,
            actual = result.first
        )

        verifyNoMoreInteractions(service)
    }

    @Test
    fun `should set emoji icon by updating document details`() {

        // SETUP

        val command = Command.SetDocumentEmojiIcon(
            context = MockDataFactory.randomUuid(),
            emoji = MockDataFactory.randomString()
        )

        val response = Rpc.Object.SetDetails.Response(event = ResponseEvent())

        val emojiIconKey = "iconEmoji"
        val imageIconKey = "iconImage"

        val emojiValue = command.emoji

        val emojiDetail = Rpc.Object.SetDetails.Detail(
            key = emojiIconKey, value_ = emojiValue
        )

        val imageDetail = Rpc.Object.SetDetails.Detail(
            key = imageIconKey
        )

        val request = Rpc.Object.SetDetails.Request(
            contextId = command.context,
            details = listOf(emojiDetail, imageDetail)
        )

        service.stub {
            on { objectSetDetails(any()) } doReturn response
        }

        // TESTING

        middleware.objectSetEmojiIcon(command)

        verify(service, times(1)).objectSetDetails(request)
        verifyNoMoreInteractions(service)
    }

    @Test
    fun `should set image icon by updating document details`() {

        // SETUP

        val command = Command.SetDocumentImageIcon(
            context = MockDataFactory.randomUuid(),
            hash = MockDataFactory.randomUuid()
        )

        val response = Rpc.Object.SetDetails.Response(event = ResponseEvent())

        val imageIconKey = "iconImage"

        val imageIconValue = command.hash

        val imageIconDetail = Rpc.Object.SetDetails.Detail(imageIconKey, imageIconValue)

        val emojiIconKey = "iconEmoji"

        val emojiIconDetail = Rpc.Object.SetDetails.Detail(emojiIconKey)

        val request = Rpc.Object.SetDetails.Request(
            contextId = command.context,
            details = listOf(imageIconDetail, emojiIconDetail)
        )

        service.stub {
            on { objectSetDetails(any()) } doReturn response
        }

        // TESTING

        middleware.objectSetImageIcon(command)

        verify(service, times(1)).objectSetDetails(request)
        verifyNoMoreInteractions(service)
    }

    @Test
    fun `should set document title by updating document details`() {

        // SETUP

        val command = Command.UpdateTitle(
            context = MockDataFactory.randomUuid(),
            title = MockDataFactory.randomString()
        )

        val response = Rpc.Object.SetDetails.Response()

        val key = "name"

        val value = command.title

        val details = Rpc.Object.SetDetails.Detail(key, value)

        val request = Rpc.Object.SetDetails.Request(
            contextId = command.context,
            details = listOf(details)
        )

        service.stub {
            on { objectSetDetails(any()) } doReturn response
        }

        // TESTING

        middleware.objectSetTitle(command)

        verify(service, times(1)).objectSetDetails(request)
        verifyNoMoreInteractions(service)
    }

    @Test
    fun `should update text style for several blocks`() {

        // SETUP

        val command = Command.UpdateStyle(
            context = MockDataFactory.randomString(),
            style = CBlockStyle.CHECKBOX,
            targets = listOf(
                MockDataFactory.randomString(),
                MockDataFactory.randomString()
            )
        )

        val request = Rpc.BlockText.ListSetStyle.Request(
            contextId = command.context,
            blockIds = command.targets,
            style = Block.Content.Text.Style.Checkbox
        )

        service.stub {
            on { blockTextListSetStyle(request) } doReturn
                    Rpc.BlockText.ListSetStyle.Response(
                        event = ResponseEvent()
                    )
        }

        // TESTING

        assertTrue { request.blockIds.size == 2 }
        assertTrue { request.blockIds[0] == command.targets[0] }
        assertTrue { request.blockIds[1] == command.targets[1] }

        middleware.blockTextListSetStyle(command)

        verify(service, times(1)).blockTextListSetStyle(request)
        verifyNoMoreInteractions(service)
    }

    @Test
    fun `should create request for dnd inside home dashboard`() {

        // SETUP

        val context = MockDataFactory.randomUuid()

        val command = Command.Move(
            ctx = context,
            targetContextId = context,
            blockIds = listOf(MockDataFactory.randomUuid()),
            targetId = MockDataFactory.randomUuid(),
            position = Position.TOP
        )

        val position = Block.Position.Top

        val request = Rpc.Block.ListMoveToExistingObject.Request(
            contextId = command.ctx,
            targetContextId = command.ctx,
            position = position,
            blockIds = command.blockIds,
            dropTargetId = command.targetId
        )

        service.stub {
            on { blockListMoveToExistingObject(request) } doReturn  Rpc.Block.ListMoveToExistingObject.Response(event = ResponseEvent())
        }

        // TESTING

        middleware.blockListMoveToExistingObject(command)

        verify(service, times(1)).blockListMoveToExistingObject(request)
        verifyNoMoreInteractions(service)
    }

    @Test
    fun `should create request for clipboard pasting`() {

        // SETUP

        val context = MockDataFactory.randomUuid()

        val command = Command.Paste(
            context = context,
            focus = MockDataFactory.randomUuid(),
            selected = listOf(MockDataFactory.randomUuid(), MockDataFactory.randomUuid()),
            range = 0..5,
            text = MockDataFactory.randomString(),
            html = MockDataFactory.randomString(),
            blocks = emptyList()
        )

        val range = Range(0, 5)

        val request = Rpc.Block.Paste.Request(
            contextId = command.context,
            focusedBlockId = command.focus,
            textSlot = command.text,
            htmlSlot = command.html.orEmpty(),
            selectedTextRange = range,
            selectedBlockIds = command.selected
        )

        service.stub {
            on { blockPaste(request) } doReturn Rpc.Block.Paste.Response(event = ResponseEvent())
        }

        // TESTING

        middleware.blockPaste(command)

        verify(service, times(1)).blockPaste(request)
        verifyNoMoreInteractions(service)
    }

    @Test
    fun `should create request for splitting`() {

        // SETUP

        val context = MockDataFactory.randomUuid()

        val command = Command.Split(
            context = context,
            range = MockDataFactory.randomInt()..MockDataFactory.randomInt(),
            style = CBlockStyle.CHECKBOX,
            target = MockDataFactory.randomUuid(),
            mode = BlockSplitMode.BOTTOM
        )

        val request = Rpc.Block.Split.Request(
            range = Range(command.range.first, command.range.last),
            style = Block.Content.Text.Style.Checkbox,
            contextId = context,
            blockId = command.target,
            mode = Rpc.Block.Split.Request.Mode.BOTTOM
        )

        service.stub {
            on { blockSplit(request) } doReturn Rpc.Block.Split.Response(event = ResponseEvent())
        }

        // TESTING

        middleware.blockSplit(command)

        verify(service, times(1)).blockSplit(request)
        verifyNoMoreInteractions(service)
    }

    @Test
    fun `should create request for uploading file as picture`() {

        // SETUP

        val path = MockDataFactory.randomString()

        val command = Command.UploadFile(
            path = path,
            type = CBlockFileType.IMAGE
        )

        val request = Rpc.File.Upload.Request(
            localPath = path,
            type = Block.Content.File.Type.Image
        )

        service.stub {
            on { fileUpload(request) } doReturn Rpc.File.Upload.Response()
        }

        // TESTING

        middleware.fileUpload(command)

        verify(service, times(1)).fileUpload(request)
        verifyNoMoreInteractions(service)
    }

    @Test
    fun `should create request for uploading file as file`() {

        // SETUP

        val path = MockDataFactory.randomString()

        val command = Command.UploadFile(
            path = path,
            type = CBlockFileType.FILE
        )

        val request = Rpc.File.Upload.Request(
            localPath = path,
            type = Block.Content.File.Type.File
        )

        service.stub {
            on { fileUpload(request) } doReturn Rpc.File.Upload.Response()
        }

        // TESTING

        middleware.fileUpload(command)

        verify(service, times(1)).fileUpload(request)
        verifyNoMoreInteractions(service)
    }

    @Test
    fun `should create request for uploading file as video`() {

        // SETUP

        val path = MockDataFactory.randomString()

        val command = Command.UploadFile(
            path = path,
            type = CBlockFileType.VIDEO
        )

        val request = Rpc.File.Upload.Request(
            localPath = path,
            type = Block.Content.File.Type.Video
        )

        service.stub {
            on { fileUpload(request) } doReturn Rpc.File.Upload.Response()
        }

        // TESTING

        middleware.fileUpload(command)

        verify(service, times(1)).fileUpload(request)
        verifyNoMoreInteractions(service)
    }

    @Test
    fun `should create request for setting block fields`() {

        // SETUP

        val ctx = MockDataFactory.randomUuid()

        val block1 = MockDataFactory.randomUuid()
        val block2 = MockDataFactory.randomUuid()

        val command = Command.SetFields(
            context = ctx,
            fields = listOf(
                Pair(
                    block1,
                    CFields(
                        map = mutableMapOf(
                            "lang" to "kotlin"
                        )
                    )
                ),
                Pair(
                    block2,
                    CFields(
                        map = mutableMapOf(
                            "lang" to "python"
                        )
                    )
                )
            )
        )

        val fields = listOf(
            Rpc.Block.ListSetFields.Request.BlockField(
                blockId = block1,
                fields = mapOf("lang" to "kotlin")
            ),
            Rpc.Block.ListSetFields.Request.BlockField(
                blockId = block2,
                fields = mapOf("lang" to "python")
            )
        )

        val request = Rpc.Block.ListSetFields.Request(
            contextId = ctx,
            blockFields = fields
        )

        service.stub {
            on { blockListSetFields(request) } doReturn Rpc.Block.ListSetFields.Response(event = ResponseEvent())
        }

        // TESTING

        middleware.blockListSetFields(command)

        verify(service, times(1)).blockListSetFields(request)
        verifyNoMoreInteractions(service)
    }
}