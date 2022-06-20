package com.anytypeio.anytype.presentation

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Block.Content.Link
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.presentation.MockBlockContentFactory.StubLinkContent
import com.anytypeio.anytype.presentation.MockBlockContentFactory.StubTextContent
import com.anytypeio.anytype.test_utils.MockDataFactory

object MockBlockFactory {

    fun divider(style: Block.Content.Divider.Style = Block.Content.Divider.Style.LINE) = Block(
        id = MockDataFactory.randomUuid(),
        fields = Block.Fields.empty(),
        children = emptyList(),
        content = Block.Content.Divider(
            style = style
        )
    )

    fun text(
        id: Id = MockDataFactory.randomUuid(),
        fields: Block.Fields = Block.Fields.empty(),
        children: List<Id> = emptyList(),
        content: Block.Content.Text = StubTextContent()
    ): Block = Block(
        id = id,
        fields = fields,
        children = children,
        content = content
    )

    fun paragraph(
        children: List<Id> = emptyList(),
        text: String = MockDataFactory.randomString(),
    ): Block = Block(
        id = MockDataFactory.randomUuid(),
        fields = Block.Fields.empty(),
        children = children,
        content = StubTextContent(
            style = Block.Content.Text.Style.P,
            text = text
        )
    )

    fun header(children: List<Id>) = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Layout(
            type = Block.Content.Layout.Type.HEADER
        ),
        fields = Block.Fields.empty(),
        children = children
    )

    fun title() = Block(
        id = MockDataFactory.randomUuid(),
        content = StubTextContent(
            style = Block.Content.Text.Style.TITLE
        ),
        children = emptyList(),
        fields = Block.Fields.empty()
    )

    fun makeOnePageWithOneTextBlock(
        root: String,
        child: String,
        style: Block.Content.Text.Style = Block.Content.Text.Style.P,
        backgroundColor: String? = null
    ) = listOf(
        Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(SmartBlockType.PAGE),
            children = listOf(child)
        ),
        Block(
            id = child,
            fields = Block.Fields(emptyMap()),
            content = StubTextContent(
                style = style
            ),
            children = emptyList(),
            backgroundColor = backgroundColor
        )
    )

    fun makeFileBlock(): Block = Block(
        id = MockDataFactory.randomUuid(),
        fields = Block.Fields(emptyMap()),
        content = Block.Content.File(
            hash = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            state = Block.Content.File.State.DONE,
            mime = MockDataFactory.randomString(),
            size = MockDataFactory.randomLong(),
            type = Block.Content.File.Type.FILE
        ),
        children = emptyList()
    )

    fun makeTitleBlock(): Block = Block(
        id = MockDataFactory.randomUuid(),
        fields = Block.Fields(emptyMap()),
        content = StubTextContent(
            style = Block.Content.Text.Style.TITLE
        ),
        children = emptyList()
    )

    fun makeOnePageWithTwoTextBlocks(
        root: String,
        firstChild: String,
        firstChildStyle: Block.Content.Text.Style = Block.Content.Text.Style.P,
        secondChild: String,
        secondChildStyle: Block.Content.Text.Style = Block.Content.Text.Style.P,
        secondChildText: String = MockDataFactory.randomString()
    ) = listOf(
        Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Page(
                style = Block.Content.Page.Style.SET
            ),
            children = listOf(firstChild, secondChild)
        ),
        Block(
            id = firstChild,
            fields = Block.Fields(emptyMap()),
            content = StubTextContent(
                style = firstChildStyle,
            ),
            children = emptyList()
        ),
        Block(
            id = secondChild,
            fields = Block.Fields(emptyMap()),
            content = StubTextContent(
                text = secondChildText,
                style = secondChildStyle,
            ),
            children = emptyList()
        )
    )

    fun link(
        id: String = MockDataFactory.randomUuid(),
        fields: Block.Fields = Block.Fields(emptyMap()),
        content: Link = StubLinkContent(),
        children: List<Id> = emptyList(),
        backgroundColor: String? = null,
    ) = Block(
        id = id,
        fields = fields,
        content = content,
        children = children,
        backgroundColor = backgroundColor
    )

    fun makeOnePageWithTitleAndOnePageLinkBlock(
        rootId: String,
        titleBlockId: String,
        pageBlockId: String
    ) = listOf(
        Block(
            id = rootId,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Page(
                style = Block.Content.Page.Style.SET
            ),
            children = listOf(titleBlockId, pageBlockId)
        ),
        Block(
            id = titleBlockId,
            fields = Block.Fields(emptyMap()),
            content = StubTextContent(
                style = Block.Content.Text.Style.TITLE,
            ),
            children = emptyList()
        ),
        Block(
            id = pageBlockId,
            fields = Block.Fields(emptyMap()),
            content = Link(
                target = MockDataFactory.randomUuid(),
                type = Link.Type.PAGE,
                iconSize = Link.IconSize.SMALL,
                cardStyle = Link.CardStyle.TEXT,
                description = Link.Description.NONE,
                relations = emptySet()
            ),
            children = emptyList()
        )
    )
}