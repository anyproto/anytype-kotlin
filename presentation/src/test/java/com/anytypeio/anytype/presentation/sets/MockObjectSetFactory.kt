package com.anytypeio.anytype.presentation.sets

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.RelationLink
import com.anytypeio.anytype.presentation.sets.state.ObjectState
import com.anytypeio.anytype.test_utils.MockDataFactory

object MockObjectSetFactory {

    private val defaultViewerRelations = listOf(
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

    fun makeDefaultSetObjectState(
        viewerId: String? = null,
        dataViewId: String? = null,
        viewerRelations: List<Block.Content.DataView.Viewer.ViewerRelation>? = null,
        relations: List<ObjectWrapper.Relation> = emptyList(),
        sorts: List<Block.Content.DataView.Sort>? = null,
        filters: List<Block.Content.DataView.Filter>? = null
    ): ObjectState {

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
                viewers = listOf(viewerGrid),
                relationLinks = relations.map {
                    RelationLink(
                        key = it.key,
                        format = it.format
                    )
                }
            ),
            fields = Block.Fields.empty(),
            children = listOf()
        )

        val blocks = listOf(title, dataView)

        return ObjectState.DataView.Set(blocks = blocks)
    }
}