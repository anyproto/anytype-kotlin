package com.anytypeio.anytype.presentation.relations

import MockDataFactory
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.presentation.mapper.toView
import com.anytypeio.anytype.presentation.sets.model.SimpleRelationView
import org.junit.Test
import kotlin.test.assertEquals

class ObjectSetRenderMapperTest {


    @Test
    fun `should return relation view with visibility true`() {

        val viewerRel1 = Block.Content.DataView.Viewer.ViewerRelation(
            key = MockDataFactory.randomUuid(),
            isVisible = true
        )

        val viewerRel2 = Block.Content.DataView.Viewer.ViewerRelation(
            key = MockDataFactory.randomUuid(),
            isVisible = true
        )

        val viewerRel3 = Block.Content.DataView.Viewer.ViewerRelation(
            key = MockDataFactory.randomUuid(),
            isVisible = true
        )

        val viewerRelations = listOf(viewerRel1, viewerRel2, viewerRel3)

        val relation = Relation(
            key = viewerRel2.key,
            name = MockDataFactory.randomString(),
            source = Relation.Source.ACCOUNT,
            format = Relation.Format.NUMBER
        )

        val viewer1 = DVViewer(
            id = MockDataFactory.randomUuid(),
            filters = emptyList(),
            sorts = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID,
            name = MockDataFactory.randomString(),
            viewerRelations = viewerRelations
        )

        val result = viewer1.toViewRelation(relation)

        val expected = SimpleRelationView(
            key = relation.key,
            title = relation.name,
            format = relation.format.toView(),
            isVisible = true,
            isHidden = false
        )

        assertEquals(expected, result)
    }

    @Test
    fun `should return relation view with visibility false`() {

        val viewerRel1 = Block.Content.DataView.Viewer.ViewerRelation(
            key = MockDataFactory.randomUuid(),
            isVisible = true
        )

        val viewerRel2 = Block.Content.DataView.Viewer.ViewerRelation(
            key = MockDataFactory.randomUuid(),
            isVisible = true
        )

        val viewerRel3 = Block.Content.DataView.Viewer.ViewerRelation(
            key = MockDataFactory.randomUuid(),
            isVisible = true
        )

        val viewerRelations = listOf(viewerRel1, viewerRel2, viewerRel3)

        val relation = Relation(
            key = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            source = Relation.Source.ACCOUNT,
            format = Relation.Format.NUMBER
        )

        val viewer1 = DVViewer(
            id = MockDataFactory.randomUuid(),
            filters = emptyList(),
            sorts = emptyList(),
            type = Block.Content.DataView.Viewer.Type.GRID,
            name = MockDataFactory.randomString(),
            viewerRelations = viewerRelations
        )

        val result = viewer1.toViewRelation(relation)

        val expected = SimpleRelationView(
            key = relation.key,
            title = relation.name,
            format = relation.format.toView(),
            isVisible = false,
            isHidden = false
        )

        assertEquals(expected, result)
    }
}