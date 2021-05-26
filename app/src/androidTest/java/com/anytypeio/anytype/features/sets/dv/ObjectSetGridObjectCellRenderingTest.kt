package com.anytypeio.anytype.features.sets.dv

import androidx.core.os.bundleOf
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.*
import com.anytypeio.anytype.emojifier.data.DefaultDocumentEmojiIconProvider
import com.anytypeio.anytype.mocking.MockDataFactory
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.ui.sets.ObjectSetFragment
import com.anytypeio.anytype.utils.checkHasText
import com.anytypeio.anytype.utils.checkIsRecyclerSize
import com.anytypeio.anytype.utils.onItemView
import com.anytypeio.anytype.utils.rVMatcher
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class ObjectSetGridObjectCellRenderingTest : TestObjectSetSetup() {

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
    fun shouldRenderOneHumanObjectFromEachOfTwoRecords() {

        // SETUP

        val relationName = "Starring"
        val object1Name = "Charlie Chaplin"
        val object1Id = MockDataFactory.randomUuid()
        val object2Name = "Jean-Pierre Léaud"
        val object2Id = MockDataFactory.randomUuid()

        val objectType = ObjectType(
            url = MockDataFactory.randomUuid(),
            name = "Movie",
            relations = emptyList(),
            emoji = MockDataFactory.randomString(),
            layout = ObjectType.Layout.PROFILE,
            description = "",
            isHidden = false
        )

        val relationId = MockDataFactory.randomUuid()
        val record1Id = MockDataFactory.randomUuid()
        val record2Id = MockDataFactory.randomUuid()

        val record1: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to record1Id,
            ObjectSetConfig.NAME_KEY to "The Great Dictator",
            ObjectSetConfig.EMOJI_KEY to DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random(),
            ObjectSetConfig.TYPE_KEY to objectType.url,
            relationId to object1Id
        )

        val record2: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to record2Id,
            ObjectSetConfig.NAME_KEY to "Les Quatre Cents Coups",
            ObjectSetConfig.EMOJI_KEY to DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random(),
            ObjectSetConfig.TYPE_KEY to objectType.url,
            relationId to object2Id
        )

        val relation = Relation(
            key = relationId,
            name = relationName,
            format = Relation.Format.OBJECT,
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

        val details = Block.Details(
            details = defaultDetails.details + mapOf(
                object1Id to Block.Fields(
                    mapOf(
                        ObjectSetConfig.NAME_KEY to object1Name,
                        ObjectSetConfig.TYPE_KEY to objectType.url,
                        "iconEmoji" to "👤"
                    )
                ),
                object2Id to Block.Fields(
                    mapOf(
                        ObjectSetConfig.NAME_KEY to object2Name,
                        ObjectSetConfig.TYPE_KEY to objectType.url,
                        "iconEmoji" to "👤"
                    )
                )
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
            details = details,
            viewer = viewer.id,
            dataview = dataview.id,
            records = listOf(record1, record2),
            total = 1,
            objectTypes = listOf(objectType)
        )

        // TESTING

        launchFragment(bundleOf(ObjectSetFragment.CONTEXT_ID_KEY to ctx))

        with(R.id.rvRows.rVMatcher()) {
            checkIsRecyclerSize(2)
            onItemView(0, R.id.tvTitle).checkHasText("The Great Dictator")
            onItemView(0, R.id.object0).check(matches(hasDescendant(withText(object1Name))))
            onItemView(1, R.id.tvTitle).checkHasText("Les Quatre Cents Coups")
            onItemView(1, R.id.object0).check(matches(hasDescendant(withText(object2Name))))
        }
    }

    @Test
    fun shouldRenderTwoHumanObjectsFromOneRecord() {

        // SETUP

        val relationName = "Starring"
        val object1Name = "Maurice Ronet"
        val object1Id = MockDataFactory.randomUuid()
        val object2Name = "Jeanne Moreau"
        val object2Id = MockDataFactory.randomUuid()

        val objectType = ObjectType(
            url = MockDataFactory.randomUuid(),
            name = "Movie",
            relations = emptyList(),
            emoji = MockDataFactory.randomString(),
            layout = ObjectType.Layout.PROFILE,
            description = "",
            isHidden = false
        )

        val relationId = MockDataFactory.randomUuid()
        val recordId = MockDataFactory.randomUuid()

        val record1: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to recordId,
            ObjectSetConfig.NAME_KEY to "Le Feu Follet",
            ObjectSetConfig.EMOJI_KEY to DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random(),
            ObjectSetConfig.TYPE_KEY to objectType.url,
            relationId to listOf(object1Id, object2Id)
        )

        val relation = Relation(
            key = relationId,
            name = relationName,
            format = Relation.Format.OBJECT,
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

        val details = Block.Details(
            details = defaultDetails.details + mapOf(
                object1Id to Block.Fields(
                    mapOf(
                        ObjectSetConfig.NAME_KEY to object1Name,
                        ObjectSetConfig.TYPE_KEY to objectType.url,
                        "iconEmoji" to "👤"
                    )
                ),
                object2Id to Block.Fields(
                    mapOf(
                        ObjectSetConfig.NAME_KEY to object2Name,
                        ObjectSetConfig.TYPE_KEY to objectType.url,
                        "iconEmoji" to "👤"
                    )
                )
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
            details = details,
            viewer = viewer.id,
            dataview = dataview.id,
            records = listOf(record1),
            total = 1,
            objectTypes = listOf(objectType)
        )

        // TESTING

        launchFragment(bundleOf(ObjectSetFragment.CONTEXT_ID_KEY to ctx))

        with(R.id.rvRows.rVMatcher()) {
            checkIsRecyclerSize(1)
            onItemView(0, R.id.tvTitle).checkHasText("Le Feu Follet")
            onItemView(0, R.id.object0).check(matches(hasDescendant(withText(object1Name))))
            onItemView(0, R.id.object1).check(matches(hasDescendant(withText(object2Name))))
        }
    }
}