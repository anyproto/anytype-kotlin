package com.anytypeio.anytype.data.auth.mapper

import com.anytypeio.anytype.data.auth.model.*
import com.anytypeio.anytype.domain.auth.model.Account
import com.anytypeio.anytype.domain.auth.model.Wallet
import com.anytypeio.anytype.domain.block.model.Block
import com.anytypeio.anytype.domain.block.model.BlockSplitMode
import com.anytypeio.anytype.domain.block.model.Command
import com.anytypeio.anytype.domain.block.model.Position
import com.anytypeio.anytype.domain.clipboard.Copy
import com.anytypeio.anytype.domain.clipboard.Paste
import com.anytypeio.anytype.domain.config.Config
import com.anytypeio.anytype.domain.event.model.Event
import com.anytypeio.anytype.domain.event.model.Payload
import com.anytypeio.anytype.domain.page.navigation.DocumentInfo
import com.anytypeio.anytype.domain.page.navigation.PageInfoWithLinks
import com.anytypeio.anytype.domain.page.navigation.PageLinks

fun AccountEntity.toDomain(): Account {
    return Account(
        id = id,
        name = name,
        color = color,
        avatar = null
    )
}

fun Account.toEntity(): AccountEntity {
    return AccountEntity(
        id = id,
        name = name,
        color = color
    )
}

fun WalletEntity.toDomain(): Wallet {
    return Wallet(
        mnemonic = mnemonic
    )
}

fun BlockEntity.toDomain(): Block {
    return Block(
        id = id,
        children = children,
        fields = Block.Fields(map = fields.map.toMap()),
        content = content.toDomain()
    )
}

fun BlockEntity.Details.toDomain(): Block.Details = Block.Details(
    details = details.map { (id, fields) -> id to Block.Fields(map = fields.map) }.toMap()
)

fun BlockEntity.Content.toDomain(): Block.Content = when (this) {
    is BlockEntity.Content.Text -> toDomain()
    is BlockEntity.Content.Page -> toDomain()
    is BlockEntity.Content.Layout -> toDomain()
    is BlockEntity.Content.Link -> toDomain()
    is BlockEntity.Content.Divider -> toDomain()
    is BlockEntity.Content.File -> toDomain()
    is BlockEntity.Content.Icon -> toDomain()
    is BlockEntity.Content.Bookmark -> toDomain()
    is BlockEntity.Content.Smart -> toDomain()
}

fun BlockEntity.Content.File.toDomain(): Block.Content.File {
    return Block.Content.File(
        hash = hash,
        name = name,
        mime = mime,
        size = size,
        type = type?.toDomain(),
        state = state?.toDomain()
    )
}

fun BlockEntity.Content.Bookmark.toDomain(): Block.Content.Bookmark {
    return Block.Content.Bookmark(
        url = url,
        title = title,
        description = description,
        image = image,
        favicon = favicon
    )
}

fun BlockEntity.Content.Icon.toDomain(): Block.Content.Icon = Block.Content.Icon(
    name = name
)

fun BlockEntity.Content.File.Type.toDomain(): Block.Content.File.Type {
    return when (this) {
        BlockEntity.Content.File.Type.NONE -> Block.Content.File.Type.NONE
        BlockEntity.Content.File.Type.FILE -> Block.Content.File.Type.FILE
        BlockEntity.Content.File.Type.IMAGE -> Block.Content.File.Type.IMAGE
        BlockEntity.Content.File.Type.VIDEO -> Block.Content.File.Type.VIDEO
    }
}

fun BlockEntity.Content.File.State.toDomain(): Block.Content.File.State {
    return when (this) {
        BlockEntity.Content.File.State.EMPTY -> Block.Content.File.State.EMPTY
        BlockEntity.Content.File.State.UPLOADING -> Block.Content.File.State.UPLOADING
        BlockEntity.Content.File.State.DONE -> Block.Content.File.State.DONE
        BlockEntity.Content.File.State.ERROR -> Block.Content.File.State.ERROR
    }
}

