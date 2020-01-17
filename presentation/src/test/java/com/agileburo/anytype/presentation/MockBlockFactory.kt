package com.agileburo.anytype.presentation

import MockDataFactory
import com.agileburo.anytype.domain.block.model.Block

object MockBlockFactory {

    fun makeOnePageWithOneTextBlock(
        root: String,
        child: String,
        style: Block.Content.Text.Style = Block.Content.Text.Style.P
    ) = listOf(
        Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Page(
                style = Block.Content.Page.Style.SET
            ),
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

    fun makeOnePageWithTwoTextBlocks(
        root: String,
        firstChild: String,
        firstChildStyle: Block.Content.Text.Style = Block.Content.Text.Style.P,
        secondChild: String,
        secondChildStyle: Block.Content.Text.Style = Block.Content.Text.Style.P
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
                text = MockDataFactory.randomString(),
                marks = emptyList(),
                style = secondChildStyle
            ),
            children = emptyList()
        )
    )
}