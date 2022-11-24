package com.anytypeio.anytype.presentation

import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DV
import com.anytypeio.anytype.core_models.DVViewer
import com.anytypeio.anytype.core_models.DVViewerRelation
import com.anytypeio.anytype.core_models.ObjectWrapper
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.RelationFormat
import com.anytypeio.anytype.core_models.RelationLink
import com.anytypeio.anytype.core_models.Relations
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.test_utils.MockDataFactory

class TypicalTwoRecordObjectSet {

    val title = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Text(
            style = Block.Content.Text.Style.TITLE,
            text = MockDataFactory.randomString(),
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

    @Deprecated("To be deleted")
    val relations = listOf(
        Relation(
            key = MockDataFactory.randomString(),
            name = MockDataFactory.randomString(),
            source = Relation.Source.DETAILS,
            defaultValue = null,
            format = Relation.Format.LONG_TEXT,
            isHidden = false,
            isMulti = false,
            isReadOnly = false,
            selections = emptyList()
        ),
        Relation(
            key = MockDataFactory.randomString(),
            name = MockDataFactory.randomString(),
            source = Relation.Source.DETAILS,
            defaultValue = null,
            format = Relation.Format.LONG_TEXT,
            isHidden = false,
            isMulti = false,
            isReadOnly = false,
            selections = emptyList()
        )
    )

    val relationsObjects = listOf(
        ObjectWrapper.Relation(
            mapOf(
                Relations.RELATION_KEY to relations[0].key,
                Relations.ID to MockDataFactory.randomUuid(),
                Relations.NAME to MockDataFactory.randomUuid(),
                Relations.RELATION_FORMAT to RelationFormat.LONG_TEXT.code.toDouble(),
                Relations.IS_HIDDEN to false,
                Relations.IS_READ_ONLY to false
            )
        ),
        ObjectWrapper.Relation(
            mapOf(
                Relations.RELATION_KEY to relations[1].key,
                Relations.ID to MockDataFactory.randomUuid(),
                Relations.NAME to MockDataFactory.randomUuid(),
                Relations.RELATION_FORMAT to RelationFormat.LONG_TEXT.code.toDouble(),
                Relations.IS_HIDDEN to false,
                Relations.IS_READ_ONLY to false
            )
        )
    )

    val vrelations = relations.map { relation ->
        DVViewerRelation(
            key = relation.key,
            isVisible = true
        )
    }

    val firstRecordId = "firstRecordId"
    val secondRecordId = "secondRecordId"
    val firstRecordName = MockDataFactory.randomString()
    val secondRecordName = MockDataFactory.randomString()
    val firstRecordType = MockDataFactory.randomString()
    val secondRecordType = MockDataFactory.randomString()

    val firstRecord = mapOf(
        ObjectSetConfig.ID_KEY to firstRecordId,
        ObjectSetConfig.NAME_KEY to firstRecordName,
        ObjectSetConfig.TYPE_KEY to firstRecordType,
        relations[0].key to MockDataFactory.randomString(),
        relations[1].key to MockDataFactory.randomString()
    )

    val firstObject = ObjectWrapper.Basic(firstRecord)

    val secondRecord = mapOf(
        ObjectSetConfig.ID_KEY to secondRecordId,
        ObjectSetConfig.NAME_KEY to secondRecordName,
        ObjectSetConfig.TYPE_KEY to secondRecordType,
        relations[0].key to MockDataFactory.randomString(),
        relations[1].key to MockDataFactory.randomString()
    )

    val secondObject = ObjectWrapper.Basic(secondRecord)

    val initialObjects = listOf(firstObject, secondObject)

    val viewer1 = DVViewer(
        id = MockDataFactory.randomUuid(),
        filters = emptyList(),
        sorts = emptyList(),
        type = Block.Content.DataView.Viewer.Type.GRID,
        name = MockDataFactory.randomString(),
        viewerRelations = vrelations
    )

    val viewer2 = DVViewer(
        id = MockDataFactory.randomUuid(),
        filters = emptyList(),
        sorts = emptyList(),
        type = Block.Content.DataView.Viewer.Type.GRID,
        name = MockDataFactory.randomString(),
        viewerRelations = vrelations.mapIndexed { index, viewerRelation ->
            if (index == 0)
                viewerRelation.copy(isVisible = false)
            else
                viewerRelation
        }
    )

    val dv = Block(
        id = MockDataFactory.randomUuid(),
        content = DV(
            sources = listOf(MockDataFactory.randomString()),
            relations = relations,
            relationsIndex = relations.map {
                RelationLink(
                    key = it.key,
                    format = it.format
                )
            },
            viewers = listOf(viewer1, viewer2)
        ),
        children = emptyList(),
        fields = Block.Fields.empty()
    )
}