fun BlockEntity.Content.Text.toDomain(): Block.Content.Text {
    return Block.Content.Text(
        text = text,
        marks = marks.map { it.toDomain() },
        style = Block.Content.Text.Style.valueOf(style.name),
        isChecked = isChecked,
        color = color,
        backgroundColor = backgroundColor,
        align = align?.toDomain()
    )
}

fun BlockEntity.Content.Page.toDomain(): Block.Content.Page {
    return Block.Content.Page(
        style = Block.Content.Page.Style.valueOf(style.name)
    )
}

fun BlockEntity.Content.Link.toDomain(): Block.Content.Link {
    return Block.Content.Link(
        target = target,
        type = Block.Content.Link.Type.valueOf(type.name),
        fields = Block.Fields(map = fields.map.toMap())
    )
}

fun BlockEntity.Content.Layout.toDomain(): Block.Content.Layout {
    return Block.Content.Layout(
        type = Block.Content.Layout.Type.valueOf(type.name)
    )
}

fun Block.Content.Layout.toEntity(): BlockEntity.Content.Layout {
    return BlockEntity.Content.Layout(
        type = BlockEntity.Content.Layout.Type.valueOf(type.name)
    )
}

fun BlockEntity.Content.Divider.toDomain() = Block.Content.Divider


fun BlockEntity.Content.Smart.toDomain() = Block.Content.Smart(
    type = Block.Content.Smart.Type.valueOf(type.name)
)

fun BlockEntity.Content.Text.Mark.toDomain(): Block.Content.Text.Mark {
    return Block.Content.Text.Mark(
        range = range,
        param = param,
        type = Block.Content.Text.Mark.Type.valueOf(type.name)
    )
}

fun Block.toEntity(): BlockEntity {
    return BlockEntity(
        id = id,
        children = children,
        fields = BlockEntity.Fields(map = fields.map.toMutableMap()),
        content = content.toEntity()
    )
}

fun Block.Content.toEntity(): BlockEntity.Content = when (this) {
    is Block.Content.Text -> toEntity()
    is Block.Content.Page -> toEntity()
    is Block.Content.Layout -> toEntity()
    is Block.Content.Link -> toEntity()
    is Block.Content.Divider -> toEntity()
    is Block.Content.File -> toEntity()
    is Block.Content.Icon -> toEntity()
    is Block.Content.Bookmark -> toEntity()
    is Block.Content.Smart -> toEntity()
}

fun Block.Content.File.toEntity(): BlockEntity.Content.File {
    return BlockEntity.Content.File(
        hash = hash,
        name = name,
        mime = mime,
        size = size,
        type = type?.toEntity(),
        state = state?.toEntity()
    )
}

fun Block.Content.Bookmark.toEntity(): BlockEntity.Content.Bookmark {
    return BlockEntity.Content.Bookmark(
        url = url,
        title = title,
        description = description,
        image = image,
        favicon = favicon
    )
}

fun Block.Content.Icon.toEntity(): BlockEntity.Content.Icon = BlockEntity.Content.Icon(
    name = name
)

fun Block.Content.File.Type.toEntity(): BlockEntity.Content.File.Type {
    return when (this) {
        Block.Content.File.Type.NONE -> BlockEntity.Content.File.Type.NONE
        Block.Content.File.Type.FILE -> BlockEntity.Content.File.Type.FILE
        Block.Content.File.Type.IMAGE -> BlockEntity.Content.File.Type.IMAGE
        Block.Content.File.Type.VIDEO -> BlockEntity.Content.File.Type.VIDEO
    }
}

fun Block.Content.File.State.toEntity(): BlockEntity.Content.File.State {
    return when (this) {
        Block.Content.File.State.EMPTY -> BlockEntity.Content.File.State.EMPTY
        Block.Content.File.State.UPLOADING -> BlockEntity.Content.File.State.UPLOADING
        Block.Content.File.State.DONE -> BlockEntity.Content.File.State.DONE
        Block.Content.File.State.ERROR -> BlockEntity.Content.File.State.ERROR
    }
}

