package com.anytypeio.anytype.presentation.sets

import MockDataFactory
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.Relation

object MockObjectSetFactory {

    val defaultViewerRelations = listOf(
        //Text
        Block.Content.DataView.Viewer.ViewerRelation(
            key = MockDataFactory.randomUuid(),
            isVisible = true
        ),
        //Text
        Block.Content.DataView.Viewer.ViewerRelation(
            key = MockDataFactory.randomUuid(),
            isVisible = true
        ),
        //Number
        Block.Content.DataView.Viewer.ViewerRelation(
            key = MockDataFactory.randomUuid(),
            isVisible = true
        ),
        //Tag
        Block.Content.DataView.Viewer.ViewerRelation(
            key = MockDataFactory.randomUuid(),
            isVisible = true
        ),
        //Check
        Block.Content.DataView.Viewer.ViewerRelation(
            key = MockDataFactory.randomUuid(),
            isVisible = true
        )
    )

    val defaultRelations = listOf(
        Relation(
            key = defaultViewerRelations[0].key,
            name = "Name",
            format = Relation.Format.LONG_TEXT,
            isReadOnly = true,
            isHidden = false,
            isMulti = false,
            source = Relation.Source.DETAILS,
            selections = listOf(),
            defaultValue = null
        ),
        Relation(
            key = defaultViewerRelations[1].key,
            name = "Author Email",
            format = Relation.Format.EMAIL,
            isReadOnly = true,
            isHidden = false,
            isMulti = false,
            source = Relation.Source.DETAILS,
            selections = listOf(),
            defaultValue = null
        ),
        Relation(
            key = defaultViewerRelations[2].key,
            name = "Year",
            format = Relation.Format.NUMBER,
            isReadOnly = true,
            isHidden = true,
            isMulti = false,
            source = Relation.Source.ACCOUNT,
            selections = listOf(),
            defaultValue = null
        ),
        Relation(
            key = defaultViewerRelations[3].key,
            name = "Genre",
            format = Relation.Format.TAG,
            isReadOnly = true,
            isHidden = false,
            isMulti = false,
            source = Relation.Source.DETAILS,
            selections = listOf(),
            defaultValue = null
        ),
        Relation(
            key = defaultViewerRelations[4].key,
            name = "Already Read ",
            format = Relation.Format.CHECKBOX,
            isReadOnly = true,
            isHidden = false,
            isMulti = false,
            source = Relation.Source.DETAILS,
            selections = listOf(),
            defaultValue = null
        )
    )

    fun makeDefaultObjectSet(
        viewerId: String? = null,
        dataViewId: String? = null,
        viewerRelations: List<Block.Content.DataView.Viewer.ViewerRelation>? = null,
        relations: List<Relation>? = null,
        sorts: List<Block.Content.DataView.Sort>? = null,
        filters: List<Block.Content.DataView.Filter>? = null
    ): ObjectSet {

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

        val viewerRelationsDefault = viewerRelations ?: defaultViewerRelations

        val relationsDefault = relations ?: defaultRelations

        val viewerGrid = Block.Content.DataView.Viewer(
            id = viewerId ?: MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            type = Block.Content.DataView.Viewer.Type.GRID,
            viewerRelations = viewerRelationsDefault,
            sorts = sorts ?: listOf(),
            filters = filters ?: listOf()
        )

        val dataView = Block(
            id = dataViewId ?: MockDataFactory.randomString(),
            content = Block.Content.DataView(
                source = "source://1",
                viewers = listOf(viewerGrid),
                relations = relationsDefault
            ),
            fields = Block.Fields.empty(),
            children = listOf()
        )

        val blocks = listOf(title, dataView)

        return ObjectSet(blocks = blocks)
    }
}