package com.anytypeio.anytype.presentation

import MockDataFactory
import com.anytypeio.anytype.core_models.*

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
            content = Block.Content.Smart(SmartBlockType.PAGE),
            children = listOf(header.id, a.id)
        )
        return listOf(page, header, title, a)
    }

    fun profile(root: Id): Document {
        val page = Block(
            id = root,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(SmartBlockType.PROFILE_PAGE),
            children = listOf(header.id, a.id)
        )
        return listOf(page, header, title, a)
    }

    fun relation(name: String) = Relation(
        key = MockDataFactory.randomString(),
        name = name,
        format = Relation.Format.SHORT_TEXT,
        source = Relation.Source.values().random()
    )

    fun objectType(name: String) = ObjectType(
        url = MockDataFactory.randomUuid(),
        name = name,
        relations = emptyList(),
        layout = ObjectType.Layout.values().random(),
        emoji = MockDataFactory.randomString(),
        description = MockDataFactory.randomString(),
        isHidden = MockDataFactory.randomBoolean(),
        smartBlockTypes = listOf(SmartBlockType.PAGE),
        isArchived = false,
        isReadOnly = false
    )
}