fun Block.Content.Text.toEntity(): BlockEntity.Content.Text {
    return BlockEntity.Content.Text(
        text = text,
        marks = marks.map { it.toEntity() },
        style = BlockEntity.Content.Text.Style.valueOf(style.name)
    )
}

fun Block.Content.Page.toEntity(): BlockEntity.Content.Page {
    return BlockEntity.Content.Page(
        style = BlockEntity.Content.Page.Style.valueOf(style.name)
    )
}

fun Block.Content.Link.toEntity(): BlockEntity.Content.Link {
    return BlockEntity.Content.Link(
        target = target,
        type = BlockEntity.Content.Link.Type.valueOf(type.name),
        fields = BlockEntity.Fields(map = fields.map.toMutableMap())
    )
}

fun Block.Content.Divider.toEntity() = BlockEntity.Content.Divider

fun Block.Content.Text.Mark.toEntity(): BlockEntity.Content.Text.Mark {
    return BlockEntity.Content.Text.Mark(
        range = range,
        param = param,
        type = BlockEntity.Content.Text.Mark.Type.valueOf(type.name)
    )
}

fun ConfigEntity.toDomain(): Config {
    return Config(
        home = home,
        profile = profile,
        gateway = gateway
    )
}

fun Command.UpdateText.toEntity(): CommandEntity.UpdateText {
    return CommandEntity.UpdateText(
        contextId = contextId,
        blockId = blockId,
        text = text,
        marks = marks.map { it.toEntity() }
    )
}

fun Command.UpdateStyle.toEntity(): CommandEntity.UpdateStyle = CommandEntity.UpdateStyle(
    context = context,
    targets = targets,
    style = BlockEntity.Content.Text.Style.valueOf(style.name)
)

fun Command.UpdateTextColor.toEntity(): CommandEntity.UpdateTextColor =
    CommandEntity.UpdateTextColor(
        context = context,
        target = target,
        color = color
    )

fun Command.UpdateBackgroundColor.toEntity(): CommandEntity.UpdateBackgroundColor =
    CommandEntity.UpdateBackgroundColor(
        context = context,
        targets = targets,
        color = color
    )

fun Command.UpdateCheckbox.toEntity(): CommandEntity.UpdateCheckbox = CommandEntity.UpdateCheckbox(
    context = context,
    target = target,
    isChecked = isChecked
)

fun Command.Create.toEntity(): CommandEntity.Create {
    return CommandEntity.Create(
        context = context,
        target = target,
        prototype = prototype.toEntity(),
        position = position.toEntity()
    )
}

fun Command.Move.toEntity(): CommandEntity.Move {
    return CommandEntity.Move(
        contextId = contextId,
        dropTargetId = targetId,
        dropTargetContextId = targetContextId,
        blockIds = blockIds,
        position = position.toEntity()
    )
}

fun Command.Unlink.toEntity(): CommandEntity.Unlink = CommandEntity.Unlink(
    context = context,
    targets = targets
)

fun Command.Duplicate.toEntity(): CommandEntity.Duplicate = CommandEntity.Duplicate(
    context = context,
    original = original
)

fun Command.Merge.toEntity(): CommandEntity.Merge = CommandEntity.Merge(
    context = context,
    pair = pair
)

fun Command.Split.toEntity(): CommandEntity.Split = CommandEntity.Split(
    context = context,
    target = target,
    range = range,
    style = BlockEntity.Content.Text.Style.valueOf(style.name),
    mode = when (mode) {
        BlockSplitMode.BOTTOM -> BlockEntity.Content.Text.SplitMode.BOTTOM
        BlockSplitMode.TOP -> BlockEntity.Content.Text.SplitMode.TOP
        BlockSplitMode.INNER -> BlockEntity.Content.Text.SplitMode.INNER
    }
)

