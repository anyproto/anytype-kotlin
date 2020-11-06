package com.anytypeio.anytype

import anytype.ResponseEvent
import anytype.Rpc
import anytype.model.Block
import anytype.model.Range
import com.anytypeio.anytype.common.MockDataFactory
import com.anytypeio.anytype.data.auth.model.BlockEntity
import com.anytypeio.anytype.data.auth.model.CommandEntity
import com.anytypeio.anytype.data.auth.model.PositionEntity
import com.anytypeio.anytype.middleware.interactor.Middleware
import com.anytypeio.anytype.middleware.interactor.MiddlewareFactory
import com.anytypeio.anytype.middleware.interactor.MiddlewareMapper
import com.anytypeio.anytype.middleware.service.MiddlewareService
import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MiddlewareTest {

    @Mock
    lateinit var service: MiddlewareService

    private lateinit var middleware: Middleware

    private val mapper = MiddlewareMapper()
    private val factory = MiddlewareFactory()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        middleware = Middleware(service, factory, mapper)
    }

    @Test
    fun `should call account-stop method when logging out`() {

        // SETUP

        val request = Rpc.Account.Stop.Request()

        service.stub {
            on { accountStop(request) } doReturn Rpc.Account.Stop.Response()
        }

        // TESTING

        middleware.logout()

        verify(service, times(1)).accountStop(request)
        verifyNoMoreInteractions(service)
    }

    @Test
    fun `should create request to create new document and return pair of ids`() {

        // SETUP

        val command = CommandEntity.CreateDocument(
            context = MockDataFactory.randomUuid(),
            target = MockDataFactory.randomUuid(),
            position = PositionEntity.INNER,
            emoji = null
        )

        val response = Rpc.Block.CreatePage.Response(
            blockId = MockDataFactory.randomUuid(),
            targetId = MockDataFactory.randomUuid(),
            event = ResponseEvent()
        )

        val request = Rpc.Block.CreatePage.Request(
            contextId = command.context,
            targetId = command.target,
            position = Block.Position.Inner,
            details = mapOf<String, Any?>()
        )

        service.stub {
            on { blockCreatePage(any()) } doReturn response
        }

        // TESTING

        val (block, target) = middleware.createDocument(command)

        verify(service, times(1)).blockCreatePage(request)

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

        val command = CommandEntity.CreateDocument(
            context = MockDataFactory.randomUuid(),
            target = MockDataFactory.randomUuid(),
            position = PositionEntity.INNER,
            emoji = emoji
        )

        val response = Rpc.Block.CreatePage.Response(
            blockId = MockDataFactory.randomUuid(),
            targetId = MockDataFactory.randomUuid(),
            event = ResponseEvent()
        )

        val request = Rpc.Block.CreatePage.Request(
            contextId = command.context,
            targetId = command.target,
            position = Block.Position.Inner,
            details = mapOf("iconEmoji" to emoji)
        )

        service.stub {
            on { blockCreatePage(any()) } doReturn response
        }

        // TESTING

        val (block, target) = middleware.createDocument(command)

        verify(service, times(1)).blockCreatePage(request)

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

        val command = CommandEntity.Replace(
            context = MockDataFactory.randomUuid(),
            target = MockDataFactory.randomUuid(),
            prototype = BlockEntity.Prototype.Text(
                style = BlockEntity.Content.Text.Style.NUMBERED
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

        val result = middleware.replace(command)

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

        val command = CommandEntity.SetDocumentEmojiIcon(
            context = MockDataFactory.randomUuid(),
            target = MockDataFactory.randomUuid(),
            emoji = MockDataFactory.randomString()
        )

        val response = Rpc.Block.Set.Details.Response(event = ResponseEvent())

        val emojiIconKey = "iconEmoji"
        val imageIconKey = "iconImage"

        val emojiValue = command.emoji

        val emojiDetail = Rpc.Block.Set.Details.Detail(
            key = emojiIconKey, value = emojiValue
        )

        val imageValue = ""

        val imageDetail = Rpc.Block.Set.Details.Detail(
            key = imageIconKey
        )

        val request = Rpc.Block.Set.Details.Request(
            contextId = command.context,
            details = listOf(emojiDetail, imageDetail)
        )

        service.stub {
            on { blockSetDetails(any()) } doReturn response
        }

        // TESTING

        middleware.setDocumentEmojiIcon(command)

        verify(service, times(1)).blockSetDetails(request)
        verifyNoMoreInteractions(service)
    }

    @Test
    fun `should set image icon by updating document details`() {

        // SETUP

        val command = CommandEntity.SetDocumentImageIcon(
            context = MockDataFactory.randomUuid(),
            hash = MockDataFactory.randomUuid()
        )

        val response = Rpc.Block.Set.Details.Response(event = ResponseEvent())

        val imageIconKey = "iconImage"

        val imageIconValue = command.hash

        val imageIconDetail = Rpc.Block.Set.Details.Detail(imageIconKey,imageIconValue)

        val emojiIconKey = "iconEmoji"

        val emojiIconDetail = Rpc.Block.Set.Details.Detail(emojiIconKey)

        val request = Rpc.Block.Set.Details.Request(
            contextId = command.context,
            details = listOf(imageIconDetail, emojiIconDetail)
        )

        service.stub {
            on { blockSetDetails(any()) } doReturn response
        }

        // TESTING

        middleware.setDocumentImageIcon(command)

        verify(service, times(1)).blockSetDetails(request)
        verifyNoMoreInteractions(service)
    }

    @Test
    fun `should set document title by updating document details`() {

        // SETUP

        val command = CommandEntity.UpdateTitle(
            context = MockDataFactory.randomUuid(),
            title = MockDataFactory.randomString()
        )

        val response = Rpc.Block.Set.Details.Response()

        val key = "name"

        val value = command.title

        val details = Rpc.Block.Set.Details.Detail(key, value)

        val request = Rpc.Block.Set.Details.Request(
            contextId = command.context,
            details = listOf(details)
        )

        service.stub {
            on { blockSetDetails(any()) } doReturn response
        }

        // TESTING

        middleware.updateDocumentTitle(command)

        verify(service, times(1)).blockSetDetails(request)
        verifyNoMoreInteractions(service)
    }

    @Test
    fun `should update text style for several blocks`() {

        // SETUP

        val command = CommandEntity.UpdateStyle(
            context = MockDataFactory.randomString(),
            style = BlockEntity.Content.Text.Style.CHECKBOX,
            targets = listOf(
                MockDataFactory.randomString(),
                MockDataFactory.randomString()
            )
        )

        val request = Rpc.BlockList.Set.Text.Style.Request(
            contextId = command.context,
            blockIds = command.targets,
            style = Block.Content.Text.Style.Checkbox
        )

        service.stub {
            on { blockListSetTextStyle(request) } doReturn
                    Rpc.BlockList.Set.Text.Style.Response(event = ResponseEvent())
        }

        // TESTING

        assertTrue { request.blockIds.size == 2 }
        assertTrue { request.blockIds[0] == command.targets[0] }
        assertTrue { request.blockIds[1] == command.targets[1] }

        middleware.updateTextStyle(command)

        verify(service, times(1)).blockListSetTextStyle(request)
        verifyNoMoreInteractions(service)
    }

    @Test
    fun `should create request for dnd inside home dashboard`() {

        // SETUP

        val context = MockDataFactory.randomUuid()

        val command = CommandEntity.Move(
            contextId = context,
            dropTargetContextId = context,
            blockIds = listOf(MockDataFactory.randomUuid()),
            dropTargetId = MockDataFactory.randomUuid(),
            position = PositionEntity.TOP
        )

        val position = Block.Position.Top

        val request = Rpc.BlockList.Move.Request(
            contextId = command.contextId,
            targetContextId = command.contextId,
            position = position,
            blockIds = command.blockIds,
            dropTargetId = command.dropTargetId
        )

        service.stub {
            on { blockListMove(request) } doReturn Rpc.BlockList.Move.Response(event = ResponseEvent())
        }

        // TESTING

        middleware.move(command)

        verify(service, times(1)).blockListMove(request)
        verifyNoMoreInteractions(service)
    }

    @Test
    fun `should create request for clipboard pasting`() {

        // SETUP

        val context = MockDataFactory.randomUuid()

        val command = CommandEntity.Paste(
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

        middleware.paste(command)

        verify(service, times(1)).blockPaste(request)
        verifyNoMoreInteractions(service)
    }

    @Test
    fun `should create request for splitting`() {

        // SETUP

        val context = MockDataFactory.randomUuid()

        val command = CommandEntity.Split(
            context = context,
            range = MockDataFactory.randomInt()..MockDataFactory.randomInt(),
            style = BlockEntity.Content.Text.Style.CHECKBOX,
            target = MockDataFactory.randomUuid(),
            mode = BlockEntity.Content.Text.SplitMode.BOTTOM
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

        middleware.split(command)

        verify(service, times(1)).blockSplit(request)
        verifyNoMoreInteractions(service)
    }

    @Test
    fun `should create request for uploading file as picture`() {

        // SETUP

        val path = MockDataFactory.randomString()

        val command = CommandEntity.UploadFile(
            path = path,
            type = BlockEntity.Content.File.Type.IMAGE
        )

        val request = Rpc.UploadFile.Request(
            localPath = path,
            type = Block.Content.File.Type.Image
        )

        service.stub {
            on { uploadFile(request) } doReturn Rpc.UploadFile.Response()
        }

        // TESTING

        middleware.uploadFile(command)

        verify(service, times(1)).uploadFile(request)
        verifyNoMoreInteractions(service)
    }

    @Test
    fun `should create request for uploading file as file`() {

        // SETUP

        val path = MockDataFactory.randomString()

        val command = CommandEntity.UploadFile(
            path = path,
            type = BlockEntity.Content.File.Type.FILE
        )

        val request = Rpc.UploadFile.Request(
            localPath = path,
            type = Block.Content.File.Type.File
        )

        service.stub {
            on { uploadFile(request) } doReturn Rpc.UploadFile.Response()
        }

        // TESTING

        middleware.uploadFile(command)

        verify(service, times(1)).uploadFile(request)
        verifyNoMoreInteractions(service)
    }

    @Test
    fun `should create request for uploading file as video`() {

        // SETUP

        val path = MockDataFactory.randomString()

        val command = CommandEntity.UploadFile(
            path = path,
            type = BlockEntity.Content.File.Type.VIDEO
        )

        val request = Rpc.UploadFile.Request(
            localPath = path,
            type = Block.Content.File.Type.Video
        )

        service.stub {
            on { uploadFile(request) } doReturn Rpc.UploadFile.Response()
        }

        // TESTING

        middleware.uploadFile(command)

        verify(service, times(1)).uploadFile(request)
        verifyNoMoreInteractions(service)
    }

    @Test
    fun `should create request for setting block fields`() {

        // SETUP

        val ctx = MockDataFactory.randomUuid()

        val block1 = MockDataFactory.randomUuid()
        val block2 = MockDataFactory.randomUuid()

        val command = CommandEntity.SetFields(
            context = ctx,
            fields = listOf(
                Pair(
                    block1,
                    BlockEntity.Fields(
                        map = mutableMapOf(
                            "lang" to "kotlin"
                        )
                    )
                ),
                Pair(
                    block2,
                    BlockEntity.Fields(
                        map = mutableMapOf(
                            "lang" to "python"
                        )
                    )
                )
            )
        )

        val fields = listOf(
            Rpc.BlockList.Set.Fields.Request.BlockField(
                blockId = block1,
                fields = mapOf("lang" to "kotlin")
            ),
            Rpc.BlockList.Set.Fields.Request.BlockField(
                blockId = block2,
                fields = mapOf("lang" to "python")
            )
        )

        val request = Rpc.BlockList.Set.Fields.Request(
            contextId = ctx,
            blockFields = fields
        )

        service.stub {
            on { blockListSetFields(request) } doReturn Rpc.BlockList.Set.Fields.Response(event = ResponseEvent())
        }

        // TESTING

        middleware.setFields(command)

        verify(service, times(1)).blockListSetFields(request)
        verifyNoMoreInteractions(service)
    }
}