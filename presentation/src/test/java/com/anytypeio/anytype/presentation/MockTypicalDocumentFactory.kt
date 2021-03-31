package com.anytypeio.anytype.presentation

import MockDataFactory
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Document
import com.anytypeio.anytype.core_models.Id

object MockTypicalDocumentFactory {

    val title = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Text(
            text = MockDataFactory.randomString(),
            style = Block.Content.Text.Style.TITLE,
            marks = emptyList()
        ),
        children = emptyList(),
        fields = Block.Fields.empty()
    )

    val header = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Layout(
            type = Block.Content.Layout.Type.HEADER
        ),
        fields = Block.Fields.empty(),
        children = listOf(title.id)
    )

    val a = Block(
        id = MockDataFactory.randomUuid(),
        fields = Block.Fields.empty(),
        children = emptyList(),
        content = Block.Content.Text(
            text = MockDataFactory.randomString(),
            marks = emptyList(),
            style = Block.Content.Text.Style.NUMBERED
        )
    )

    fun page(root: Id): Document {
        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PAGE
            ),
            children = listOf(header.id, a.id)
        )
        return listOf(page, header, title, a)
    }

    fun profile(root: Id): Document {
        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(
                type = Block.Content.Smart.Type.PROFILE
            ),
            children = listOf(header.id, a.id)
        )
        return listOf(page, header, title, a)
    }
}