fun Command.SetDocumentEmojiIcon.toEntity() = CommandEntity.SetDocumentEmojiIcon(
    target = target,
    context = context,
    emoji = emoji
)

fun Command.SetDocumentImageIcon.toEntity() = CommandEntity.SetDocumentImageIcon(
    hash = hash,
    context = context
)

fun Command.UploadBlock.toEntity(): CommandEntity.UploadBlock = CommandEntity.UploadBlock(
    contextId = contextId,
    blockId = blockId,
    url = url,
    filePath = filePath
)

fun Command.SetupBookmark.toEntity() = CommandEntity.SetupBookmark(
    target = target,
    context = context,
    url = url
)

fun Command.Undo.toEntity() = CommandEntity.Undo(
    context = context
)

fun Command.Redo.toEntity() = CommandEntity.Redo(
    context = context
)

fun Command.ArchiveDocument.toEntity() = CommandEntity.ArchiveDocument(
    context = context,
    target = target,
    isArchived = isArchived
)

fun Command.TurnIntoDocument.toEntity() = CommandEntity.TurnIntoDocument(
    context = context,
    targets = targets
)

fun Command.Paste.toEntity() = CommandEntity.Paste(
    context = context,
    focus = focus,
    text = text,
    html = html,
    selected = selected,
    blocks = blocks.map { it.toEntity() },
    range = range
)

fun Command.Copy.toEntity() = CommandEntity.Copy(
    context = context,
    blocks = blocks.map { it.toEntity() },
    range = range
)

fun Command.CreateDocument.toEntity() = CommandEntity.CreateDocument(
    context = context,
    target = target,
    prototype = prototype.toEntity(),
    position = position.toEntity(),
    emoji = emoji
)

fun Command.Replace.toEntity() = CommandEntity.Replace(
    context = context,
    target = target,
    prototype = prototype.toEntity()
)

fun Command.UpdateTitle.toEntity() = CommandEntity.UpdateTitle(
    context = context,
    title = title
)

fun Command.UpdateAlignment.toEntity(): CommandEntity.UpdateAlignment = CommandEntity.UpdateAlignment(
    context = context,
    targets = targets,
    alignment = alignment.toEntity()
)

fun Command.UploadFile.toEntity(): CommandEntity.UploadFile = CommandEntity.UploadFile(
    path = path,
    type = BlockEntity.Content.File.Type.valueOf(type.name)
)

fun Position.toEntity(): PositionEntity {
    return PositionEntity.valueOf(name)
}

fun PayloadEntity.toDomain(): Payload = Payload(
    context = context,
    events = events.map { it.toDomain() }
)

fun EventEntity.toDomain(): Event {
    return when (this) {
        is EventEntity.Command.ShowBlock -> {
            Event.Command.ShowBlock(
                root = root,
                blocks = blocks.map { it.toDomain() },
                context = context,
                details = details.toDomain(),
                type = Event.Command.ShowBlock.Type.valueOf(type.name)
            )
        }
        is EventEntity.Command.AddBlock -> {
            Event.Command.AddBlock(
                blocks = blocks.map { it.toDomain() },
                context = context
            )
        }
        is EventEntity.Command.UpdateBlockText -> {
            Event.Command.UpdateBlockText(
                id = id,
                text = text,
                context = context
            )
        }
        is EventEntity.Command.UpdateStructure -> {
            Event.Command.UpdateStructure(
                context = context,
                id = id,
                children = children
            )
        }
        is EventEntity.Command.DeleteBlock -> {
            Event.Command.DeleteBlock(
                context = context,
                targets = targets
            )
        }
        is EventEntity.Command.GranularChange -> {
            Event.Command.GranularChange(
                context = context,
                id = id,
                text = text,
                style = if (style != null)
                    Block.Content.Text.Style.valueOf(style.name)
                else
                    null,
                color = color,
                backgroundColor = backgroundColor,
                marks = marks?.map { it.toDomain() },
                alignment = alignment?.toDomain(),
                checked = checked
            )
        }
        is EventEntity.Command.LinkGranularChange -> {
            Event.Command.LinkGranularChange(
                context = context,
                id = id,
                target = target,
                fields = fields?.let { Block.Fields(it.map) }
            )
        }
        is EventEntity.Command.BookmarkGranularChange -> {
            Event.Command.BookmarkGranularChange(
                context = context,
                target = target,
                url = url,
                title = title,
                description = description,
                favicon = faviconHash,
                image = imageHash
            )
        }
        is EventEntity.Command.UpdateFields -> {
            Event.Command.UpdateFields(
                context = context,
                target = target,
                fields = Block.Fields(fields.map)
            )
        }
        is EventEntity.Command.UpdateDetails -> {
            Event.Command.UpdateDetails(
                context = context,
                target = target,
                details = Block.Fields(details.map)
            )
        }
        is EventEntity.Command.UpdateBlockFile -> {
            Event.Command.UpdateFileBlock(
                context = context,
                id = id,
                type = type?.toDomain(),
                state = state?.toDomain(),
                size = size,
                mime = mime,
                hash = hash,
                name = name
            )
        }
    }
}

