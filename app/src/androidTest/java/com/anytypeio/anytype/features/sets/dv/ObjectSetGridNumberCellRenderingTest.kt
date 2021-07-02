package com.anytypeio.anytype.features.sets.dv

import androidx.core.os.bundleOf
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.emojifier.data.DefaultDocumentEmojiIconProvider
import com.anytypeio.anytype.mocking.MockDataFactory
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.ui.sets.ObjectSetFragment
import com.anytypeio.anytype.utils.checkHasText
import com.anytypeio.anytype.utils.onItemView
import com.anytypeio.anytype.utils.rVMatcher
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class ObjectSetGridNumberCellRenderingTest : TestObjectSetSetup() {

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    override val title: Block = Block(
        id = MockDataFactory.randomUuid(),
        content = Block.Content.Text(
            style = Block.Content.Text.Style.TITLE,
            text = "Data View UI Testing",
            marks = emptyList()
        ),
        children = emptyList(),
        fields = Block.Fields.empty()
    )

    @Before
    override fun setup() {
        super.setup()
    }

    @Test
    fun shouldRenderEmptyNumberCellWhenValueIsNull() {

        // SETUP

        val relationName = "Number"
        val valueText = null

        val relationId = MockDataFactory.randomUuid()
        val record1Id = MockDataFactory.randomUuid()

        val objectType = ObjectType(
            url = MockDataFactory.randomUuid(),
            name = "Movie",
            relations = emptyList(),
            emoji = MockDataFactory.randomString(),
            layout = ObjectType.Layout.PROFILE,
            description = "",
            isHidden = false,
            smartBlockTypes = listOf()
        )

        val record1: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to record1Id,
            ObjectSetConfig.TYPE_KEY to objectType.url,
            ObjectSetConfig.NAME_KEY to "The Great Dictator",
            ObjectSetConfig.EMOJI_KEY to DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random(),
            relationId to valueText
        )

        val relation = Relation(
            key = relationId,
            name = relationName,
            format = Relation.Format.NUMBER,
            source = Relation.Source.values().random()
        )

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = listOf(
                DVViewerRelation(
                    key = relation.key,
                    isVisible = true
                )
            ),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        val dataview = Block(
            id = MockDataFactory.randomUuid(),
            children = emptyList(),
            fields = Block.Fields.empty(),
            content = Block.Content.DataView(
                relations = listOf(relation),
                viewers = listOf(viewer),
                source = MockDataFactory.randomUuid()
            )
        )

        val root = Block(
            id = ctx,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(SmartBlockType.SET),
            children = listOf(header.id, dataview.id)
        )

        val set = listOf(root, header, title, dataview)

        stubInterceptEvents()
        stubOpenObjectSetWithRecord(
            set = set,
            relations = listOf(relation),
            viewer = viewer.id,
            dataview = dataview.id,
            records = listOf(record1),
            total = 1,
            objectTypes = listOf(objectType)
        )

        // TESTING

        launchFragment(bundleOf(ObjectSetFragment.CONTEXT_ID_KEY to ctx))

        with(R.id.rvRows.rVMatcher()) {

            onItemView(0, R.id.tvTitle).checkHasText("The Great Dictator")
            onItemView(0, R.id.tvText).checkHasText("")
        }
    }

    @Test
    fun shouldRenderEmptyNumberCellWhenValueIsString() {

        // SETUP

        val relationName = "Number"
        val valueText = "x2021"

        val relationId = MockDataFactory.randomUuid()
        val record1Id = MockDataFactory.randomUuid()

        val objectType = ObjectType(
            url = MockDataFactory.randomUuid(),
            name = "Movie",
            relations = emptyList(),
            emoji = MockDataFactory.randomString(),
            layout = ObjectType.Layout.PROFILE,
            description = "",
            isHidden = false,
            smartBlockTypes = listOf()
        )

        val record1: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to record1Id,
            ObjectSetConfig.TYPE_KEY to objectType.url,
            ObjectSetConfig.NAME_KEY to "The Great Dictator",
            ObjectSetConfig.EMOJI_KEY to DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random(),
            relationId to valueText
        )

        val relation = Relation(
            key = relationId,
            name = relationName,
            format = Relation.Format.NUMBER,
            source = Relation.Source.values().random()
        )

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = listOf(
                DVViewerRelation(
                    key = relation.key,
                    isVisible = true
                )
            ),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        val dataview = Block(
            id = MockDataFactory.randomUuid(),
            children = emptyList(),
            fields = Block.Fields.empty(),
            content = Block.Content.DataView(
                relations = listOf(relation),
                viewers = listOf(viewer),
                source = MockDataFactory.randomUuid()
            )
        )

        val root = Block(
            id = ctx,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(SmartBlockType.SET),
            children = listOf(header.id, dataview.id)
        )

        val set = listOf(root, header, title, dataview)

        stubInterceptEvents()
        stubOpenObjectSetWithRecord(
            set = set,
            relations = listOf(relation),
            viewer = viewer.id,
            dataview = dataview.id,
            records = listOf(record1),
            total = 1,
            objectTypes = listOf(objectType)
        )

        // TESTING

        launchFragment(bundleOf(ObjectSetFragment.CONTEXT_ID_KEY to ctx))

        with(R.id.rvRows.rVMatcher()) {

            onItemView(0, R.id.tvTitle).checkHasText("The Great Dictator")
            onItemView(0, R.id.tvText).checkHasText("")
        }
    }

    @Test
    fun shouldRenderNumberCellWhenValueIsDoubleString() {

        // SETUP

        val relationName = "Number"
        val valueText = "1234.012"

        val relationId = MockDataFactory.randomUuid()
        val record1Id = MockDataFactory.randomUuid()

        val objectType = ObjectType(
            url = MockDataFactory.randomUuid(),
            name = "Movie",
            relations = emptyList(),
            emoji = MockDataFactory.randomString(),
            layout = ObjectType.Layout.PROFILE,
            description = "",
            isHidden = false,
            smartBlockTypes = listOf()
        )

        val record1: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to record1Id,
            ObjectSetConfig.TYPE_KEY to objectType.url,
            ObjectSetConfig.NAME_KEY to "The Great Dictator",
            ObjectSetConfig.EMOJI_KEY to DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random(),
            relationId to valueText
        )

        val relation = Relation(
            key = relationId,
            name = relationName,
            format = Relation.Format.NUMBER,
            source = Relation.Source.values().random()
        )

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = listOf(
                DVViewerRelation(
                    key = relation.key,
                    isVisible = true
                )
            ),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        val dataview = Block(
            id = MockDataFactory.randomUuid(),
            children = emptyList(),
            fields = Block.Fields.empty(),
            content = Block.Content.DataView(
                relations = listOf(relation),
                viewers = listOf(viewer),
                source = MockDataFactory.randomUuid()
            )
        )

        val root = Block(
            id = ctx,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(SmartBlockType.SET),
            children = listOf(header.id, dataview.id)
        )

        val set = listOf(root, header, title, dataview)

        stubInterceptEvents()
        stubOpenObjectSetWithRecord(
            set = set,
            relations = listOf(relation),
            viewer = viewer.id,
            dataview = dataview.id,
            records = listOf(record1),
            total = 1,
            objectTypes = listOf(objectType)
        )

        // TESTING

        launchFragment(bundleOf(ObjectSetFragment.CONTEXT_ID_KEY to ctx))

        with(R.id.rvRows.rVMatcher()) {

            onItemView(0, R.id.tvTitle).checkHasText("The Great Dictator")
            onItemView(0, R.id.tvText).checkHasText("1234.012")
        }
    }

    @Test
    fun shouldRenderNumberCellWhenValueIsWholeDoubleString() {

        // SETUP

        val relationName = "Number"
        val valueText = "1234.0"

        val relationId = MockDataFactory.randomUuid()
        val record1Id = MockDataFactory.randomUuid()

        val objectType = ObjectType(
            url = MockDataFactory.randomUuid(),
            name = "Movie",
            relations = emptyList(),
            emoji = MockDataFactory.randomString(),
            layout = ObjectType.Layout.PROFILE,
            description = "",
            isHidden = false,
            smartBlockTypes = listOf()
        )

        val record1: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to record1Id,
            ObjectSetConfig.TYPE_KEY to objectType.url,
            ObjectSetConfig.NAME_KEY to "The Great Dictator",
            ObjectSetConfig.EMOJI_KEY to DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random(),
            relationId to valueText
        )

        val relation = Relation(
            key = relationId,
            name = relationName,
            format = Relation.Format.NUMBER,
            source = Relation.Source.values().random()
        )

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = listOf(
                DVViewerRelation(
                    key = relation.key,
                    isVisible = true
                )
            ),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        val dataview = Block(
            id = MockDataFactory.randomUuid(),
            children = emptyList(),
            fields = Block.Fields.empty(),
            content = Block.Content.DataView(
                relations = listOf(relation),
                viewers = listOf(viewer),
                source = MockDataFactory.randomUuid()
            )
        )

        val root = Block(
            id = ctx,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(SmartBlockType.SET),
            children = listOf(header.id, dataview.id)
        )

        val set = listOf(root, header, title, dataview)

        stubInterceptEvents()
        stubOpenObjectSetWithRecord(
            set = set,
            relations = listOf(relation),
            viewer = viewer.id,
            dataview = dataview.id,
            records = listOf(record1),
            total = 1,
            objectTypes = listOf(objectType)
        )

        // TESTING

        launchFragment(bundleOf(ObjectSetFragment.CONTEXT_ID_KEY to ctx))

        with(R.id.rvRows.rVMatcher()) {

            onItemView(0, R.id.tvTitle).checkHasText("The Great Dictator")
            onItemView(0, R.id.tvText).checkHasText("1234")
        }
    }

    @Test
    fun shouldRenderNumberCellWhenValueIsDouble() {

        // SETUP

        val relationName = "Number"
        val valueText = 1234.0564321

        val relationId = MockDataFactory.randomUuid()
        val record1Id = MockDataFactory.randomUuid()

        val objectType = ObjectType(
            url = MockDataFactory.randomUuid(),
            name = "Movie",
            relations = emptyList(),
            emoji = MockDataFactory.randomString(),
            layout = ObjectType.Layout.PROFILE,
            description = "",
            isHidden = false,
            smartBlockTypes = listOf()
        )

        val record1: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to record1Id,
            ObjectSetConfig.TYPE_KEY to objectType.url,
            ObjectSetConfig.NAME_KEY to "The Great Dictator",
            ObjectSetConfig.EMOJI_KEY to DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random(),
            relationId to valueText
        )

        val relation = Relation(
            key = relationId,
            name = relationName,
            format = Relation.Format.NUMBER,
            source = Relation.Source.values().random()
        )

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = listOf(
                DVViewerRelation(
                    key = relation.key,
                    isVisible = true
                )
            ),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        val dataview = Block(
            id = MockDataFactory.randomUuid(),
            children = emptyList(),
            fields = Block.Fields.empty(),
            content = Block.Content.DataView(
                relations = listOf(relation),
                viewers = listOf(viewer),
                source = MockDataFactory.randomUuid()
            )
        )

        val root = Block(
            id = ctx,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(SmartBlockType.SET),
            children = listOf(header.id, dataview.id)
        )

        val set = listOf(root, header, title, dataview)

        stubInterceptEvents()
        stubOpenObjectSetWithRecord(
            set = set,
            relations = listOf(relation),
            viewer = viewer.id,
            dataview = dataview.id,
            records = listOf(record1),
            total = 1,
            objectTypes = listOf(objectType)
        )

        // TESTING

        launchFragment(bundleOf(ObjectSetFragment.CONTEXT_ID_KEY to ctx))

        with(R.id.rvRows.rVMatcher()) {

            onItemView(0, R.id.tvTitle).checkHasText("The Great Dictator")
            onItemView(0, R.id.tvText).checkHasText("1234.0564321")
        }
    }

    @Test
    fun shouldRenderNumberCellWhenValueIsWholeDouble() {

        // SETUP

        val relationName = "Number"
        val valueText = -1234.0

        val relationId = MockDataFactory.randomUuid()
        val record1Id = MockDataFactory.randomUuid()

        val objectType = ObjectType(
            url = MockDataFactory.randomUuid(),
            name = "Movie",
            relations = emptyList(),
            emoji = MockDataFactory.randomString(),
            layout = ObjectType.Layout.PROFILE,
            description = "",
            isHidden = false,
            smartBlockTypes = listOf()
        )

        val record1: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to record1Id,
            ObjectSetConfig.TYPE_KEY to objectType.url,
            ObjectSetConfig.NAME_KEY to "The Great Dictator",
            ObjectSetConfig.EMOJI_KEY to DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random(),
            relationId to valueText
        )

        val relation = Relation(
            key = relationId,
            name = relationName,
            format = Relation.Format.NUMBER,
            source = Relation.Source.values().random()
        )

        val viewer = Block.Content.DataView.Viewer(
            id = MockDataFactory.randomUuid(),
            name = MockDataFactory.randomString(),
            filters = emptyList(),
            sorts = emptyList(),
            viewerRelations = listOf(
                DVViewerRelation(
                    key = relation.key,
                    isVisible = true
                )
            ),
            type = Block.Content.DataView.Viewer.Type.GRID
        )

        val dataview = Block(
            id = MockDataFactory.randomUuid(),
            children = emptyList(),
            fields = Block.Fields.empty(),
            content = Block.Content.DataView(
                relations = listOf(relation),
                viewers = listOf(viewer),
                source = MockDataFactory.randomUuid()
            )
        )

        val root = Block(
            id = ctx,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(SmartBlockType.SET),
            children = listOf(header.id, dataview.id)
        )

        val set = listOf(root, header, title, dataview)

        stubInterceptEvents()
        stubOpenObjectSetWithRecord(
            set = set,
            relations = listOf(relation),
            viewer = viewer.id,
            dataview = dataview.id,
            records = listOf(record1),
            total = 1,
            objectTypes = listOf(objectType)
        )

        // TESTING

        launchFragment(bundleOf(ObjectSetFragment.CONTEXT_ID_KEY to ctx))

        with(R.id.rvRows.rVMatcher()) {

            onItemView(0, R.id.tvTitle).checkHasText("The Great Dictator")
            onItemView(0, R.id.tvText).checkHasText("-1234")
        }
    }
}