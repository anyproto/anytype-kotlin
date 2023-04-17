package com.anytypeio.anytype.features.sets.dv

import androidx.core.os.bundleOf
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.anytypeio.anytype.R
import com.anytypeio.anytype.core_models.Block
import com.anytypeio.anytype.core_models.DVViewerRelation
import com.anytypeio.anytype.core_models.ObjectType
import com.anytypeio.anytype.core_models.Relation
import com.anytypeio.anytype.core_models.SmartBlockType
import com.anytypeio.anytype.emojifier.data.DefaultDocumentEmojiIconProvider
import com.anytypeio.anytype.presentation.MockBlockContentFactory.StubTextContent
import com.anytypeio.anytype.presentation.relations.ObjectSetConfig
import com.anytypeio.anytype.test_utils.MockDataFactory
import com.anytypeio.anytype.test_utils.utils.checkHasText
import com.anytypeio.anytype.test_utils.utils.checkIsRecyclerSize
import com.anytypeio.anytype.test_utils.utils.onItemView
import com.anytypeio.anytype.test_utils.utils.rVMatcher
import com.anytypeio.anytype.ui.sets.ObjectSetFragment
import com.bartoszlipinski.disableanimationsrule.DisableAnimationsRule
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class ObjectSetGridTagCellRenderingTest : TestObjectSetSetup() {

    @get:Rule
    val animationsRule = DisableAnimationsRule()

    override val title: Block = Block(
        id = MockDataFactory.randomUuid(),
        content = StubTextContent(
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
    fun shouldRenderOneSameTagAndTwoDifferentTagsOfTwoRecords() {

        // SETUP

        val relationName = "FilmTags"
        val tag1Name = "Silent film"
        val tag1Id = MockDataFactory.randomUuid()
        val tag2Name = "Sound film"
        val tag2Id = MockDataFactory.randomUuid()
        val tag3Name = "Director"
        val tag3Id = MockDataFactory.randomUuid()

        val objectType = ObjectType(
            url = MockDataFactory.randomUuid(),
            name = "Film",
            relationLinks = emptyList(),
            emoji = MockDataFactory.randomString(),
            layout = ObjectType.Layout.BASIC    ,
            description = "",
            isHidden = false,
            smartBlockTypes = listOf(),
            isArchived = false,
            isReadOnly = false
        )

        val relationId = MockDataFactory.randomUuid()
        val record1Id = MockDataFactory.randomUuid()
        val record2Id = MockDataFactory.randomUuid()

        val record1: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to record1Id,
            ObjectSetConfig.NAME_KEY to "The Face on the Bar Room Floor",
            ObjectSetConfig.EMOJI_KEY to DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random(),
            ObjectSetConfig.TYPE_KEY to objectType.url,
            relationId to listOf(tag1Id, tag3Id)
        )

        val record2: Map<String, Any?> = mapOf(
            ObjectSetConfig.ID_KEY to record2Id,
            ObjectSetConfig.NAME_KEY to "The Great Dictator",
            ObjectSetConfig.EMOJI_KEY to DefaultDocumentEmojiIconProvider.DOCUMENT_SET.random(),
            ObjectSetConfig.TYPE_KEY to objectType.url,
            relationId to listOf(tag2Id, tag3Id)
        )

        val relation = Relation(
            key = relationId,
            name = relationName,
            format = Relation.Format.TAG,
            selections = listOf(
                Relation.Option(id = tag1Id, text = tag1Name, color = "blue"),
                Relation.Option(id = tag2Id, text = tag2Name, color = "red"),
                Relation.Option(id = tag3Id, text = tag3Name, color = "black"),
                Relation.Option(
                    id = MockDataFactory.randomUuid(),
                    text = MockDataFactory.randomString(),
                    color = "black"
                )
            ),
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
                viewers = listOf(viewer),
                
            )
        )

        val details = Block.Details()

        val root = Block(
            id = ctx,
            fields = Block.Fields(emptyMap()),
            content = Block.Content.Smart(),
            children = listOf(header.id, dataview.id)
        )

        val set = listOf(root, header, title, dataview)

        stubInterceptEvents()
        stubInterceptThreadStatus()
        stubOpenObjectSetWithRecord(
            set = set,
            relations = listOf(relation),
            details = details,
            objectTypes = listOf(objectType)
        )

        // TESTING

        launchFragment(bundleOf(ObjectSetFragment.CONTEXT_ID_KEY to ctx))

        with(R.id.rvRows.rVMatcher()) {
            checkIsRecyclerSize(2)
            onItemView(0, R.id.tvTitle).checkHasText("The Face on the Bar Room Floor")
            onItemView(0, R.id.tag0).check(
                ViewAssertions.matches(
                    ViewMatchers.withText("Silent film")
                )
            )
            onItemView(0, R.id.tag1).check(
                ViewAssertions.matches(
                    ViewMatchers.withText("Director")
                )
            )
            onItemView(0, R.id.tag2).check(
                ViewAssertions.matches(
                    not(ViewMatchers.isDisplayed())
                )
            )
            onItemView(1, R.id.tvTitle).checkHasText("The Great Dictator")
            onItemView(1, R.id.tag0).check(
                ViewAssertions.matches(
                    ViewMatchers.withText("Sound film")
                )
            )
            onItemView(1, R.id.tag1).check(
                ViewAssertions.matches(
                    ViewMatchers.withText("Director")
                )
            )
            onItemView(1, R.id.tag2).check(
                ViewAssertions.matches(
                    not(ViewMatchers.isDisplayed())
                )
            )
        }
    }

}