fun Block.Prototype.toEntity(): BlockEntity.Prototype = when (this) {
    is Block.Prototype.Text -> {
        BlockEntity.Prototype.Text(
            style = BlockEntity.Content.Text.Style.valueOf(this.style.name)
        )
    }
    is Block.Prototype.Page -> {
        BlockEntity.Prototype.Page(
            style = BlockEntity.Content.Page.Style.valueOf(this.style.name)
        )
    }
    is Block.Prototype.Bookmark -> BlockEntity.Prototype.Bookmark
    is Block.Prototype.Divider -> BlockEntity.Prototype.Divider
    is Block.Prototype.File -> {
        BlockEntity.Prototype.File(
            type = BlockEntity.Content.File.Type.valueOf(this.type.name),
            state = BlockEntity.Content.File.State.valueOf(this.state.name)
        )
    }
    is Block.Prototype.Link -> BlockEntity.Prototype.Link(target)
}

fun Block.Prototype.Page.toEntity() = BlockEntity.Prototype.Page(
    style = BlockEntity.Content.Page.Style.valueOf(style.name)
)

fun Block.Align.toEntity(): BlockEntity.Align = when (this) {
    Block.Align.AlignLeft -> BlockEntity.Align.AlignLeft
    Block.Align.AlignCenter -> BlockEntity.Align.AlignCenter
    Block.Align.AlignRight -> BlockEntity.Align.AlignRight
}

fun BlockEntity.Align.toDomain(): Block.Align = when (this) {
    BlockEntity.Align.AlignLeft -> Block.Align.AlignLeft
    BlockEntity.Align.AlignCenter -> Block.Align.AlignCenter
    BlockEntity.Align.AlignRight -> Block.Align.AlignRight
}

fun Response.Clipboard.Paste.toDomain() = Paste.Response(
    blocks = blocks,
    cursor = cursor,
    isSameBlockCursor = isSameBlockCursor,
    payload = payload.toDomain()
)

fun Response.Clipboard.Copy.toDomain() = Copy.Response(
    text = plain,
    html = html,
    blocks = blocks.map { it.toDomain() }
)

fun PageInfoWithLinksEntity.toDomain(): PageInfoWithLinks =
    PageInfoWithLinks(
        id = id,
        documentInfo = docInfo.toDomain(),
        links = this.links.toDomain()
    )

fun PageLinksEntity.toDomain(): PageLinks =
    PageLinks(
        outbound = outbound.map { it.toDomain() },
        inbound = inbound.map { it.toDomain() }
    )

fun DocumentInfoEntity.toDomain(): DocumentInfo = DocumentInfo(
    id = id,
    fields = Block.Fields(fields.map),
    snippet = snippet,
    hasInboundLinks = hasInboundLinks,
    type = DocumentInfo.Type.valueOf(type.name)
)