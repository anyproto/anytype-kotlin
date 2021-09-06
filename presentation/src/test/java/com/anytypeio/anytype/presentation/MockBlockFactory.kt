package com.anytypeio.anytype.presentation

import MockDataFactory
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Id
import com.anytypeio.anytype.core_models.SmartBlockType

object MockBlockFactory {

    fun divider(style: Block.Content.Divider.Style = Block.Content.Divider.Style.LINE) = Block(
        id = MockDataFactory.randomUuid(),
        fields = Block.Fields.empty(),
        children = emptyList(),
        content = Block.Content.Divider(
            style = style
        )
    )

    fun paragraph(
        children: List<Id> = emptyList()
    ) : Block = Block(
        id = MockDataFactory.randomUuid(),
        fields = Block.Fields.empty(),
        children = children,
        content = Block.Content.Text(
            text = MockDataFactory.randomString(),
            marks = emptyList(),
            style = Block.Content.Text.Style.P
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
        content = Block.Content.Text(
            text = MockDataFactory.randomString(),
            style = Block.Content.Text.Style.TITLE,
            marks = emptyList()
        ),
        children = emptyList(),
        fields = Block.Fields.empty()
    )

    fun makeOnePageWithOneTextBlock(
        root: String,
        child: String,
        style: Block.Content.Text.Style = Block.Content.Text.Style.P
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
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = style
            ),
            children = emptyList()
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
        content = Block.Content.Text(
            text = MockDataFactory.randomString(),
            marks = emptyList(),
            style = Block.Content.Text.Style.TITLE
        ),
        children = emptyList()
    )

    fun makeParagraphBlock(): Block = Block(
        id = MockDataFactory.randomUuid(),
        fields = Block.Fields.empty(),
        content = Block.Content.Text(
            text = MockDataFactory.randomString(),
            marks = emptyList(),
            style = Block.Content.Text.Style.P
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
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = firstChildStyle
            ),
            children = emptyList()
        ),
        Block(
            id = secondChild,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = secondChildText,
                marks = emptyList(),
                style = secondChildStyle
            ),
            children = emptyList()
        )
    )

    fun makeOnePageWithThreeTextBlocks(
        root: String,
        firstChild: String,
        firstChildStyle: Block.Content.Text.Style = Block.Content.Text.Style.P,
        secondChild: String,
        secondChildStyle: Block.Content.Text.Style = Block.Content.Text.Style.P,
        thirdChild: String,
        thirdChildStyle: Block.Content.Text.Style = Block.Content.Text.Style.P
    ) = listOf(
        Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Page(
                style = Block.Content.Page.Style.SET
            ),
            children = listOf(firstChild, secondChild, thirdChild)
        ),
        Block(
            id = firstChild,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = firstChildStyle
            ),
            children = emptyList()
        ),
        Block(
            id = secondChild,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = secondChildStyle
            ),
            children = emptyList()
        ),
        Block(
            id = thirdChild,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = thirdChildStyle
            ),
            children = emptyList()
        )
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
            content = Block.Content.Text(
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = Block.Content.Text.Style.TITLE
            ),
            children = emptyList()
        ),
        Block(
            id = pageBlockId,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Link(
                target = MockDataFactory.randomUuid(),
                fields = Block.Fields.empty(),
                type = Block.Content.Link.Type.PAGE
            ),
            children = emptyList()
        )
    )
}