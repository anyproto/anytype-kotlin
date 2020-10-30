package com.anytypeio.anytype

import anytype.Commands.Rpc.*
import anytype.model.Models
import anytype.model.Models.Range
import com.anytypeio.anytype.common.MockDataFactory
import com.anytypeio.anytype.data.auth.model.BlockEntity
import com.anytypeio.anytype.data.auth.model.CommandEntity
import com.anytypeio.anytype.data.auth.model.PositionEntity
import com.anytypeio.anytype.middleware.interactor.Middleware
import com.anytypeio.anytype.middleware.interactor.MiddlewareFactory
import com.anytypeio.anytype.middleware.interactor.MiddlewareMapper
import com.anytypeio.anytype.middleware.service.MiddlewareService
import com.google.protobuf.Struct
import com.google.protobuf.Value
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

        val request = Account.Stop.Request.newBuilder().build()

        service.stub {
            on { accountStop(request) } doReturn Account.Stop.Response.getDefaultInstance()
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

        val response = Block.CreatePage.Response
            .newBuilder()
            .setBlockId(MockDataFactory.randomUuid())
            .setTargetId(MockDataFactory.randomUuid())
            .build()

        val request = Block.CreatePage.Request
            .newBuilder()
            .setContextId(command.context)
            .setTargetId(command.target)
            .setPosition(Models.Block.Position.Inner)
            .setDetails(Struct.getDefaultInstance())
            .build()

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

        val response = Block.CreatePage.Response
            .newBuilder()
            .setBlockId(MockDataFactory.randomUuid())
            .setTargetId(MockDataFactory.randomUuid())
            .build()

        val request = Block.CreatePage.Request
            .newBuilder()
            .setContextId(command.context)
            .setTargetId(command.target)
            .setPosition(Models.Block.Position.Inner)
            .setDetails(
                Struct.newBuilder()
                    .putFields(
                        "iconEmoji",
                        Value.newBuilder().setStringValue(emoji).build()
                    )
                    .build()
            )
            .build()

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

        val response = Block.Create.Response
            .newBuilder()
            .setBlockId(MockDataFactory.randomUuid())
            .build()

        val model = Models.Block
            .newBuilder()
            .setText(
                Models.Block.Content.Text
                    .newBuilder()
                    .setStyle(Models.Block.Content.Text.Style.Numbered)
            )

        val request = Block.Create.Request
            .newBuilder()
            .setContextId(command.context)
            .setTargetId(command.target)
            .setPosition(Models.Block.Position.Replace)
            .setBlock(model)
            .build()

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

        val response = Block.Set.Details.Response.getDefaultInstance()

        val emojiIconKey = "iconEmoji"
        val imageIconKey = "iconImage"

        val emojiValue = Value.newBuilder().setStringValue(command.emoji)

        val emojiDetail = Block.Set.Details.Detail.newBuilder()
            .setKey(emojiIconKey)
            .setValue(emojiValue)

        val imageValue = Value.newBuilder().setStringValue("")

        val imageDetail = Block.Set.Details.Detail.newBuilder()
            .setKey(imageIconKey)
            .setValue(imageValue)

        val request = Block.Set.Details.Request.newBuilder()
            .setContextId(command.context)
            .addDetails(emojiDetail)
            .addDetails(imageDetail)
            .build()

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

        val response = Block.Set.Details.Response.getDefaultInstance()

        val imageIconKey = "iconImage"

        val imageIconValue = Value.newBuilder().setStringValue(command.hash)

        val imageIconDetail = Block.Set.Details.Detail.newBuilder()
            .setKey(imageIconKey)
            .setValue(imageIconValue)

        val emojiIconKey = "iconEmoji"

        val emojiIconValue = Value.newBuilder().setStringValue("")

        val emojiIconDetail = Block.Set.Details.Detail.newBuilder()
            .setKey(emojiIconKey)
            .setValue(emojiIconValue)

        val request = Block.Set.Details.Request.newBuilder()
            .setContextId(command.context)
            .addDetails(imageIconDetail)
            .addDetails(emojiIconDetail)
            .build()

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

        val response = Block.Set.Details.Response.getDefaultInstance()

        val key = "name"

        val value = Value.newBuilder().setStringValue(command.title)

        val details = Block.Set.Details.Detail.newBuilder()
            .setKey(key)
            .setValue(value)

        val request = Block.Set.Details.Request.newBuilder()
            .setContextId(command.context)
            .addDetails(details)
            .build()

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

        val request = BlockList.Set.Text.Style.Request
            .newBuilder()
            .setStyle(Models.Block.Content.Text.Style.Checkbox)
            .addAllBlockIds(command.targets)
            .setContextId(command.context)
            .build()

        service.stub {
            on { blockSetTextStyle(request) } doReturn BlockList.Set.Text.Style.Response.getDefaultInstance()
        }

        // TESTING

        assertTrue { request.blockIdsList.size == 2 }
        assertTrue { request.blockIdsList[0] == command.targets[0] }
        assertTrue { request.blockIdsList[1] == command.targets[1] }

        middleware.updateTextStyle(command)

        verify(service, times(1)).blockSetTextStyle(request)
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

        val position = Models.Block.Position.Top

        val request = BlockList.Move.Request
            .newBuilder()
            .setContextId(command.contextId)
            .setTargetContextId(command.contextId)
            .setPosition(position)
            .addAllBlockIds(command.blockIds)
            .setDropTargetId(command.dropTargetId)
            .build()

        service.stub {
            on { blockListMove(request) } doReturn BlockList.Move.Response.getDefaultInstance()
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

        val range = Range.newBuilder().setFrom(0).setTo(5).build()

        val request = Block.Paste.Request
            .newBuilder()
            .setContextId(command.context)
            .setFocusedBlockId(command.focus)
            .setTextSlot(command.text)
            .setHtmlSlot(command.html)
            .setSelectedTextRange(range)
            .addAllSelectedBlockIds(command.selected)
            .build()

        service.stub {
            on { blockPaste(request) } doReturn Block.Paste.Response.getDefaultInstance()
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

        val request = Block.Split.Request
            .newBuilder()
            .setRange(
                Range
                    .newBuilder()
                    .setFrom(command.range.first)
                    .setTo(command.range.last)
                    .build()
            )
            .setStyle(Models.Block.Content.Text.Style.Checkbox)
            .setContextId(context)
            .setBlockId(command.target)
            .setMode(Block.Split.Request.Mode.BOTTOM)
            .build()

        service.stub {
            on { blockSplit(request) } doReturn Block.Split.Response.getDefaultInstance()
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

        val request = UploadFile.Request
            .newBuilder()
            .setLocalPath(path)
            .setType(Models.Block.Content.File.Type.Image)
            .build()

        service.stub {
            on { uploadFile(request) } doReturn UploadFile.Response.getDefaultInstance()
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

        val request = UploadFile.Request
            .newBuilder()
            .setLocalPath(path)
            .setType(Models.Block.Content.File.Type.File)
            .build()

        service.stub {
            on { uploadFile(request) } doReturn UploadFile.Response.getDefaultInstance()
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

        val request = UploadFile.Request
            .newBuilder()
            .setLocalPath(path)
            .setType(Models.Block.Content.File.Type.Video)
            .build()

        service.stub {
            on { uploadFile(request) } doReturn UploadFile.Response.getDefaultInstance()
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
            BlockList.Set.Fields.Request.BlockField.newBuilder()
                .setBlockId(block1)
                .setFields(
                    Struct.newBuilder()
                        .putFields("lang", Value.newBuilder().setStringValue("kotlin").build())
                )
                .build(),
            BlockList.Set.Fields.Request.BlockField.newBuilder()
                .setBlockId(block2)
                .setFields(
                    Struct.newBuilder()
                        .putFields("lang", Value.newBuilder().setStringValue("python").build())
                )
                .build()
        )

        val request = BlockList.Set.Fields.Request.newBuilder()
            .setContextId(ctx)
            .addAllBlockFields(fields)
            .build()

        service.stub {
            on { blockListSetFields(request) } doReturn BlockList.Set.Fields.Response.getDefaultInstance()
        }

        // TESTING

        middleware.setFields(command)

        verify(service, times(1)).blockListSetFields(request)
        verifyNoMoreInteractions(service)
    }
}